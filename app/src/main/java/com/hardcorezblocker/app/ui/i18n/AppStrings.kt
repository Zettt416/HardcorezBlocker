package com.hardcorezblocker.app.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.hardcorezblocker.app.data.model.AppLanguage
import com.hardcorezblocker.app.data.model.RuleType
import com.hardcorezblocker.app.data.model.TargetChannel

interface Strings {

    val appName: String
    val appSubtitle: String
    val active: String
    val inactive: String
    val cancel: String
    val save: String
    val delete: String
    val close: String
    val copy: String
    val done: String
    val search: String
    val filter: String
    val all: String
    val calls: String
    val sms: String
    val back: String
    val skip: String
    val next: String
    val getStarted: String
    val stepOf: (Int, Int) -> String

    val tabDashboard: String
    val tabInbox: String
    val tabRules: String
    val tabLogs: String
    val tabSettings: String

    val splashSubtitle: String

    val onbStep1Title: String
    val onbStep1Subtitle: String
    val onbFeat1Title: String
    val onbFeat1Desc: String
    val onbFeat2Title: String
    val onbFeat2Desc: String
    val onbFeat3Title: String
    val onbFeat3Desc: String

    val onbStep2Title: String
    val onbStep2Subtitle: String
    val onbModeHardcoreTitle: String
    val onbModeHardcoreDesc: String
    val onbModeHardcoreBest: String
    val onbModeSmartTitle: String
    val onbModeSmartDesc: String
    val onbModeSmartBest: String

    val onbStep3Title: String
    val onbStep3Subtitle: String
    val onbPermRoleTitle: String
    val onbPermRoleDesc: String
    val onbPermContactsTitle: String
    val onbPermContactsDesc: String
    val onbPermSmsTitle: String
    val onbPermSmsDesc: String

    val dashHeaderSubtitle: String
    val dashProtectionActive: String
    val dashProtectionInactive: String
    val dashDeviceProtected: String
    val dashProtectionPaused: String
    val dashTapToDisable: String
    val dashTapToEnable: String
    val dashPermAlertTitle: String
    val dashPermAlertDesc: String
    val dashGrantPerm: String
    val dashModeTitle: String
    val dashModeHardcore: String
    val dashModeHardcoreTitle: String
    val dashModeHardcoreSub: String
    val dashModeSmart: String
    val dashModeSmartTitle: String
    val dashModeSmartSub: String
    val dashStatusHardcoreDesc: String
    val dashStatusSmartDesc: String
    val dashStatusPausedDesc: String
    val dashModeOff: String
    val dashStatBlockedCalls: String
    val dashStatBlockedSms: String
    val dashStatActiveRules: String
    val dashStatWhitelist: String
    val dashQuickActions: String
    val dashActionAddRule: String
    val dashActionCheckSms: String
    val dashActionTestNumber: String
    val dashActionSystemStatus: String
    val dashRecentActivity: String
    val dashViewAll: String
    val dashNoRecentActivity: String
    val dashCallBlockedTag: String
    val dashSmsBlockedTag: String

    val inboxTitle: String
    val inboxFilterAll: String
    val inboxFilterPhishing: String
    val inboxFilterPromo: String
    val inboxFilterOtp: String
    val inboxFilterInfo: String
    val inboxFilterContacts: String
    val inboxSearchPlaceholder: String
    val inboxShowingCount: (Int) -> String
    val inboxSelectedCount: (Int) -> String
    val inboxSelectAll: String
    val inboxEmptyTitle: String
    val inboxEmptySubtitle: String
    val inboxEmptySearchTitle: String
    val inboxEmptySearchSubtitle: String
    val inboxBannerSmsRoleTitle: String
    val inboxBannerSmsRoleDesc: String
    val inboxBannerSmsRoleBtn: String
    val inboxDeleteConfirmTitle: (Int) -> String
    val inboxDeleteConfirmMsg: String
    val inboxMarkAllRead: String
    val toastAllRead: String
    val inboxThreatBannerTitle: (Int) -> String
    val inboxThreatBannerDesc: String
    val inboxThreatBannerBtn: String
    val inboxUnreadBadge: String
    val inboxComposeSmsTitle: String
    val inboxComposeRecipientLabel: String
    val inboxComposeRecipientPlaceholder: String
    val inboxComposeMessageLabel: String
    val inboxComposeMessagePlaceholder: String
    val inboxComposeSendBtn: String

    val rulesTitle: String
    val rulesSubtitle: String
    val rulesSubtitleFormatted: (Int) -> String
    val rulesSimulatorBtn: String
    val rulesTabBlacklist: String
    val rulesTabWhitelist: String
    val rulesTabKeywords: String
    val rulesTabPrefix: String
    val rulesSearchPlaceholder: String
    val rulesSortTitle: String
    val rulesResetFilter: String
    val rulesActiveCount: (Int) -> String
    val rulesAddBtn: String
    val rulesGuidelineTitle: String
    val rulesGuidelineSubtitle: String
    val rulesGuidelineClose: String
    val rulesGuidelineLearn: String
    val rulesGuidelinePrefixBadge: String
    val rulesGuidelinePrefixDesc: String
    val rulesGuidelineExactBadge: String
    val rulesGuidelineExactDesc: String
    val rulesGuidelineKeywordBadge: String
    val rulesGuidelineKeywordDesc: String
    val rulesSearchNotFoundTitle: String
    val rulesSearchNotFoundDesc: (String) -> String
    val rulesEmptyTitle: String
    val rulesEmptyDesc: String
    val rulesEmptySubtitle: String
    val rulesAddRuleDesc: String
    val rulesDeleteConfirmTitle: String
    val rulesDeleteConfirmMsg: (String) -> String

    val ruleTypeExact: String
    val ruleTypePrefix: String
    val ruleTypeContains: String
    val ruleTypeKeyword: String
    val ruleTypeRegex: String
    val channelBoth: String
    val channelCallOnly: String
    val channelSmsOnly: String

    val logsTitle: String
    val logsSubtitle: String
    val logsHeaderSubtitle: (Int) -> String
    val logsFilterAll: String
    val logsFilterCalls: String
    val logsFilterSms: String
    val logsClearBtn: String
    val logsSortTitle: String
    val logsResetFilter: String
    val logsSearchPlaceholder: String
    val logsSearchNotFoundTitle: String
    val logsSearchNotFoundDesc: (String) -> String
    val logsEmptyAllTitle: String
    val logsEmptyAllDesc: String
    val logsEmptyCategoryTitle: String
    val logsEmptyCategoryDesc: String
    val logsEmptyTitle: String
    val logsEmptySubtitle: String
    val logsClearDialogTitle: String
    val logsClearDialogPrompt: String
    val logsClearDialogDesc: String
    val logsClearDialogAll: String
    val logsClearDialogCalls: String
    val logsClearDialogSms: String
    val logsClearConfirmBtn: (Int) -> String
    val logsClearAll: (Int) -> String
    val logsClearCallsOnly: (Int) -> String
    val logsClearSmsOnly: (Int) -> String
    val logsReasonPrefix: String
    val logsContentPrefix: String

