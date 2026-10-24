package com.hardcorezblocker.app.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hardcorezblocker.app.data.model.AppLanguage
import com.hardcorezblocker.app.data.model.AppThemeMode
import com.hardcorezblocker.app.ui.components.*
import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.theme.*
import com.hardcorezblocker.app.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onRequestRole: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onRequestSmsPermission: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isRoleHeld by viewModel.isRoleHeld.collectAsState()
    val rules by viewModel.rules.collectAsState()

    var exportedJsonPreview by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val hasContactsPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    val hasSmsPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    ScreenContainer(scrollable = true) {
        // AppHeader
        AppHeader(
            title = strings.settingsTitle,
            subtitle = strings.settingsSubtitle
        )

        // --- 1. SECTION: TEMA TAMPILAN (LIGHT / DARK / SYSTEM) ---
        Text(
            text = strings.settingsSecTheme,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = strings.settingsThemeVisualMode,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = strings.settingsThemeVisualDesc,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Theme Selector (3 Kapsul)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        listOf(
                            Triple(AppThemeMode.LIGHT, strings.settingsThemeLight, Icons.Default.LightMode),
                            Triple(AppThemeMode.DARK, strings.settingsThemeDark, Icons.Default.DarkMode),
                            Triple(AppThemeMode.SYSTEM, strings.settingsThemeSystem, Icons.Default.BrightnessAuto)
                        ).forEach { (mode, label, icon) ->
                            val isSelected = settings.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) PrimaryBlue.copy(alpha = 0.45f) else Color.Transparent,
                                        shape = RoundedCornerShape(9.dp)
                                    )
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryBlue else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryBlue else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. SECTION: BAHASA APLIKASI (INDONESIAN / ENGLISH) ---
        Text(
            text = strings.settingsSecLanguage,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = strings.settingsLanguageTitle,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = strings.settingsLanguageDesc,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Language Selector (2 Kapsul: ID & EN)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        listOf(
                            Pair(AppLanguage.INDONESIAN, strings.settingsLangId),
                            Pair(AppLanguage.ENGLISH, strings.settingsLangEn)
                        ).forEach { (lang, label) ->
                            val isSelected = settings.language == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) PrimaryBlue.copy(alpha = 0.45f) else Color.Transparent,
                                        shape = RoundedCornerShape(9.dp)
                                    )
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PrimaryBlue else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. SECTION: STATUS HAK AKSES SISTEM ---
        Text(
            text = strings.settingsSecPermissions,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                PermissionStatusRow(
                    title = strings.settingsPermRole,
                    description = strings.onbPermRoleDesc,
                    isGranted = isRoleHeld,
                    onGrantClick = onRequestRole,
                    grantedLabel = strings.settingsPermRoleActive,
                    missingLabel = strings.settingsPermRoleMissing
                )
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))
                PermissionStatusRow(
                    title = strings.settingsPermContacts,
                    description = strings.onbPermContactsDesc,
                    isGranted = hasContactsPermission,
                    onGrantClick = onRequestContactsPermission,
                    grantedLabel = strings.settingsPermContactsGranted,
                    missingLabel = strings.settingsPermContactsMissing
                )
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))
                PermissionStatusRow(
                    title = strings.settingsPermSms,
                    description = strings.onbPermSmsDesc,
                    isGranted = hasSmsPermission,
                    onGrantClick = onRequestSmsPermission,
                    grantedLabel = strings.settingsPermSmsGranted,
                    missingLabel = strings.settingsPermSmsMissing
                )
            }
        }

        // --- 4. SECTION: PREFERENSI PERLINDUNGAN ---
        Text(
            text = strings.settingsSecFilters,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingSwitchRow(
                    title = strings.settingsForeignTitle,
                    description = strings.settingsForeignDesc,
                    isChecked = settings.blockForeignNumbers,
                    onCheckedChange = { viewModel.setBlockForeignNumbers(it) }
                )
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))
                SettingSwitchRow(
                    title = strings.settingsSuspiciousLinksTitle,
                    description = strings.settingsSuspiciousLinksDesc,
                    isChecked = settings.blockSuspiciousLinksInSms,
                    onCheckedChange = { viewModel.setBlockSuspiciousLinks(it) }
                )
            }
        }

        // --- 4. KONTROL NOTIFIKASI GRANULAR ---
        Text(
            text = strings.settingsSecNotificationControl,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Master Toggle Notifikasi
                SettingSwitchRow(
                    title = strings.settingsNotifyTitle,
                    description = strings.settingsNotifyDesc,
                    isChecked = settings.sendNotificationOnBlock,
                    onCheckedChange = { viewModel.setSendNotificationOnBlock(it) }
                )

                if (settings.sendNotificationOnBlock) {
                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 1. Notifikasi Scam / Bahaya
                    SettingSwitchRow(
                        title = strings.settingsNotifyThreatTitle,
                        description = strings.settingsNotifyThreatDesc,
                        isChecked = settings.notifyThreatSms,
                        onCheckedChange = { viewModel.setNotifyThreatSms(it) }
                    )

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 2. Notifikasi OTP Resmi
                    SettingSwitchRow(
                        title = strings.settingsNotifyOtpTitle,
                        description = strings.settingsNotifyOtpDesc,
                        isChecked = settings.notifyOtpSms,
                        onCheckedChange = { viewModel.setNotifyOtpSms(it) }
                    )

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 3. Notifikasi Promo & Iklan
                    SettingSwitchRow(
                        title = strings.settingsNotifyPromoTitle,
                        description = strings.settingsNotifyPromoDesc,
                        isChecked = settings.notifyPromoSms,
                        onCheckedChange = { viewModel.setNotifyPromoSms(it) }
                    )

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 4. Notifikasi Panggilan Diblokir
                    SettingSwitchRow(
                        title = strings.settingsNotifyCallsTitle,
                        description = strings.settingsNotifyCallsDesc,
                        isChecked = settings.notifyBlockedCalls,
                        onCheckedChange = { viewModel.setNotifyBlockedCalls(it) }
                    )
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                // Uji Notifikasi & Badge Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.settingsTestBadgeTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = strings.settingsTestBadgeDesc,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            com.hardcorezblocker.app.HardcorezBlockerApp.sendTestNotification(context)
                            Toast.makeText(context, strings.settingsTestBadgeSuccess, Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.settingsTestBadgeBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                // Setelan Lencana Sistem (HyperOS / Android)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.settingsSystemBadgeTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = strings.settingsSystemBadgeDesc,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val fallback = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                context.startActivity(fallback)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.settingsSystemBadgeBtn, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 5. SECTION: CADANGAN & PEMULIHAN ATURAN ---
        Text(
            text = strings.settingsSecBackup,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Row Ekspor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.settingsExportTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = strings.settingsExportDesc,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val json = viewModel.exportRules()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("HardcorezBlocker Rules", json))
                            Toast.makeText(context, strings.toastCopiedText, Toast.LENGTH_SHORT).show()
                            exportedJsonPreview = json
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.settingsExportBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Row Impor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.settingsImportTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = strings.settingsImportDesc,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.settingsImportBtn, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 6. SECTION: PRIVASI & GARANSI ON-DEVICE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ShieldActive.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = ShieldActive,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.settingsPrivacyTitle,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.settingsPrivacyDesc,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // --- 7. SECTION: PANDUAN & BANTUAN ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.settingsTutorialTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = strings.settingsTutorialDesc,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.triggerTutorial() },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(strings.settingsTutorialBtn, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (exportedJsonPreview != null) {
        ExportSuccessDialog(
            jsonText = exportedJsonPreview!!,
            onDismiss = { exportedJsonPreview = null }
        )
    }

    if (showImportDialog) {
        ImportRulesDialog(
            onDismiss = { showImportDialog = false },
            onImport = { jsonString, replaceExisting ->
                val count = viewModel.importRules(jsonString, replaceExisting)
                if (count >= 0) {
                    Toast.makeText(context, strings.toastImportSuccess(count), Toast.LENGTH_SHORT).show()
                    showImportDialog = false
                } else {
                    Toast.makeText(context, strings.toastImportFailed, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun PermissionStatusRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    grantedLabel: String,
    missingLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Surface(
                color = ShieldActive.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ShieldActive.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ShieldActive,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = grantedLabel,
                        color = ShieldActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(missingLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ShieldActive,
                uncheckedTrackColor = DarkSurfaceVariant,
                uncheckedThumbColor = TextMuted
            )
        )
    }
}
