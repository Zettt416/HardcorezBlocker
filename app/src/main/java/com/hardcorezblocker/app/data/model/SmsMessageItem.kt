package com.hardcorezblocker.app.data.model

enum class SmsSafetyCategory {
    ALL,
    SPAM_OR_PHISHING,
    PROMO_MARKETING,
    OTP_OFFICIAL,
    SAFE_CONTACT,
    NORMAL
}

data class SmsMessageItem(
    val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val category: SmsSafetyCategory,
    val threatDetail: String? = null,
    val isMms: Boolean = false,
    val isRead: Boolean = true
)