    val settingsTitle: String
    val settingsSubtitle: String
    val settingsSecTheme: String
    val settingsThemeVisualMode: String
    val settingsThemeVisualDesc: String
    val settingsThemeSystem: String
    val settingsThemeLight: String
    val settingsThemeDark: String
    val settingsSecLanguage: String
    val settingsLanguageTitle: String
    val settingsLanguageDesc: String
    val settingsLangId: String
    val settingsLangEn: String
    val settingsSecFilters: String
    val settingsForeignTitle: String
    val settingsForeignDesc: String
    val settingsSuspiciousLinksTitle: String
    val settingsSuspiciousLinksDesc: String
    val settingsNotifyTitle: String
    val settingsNotifyDesc: String
    val settingsSecNotificationControl: String
    val settingsNotifyThreatTitle: String
    val settingsNotifyThreatDesc: String
    val settingsNotifyOtpTitle: String
    val settingsNotifyOtpDesc: String
    val settingsNotifyPromoTitle: String
    val settingsNotifyPromoDesc: String
    val settingsNotifyCallsTitle: String
    val settingsNotifyCallsDesc: String
    val settingsTestBadgeTitle: String
    val settingsTestBadgeDesc: String
    val settingsTestBadgeBtn: String
    val settingsTestBadgeSuccess: String
    val settingsSystemBadgeTitle: String
    val settingsSystemBadgeDesc: String
    val settingsSystemBadgeBtn: String
    val settingsSecPermissions: String
    val settingsPermRole: String
    val settingsPermRoleActive: String
    val settingsPermRoleMissing: String
    val settingsPermContacts: String
    val settingsPermContactsGranted: String
    val settingsPermContactsMissing: String
    val settingsPermSms: String
    val settingsPermSmsGranted: String
    val settingsPermSmsMissing: String
    val settingsSecTutorial: String
    val settingsTutorialTitle: String
    val settingsTutorialDesc: String
    val settingsTutorialBtn: String
    val settingsSecBackup: String
    val settingsExportTitle: String
    val settingsExportDesc: String
    val settingsExportBtn: String
    val settingsImportTitle: String
    val settingsImportDesc: String
    val settingsImportBtn: String
    val settingsSecPrivacy: String
    val settingsPrivacyTitle: String
    val settingsPrivacyDesc: String
    val settingsVersion: String

    val dialogAddRuleTitle: String
    val dialogEditRuleTitle: String
    val dialogTabNumberPrefix: String
    val dialogTabSmsKeyword: String
    val dialogFieldPatternNumber: String
    val dialogFieldPatternKeyword: String
    val dialogHintPatternNumber: String
    val dialogHintPatternKeyword: String
    val dialogFieldLabel: String
    val dialogHintLabel: String
    val dialogApplyTo: String
    val dialogChannelBoth: String
    val dialogChannelCall: String
    val dialogChannelSms: String
    val dialogSaveRuleBtn: String
    val dialogQuickPresets: String

    val simTitle: String
    val simSubtitle: String
    val simTabCall: String
    val simTabSms: String
    val simFieldCall: String
    val simFieldSms: String
    val simQuickSamples: String
    val simResultTitle: String
    val simStatusBlocked: String
    val simStatusAllowed: String
    val simInputCallPlaceholder: String
    val simInputSmsPlaceholder: String

    val smsDetailTitle: String
    val smsThreatHeader: String
    val smsPromoHeader: String
    val smsOtpHeader: String
    val smsContactHeader: String
    val smsNormalHeader: String
    val smsThreatLinksDetected: String
    val smsOtpDetected: String
    val smsCopyOtpBtn: String
    val smsBlockSenderBtn: String
    val smsDeleteBtn: String
    val smsCopyTextBtn: String
    val smsThreatWarning: String
    val smsReplyPlaceholder: String
    val smsSendBtn: String
    val smsReplySentToast: String
    val smsReplyFailedToast: String
    val smsNonReplyableNote: String

    val backupSuccessTitle: String
    val backupSuccessDesc: String
    val backupCopyAgainBtn: String
    val backupCopiedToast: String
    val importTitle: String
    val importPasteBtn: String
    val importDesc: String
    val importReplaceCheckbox: String
    val importAppendCheckbox: String
    val importNowBtn: String
    val importEmptyToast: String

    val notifCallBlockedTitle: (String) -> String
    val notifCallBlockedReason: (String) -> String
    val notifCallBlockedBigText: (String, String) -> String
    val notifSmsScamTitle: (String) -> String
    val notifSmsScamReason: (String) -> String
    val notifSmsScamBigText: (String, String, String) -> String

    val toastRoleSuccess: String
    val toastCopiedNumber: String
    val toastCopiedText: String
    val toastCopiedOtp: (String) -> String
    val toastRuleAdded: String
    val toastSmsDeleted: (Int) -> String
    val toastImportSuccess: (Int) -> String
    val toastImportFailed: String
    val toastPressBackAgainToExit: String
}

object IndonesianStrings : Strings {
    override val appName = "HardcorezBlocker"
    override val appSubtitle = "Perlindungan Panggilan & SMS"
    override val active = "Aktif"
    override val inactive = "Nonaktif"
    override val cancel = "Batal"
    override val save = "Simpan"
    override val delete = "Hapus"
    override val close = "Tutup"
    override val copy = "Salin"
    override val done = "Selesai"
    override val search = "Cari"
    override val filter = "Filter"
    override val all = "Semua"
    override val calls = "Panggilan"
    override val sms = "SMS"
    override val back = "Kembali"
    override val skip = "Lewati"
    override val next = "Lanjut"
    override val getStarted = "Mulai Sekarang"
    override val stepOf: (Int, Int) -> String = { current, total -> "Langkah $current dari $total" }

    override val tabDashboard = "Beranda"
    override val tabInbox = "SMS"
    override val tabRules = "Aturan"
    override val tabLogs = "Riwayat"
    override val tabSettings = "Pengaturan"

    override val splashSubtitle = "100% Offline • Bebas Spam & Penipuan • Privasi Mutlak"

    override val onbStep1Title = "Selamat Datang di HardcorezBlocker"
    override val onbStep1Subtitle = "Perlindungan mutlak panggilan spam, telemarketing agresif, dan SMS phishing tanpa mengirim data Anda ke server internet."
    override val onbFeat1Title = "100% Offline & Privat"
    override val onbFeat1Desc = "Tidak ada analitik, tidak ada sinkronisasi server, tidak ada kebocoran kontak Anda."
    override val onbFeat2Title = "Pemutusan Panggilan Instan"
    override val onbFeat2Desc = "Memutus panggilan spam di tingkat sistem Android Telecom tanpa sempat berdering."
    override val onbFeat3Title = "Karantina SMS Cerdas"
    override val onbFeat3Desc = "Memisahkan link berbahaya, file APK scam, dan promosi mencurigakan ke kotak terisolasi."

    override val onbStep2Title = "Pilih Strategi Pertahanan"
    override val onbStep2Subtitle = "Tentukan seberapa agresif sistem menyaring nomor telepon dan SMS yang masuk ke perangkat Anda."
    override val onbModeHardcoreTitle = "Mode Hardcore (Perlindungan Maksimal)"
    override val onbModeHardcoreDesc = "Semua nomor tak dikenal yang tidak tersimpan di Buku Kontak Anda langsung ditolak seketika. Paling efektif menghentikan 100% penipuan."
    override val onbModeHardcoreBest = "Direkomendasikan untuk keamanan total"
    override val onbModeSmartTitle = "Mode Smart (Berbasis Aturan)"
    override val onbModeSmartDesc = "Hanya memblokir nomor dan kata kunci yang Anda masukkan ke daftar aturan (Blacklist, Awalan/Prefix, dan Kata Kunci SMS)."
    override val onbModeSmartBest = "Ideal jika sering menerima kurir / pesanan baru"

