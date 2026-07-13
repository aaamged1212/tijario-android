package app.tijario.ui.screens

import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import app.tijario.data.model.DocumentType
import app.tijario.data.model.DocumentSummary
import app.tijario.ui.state.DocumentFormState
import app.tijario.ui.state.DocumentItemState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentFormEditFlowTests {
    @Test
    fun editDocumentLoadsOnlyOncePerDocumentId() {
        assertTrue(shouldLoadEditDocument(null, "doc-1"))
        assertFalse(shouldLoadEditDocument("doc-1", "doc-1"))
        assertTrue(shouldLoadEditDocument("doc-1", "doc-2"))
    }

    @Test
    fun alreadyLoadedEditDocumentDoesNotRequestReload() {
        assertFalse(shouldLoadEditDocument("doc-1", "doc-1"))
    }

    @Test
    fun selectingProductForExistingRowUpdatesThatRow() {
        val items = listOf(
            DocumentItemState(id = "item-1", productId = "old-1", name = "Old 1", quantity = "2", unitPrice = "10"),
            DocumentItemState(id = "item-2", productId = "old-2", name = "Old 2", quantity = "3", unitPrice = "20"),
        )
        val product = Product(
            id = "product-1",
            kind = ProductKind.Product,
            name = "New Product",
            price = 19.5,
            currency = "SAR",
        )

        val (updatedItems, index) = mergeSelectedProductIntoItems(items, product, 1)

        assertEquals(2, updatedItems.size)
        assertEquals(1, index)
        assertEquals("old-1", updatedItems[0].productId)
        assertEquals("New Product", updatedItems[1].name)
        assertEquals("product-1", updatedItems[1].productId)
        assertEquals("19.5", updatedItems[1].unitPrice)
        assertEquals("3", updatedItems[1].quantity)
    }

    @Test
    fun selectingProductForNewRowAppendsWithoutMaxItemLimit() {
        val items = listOf(
            DocumentItemState(id = "item-1", name = "Item 1", quantity = "1", unitPrice = "10"),
            DocumentItemState(id = "item-2", name = "Item 2", quantity = "1", unitPrice = "10"),
            DocumentItemState(id = "item-3", name = "Item 3", quantity = "1", unitPrice = "10"),
            DocumentItemState(id = "item-4", name = "Item 4", quantity = "1", unitPrice = "10"),
            DocumentItemState(id = "item-5", name = "Item 5", quantity = "1", unitPrice = "10"),
        )
        val product = Product(
            id = "product-6",
            kind = ProductKind.Service,
            name = "Appended Product",
            price = 25.0,
            currency = "SAR",
        )

        val (updatedItems, index) = mergeSelectedProductIntoItems(items, product, items.size)

        assertEquals(6, updatedItems.size)
        assertEquals(5, index)
        assertEquals("product-6", updatedItems.last().productId)
        assertEquals("Appended Product", updatedItems.last().name)
        assertEquals("25", updatedItems.last().unitPrice)
        assertEquals("1", updatedItems.last().quantity)
    }

    @Test
    fun defaultDocumentTitleFollowsDocumentLanguage() {
        assertEquals("فاتورة", defaultDocumentTitle(DocumentType.Invoice, "AR"))
        assertEquals("Invoice", defaultDocumentTitle(DocumentType.Invoice, "EN"))
        assertEquals("عرض سعر", defaultDocumentTitle(DocumentType.Quote, "AR"))
        assertEquals("Quote", defaultDocumentTitle(DocumentType.Quote, "EN"))
    }

    @Test
    fun documentLanguageChangeUpdatesOnlyDefaultTitle() {
        assertEquals(
            "Invoice",
            documentTitleAfterLanguageChange(
                type = DocumentType.Invoice,
                currentTitle = "فاتورة",
                nextDocumentLanguage = "EN",
                titleEditedByUser = false,
            ),
        )
        assertEquals(
            "Custom title",
            documentTitleAfterLanguageChange(
                type = DocumentType.Invoice,
                currentTitle = "Custom title",
                nextDocumentLanguage = "AR",
                titleEditedByUser = true,
            ),
        )
    }

    @Test
    fun documentLanguageChangePreservesCustomTitleEvenWhenAppLanguageChanges() {
        assertEquals(
            "Special Ramadan Invoice",
            documentTitleAfterLanguageChange(
                type = DocumentType.Invoice,
                currentTitle = "Special Ramadan Invoice",
                nextDocumentLanguage = "AR",
                titleEditedByUser = true,
            ),
        )
    }

    @Test
    fun draftDisplayKeepsOfficialDocumentNumberExactly() {
        assertEquals("INV-00005", displayDraftDocumentNumber("INV-00005", DocumentType.Invoice))
        assertEquals("INV-0005", displayDraftDocumentNumber("INV-0005", DocumentType.Invoice))
        assertEquals("Q-00009", displayDraftDocumentNumber("Q-00009", DocumentType.Quote))
    }

    @Test
    fun documentNumberEditablePartKeepsOnlyEditableDigits() {
        assertEquals("00005", documentNumberEditablePart("INV-00005", DocumentType.Invoice))
        assertEquals("00009", documentNumberEditablePart("Q-00009", DocumentType.Quote))
    }

    @Test
    fun nextLocalDocumentNumberUsesTypeSpecificFiveDigitSequence() {
        val documents = listOf(
            DocumentSummary(
                id = "invoice-1",
                customerId = "customer-1",
                type = DocumentType.Invoice,
                documentNumber = "INV-00015",
                status = "issued",
                issueDate = "2026-07-13",
                total = 100.0,
                currency = "SAR",
            ),
            DocumentSummary(
                id = "quote-1",
                customerId = "customer-1",
                type = DocumentType.Quote,
                documentNumber = "Q-00002",
                status = "issued",
                issueDate = "2026-07-13",
                total = 100.0,
                currency = "SAR",
            ),
        )

        assertEquals("INV-00016", nextLocalDocumentNumber(documents, DocumentType.Invoice))
        assertEquals("Q-00003", nextLocalDocumentNumber(documents, DocumentType.Quote))
        assertEquals("INV-00001", nextLocalDocumentNumber(emptyList(), DocumentType.Invoice))
    }

    @Test
    fun documentCustomerInputIncludesSelectedCustomerId() {
        val input = buildDocumentCustomerInput(
            DocumentFormState(
                customerId = "customer-1",
                customerName = "Customer",
                customerWhatsapp = "+966500000000",
                customerCity = "Riyadh",
            ),
        )

        assertEquals("customer-1", input.id)
        assertEquals("Customer", input.name)
        assertEquals("+966500000000", input.whatsappNumber)
        assertEquals("Riyadh", input.city)
    }

    @Test
    fun invoiceStockValidationOnlyAppliesToSavedProducts() {
        val products = listOf(
            Product(
                id = "product-1",
                kind = ProductKind.Product,
                name = "Tracked Product",
                price = 10.0,
                stockQuantity = 5,
            ),
            Product(
                id = "service-1",
                kind = ProductKind.Service,
                name = "Service",
                price = 10.0,
                stockQuantity = 1,
            ),
        )

        assertEquals(
            "Quantity exceeds available stock. Current stock: 5",
            invoiceStockValidationMessage(
                DocumentType.Invoice,
                DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "6", unitPrice = "10"),
                listOf(DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "6", unitPrice = "10")),
                products,
                app.tijario.config.AppLanguage.EN,
            ),
        )
        assertEquals(
            null,
            invoiceStockValidationMessage(
                DocumentType.Invoice,
                DocumentItemState(productId = "service-1", name = "Service", quantity = "6", unitPrice = "10"),
                listOf(DocumentItemState(productId = "service-1", name = "Service", quantity = "6", unitPrice = "10")),
                products,
                app.tijario.config.AppLanguage.EN,
            ),
        )
        assertEquals(
            null,
            invoiceStockValidationMessage(
                DocumentType.Quote,
                DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "6", unitPrice = "10"),
                listOf(DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "6", unitPrice = "10")),
                products,
                app.tijario.config.AppLanguage.EN,
            ),
        )
    }

    @Test
    fun editInvoiceStockValidationAllowsOriginalReservedQuantity() {
        val products = listOf(
            Product(
                id = "product-1",
                kind = ProductKind.Product,
                name = "Tracked Product",
                price = 10.0,
                stockQuantity = 1,
            ),
        )

        assertEquals(
            null,
            firstInvoiceStockValidationMessage(
                DocumentType.Invoice,
                listOf(DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "5", unitPrice = "10")),
                products,
                app.tijario.config.AppLanguage.EN,
                mapOf("product-1" to 4),
            ),
        )
        assertEquals(
            "Quantity exceeds available stock. Current stock: 5",
            firstInvoiceStockValidationMessage(
                DocumentType.Invoice,
                listOf(DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "6", unitPrice = "10")),
                products,
                app.tijario.config.AppLanguage.EN,
                mapOf("product-1" to 4),
            ),
        )
    }

    @Test
    fun invoiceStockValidationAggregatesDuplicateProductRows() {
        val products = listOf(
            Product(
                id = "product-1",
                kind = ProductKind.Product,
                name = "Tracked Product",
                price = 10.0,
                stockQuantity = 5,
            ),
        )

        assertEquals(
            "Quantity exceeds available stock. Current stock: 5",
            firstInvoiceStockValidationMessage(
                DocumentType.Invoice,
                listOf(
                    DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "3", unitPrice = "10"),
                    DocumentItemState(productId = "product-1", name = "Tracked Product", quantity = "3", unitPrice = "10"),
                ),
                products,
                app.tijario.config.AppLanguage.EN,
            ),
        )
    }
}
