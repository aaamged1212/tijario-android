package app.tijario.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentNumberReconciliationTest {

    @Test
    fun serverNumberWinsWhenAndroidSentAPreviewNumber() {
        assertEquals(
            "INV-0016",
            resolveCachedDocumentNumber(
                serverNumber = "INV-0016",
                requestedPreviewNumber = "INV-0015",
            ),
        )
    }

    @Test
    fun serverNumberWinsWhenAndroidDidNotSendAPreviewNumber() {
        assertEquals(
            "Q-0042",
            resolveCachedDocumentNumber(
                serverNumber = "Q-0042",
                requestedPreviewNumber = null,
            ),
        )
    }
}
