package app.tijario.features.documents

import app.tijario.data.remote.CreateDocumentRequest
import app.tijario.data.remote.DocumentCustomerInput
import app.tijario.data.model.DocumentType
import app.tijario.ui.state.DocumentFormState
import app.tijario.config.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DocumentNumberingIdempotencyTests {

    @Test
    fun testOperationId_isGeneratedOnceAndRemainsStable() {
        val form1 = DocumentFormState(
            customerId = "123",
            customerName = "Test Client",
            customerWhatsapp = "+966500000000",
            lang = AppLanguage.AR
        )
        val opId = form1.operationId
        assertNotNull(opId)
        val regex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", RegexOption.IGNORE_CASE)
        org.junit.Assert.assertTrue(regex.matches(opId))

        val form2 = form1.copy(customerName = "Updated Name")
        assertEquals(opId, form2.operationId)
    }

    @Test
    fun testCreateDocumentRequest_retainsOperationId() {
        val req = CreateDocumentRequest(
            type = DocumentType.Invoice,
            operationId = "e5b8d4f4-52d3-46ea-9d8c-bd5bf755f111",
            customer = DocumentCustomerInput(name = "Customer", whatsappNumber = "+966500000000"),
            items = emptyList()
        )
        assertEquals("e5b8d4f4-52d3-46ea-9d8c-bd5bf755f111", req.operationId)
    }
}
