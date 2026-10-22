# 🛡️ HardcoreBlocker

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="HardcoreBlocker Logo" width="120" height="120" style="border-radius: 24px; box-shadow: 0 8px 24px rgba(0,0,0,0.3);" />
</p>

<p align="center">
  <strong>Perisai Pemblokir Panggilan Spam & Karantina SMS 100% Offline Tanpa Kompromi Privasi</strong><br>
  <em>Bahasa: 🇮🇩 <strong>Bahasa Indonesia</strong> | 🇬🇧 <a href="README.md">English</a></em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Privacy-100%25_Offline-00C853?style=for-the-badge&logo=shield&logoColor=white" alt="100% Offline" />
  <img src="https://img.shields.io/badge/Zero_Trackers-No_Internet_Permission-FF6D00?style=for-the-badge" alt="Zero Trackers" />
  <img src="https://img.shields.io/badge/Languages-ID_%7C_EN-00E5FF?style=for-the-badge" alt="Bilingual" />
</p>

---

## 🌟 Mengapa HardcoreBlocker?

Aplikasi pemblokir komersial umum mengharuskan Anda mengunggah seluruh kontak dan log panggilan ke server cloud mereka dengan dalih "analisis komunitas". Data pribadi keluarga, rekan bisnis, dan kontak sensitif Anda rentan bocor dan diperjualbelikan.

**HardcoreBlocker dibangun dengan prinsip privasi mutlak:**
- 🚫 **Tanpa Izin Internet**: Izin `android.permission.INTERNET` **sama sekali tidak dicantumkan** di dalam `AndroidManifest.xml`. Secara teknis **mustahil** ada data yang bocor keluar dari ponsel Anda.
- ⚡ **Pemutusan 0-Milidetik**: Terintegrasi langsung dengan subsistem resmi `Android Telecom Framework` (`CallScreeningService`), memutus panggilan spam sebelum perangkat bergetar atau berdering.
- 💬 **Karantina SMS Cerdas**: Memisahkan SMS penipuan (APK kurir palsu, surat tilang fiktif, pinjol ilegal, tautan phishing) ke kotak terisolasi.
- 🌐 **Dukungan Bilingual Penuh**: Transisi instan antara **🇮🇩 Bahasa Indonesia** dan **🇬🇧 English** langsung dari Pengaturan tanpa restart.

---

## 🚀 Fitur Utama

### 1. 🛡️ Mode Pertahanan Ganda
- **Mode Hardcore (Perlindungan Maksimal)**: Hanya nomor yang sudah tersimpan di Buku Kontak Anda yang dapat menghubungi Anda. Semua nomor asing / tak dikenal otomatis ditolak seketika. Menghentikan 100% teror pinjol, penipu, dan robot telemarketing.
- **Mode Smart (Berbasis Aturan)**: Memblokir panggilan dan SMS berdasarkan kombinasi aturan spesifik yang Anda tentukan (Blacklist nomor, Awalan wilayah/negara, Kata Kunci isi SMS).

### 2. 📋 Mesin Aturan Fleksibel
- **Nomor Persis (Exact)**: Blokir nomor tertentu (contoh: `081299998888`).
- **Awalan (+Prefix)**: Blokir kode negara luar negeri (contoh: `+60`, `+1`, `+44`) atau kode area telemarketing (contoh: `021`, `140`).
- **Kata Kunci SMS (Keyword)**: Deteksi teks pesan penipuan seperti *"pinjaman"*, *"paket kurir"*, *"undian"*, *"surat tilang"*.
- **Pola Regex**: Dukungan ekspresi reguler tingkat lanjut untuk pola nomor rumit.

### 3. 🧪 Simulator Evaluasi Interaktif
Uji coba apakah nomor panggilan atau isi pesan SMS tertentu akan dicegat atau diloloskan secara instan sebelum aturan benar-benar diterapkan.

### 4. 💬 Kotak Karantina & Pengelola SMS
- **Deteksi Tautan Mencurigakan**: Mengidentifikasi pesan dengan tautan berbahaya (`.apk`, `bit.ly`, `t.me`, `wa.me`) dan mengisolasinya.
- **Ekstraksi Otomatis Kode OTP**: Menyaring kode OTP 4-8 digit dengan tombol 1-klik salin ke clipboard.
- **1-Klik Blokir Nomor Pengirim**: Tambahkan nomor pengirim spam langsung ke daftar blokir tanpa keluar dari aplikasi.

