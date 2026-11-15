package com.hardcorezblocker.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.data.model.AppLanguage
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.RuleType
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
fun LogsScreen(
    viewModel: MainViewModel,
    initialChannelFilter: TargetChannel? = null,
    onFilterChanged: (TargetChannel?) -> Unit = {}
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val logs by viewModel.logs.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(AppSortOrder.NEWEST) }
    var selectedChannelFilter by remember { mutableStateOf(initialChannelFilter) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var clearScope by remember { mutableStateOf(ClearLogsScope.ALL) }
    var activeDetailLog by remember { mutableStateOf<BlockedLog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshLogs()
    }

    LaunchedEffect(initialChannelFilter) {
        selectedChannelFilter = initialChannelFilter
    }

    fun updateChannelFilter(channel: TargetChannel?) {
        selectedChannelFilter = channel
        onFilterChanged(channel)
    }

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
            action = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (logs.isNotEmpty()) {
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
                            Text(strings.logsClearBtn, color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = { viewModel.refreshLogs() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )

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
                            updateChannelFilter(null)
                            searchQuery = ""
                            sortOrder = AppSortOrder.NEWEST
                            dismiss()
                        }
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppFilterChip(
                label = strings.all,
                count = logs.size,
                isSelected = selectedChannelFilter == null,
                onClick = { updateChannelFilter(null) },
                accentColor = PrimaryBlue
            )
            AppFilterChip(
                label = strings.calls,
                count = callLogsCount,
                isSelected = selectedChannelFilter == TargetChannel.CALL_ONLY,
                onClick = { updateChannelFilter(TargetChannel.CALL_ONLY) },
                accentColor = DangerRed
            )
            AppFilterChip(
                label = strings.sms,
                count = smsLogsCount,
                isSelected = selectedChannelFilter == TargetChannel.SMS_ONLY,
                onClick = { updateChannelFilter(TargetChannel.SMS_ONLY) },
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
                    LogItemCard(
                        log = log,
                        onClick = { activeDetailLog = log },
                        onCopyNumber = { number ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", number))
                            Toast.makeText(context, strings.toastCopiedNumber, Toast.LENGTH_SHORT).show()
                        },
                        onDeleteLog = { logId ->
                            viewModel.deleteLog(logId)
                            Toast.makeText(context, strings.toastLogDeleted, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (activeDetailLog != null) {
        val currentLog = activeDetailLog!!
        val cleanTarget = currentLog.senderOrNumber.replace("[^0-9+]".toRegex(), "")
        val isAlreadyBlacklisted = remember(rules, currentLog.senderOrNumber) {
            rules.any { r ->
                val rClean = r.pattern.replace("[^0-9+]".toRegex(), "")
                (r.type == RuleType.EXACT) &&
                    ((cleanTarget.isNotEmpty() && rClean == cleanTarget) ||
                        r.pattern.equals(currentLog.senderOrNumber.trim(), ignoreCase = true))
            }
        }

        BlockedLogDetailBottomSheet(
            log = currentLog,
            isAlreadyBlacklisted = isAlreadyBlacklisted,
            onDismiss = { activeDetailLog = null },
            onAddBlacklist = { sender ->
                val isCall = currentLog.channel == TargetChannel.CALL_ONLY || currentLog.channel == TargetChannel.BOTH
                val label = if (settings.language == AppLanguage.ENGLISH) {
                    if (isCall) "Blocked from Call Log" else "Blocked from SMS Log"
                } else {
                    if (isCall) "Blokir dari Riwayat Panggilan" else "Blokir dari Riwayat SMS"
                }
                viewModel.addRule(
                    pattern = sender,
                    type = RuleType.EXACT,
                    channel = TargetChannel.BOTH,
                    label = label
                )
                Toast.makeText(context, strings.toastRuleAdded, Toast.LENGTH_SHORT).show()
            },
            onCopyNumber = { number ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", number))
                Toast.makeText(context, strings.toastCopiedNumber, Toast.LENGTH_SHORT).show()
            },
            onSaveContact = { number ->
                try {
                    val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                        type = ContactsContract.RawContacts.CONTENT_TYPE
                        putExtra(ContactsContract.Intents.Insert.PHONE, number)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                }
            },
            onDialNumber = { number ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$number"))
                    context.startActivity(intent)
                } catch (_: Exception) {
                }
            },
            onDeleteLog = { logId ->
                viewModel.deleteLog(logId)
                activeDetailLog = null
                Toast.makeText(context, strings.toastLogDeleted, Toast.LENGTH_SHORT).show()
            }
        )
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
fun LogItemCard(
    log: BlockedLog,
    onClick: () -> Unit = {},
    onCopyNumber: (String) -> Unit = {},
    onDeleteLog: (String) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val isCall = log.channel == TargetChannel.CALL_ONLY || log.channel == TargetChannel.BOTH
    val accentColor = if (isCall) DangerRed else WarningAmber
    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.14f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCall) Icons.AutoMirrored.Filled.PhoneMissed else Icons.Default.SmsFailed,
                        contentDescription = null,
                        tint = accentColor,
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
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeFormatted,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(5.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (isCall) strings.logDetailCallBadge else strings.logDetailSmsBadge,
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = log.reason,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (!log.contentSnippet.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${log.contentSnippet}\"",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isCall) strings.logDetailSaveContactBtn else strings.logDetailReasonTitle,
                        color = PrimaryBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCopyNumber(log.senderOrNumber) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = strings.logDetailCopyNumberBtn,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.logDetailCopyNumberBtn,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DangerRed.copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.30f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDeleteLog(log.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = strings.logDetailDeleteBtn,
                                tint = DangerRed,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockedLogDetailBottomSheet(
    log: BlockedLog,
    isAlreadyBlacklisted: Boolean,
    onDismiss: () -> Unit,
    onAddBlacklist: (String) -> Unit,
    onCopyNumber: (String) -> Unit,
    onSaveContact: (String) -> Unit,
    onDialNumber: (String) -> Unit = {},
    onDeleteLog: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isCall = log.channel == TargetChannel.CALL_ONLY || log.channel == TargetChannel.BOTH
    val accentColor = if (isCall) DangerRed else WarningAmber
    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm:ss", Locale.getDefault()) }
    val fullTime = remember(log.timestamp) {
        dateFormat.format(Date(log.timestamp))
    }
    val isValidPhone = remember(log.senderOrNumber) {
        log.senderOrNumber.any { it.isDigit() } &&
            !log.senderOrNumber.equals("Unknown / Hidden", ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCall) Icons.AutoMirrored.Filled.PhoneMissed else Icons.Default.SmsFailed,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Text(
                            text = log.senderOrNumber,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = if (isCall) strings.logDetailCallBadge else strings.logDetailSmsBadge,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fullTime,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                if (isValidPhone && isCall) {
                    IconButton(
                        onClick = { onDialNumber(log.senderOrNumber) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = strings.calls,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = accentColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.logDetailReasonTitle,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.reason,
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (!log.contentSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.logsContentPrefix,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = log.contentSnippet,
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isValidPhone) {
                Button(
                    onClick = { onAddBlacklist(log.senderOrNumber) },
                    enabled = !isAlreadyBlacklisted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = if (isAlreadyBlacklisted) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        tint = if (isAlreadyBlacklisted) TextMuted else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAlreadyBlacklisted) strings.logDetailAlreadyBlacklisted else strings.logDetailAddBlacklistBtn,
                        color = if (isAlreadyBlacklisted) TextMuted else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopyNumber(log.senderOrNumber) },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.logDetailCopyNumberBtn,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                if (isValidPhone && isCall) {
                    OutlinedButton(
                        onClick = { onSaveContact(log.senderOrNumber) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldActive.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = ShieldActive,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.logDetailSaveContactBtn,
                            color = ShieldActive,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = { onDeleteLog(log.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.logDetailDeleteBtn,
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
