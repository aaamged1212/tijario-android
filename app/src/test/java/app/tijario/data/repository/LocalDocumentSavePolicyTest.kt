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
    fun localDriveSaveUsesTheSignedPlanLimitWithoutRequiringALease() {
        val source = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()
        val localDriveReservation = source.substringAfter("if (dataMode == AccountDataMode.LocalDrive)")
            .substringBefore("val pendingLedgers")

        assertTrue(localDriveReservation.contains("entitlement.documentsUsed + pendingEvents >= limit"))
        assertTrue(localDriveReservation.contains("leaseIdForEvent"))
        assertFalse(localDriveReservation.contains("?: error(\"OFFLINE_LEASE_REQUIRED\")"))
        assertFalse(localDriveReservation.contains("lease.consumedCount + leasePending >= lease.allowedLimit"))
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

    @Test
    fun localDriveBusinessSettingsMirrorRunsOnceForEachChangedPayload() {
        val source = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()
        val mirror = source.substringAfter("private suspend fun mirrorLocalDriveBusinessSettings")
            .substringBefore("// Legacy Save / Cache adapters")

        assertTrue(mirror.contains("getBusinessSettingsMirrorFingerprint"))
        assertTrue(mirror.contains("supabaseClient.from(\"business_settings\").upsert(remoteSettings)"))
        assertTrue(mirror.contains("setBusinessSettingsMirrorFingerprint"))
        assertFalse(mirror.contains("SyncScheduler(context).triggerSync"))
        assertFalse(mirror.contains("enqueueOperationalOutbox"))
    }
}
