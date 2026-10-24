package com.hardcorezblocker.app

import com.hardcorezblocker.app.data.model.SmsSafetyCategory
import com.hardcorezblocker.app.data.repository.BlockRepository
import org.junit.Assert.*
import org.junit.Test

class BlockRepositoryFilterTest {

    @Test
    fun testIsPersonalMobileNumber() {
        // Shortcodes resmi operator BUKAN nomor pribadi
        assertFalse(BlockRepository.isPersonalMobileNumber("3636"))
        assertFalse(BlockRepository.isPersonalMobileNumber("777"))
        assertFalse(BlockRepository.isPersonalMobileNumber("4444"))
        assertFalse(BlockRepository.isPersonalMobileNumber("1166"))
        assertFalse(BlockRepository.isPersonalMobileNumber("97080"))
        assertFalse(BlockRepository.isPersonalMobileNumber("92200"))
        assertFalse(BlockRepository.isPersonalMobileNumber("111"))
        assertFalse(BlockRepository.isPersonalMobileNumber("888"))

        // Alphanumeric sender BUKAN nomor pribadi
        assertFalse(BlockRepository.isPersonalMobileNumber("TELKOMSEL"))
        assertFalse(BlockRepository.isPersonalMobileNumber("Telkomsel"))
        assertFalse(BlockRepository.isPersonalMobileNumber("INDOSAT"))
        assertFalse(BlockRepository.isPersonalMobileNumber("BCA"))
        assertFalse(BlockRepository.isPersonalMobileNumber("Shopee"))

        // Nomor seluler pribadi Indonesia (10-15 digit)
        assertTrue(BlockRepository.isPersonalMobileNumber("081234567890"))
        assertTrue(BlockRepository.isPersonalMobileNumber("+6281234567890"))
        assertTrue(BlockRepository.isPersonalMobileNumber("6281234567890"))
        assertTrue(BlockRepository.isPersonalMobileNumber("0895-1234-5678"))
        assertTrue(BlockRepository.isPersonalMobileNumber("+62 812 3456 7890"))
    }

    @Test
    fun testOfficialTelkomselNotFlaggedAsThreat() {
        // 1. Pesan sisa pulsa Telkomsel dari shortcode 3636 dengan link tsel.me
        val msg1 = "Sisa Pulsa Anda Rp 5.000 berlaku s.d 30/11/2026. Beli paket hemat di tsel.me/tsel1 atau hub *363#"
        val threat1 = BlockRepository.detectPhishingThreat("3636", msg1)
        assertNull("Pesan pulsa dari 3636 tidak boleh dianggap phishing", threat1)

        val (cat1, reason1) = BlockRepository.classifySafetyCategory("3636", msg1)
        assertNotEquals("Pesan resmi operator tidak boleh masuk SPAM_OR_PHISHING", SmsSafetyCategory.SPAM_OR_PHISHING, cat1)
        assertEquals(SmsSafetyCategory.PROMO_MARKETING, cat1)
        assertNull(reason1)

        // 2. Pesan isi ulang pulsa berhasil dari pengirim TELKOMSEL
        val msg2 = "Terima kasih telah melakukan isi ulang pulsa Rp 50.000. Masa aktif s.d 30/12/2026. Cek paket di tsel.id/app"
        val threat2 = BlockRepository.detectPhishingThreat("TELKOMSEL", msg2)
        assertNull("Pesan isi ulang pulsa resmi tidak boleh dianggap ancaman", threat2)

        val (cat2, _) = BlockRepository.classifySafetyCategory("TELKOMSEL", msg2)
        assertNotEquals(SmsSafetyCategory.SPAM_OR_PHISHING, cat2)

        // 3. Pesan tukar poin resmi dari shortcode 777 dengan link tsel.id
        val msg3 = "Poin Telkomsel Anda 150. Segera tukar poin Anda sebelum hangus di tsel.id/poin atau MyTelkomsel."
        val threat3 = BlockRepository.detectPhishingThreat("777", msg3)
        assertNull("Tukar poin resmi dengan link tsel.id tidak boleh dianggap ancaman", threat3)

        val (cat3, _) = BlockRepository.classifySafetyCategory("777", msg3)
        assertEquals(SmsSafetyCategory.PROMO_MARKETING, cat3)

        // 4. Operator lain (Indosat, XL, BPS)
        val msg4 = "Sisa pulsa utama Anda Rp 10.000 aktif s.d 10-12-2026. Cek di im3.id/app atau *123#"
        val threat4 = BlockRepository.detectPhishingThreat("92200", msg4)
        assertNull("Pesan pulsa Indosat resmi tidak boleh dianggap ancaman", threat4)
        val (cat4, _) = BlockRepository.classifySafetyCategory("92200", msg4)
        assertNotEquals(SmsSafetyCategory.SPAM_OR_PHISHING, cat4)
    }

