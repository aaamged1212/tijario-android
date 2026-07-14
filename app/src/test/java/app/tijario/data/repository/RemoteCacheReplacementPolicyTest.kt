package app.tijario.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteCacheReplacementPolicyTest {

    @Test
    fun protectsUnsyncedAndConflictedLocalRows() {
        listOf(
            "LOCAL_ONLY",
            "PENDING_SYNC",
            "PENDING_DELETE",
            "CONFLICT",
        ).forEach { status ->
            assertFalse(RemoteCacheReplacementPolicy.shouldReplace(status))
        }
    }

    @Test
    fun allowsMissingOrSyncedLocalRowsToBeReplaced() {
        assertTrue(RemoteCacheReplacementPolicy.shouldReplace(null))
        assertTrue(RemoteCacheReplacementPolicy.shouldReplace("SYNCED"))
    }

    @Test
    fun doesNotTreatUnrelatedTerminalStatusesAsPendingLocalEdits() {
        assertTrue(RemoteCacheReplacementPolicy.shouldReplace("BLOCKED_BY_PLAN"))
        assertTrue(RemoteCacheReplacementPolicy.shouldReplace("failed_non_retryable"))
    }
}
