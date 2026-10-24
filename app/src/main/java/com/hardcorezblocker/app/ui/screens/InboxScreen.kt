package com.hardcorezblocker.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.data.model.SmsMessageItem
import com.hardcorezblocker.app.data.model.SmsSafetyCategory
import com.hardcorezblocker.app.ui.components.*
import com.hardcorezblocker.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.i18n.Strings

enum class InboxFilter {
    ALL,
    THREATS_ONLY,
    PROMO_ONLY,
    OTP_ONLY,
    INFO_ONLY,
    CONTACTS_ONLY;

    fun getLabel(strings: Strings): String = when (this) {
        ALL -> strings.inboxFilterAll
        THREATS_ONLY -> strings.inboxFilterPhishing
        PROMO_ONLY -> strings.inboxFilterPromo
        OTP_ONLY -> strings.inboxFilterOtp
        INFO_ONLY -> strings.inboxFilterInfo
        CONTACTS_ONLY -> strings.inboxFilterContacts
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    messages: List<SmsMessageItem>,
    isDefaultSms: Boolean,
    onRequestSmsRole: () -> Unit,
    onRefresh: () -> Unit,
    onMarkAsRead: (Long, Boolean) -> Unit = { _, _ -> },
    onMarkAllAsRead: () -> Unit = {},
    onDeleteBatch: (List<Long>) -> Unit,
    onBlockSender: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(AppSortOrder.NEWEST) }
    var selectedFilter by remember { mutableStateOf(InboxFilter.ALL) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteFilterDialog by remember { mutableStateOf(false) }
    var pendingDeleteIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var activeDetailMessage by remember { mutableStateOf<SmsMessageItem?>(null) }

    val threatMessages = remember(messages) {
        messages.filter { it.category == SmsSafetyCategory.SPAM_OR_PHISHING }
    }
    val promoMessages = remember(messages) {
        messages.filter { it.category == SmsSafetyCategory.PROMO_MARKETING }
    }
    val otpMessages = remember(messages) {
        messages.filter { it.category == SmsSafetyCategory.OTP_OFFICIAL }
    }
    val infoMessages = remember(messages) {
        messages.filter { it.category == SmsSafetyCategory.NORMAL }
    }
    val contactMessages = remember(messages) {
        messages.filter { it.category == SmsSafetyCategory.SAFE_CONTACT }
    }

    val threatCount = threatMessages.size
    val promoCount = promoMessages.size
    val otpCount = otpMessages.size
    val infoCount = infoMessages.size
    val contactCount = contactMessages.size

    val filteredMessages = remember(messages, selectedFilter, searchQuery, sortOrder) {
        val categoryFiltered = when (selectedFilter) {
            InboxFilter.ALL -> messages
            InboxFilter.THREATS_ONLY -> threatMessages
            InboxFilter.PROMO_ONLY -> promoMessages
            InboxFilter.OTP_ONLY -> otpMessages
            InboxFilter.INFO_ONLY -> infoMessages
            InboxFilter.CONTACTS_ONLY -> contactMessages
        }
        val searchFiltered = if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            categoryFiltered.filter {
                it.sender.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
        }
        when (sortOrder) {
            AppSortOrder.NEWEST -> searchFiltered.sortedByDescending { it.timestamp }
            AppSortOrder.OLDEST -> searchFiltered.sortedBy { it.timestamp }
            AppSortOrder.NAME_ASC -> searchFiltered.sortedBy { it.sender.lowercase() }
        }
    }

    fun initiateDelete(ids: List<Long>) {
        if (ids.isEmpty()) return
        if (isDefaultSms) {
            onDeleteBatch(ids)
            selectedIds = emptySet()
        } else {
            pendingDeleteIds = ids
            showPermissionDialog = true
        }
    }

    ScreenContainer {
        AppHeader(
            title = strings.inboxTitle,
            subtitle = "${messages.size} ${if (strings.tabDashboard == "Dashboard") "SMS read on device" else "total SMS terbaca di ponsel"}",
            action = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            onMarkAllAsRead()
                            Toast.makeText(context, strings.toastAllRead, Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = strings.inboxMarkAllRead,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.inboxMarkAllRead,
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onRefresh,
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

        // --- 1. SPOTLIGHT CARD KEAMANAN (PRIORITAS UTAMA / FOCUS POINT) ---
        if (threatCount > 0 && selectedFilter != InboxFilter.THREATS_ONLY) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, DangerRed.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable {
                        selectedFilter = InboxFilter.THREATS_ONLY
                        selectedIds = emptySet()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DangerRed.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.inboxThreatBannerTitle(threatCount),
                            color = DangerRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.inboxThreatBannerDesc,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedFilter = InboxFilter.THREATS_ONLY
                            selectedIds = emptySet()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(strings.inboxThreatBannerBtn, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (selectedFilter == InboxFilter.THREATS_ONLY) {
            // Notice saat sedang memfilter ancaman
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DangerRed.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menyaring ${filteredMessages.size} pesan bahaya / scam",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { selectedFilter = InboxFilter.ALL },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tampilkan Semua",
                            color = PrimaryBlue,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- 2. SEARCH & FILTER ROW SERAGAM ---
        AppSearchFilterRow(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = strings.inboxSearchPlaceholder,
            isFilterActive = selectedFilter != InboxFilter.ALL || searchQuery.isNotBlank() || sortOrder != AppSortOrder.NEWEST,
            filterMenuContent = { dismiss ->
                Text(
                    text = if (strings.tabDashboard == "Dashboard") "SORT SMS" else "URUTKAN SMS",
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
                if (selectedFilter != InboxFilter.ALL || searchQuery.isNotBlank() || sortOrder != AppSortOrder.NEWEST) {
                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = {
                            Text(if (strings.tabDashboard == "Dashboard") "Reset Filters & Search" else "Reset Filter & Pencarian", color = DangerRed, fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.FilterListOff, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            selectedFilter = InboxFilter.ALL
                            searchQuery = ""
                            sortOrder = AppSortOrder.NEWEST
                            dismiss()
                        }
                    )
                }
            }
        )

        // --- 3. FILTER CHIPS BAR ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            item {
                AppFilterChip(
                    label = strings.inboxFilterAll,
                    count = messages.size,
                    isSelected = selectedFilter == InboxFilter.ALL,
                    accentColor = PrimaryBlue,
                    onClick = {
                        selectedFilter = InboxFilter.ALL
                        selectedIds = emptySet()
                    }
                )
            }
            item {
                AppFilterChip(
                    label = strings.inboxFilterPhishing,
                    count = threatCount,
                    isSelected = selectedFilter == InboxFilter.THREATS_ONLY,
                    accentColor = DangerRed,
                    hasAlert = threatCount > 0,
                    onClick = {
                        selectedFilter = InboxFilter.THREATS_ONLY
                        selectedIds = emptySet()
                    }
                )
            }
            item {
                AppFilterChip(
                    label = strings.inboxFilterPromo,
                    count = promoCount,
                    isSelected = selectedFilter == InboxFilter.PROMO_ONLY,
                    accentColor = WarningAmber,
                    onClick = {
                        selectedFilter = InboxFilter.PROMO_ONLY
                        selectedIds = emptySet()
                    }
                )
            }
            item {
                AppFilterChip(
                    label = strings.inboxFilterOtp,
                    count = otpCount,
                    isSelected = selectedFilter == InboxFilter.OTP_ONLY,
                    accentColor = PrimaryBlue,
                    onClick = {
                        selectedFilter = InboxFilter.OTP_ONLY
                        selectedIds = emptySet()
                    }
                )
            }
            item {
                AppFilterChip(
                    label = strings.inboxFilterInfo,
                    count = infoCount,
                    isSelected = selectedFilter == InboxFilter.INFO_ONLY,
                    accentColor = TextSecondary,
                    onClick = {
                        selectedFilter = InboxFilter.INFO_ONLY
                        selectedIds = emptySet()
                    }
                )
            }
            item {
                AppFilterChip(
                    label = strings.inboxFilterContacts,
                    count = contactCount,
                    isSelected = selectedFilter == InboxFilter.CONTACTS_ONLY,
                    accentColor = ShieldActive,
                    onClick = {
                        selectedFilter = InboxFilter.CONTACTS_ONLY
                        selectedIds = emptySet()
                    }
                )
            }
        }

        // --- FILTER ACTION TOOLBAR (HANYA MUNCUL JIKA ADA PESAN DI KATEGORI INI) ---
        if (filteredMessages.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank())
                            "${filteredMessages.size} ${if (strings.tabDashboard == "Dashboard") "search results" else "hasil pencarian"}"
                        else
                            strings.inboxShowingCount(filteredMessages.size),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DangerRed.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
                        modifier = Modifier.clickable {
                            showConfirmDeleteFilterDialog = true
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = strings.delete,
                                tint = DangerRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.delete} ${strings.all} (${filteredMessages.size})",
                                color = DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- MULTI-SELECT BAR JIKA USER MEMILIH BEBERAPA ITEM ---
        AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.inboxSelectedCount(selectedIds.size),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { selectedIds = emptySet() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = strings.cancel, color = TextSecondary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { initiateDelete(selectedIds.toList()) },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.delete,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${strings.delete} (${selectedIds.size})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- DAFTAR SMS ---
        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.isNotBlank()) {
                    AppEmptyState(
                        icon = Icons.Default.SearchOff,
                        title = strings.inboxEmptySearchTitle,
                        description = "${if (strings.tabDashboard == "Dashboard") "No SMS matched" else "Tidak ada SMS yang cocok dengan"} \"$searchQuery\".",
                        accentColor = PrimaryBlue
                    )
                } else if (messages.isEmpty()) {
                    AppEmptyState(
                        icon = Icons.Default.MarkChatRead,
                        title = strings.inboxEmptyTitle,
                        description = strings.inboxEmptySubtitle,
                        accentColor = ShieldActive
                    )
                } else {
                    AppEmptyState(
                        icon = Icons.Default.FolderOpen,
                        title = if (strings.tabDashboard == "Dashboard") "Category is Empty" else "Kategori Ini Kosong",
                        description = "${if (strings.tabDashboard == "Dashboard") "No SMS messages found in category" else "Tidak ditemukan pesan SMS pada kategori"} '${selectedFilter.getLabel(strings)}'.",
                        accentColor = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredMessages, key = { it.id }) { msg ->
                    val isChecked = selectedIds.contains(msg.id)
                    CalmSmsCard(
                        message = msg,
                        isSelected = isChecked,
                        onClick = {
                            activeDetailMessage = msg
                            onMarkAsRead(msg.id, msg.isMms)
                        },
                        onToggleSelect = {
                            selectedIds = if (isChecked) selectedIds - msg.id else selectedIds + msg.id
                        },
                        onDeleteSingle = {
                            initiateDelete(listOf(msg.id))
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SMS Content", msg.body))
                            Toast.makeText(context, strings.toastCopiedText, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (activeDetailMessage != null) {
        SmsDetailBottomSheet(
            message = activeDetailMessage!!,
            onDismiss = { activeDetailMessage = null },
            onBlockSender = { sender ->
                onBlockSender(sender)
                activeDetailMessage = null
            },
            onDelete = { id ->
                initiateDelete(listOf(id))
                activeDetailMessage = null
            },
            onCopy = { text ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("SMS Content", text))
                Toast.makeText(context, strings.toastCopiedText, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- DIALOG KONFIRMASI HAPUS SEMUA DI FILTER INI ---
    if (showConfirmDeleteFilterDialog) {
        val filterTitle = selectedFilter.getLabel(strings)

        AlertDialog(
            onDismissRequest = { showConfirmDeleteFilterDialog = false },
            title = {
                Text(
                    text = strings.inboxDeleteConfirmTitle(filteredMessages.size),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "${strings.inboxDeleteConfirmMsg}\n($filterTitle)",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteFilterDialog = false
                        initiateDelete(filteredMessages.map { it.id })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("${strings.delete} ${filteredMessages.size} SMS", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteFilterDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- DIALOG IZIN DEFAULT SMS SISTEM ANDROID ---
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = strings.inboxBannerSmsRoleTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = strings.inboxBannerSmsRoleDesc,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        onRequestSmsRole()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White)
                ) {
                    Text(strings.inboxBannerSmsRoleBtn, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CalmSmsCard(
    message: SmsMessageItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDeleteSingle: () -> Unit,
    onCopy: () -> Unit,
    onClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val timeFormatted = remember(message.timestamp) {
        dateFormat.format(Date(message.timestamp))
    }

    val isThreat = message.category == SmsSafetyCategory.SPAM_OR_PHISHING

    val borderColor = when {
        isSelected -> PrimaryBlue
        isThreat -> DangerRed.copy(alpha = 0.60f)
        !message.isRead -> PrimaryBlue.copy(alpha = 0.50f)
        else -> DarkBorder
    }

    val cardBg = when {
        isSelected -> DarkSurfaceVariant
        isThreat -> DangerRed.copy(alpha = 0.08f)
        !message.isRead -> DarkSurfaceElevated
        else -> DarkSurfaceElevated.copy(alpha = 0.65f)
    }

    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(if (isThreat || isSelected || !message.isRead) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Kolom Kiri: Checkbox & Avatar vertikal sejajar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue,
                        uncheckedColor = TextMuted
                    ),
                    modifier = Modifier.size(20.dp)
                )

                // Avatar Bulat Indikator
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isThreat -> DangerRed.copy(alpha = 0.16f)
                                message.category == SmsSafetyCategory.OTP_OFFICIAL -> PrimaryBlue.copy(alpha = 0.16f)
                                message.category == SmsSafetyCategory.PROMO_MARKETING -> WarningAmber.copy(alpha = 0.16f)
                                message.category == SmsSafetyCategory.SAFE_CONTACT -> ShieldActive.copy(alpha = 0.16f)
                                else -> DarkSurfaceVariant
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isThreat -> DangerRed.copy(alpha = 0.45f)
                                message.category == SmsSafetyCategory.OTP_OFFICIAL -> PrimaryBlue.copy(alpha = 0.45f)
                                message.category == SmsSafetyCategory.PROMO_MARKETING -> WarningAmber.copy(alpha = 0.45f)
                                message.category == SmsSafetyCategory.SAFE_CONTACT -> ShieldActive.copy(alpha = 0.45f)
                                else -> DarkBorder
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isThreat) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = message.sender.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = when (message.category) {
                                SmsSafetyCategory.OTP_OFFICIAL -> PrimaryBlue
                                SmsSafetyCategory.PROMO_MARKETING -> WarningAmber
                                SmsSafetyCategory.SAFE_CONTACT -> ShieldActive
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Kolom Kanan: Seluruh konten pesan (Menggunakan 100% lebar yang tersisa)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Baris 1: Judul Pengirim & Waktu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.sender,
                        fontWeight = if (!message.isRead || isThreat) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isThreat) DangerRed else TextPrimary,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Baris 2: Tag Badges (Kategori Bahaya/Promo/OTP + MMS + Indikator Baru)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppSafetyBadge(category = message.category)

                    if (message.isMms) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PromoPurple.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PromoPurple.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "MMS",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PromoPurple,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!message.isRead) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = (if (isThreat) DangerRed else PrimaryBlue).copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, (if (isThreat) DangerRed else PrimaryBlue).copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isThreat) DangerRed else PrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.inboxUnreadBadge.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isThreat) DangerRed else PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // Baris 3: Banner Ancaman Khusus (Jika terdeteksi bahaya)
                if (isThreat && message.threatDetail != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DangerRed.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.threatDetail,
                                color = DangerRed,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Baris 4: Cuplikan Isi Pesan
                Text(
                    text = message.body,
                    color = if (!message.isRead) TextPrimary else TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Baris 5: Panduan & Tombol Salin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (strings.tabDashboard == "Dashboard") "Tap for blocking & details" else "Ketuk untuk opsi blokir & detail",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onCopy,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = strings.copy,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = strings.copy, color = TextSecondary, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDeleteSingle,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = strings.delete,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

