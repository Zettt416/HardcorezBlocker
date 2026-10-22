package com.hardcoreblocker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcoreblocker.app.ui.i18n.LocalAppStrings
import com.hardcoreblocker.app.ui.i18n.getDisplayName
import com.hardcoreblocker.app.ui.theme.*
import com.hardcoreblocker.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleSimulatorBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val rules by viewModel.rules.collectAsState()
    var isSmsMode by remember { mutableStateOf(false) }
    var testInput by remember { mutableStateOf("") }

    val result = remember(testInput, isSmsMode, rules) {
        viewModel.testSimulation(testInput, isSmsMode)
    }

    val quickChips = if (!isSmsMode) {
        listOf(
            Pair("+442079460919", "Luar Negeri (+44)"),
            Pair("081299998888", "Blacklist Penipu"),
            Pair("+18005550199", "VoIP Asing (+1)"),
            Pair("081234567890", "Nomor Biasa")
        )
    } else {
        listOf(
            Pair("Paket kurir tertahan, unduh APK di bit.ly/paket", "Malware APK Kurir"),
            Pair("Selamat! Nomor Anda menang undian 100jt", "Modus Undian"),
            Pair("Surat tilang ETLE resmi unduh file berikut", "APK Surat Tilang"),
            Pair("Pinjaman online kilat tanpa agunan bunga 0%", "Spam Pinjol"),
            Pair("Kode OTP verifikasi Anda adalah 729104", "OTP Resmi"),
            Pair("Halo besok jadi ketemuan jam 1 siang?", "Pesan Normal")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.simTitle,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.simSubtitle,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switcher Mode: Panggilan vs SMS
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
                    // Panggilan
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSmsMode) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (!isSmsMode) 1.dp else 0.dp,
                                color = if (!isSmsMode) PrimaryBlue.copy(alpha = 0.45f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isSmsMode = false
                                testInput = ""
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = if (!isSmsMode) PrimaryBlue else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.simTabCall,
                                fontSize = 12.sp,
                                fontWeight = if (!isSmsMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSmsMode) PrimaryBlue else TextSecondary
                            )
                        }
                    }

                    // SMS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSmsMode) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (isSmsMode) 1.dp else 0.dp,
                                color = if (isSmsMode) PrimaryBlue.copy(alpha = 0.45f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isSmsMode = true
                                testInput = ""
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isSmsMode) PrimaryBlue else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.simTabSms,
                                fontSize = 12.sp,
                                fontWeight = if (isSmsMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSmsMode) PrimaryBlue else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Input Field
            OutlinedTextField(
                value = testInput,
                onValueChange = { testInput = it },
                placeholder = {
                    Text(
                        text = if (!isSmsMode) strings.simInputCallPlaceholder else strings.simInputSmsPlaceholder,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                trailingIcon = {
                    if (testInput.isNotEmpty()) {
                        IconButton(onClick = { testInput = "" }) {
                            Icon(Icons.Default.Close, contentDescription = strings.delete, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                maxLines = if (isSmsMode) 3 else 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Test Chips
            Text(
                text = strings.simQuickSamples,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickChips) { (sampleValue, label) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.clickable { testInput = sampleValue }
                    ) {
                        Text(
                            text = label,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Display Card
            if (testInput.isBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.simSubtitle,
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                val isBlocked = result.isBlocked
                val cardBg = if (isBlocked) DangerRed.copy(alpha = 0.12f) else ShieldActive.copy(alpha = 0.12f)
                val cardBorder = if (isBlocked) DangerRed.copy(alpha = 0.45f) else ShieldActive.copy(alpha = 0.45f)
                val accentColor = if (isBlocked) DangerRed else ShieldActive
                val statusTitle = if (isBlocked) strings.simStatusBlocked else strings.simStatusAllowed
                val statusIcon = if (isBlocked) Icons.Default.Block else Icons.Default.CheckCircle

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusTitle,
                                color = accentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = strings.simResultTitle,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = result.reason,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        if (result.matchedRule != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${strings.tabRules}: ",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${result.matchedRule.label.ifEmpty { result.matchedRule.pattern }} (${result.matchedRule.type.getDisplayName(strings)})",
                                        color = PrimaryBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
