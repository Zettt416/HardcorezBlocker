package com.hardcorezblocker.app.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.data.model.*
import com.hardcorezblocker.app.ui.screens.*
import com.hardcorezblocker.app.ui.theme.*
import com.hardcorezblocker.app.ui.viewmodel.MainViewModel

import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.i18n.Strings

enum class NavigationTab(val icon: ImageVector) {
    DASHBOARD(Icons.Default.Shield),
    INBOX(Icons.AutoMirrored.Filled.Chat),
    RULES(Icons.AutoMirrored.Filled.Rule),
    LOGS(Icons.Default.History),
    SETTINGS(Icons.Default.Settings);

    fun getTitle(strings: Strings): String = when (this) {
        DASHBOARD -> strings.tabDashboard
        INBOX -> strings.tabInbox
        RULES -> strings.tabRules
        LOGS -> strings.tabLogs
        SETTINGS -> strings.tabSettings
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val callScreeningRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val strings = com.hardcorezblocker.app.ui.i18n.getAppStrings(viewModel.settings.value.language)
            Toast.makeText(this, strings.toastRoleSuccess, Toast.LENGTH_SHORT).show()
        }
        viewModel.checkCallScreeningRole()
    }

    private val smsRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.checkDefaultSmsRole()
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.checkCallScreeningRole()
        viewModel.checkDefaultSmsRole()
        viewModel.refreshInbox()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (viewModel.isOnboardingCompleted.value) {
            requestAppPermissions()
        }

        val targetTab = intent?.getStringExtra("EXTRA_TAB")
        val initialTab = listOf(NavigationTab.DASHBOARD, NavigationTab.INBOX, NavigationTab.RULES, NavigationTab.LOGS, NavigationTab.SETTINGS).find { it.name.equals(targetTab, ignoreCase = true) } ?: NavigationTab.DASHBOARD

        setContent {
            val settings by viewModel.settings.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
            var isSplashActive by remember { mutableStateOf(!viewModel.isSplashShown()) }

            HardcorezBlockerTheme(
                themeMode = settings.themeMode,
                language = settings.language
            ) {
                if (isSplashActive) {
                    SplashScreen(
                        onTimeout = {
                            viewModel.markSplashShown()
                            isSplashActive = false
                        }
                    )
                } else if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinished = {
                            viewModel.completeOnboarding()
                            requestAppPermissions()
                        }
                    )
                } else {
                    MainAppScreen(
                        viewModel = viewModel,
                        initialTab = initialTab,
                        onRequestRole = { requestCallScreeningRole() },
                        onRequestSmsRole = { requestSmsRole() },
                        onRequestContactsPermission = {
                            permissionsLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                        },
                        onRequestSmsPermission = {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCallScreeningRole()
        viewModel.checkDefaultSmsRole()
        viewModel.refreshInbox()
    }

    private fun requestSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    smsRoleLauncher.launch(intent)
                    return
                }
            }
        }
        Toast.makeText(this, "Izin Default SMS aktif", Toast.LENGTH_SHORT).show()
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    callScreeningRoleLauncher.launch(intent)
                    return
                }
            }
        }
        Toast.makeText(this, "Call Screening sudah aktif di sistem Anda", Toast.LENGTH_SHORT).show()
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    initialTab: NavigationTab = NavigationTab.DASHBOARD,
    onRequestRole: () -> Unit,
    onRequestSmsRole: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onRequestSmsPermission: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var selectedTab by remember { mutableStateOf(initialTab) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (selectedTab != NavigationTab.DASHBOARD) {
            selectedTab = NavigationTab.DASHBOARD
            lastBackPressTime = currentTime
            Toast.makeText(context, strings.toastPressBackAgainToExit, Toast.LENGTH_SHORT).show()
        } else {
            if (currentTime - lastBackPressTime < 2000L) {
                (context as? ComponentActivity)?.finish()
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, strings.toastPressBackAgainToExit, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val settings by viewModel.settings.collectAsState()
    val inboxMessages by viewModel.inboxMessages.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val unreadThreats = remember(inboxMessages) {
        inboxMessages.filter { it.category == SmsSafetyCategory.SPAM_OR_PHISHING && !it.isRead }
    }
    val unreadThreatCount = unreadThreats.size
    val blockedCount = logs.size

    LaunchedEffect(Unit) {
        val intent = (context as? android.app.Activity)?.intent
        if (intent?.getBooleanExtra("FILTER_THREATS", false) == true) {
            selectedTab = NavigationTab.INBOX
        }
    }

    LaunchedEffect(unreadThreats, settings.sendNotificationOnBlock, settings.notifyThreatSms) {
        if (settings.sendNotificationOnBlock && settings.notifyThreatSms) {
            com.hardcorezblocker.app.HardcorezBlockerApp.syncThreatNotification(context, unreadThreats)
        } else {
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(com.hardcorezblocker.app.HardcorezBlockerApp.THREAT_NOTIFICATION_ID)
            com.hardcorezblocker.app.HardcorezBlockerApp.updateLauncherBadge(context, 0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurfaceElevated,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                listOf(NavigationTab.DASHBOARD, NavigationTab.INBOX, NavigationTab.RULES, NavigationTab.LOGS, NavigationTab.SETTINGS).forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabTitle = tab.getTitle(strings)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            BadgedBox(
                                badge = {
                                    when (tab) {
                                        NavigationTab.INBOX -> {
                                            if (unreadThreatCount > 0) {
                                                Badge(
                                                    containerColor = DangerRed,
                                                    contentColor = androidx.compose.ui.graphics.Color.White
                                                ) {
                                                    Text(
                                                        text = if (unreadThreatCount > 99) "99+" else "$unreadThreatCount",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        NavigationTab.LOGS -> {
                                            if (blockedCount > 0) {
                                                Badge(
                                                    containerColor = PrimaryBlue,
                                                    contentColor = androidx.compose.ui.graphics.Color.White
                                                ) {
                                                    Text(
                                                        text = if (blockedCount > 99) "99+" else "$blockedCount",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        else -> Unit
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tabTitle,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tabTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = DarkBackground
        ) {
            when (selectedTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onRequestRole = onRequestRole,
                    onNavigateToTab = { selectedTab = it }
                )
                NavigationTab.INBOX -> {
                    LaunchedEffect(Unit) {
                        viewModel.refreshInbox()
                    }
                    val inboxMessages by viewModel.inboxMessages.collectAsState()
                    val isDefaultSms by viewModel.isDefaultSms.collectAsState()
                    InboxScreen(
                        messages = inboxMessages,
                        isDefaultSms = isDefaultSms,
                        onRequestSmsRole = onRequestSmsRole,
                        onRefresh = { viewModel.refreshInbox() },
                        onMarkAsRead = { id, isMms -> viewModel.markAsRead(id, isMms) },
                        onMarkAllAsRead = { viewModel.markAllAsRead() },
                        onDeleteBatch = { ids ->
                            viewModel.deleteSmsBatch(ids) { deletedCount ->
                                Toast.makeText(
                                    viewModel.getApplication(),
                                    strings.toastSmsDeleted(deletedCount),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onBlockSender = { sender ->
                            viewModel.addRule(
                                pattern = sender,
                                type = RuleType.EXACT,
                                channel = TargetChannel.BOTH,
                                label = if (viewModel.settings.value.language == com.hardcorezblocker.app.data.model.AppLanguage.ENGLISH) "Blocked from SMS" else "Blokir dari SMS"
                            )
                            Toast.makeText(
                                viewModel.getApplication(),
                                strings.toastRuleAdded,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSendReply = { recipient, text ->
                            viewModel.sendSmsReply(recipient, text) { success ->
                                Toast.makeText(
                                    viewModel.getApplication(),
                                    if (success) strings.smsReplySentToast else strings.smsReplyFailedToast,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
                NavigationTab.RULES -> RulesScreen(
                    viewModel = viewModel
                )
                NavigationTab.LOGS -> LogsScreen(
                    viewModel = viewModel
                )
                NavigationTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onRequestRole = onRequestRole,
                    onRequestContactsPermission = onRequestContactsPermission,
                    onRequestSmsPermission = onRequestSmsPermission
                )
            }
        }
    }
}
