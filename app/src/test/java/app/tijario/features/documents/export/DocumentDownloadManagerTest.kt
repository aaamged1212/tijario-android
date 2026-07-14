package app.tijario.features.documents.export

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentDownloadManagerTest {
    @Test
    fun usesMediaStoreDownloads_onlyFromAndroid10() {
        assertFalse(
            DocumentDownloadManager.usesMediaStoreDownloads(Build.VERSION_CODES.P),
        )
        assertTrue(
            DocumentDownloadManager.usesMediaStoreDownloads(Build.VERSION_CODES.Q),
        )
        assertTrue(
            DocumentDownloadManager.usesMediaStoreDownloads(Build.VERSION_CODES.Q + 1),
        )
    }
}
