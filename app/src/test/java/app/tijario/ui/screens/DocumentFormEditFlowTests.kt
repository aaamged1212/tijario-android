package app.tijario.ui.screens

import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
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
}