    override val onbStep3Title = "Izin & Hak Akses Sistem"
    override val onbStep3Subtitle = "HardcorezBlocker membutuhkan 3 izin resmi Android untuk dapat bertindak sebagai perisai pemblokir panggilan dan SMS."
    override val onbPermRoleTitle = "Peran Call Screening Resmi"
    override val onbPermRoleDesc = "Memberi wewenang ke sistem Android untuk menolak panggilan telepon spam sebelum perangkat Anda berdering."
    override val onbPermContactsTitle = "Buku Kontak (Hanya Baca)"
    override val onbPermContactsDesc = "Memastikan panggilan dari keluarga, teman, dan rekan kerja yang tersimpan selalu lolos tanpa terganggu."
    override val onbPermSmsTitle = "Akses Penerimaan SMS"
    override val onbPermSmsDesc = "Menganalisis tautan phishing, paket APK scam kurir, dan kata kunci penipuan secara lokal."

    override val dashHeaderSubtitle = "Perisai Panggilan & SMS Spam Aktif"
    override val dashProtectionActive = "PROTEKSI AKTIF"
    override val dashProtectionInactive = "PROTEKSI NONAKTIF"
    override val dashDeviceProtected = "Perangkat Terlindungi Mutlak"
    override val dashProtectionPaused = "Proteksi Sedang Dijeda"
    override val dashTapToDisable = "Ketuk untuk menjeda proteksi"
    override val dashTapToEnable = "Ketuk untuk mengaktifkan proteksi"
    override val dashPermAlertTitle = "Izin Call Screening Belum Aktif"
    override val dashPermAlertDesc = "Aktifkan peran sistem agar aplikasi dapat menolak panggilan spam secara otomatis sebelum berdering."
    override val dashGrantPerm = "Aktifkan Sekarang"
    override val dashModeTitle = "MODE PROTEKSI AKTIF"
    override val dashModeHardcore = "Hardcore (Hanya Kontak)"
    override val dashModeHardcoreTitle = "Hardcore"
    override val dashModeHardcoreSub = "Hanya Kontak"
    override val dashModeSmart = "Smart (Berdasarkan Aturan)"
    override val dashModeSmartTitle = "Smart Filter"
    override val dashModeSmartSub = "Basis Aturan"
    override val dashStatusHardcoreDesc = "Semua nomor di luar Kontak otomatis ditolak"
    override val dashStatusSmartDesc = "Scam & nomor mencurigakan otomatis diblokir"
    override val dashStatusPausedDesc = "Proteksi dijeda, ketuk switch untuk aktifkan"
    override val dashModeOff = "Nonaktif"
    override val dashStatBlockedCalls = "Panggilan Dicegat"
    override val dashStatBlockedSms = "SMS Dikarantina"
    override val dashStatActiveRules = "Aturan Aktif"
    override val dashStatWhitelist = "Kontak Lolos"
    override val dashQuickActions = "AKSI CEPAT"
    override val dashActionAddRule = "Tambah Aturan"
    override val dashActionCheckSms = "Karantina SMS"
    override val dashActionTestNumber = "Uji Aturan"
    override val dashActionSystemStatus = "Status Sistem"
    override val dashRecentActivity = "AKTIVITAS TERBARU"
    override val dashViewAll = "Lihat Semua"
    override val dashNoRecentActivity = "Belum ada aktivitas ancaman"
    override val dashCallBlockedTag = "Panggilan"
    override val dashSmsBlockedTag = "SMS"

    override val inboxTitle = "Karantina & Pesan SMS"
    override val inboxFilterAll = "Semua"
    override val inboxFilterPhishing = "Bahaya / Scam"
    override val inboxFilterPromo = "Promosi"
    override val inboxFilterOtp = "Kode OTP"
    override val inboxFilterInfo = "Info / Layanan"
    override val inboxFilterContacts = "Kontak Saya"
    override val inboxSearchPlaceholder = "Cari pengirim, kata kunci, isi pesan..."
    override val inboxShowingCount: (Int) -> String = { count -> "Menampilkan $count pesan SMS" }
    override val inboxSelectedCount: (Int) -> String = { count -> "$count SMS Dipilih" }
    override val inboxSelectAll = "Pilih Semua"
    override val inboxEmptyTitle = "Kotak SMS Bersih"
    override val inboxEmptySubtitle = "Tidak ada pesan SMS mencurigakan atau pesan promosi yang perlu ditinjau."
    override val inboxEmptySearchTitle = "Tidak Ditemukan"
    override val inboxEmptySearchSubtitle = "Tidak ada SMS yang sesuai dengan kata kunci pencarian Anda."
    override val inboxBannerSmsRoleTitle = "Jadikan Aplikasi SMS Utama"
    override val inboxBannerSmsRoleDesc = "Dapatkan kemampuan memblokir & menghapus SMS scam secara langsung di level sistem."
    override val inboxBannerSmsRoleBtn = "Atur Sekarang"
    override val inboxDeleteConfirmTitle: (Int) -> String = { count -> "Hapus $count Pesan SMS?" }
    override val inboxDeleteConfirmMsg = "Pesan SMS yang dipilih akan dihapus secara permanen dari perangkat Anda."
    override val inboxMarkAllRead = "Tandai Dibaca"
    override val toastAllRead = "Semua pesan ditandai telah dibaca"
    override val inboxThreatBannerTitle: (Int) -> String = { count -> "🚨 $count Pesan Bahaya / Scam Terdeteksi" }
    override val inboxThreatBannerDesc = "Dicurigai penipuan phishing atau tautan berbahaya. Ketuk untuk menyaring hanya pesan ini."
    override val inboxThreatBannerBtn = "Lihat Pesan"
    override val inboxUnreadBadge = "Baru"
    override val inboxComposeSmsTitle = "Tulis SMS Baru"
    override val inboxComposeRecipientLabel = "Nomor Tujuan"
    override val inboxComposeRecipientPlaceholder = "Nomor HP tujuan (misal: 0812...)"
    override val inboxComposeMessageLabel = "Isi Pesan"
    override val inboxComposeMessagePlaceholder = "Ketik pesan SMS di sini..."
    override val inboxComposeSendBtn = "Kirim SMS"

    override val rulesTitle = "Aturan Pemblokiran"
    override val rulesSubtitle = "Aturan penyaring panggilan & SMS spam"
    override val rulesSubtitleFormatted: (Int) -> String = { count -> "$count aturan aktif menyaring panggilan & SMS" }
    override val rulesSimulatorBtn = "Uji Aturan"
    override val rulesTabBlacklist = "Blacklist"
    override val rulesTabWhitelist = "Whitelist"
    override val rulesTabKeywords = "Kata Kunci SMS"
    override val rulesTabPrefix = "Awalan (Prefix)"
    override val rulesSearchPlaceholder = "Cari nomor, awalan, atau label aturan..."
    override val rulesSortTitle = "URUTKAN ATURAN"
    override val rulesResetFilter = "Reset Filter & Pencarian"
    override val rulesActiveCount: (Int) -> String = { count -> "$count aturan aktif" }
    override val rulesAddBtn = "Tambah Aturan"
    override val rulesGuidelineTitle = "Panduan Format Aturan"
    override val rulesGuidelineSubtitle = "Pelajari cara kerja awalan kode negara, nomor, & SMS"
    override val rulesGuidelineClose = "Tutup"
    override val rulesGuidelineLearn = "Pelajari"
    override val rulesGuidelinePrefixBadge = "Awalan (+Prefix)"
    override val rulesGuidelinePrefixDesc = "Cukup masukkan awalan nomor. Semua panggilan dari kode negara/area ini langsung ditolak otomatis."
    override val rulesGuidelineExactBadge = "Nomor Lengkap"
    override val rulesGuidelineExactDesc = "Blokir nomor telepon tunggal yang spesifik melakukan panggilan spam atau penipuan."
    override val rulesGuidelineKeywordBadge = "Kata Kunci SMS"
    override val rulesGuidelineKeywordDesc = "Deteksi isi pesan SMS masuk. Jika pesan memuat kata kunci ini, SMS langsung masuk ke karantina."
    override val rulesSearchNotFoundTitle = "Aturan Tidak Ditemukan"
    override val rulesSearchNotFoundDesc: (String) -> String = { q -> "Tidak ditemukan aturan pemblokiran untuk \"$q\"." }
    override val rulesEmptyTitle = "Tidak Ada Aturan"
    override val rulesEmptyDesc = "Ketuk tombol (+) di pojok kanan bawah untuk menambahkan nomor spam atau kata kunci terlarang."
    override val rulesEmptySubtitle = "Tambahkan nomor telepon atau kata kunci untuk mulai menyaring panggilan dan SMS."
    override val rulesAddRuleDesc = "Tambah Aturan"
    override val rulesDeleteConfirmTitle = "Hapus Aturan"
    override val rulesDeleteConfirmMsg: (String) -> String = { name -> "Apakah Anda yakin ingin menghapus aturan \"$name\"?" }

