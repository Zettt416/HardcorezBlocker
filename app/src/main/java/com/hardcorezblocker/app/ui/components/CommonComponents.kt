package com.hardcorezblocker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.ui.theme.*

import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.i18n.Strings

enum class AppSortOrder(val label: String) {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    NAME_ASC("Nama / Nomor (A-Z)");

    fun getLabel(strings: Strings): String = when (this) {
        NEWEST -> if (strings.tabDashboard == "Dashboard") "Newest" else "Terbaru"
        OLDEST -> if (strings.tabDashboard == "Dashboard") "Oldest" else "Terlama"
        NAME_ASC -> if (strings.tabDashboard == "Dashboard") "Name / Number (A-Z)" else "Nama / Nomor (A-Z)"
    }
}

@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(PrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppFilterButton(
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) PrimaryBlue.copy(alpha = 0.15f) else DarkSurfaceElevated)
            .border(
                1.dp,
                if (isActive) PrimaryBlue.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Filter",
            tint = if (isActive) PrimaryBlue else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
            )
        }
    }
}

@Composable
fun AppSearchFilterRow(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isFilterActive: Boolean = false,
    onFilterClick: () -> Unit = {},
    filterMenuContent: @Composable (ColumnScope.(() -> Unit) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box {
            AppFilterButton(
                onClick = {
                    if (filterMenuContent != null) {
                        expanded = !expanded
                    } else {
                        onFilterClick()
                    }
                },
                isActive = isFilterActive
            )
            if (filterMenuContent != null) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    filterMenuContent { expanded = false }
                }
            }
        }
    }
}

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(12.dp))
            action()
        }
    }
}

@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .fillMaxSize()
        .background(DarkBackground)
        .padding(horizontal = 20.dp, vertical = 4.dp)

    if (scrollable) {
        Column(
            modifier = baseModifier.verticalScroll(rememberScrollState()),
            content = content
        )
    } else {
        Column(
            modifier = baseModifier,
            content = content
        )
    }
}

@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ShieldActive
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AppFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    accentColor: Color = PrimaryBlue,
    hasAlert: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = when {
            isSelected -> accentColor.copy(alpha = 0.22f)
            hasAlert -> DangerRed.copy(alpha = 0.10f)
            else -> DarkSurfaceElevated
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else if (hasAlert) 1.2.dp else 1.dp,
            color = when {
                isSelected -> accentColor
                hasAlert -> DangerRed.copy(alpha = 0.65f)
                else -> DarkBorder
            }
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.5.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = when {
                    isSelected -> accentColor
                    hasAlert -> DangerRed
                    else -> TextSecondary
                },
                maxLines = 1,
                softWrap = false
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isSelected -> accentColor.copy(alpha = 0.32f)
                                hasAlert -> DangerRed.copy(alpha = 0.25f)
                                else -> DarkSurfaceVariant
                            }
                        )
                        .padding(horizontal = 7.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSelected -> accentColor
                            hasAlert -> DangerRed
                            else -> TextSecondary
                        },
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun AppSafetyBadge(
    category: com.hardcorezblocker.app.data.model.SmsSafetyCategory,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val (label, bg, textCol) = when (category) {
        com.hardcorezblocker.app.data.model.SmsSafetyCategory.SPAM_OR_PHISHING -> Triple(strings.inboxFilterPhishing, DangerRed.copy(alpha = 0.16f), DangerRed)
        com.hardcorezblocker.app.data.model.SmsSafetyCategory.PROMO_MARKETING -> Triple(strings.inboxFilterPromo, WarningAmber.copy(alpha = 0.16f), WarningAmber)
        com.hardcorezblocker.app.data.model.SmsSafetyCategory.OTP_OFFICIAL -> Triple(strings.inboxFilterOtp, PrimaryBlue.copy(alpha = 0.16f), PrimaryBlue)
        com.hardcorezblocker.app.data.model.SmsSafetyCategory.SAFE_CONTACT -> Triple(strings.inboxFilterContacts, ShieldActive.copy(alpha = 0.16f), ShieldActive)
        com.hardcorezblocker.app.data.model.SmsSafetyCategory.NORMAL, com.hardcorezblocker.app.data.model.SmsSafetyCategory.ALL -> Triple(strings.inboxFilterInfo, DarkSurfaceVariant, TextMuted)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, textCol.copy(alpha = 0.40f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textCol,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

