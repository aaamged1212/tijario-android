package app.tijario.domain

import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentSearchTests {
    @Test
    fun filterDocumentsBySearch_matchesDocumentNumber() {
        val documents = listOf(document("invoice-1", "customer-1", "INV-00018"))

        val result = filterDocumentsBySearch(documents, mapOf("customer-1" to "Ahmed"), "0018")

        assertEquals(listOf("invoice-1"), result.map { it.id })
    }

    @Test
    fun filterDocumentsBySearch_matchesCustomerNameIgnoringCase() {
        val documents = listOf(
            document("invoice-1", "customer-1", "INV-00018"),
            document("invoice-2", "customer-2", "INV-00019"),
        )

        val result = filterDocumentsBySearch(
            documents,
            mapOf("customer-1" to "Ahmed Store", "customer-2" to "Noura Shop"),
            "noura",
        )

        assertEquals(listOf("invoice-2"), result.map { it.id })
    }

    @Test
    fun filterDocumentsBySearch_blankQueryPreservesInputOrder() {
        val documents = listOf(
            document("invoice-2", "customer-2", "INV-00019"),
            document("invoice-1", "customer-1", "INV-00018"),
        )

        assertEquals(documents, filterDocumentsBySearch(documents, emptyMap(), "  "))
    }

    private fun document(id: String, customerId: String, number: String) = DocumentSummary(
        id = id,
        customerId = customerId,
        type = DocumentType.Invoice,
        documentNumber = number,
        status = "saved",
        issueDate = "2026-08-16",
        total = 100.0,
        currency = "SAR",
    )
}
