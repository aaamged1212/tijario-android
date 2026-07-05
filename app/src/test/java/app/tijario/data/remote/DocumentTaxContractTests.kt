package app.tijario.data.remote

import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentTaxContractTests {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun createDocumentRequestSerializesTaxFields() {
        val request = CreateDocumentRequest(
            type = DocumentType.Invoice,
            customer = DocumentCustomerInput(name = "Customer", whatsappNumber = "+966500000000"),
            items = listOf(
                DocumentItemInput(name = "Item", quantity = 1, unitPrice = 100.0),
            ),
            taxName = "VAT",
            taxRate = 15.0,
        )

        val payload = json.encodeToJsonElement(CreateDocumentRequest.serializer(), request).jsonObject
        assertEquals("VAT", payload["tax_name"]?.jsonPrimitive?.content)
        assertEquals(15.0, payload["tax_rate"]?.jsonPrimitive?.double ?: -1.0, 0.0)
    }

    @Test
    fun completeDocumentParsesTaxFields() {
        val raw = """
            {
              "id":"doc-1",
              "user_id":"user-1",
              "customer_id":"customer-1",
              "type":"invoice",
              "document_number":"INV-0001",
              "document_title":"Invoice",
              "status":"draft",
              "payment_status":"unpaid",
              "amount_paid":0,
              "issue_date":"2026-07-05",
              "subtotal":100,
              "discount":0,
              "discount_label":null,
              "extra_fees":0,
              "extra_fees_label":null,
              "tax_name":"VAT",
              "tax_rate":15,
              "tax_amount":15,
              "total":115,
              "currency":"SAR",
              "template_id":"tijario-classic",
              "notes":null,
              "terms_text":null,
              "customer": null,
              "items": []
            }
        """.trimIndent()

        val document = json.decodeFromString(CompleteDocument.serializer(), raw)

        assertEquals("VAT", document.taxName)
        assertEquals(15.0, document.taxRate, 0.0)
        assertEquals(15.0, document.taxAmount, 0.0)
    }
}
