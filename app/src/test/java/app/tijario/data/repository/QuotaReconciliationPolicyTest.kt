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
    fun onlyPermanentInvalidEventsAreRejected() {
        assertTrue(isPermanentDocumentCreationEventFailure("INVALID_EVENT_PAYLOAD"))
        assertTrue(isPermanentDocumentCreationEventFailure("INVALID_PAYLOAD_HASH"))
        assertTrue(isPermanentDocumentCreationEventFailure("OPERATION_PAYLOAD_MISMATCH"))
        assertFalse(isPermanentDocumentCreationEventFailure("OFFLINE_LEASE_EXHAUSTED"))
        assertFalse(isPermanentDocumentCreationEventFailure("DOCUMENT_LIMIT_REACHED"))
    }

    @Test
    fun retryableLeaseFailuresClearOnlyPendingEventLeases() {
        val dao = File("src/main/java/app/tijario/data/local/TijarioDao.kt").readText()
        val repository = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()

        assertTrue(dao.contains("SET lease_id = NULL"))
        assertTrue(dao.contains("status = 'PENDING'"))
        assertTrue(repository.contains("dao.clearLeaseFromPendingCreationEvent(userId, event.operationId)"))
    }
}
