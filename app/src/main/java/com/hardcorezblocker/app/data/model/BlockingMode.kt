package com.hardcorezblocker.app.data.model

enum class BlockingMode(val title: String, val description: String) {
    HARDCORE_CONTACTS_ONLY(
        title = "Hardcore Mode (Kontak Saja)",
        description = "Hanya terima panggilan dari daftar kontak Anda. Semua nomor asing langsung ditolak seketika."
    ),
    SMART_FILTER(
        title = "Smart Filter (Aturan & Pola)",
        description = "Blokir nomor berdasarkan daftar blacklist, kode awalan (prefix asing/VoIP), dan kata kunci SMS spam."
    ),
    OFF(
        title = "Perlindungan Nonaktif",
        description = "Semua panggilan dan pesan diloloskan seperti biasa tanpa filter."
    )
}
