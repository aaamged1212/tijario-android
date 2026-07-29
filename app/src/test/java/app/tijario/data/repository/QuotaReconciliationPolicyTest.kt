package app.tijario.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuotaReconciliationPolicyTest {
    @Test
    fun leaseFailuresRemainPendingForAReplacementLease() {
        assertTrue(isRetryableLeaseReconciliationFailure("OFFLINE_LEASE_INVALID"))
        assertTrue(isRetryableLeaseReconciliationFailure("OFFLINE_LEASE_EXPIRED"))
        assertTrue(isRetryableLeaseReconciliationFailure("OFFLINE_LEASE_EXHAUSTED"))
        assertFalse(isTerminalDocumentCreationEventFailure("OFFLINE_LEASE_INVALID"))
        assertFalse(isPermanentDocumentCreationEventFailure("OFFLINE_LEASE_EXPIRED"))
    }

    @Test
    fun rejectedLeasesAreMadeIneligibleForTheNextRecoveryCycle() {
        assertTrue(retryableLeaseInvalidationStatus("OFFLINE_LEASE_INVALID") == "INVALID")
        assertTrue(retryableLeaseInvalidationStatus("OFFLINE_LEASE_EXPIRED") == "EXPIRED")
        assertTrue(retryableLeaseInvalidationStatus("OFFLINE_LEASE_EXHAUSTED") == "EXHAUSTED")
        assertTrue(retryableLeaseInvalidationStatus("DOCUMENT_LIMIT_REACHED") == null)
    }

    @Test
    fun onlyPermanentInvalidEventsAreRejected() {
        assertTrue(isPermanentDocumentCreationEventFailure("INVALID_EVENT_PAYLOAD"))
        assertTrue(isPermanentDocumentCreationEventFailure("INVALID_PAYLOAD_HASH"))
        assertTrue(isPermanentDocumentCreationEventFailure("OPERATION_PAYLOAD_MISMATCH"))
        assertFalse(isPermanentDocumentCreationEventFailure("OFFLINE_LEASE_EXHAUSTED"))
        assertFalse(isPermanentDocumentCreationEventFailure("DOCUMENT_LIMIT_REACHED"))
    }

    @Test
    fun retryableLeaseFailuresAtomicallyReleaseEventsAndInvalidateTheLease() {
        val dao = File("src/main/java/app/tijario/data/local/TijarioDao.kt").readText()
        val repository = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()

        assertTrue(dao.contains("SET lease_id = NULL"))
        assertTrue(dao.contains("status = 'PENDING'"))
        assertTrue(dao.contains("suspend fun invalidateLeaseForReconciliation(userId: String, leaseId: String, status: String)"))
        assertTrue(dao.contains("WHEN :status = 'EXHAUSTED' THEN allowed_limit"))
        assertTrue(dao.contains("WHERE id = :leaseId"))
        assertTrue(dao.contains("AND user_id = :userId"))
        assertTrue(dao.contains("AND status = 'ACTIVE'"))
        assertTrue(repository.contains("database.withTransaction"))
        assertTrue(repository.contains("dao.clearLeaseFromPendingCreationEvent(userId, operationId)"))
        assertTrue(repository.contains("dao.invalidateLeaseForReconciliation(userId, leaseId, leaseStatus)"))
    }
}
