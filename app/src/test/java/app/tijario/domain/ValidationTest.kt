package app.tijario.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationTest {
    @Test
    fun parseNonNegativeMoney_acceptsWesternArabicAndPersianDigits() {
        assertEquals(1100.0, Validation.parseNonNegativeMoney("1100")!!, 0.0)
        assertEquals(1100.0, Validation.parseNonNegativeMoney("١١٠٠")!!, 0.0)
        assertEquals(1100.0, Validation.parseNonNegativeMoney("۱۱۰۰")!!, 0.0)
    }

    @Test
    fun parseNonNegativeMoney_acceptsArabicDecimalAndThousandsSeparators() {
        assertEquals(1100.5, Validation.parseNonNegativeMoney("١٬١٠٠٫٥")!!, 0.0)
        assertEquals(1100.5, Validation.parseNonNegativeMoney("1,100.50")!!, 0.0)
    }

    @Test
    fun parseNonNegativeMoney_rejectsNegativeValues() {
        assertNull(Validation.parseNonNegativeMoney("-1"))
        assertNotNull(Validation.nonNegativeMoney("-1", "السعر", app.tijario.config.AppLanguage.AR))
    }

    @Test
    fun parsePositiveInt_requiresWholePositiveNumber() {
        assertEquals(2, Validation.parsePositiveInt("٢"))
        assertNull(Validation.parsePositiveInt("0"))
        assertNull(Validation.parsePositiveInt("1.5"))
    }
}