    override val ruleTypeExact = "Nomor Persis"
    override val ruleTypePrefix = "Awalan Nomor (Prefix)"
    override val ruleTypeContains = "Mengandung Angka"
    override val ruleTypeKeyword = "Kata Kunci SMS"
    override val ruleTypeRegex = "Pola Regex"
    override val channelBoth = "Panggilan & SMS"
    override val channelCallOnly = "Panggilan"
    override val channelSmsOnly = "SMS"

    override val logsTitle = "Riwayat Pemblokiran"
    override val logsSubtitle = "Catatan panggilan dan SMS yang berhasil digagalkan"
    override val logsHeaderSubtitle: (Int) -> String = { count -> "$count aktivitas ancaman telah digagalkan" }
    override val logsFilterAll = "Semua"
    override val logsFilterCalls = "Panggilan"
    override val logsFilterSms = "SMS"
    override val logsClearBtn = "Bersihkan"
    override val logsSortTitle = "URUTKAN RIWAYAT"
    override val logsResetFilter = "Reset Filter & Pencarian"
    override val logsSearchPlaceholder = "Cari nomor, pengirim, atau alasan..."
    override val logsSearchNotFoundTitle = "Riwayat Tidak Ditemukan"
    override val logsSearchNotFoundDesc: (String) -> String = { q -> "Tidak ada catatan pemblokiran yang cocok dengan \"$q\"." }
    override val logsEmptyAllTitle = "Belum Ada Aktivitas Terblokir"
    override val logsEmptyAllDesc = "Ketika ada panggilan spam atau SMS penipuan yang dicegat, catatan pemblokirannya akan langsung tampil di sini."
    override val logsEmptyCategoryTitle = "Kategori Ini Kosong"
    override val logsEmptyCategoryDesc = "Belum ada aktivitas terblokir pada kategori ini."
    override val logsEmptyTitle = "Riwayat Masih Kosong"
    override val logsEmptySubtitle = "Panggilan atau SMS yang diblokir akan dicatat secara otomatis di sini."
    override val logsClearDialogTitle = "Bersihkan Riwayat"
    override val logsClearDialogPrompt = "Pilih kategori log yang ingin dihapus:"
    override val logsClearDialogDesc = "Pilih kategori log yang ingin dihapus:"
    override val logsClearDialogAll = "Semua Riwayat"
    override val logsClearDialogCalls = "Hanya Panggilan"
    override val logsClearDialogSms = "Hanya SMS"
    override val logsClearConfirmBtn: (Int) -> String = { count -> if (count > 0) "Hapus ($count)" else "Hapus" }
    override val logsClearAll: (Int) -> String = { count -> "Semua Riwayat ($count)" }
    override val logsClearCallsOnly: (Int) -> String = { count -> "Hanya Panggilan ($count)" }
    override val logsClearSmsOnly: (Int) -> String = { count -> "Hanya SMS ($count)" }
    override val logsReasonPrefix = "Alasan: "
    override val logsContentPrefix = "Isi Pesan: "

    override val settingsTitle = "Pengaturan & Sistem"
    override val settingsSubtitle = "Tema antarmuka, bahasa, hak akses, dan privasi"
    override val settingsSecTheme = "TEMA TAMPILAN"
    override val settingsThemeVisualMode = "Mode Visual Aplikasi"
    override val settingsThemeVisualDesc = "Pilih tampilan cerah modern atau gelap pekat hemat baterai."
    override val settingsThemeSystem = "Sistem"
    override val settingsThemeLight = "Terang"
    override val settingsThemeDark = "Gelap"
    override val settingsSecLanguage = "BAHASA APLIKASI / LANGUAGE"
    override val settingsLanguageTitle = "Pilih Bahasa / Language"
    override val settingsLanguageDesc = "Ubah bahasa tampilan aplikasi secara instan tanpa perlu restart."
    override val settingsLangId = "🇮🇩 Bahasa Indonesia"
    override val settingsLangEn = "🇬🇧 English"
    override val settingsSecFilters = "FILTER & DETEKSI"
    override val settingsForeignTitle = "Blokir Nomor Asing (+...)"
    override val settingsForeignDesc = "Tolak otomatis panggilan dari kode negara luar negeri selain +62."
    override val settingsSuspiciousLinksTitle = "Deteksi Tautan Mencurigakan di SMS"
    override val settingsSuspiciousLinksDesc = "Pindai tautan phishing, file APK berbahaya, dan domain scam di pesan."
    override val settingsNotifyTitle = "Master Notifikasi Pemblokiran"
    override val settingsNotifyDesc = "Aktifkan/nonaktifkan pengiriman notifikasi dari aplikasi ini."
    override val settingsSecNotificationControl = "PILIHAN NOTIFIKASI GRANULAR"
    override val settingsNotifyThreatTitle = "Notifikasi SMS Bahaya / Scam"
    override val settingsNotifyThreatDesc = "Tampilkan notifikasi saat ada SMS scam/phishing diblokir. Matikan agar diblokir senyap tanpa notif."
    override val settingsNotifyOtpTitle = "Notifikasi Kode OTP Resmi"
    override val settingsNotifyOtpDesc = "Tampilkan notifikasi prioritas tinggi saat menerima kode OTP/verifikasi resmi."
    override val settingsNotifyPromoTitle = "Notifikasi SMS Promo / Iklan"
    override val settingsNotifyPromoDesc = "Tampilkan notifikasi untuk SMS penawaran paket kuota dan promo resmi operator."
    override val settingsNotifyCallsTitle = "Notifikasi Panggilan Diblokir"
    override val settingsNotifyCallsDesc = "Tampilkan notifikasi saat ada panggilan nomor asing/spam yang ditolak."
    override val settingsTestBadgeTitle = "Uji Notifikasi & Lencana Icon"
    override val settingsTestBadgeDesc = "Kirim notifikasi simulasi untuk memunculkan badge titik/angka di icon aplikasi launcher."
    override val settingsTestBadgeBtn = "Uji Coba"
    override val settingsTestBadgeSuccess = "Notifikasi uji coba dikirim! Cek bilah status dan icon di layar utama."
    override val settingsSystemBadgeTitle = "Setelan Lencana HP (HyperOS)"
    override val settingsSystemBadgeDesc = "Buka setelan sistem untuk memastikan opsi 'Lencana Notifikasi' aktif pada aplikasi ini."
    override val settingsSystemBadgeBtn = "Buka Setelan"
    override val settingsSecPermissions = "STATUS HAK AKSES SISTEM"
    override val settingsPermRole = "Call Screening Resmi"
    override val settingsPermRoleActive = "Aktif (Level Sistem)"
    override val settingsPermRoleMissing = "Belum Diberikan"
    override val settingsPermContacts = "Buku Kontak"
    override val settingsPermContactsGranted = "Izin Diberikan"
    override val settingsPermContactsMissing = "Perlu Izin"
    override val settingsPermSms = "Akses Baca SMS"
    override val settingsPermSmsGranted = "Izin Diberikan"
    override val settingsPermSmsMissing = "Perlu Izin"
    override val settingsSecTutorial = "PANDUAN & BANTUAN"
    override val settingsTutorialTitle = "Panduan & Tutorial"
    override val settingsTutorialDesc = "Pelajari kembali strategi blokir dan izin sistem"
    override val settingsTutorialBtn = "Buka"
    override val settingsSecBackup = "CADANGAN & PEMULIHAN"
    override val settingsExportTitle = "Ekspor Aturan (JSON)"
    override val settingsExportDesc = "Cadangkan seluruh daftar aturan ke format teks JSON"
    override val settingsExportBtn = "Ekspor"
    override val settingsImportTitle = "Impor Aturan (JSON)"
    override val settingsImportDesc = "Pulihkan aturan pemblokiran dari teks cadangan"
    override val settingsImportBtn = "Impor"
    override val settingsSecPrivacy = "PRIVASI & KEAMANAN"
    override val settingsPrivacyTitle = "100% Offline & Terenkripsi"
    override val settingsPrivacyDesc = "Aplikasi ini beroperasi tanpa server cloud eksternal. Semua kontak, aturan, dan riwayat pemblokiran diproses langsung di perangkat Anda demi menjaga kerahasiaan data pribadi."
    override val settingsVersion = "Versi Aplikasi"

