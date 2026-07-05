package app.tijario.data.repository

import app.tijario.data.local.DocumentEntity
import app.tijario.data.local.DocumentItemEntity
import app.tijario.data.model.DocumentType
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import java.math.BigDecimal
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.double
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentSyncPayloadTests {
    @Test
    fun productPayloadIncludesCategory() {
        val payload = buildProductSyncPayload(
            Product(
                id = "p-1",
                kind = ProductKind.Product,
                name = "Laptop",
                description = "Premium",
                price = 1500.0,
                currency = "SAR",
                stockQuantity = 2,
                category = "Electronics",
            ),
        ).jsonObject

        assertEquals("Electronics", payload["category"]?.jsonPrimitive?.content)
    }

    @Test
    fun documentPayloadIncludesTemplateTitleLabelsAndProductId() {
        val payload = buildDocumentSyncPayload(
            doc = DocumentEntity(
                id = "d-1",
                userId = "u-1",
                customerId = "c-1",
                type = "invoice",
                documentNumber = "INV-0001",
                status = "draft",
                paymentStatus = "unpaid",
                amountPaid = BigDecimal.ZERO,
                issueDate = "2026-07-05",
                templateId = "tijario-modern",
                documentTitle = "Invoice",
                total = BigDecimal("250.00"),
                currency = "SAR",
                syncedAt = 123L,
                subtotal = BigDecimal("200.00"),
                discount = BigDecimal("10.00"),
                discountLabel = "Promo",
                extraFees = BigDecimal("60.00"),
                extraFeesLabel = "Delivery",
                taxName = "VAT",
                taxRate = BigDecimal("15.00"),
                taxAmount = BigDecimal("31.50"),
                notes = null,
                termsText = "Terms",
            ),
            items = listOf(
                DocumentItemEntity(
                    id = "i-1",
                    userId = "u-1",
                    documentId = "d-1",
                    productId = "p-1",
                    name = "Item",
                    description = "Desc",
                    quantity = 1,
                    unitPrice = BigDecimal("100.00"),
                    lineTotal = BigDecimal("100.00"),
                    sortOrder = 0,
                ),
            ),
        ).jsonObject

        assertEquals("tijario-modern", payload["template_id"]?.jsonPrimitive?.content)
        assertEquals("Invoice", payload["document_title"]?.jsonPrimitive?.content)
        assertEquals("Promo", payload["discount_label"]?.jsonPrimitive?.content)
        assertEquals("Delivery", payload["extra_fees_label"]?.jsonPrimitive?.content)
        assertEquals("VAT", payload["tax_name"]?.jsonPrimitive?.content)
        assertEquals(15.0, payload["tax_rate"]?.jsonPrimitive?.double ?: -1.0, 0.0)
        assertEquals(31.5, payload["tax_amount"]?.jsonPrimitive?.double ?: -1.0, 0.0)
        val items = payload["items"]?.jsonArray ?: error("items missing")
        assertTrue(items.isNotEmpty())
        assertEquals("p-1", items.first().jsonObject["product_id"]?.jsonPrimitive?.content)
    }
}