### 5. 🌓 Mode Visual Dinamis
- **Gelap (Dark Mode)**: Warna pekat OLED-friendly yang nyaman di mata dan hemat daya baterai.
- **Terang (Light Mode)**: Desain cerah, kontras tinggi, dan bersih dengan aksen biru profesional.
- **Sistem (Auto)**: Menyesuaikan tema otomatis dengan pengaturan sistem Android.

### 6. 🔄 Cadangan & Pemulihan Portabel
Ekspor seluruh basis aturan Anda ke format JSON terenkripsi lokal, dan impor kembali kapan saja via clipboard atau file teks.

---

## 🛠️ Arsitektur & Teknologi

```
HardcoreBlocker
├── app/
│   ├── src/main/java/com/hardcoreblocker/app/
│   │   ├── data/
│   │   │   ├── model/         # BlockRule, BlockedLog, BlockSettings, SmsMessageItem
│   │   │   └── repository/    # BlockRepository (Offline DB & SharedPreferences)
│   │   ├── service/           # HardcoreCallScreeningService & HardcoreSmsReceiver
│   │   ├── ui/
│   │   │   ├── components/    # CommonComponents, Dialogs, BottomSheets
│   │   │   ├── i18n/          # Centralized AppStrings (Indonesian & English)
│   │   │   ├── screens/       # Splash, Onboarding, Dashboard, Inbox, Rules, Logs, Settings
│   │   │   ├── theme/         # Color tokens, Typography, Theme provider
│   │   │   └── viewmodel/     # MainViewModel (MVI/MVVM with StateFlow)
│   │   ├── HardcoreBlockerApp.kt
│   │   └── MainActivity.kt
```

- **Bahasa**: Kotlin 2.0
- **UI Toolkit**: Jetpack Compose (Declarative UI) & Material 3
- **State Management**: Kotlin Coroutines & `StateFlow`
- **Integrasi OS**: `android.telecom.CallScreeningService`
- **Penyimpanan**: SQLite Room Database & Android Encrypted Preferences
- **Kemandirian**: Zero External Network SDKs, No Firebase, No Google Play Services Ads, No Trackers.

---

## 📲 Panduan Instalasi & Build

### Persyaratan Sistem:
- **Android Studio**: Ladybug / Koala (atau versi terbaru)
- **JDK**: Versi 17 atau lebih tinggi
- **Android SDK**: Min SDK 28 (Android 9.0 Pie) • Target SDK 35 (Android 15)

### Langkah Kompilasi:

1. **Clone repositori**:
   ```powershell
   git clone https://github.com/Zettt416/HardcoreBlockerzp.git
   cd HardcoreBlockerzp
   ```

2. **Build Debug APK**:
   ```powershell
   .\gradlew assembleDebug
   ```
   *File output APK:* `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Release APK (Optimal & Proguard/R8)**:
   ```powershell
   .\gradlew assembleRelease
   ```
   *File output APK:* `app/build/outputs/apk/release/app-release.apk`

4. **Instal ke Perangkat / Emulator**:
   ```powershell
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

---

## 🔐 Transparansi Hak Akses Sistem

Aplikasi ini hanya meminta izin resmi yang mutlak dibutuhkan untuk fungsi keamanan:

| Hak Akses (Permission) | Mengapa Dibutuhkan? (Rationale) |
|---|---|
| `ROLE_CALL_SCREENING` | Memberikan wewenang sistem kepada HardcoreBlocker untuk menolak panggilan spam sebelum berdering. |
| `READ_CONTACTS` | Memverifikasi apakah nomor penelepon ada di buku kontak Anda (Whitelist lokal, tidak pernah diunggah). |
| `RECEIVE_SMS` & `READ_SMS` | Menganalisis SMS masuk secara lokal untuk karantina pesan phishing dan ekstraksi OTP. |
| `POST_NOTIFICATIONS` | Menampilkan pemberitahuan senyap opsional saat panggilan atau SMS scam berhasil diblokir. |

> 🛡️ **Jaminan Privasi:** Aplikasi ini sama sekali tidak memiliki izin `INTERNET`. Kontak dan riwayat Anda tidak akan pernah bisa meninggalkan ponsel Anda.

---

## 🤝 Lisensi & Kontribusi

Dibuat dengan dedikasi tinggi untuk kebebasan privasi digital.  
Silakan ajukan Issue atau Pull Request jika Anda memiliki saran perbaikan atau fitur baru.

**Hak Cipta © 2026 HardcoreBlocker.**  
Didistribusikan di bawah lisensi [MIT License](LICENSE).
