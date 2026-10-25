package com.hardcorezblocker.app.data.model

enum class AppLanguage(val code: String, val displayName: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    ENGLISH("en", "English")
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("Sistem"),
    LIGHT("Terang"),
    DARK("Gelap")
}

data class BlockSettings(
    val isProtectionActive: Boolean = true,
    val mode: BlockingMode = BlockingMode.HARDCORE_CONTACTS_ONLY,
    val blockForeignNumbers: Boolean = true,
    val blockSuspiciousLinksInSms: Boolean = true,
    val blockPromoSms: Boolean = true,
    val sendNotificationOnBlock: Boolean = true,
    val notifyThreatSms: Boolean = true,
    val notifyOtpSms: Boolean = true,
    val notifyPromoSms: Boolean = false,
    val notifyBlockedCalls: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val language: AppLanguage = AppLanguage.INDONESIAN
)

