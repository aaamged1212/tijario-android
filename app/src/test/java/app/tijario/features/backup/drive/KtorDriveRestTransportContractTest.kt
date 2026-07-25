package app.tijario.features.backup.drive

import org.junit.Assert.assertEquals
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
    fun transportResolvesDriveIdentityAndMapsHttpFailuresWithoutLoggingSecrets() {
        val source = File("src/main/java/app/tijario/features/backup/drive/KtorDriveRestTransport.kt").readText()

        assertTrue(source.contains("/about"))
        assertTrue(source.contains("user(permissionId,emailAddress)"))
        assertTrue(source.contains("safeDriveLog"))
        assertFalse(source.contains("println("))
        assertFalse(source.contains("accessToken="))
        assertTrue(driveFailureFor(401, "authError") is DriveHttpException.Unauthorized)
        assertTrue(driveFailureFor(403, "accessNotConfigured") is DriveHttpException.NotConfigured)
        assertTrue(driveFailureFor(403, "permissionDenied") is DriveHttpException.PermissionDenied)
        assertTrue(driveFailureFor(400, "invalid") is DriveHttpException.BadRequest)
        assertTrue(driveFailureFor(429, "rateLimitExceeded") is DriveBackupException.Retryable)
        assertTrue(driveFailureFor(503, "backendError") is DriveBackupException.Retryable)
        assertEquals(
            DriveConnectionState.ReauthorizationRequired,
            driveConnectionStateFor(driveFailureFor(401, "authError")),
        )
    }
}
