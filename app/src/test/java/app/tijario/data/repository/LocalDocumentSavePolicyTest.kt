package app.tijario.data.repository

import app.tijario.config.AppLanguage
import app.tijario.domain.LocalizedErrorMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalDocumentSavePolicyTest {
    @Test
    fun missingBootstrapAndLeaseFailuresRemainTypedForLocalSave() {
        assertEquals(
            "ENTITLEMENT_INITIALIZATION_REQUIRED",
            localDocumentFailureCode(IllegalStateException("ENTITLEMENT_INITIALIZATION_REQUIRED")),
        )
        assertEquals(
            "OFFLINE_LEASE_REQUIRED",
            localDocumentFailureCode(IllegalStateException("OFFLINE_LEASE_REQUIRED")),
        )
        assertEquals(
            "ENTITLEMENT_INITIALIZATION_REQUIRED",
            localDocumentFailureCode(IllegalStateException("ENTITLEMENT_EXPIRED")),
        )
        assertEquals(
            "DOCUMENT_LIMIT_REACHED",
            localDocumentFailureCode(IllegalStateException("QUOTA_LIMIT_EXCEEDED")),
        )
        assertTrue(
            LocalizedErrorMapper.map("OFFLINE_LEASE_REQUIRED", null, AppLanguage.AR)
                .contains("الاتصال"),
        )
    }

    @Test
    fun unknownLocalFailureDoesNotExposeItsMessage() {
        assertEquals(
            "LOCAL_DOCUMENT_SAVE_FAILED",
            localDocumentFailureCode(IllegalStateException("room corruption details")),
        )
    }

    @Test
    fun localDocumentCreateAndUpdateDoNotScheduleOperationalCloudSync() {
        val source = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()
        val createAndUpdate = source.substringAfter("suspend fun createDocumentLocal")
            .substringBefore("suspend fun deleteDocumentLocal")

        assertFalse(createAndUpdate.contains("SyncScheduler(context).triggerSync"))
        assertFalse(createAndUpdate.contains("enqueueOutbox("))
        assertTrue(createAndUpdate.contains("if (!isLocalDrive)"))
    }

    @Test
    fun localDriveSaveKeepsDocumentsAndQuotaEventsInOneTransaction() {
        val source = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()
        val create = source.substringAfter("suspend fun createDocumentLocal")
            .substringBefore("suspend fun updateDocumentLocal")
        val update = source.substringAfter("suspend fun updateDocumentLocal")
            .substringBefore("suspend fun deleteDocumentLocal")

        assertTrue(create.contains("DocumentType.Invoice"))
        assertTrue(create.contains("DocumentType.Quote"))
        assertTrue(create.contains("database.withTransaction"))
        assertTrue(create.contains("dao.upsertCustomer(customerEntityToUpsert)"))
        assertTrue(create.contains("dao.upsertDocument(docEntity)"))
        assertTrue(create.contains("dao.insertDocumentItems(itemsEntities)"))
        assertTrue(create.contains("reserveDocumentQuotaLedger(userId, docId)"))
        assertTrue(update.contains("syncStatus = nextStatus"))
        assertTrue(update.contains("dao.deleteDocumentItems(userId, documentId)"))
        assertFalse(update.contains("reserveDocumentQuotaLedger"))
    }
}
