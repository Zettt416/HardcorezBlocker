package com.hardcorezblocker.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.hardcorezblocker.app.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.BlockingMode
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.ui.NavigationTab
import com.hardcorezblocker.app.ui.theme.*
import com.hardcorezblocker.app.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.hardcorezblocker.app.ui.components.AppHeader
import com.hardcorezblocker.app.ui.components.ScreenContainer

import com.hardcorezblocker.app.ui.i18n.LocalAppStrings

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onRequestRole: () -> Unit,
    onRequestSmsRole: () -> Unit,
    onNavigateToTab: (NavigationTab) -> Unit
) {
    val strings = LocalAppStrings.current
    val settings by viewModel.settings.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val isRoleHeld by viewModel.isRoleHeld.collectAsState()
    val isDefaultSms by viewModel.isDefaultSms.collectAsState()

    val totalBlockedCalls = logs.count { it.channel == TargetChannel.CALL_ONLY || it.channel == TargetChannel.BOTH }
    val totalBlockedSms = logs.count { it.channel == TargetChannel.SMS_ONLY }
    val activeRulesCount = rules.count { it.isEnabled }

    val isShieldActive = settings.isProtectionActive && settings.mode != BlockingMode.OFF
    val shieldColor by animateColorAsState(
        targetValue = when {
            !isShieldActive -> ShieldInactive
            settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY -> ShieldActive
            else -> PrimaryBlue
        },
        label = "shield_color"
    )

    ScreenContainer(scrollable = true) {

        AppHeader(
            title = strings.appName,
            subtitle = strings.dashHeaderSubtitle,
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(36.dp)
                )
            },
            action = {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isShieldActive) ShieldActive.copy(alpha = 0.12f) else DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isShieldActive) ShieldActive.copy(alpha = 0.35f) else DarkBorder
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(shieldColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isShieldActive) strings.active else strings.inactive,
                            color = if (isShieldActive) ShieldActive else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        )

        if (!isRoleHeld) {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(1.dp, WarningAmber.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.dashPermAlertTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.dashPermAlertDesc,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRequestRole,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.dashGrantPerm, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (!isDefaultSms) {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(1.dp, WarningAmber.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.dashDefaultSmsAlertTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.dashDefaultSmsAlertDesc,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRequestSmsRole,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.dashGrantDefaultSms, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            shieldColor.copy(alpha = if (isShieldActive) 0.18f else 0.08f),
                            DarkSurfaceElevated
                        )
                    )
                )
                .border(
                    width = if (isShieldActive) 1.5.dp else 1.dp,
                    color = if (isShieldActive) shieldColor.copy(alpha = 0.50f) else DarkBorder,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(shieldColor.copy(alpha = 0.16f))
                            .border(1.5.dp, shieldColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isShieldActive) Icons.Default.Shield else Icons.Default.SecurityUpdateWarning,
                            contentDescription = "Shield",
                            tint = shieldColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isShieldActive) strings.dashProtectionActive else strings.dashProtectionInactive,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isShieldActive) {
                                if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) strings.dashStatusHardcoreDesc else strings.dashStatusSmartDesc
                            } else {
                                strings.dashStatusPausedDesc
                            },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Switch(
                        checked = isShieldActive,
                        onCheckedChange = { active ->
                            if (active) {
                                viewModel.setProtectionActive(true)
                                if (settings.mode == BlockingMode.OFF) {
                                    viewModel.setBlockingMode(BlockingMode.HARDCORE_CONTACTS_ONLY)
                                }
                            } else {
                                viewModel.setProtectionActive(false)
                                viewModel.setBlockingMode(BlockingMode.OFF)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ShieldActive,
                            uncheckedTrackColor = DarkSurfaceVariant,
                            uncheckedThumbColor = TextMuted
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isShieldActive) shieldColor.copy(alpha = 0.20f) else DarkBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.dashModeTitle.uppercase(Locale.ROOT),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    if (isShieldActive) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = shieldColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, shieldColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(shieldColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) strings.dashModeHardcoreTitle else strings.dashModeSmartTitle,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = shieldColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val isHardcoreSelected = settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY && isShieldActive
                        val isSmartSelected = settings.mode == BlockingMode.SMART_FILTER && isShieldActive

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isHardcoreSelected) ShieldActive.copy(alpha = 0.22f) else Color.Transparent)
                                .border(
                                    width = if (isHardcoreSelected) 1.5.dp else 0.dp,
                                    color = if (isHardcoreSelected) ShieldActive else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.setProtectionActive(true)
                                    viewModel.setBlockingMode(BlockingMode.HARDCORE_CONTACTS_ONLY)
                                }
                                .padding(vertical = 9.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isHardcoreSelected) ShieldActive else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = strings.dashModeHardcoreTitle,
                                        fontSize = 13.sp,
                                        fontWeight = if (isHardcoreSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isHardcoreSelected) Color.White else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.dashModeHardcoreSub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isHardcoreSelected) ShieldActive else TextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSmartSelected) PrimaryBlue.copy(alpha = 0.22f) else Color.Transparent)
                                .border(
                                    width = if (isSmartSelected) 1.5.dp else 0.dp,
                                    color = if (isSmartSelected) PrimaryBlue else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.setProtectionActive(true)
                                    viewModel.setBlockingMode(BlockingMode.SMART_FILTER)
                                }
                                .padding(vertical = 9.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = if (isSmartSelected) PrimaryBlue else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = strings.dashModeSmartTitle,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSmartSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSmartSelected) Color.White else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.dashModeSmartSub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSmartSelected) PrimaryBlue else TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = (if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) ShieldActive else PrimaryBlue).copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        (if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) ShieldActive else PrimaryBlue).copy(alpha = 0.22f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) ShieldActive else PrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                !isShieldActive -> strings.dashProtectionPaused
                                settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY -> strings.onbModeHardcoreDesc
                                else -> strings.onbModeSmartDesc
                            },
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InteractiveStatCard(
                title = strings.calls,
                count = totalBlockedCalls.toString(),
                subtitle = strings.dashStatBlockedCalls,
                actionHint = "${strings.tabLogs} ➜",
                icon = Icons.AutoMirrored.Filled.PhoneMissed,
                color = DangerRed,
                onClick = { onNavigateToTab(NavigationTab.LOGS) },
                modifier = Modifier.weight(1f)
            )
            InteractiveStatCard(
                title = strings.sms,
                count = totalBlockedSms.toString(),
                subtitle = strings.dashStatBlockedSms,
                actionHint = "${strings.tabInbox} ➜",
                icon = Icons.Default.SecurityUpdateWarning,
                color = WarningAmber,
                onClick = { onNavigateToTab(NavigationTab.INBOX) },
                modifier = Modifier.weight(1f)
            )
            InteractiveStatCard(
                title = strings.tabRules,
                count = activeRulesCount.toString(),
                subtitle = strings.dashStatActiveRules,
                actionHint = "${strings.tabRules} ➜",
                icon = Icons.AutoMirrored.Filled.Rule,
                color = PrimaryBlue,
                onClick = { onNavigateToTab(NavigationTab.RULES) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.dashRecentActivity.uppercase(Locale.ROOT),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )

            if (logs.isNotEmpty()) {
                Text(
                    text = "${strings.dashViewAll} ➜",
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTab(NavigationTab.LOGS) }
                )
            }
        }

        if (logs.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ShieldActive.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ShieldActive,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.dashNoRecentActivity,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = strings.dashDeviceProtected,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val recentLogs = logs.take(2)
                    recentLogs.forEachIndexed { index, log ->
                        RecentLogMiniRow(log = log)
                        if (index < recentLogs.size - 1) {
                            HorizontalDivider(
                                color = DarkBorder,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = strings.dashQuickActions.uppercase(Locale.ROOT),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickShortcutCard(
                title = strings.dashActionAddRule,
                subtitle = strings.rulesSubtitle,
                icon = Icons.Default.AddModerator,
                accentColor = PrimaryBlue,
                onClick = { onNavigateToTab(NavigationTab.RULES) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            QuickShortcutCard(
                title = strings.dashActionCheckSms,
                subtitle = strings.inboxTitle,
                icon = Icons.Default.MarkChatRead,
                accentColor = PromoPurple,
                onClick = { onNavigateToTab(NavigationTab.INBOX) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun InteractiveStatCard(
    title: String,
    count: String,
    subtitle: String,
    actionHint: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, color.copy(alpha = 0.30f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.16f))
                        .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.85f),
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = count,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                minLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun RecentLogMiniRow(log: BlockedLog) {
    val isCall = log.channel == TargetChannel.CALL_ONLY || log.channel == TargetChannel.BOTH
    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCall) DangerRed.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCall) Icons.AutoMirrored.Filled.PhoneMissed else Icons.Default.SmsFailed,
                contentDescription = null,
                tint = if (isCall) DangerRed else WarningAmber,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.senderOrNumber,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = log.reason,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = timeFormatted,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
