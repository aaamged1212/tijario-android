package app.tijario.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberTests {
    @Test
    fun normalizesArabicDigitsAndLocalTrunkPrefix() {
        assertEquals("+966501234567", normalizePhoneWithDialCode("+966", "٠٥٠ ١٢٣ ٤٥٦٧"))
    }

    @Test
    fun preservesPastedInternationalNumberWithoutDuplicatingDialCode() {
        assertEquals("+971501234567", normalizePhoneWithDialCode("+966", "+971 50 123 4567"))
        assertEquals("+966501234567", normalizePhoneWithDialCode("+966", "966501234567"))
    }

    @Test
    fun appliesTheSelectedCountryDialCodeToAnExistingLocalNumber() {
        val localNumber = splitPhoneNumber("+966501234567").localNumber

        assertEquals("+971501234567", normalizePhoneWithDialCode("+971", localNumber))
        assertEquals("+971", splitPhoneNumber("+971501234567").dialCode)
    }

    @Test
    fun validatesE164LengthAndPrefix() {
        assertTrue(isValidE164Phone("+966501234567"))
        assertTrue(isValidE164Phone("+٩٦٦٥٠١٢٣٤٥٦٧"))
        assertFalse(isValidE164Phone("0501234567"))
        assertFalse(isValidE164Phone("+0000000"))
    }
}
