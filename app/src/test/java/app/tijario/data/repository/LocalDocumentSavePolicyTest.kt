package app.tijario.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalDocumentSavePolicyTest {
    @Test
    fun missingBootstrapAndQuotaFailuresRemainTypedForLocalSave() {
        assertEquals(
            "ENTITLEMENT_INITIALIZATION_REQUIRED",
            localDocumentFailureCode(IllegalStateException("ENTITLEMENT_INITIALIZATION_REQUIRED")),
        )
        assertEquals(
            "OFFLINE_QUOTA_UNAVAILABLE",
            localDocumentFailureCode(IllegalStateException("OFFLINE_QUOTA_UNAVAILABLE")),
        )
        assertEquals(
            "DOCUMENT_LIMIT_REACHED",
            localDocumentFailureCode(IllegalStateException("QUOTA_LIMIT_EXCEEDED")),
        )
    }

    @Test
    fun localDriveSaveRequiresAReconciliableLeaseCredit() {
        val source = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()
        val localDriveReservation = source.substringAfter("if (dataMode == AccountDataMode.LocalDrive)")
            .substringBefore("val pendingLedgers")

        assertTrue(source.contains("ensureQuotaCreditForDocumentCreation"))
        assertTrue(source.contains("quotaReservationMutex"))
        assertTrue(localDriveReservation.contains("leaseId = lease.id"))
        assertTrue(localDriveReservation.contains("OFFLINE_QUOTA_UNAVAILABLE"))
        assertFalse(localDriveReservation.contains("leaseIdForEvent"))
    }

    @Test
    fun oneLeaseCreditCannotBeReservedTwice() {
        assertTrue(hasLeaseCredit(1, 0, 0))
        assertFalse(hasLeaseCredit(1, 0, 1))
        assertFalse(hasLeaseCredit(1, 1, 0))
    }

    @Test
    fun consumedLeaseCreditRemainsUnavailableWhenALaterRefreshFails() {
        // A refresh result cannot reset the persisted consumed count.
        assertFalse(hasLeaseCredit(1, 1, 0))
    }

    @Test
    fun leaseMustMatchTheCurrentPeriodForEveryQuotaScope() {
        assertTrue(leaseMatchesPeriod("lifetime", "lifetime"))
        assertTrue(leaseMatchesPeriod("2026-07-01", "2026-07-01"))
        assertFalse(leaseMatchesPeriod("2026-06-01", "2026-07-01"))
    }

    @Test
    fun legacyLeaseRecoveryAssignsOnlyAvailableCreditsAndLeavesExcessPending() {
        assertEquals(1, assignableLegacyEventCount(5, 3, 1, 3))
        assertEquals(0, assignableLegacyEventCount(5, 5, 0, 3))
        assertEquals(3, assignableLegacyEventCount(5, 0, 0, 3))
    }

    @Test
    fun onlyServerTerminalCreationResultsBlockLegacyEvents() {
        assertTrue(isTerminalDocumentCreationEventFailure("DOCUMENT_LIMIT_REACHED"))
        assertTrue(isTerminalDocumentCreationEventFailure("INSTALLATION_REVOKED"))
        assertTrue(isTerminalDocumentCreationEventFailure("ENTITLEMENT_VERSION_MISMATCH"))
        assertFalse(isTerminalDocumentCreationEventFailure("OFFLINE_LEASE_EXHAUSTED"))
        assertFalse(isTerminalDocumentCreationEventFailure("OFFLINE_QUOTA_UNAVAILABLE"))
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
        assertTrue(create.contains("reserveDocumentQuotaLedger(userId, docId, quotaCredit)"))
        assertTrue(update.contains("syncStatus = nextStatus"))
        assertTrue(update.contains("dao.deleteDocumentItems(userId, documentId)"))
        assertFalse(update.contains("reserveDocumentQuotaLedger"))
    }
}
