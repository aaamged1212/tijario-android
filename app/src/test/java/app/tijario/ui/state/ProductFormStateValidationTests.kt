package app.tijario.ui.state

import app.tijario.config.AppLanguage
import app.tijario.data.model.ProductKind
import app.tijario.domain.normalizePhoneWithDialCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductFormStateValidationTests {
    @Test
    fun productRequiresPositiveStock() {
        val missingStock = ProductFormState(
            name = "Product",
            price = "10",
            kind = ProductKind.Product,
            lang = AppLanguage.EN,
        )
        val zeroStock = missingStock.copy(stockQuantity = "0")
        val validStock = missingStock.copy(stockQuantity = "5")

        assertFalse(missingStock.canSubmit)
        assertFalse(zeroStock.canSubmit)
        assertTrue(validStock.canSubmit)
        assertNull(validStock.stockQuantityError)
    }

    @Test
    fun serviceAllowsEmptyStock() {
        val service = ProductFormState(
            name = "Service",
            price = "10",
            kind = ProductKind.Service,
            stockQuantity = "",
            lang = AppLanguage.EN,
        )

        assertTrue(service.canSubmit)
        assertNull(service.stockQuantityError)
    }

    @Test
    fun phoneNumberIsNormalizedFromDialCodeAndLocalNumber() {
        assertEquals("+967771234567", normalizePhoneWithDialCode("+967", "077-123 4567"))
        assertEquals("+966500000000", normalizePhoneWithDialCode("966", "+500 000 000"))
    }
}