    override val dialogAddRuleTitle = "Tambah Aturan"
    override val dialogEditRuleTitle = "Edit Aturan"
    override val dialogTabNumberPrefix = "Nomor / Awalan"
    override val dialogTabSmsKeyword = "Kata Kunci SMS"
    override val dialogFieldPatternNumber = "Nomor atau Awalan"
    override val dialogFieldPatternKeyword = "Kata Kunci SMS"
    override val dialogHintPatternNumber = "cth: +60, 021, 081234567890"
    override val dialogHintPatternKeyword = "cth: pinjaman, paket kurir, menang undian"
    override val dialogFieldLabel = "Keterangan (Opsional)"
    override val dialogHintLabel = "cth: Penipuan Luar Negeri"
    override val dialogApplyTo = "Terapkan Pada:"
    override val dialogChannelBoth = "Panggilan & SMS"
    override val dialogChannelCall = "Panggilan"
    override val dialogChannelSms = "SMS"
    override val dialogSaveRuleBtn = "Simpan Aturan"
    override val dialogQuickPresets = "Rekomendasi Cepat:"

    override val simTitle = "Simulator Evaluasi Aturan"
    override val simSubtitle = "Uji nomor panggilan atau teks SMS secara instan"
    override val simTabCall = "Panggilan"
    override val simTabSms = "SMS"
    override val simFieldCall = "Nomor Telepon Uji"
    override val simFieldSms = "Teks Pesan SMS Uji"
    override val simQuickSamples = "Sampel Cepat:"
    override val simResultTitle = "Hasil Evaluasi:"
    override val simStatusBlocked = "DIBLOKIR"
    override val simStatusAllowed = "LOLOS (AMAN)"
    override val simInputCallPlaceholder = "cth: +442079460919 atau 081234567890"
    override val simInputSmsPlaceholder = "Ketik atau tempel teks pesan SMS yang ingin diuji..."

    override val smsDetailTitle = "Detail Pesan SMS"
    override val smsThreatHeader = "Ancaman Keamanan Terdeteksi"
    override val smsPromoHeader = "Pesan Promosi / Marketing"
    override val smsOtpHeader = "Kode OTP / Layanan Resmi"
    override val smsContactHeader = "Pesan dari Kontak Tersimpan"
    override val smsNormalHeader = "Pesan Normal"
    override val smsThreatLinksDetected = "Tautan Mencurigakan Terdeteksi"
    override val smsOtpDetected = "Kode OTP Terdeteksi"
    override val smsCopyOtpBtn = "Salin Kode OTP"
    override val smsBlockSenderBtn = "Blokir Nomor Ini"
    override val smsDeleteBtn = "Hapus SMS"
    override val smsCopyTextBtn = "Salin Teks Pesan"
    override val smsThreatWarning = "⚠️ Peringatan: Jangan pernah mengeklik tautan atau mengunduh file APK dari pesan yang mencurigakan!"
    override val smsReplyPlaceholder = "Ketik balasan SMS..."
    override val smsSendBtn = "Kirim"
    override val smsReplySentToast = "Pesan SMS berhasil dikirim"
    override val smsReplyFailedToast = "Gagal mengirim SMS. Pastikan pulsa / sinyal mencukupi."
    override val smsNonReplyableNote = "Pengirim bertipe alfanumerik / bot (tidak dapat dibalas)"

    override val backupSuccessTitle = "Cadangan Berhasil Dibuat"
    override val backupSuccessDesc = "Data aturan telah otomatis disalin ke clipboard ponsel Anda. Anda juga dapat menyalin atau menyimpan teks JSON di bawah ini:"
    override val backupCopyAgainBtn = "Salin Teks JSON"
    override val backupCopiedToast = "Teks JSON disalin!"
    override val importTitle = "Impor Aturan Pemblokiran"
    override val importPasteBtn = "Tempel dari Clipboard"
    override val importDesc = "Tempel teks cadangan JSON aturan pemblokiran Anda di bawah ini:"
    override val importReplaceCheckbox = "Gantikan seluruh aturan yang ada sekarang"
    override val importAppendCheckbox = "Tambahkan ke aturan yang sudah ada (Gabung)"
    override val importNowBtn = "Impor Sekarang"
    override val importEmptyToast = "Teks JSON kosong!"

    override val notifCallBlockedTitle: (String) -> String = { number -> "🛡️ Panggilan Diblokir: $number" }
    override val notifCallBlockedReason: (String) -> String = { reason -> "Alasan: $reason" }
    override val notifCallBlockedBigText: (String, String) -> String = { number, reason -> "Panggilan dari $number berhasil digagalkan.\nAlasan: $reason" }
    override val notifSmsScamTitle: (String) -> String = { sender -> "⚠️ SMS Penipuan/Spam: $sender" }
    override val notifSmsScamReason: (String) -> String = { reason -> "Alasan: $reason" }
    override val notifSmsScamBigText: (String, String, String) -> String = { sender, reason, snippet -> "SMS dari $sender dicurigai sebagai penipuan.\nAlasan: $reason\n\nIsi: \"$snippet\"" }

    override val toastRoleSuccess = "Aplikasi berhasil diatur sebagai Call Screening resmi!"
    override val toastCopiedNumber = "Nomor berhasil disalin!"
    override val toastCopiedText = "Teks berhasil disalin!"
    override val toastCopiedOtp: (String) -> String = { otp -> "Kode OTP disalin: $otp" }
    override val toastRuleAdded = "Aturan berhasil ditambahkan"
    override val toastSmsDeleted: (Int) -> String = { count -> "$count SMS berhasil dihapus" }
    override val toastImportSuccess: (Int) -> String = { count -> "$count aturan berhasil diimpor!" }
    override val toastImportFailed = "Gagal mengimpor: Format JSON tidak valid"
    override val toastPressBackAgainToExit = "Tekan sekali lagi untuk keluar"
}

