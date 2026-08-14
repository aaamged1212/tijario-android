package app.tijario.data.local

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentOrderingSourceTests {
    @Test
    fun documentsAreOrderedByCreatedAtBeforeSyncFallback() {
        val source = File("src/main/java/app/tijario/data/local/TijarioDao.kt").readText()

        assertTrue(source.contains("COALESCE(created_at, issue_date) DESC"))
        assertTrue(source.contains("synced_at DESC, local_revision DESC, id DESC"))
        assertTrue(!source.contains("synced_at DESC, document_number DESC"))
    }

    @Test
    fun legacyDownloadsDeclareAndCheckWritePermission() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val downloadManager = File("src/main/java/app/tijario/features/documents/export/DocumentDownloadManager.kt").readText()

        assertTrue(manifest.contains("android.permission.WRITE_EXTERNAL_STORAGE"))
        assertTrue(manifest.contains("android:maxSdkVersion=\"28\""))
        assertTrue(downloadManager.contains("needsLegacyWritePermission"))
        assertTrue(downloadManager.contains("Build.VERSION.SDK_INT < Build.VERSION_CODES.Q"))
    }
}
