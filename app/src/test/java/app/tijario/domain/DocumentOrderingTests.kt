package app.tijario.domain

import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentOrderingTests {
    @Test
    fun newestUsesCreatedAtAcrossDocumentTypes() {
        val documents = listOf(
            document("invoice", DocumentType.Invoice, "2026-07-12T09:00:00Z", "2026-07-12"),
            document("quote", DocumentType.Quote, "2026-07-12T11:00:00Z", "2026-06-01"),
        )

        assertEquals(listOf("quote", "invoice"), newestDocuments(documents).map { it.id })
    }

    @Test
    fun legacyRowsFallBackToIssueDateAndUseStableIdTieBreak() {
        val documents = listOf(
            document("a", DocumentType.Invoice, null, "2026-07-10"),
            document("c", DocumentType.Quote, null, "2026-07-11"),
            document("b", DocumentType.Invoice, null, "2026-07-11"),
        )

        assertEquals(listOf("c", "b", "a"), newestDocuments(documents).map { it.id })
    }

    private fun document(
        id: String,
        type: DocumentType,
        createdAt: String?,
        issueDate: String,
    ) = DocumentSummary(
        id = id,
        customerId = "customer",
        type = type,
        documentNumber = id,
        status = "draft",
        issueDate = issueDate,
        createdAt = createdAt,
        total = 0.0,
        currency = "SAR",
    )
}
