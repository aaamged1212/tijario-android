package app.tijario.data.local

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentOrderingSourceTests {
    @Test
    fun documentsAreOrderedByNewestSyncTimestampBeforeIssueDateFallback() {
        val source = File("src/main/java/app/tijario/data/local/TijarioDao.kt").readText()

        assertTrue(source.contains("COALESCE(server_updated_at, last_synced_at, synced_at) DESC"))
        assertTrue(source.contains("issue_date DESC, document_number DESC"))
    }
}
