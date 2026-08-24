package app.tijario.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YemenPaymentMethodTest {
    @Test
    fun `manual payment methods have only server-supported identifiers`() {
        assertEquals(
            setOf("kuraimi", "jeeb", "internal_transfer"),
            YemenPaymentMethod.entries.map { it.id }.toSet(),
        )
    }

    @Test
    fun `every payment method exposes copyable account details`() {
        YemenPaymentMethod.entries.forEach { method ->
            assertTrue(method.accountName.isNotBlank())
            assertTrue(method.accountValues.isNotEmpty())
        }
    }

    @Test
    fun `yearly Yemen prices apply the twelve month total and twenty percent discount`() {
        val starter = planPrice("starter", "yearly", isArabic = true)
        val pro = planPrice("pro", "yearly", isArabic = false)

        assertTrue(starter.contains("27,840 ريال يمني"))
        assertTrue(starter.contains("182.40 ريال سعودي"))
        assertTrue(pro.contains("YER 47,040"))
        assertTrue(pro.contains("USD 95.90"))
    }
}
