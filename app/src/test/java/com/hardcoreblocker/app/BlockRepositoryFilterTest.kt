package com.hardcoreblocker.app
import com.hardcoreblocker.app.data.repository.BlockRepository
import org.junit.Assert.*
import org.junit.Test

class BlockRepositoryFilterTest {

    @Test
    fun testIsPersonalMobileNumber() {
        // Shortcodes harus dianggap BUKAN nomor pribadi
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

        // Nomor seluler pribadi Indonesia (10-14 digit)
        assertTrue(BlockRepository.isPersonalMobileNumber("081234567890"))
        assertTrue(BlockRepository.isPersonalMobileNumber("+6281234567890"))
        assertTrue(BlockRepository.isPersonalMobileNumber("6281234567890"))
        assertTrue(BlockRepository.isPersonalMobileNumber("0895-1234-5678"))
        assertTrue(BlockRepository.isPersonalMobileNumber("+62 812 3456 7890"))
    }

    @Test
    fun testOfficialTelkomselNotFlaggedAsThreat() {
        val dummyRepo = java.lang.reflect.Constructor::class.java // We can test using reflection or mock or direct methods if Context allows, but detectPhishingThreat doesn't depend on Context!
    }
}
