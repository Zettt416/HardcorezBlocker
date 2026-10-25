package com.hardcorezblocker.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.hardcorezblocker.app.data.model.BlockRule
import com.hardcorezblocker.app.data.model.BlockSettings
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.BlockingMode
import com.hardcorezblocker.app.data.model.RuleType
import com.hardcorezblocker.app.data.model.SmsSafetyCategory
import com.hardcorezblocker.app.data.model.TargetChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.regex.Pattern

class BlockRepository private constructor(context: Context) {

    private val dbHelper = BlockDatabaseHelper(context.applicationContext)
    private val prefs: SharedPreferences = context.getSharedPreferences("hardcore_blocker_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<BlockSettings> = _settingsFlow.asStateFlow()

    private val _rulesFlow = MutableStateFlow<List<BlockRule>>(emptyList())
    val rulesFlow: StateFlow<List<BlockRule>> = _rulesFlow.asStateFlow()

    private val _logsFlow = MutableStateFlow<List<BlockedLog>>(emptyList())
    val logsFlow: StateFlow<List<BlockedLog>> = _logsFlow.asStateFlow()

    init {
        refreshRules()
        refreshLogs()
    }

    companion object {
        @Volatile
        private var INSTANCE: BlockRepository? = null

        fun getInstance(context: Context): BlockRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BlockRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        private val URL_PATTERN = Pattern.compile(
            "(?:https?://|www\\.)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(?:/[^\\s]*)?",
            Pattern.CASE_INSENSITIVE
        )
        private val SCAM_TLD_REGEX = Regex(
            ".*\\.(icu|top|xyz|autos|sbs|cloud|baby|space|xin|shop|hair|online|cfd|lol|lat|run|buzz|fit|vip|fun|pw|cc|tk|ml|ga|cf|gq)$",
            RegexOption.IGNORE_CASE
        )
        private val GAMBLING_REGEX = Regex(
            "(slot\\s*gacor|maxwin|pragmatic\\s*play|zeus\\s*olympus|judol|judi\\s*online|depo\\s*pulsa\\s*tanpa\\s*potongan|deposit\\s*tanpa\\s*potongan|situs\\s*gacor|link\\s*gacor|bandar\\s*togel)",
            RegexOption.IGNORE_CASE
        )
        private val ALPHANUMERIC_SENDER_REGEX = Regex("^[a-zA-Z0-9\\-_\\s]{2,15}$")
        private val LINK_PATTERN = Pattern.compile(
            "(https?://|www\\.|bit\\.ly|s\\.id|t\\.me|tinyurl)",
            Pattern.CASE_INSENSITIVE
        )
        private val CLEAN_NUMBER_REGEX = Regex("[^0-9+]")

        val OFFICIAL_OPERATOR_DOMAINS = setOf(
            "telkomsel.com", "telkomsel.co.id", "tsel.me", "tsel.id", "tsurvey.id", "myads.id", "upoint.id",
            "indosat.com", "ioh.co.id", "im3.id", "tri.co.id", "bima.tri.co.id",
            "xl.co.id", "axis.co.id", "axisnet.id", "myxl.co.id",
            "smartfren.com", "byu.id", "rbm.goog"
        )

        val TRUSTED_OFFICIAL_DOMAINS = setOf(
            "go.id", "bps.go.id", "kominfo.go.id", "polri.go.id", "pajak.go.id", "bi.go.id", "ojk.go.id",
            "bca.co.id", "klikbca.com", "bca.id",
            "bankmandiri.co.id", "mandiri.co.id", "livinbymandiri.com",
            "bri.co.id", "bni.co.id", "cimbniaga.co.id", "btn.co.id", "bankmega.com",
            "danamon.co.id", "permatabank.com", "bankbsi.co.id",
            "dana.id", "gopay.co.id", "ovo.id", "shopee.co.id", "tokopedia.com", "wetv.vip", "wetv.me"
        )

        fun isTrustedOfficialDomain(domain: String): Boolean {
            val lower = domain.lowercase(Locale.ROOT)
            return OFFICIAL_OPERATOR_DOMAINS.any { lower == it || lower.endsWith(".$it") } ||
                    TRUSTED_OFFICIAL_DOMAINS.any { lower == it || lower.endsWith(".$it") }
        }

        fun isPersonalMobileNumber(sender: String): Boolean {
            val clean = sender.trim().replace(" ", "").replace("-", "")
            if (clean.length in 3..6 && clean.all { it.isDigit() }) {
                return false
            }
            if (clean.startsWith("08") && clean.length in 10..14 && clean.all { it.isDigit() }) {
                return true
            }
            if ((clean.startsWith("+628") || clean.startsWith("628")) && clean.length in 11..15 && clean.drop(1).all { it.isDigit() }) {
                return true
            }
            if (clean.startsWith("+") && clean.length >= 10 && clean.drop(1).all { it.isDigit() }) {
                return true
            }
            return false
        }

        fun detectPhishingThreat(sender: String, body: String): Pair<Boolean, String>? {
            val lowerBody = body.lowercase(Locale.ROOT)
            val lowerSender = sender.lowercase(Locale.ROOT)

            if (body.contains(".apk", ignoreCase = true)) {
                return Pair(true, "Malware APK terdeteksi di dalam pesan")
            }

            val matcher = URL_PATTERN.matcher(body)
            val foundDomains = mutableListOf<String>()
            while (matcher.find()) {
                val domain = matcher.group(1)?.lowercase(Locale.ROOT) ?: ""
                foundDomains.add(domain)
            }

            val isClaimingBca = lowerSender.contains("bca") ||
                    lowerBody.contains("bca") ||
                    lowerBody.contains("bank central asia")

            if (isClaimingBca && foundDomains.isNotEmpty()) {
                val legitimateBcaDomains = listOf("bca.co.id", "klikbca.com", "bca.id")
                val isOfficialBcaUrl = foundDomains.all { domain ->
                    legitimateBcaDomains.any { valid -> domain == valid || domain.endsWith(".$valid") } ||
                    isTrustedOfficialDomain(domain)
                }
                if (!isOfficialBcaUrl) {
                    val fakeDomain = foundDomains.firstOrNull { domain ->
                        !legitimateBcaDomains.any { valid -> domain == valid || domain.endsWith(".$valid") } &&
                        !isTrustedOfficialDomain(domain)
                    } ?: "Domain Palsu"
                    return Pair(true, "Phishing BCA: Domain palsu ($fakeDomain)")
                }
            }

            val isClaimingBank = lowerSender.contains("bri") || lowerSender.contains("mandiri") || lowerSender.contains("bni") ||
                    lowerBody.contains("bank mandiri") || lowerBody.contains("bank bri") || lowerBody.contains("bank bni") ||
                    lowerSender.contains("mega") || lowerBody.contains("bank mega")
            if (isClaimingBank && foundDomains.isNotEmpty()) {
                val legitimateBankDomains = listOf(
                    "bankmandiri.co.id", "mandiri.co.id", "livinbymandiri.com",
                    "bri.co.id", "bni.co.id", "cimbniaga.co.id", "btn.co.id", "bankmega.com",
                    "danamon.co.id", "permatabank.com", "bankbsi.co.id"
                )
                val isOfficialBankUrl = foundDomains.all { domain ->
                    legitimateBankDomains.any { valid -> domain == valid || domain.endsWith(".$valid") } ||
                    isTrustedOfficialDomain(domain)
                }
                if (!isOfficialBankUrl) {
                    return Pair(true, "Phishing Perbankan: Link bukan domain bank resmi")
                }
            }

            for (domain in foundDomains) {
                val isOfficialDomain = OFFICIAL_OPERATOR_DOMAINS.any { domain == it || domain.endsWith(".$it") } ||
                        TRUSTED_OFFICIAL_DOMAINS.any { domain == it || domain.endsWith(".$it") }
                if (!isOfficialDomain && SCAM_TLD_REGEX.matches(domain)) {
                    return Pair(true, "Domain phishing mencurigakan: $domain")
                }
            }

            val isScamUrgency = (lowerBody.contains("poin") && (lowerBody.contains("hangus") || lowerBody.contains("habis masa") || lowerBody.contains("kadaluarsa") || lowerBody.contains("tukarkan poin") || lowerBody.contains("tukar poin"))) ||
                    lowerBody.contains("voucher kartu kredit") ||
                    lowerBody.contains("rekening anda telah diblokir")
            if (isScamUrgency && foundDomains.isNotEmpty()) {
                val isAllOfficial = foundDomains.all { domain ->
                    OFFICIAL_OPERATOR_DOMAINS.any { domain == it || domain.endsWith(".$it") } ||
                    TRUSTED_OFFICIAL_DOMAINS.any { domain == it || domain.endsWith(".$it") }
                }
                if (!isAllOfficial) {
                    return Pair(true, "Indikasi scam tukar poin/rekening palsu")
                }
            }

            if (isPersonalMobileNumber(sender) && foundDomains.isNotEmpty()) {
                val isAllOfficial = foundDomains.all { domain ->
                    OFFICIAL_OPERATOR_DOMAINS.any { domain == it || domain.endsWith(".$it") } ||
                    TRUSTED_OFFICIAL_DOMAINS.any { domain == it || domain.endsWith(".$it") }
                }
                if (!isAllOfficial) {
                    return Pair(true, "SMS dari nomor pribadi menyertakan tautan: ${foundDomains.first()}")
                }
            }

            if (GAMBLING_REGEX.containsMatchIn(body)) {
                return Pair(true, "Indikasi scam judi online / undian palsu")
            }

            return null
        }

        fun isPromoMarketing(sender: String, body: String): Boolean {
            val lowerBody = body.lowercase(Locale.ROOT)
            val lowerSender = sender.lowercase(Locale.ROOT)

            val promoSenders = listOf("myads", "tsurvey", "promo", "iklan", "penawaran", "diskon", "deal", "reward", "info-tsel")
            if (promoSenders.any { lowerSender.contains(it) }) return true

            val promoIndicators = listOf(
                "poin", "point", "points", "tukar poin", "tukarpoin", "perolehan poin",
                "dapat poin", "bonus poin", "kumpulkan poin", "poin telkomsel", "poin reward",
                "reward", "rewards", "loyalty", "hadiah", "undian", "gebyar", "rejeki", "menangkan",
                "kuota", "paket data", "paket internet", "ekstra kuota", "bonus kuota",
                "paket nelpon", "nelpon puas", "unlimited", "isi ulang", "pulsa", "masa aktif",
                "perpanjangan paket", "roaming", "tarif hemat", "combo sakti", "*363#", "*888#",
                "*123#", "*500*", "*606#", "*700#", "mytelkomsel", "myim3", "myxl", "bima+",
                "cashback", "diskon", "promo", "promosi", "kupon", "voucher", "sale",
                "hemat", "hanya hari ini", "beli 1 gratis 1", "beli 1 dapat", "beli 2 dapat",
                "gratis 1 jam", "gratis ongkir", "flash sale", "special offer", "spesial untukmu",
                "penawaran eksklusif", "penawaran terbatas", "khusus hari ini",
                "apply kartu kredit", "paylater", "bunga 0%", "buka tabungan", "bonus s.d",
                "limit s.d", "dana instan", "limit pinjaman", "cicilan ringan",
                "myads.id", "tsel.id", "tsurvey.id", "upoint.id", "kirim stop", "ketik stop",
                "s&k berlaku", "syarat & ketentuan", "survei", "responden", "pretzel", "karaoke"
            )

            return promoIndicators.any { lowerBody.contains(it) }
        }

        fun classifySafetyCategory(
            cleanSender: String,
            body: String,
            isContact: Boolean = false,
            isCustomThreat: Boolean = false,
            customReason: String = ""
        ): Pair<SmsSafetyCategory, String?> {
            val phishingThreat = detectPhishingThreat(cleanSender, body)
            val isThreat = phishingThreat != null || isCustomThreat
            val threatReason = phishingThreat?.second ?: customReason

            val category = when {
                isThreat -> SmsSafetyCategory.SPAM_OR_PHISHING
                isContact -> SmsSafetyCategory.SAFE_CONTACT
                OtpDetector.isOtp(body) -> SmsSafetyCategory.OTP_OFFICIAL
                isPromoMarketing(cleanSender, body) -> SmsSafetyCategory.PROMO_MARKETING
                else -> SmsSafetyCategory.NORMAL
            }
            return Pair(category, if (category == SmsSafetyCategory.SPAM_OR_PHISHING) threatReason else null)
        }
    }

    object OtpDetector {
        private val OTP_PREFIX_REGEX = Regex(
            "(?:kode\\s*(?:otp|verifikasi|otentikasi|keamanan|rahasia|login|masuk|konfirmasi|aktivasi)?|otp\\s*code|verification\\s*code|security\\s*code|authentication\\s*code|login\\s*code|activation\\s*code|confirmation\\s*code|passcode)\\s*(?:anda|kamu|mu|is|adalah|:)?\\s*[:=\\s-]+\\s*([A-Za-z0-9]{4,8})\\b",
            RegexOption.IGNORE_CASE
        )

        private val OTP_SUFFIX_REGEX = Regex(
            "\\b([0-9]{4,8})\\s*(?:adalah|is|merupakan)?\\s*(?:kode\\s*(?:otp|verifikasi|otentikasi|keamanan|rahasia|login|aktivasi)|your\\s*(?:otp|verification|security|login|activation)\\s*code)",
            RegexOption.IGNORE_CASE
        )

        private val GOOGLE_OTP_REGEX = Regex(
            "\\b(G-\\d{6})\\b",
            RegexOption.IGNORE_CASE
        )

        private val OTP_KEYWORDS = listOf(
            "kode otp", "kode verifikasi", "verification code", "kode otentikasi",
            "authentication code", "one-time password", "onetime password",
            "kode login", "kode keamanan", "security code", "kode rahasia",
            "kode konfirmasi", "kode aktivasi", "activation code", "otp "
        )

        fun extractOtp(body: String): String? {
            val lower = body.lowercase(Locale.ROOT)

            if ((lower.contains("poin") || lower.contains("point") || lower.contains("diskon") || lower.contains("kuota")) &&
                !lower.contains("otp") && !lower.contains("verif") && !lower.contains("kode rahasia")
            ) {
                return null
            }

            val gMatch = GOOGLE_OTP_REGEX.find(body)
            if (gMatch != null && (lower.contains("google") || lower.contains("verif"))) {
                return gMatch.groupValues[1]
            }

            val pMatch = OTP_PREFIX_REGEX.find(body)
            if (pMatch != null) {
                val code = pMatch.groupValues[1]
                if (isValidOtpCode(code, body)) return code
            }

            val sMatch = OTP_SUFFIX_REGEX.find(body)
            if (sMatch != null) {
                val code = sMatch.groupValues[1]
                if (isValidOtpCode(code, body)) return code
            }

            val hasExplicitOtp = OTP_KEYWORDS.any { lower.contains(it) } || lower.contains("otp")
            if (hasExplicitOtp) {
                val numberRegex = Regex("\\b(\\d{4,8})\\b")
                val matches = numberRegex.findAll(body).map { it.groupValues[1] }.toList()
                for (num in matches) {
                    if (isValidOtpCode(num, body)) {
                        return num
                    }
                }
            }

            return null
        }

        private fun isValidOtpCode(code: String, fullBody: String): Boolean {
            if (code.length !in 4..8) return false
            if (code.startsWith("08") || code.startsWith("62")) return false

            val idx = fullBody.indexOf(code)
            if (idx > 0) {
                val prefix = fullBody.substring(maxOf(0, idx - 4), idx).lowercase(Locale.ROOT)
                if (prefix.contains("rp") || prefix.contains("idr")) return false
            }

            if (idx + code.length < fullBody.length) {
                val suffix = fullBody.substring(idx + code.length, minOf(fullBody.length, idx + code.length + 8)).lowercase(Locale.ROOT)
                if (suffix.startsWith(" poin") || suffix.startsWith("point") ||
                    suffix.startsWith(" gb") || suffix.startsWith(" mb") ||
                    suffix.startsWith(" kb") || suffix.startsWith(" rb") ||
                    suffix.startsWith(" jt") || suffix.startsWith(" rupiah")) {
                    return false
                }
            }

            val intVal = code.toIntOrNull()
            if (intVal != null && intVal in 2020..2035) {
                val contextAround = fullBody.substring(maxOf(0, idx - 10), minOf(fullBody.length, idx + code.length + 10)).lowercase(Locale.ROOT)
                if (!contextAround.contains("otp") && !contextAround.contains("kode") && !contextAround.contains("code")) {
                    return false
                }
            }

            return true
        }

        fun isOtp(body: String): Boolean {
            val lower = body.lowercase(Locale.ROOT)
            val hasOtpKeyword = OTP_KEYWORDS.any { lower.contains(it) } || lower.contains("otp")
            if (!hasOtpKeyword) return false
            return extractOtp(body) != null
        }
    }

    fun loadSettings(): BlockSettings {
        val isActive = prefs.getBoolean("is_active", true)
        val modeStr = prefs.getString("mode", BlockingMode.HARDCORE_CONTACTS_ONLY.name)
        val mode = try {
            BlockingMode.valueOf(modeStr ?: BlockingMode.HARDCORE_CONTACTS_ONLY.name)
        } catch (e: Exception) {
            BlockingMode.HARDCORE_CONTACTS_ONLY
        }
        val blockForeign = prefs.getBoolean("block_foreign", true)
        val blockLinks = prefs.getBoolean("block_links", true)
        val notifyBlocked = prefs.getBoolean("notify_blocked", true)
        val notifyThreat = prefs.getBoolean("notify_threat_sms", true)
        val notifyOtp = prefs.getBoolean("notify_otp_sms", true)
        val notifyPromo = prefs.getBoolean("notify_promo_sms", false)
        val notifyCalls = prefs.getBoolean("notify_blocked_calls", true)
        val themeStr = prefs.getString("theme_mode", com.hardcorezblocker.app.data.model.AppThemeMode.LIGHT.name)
        val themeMode = try {
            com.hardcorezblocker.app.data.model.AppThemeMode.valueOf(themeStr ?: com.hardcorezblocker.app.data.model.AppThemeMode.LIGHT.name)
        } catch (e: Exception) {
            com.hardcorezblocker.app.data.model.AppThemeMode.LIGHT
        }
        val langStr = prefs.getString("language", com.hardcorezblocker.app.data.model.AppLanguage.INDONESIAN.name)
        val language = try {
            com.hardcorezblocker.app.data.model.AppLanguage.valueOf(langStr ?: com.hardcorezblocker.app.data.model.AppLanguage.INDONESIAN.name)
        } catch (e: Exception) {
            com.hardcorezblocker.app.data.model.AppLanguage.INDONESIAN
        }

        return BlockSettings(
            isProtectionActive = isActive,
            mode = mode,
            blockForeignNumbers = blockForeign,
            blockSuspiciousLinksInSms = blockLinks,
            sendNotificationOnBlock = notifyBlocked,
            notifyThreatSms = notifyThreat,
            notifyOtpSms = notifyOtp,
            notifyPromoSms = notifyPromo,
            notifyBlockedCalls = notifyCalls,
            themeMode = themeMode,
            language = language
        )
    }

    fun updateSettings(newSettings: BlockSettings) {
        prefs.edit().apply {
            putBoolean("is_active", newSettings.isProtectionActive)
            putString("mode", newSettings.mode.name)
            putBoolean("block_foreign", newSettings.blockForeignNumbers)
            putBoolean("block_links", newSettings.blockSuspiciousLinksInSms)
            putBoolean("notify_blocked", newSettings.sendNotificationOnBlock)
            putBoolean("notify_threat_sms", newSettings.notifyThreatSms)
            putBoolean("notify_otp_sms", newSettings.notifyOtpSms)
            putBoolean("notify_promo_sms", newSettings.notifyPromoSms)
            putBoolean("notify_blocked_calls", newSettings.notifyBlockedCalls)
            putString("theme_mode", newSettings.themeMode.name)
            putString("language", newSettings.language.name)
            apply()
        }
        _settingsFlow.value = newSettings
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun isSplashShown(): Boolean {
        return prefs.getBoolean("splash_shown", prefs.getBoolean("onboarding_completed", false))
    }

    fun setSplashShown(shown: Boolean) {
        prefs.edit().putBoolean("splash_shown", shown).apply()
    }

    fun refreshRules() {
        val list = mutableListOf<BlockRule>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            BlockDatabaseHelper.TABLE_RULES,
            null,
            null,
            null,
            null,
            null,
            "${BlockDatabaseHelper.COL_RULE_CREATED_AT} DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_ID))
                val pattern = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_PATTERN))
                val type = RuleType.valueOf(c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_TYPE)))
                val channel = TargetChannel.valueOf(c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_CHANNEL)))
                val label = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_LABEL)) ?: ""
                val isEnabled = c.getInt(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_ENABLED)) == 1
                val createdAt = c.getLong(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_RULE_CREATED_AT))

                list.add(BlockRule(id, pattern, type, channel, label, isEnabled, createdAt))
            }
        }
        _rulesFlow.value = list
    }

    fun addRule(rule: BlockRule) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(BlockDatabaseHelper.COL_RULE_ID, rule.id)
            put(BlockDatabaseHelper.COL_RULE_PATTERN, rule.pattern)
            put(BlockDatabaseHelper.COL_RULE_TYPE, rule.type.name)
            put(BlockDatabaseHelper.COL_RULE_CHANNEL, rule.channel.name)
            put(BlockDatabaseHelper.COL_RULE_LABEL, rule.label)
            put(BlockDatabaseHelper.COL_RULE_ENABLED, if (rule.isEnabled) 1 else 0)
            put(BlockDatabaseHelper.COL_RULE_CREATED_AT, rule.createdAt)
        }
        db.insert(BlockDatabaseHelper.TABLE_RULES, null, cv)
        refreshRules()
    }

    fun toggleRule(ruleId: String, isEnabled: Boolean) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(BlockDatabaseHelper.COL_RULE_ENABLED, if (isEnabled) 1 else 0)
        }
        db.update(BlockDatabaseHelper.TABLE_RULES, cv, "${BlockDatabaseHelper.COL_RULE_ID} = ?", arrayOf(ruleId))
        refreshRules()
    }

    fun deleteRule(ruleId: String) {
        val db = dbHelper.writableDatabase
        db.delete(BlockDatabaseHelper.TABLE_RULES, "${BlockDatabaseHelper.COL_RULE_ID} = ?", arrayOf(ruleId))
        refreshRules()
    }

    fun refreshLogs() {
        val list = mutableListOf<BlockedLog>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            BlockDatabaseHelper.TABLE_LOGS,
            null,
            null,
            null,
            null,
            null,
            "${BlockDatabaseHelper.COL_LOG_TIMESTAMP} DESC",
            "200"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_LOG_ID))
                val sender = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_LOG_SENDER))
                val channel = TargetChannel.valueOf(c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_LOG_CHANNEL)))
                val reason = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_LOG_REASON))
                val snippet = c.getString(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_LOG_SNIPPET))
                val timestamp = c.getLong(c.getColumnIndexOrThrow(BlockDatabaseHelper.COL_LOG_TIMESTAMP))

                list.add(BlockedLog(id, sender, channel, reason, snippet, timestamp))
            }
        }
        _logsFlow.value = list
    }

    fun addLog(log: BlockedLog) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(BlockDatabaseHelper.COL_LOG_ID, log.id)
            put(BlockDatabaseHelper.COL_LOG_SENDER, log.senderOrNumber)
            put(BlockDatabaseHelper.COL_LOG_CHANNEL, log.channel.name)
            put(BlockDatabaseHelper.COL_LOG_REASON, log.reason)
            put(BlockDatabaseHelper.COL_LOG_SNIPPET, log.contentSnippet)
            put(BlockDatabaseHelper.COL_LOG_TIMESTAMP, log.timestamp)
        }
        db.insert(BlockDatabaseHelper.TABLE_LOGS, null, cv)
        refreshLogs()
    }

    fun clearLogs(channel: TargetChannel? = null) {
        val db = dbHelper.writableDatabase
        if (channel == null) {
            db.delete(BlockDatabaseHelper.TABLE_LOGS, null, null)
        } else if (channel == TargetChannel.CALL_ONLY) {
            db.delete(
                BlockDatabaseHelper.TABLE_LOGS,
                "${BlockDatabaseHelper.COL_LOG_CHANNEL} = ? OR ${BlockDatabaseHelper.COL_LOG_CHANNEL} = ?",
                arrayOf(TargetChannel.CALL_ONLY.name, TargetChannel.BOTH.name)
            )
        } else if (channel == TargetChannel.SMS_ONLY) {
            db.delete(
                BlockDatabaseHelper.TABLE_LOGS,
                "${BlockDatabaseHelper.COL_LOG_CHANNEL} = ?",
                arrayOf(TargetChannel.SMS_ONLY.name)
            )
        }
        refreshLogs()
    }

    fun isNumberInContacts(context: Context, rawNumber: String): Boolean {
        if (rawNumber.isBlank() || rawNumber.any { it.isLetter() }) return false
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(rawNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
            } ?: false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun evaluateIncomingCall(context: Context, rawNumber: String?): Pair<Boolean, String> {
        val settings = loadSettings()
        if (!settings.isProtectionActive || settings.mode == BlockingMode.OFF) {
            return Pair(false, "Perlindungan nonaktif")
        }

        val number = rawNumber?.trim() ?: ""
        if (number.isEmpty()) {
            return Pair(true, "Nomor tersembunyi / Private Number")
        }

        val isContact = isNumberInContacts(context, number)
        if (isContact) {
            return Pair(false, "Nomor tersimpan di buku kontak (Aman)")
        }

        if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) {
            return Pair(true, "Nomor tidak terdaftar di buku kontak (Hardcore Protection)")
        }

        if (settings.blockForeignNumbers && number.startsWith("+") && !number.startsWith("+62")) {
            return Pair(true, "Nomor dari luar negeri / VoIP ($number)")
        }

        val activeRules = rulesFlow.value.filter { it.isEnabled && (it.channel == TargetChannel.CALL_ONLY || it.channel == TargetChannel.BOTH) }
        for (rule in activeRules) {
            when (rule.type) {
                RuleType.EXACT -> {
                    if (cleanNumber(number) == cleanNumber(rule.pattern)) {
                        return Pair(true, "Blacklist nomor: ${rule.label.ifEmpty { rule.pattern }}")
                    }
                }
                RuleType.PREFIX -> {
                    if (number.startsWith(rule.pattern)) {
                        return Pair(true, "Awalan terblokir: ${rule.pattern} (${rule.label})")
                    }
                }
                RuleType.CONTAINS -> {
                    if (number.contains(rule.pattern)) {
                        return Pair(true, "Mengandung pola angka: ${rule.pattern}")
                    }
                }
                RuleType.REGEX -> {
                    try {
                        if (Pattern.compile(rule.pattern).matcher(number).find()) {
                            return Pair(true, "Cocok pola regex: ${rule.label}")
                        }
                    } catch (e: Exception) {
                    }
                }
                else -> Unit
            }
        }

        return Pair(false, "Lolos filter (Aman)")
    }

    fun detectPhishingThreat(sender: String, body: String): Pair<Boolean, String>? =
        Companion.detectPhishingThreat(sender, body)

    fun extractOtpCode(body: String): String? = OtpDetector.extractOtp(body)
    fun isGenuineOtp(body: String): Boolean = OtpDetector.isOtp(body)

    fun isPromoMarketing(sender: String, body: String): Boolean =
        Companion.isPromoMarketing(sender, body)

    fun evaluateIncomingSms(context: Context, sender: String?, messageBody: String?): Pair<Boolean, String> {
        val settings = loadSettings()
        if (!settings.isProtectionActive || settings.mode == BlockingMode.OFF) {
            return Pair(false, "Perlindungan nonaktif")
        }

        val cleanSender = sender?.trim() ?: "Unknown"
        val body = messageBody?.trim() ?: ""

        val phishingThreat = detectPhishingThreat(cleanSender, body)
        if (phishingThreat != null) {
            return phishingThreat
        }

        if (isNumberInContacts(context, cleanSender)) {
            return Pair(false, "Pengirim tersimpan di buku kontak")
        }

        val lowerBody = body.lowercase(Locale.ROOT)
        val activeRules = rulesFlow.value.filter { it.isEnabled && (it.channel == TargetChannel.SMS_ONLY || it.channel == TargetChannel.BOTH) }
        for (rule in activeRules) {
            when (rule.type) {
                RuleType.KEYWORD -> {
                    if (lowerBody.contains(rule.pattern.lowercase(Locale.ROOT))) {
                        return Pair(true, "Kata kunci spam: \"${rule.pattern}\" (${rule.label})")
                    }
                }
                RuleType.EXACT -> {
                    val sClean = cleanNumber(cleanSender)
                    val pClean = cleanNumber(rule.pattern)
                    val matches = if (sClean.isNotEmpty() && pClean.isNotEmpty()) {
                        sClean == pClean
                    } else {
                        cleanSender.equals(rule.pattern.trim(), ignoreCase = true)
                    }
                    if (matches) {
                        return Pair(true, "Blacklist nomor SMS: ${rule.label}")
                    }
                }
                RuleType.PREFIX -> {
                    if (cleanSender.startsWith(rule.pattern)) {
                        return Pair(true, "Awalan pengirim terblokir: ${rule.pattern}")
                    }
                }
                RuleType.CONTAINS -> {
                    if (cleanSender.contains(rule.pattern, ignoreCase = true) || lowerBody.contains(rule.pattern.lowercase(Locale.ROOT))) {
                        return Pair(true, "Mengandung pola: ${rule.pattern} (${rule.label.ifEmpty { "Aturan SMS" }})")
                    }
                }
                RuleType.REGEX -> {
                    try {
                        val p = Pattern.compile(rule.pattern, Pattern.CASE_INSENSITIVE)
                        if (p.matcher(cleanSender).find() || p.matcher(body).find()) {
                            return Pair(true, "Cocok pola regex: ${rule.label.ifEmpty { rule.pattern }}")
                        }
                    } catch (_: Exception) {
                    }
                }
                else -> Unit
            }
        }

        val isEnterpriseOrShortcode = !isPersonalMobileNumber(cleanSender)
        val isOtpMessage = OtpDetector.isOtp(body)

        if (isEnterpriseOrShortcode || isOtpMessage) {
            return Pair(false, "Layanan Resmi / Operator Terverifikasi")
        }

        if (settings.blockSuspiciousLinksInSms && isPersonalMobileNumber(cleanSender)) {
            if (LINK_PATTERN.matcher(body).find()) {
                return Pair(true, "SMS nomor pribadi menyertakan tautan")
            }
        }

        return Pair(false, "Lolos filter SMS")
    }

    fun isMessageRead(context: Context, id: Long, isMms: Boolean, systemRead: Boolean): Boolean {
        if (systemRead) return true
        val prefs = context.getSharedPreferences("hardcorez_read_status", Context.MODE_PRIVATE)
        val key = if (isMms) "mms_$id" else "sms_$id"
        return prefs.getBoolean(key, false)
    }

    fun markMessageAsRead(context: Context, id: Long, isMms: Boolean) {
        val prefs = context.getSharedPreferences("hardcorez_read_status", Context.MODE_PRIVATE)
        val key = if (isMms) "mms_$id" else "sms_$id"
        prefs.edit().putBoolean(key, true).apply()

        if (!isMms) {
            try {
                val cv = android.content.ContentValues().apply { put("read", 1) }
                context.contentResolver.update(
                    Uri.parse("content://sms/$id"),
                    cv,
                    null,
                    null
                )
            } catch (_: Throwable) {
            }
        }
    }

    fun markAllMessagesAsRead(context: Context, ids: List<Long>) {
        val prefs = context.getSharedPreferences("hardcorez_read_status", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        ids.forEach { id ->
            editor.putBoolean("sms_$id", true)
            editor.putBoolean("mms_$id", true)
        }
        editor.apply()

        try {
            val cv = android.content.ContentValues().apply { put("read", 1) }
            context.contentResolver.update(
                Uri.parse("content://sms/inbox"),
                cv,
                "read = 0",
                null
            )
        } catch (_: Throwable) {
        }
    }

    fun readInboxSms(context: Context): List<com.hardcorezblocker.app.data.model.SmsMessageItem> {
        val messages = mutableListOf<com.hardcorezblocker.app.data.model.SmsMessageItem>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date", "read")

        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC")
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow("_id")
                val addressCol = it.getColumnIndexOrThrow("address")
                val bodyCol = it.getColumnIndexOrThrow("body")
                val dateCol = it.getColumnIndexOrThrow("date")
                val readCol = it.getColumnIndex("read")

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val sender = it.getString(addressCol) ?: "Unknown"
                    val body = it.getString(bodyCol) ?: ""
                    val timestamp = it.getLong(dateCol)
                    val systemRead = if (readCol != -1) it.getInt(readCol) == 1 else true
                    val isRead = isMessageRead(context, id, isMms = false, systemRead = systemRead)

                    val cleanSender = sender.trim()
                    val isContact = isNumberInContacts(context, cleanSender)
                    val (isCustomThreat, customReason) = evaluateIncomingSms(context, cleanSender, body)

                    val (category, threatDetail) = classifySafetyCategory(
                        cleanSender = cleanSender,
                        body = body,
                        isContact = isContact,
                        isCustomThreat = isCustomThreat,
                        customReason = customReason
                    )

                    messages.add(
                        com.hardcorezblocker.app.data.model.SmsMessageItem(
                            id = id,
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            category = category,
                            threatDetail = threatDetail,
                            isMms = false,
                            isRead = isRead
                        )
                    )

                    if (messages.size >= 800) break
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HardcorezBlocker", "readInboxSms: gagal membaca inbox SMS", e)
        }

        val mmsMessages = readMmsMessages(context)
        messages.addAll(mmsMessages)

        val combined = messages.sortedByDescending { it.timestamp }
        val finalResult = if (combined.size > 1000) combined.take(1000) else combined
        android.util.Log.i("HardcorezBlocker", "readInboxSms: berhasil memuat ${finalResult.size} pesan (SMS & MMS)")
        return finalResult
    }

    private fun readMmsMessages(context: Context): List<com.hardcorezblocker.app.data.model.SmsMessageItem> {
        val mmsList = mutableListOf<com.hardcorezblocker.app.data.model.SmsMessageItem>()
        val mmsUri = Uri.parse("content://mms")
        val projection = arrayOf("_id", "date", "sub")

        try {
            val cursor = context.contentResolver.query(mmsUri, projection, null, null, "date DESC")
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow("_id")
                val dateCol = it.getColumnIndexOrThrow("date")
                val subCol = it.getColumnIndexOrThrow("sub")

                while (it.moveToNext()) {
                    val mmsId = it.getLong(idCol)
                    val dateSec = it.getLong(dateCol)
                    val timestamp = if (dateSec < 10000000000L) dateSec * 1000L else dateSec
                    val subject = it.getString(subCol) ?: ""

                    val sender = extractMmsSender(context, mmsId)
                    val body = extractMmsBody(context, mmsId, subject)

                    val cleanSender = sender.trim()
                    val isContact = isNumberInContacts(context, cleanSender)
                    val (isCustomThreat, customReason) = evaluateIncomingSms(context, cleanSender, body)

                    val (category, threatDetail) = classifySafetyCategory(
                        cleanSender = cleanSender,
                        body = body,
                        isContact = isContact,
                        isCustomThreat = isCustomThreat,
                        customReason = customReason
                    )

                    val isRead = isMessageRead(context, mmsId, isMms = true, systemRead = false)

                    mmsList.add(
                        com.hardcorezblocker.app.data.model.SmsMessageItem(
                            id = mmsId,
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            category = category,
                            threatDetail = threatDetail,
                            isMms = true,
                            isRead = isRead
                        )
                    )

                    if (mmsList.size >= 300) break
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HardcorezBlocker", "readMmsMessages: gagal membaca MMS", e)
        }
        return mmsList
    }

    private fun extractMmsSender(context: Context, mmsId: Long): String {
        val addrUri = Uri.parse("content://mms/$mmsId/addr")
        try {
            val cursor = context.contentResolver.query(
                addrUri,
                arrayOf("address", "type"),
                "type = 137",
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val rawAddress = it.getString(0) ?: ""
                    if (rawAddress.isNotBlank() && !rawAddress.contains("insert-address-token", ignoreCase = true)) {
                        if (rawAddress.contains("@rbm.", ignoreCase = true)) {
                            val botPrefix = rawAddress.substringBefore("_agent@")
                                .substringBefore("@")
                                .replace(Regex("_[a-z0-9]{6,}$"), "")
                            val formatted = botPrefix.split("_", "-")
                                .filter { part -> part.isNotBlank() }
                                .joinToString(" ") { part -> part.replaceFirstChar { c -> c.uppercase() } }
                            if (formatted.isNotBlank()) return formatted
                        }
                        return rawAddress
                    }
                }
            }
        } catch (e: Exception) {
        }
        return "MMS"
    }

    private fun extractMmsBody(context: Context, mmsId: Long, subject: String): String {
        val bodyBuilder = java.lang.StringBuilder()
        try {
            val partCursor = context.contentResolver.query(
                Uri.parse("content://mms/part"),
                arrayOf("_id", "ct", "text"),
                "mid = ?",
                arrayOf(mmsId.toString()),
                null
            )
            partCursor?.use {
                while (it.moveToNext()) {
                    val partId = it.getLong(0)
                    val ct = it.getString(1) ?: ""
                    val directText = it.getString(2)

                    if (ct.equals("text/plain", ignoreCase = true)) {
                        if (!directText.isNullOrBlank()) {
                            bodyBuilder.append(directText).append(" ")
                        } else {
                            val streamText = readStreamToString(context, Uri.parse("content://mms/part/$partId"))
                            if (streamText.isNotBlank()) {
                                bodyBuilder.append(streamText).append(" ")
                            }
                        }
                    } else if (ct.contains("gsma.botmessage", ignoreCase = true) || ct.contains("json", ignoreCase = true)) {
                        val jsonStr = readStreamToString(context, Uri.parse("content://mms/part/$partId"))
                        if (jsonStr.isNotBlank()) {
                            val parsedRbm = parseRbmBotMessage(jsonStr)
                            if (parsedRbm.isNotBlank()) {
                                bodyBuilder.append(parsedRbm).append(" ")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }

        val result = bodyBuilder.toString().trim()
        return when {
            result.isNotBlank() -> result
            subject.isNotBlank() -> subject
            else -> "[Pesan Bergambar / MMS]"
        }
    }

    private fun parseRbmBotMessage(jsonStr: String): String {
        return try {
            val json = org.json.JSONObject(jsonStr)
            val card = json.optJSONObject("message")
                ?.optJSONObject("generalPurposeCard")
                ?.optJSONObject("content")
            val title = card?.optString("title") ?: ""
            val desc = card?.optString("description") ?: ""
            val suggestions = card?.optJSONArray("suggestions")
            var actionUrl = ""
            if (suggestions != null && suggestions.length() > 0) {
                val action = suggestions.optJSONObject(0)?.optJSONObject("action")
                actionUrl = action?.optJSONObject("urlAction")?.optJSONObject("openUrl")?.optString("url") ?: ""
            }
            buildString {
                if (title.isNotBlank()) append(title).append("\n")
                if (desc.isNotBlank()) append(desc)
                if (actionUrl.isNotBlank()) append("\n").append(actionUrl)
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun readStreamToString(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                java.io.BufferedReader(java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun deleteSmsBatch(context: Context, ids: List<Long>): Int {
        var count = 0
        try {
            for (id in ids) {
                val smsUri = Uri.parse("content://sms/$id")
                val deleted = context.contentResolver.delete(smsUri, null, null)
                if (deleted > 0) {
                    count += deleted
                } else {
                    val mmsUri = Uri.parse("content://mms/$id")
                    count += context.contentResolver.delete(mmsUri, null, null)
                }
            }
        } catch (e: Exception) {
        }
        return count
    }

    fun writeSmsToInbox(context: Context, sender: String, body: String, timestamp: Long): Uri? {
        return try {
            val values = ContentValues().apply {
                put(android.provider.Telephony.Sms.Inbox.ADDRESS, sender)
                put(android.provider.Telephony.Sms.Inbox.BODY, body)
                put(android.provider.Telephony.Sms.Inbox.DATE, timestamp)
                put(android.provider.Telephony.Sms.Inbox.READ, 0)
                put(android.provider.Telephony.Sms.Inbox.SEEN, 0)
            }
            context.contentResolver.insert(android.provider.Telephony.Sms.Inbox.CONTENT_URI, values)
        } catch (e: Exception) {
            null
        }
    }

    fun sendSms(context: Context, recipient: String, messageText: String): Boolean {
        return try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(android.telephony.SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                android.telephony.SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(messageText)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(recipient, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(recipient, null, messageText, null, null)
            }

            val values = ContentValues().apply {
                put(android.provider.Telephony.Sms.Sent.ADDRESS, recipient)
                put(android.provider.Telephony.Sms.Sent.BODY, messageText)
                put(android.provider.Telephony.Sms.Sent.DATE, System.currentTimeMillis())
                put(android.provider.Telephony.Sms.Sent.READ, 1)
                put(android.provider.Telephony.Sms.Sent.STATUS, android.provider.Telephony.Sms.STATUS_COMPLETE)
            }
            context.contentResolver.insert(android.provider.Telephony.Sms.Sent.CONTENT_URI, values)
            true
        } catch (e: Exception) {
            false
        }
    }

    data class SimulationResult(
        val isBlocked: Boolean,
        val reason: String,
        val matchedRule: BlockRule? = null
    )

    fun testRuleSimulation(input: String, isSms: Boolean, context: Context): SimulationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return SimulationResult(false, "Masukkan nomor telepon atau teks SMS untuk diuji")
        }

        if (!isSms) {
            val (isBlocked, reason) = evaluateIncomingCall(context, trimmed)
            var matchingRule: BlockRule? = null
            if (isBlocked) {
                val activeRules = rulesFlow.value.filter { it.isEnabled && (it.channel == TargetChannel.CALL_ONLY || it.channel == TargetChannel.BOTH) }
                for (r in activeRules) {
                    when (r.type) {
                        RuleType.EXACT -> if (cleanNumber(trimmed) == cleanNumber(r.pattern)) { matchingRule = r; break }
                        RuleType.PREFIX -> if (trimmed.startsWith(r.pattern)) { matchingRule = r; break }
                        RuleType.CONTAINS -> if (trimmed.contains(r.pattern)) { matchingRule = r; break }
                        RuleType.REGEX -> try {
                            if (Pattern.compile(r.pattern).matcher(trimmed).find()) { matchingRule = r; break }
                        } catch (e: Exception) {}
                        else -> Unit
                    }
                }
            }
            return SimulationResult(isBlocked, reason, matchingRule)
        } else {
            val (isBlocked, reason) = if (trimmed.startsWith("+") || (trimmed.all { it.isDigit() || it == '-' || it == ' ' } && trimmed.length <= 16)) {
                evaluateIncomingSms(context, trimmed, "Halo ini adalah tes pesan SMS")
            } else {
                evaluateIncomingSms(context, "081299998888", trimmed)
            }

            var matchingRule: BlockRule? = null
            if (isBlocked) {
                val lower = trimmed.lowercase(Locale.ROOT)
                val activeRules = rulesFlow.value.filter { it.isEnabled && (it.channel == TargetChannel.SMS_ONLY || it.channel == TargetChannel.BOTH) }
                for (r in activeRules) {
                    when (r.type) {
                        RuleType.KEYWORD -> if (lower.contains(r.pattern.lowercase(Locale.ROOT))) { matchingRule = r; break }
                        RuleType.EXACT -> {
                            val sClean = cleanNumber(trimmed)
                            val pClean = cleanNumber(r.pattern)
                            val matches = if (sClean.isNotEmpty() && pClean.isNotEmpty()) {
                                sClean == pClean
                            } else {
                                trimmed.equals(r.pattern.trim(), ignoreCase = true)
                            }
                            if (matches) { matchingRule = r; break }
                        }
                        RuleType.PREFIX -> if (trimmed.startsWith(r.pattern)) { matchingRule = r; break }
                        RuleType.CONTAINS -> if (trimmed.contains(r.pattern, ignoreCase = true)) { matchingRule = r; break }
                        RuleType.REGEX -> try {
                            if (Pattern.compile(r.pattern, Pattern.CASE_INSENSITIVE).matcher(trimmed).find()) { matchingRule = r; break }
                        } catch (e: Exception) {}
                        else -> Unit
                    }
                }
            }
            return SimulationResult(isBlocked, reason, matchingRule)
        }
    }

    fun exportRulesJson(): String {
        val array = org.json.JSONArray()
        for (rule in rulesFlow.value) {
            val obj = org.json.JSONObject()
            obj.put("pattern", rule.pattern)
            obj.put("type", rule.type.name)
            obj.put("channel", rule.channel.name)
            obj.put("label", rule.label)
            obj.put("isEnabled", rule.isEnabled)
            array.put(obj)
        }
        return array.toString(2)
    }

    fun importRulesJson(jsonString: String, replaceExisting: Boolean): Int {
        return try {
            val array = org.json.JSONArray(jsonString.trim())
            val importedRules = mutableListOf<BlockRule>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pattern = obj.getString("pattern").trim()
                if (pattern.isEmpty()) continue
                val type = try {
                    RuleType.valueOf(obj.getString("type"))
                } catch (e: Exception) {
                    RuleType.EXACT
                }
                val channel = try {
                    if (obj.has("channel")) TargetChannel.valueOf(obj.getString("channel")) else TargetChannel.BOTH
                } catch (e: Exception) {
                    TargetChannel.BOTH
                }
                val label = obj.optString("label", "")
                val isEnabled = obj.optBoolean("isEnabled", true)
                importedRules.add(BlockRule(pattern = pattern, type = type, channel = channel, label = label, isEnabled = isEnabled))
            }

            if (importedRules.isEmpty()) return 0

            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                if (replaceExisting) {
                    db.delete(BlockDatabaseHelper.TABLE_RULES, null, null)
                }
                for (rule in importedRules) {
                    val cv = ContentValues().apply {
                        put(BlockDatabaseHelper.COL_RULE_ID, rule.id)
                        put(BlockDatabaseHelper.COL_RULE_PATTERN, rule.pattern)
                        put(BlockDatabaseHelper.COL_RULE_TYPE, rule.type.name)
                        put(BlockDatabaseHelper.COL_RULE_CHANNEL, rule.channel.name)
                        put(BlockDatabaseHelper.COL_RULE_LABEL, rule.label)
                        put(BlockDatabaseHelper.COL_RULE_ENABLED, if (rule.isEnabled) 1 else 0)
                        put(BlockDatabaseHelper.COL_RULE_CREATED_AT, rule.createdAt)
                    }
                    db.insertWithOnConflict(BlockDatabaseHelper.TABLE_RULES, null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            refreshRules()
            importedRules.size
        } catch (e: Exception) {
            -1
        }
    }

    private fun cleanNumber(num: String): String {
        return num.replace(CLEAN_NUMBER_REGEX, "")
    }
}
