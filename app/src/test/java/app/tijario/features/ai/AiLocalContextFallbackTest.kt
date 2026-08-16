package app.tijario.features.ai

import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiLocalContextFallbackTest {
    private val customer = Customer(
        id = "local-customer-id",
        name = "Local customer",
        whatsappNumber = "966555000000",
        city = "Sanaa",
    )
    private val product = Product(
        id = "local-product-id",
        kind = ProductKind.Product,
        name = "Local product",
        description = "Local description",
        price = 120.0,
        currency = "SAR",
        category = "Accessories",
    )

    private val business = BusinessSettings(
        businessName = "Local store",
        whatsappNumber = "966555000000",
        country = "Yemen",
        currency = "YER",
    )

    @Test
    fun snapshot_keepsLocalContextWithoutPhoneNumbers() {
        val snapshot = buildAiContextSnapshot(
            businessSettings = business,
            customer = customer,
            product = product,
            currency = "YER",
        )

        assertEquals("Local customer", snapshot.customer?.name)
        assertEquals("local-customer-id", snapshot.customer?.localId)
        assertEquals("Local product", snapshot.product?.name)
        assertEquals(120.0, snapshot.product?.price)
        assertEquals("SAR", snapshot.product?.currency)
        assertEquals("Accessories", snapshot.product?.category)
        assertFalse(snapshot.toString().contains(customer.whatsappNumber))
    }

    @Test
    fun snapshot_keepsTheProductCurrencySeparateFromTheStoreCurrency() {
        val snapshot = buildAiContextSnapshot(
            businessSettings = business,
            product = product,
            currency = "YER",
        )

        assertEquals("YER", snapshot.business.currency)
        assertEquals("SAR", snapshot.product?.currency)
        assertTrue(snapshot.product?.description.orEmpty().length <= 500)
    }
}
