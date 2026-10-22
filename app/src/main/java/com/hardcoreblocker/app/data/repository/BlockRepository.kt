package com.hardcoreblocker.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.hardcoreblocker.app.data.model.BlockRule
import com.hardcoreblocker.app.data.model.BlockSettings
import com.hardcoreblocker.app.data.model.BlockedLog
import com.hardcoreblocker.app.data.model.BlockingMode
import com.hardcoreblocker.app.data.model.RuleType
import com.hardcoreblocker.app.data.model.TargetChannel
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
            "(slot\\s*gacor|maxwin|pragmatic|zeus|judol|deposit\\s*pulsa|menang\\s*undian|selamat\\s*anda\\s*mendapatkan)",
            RegexOption.IGNORE_CASE
        )
        private val ALPHANUMERIC_SENDER_REGEX = Regex("^[a-zA-Z\\-_\\s]{2,15}$")
        private val LINK_PATTERN = Pattern.compile(
            "(https?://|www\\.|bit\\.ly|s\\.id|t\\.me|tinyurl)",
            Pattern.CASE_INSENSITIVE
        )
        private val CLEAN_NUMBER_REGEX = Regex("[^0-9+]")
    }

    // --- Settings ---

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
        val themeStr = prefs.getString("theme_mode", com.hardcoreblocker.app.data.model.AppThemeMode.LIGHT.name)
        val themeMode = try {
            com.hardcoreblocker.app.data.model.AppThemeMode.valueOf(themeStr ?: com.hardcoreblocker.app.data.model.AppThemeMode.LIGHT.name)
        } catch (e: Exception) {
            com.hardcoreblocker.app.data.model.AppThemeMode.LIGHT
        }
        val langStr = prefs.getString("language", com.hardcoreblocker.app.data.model.AppLanguage.INDONESIAN.name)
        val language = try {
            com.hardcoreblocker.app.data.model.AppLanguage.valueOf(langStr ?: com.hardcoreblocker.app.data.model.AppLanguage.INDONESIAN.name)
        } catch (e: Exception) {
            com.hardcoreblocker.app.data.model.AppLanguage.INDONESIAN
        }

        return BlockSettings(
            isProtectionActive = isActive,
            mode = mode,
            blockForeignNumbers = blockForeign,
            blockSuspiciousLinksInSms = blockLinks,
            sendNotificationOnBlock = notifyBlocked,
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

    // --- Rules CRUD ---

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

    // --- Logs CRUD ---

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
            "200" // Batasi 200 log terbaru agar cepat
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

    // --- Decision Engine (Deteksi & Evaluasi Instan) ---

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

    /**
     * Evaluasi panggilan masuk (dieksekusi langsung di onScreenCall dalam < 2 ms)
     * @return Pair(shouldBlock: Boolean, reason: String)
     */
    fun evaluateIncomingCall(context: Context, rawNumber: String?): Pair<Boolean, String> {
        val settings = loadSettings()
        if (!settings.isProtectionActive || settings.mode == BlockingMode.OFF) {
            return Pair(false, "Perlindungan nonaktif")
        }

        val number = rawNumber?.trim() ?: ""
        if (number.isEmpty()) {
            return Pair(true, "Nomor tersembunyi / Private Number")
        }

        // 1. Cek apakah ada di Kontak Whitelist
        val isContact = isNumberInContacts(context, number)
        if (isContact) {
            return Pair(false, "Nomor tersimpan di buku kontak (Aman)")
        }

        // 2. Jika HARDCORE MODE: Semua nomor non-kontak ditolak otomatis!
        if (settings.mode == BlockingMode.HARDCORE_CONTACTS_ONLY) {
            return Pair(true, "Nomor tidak terdaftar di buku kontak (Hardcore Protection)")
        }

        // 3. SMART FILTER MODE:
        // A. Cek nomor asing (+... yang bukan +62)
        if (settings.blockForeignNumbers && number.startsWith("+") && !number.startsWith("+62")) {
            return Pair(true, "Nomor dari luar negeri / VoIP ($number)")
        }

        // B. Cek daftar aturan database
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
                        // ignore bad regex
                    }
                }
                else -> Unit
            }
        }

        return Pair(false, "Lolos filter (Aman)")
    }

    /**
     * Evaluasi SMS Masuk
     * @return Pair(shouldBlock: Boolean, reason: String)
     */
    /**
     * Memeriksa apakah sebuah pesan SMS mengandung link/konten phishing berbahaya,
     * terutama spoofing perbankan (BCA, Mandiri, BRI, dll) atau TLD scam berbahaya.
     */
    fun detectPhishingThreat(sender: String, body: String): Pair<Boolean, String>? {
        val lowerBody = body.lowercase(Locale.ROOT)
        val lowerSender = sender.lowercase(Locale.ROOT)

        // 1. Deteksi Malware APK
        if (body.contains(".apk", ignoreCase = true)) {
            return Pair(true, "Malware APK terdeteksi di dalam pesan")
        }

        // 2. Ekstrak URL/Domain dari teks pesan
        val matcher = URL_PATTERN.matcher(body)
        val foundDomains = mutableListOf<String>()
        while (matcher.find()) {
            val domain = matcher.group(1)?.lowercase(Locale.ROOT) ?: ""
            foundDomains.add(domain)
        }

        // 3. Phishing Perbankan Khusus (BCA Spoofing)
        val isClaimingBca = lowerSender.contains("bca") ||
                lowerBody.contains("bca") ||
                lowerBody.contains("bank central asia")
        
        if (isClaimingBca && foundDomains.isNotEmpty()) {
            val legitimateBcaDomains = listOf("bca.co.id", "klikbca.com", "bca.id")
            val isOfficialBcaUrl = foundDomains.all { domain ->
                legitimateBcaDomains.any { valid -> domain == valid || domain.endsWith(".$valid") }
            }
            if (!isOfficialBcaUrl) {
                val fakeDomain = foundDomains.firstOrNull { domain ->
                    !legitimateBcaDomains.any { valid -> domain == valid || domain.endsWith(".$valid") }
                } ?: "Domain Palsu"
                return Pair(true, "Phishing BCA: Domain palsu ($fakeDomain)")
            }
        }

        // 4. Phishing Perbankan Lainnya (Mandiri, BRI, BNI)
        val isClaimingBank = lowerSender.contains("bri") || lowerSender.contains("mandiri") || lowerSender.contains("bni") ||
                lowerBody.contains("bank mandiri") || lowerBody.contains("bank bri") || lowerBody.contains("bank bni")
        if (isClaimingBank && foundDomains.isNotEmpty()) {
            val legitimateBankDomains = listOf("bankmandiri.co.id", "mandiri.co.id", "bri.co.id", "bni.co.id")
            val isOfficialBankUrl = foundDomains.all { domain ->
                legitimateBankDomains.any { valid -> domain == valid || domain.endsWith(".$valid") }
            }
            if (!isOfficialBankUrl) {
                return Pair(true, "Phishing Perbankan: Link bukan domain bank resmi")
            }
        }

        // 5. Deteksi TLD mencurigakan yang lazim dipakai scammer
        for (domain in foundDomains) {
            if (SCAM_TLD_REGEX.matches(domain)) {
                return Pair(true, "Domain phishing mencurigakan: $domain")
            }
        }

        // 6. Deteksi Kata Kunci Phishing Poin / Rekening Hangus
        val isScamUrgency = (lowerBody.contains("poin") && (lowerBody.contains("hangus") || lowerBody.contains("habis masa") || lowerBody.contains("kadaluarsa") || lowerBody.contains("tukarkan poin") || lowerBody.contains("tukar poin"))) ||
                lowerBody.contains("voucher kartu kredit") ||
                lowerBody.contains("tarif transaksi normal") ||
                lowerBody.contains("rekening anda telah diblokir")
        if (isScamUrgency && foundDomains.isNotEmpty()) {
            return Pair(true, "Indikasi scam tukar poin/rekening palsu")
        }

        // 7. Nomor seluler biasa (08... / +628...) yang mengirim link eksternal
        val isNumericSender = sender.startsWith("+") || sender.startsWith("08") || sender.all { it.isDigit() || it == '+' || it == '-' }
        if (isNumericSender && foundDomains.isNotEmpty()) {
            return Pair(true, "SMS dari nomor pribadi menyertakan tautan: ${foundDomains.first()}")
        }

        // 8. Kata Kunci Judi Online / Undian Palsu
        if (GAMBLING_REGEX.containsMatchIn(body)) {
            return Pair(true, "Indikasi scam judi online / undian palsu")
        }

        return null
    }

    /**
     * Memeriksa apakah pesan adalah promosi marketing LBA operator
     */
    fun isPromoMarketing(sender: String, body: String): Boolean {
        val lowerBody = body.lowercase(Locale.ROOT)
        val lowerSender = sender.lowercase(Locale.ROOT)

        val promoIndicators = listOf(
            "myads.id", "tsel.id", "*606#", "*500*", "kirim stop", "ketik stop",
            "s&k berlaku", "cashback", "diskon", "promo", "kupon", "voucher",
            "hanya hari ini", "beli 1 gratis 1", "beli 1 dapat", "beli 2 dapat",
            "gratis 1 jam", "apply kartu kredit", "paylater", "bunga 0%",
            "pretzel", "karaoke", "survei", "responden", "tsurvey.id", "upoint.id",
            "poin reward", "buka tabungan", "bonus s.d", "limit s.d", "dana instan"
        )

        return promoIndicators.any { lowerBody.contains(it) } ||
                lowerSender.contains("myads") ||
                lowerSender.contains("tsurvey")
    }

    /**
     * Mengevaluasi apakah pesan SMS masuk harus diblokir secara realtime.
     * @return Pair(shouldBlock: Boolean, reason: String)
     */
    fun evaluateIncomingSms(context: Context, sender: String?, messageBody: String?): Pair<Boolean, String> {
        val settings = loadSettings()
        if (!settings.isProtectionActive || settings.mode == BlockingMode.OFF) {
            return Pair(false, "Perlindungan nonaktif")
        }

        val cleanSender = sender?.trim() ?: "Unknown"
        val body = messageBody?.trim() ?: ""

        // 1. Prioritas Utama: Periksa ancaman phishing / malware berbahaya
        val phishingThreat = detectPhishingThreat(cleanSender, body)
        if (phishingThreat != null) {
            return phishingThreat
        }

        // 2. Jika nomor kontak tersimpan, loloskan
        if (isNumberInContacts(context, cleanSender)) {
            return Pair(false, "Pengirim tersimpan di buku kontak")
        }

        // 3. Cek aturan kustom kata kunci & blacklist pengguna
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
                    if (cleanNumber(cleanSender) == cleanNumber(rule.pattern)) {
                        return Pair(true, "Blacklist nomor SMS: ${rule.label}")
                    }
                }
                RuleType.PREFIX -> {
                    if (cleanSender.startsWith(rule.pattern)) {
                        return Pair(true, "Awalan pengirim terblokir: ${rule.pattern}")
                    }
                }
                else -> Unit
            }
        }

        // 4. Loloskan pengirim alfanumerik resmi & OTP murni jika tidak terdeteksi bahaya
        val isAlphanumericSender = cleanSender.matches(ALPHANUMERIC_SENDER_REGEX)
        val isOtpMessage = body.contains("otp", ignoreCase = true) ||
                body.contains("kode verifikasi", ignoreCase = true) ||
                body.contains("verification code", ignoreCase = true) ||
                body.contains("kode keamanan", ignoreCase = true) ||
                body.contains("one-time password", ignoreCase = true) ||
                body.contains("kode login", ignoreCase = true)

        if (isAlphanumericSender || isOtpMessage) {
            return Pair(false, "Layanan Resmi / OTP Terverifikasi")
        }

        // 5. Cek tautan mencurigakan umum jika opsi aktif
        if (settings.blockSuspiciousLinksInSms) {
            if (LINK_PATTERN.matcher(body).find()) {
                return Pair(true, "SMS nomor asing menyertakan tautan")
            }
        }

        return Pair(false, "Lolos filter SMS")
    }

    // --- PEMBACA KOTAK MASUK (INBOX SMS) & SCANNER ---

    fun readInboxSms(context: Context): List<com.hardcoreblocker.app.data.model.SmsMessageItem> {
        val messages = mutableListOf<com.hardcoreblocker.app.data.model.SmsMessageItem>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")

        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC")
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow("_id")
                val addressCol = it.getColumnIndexOrThrow("address")
                val bodyCol = it.getColumnIndexOrThrow("body")
                val dateCol = it.getColumnIndexOrThrow("date")

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val sender = it.getString(addressCol) ?: "Unknown"
                    val body = it.getString(bodyCol) ?: ""
                    val timestamp = it.getLong(dateCol)

                    val cleanSender = sender.trim()
                    val isContact = isNumberInContacts(context, cleanSender)
                    val phishingThreat = detectPhishingThreat(cleanSender, body)
                    val (isCustomThreat, customReason) = evaluateIncomingSms(context, cleanSender, body)

                    val isThreat = phishingThreat != null || isCustomThreat
                    val threatReason = phishingThreat?.second ?: customReason

                    val category = when {
                        isThreat -> {
                            com.hardcoreblocker.app.data.model.SmsSafetyCategory.SPAM_OR_PHISHING
                        }
                        isContact -> {
                            com.hardcoreblocker.app.data.model.SmsSafetyCategory.SAFE_CONTACT
                        }
                        isPromoMarketing(cleanSender, body) -> {
                            com.hardcoreblocker.app.data.model.SmsSafetyCategory.PROMO_MARKETING
                        }
                        cleanSender.matches(ALPHANUMERIC_SENDER_REGEX) ||
                                body.contains("otp", ignoreCase = true) ||
                                body.contains("kode verifikasi", ignoreCase = true) ||
                                body.contains("verification code", ignoreCase = true) -> {
                            com.hardcoreblocker.app.data.model.SmsSafetyCategory.OTP_OFFICIAL
                        }
                        else -> {
                            com.hardcoreblocker.app.data.model.SmsSafetyCategory.NORMAL
                        }
                    }

                    messages.add(
                        com.hardcoreblocker.app.data.model.SmsMessageItem(
                            id = id,
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            category = category,
                            threatDetail = if (category == com.hardcoreblocker.app.data.model.SmsSafetyCategory.SPAM_OR_PHISHING) threatReason else null
                        )
                    )

                    // Muat seluruh kotak masuk (maksimal 1000 pesan)
                    if (messages.size >= 1000) break
                }
            }
            android.util.Log.i("HardcoreBlocker", "readInboxSms: berhasil memuat ${messages.size} pesan")
        } catch (e: Exception) {
            android.util.Log.e("HardcoreBlocker", "readInboxSms: gagal membaca inbox SMS", e)
            e.printStackTrace()
        }

        return messages
    }

    // --- PENGHAPUSAN MASSAL SMS DARI KOTAK MASUK SISTEM ---
    fun deleteSmsBatch(context: Context, ids: List<Long>): Int {
        var count = 0
        try {
            for (id in ids) {
                val uri = Uri.parse("content://sms/$id")
                val deleted = context.contentResolver.delete(uri, null, null)
                count += deleted
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    // --- SIMULATOR EVALUASI ATURAN & PENGUJIAN ---
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
                        RuleType.EXACT -> if (cleanNumber(trimmed) == cleanNumber(r.pattern)) { matchingRule = r; break }
                        RuleType.PREFIX -> if (trimmed.startsWith(r.pattern)) { matchingRule = r; break }
                        RuleType.REGEX -> try {
                            if (Pattern.compile(r.pattern).matcher(trimmed).find()) { matchingRule = r; break }
                        } catch (e: Exception) {}
                        else -> Unit
                    }
                }
            }
            return SimulationResult(isBlocked, reason, matchingRule)
        }
    }

    // --- CADANGAN & PEMULIHAN ATURAN (BACKUP & RESTORE JSON) ---
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
