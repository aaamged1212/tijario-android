package app.tijario.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    }
}
