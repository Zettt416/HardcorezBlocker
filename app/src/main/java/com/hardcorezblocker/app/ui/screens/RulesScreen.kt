package com.hardcorezblocker.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.data.model.BlockRule
import com.hardcorezblocker.app.data.model.RuleType
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.ui.components.*
import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.i18n.getDisplayName
import com.hardcorezblocker.app.ui.theme.*
import com.hardcorezblocker.app.ui.viewmodel.MainViewModel

@Composable
fun RulesScreen(viewModel: MainViewModel) {
    val strings = LocalAppStrings.current
    val rules by viewModel.rules.collectAsState()
    var selectedChannelFilter by remember { mutableStateOf<TargetChannel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(AppSortOrder.NEWEST) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSimulator by remember { mutableStateOf(false) }

    val filteredRules = remember(rules, selectedChannelFilter, searchQuery, sortOrder) {
        val channelFiltered = if (selectedChannelFilter == null) {
            rules
        } else {
            rules.filter { it.channel == selectedChannelFilter || it.channel == TargetChannel.BOTH }
        }
        val searchFiltered = if (searchQuery.isBlank()) {
            channelFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            channelFiltered.filter {
                it.pattern.lowercase().contains(q) || it.label.lowercase().contains(q)
            }
        }
        when (sortOrder) {
            AppSortOrder.NEWEST -> searchFiltered.sortedByDescending { it.id }
            AppSortOrder.OLDEST -> searchFiltered.sortedBy { it.id }
            AppSortOrder.NAME_ASC -> searchFiltered.sortedBy { it.pattern.lowercase() }
        }
    }

    val callRulesCount = remember(rules) {
        rules.count { it.channel == TargetChannel.CALL_ONLY || it.channel == TargetChannel.BOTH }
    }
    val smsRulesCount = remember(rules) {
        rules.count { it.channel == TargetChannel.SMS_ONLY || it.channel == TargetChannel.BOTH }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenContainer {

            AppHeader(
                title = strings.rulesTitle,
                subtitle = strings.rulesSubtitleFormatted(rules.size),
                action = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.clickable { showSimulator = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = strings.rulesSimulatorBtn,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.rulesSimulatorBtn,
                                color = PrimaryBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )

            AppSearchFilterRow(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = strings.rulesSearchPlaceholder,
                isFilterActive = selectedChannelFilter != null || searchQuery.isNotBlank() || sortOrder != AppSortOrder.NEWEST,
                filterMenuContent = { dismiss ->
                    Text(
                        text = strings.rulesSortTitle,
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
                                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
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
                            text = { Text(strings.rulesResetFilter, color = DangerRed, fontSize = 13.sp) },
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppFilterChip(
                    label = strings.all,
                    count = rules.size,
                    isSelected = selectedChannelFilter == null,
                    onClick = { selectedChannelFilter = null },
                    accentColor = PrimaryBlue
                )
                AppFilterChip(
                    label = strings.calls,
                    count = callRulesCount,
                    isSelected = selectedChannelFilter == TargetChannel.CALL_ONLY,
                    onClick = { selectedChannelFilter = TargetChannel.CALL_ONLY },
                    accentColor = DangerRed
                )
                AppFilterChip(
                    label = strings.sms,
                    count = smsRulesCount,
                    isSelected = selectedChannelFilter == TargetChannel.SMS_ONLY,
                    onClick = { selectedChannelFilter = TargetChannel.SMS_ONLY },
                    accentColor = WarningAmber
                )
            }

            RuleGuidelineCard(
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (filteredRules.isEmpty()) {
                if (searchQuery.isNotBlank()) {
                    AppEmptyState(
                        icon = Icons.Default.SearchOff,
                        title = strings.rulesSearchNotFoundTitle,
                        description = strings.rulesSearchNotFoundDesc(searchQuery),
                        modifier = Modifier.weight(1f),
                        accentColor = PrimaryBlue
                    )
                } else {
                    AppEmptyState(
                        icon = Icons.Default.RuleFolder,
                        title = strings.rulesEmptyTitle,
                        description = strings.rulesEmptyDesc,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredRules, key = { it.id }) { rule ->
                        RuleItemCard(
                            rule = rule,
                            onToggle = { isEnabled -> viewModel.toggleRule(rule.id, isEnabled) },
                            onDelete = { viewModel.deleteRule(rule.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.rulesAddRuleDesc, modifier = Modifier.size(24.dp))
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { pattern, type, channel, label ->
                viewModel.addRule(pattern, type, channel, label)
                showAddDialog = false
            }
        )
    }

    if (showSimulator) {
        RuleSimulatorBottomSheet(
            viewModel = viewModel,
            onDismiss = { showSimulator = false }
        )
    }
}

@Composable
fun RuleGuidelineCard(
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isExpanded) PrimaryBlue.copy(alpha = 0.35f) else DarkBorder, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.rulesGuidelineTitle,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isExpanded) {
                            Text(
                                text = strings.rulesGuidelineSubtitle,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) strings.rulesGuidelineClose else strings.rulesGuidelineLearn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    GuidelineItemRow(
                        badge = strings.rulesGuidelinePrefixBadge,
                        badgeColor = PrimaryBlue,
                        example = "+60, 021, 140",
                        description = strings.rulesGuidelinePrefixDesc
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    GuidelineItemRow(
                        badge = strings.rulesGuidelineExactBadge,
                        badgeColor = DangerRed,
                        example = "081234567890",
                        description = strings.rulesGuidelineExactDesc
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    GuidelineItemRow(
                        badge = strings.rulesGuidelineKeywordBadge,
                        badgeColor = WarningAmber,
                        example = "pinjaman, paket kurir, undian",
                        description = strings.rulesGuidelineKeywordDesc
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidelineItemRow(
    badge: String,
    badgeColor: Color,
    example: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = badge,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = example,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun RuleItemCard(
    rule: BlockRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (rule.type) {
                            RuleType.PREFIX -> PrimaryBlue.copy(alpha = 0.12f)
                            RuleType.KEYWORD -> WarningAmber.copy(alpha = 0.12f)
                            RuleType.EXACT -> DangerRed.copy(alpha = 0.12f)
                            else -> AccentIndigo.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (rule.type) {
                        RuleType.KEYWORD -> Icons.Default.ChatBubbleOutline
                        RuleType.PREFIX -> Icons.Default.Dialpad
                        RuleType.EXACT -> Icons.Default.PhoneDisabled
                        else -> Icons.AutoMirrored.Filled.Rule
                    },
                    contentDescription = null,
                    tint = when (rule.type) {
                        RuleType.PREFIX -> PrimaryBlue
                        RuleType.KEYWORD -> WarningAmber
                        RuleType.EXACT -> DangerRed
                        else -> AccentIndigo
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Surface(
                        color = when (rule.type) {
                            RuleType.PREFIX -> PrimaryBlue.copy(alpha = 0.12f)
                            RuleType.KEYWORD -> WarningAmber.copy(alpha = 0.12f)
                            RuleType.EXACT -> DangerRed.copy(alpha = 0.12f)
                            else -> AccentIndigo.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (rule.type) {
                                RuleType.PREFIX -> PrimaryBlue.copy(alpha = 0.35f)
                                RuleType.KEYWORD -> WarningAmber.copy(alpha = 0.35f)
                                RuleType.EXACT -> DangerRed.copy(alpha = 0.35f)
                                else -> AccentIndigo.copy(alpha = 0.35f)
                            }
                        )
                    ) {
                        Text(
                            text = rule.type.getDisplayName(strings),
                            color = when (rule.type) {
                                RuleType.PREFIX -> PrimaryBlue
                                RuleType.KEYWORD -> WarningAmber
                                RuleType.EXACT -> DangerRed
                                else -> AccentIndigo
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = rule.channel.getDisplayName(strings),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = rule.pattern,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (rule.label.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rule.label,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.82f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ShieldActive,
                    uncheckedTrackColor = DarkSurfaceVariant,
                    uncheckedThumbColor = TextMuted
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = strings.delete,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
