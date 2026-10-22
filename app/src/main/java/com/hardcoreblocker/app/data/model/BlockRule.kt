package com.hardcoreblocker.app.data.model

import java.util.UUID

enum class RuleType(val displayName: String) {
    EXACT("Nomor Persis"),
    PREFIX("Awalan Nomor (Prefix)"),
    CONTAINS("Mengandung Angka"),
    KEYWORD("Kata Kunci SMS"),
    REGEX("Ekspresi Pola (Regex)")
}

enum class TargetChannel(val displayName: String) {
    CALL_ONLY("Panggilan"),
    SMS_ONLY("SMS"),
    BOTH("Panggilan & SMS")
}

data class BlockRule(
    val id: String = UUID.randomUUID().toString(),
    val pattern: String,
    val type: RuleType,
    val channel: TargetChannel = TargetChannel.BOTH,
    val label: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