object EnglishStrings : Strings {
    override val appName = "HardcorezBlocker"
    override val appSubtitle = "Call & SMS Shield Protection"
    override val active = "Active"
    override val inactive = "Inactive"
    override val cancel = "Cancel"
    override val save = "Save"
    override val delete = "Delete"
    override val close = "Close"
    override val copy = "Copy"
    override val done = "Done"
    override val search = "Search"
    override val filter = "Filter"
    override val all = "All"
    override val calls = "Calls"
    override val sms = "SMS"
    override val back = "Back"
    override val skip = "Skip"
    override val next = "Next"
    override val getStarted = "Get Started"
    override val stepOf: (Int, Int) -> String = { current, total -> "Step $current of $total" }

    override val tabDashboard = "Home"
    override val tabInbox = "SMS"
    override val tabRules = "Rules"
    override val tabLogs = "Logs"
    override val tabSettings = "Settings"

    override val splashSubtitle = "100% Offline • Spam & Scam Free • Absolute Privacy"

    override val onbStep1Title = "Welcome to HardcorezBlocker"
    override val onbStep1Subtitle = "Absolute protection against spam calls, aggressive telemarketers, and phishing SMS without ever uploading data to internet servers."
    override val onbFeat1Title = "100% Offline & Private"
    override val onbFeat1Desc = "No analytics, no cloud sync, and zero contact leakage."
    override val onbFeat2Title = "Instant Call Termination"
    override val onbFeat2Desc = "Terminates spam calls directly at the Android Telecom system layer before ringing."
    override val onbFeat3Title = "Smart SMS Quarantine"
    override val onbFeat3Desc = "Separates dangerous phishing links, scam APK files, and spam into an isolated vault."

    override val onbStep2Title = "Choose Defense Strategy"
    override val onbStep2Subtitle = "Choose how strictly the system filters incoming phone calls and text messages on your device."
    override val onbModeHardcoreTitle = "Hardcore Mode (Maximum Defense)"
    override val onbModeHardcoreDesc = "All unknown numbers not saved in your Contacts book are instantly rejected. The most effective shield against 100% of scams."
    override val onbModeHardcoreBest = "Recommended for bulletproof security"
    override val onbModeSmartTitle = "Smart Mode (Rule-Based)"
    override val onbModeSmartDesc = "Only blocks numbers and keywords matching your custom rules (Blacklist, Area/Country Prefix, and SMS Keywords)."
    override val onbModeSmartBest = "Ideal if you frequently receive courier / delivery calls"

    override val onbStep3Title = "Permissions & System Roles"
    override val onbStep3Subtitle = "HardcorezBlocker requires 3 official Android permissions to act as an unyielding call and SMS shield."
    override val onbPermRoleTitle = "Official Call Screening Role"
    override val onbPermRoleDesc = "Authorizes the Android system to intercept and drop spam calls before your device rings."
    override val onbPermContactsTitle = "Contacts Book (Read-Only)"
    override val onbPermContactsDesc = "Ensures legitimate calls from family, friends, and colleagues always pass through seamlessly."
    override val onbPermSmsTitle = "SMS Receive Access"
    override val onbPermSmsDesc = "Analyzes phishing links, delivery APK scams, and fraudulent keywords 100% locally."

    override val dashHeaderSubtitle = "Spam Call & SMS Shield Active"
    override val dashProtectionActive = "SHIELD ACTIVE"
    override val dashProtectionInactive = "SHIELD PAUSED"
    override val dashDeviceProtected = "Device Fully Protected"
    override val dashProtectionPaused = "Protection Currently Paused"
    override val dashTapToDisable = "Tap to pause shield protection"
    override val dashTapToEnable = "Tap to enable shield protection"
    override val dashPermAlertTitle = "Call Screening Role Inactive"
    override val dashPermAlertDesc = "Grant the system role so the app can automatically drop spam calls before they ring."
    override val dashGrantPerm = "Enable Role Now"
    override val dashModeTitle = "ACTIVE PROTECTION MODE"
    override val dashModeHardcore = "Hardcore (Contacts Only)"
    override val dashModeHardcoreTitle = "Hardcore"
    override val dashModeHardcoreSub = "Contacts Only"
    override val dashModeSmart = "Smart (Rule-Based)"
    override val dashModeSmartTitle = "Smart Filter"
    override val dashModeSmartSub = "Rule-Based"
    override val dashStatusHardcoreDesc = "All non-contact numbers are blocked"
    override val dashStatusSmartDesc = "Scams & suspicious numbers are blocked"
    override val dashStatusPausedDesc = "Protection paused, tap switch to enable"
    override val dashModeOff = "Disabled"
    override val dashStatBlockedCalls = "Calls Intercepted"
    override val dashStatBlockedSms = "SMS Quarantined"
    override val dashStatActiveRules = "Active Rules"
    override val dashStatWhitelist = "Contacts Passed"
    override val dashQuickActions = "QUICK ACTIONS"
    override val dashActionAddRule = "Add Rule"
    override val dashActionCheckSms = "SMS Quarantine"
    override val dashActionTestNumber = "Test Rules"
    override val dashActionSystemStatus = "System Status"
    override val dashRecentActivity = "RECENT ACTIVITY"
    override val dashViewAll = "View All"
    override val dashNoRecentActivity = "No threat activity recorded yet"
    override val dashCallBlockedTag = "Call"
    override val dashSmsBlockedTag = "SMS"

    override val inboxTitle = "Quarantine & SMS Inbox"
    override val inboxFilterAll = "All"
    override val inboxFilterPhishing = "Threat / Scam"
    override val inboxFilterPromo = "Promotions"
    override val inboxFilterOtp = "OTP Codes"
    override val inboxFilterInfo = "Info / Services"
    override val inboxFilterContacts = "My Contacts"
    override val inboxSearchPlaceholder = "Search sender, keyword, content..."
    override val inboxShowingCount: (Int) -> String = { count -> "Showing $count SMS messages" }
    override val inboxSelectedCount: (Int) -> String = { count -> "$count SMS Selected" }
    override val inboxSelectAll = "Select All"
    override val inboxEmptyTitle = "SMS Inbox Clear"
    override val inboxEmptySubtitle = "No suspicious messages or unwanted promotional texts need review."
    override val inboxEmptySearchTitle = "No Results Found"
    override val inboxEmptySearchSubtitle = "No SMS messages match your search query."
    override val inboxBannerSmsRoleTitle = "Set as Default SMS App"
    override val inboxBannerSmsRoleDesc = "Gain power to block & delete spam/scam SMS directly at the system level."
    override val inboxBannerSmsRoleBtn = "Configure Now"
    override val inboxDeleteConfirmTitle: (Int) -> String = { count -> "Delete $count SMS Messages?" }
    override val inboxDeleteConfirmMsg = "Selected SMS messages will be permanently deleted from your device."
    override val inboxMarkAllRead = "Mark Read"
    override val toastAllRead = "All messages marked as read"
    override val inboxThreatBannerTitle: (Int) -> String = { count -> "🚨 $count Threats & Scams Detected" }
    override val inboxThreatBannerDesc = "Suspected phishing or unsafe links. Tap to isolate and review these messages."
    override val inboxThreatBannerBtn = "View Threats"
    override val inboxUnreadBadge = "New"
    override val inboxComposeSmsTitle = "Compose New SMS"
    override val inboxComposeRecipientLabel = "Recipient Number"
    override val inboxComposeRecipientPlaceholder = "Phone number (e.g. 0812...)"
    override val inboxComposeMessageLabel = "Message Content"
    override val inboxComposeMessagePlaceholder = "Type your SMS message here..."
    override val inboxComposeSendBtn = "Send SMS"

