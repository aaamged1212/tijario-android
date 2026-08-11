package app.tijario.domain

import app.tijario.data.model.DocumentType
import app.tijario.data.model.UserPlanUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeQaPolicyTests {
    @Test
    fun documentNumbers_preserveWidthAndKeepInvoiceAndQuoteSequencesIndependent() {
        assertEquals("INV-00101", DocumentNumbering.resolveForCreate("INV-00101", emptyList(), DocumentType.Invoice))
        assertEquals("INV-00102", DocumentNumbering.nextDocumentNumber(listOf("INV-00101"), DocumentType.Invoice))
        assertEquals("INV-000102", DocumentNumbering.nextDocumentNumber(listOf("INV-000101"), DocumentType.Invoice))
        assertEquals("INV-0000102", DocumentNumbering.nextDocumentNumber(listOf("INV-0000101"), DocumentType.Invoice))
        assertEquals("INV-100000", DocumentNumbering.nextDocumentNumber(listOf("INV-99999"), DocumentType.Invoice))
        assertEquals("Q-00001", DocumentNumbering.nextDocumentNumber(listOf("INV-00101"), DocumentType.Quote))
        assertEquals("Q-00042", DocumentNumbering.nextDocumentNumber(listOf("QT-00041"), DocumentType.Quote))
    }

    @Test
    fun localDriveOverlay_usesActiveCustomerAndProductCountsWithoutChangingLimits() {
        val authoritative = UserPlanUsage(
            planCode = "free",
            planName = "Free",
            periodMonth = "2026-08-01",
            documentsUsed = 1,
            documentsLimit = 5,
            aiUsed = 0,
            aiLimit = 5,
            customersUsed = 0,
            customersLimit = 5,
            productsUsed = 0,
            productsLimit = 5,
        )

        val effective = effectivePlanUsage(
            usage = authoritative,
            isLocalDrive = true,
            activeCustomers = 5,
            activeProducts = 4,
            pendingDocumentEvents = 1,
        )

        assertEquals(5, effective.customersUsed)
        assertEquals(4, effective.productsUsed)
        assertEquals(2, effective.documentsUsed)
        assertEquals(5, effective.customersLimit)
        assertEquals(5, effective.productsLimit)
    }

    @Test
    fun creationTargets_keepTypedLimitCodes() {
        assertEquals("CUSTOMER_LIMIT_REACHED", CreationTarget.Customer.limitErrorCode())
        assertEquals("PRODUCT_LIMIT_REACHED", CreationTarget.Product.limitErrorCode())
        assertEquals("DOCUMENT_LIMIT_REACHED", CreationTarget.Invoice.limitErrorCode())
        assertEquals(CreationTarget.Invoice, creationTargetForErrorCode("QUOTA_LIMIT_EXCEEDED"))
        assertEquals(CreationTarget.Product, creationTargetForErrorCode("PRODUCT_LIMIT_REACHED"))
        assertTrue(creationTargetForErrorCode("unknown") == null)
    }

    @Test
    fun nonLocalDriveOverlay_keepsServerCustomerAndProductUsage() {
        val usage = UserPlanUsage("starter", "Starter", "2026-08-01", 2, 20, 0, 20, 1, 10, 2, 10)
        val effective = effectivePlanUsage(usage, isLocalDrive = false, activeCustomers = 9, activeProducts = 8)

        assertEquals(1, effective.customersUsed)
        assertEquals(2, effective.productsUsed)
        assertEquals(2, effective.documentsUsed)
    }
}
