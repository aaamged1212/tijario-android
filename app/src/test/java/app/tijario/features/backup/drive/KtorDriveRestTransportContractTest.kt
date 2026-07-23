package app.tijario.features.backup.drive

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KtorDriveRestTransportContractTest {
    @Test
    fun productionTransportUsesEncodedDriveQueriesAndStreamingFiles() {
        val source = File("src/main/java/app/tijario/features/backup/drive/KtorDriveRestTransport.kt").readText()

        assertTrue(source.contains("parameters.append(\"q\", query)"))
        assertTrue(source.contains("parameters.append(\"fields\", DRIVE_LIST_FIELDS)"))
        assertTrue(source.contains("FileStreamingContent"))
        assertTrue(source.contains("bodyAsChannel"))
        assertTrue(source.contains("MAX_DOWNLOAD_BYTES"))
        assertFalse(source.contains("file.readBytes()"))
    }

    @Test
    fun transportMapsAuthorizationAndRetryableHttpFailuresWithoutLoggingSecrets() {
        val source = File("src/main/java/app/tijario/features/backup/drive/KtorDriveRestTransport.kt").readText()

        assertTrue(source.contains("401 -> throw DriveHttpException.Unauthorized"))
        assertTrue(source.contains("429 -> throw DriveBackupException.Retryable"))
        assertTrue(source.contains("in 500..599 -> throw DriveBackupException.Retryable"))
        assertFalse(source.contains("println("))
        assertFalse(source.contains("Log."))

        val client = File("src/main/java/app/tijario/features/backup/drive/GoogleDriveRestClient.kt").readText()
        assertTrue(client.contains("catch (_: DriveHttpException.Forbidden)"))
        assertTrue(client.contains("catch (_: DriveHttpException.NotFound)"))
    }
}
