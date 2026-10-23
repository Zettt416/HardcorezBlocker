package com.hardcorezblocker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.ui.components.*
import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.theme.*
import com.hardcorezblocker.app.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ClearLogsScope {
    ALL,
    CALL_ONLY,
    SMS_ONLY
}

@Composable
fun LogsScreen(viewModel: MainViewModel) {
    val strings = LocalAppStrings.current
    val logs by viewModel.logs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(AppSortOrder.NEWEST) }
    var selectedChannelFilter by remember { mutableStateOf<TargetChannel?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var clearScope by remember { mutableStateOf(ClearLogsScope.ALL) }

    val callLogsCount = remember(logs) {
        logs.count { it.channel == TargetChannel.CALL_ONLY || it.channel == TargetChannel.BOTH }
    }
    val smsLogsCount = remember(logs) {
        logs.count { it.channel == TargetChannel.SMS_ONLY }
    }

    val filteredLogs = remember(logs, selectedChannelFilter, searchQuery, sortOrder) {
        val channelFiltered = if (selectedChannelFilter == null) {
            logs
        } else if (selectedChannelFilter == TargetChannel.CALL_ONLY) {
            logs.filter { it.channel == TargetChannel.CALL_ONLY || it.channel == TargetChannel.BOTH }
        } else {
            logs.filter { it.channel == TargetChannel.SMS_ONLY }
        }

        val searchFiltered = if (searchQuery.isBlank()) {
            channelFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            channelFiltered.filter {
                it.senderOrNumber.lowercase().contains(q) ||
                it.reason.lowercase().contains(q) ||
                (it.contentSnippet?.lowercase()?.contains(q) == true)
            }
        }

        when (sortOrder) {
            AppSortOrder.NEWEST -> searchFiltered.sortedByDescending { it.timestamp }
            AppSortOrder.OLDEST -> searchFiltered.sortedBy { it.timestamp }
            AppSortOrder.NAME_ASC -> searchFiltered.sortedBy { it.senderOrNumber.lowercase() }
        }
    }

    ScreenContainer {
        AppHeader(
            title = strings.logsTitle,
            subtitle = strings.logsHeaderSubtitle(logs.size),
            action = if (logs.isNotEmpty()) {
                {
                    TextButton(onClick = {
                        clearScope = when (selectedChannelFilter) {
                            TargetChannel.CALL_ONLY -> ClearLogsScope.CALL_ONLY
                            TargetChannel.SMS_ONLY -> ClearLogsScope.SMS_ONLY
                            else -> ClearLogsScope.ALL
                        }
                        showClearConfirm = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = strings.logsClearBtn,
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.logsClearBtn, color = DangerRed, fontSize = 13.sp)
                    }
                }
            } else null
        )

        // Search & Filter Row
        AppSearchFilterRow(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = strings.logsSearchPlaceholder,
            isFilterActive = selectedChannelFilter != null || searchQuery.isNotBlank() || sortOrder != AppSortOrder.NEWEST,
            filterMenuContent = { dismiss ->
                Text(
                    text = strings.logsSortTitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
                listOf(AppSortOrder.NEWEST, AppSortOrder.OLDEST, AppSortOrder.NAME_ASC).forEach { order ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = order.getLabel(strings),
                                    color = if (sortOrder == order) PrimaryBlue else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                )
                                if (sortOrder == order) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            sortOrder = order
                            dismiss()
                        }
                    )
                }
                if (selectedChannelFilter != null || searchQuery.isNotBlank() || sortOrder != AppSortOrder.NEWEST) {
                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text(strings.logsResetFilter, color = DangerRed, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.FilterListOff, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            selectedChannelFilter = null
                            searchQuery = ""
                            sortOrder = AppSortOrder.NEWEST
                            dismiss()
                        }
                    )
                }
            }
        )

        // Filter Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppFilterChip(
                label = strings.all,
                count = logs.size,
                isSelected = selectedChannelFilter == null,
                onClick = { selectedChannelFilter = null },
                accentColor = PrimaryBlue
            )
            AppFilterChip(
                label = strings.calls,
                count = callLogsCount,
                isSelected = selectedChannelFilter == TargetChannel.CALL_ONLY,
                onClick = { selectedChannelFilter = TargetChannel.CALL_ONLY },
                accentColor = DangerRed
            )
            AppFilterChip(
                label = strings.sms,
                count = smsLogsCount,
                isSelected = selectedChannelFilter == TargetChannel.SMS_ONLY,
                onClick = { selectedChannelFilter = TargetChannel.SMS_ONLY },
                accentColor = WarningAmber
            )
        }

        if (filteredLogs.isEmpty()) {
            if (searchQuery.isNotBlank()) {
                AppEmptyState(
                    icon = Icons.Default.SearchOff,
                    title = strings.logsSearchNotFoundTitle,
                    description = strings.logsSearchNotFoundDesc(searchQuery),
                    accentColor = PrimaryBlue
                )
            } else if (logs.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.VerifiedUser,
                    title = strings.logsEmptyAllTitle,
                    description = strings.logsEmptyAllDesc,
                    accentColor = ShieldActive
                )
            } else {
                AppEmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = strings.logsEmptyCategoryTitle,
                    description = strings.logsEmptyCategoryDesc,
                    accentColor = TextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    LogItemCard(log = log)
                }
            }
        }
    }

    if (showClearConfirm) {
        val countToDelete = when (clearScope) {
            ClearLogsScope.ALL -> logs.size
            ClearLogsScope.CALL_ONLY -> callLogsCount
            ClearLogsScope.SMS_ONLY -> smsLogsCount
        }

        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    text = strings.logsClearDialogTitle,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.logsClearDialogDesc,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val options = listOf(
                        Triple(ClearLogsScope.ALL, strings.logsClearDialogAll, logs.size),
                        Triple(ClearLogsScope.CALL_ONLY, strings.logsClearDialogCalls, callLogsCount),
                        Triple(ClearLogsScope.SMS_ONLY, strings.logsClearDialogSms, smsLogsCount)
                    )

                    options.forEach { (scope, label, count) ->
                        val isSelected = clearScope == scope
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) DangerRed.copy(alpha = 0.12f) else DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) DangerRed.copy(alpha = 0.5f) else DarkBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { clearScope = scope }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { clearScope = scope },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = DangerRed,
                                            unselectedColor = TextMuted
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DangerRed.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) DangerRed else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetChannel = when (clearScope) {
                            ClearLogsScope.ALL -> null
                            ClearLogsScope.CALL_ONLY -> TargetChannel.CALL_ONLY
                            ClearLogsScope.SMS_ONLY -> TargetChannel.SMS_ONLY
                        }
                        viewModel.clearLogs(targetChannel)
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = countToDelete > 0
                ) {
                    Text(
                        text = strings.logsClearConfirmBtn(countToDelete),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun LogItemCard(log: BlockedLog) {
    val isCall = log.channel == TargetChannel.CALL_ONLY || log.channel == TargetChannel.BOTH
    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCall) DangerRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                    .border(1.dp, if (isCall) DangerRed.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCall) Icons.AutoMirrored.Filled.PhoneMissed else Icons.Default.SmsFailed,
                    contentDescription = null,
                    tint = if (isCall) DangerRed else WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.senderOrNumber,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormatted,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Text(
                        text = log.reason,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                if (!log.contentSnippet.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${log.contentSnippet}\"",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