    @Test
    fun testRealThreatsAreDetected() {
        // 1. Malware APK
        val apkMsg = "Unduh surat tilang elektronik dari kepolisian.apk sekarang"
        val apkThreat = BlockRepository.detectPhishingThreat("081234567890", apkMsg)
        assertNotNull(apkThreat)
        assertTrue(apkThreat!!.first)
        val (apkCat, _) = BlockRepository.classifySafetyCategory("081234567890", apkMsg)
        assertEquals(SmsSafetyCategory.SPAM_OR_PHISHING, apkCat)

        // 2. Phishing BCA Palsu
        val bcaMsg = "Rekening BCA Anda terblokir. Segera login verifikasi di https://klikbca-verifikasi.xyz/login"
        val bcaThreat = BlockRepository.detectPhishingThreat("081234567890", bcaMsg)
        assertNotNull(bcaThreat)
        assertTrue(bcaThreat!!.first)

        // 3. Nomor seluler pribadi yang menyebarkan link asing
        val spamLinkMsg = "Halo kak hadiah 100jt bisa diclaim di bit.ly/hadiah100jt"
        val spamThreat = BlockRepository.detectPhishingThreat("081234567890", spamLinkMsg)
        assertNotNull(spamThreat)
        assertTrue(spamThreat!!.first)

        // 4. Judi online / slot gacor
        val judolMsg = "Depo pulsa tanpa potongan slot gacor pragmatic play maxwin malam ini daftar di link-gacor.club"
        val judolThreat = BlockRepository.detectPhishingThreat("081234567890", judolMsg)
        assertNotNull(judolThreat)
        assertTrue(judolThreat!!.first)
    }

    @Test
    fun testOtpDetectionAccurate() {
        // OTP murni
        val otpBody = "123456 adalah kode verifikasi OTP MyTelkomsel Anda. Jangan berikan kepada siapapun."
        assertTrue(BlockRepository.OtpDetector.isOtp(otpBody))
        assertEquals("123456", BlockRepository.OtpDetector.extractOtp(otpBody))

        val (cat, _) = BlockRepository.classifySafetyCategory("TELKOMSEL", otpBody)
        assertEquals(SmsSafetyCategory.OTP_OFFICIAL, cat)

        // Bukan OTP: Poin atau pulsa
        val pulsaBody = "Pulsa Anda bertambah Rp 50000. Masa aktif s.d 2026-12-31."
        assertFalse(BlockRepository.OtpDetector.isOtp(pulsaBody))

        val poinBody = "Anda mendapatkan 200 poin Telkomsel dari transaksi isi ulang."
        assertFalse(BlockRepository.OtpDetector.isOtp(poinBody))
    }

    @Test
    fun testMmsPromoAndThreatClassification() {
        // MMS Promo resmi dari bank/merchant
        val rbmPromo = "Ajukan Kartu Kredit Mega. Apply Kartu Kredit Bank Mega sekarang dan kamu bisa dapatkan limit hingga Rp100JT."
        val (cat1, _) = BlockRepository.classifySafetyCategory("Bank Mega", rbmPromo)
        assertEquals(SmsSafetyCategory.PROMO_MARKETING, cat1)

        // MMS Phishing / Malware APK bertopeng instansi resmi
        val mmsPhishing = "Surat Tilang Elektronik ETLE. Silakan unduh dokumen_tilang.apk untuk melihat rincian denda."
        val (cat2, reason2) = BlockRepository.classifySafetyCategory("Polri ETLE", mmsPhishing)
        assertEquals(SmsSafetyCategory.SPAM_OR_PHISHING, cat2)
        assertNotNull(reason2)
    }
}