    override val rulesTitle = "Blocking Rules"
    override val rulesSubtitle = "Custom rules filtering spam calls & SMS"
    override val rulesSubtitleFormatted: (Int) -> String = { count -> "$count active rules filtering calls & SMS" }
    override val rulesSimulatorBtn = "Test Rules"
    override val rulesTabBlacklist = "Blacklist"
    override val rulesTabWhitelist = "Whitelist"
    override val rulesTabKeywords = "SMS Keywords"
    override val rulesTabPrefix = "Prefix Codes"
    override val rulesSearchPlaceholder = "Search number, prefix, or rule label..."
    override val rulesSortTitle = "SORT RULES"
    override val rulesResetFilter = "Reset Filter & Search"
    override val rulesActiveCount: (Int) -> String = { count -> "$count active rules" }
    override val rulesAddBtn = "Add Rule"
    override val rulesGuidelineTitle = "Rule Format Guidelines"
    override val rulesGuidelineSubtitle = "Learn how country code prefixes, exact numbers & SMS keywords work"
    override val rulesGuidelineClose = "Close"
    override val rulesGuidelineLearn = "Learn"
    override val rulesGuidelinePrefixBadge = "Prefix (+Prefix)"
    override val rulesGuidelinePrefixDesc = "Enter number prefix. All incoming calls starting with this country/area code are automatically rejected."
    override val rulesGuidelineExactBadge = "Exact Number"
    override val rulesGuidelineExactDesc = "Block a specific single phone number engaging in spam or fraudulent calls."
    override val rulesGuidelineKeywordBadge = "SMS Keyword"
    override val rulesGuidelineKeywordDesc = "Detect incoming SMS message content. If it contains this keyword, the SMS is quarantined immediately."
    override val rulesSearchNotFoundTitle = "No Rules Found"
    override val rulesSearchNotFoundDesc: (String) -> String = { q -> "No blocking rules found matching \"$q\"." }
    override val rulesEmptyTitle = "No Rules Defined"
    override val rulesEmptyDesc = "Tap the (+) button at bottom-right to add a spam number or banned keyword."
    override val rulesEmptySubtitle = "Add phone numbers or keywords to start filtering calls and SMS."
    override val rulesAddRuleDesc = "Add Rule"
    override val rulesDeleteConfirmTitle = "Delete Rule"
    override val rulesDeleteConfirmMsg: (String) -> String = { name -> "Are you sure you want to delete rule \"$name\"?" }

    override val ruleTypeExact = "Exact Number"
    override val ruleTypePrefix = "Number Prefix"
    override val ruleTypeContains = "Contains Digits"
    override val ruleTypeKeyword = "SMS Keyword"
    override val ruleTypeRegex = "Regex Pattern"
    override val channelBoth = "Calls & SMS"
    override val channelCallOnly = "Calls"
    override val channelSmsOnly = "SMS"

    override val logsTitle = "Block History & Logs"
    override val logsSubtitle = "Records of calls and SMS successfully blocked"
    override val logsHeaderSubtitle: (Int) -> String = { count -> "$count threat activities thwarted" }
    override val logsFilterAll = "All"
    override val logsFilterCalls = "Calls"
    override val logsFilterSms = "SMS"
    override val logsClearBtn = "Clear"
    override val logsSortTitle = "SORT LOGS"
    override val logsResetFilter = "Reset Filter & Search"
    override val logsSearchPlaceholder = "Search number, sender, or reason..."
    override val logsSearchNotFoundTitle = "No Logs Found"
    override val logsSearchNotFoundDesc: (String) -> String = { q -> "No blocking records matched \"$q\"." }
    override val logsEmptyAllTitle = "No Blocked Activity Yet"
    override val logsEmptyAllDesc = "When spam calls or phishing SMS are intercepted, blocking logs will appear here."
    override val logsEmptyCategoryTitle = "This Category is Empty"
    override val logsEmptyCategoryDesc = "No blocked activities recorded in this category."
    override val logsEmptyTitle = "No Logs Yet"
    override val logsEmptySubtitle = "Calls or SMS that get blocked will be automatically recorded here."
    override val logsClearDialogTitle = "Clear Block History"
    override val logsClearDialogPrompt = "Select history logs to remove from storage:"
    override val logsClearDialogDesc = "Select log category to clear:"
    override val logsClearDialogAll = "All History"
    override val logsClearDialogCalls = "Calls Only"
    override val logsClearDialogSms = "SMS Only"
    override val logsClearConfirmBtn: (Int) -> String = { count -> if (count > 0) "Clear ($count)" else "Clear" }
    override val logsClearAll: (Int) -> String = { count -> "All History ($count)" }
    override val logsClearCallsOnly: (Int) -> String = { count -> "Calls Only ($count)" }
    override val logsClearSmsOnly: (Int) -> String = { count -> "SMS Only ($count)" }
    override val logsReasonPrefix = "Reason: "
    override val logsContentPrefix = "Content: "

    override val settingsTitle = "Settings & System"
    override val settingsSubtitle = "App theme, language, permissions, and privacy"
    override val settingsSecTheme = "APPEARANCE THEME"
    override val settingsThemeVisualMode = "App Visual Mode"
    override val settingsThemeVisualDesc = "Choose clean modern light mode or battery-saving dark mode."
    override val settingsThemeSystem = "System"
    override val settingsThemeLight = "Light"
    override val settingsThemeDark = "Dark"
    override val settingsSecLanguage = "APP LANGUAGE / BAHASA"
    override val settingsLanguageTitle = "App Language Selection"
    override val settingsLanguageDesc = "Change app display language instantly without restarting."
    override val settingsLangId = "🇮🇩 Bahasa Indonesia"
    override val settingsLangEn = "🇬🇧 English"
    override val settingsSecFilters = "FILTERS & DETECTION"
    override val settingsForeignTitle = "Block Foreign Numbers (+...)"
    override val settingsForeignDesc = "Automatically reject calls with foreign country codes other than +62."
    override val settingsSuspiciousLinksTitle = "Detect Suspicious Links in SMS"
    override val settingsSuspiciousLinksDesc = "Scan for phishing links, dangerous APK files, and scam domains in messages."
    override val settingsNotifyTitle = "Master Block Notifications"
    override val settingsNotifyDesc = "Enable or disable all notifications sent by this app."
    override val settingsSecNotificationControl = "GRANULAR NOTIFICATION CONTROLS"
    override val settingsNotifyThreatTitle = "Threat / Scam SMS Notification"
    override val settingsNotifyThreatDesc = "Show notification when scam/phishing SMS is blocked. Turn off for silent quarantine."
    override val settingsNotifyOtpTitle = "Official OTP SMS Notification"
    override val settingsNotifyOtpDesc = "Show high-priority notification with quick-copy for official OTP codes."
    override val settingsNotifyPromoTitle = "Promo & Marketing SMS Notification"
    override val settingsNotifyPromoDesc = "Show notification for official commercial carrier promos and offers."
    override val settingsNotifyCallsTitle = "Blocked Calls Notification"
    override val settingsNotifyCallsDesc = "Show notification when an unwanted or foreign call is rejected."
    override val settingsTestBadgeTitle = "Test Notification & App Badge"
    override val settingsTestBadgeDesc = "Send a test notification to trigger the dot/number badge on the launcher app icon."
    override val settingsTestBadgeBtn = "Test"
    override val settingsTestBadgeSuccess = "Test notification sent! Check status bar and home screen icon."
    override val settingsSystemBadgeTitle = "System Badge Settings (HyperOS)"
    override val settingsSystemBadgeDesc = "Open system settings to verify 'Notification Badges' is enabled for this app."
    override val settingsSystemBadgeBtn = "Open Settings"
    override val settingsSecPermissions = "SYSTEM PERMISSIONS STATUS"
    override val settingsPermRole = "Official Call Screening"
    override val settingsPermRoleActive = "Active (System Level)"
    override val settingsPermRoleMissing = "Not Granted"
    override val settingsPermContacts = "Contacts Access"
    override val settingsPermContactsGranted = "Permission Granted"
    override val settingsPermContactsMissing = "Permission Needed"
    override val settingsPermSms = "SMS Read Access"
    override val settingsPermSmsGranted = "Permission Granted"
    override val settingsPermSmsMissing = "Permission Needed"
    override val settingsSecTutorial = "GUIDE & HELP"
    override val settingsTutorialTitle = "Guide & Tutorial"
    override val settingsTutorialDesc = "Review blocking strategies and system permissions"
    override val settingsTutorialBtn = "Open"
    override val settingsSecBackup = "BACKUP & RESTORE"
    override val settingsExportTitle = "Export Rules (JSON)"
    override val settingsExportDesc = "Backup all rules to JSON text format"
    override val settingsExportBtn = "Export"
    override val settingsImportTitle = "Import Rules (JSON)"
    override val settingsImportDesc = "Restore blocking rules from backup text"
    override val settingsImportBtn = "Import"
    override val settingsSecPrivacy = "PRIVACY & SECURITY"
    override val settingsPrivacyTitle = "100% Offline & Encrypted"
    override val settingsPrivacyDesc = "This app operates without external cloud servers. All contacts, rules, and block history are processed locally on your device to protect your privacy."
    override val settingsVersion = "App Version"

