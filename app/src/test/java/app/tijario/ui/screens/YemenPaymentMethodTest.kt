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
}
