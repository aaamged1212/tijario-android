package app.tijario.data.local

import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentSnapshotPersistenceTest {

    @Test
    fun completeDocumentStoresHistoricalCustomerSnapshot() {
        val document = CompleteDocument(
            id = "document-1",
            userId = "user-1",
            customerId = "customer-1",
            type = DocumentType.Invoice,
            documentNumber = "INV-00001",
            status = "draft",
            issueDate = "2026-07-18",
            subtotal = 10.0,
            discount = 0.0,
            extraFees = 0.0,
            total = 10.0,
            currency = "SAR",
            customer = Customer(
                id = "customer-1",
                userId = "user-1",
                name = "Historical name",
                whatsappNumber = "966500000000",
                city = "Riyadh",
            ),
        )

        val entity = document.toEntity("user-1", 1L)

        assertEquals("Historical name", entity.customerSnapshotName)
        assertEquals("966500000000", entity.customerSnapshotWhatsapp)
        assertEquals("Riyadh", entity.customerSnapshotCity)
    }
}