    override val dialogAddRuleTitle = "Add Rule"
    override val dialogEditRuleTitle = "Edit Rule"
    override val dialogTabNumberPrefix = "Number / Prefix"
    override val dialogTabSmsKeyword = "SMS Keyword"
    override val dialogFieldPatternNumber = "Phone Number or Prefix"
    override val dialogFieldPatternKeyword = "SMS Keyword Text"
    override val dialogHintPatternNumber = "e.g.: +60, 021, 081234567890"
    override val dialogHintPatternKeyword = "e.g.: loan, casino, package delivery, lottery winner"
    override val dialogFieldLabel = "Label / Note (Optional)"
    override val dialogHintLabel = "e.g.: Foreign Telemarketer"
    override val dialogApplyTo = "Apply To:"
    override val dialogChannelBoth = "Calls & SMS"
    override val dialogChannelCall = "Calls Only"
    override val dialogChannelSms = "SMS Only"
    override val dialogSaveRuleBtn = "Save Rule"
    override val dialogQuickPresets = "Quick Presets:"

    override val simTitle = "Rule Evaluation Simulator"
    override val simSubtitle = "Test phone numbers or SMS text instantly against active rules"
    override val simTabCall = "Call"
    override val simTabSms = "SMS"
    override val simFieldCall = "Number to Test"
    override val simFieldSms = "SMS Text to Test"
    override val simQuickSamples = "Quick Samples:"
    override val simResultTitle = "Evaluation Result:"
    override val simStatusBlocked = "BLOCKED"
    override val simStatusAllowed = "ALLOWED (SAFE)"
    override val simInputCallPlaceholder = "e.g.: +442079460919 or 081234567890"
    override val simInputSmsPlaceholder = "Type or paste SMS message text to test..."

    override val smsDetailTitle = "SMS Details"
    override val smsThreatHeader = "Security Threat Detected"
    override val smsPromoHeader = "Promotional / Marketing SMS"
    override val smsOtpHeader = "OTP Code / Official Service"
    override val smsContactHeader = "Message from Saved Contact"
    override val smsNormalHeader = "Normal Message"
    override val smsThreatLinksDetected = "Suspicious Links Detected"
    override val smsOtpDetected = "Detected OTP Code"
    override val smsCopyOtpBtn = "Copy OTP Code"
    override val smsBlockSenderBtn = "Block This Number"
    override val smsDeleteBtn = "Delete SMS"
    override val smsCopyTextBtn = "Copy Message Text"
    override val smsThreatWarning = "⚠️ Warning: Never click links or download APK files from suspicious messages!"
    override val smsReplyPlaceholder = "Type SMS reply..."
    override val smsSendBtn = "Send"
    override val smsReplySentToast = "SMS message sent successfully"
    override val smsReplyFailedToast = "Failed to send SMS. Check mobile signal / credit."
    override val smsNonReplyableNote = "Alphanumeric / bot sender (cannot be replied to)"

    override val backupSuccessTitle = "Backup Created Successfully"
    override val backupSuccessDesc = "Rule data has been automatically copied to your clipboard. You can also copy or save the JSON text below:"
    override val backupCopyAgainBtn = "Copy JSON Text"
    override val backupCopiedToast = "JSON text copied to clipboard!"
    override val importTitle = "Import Rules Backup"
    override val importPasteBtn = "Paste Clipboard"
    override val importDesc = "Paste your rules JSON backup text below:"
    override val importReplaceCheckbox = "Replace all existing rules"
    override val importAppendCheckbox = "Append to existing rules (Merge)"
    override val importNowBtn = "Import Now"
    override val importEmptyToast = "JSON text is empty!"

    override val notifCallBlockedTitle: (String) -> String = { number -> "🛡️ Call Blocked: $number" }
    override val notifCallBlockedReason: (String) -> String = { reason -> "Reason: $reason" }
    override val notifCallBlockedBigText: (String, String) -> String = { number, reason -> "Call from $number was successfully blocked.\nReason: $reason" }
    override val notifSmsScamTitle: (String) -> String = { sender -> "⚠️ Scam/Spam SMS: $sender" }
    override val notifSmsScamReason: (String) -> String = { reason -> "Reason: $reason" }
    override val notifSmsScamBigText: (String, String, String) -> String = { sender, reason, snippet -> "SMS from $sender is suspected as fraud.\nReason: $reason\n\nContent: \"$snippet\"" }

    override val toastRoleSuccess = "App successfully set as official Call Screening!"
    override val toastCopiedNumber = "Number copied to clipboard!"
    override val toastCopiedText = "Message text copied!"
    override val toastCopiedOtp: (String) -> String = { otp -> "OTP code copied: $otp" }
    override val toastRuleAdded = "Rule added successfully"
    override val toastSmsDeleted: (Int) -> String = { count -> "$count SMS deleted successfully" }
    override val toastImportSuccess: (Int) -> String = { count -> "$count rules imported successfully!" }
    override val toastImportFailed = "Failed to import: Invalid JSON format"
    override val toastPressBackAgainToExit = "Press back again to exit"
}

fun RuleType.getDisplayName(strings: Strings): String = when (this) {
    RuleType.EXACT -> strings.ruleTypeExact
    RuleType.PREFIX -> strings.ruleTypePrefix
    RuleType.CONTAINS -> strings.ruleTypeContains
    RuleType.KEYWORD -> strings.ruleTypeKeyword
    RuleType.REGEX -> strings.ruleTypeRegex
}

fun TargetChannel.getDisplayName(strings: Strings): String = when (this) {
    TargetChannel.CALL_ONLY -> strings.channelCallOnly
    TargetChannel.SMS_ONLY -> strings.channelSmsOnly
    TargetChannel.BOTH -> strings.channelBoth
}

fun getAppStrings(language: AppLanguage): Strings = when (language) {
    AppLanguage.INDONESIAN -> IndonesianStrings
    AppLanguage.ENGLISH -> EnglishStrings
}

val LocalAppStrings = staticCompositionLocalOf<Strings> { IndonesianStrings }
