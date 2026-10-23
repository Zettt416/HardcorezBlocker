package com.hardcorezblocker.app.data.model

enum class SmsSafetyCategory {
    ALL,
    SPAM_OR_PHISHING, // Dicurigai penipuan phishing bank, malware .apk, atau kata kunci berbahaya
    PROMO_MARKETING,  // Iklan LBA, promo komersial, pinjol, kupon diskon (bukan OTP)
    OTP_OFFICIAL,     // OTP murni & Layanan Resmi terverifikasi (bebas link mencurigakan)
    SAFE_CONTACT,     // Nomor tersimpan di Buku Kontak pengguna
    NORMAL            // SMS biasa lainnya
}

data class SmsMessageItem(
    val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val category: SmsSafetyCategory,
    val threatDetail: String? = null
)

