package com.hardcorezblocker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hardcorezblocker.app.data.model.RuleType
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.i18n.getDisplayName
import com.hardcorezblocker.app.ui.theme.*

private data class AddRuleTab(val id: Int, val icon: ImageVector)

@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (pattern: String, type: RuleType, channel: TargetChannel, label: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedTabId by remember { mutableStateOf(0) }
    var pattern by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf(TargetChannel.BOTH) }

    val tabs = remember {
        listOf(
            AddRuleTab(0, Icons.Default.Phone),
            AddRuleTab(1, Icons.AutoMirrored.Filled.Chat)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.dialogAddRuleTitle,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Tabs: Nomor vs Kata Kunci
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        tabs.forEach { tab ->
                            val isSelected = selectedTabId == tab.id
                            val tabTitle = if (tab.id == 0) strings.dialogTabNumberPrefix else strings.dialogTabSmsKeyword
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) PrimaryBlue.copy(alpha = 0.45f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedTabId = tab.id
                                        pattern = ""
                                        label = ""
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryBlue else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tabTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryBlue else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTabId == 0) {
                    // TAB: NOMOR / AWALAN
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = { pattern = it },
                        label = { Text(strings.dialogFieldPatternNumber, fontSize = 14.sp) },
                        placeholder = { Text(strings.dialogHintPatternNumber, fontSize = 13.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Channel Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            TargetChannel.BOTH to strings.channelBoth,
                            TargetChannel.CALL_ONLY to strings.channelCallOnly,
                            TargetChannel.SMS_ONLY to strings.channelSmsOnly
                        ).forEach { (ch, chTitle) ->
                            val isChSelected = selectedChannel == ch
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChSelected) PrimaryBlue.copy(alpha = 0.12f) else DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChSelected) PrimaryBlue.copy(alpha = 0.45f) else DarkBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedChannel = ch }
                            ) {
                                Text(
                                    text = chTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isChSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChSelected) PrimaryBlue else TextSecondary,
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips
                    Text(text = strings.dialogQuickPresets, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val numberPresets = listOf(
                            Pair("+60", "Spam Luar Negeri (+60)"),
                            Pair("021", "Spam Telemarketing (021)"),
                            Pair("140", "Promo Bank (140xx)"),
                            Pair("+1", "Spam VoIP/USA (+1)")
                        )
                        items(numberPresets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.clickable {
                                    pattern = preset.first
                                    label = preset.second
                                }
                            ) {
                                Text(
                                    text = preset.first,
                                    fontSize = 12.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(strings.dialogFieldLabel, fontSize = 14.sp) },
                        placeholder = { Text(strings.dialogHintLabel, fontSize = 13.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    // TAB: KATA KUNCI SMS
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = { pattern = it },
                        label = { Text(strings.dialogFieldPatternKeyword, fontSize = 14.sp) },
                        placeholder = { Text(strings.dialogHintPatternKeyword, fontSize = 13.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips Kata Kunci
                    Text(text = strings.dialogQuickPresets, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val keywordPresets = listOf(
                            Pair("pinjaman", "Modus Pinjol Ilegal"),
                            Pair("paket kurir", "Malware APK Kurir"),
                            Pair("surat tilang", "Malware APK Tilang"),
                            Pair("klaim hadiah", "Phishing Hadiah"),
                            Pair("menang undian", "Modus Undian Palsu")
                        )
                        items(keywordPresets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.clickable {
                                    pattern = preset.first
                                    label = preset.second
                                }
                            ) {
                                Text(
                                    text = preset.first,
                                    fontSize = 12.sp,
                                    color = WarningAmber,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(strings.dialogFieldLabel, fontSize = 14.sp) },
                        placeholder = { Text(strings.dialogHintLabel, fontSize = 13.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Aksi Bawah Seimbang (50/50 Split)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(vertical = 11.dp)
                    ) {
                        Text(strings.cancel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val cleanPattern = pattern.trim()
                            if (cleanPattern.isNotBlank()) {
                                val isKeyword = selectedTabId == 1
                                val type = when {
                                    isKeyword -> RuleType.KEYWORD
                                    cleanPattern.startsWith("+") || cleanPattern.length <= 5 -> RuleType.PREFIX
                                    else -> RuleType.EXACT
                                }
                                val finalChannel = if (isKeyword) TargetChannel.SMS_ONLY else selectedChannel
                                val finalLabel = label.trim().ifEmpty {
                                    if (isKeyword) strings.ruleTypeKeyword else strings.rulesTitle
                                }
                                onAdd(cleanPattern, type, finalChannel, finalLabel)
                            }
                        },
                        enabled = pattern.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White,
                            disabledContainerColor = DarkSurfaceVariant,
                            disabledContentColor = TextMuted
                        ),
                        contentPadding = PaddingValues(vertical = 11.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.dialogSaveRuleBtn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
