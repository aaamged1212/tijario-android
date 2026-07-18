package app.tijario.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RemoteCacheReplacementPolicyTest {

    @Test
    fun protectsUnsyncedAndConflictedLocalRows() {
        listOf(
            "LOCAL_ONLY",
            "PENDING_SYNC",
            "PENDING_DELETE",
            "CONFLICT",
            "BLOCKED_BY_PLAN",
            "failed_non_retryable",
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
    fun repositoryRemoteCacheCallSitesDelegateToSharedPolicy() {
        val source = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()

        assertTrue(source.contains("RemoteCacheReplacementPolicy.shouldReplace(existing?.syncStatus)"))
        assertTrue(source.contains("RemoteCacheReplacementPolicy.shouldReplace(local?.syncStatus)"))
        assertFalse(source.contains("syncStatus in listOf(\"LOCAL_ONLY\", \"PENDING_SYNC\", \"PENDING_DELETE\", \"CONFLICT\")"))
        assertFalse(source.contains("syncStatus !in listOf(\"LOCAL_ONLY\", \"PENDING_SYNC\", \"PENDING_DELETE\", \"CONFLICT\")"))
    }
}
