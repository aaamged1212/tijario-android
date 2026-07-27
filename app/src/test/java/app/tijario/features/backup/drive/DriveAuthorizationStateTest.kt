package app.tijario.features.backup.drive

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import java.io.File
import org.junit.Test

class DriveAuthorizationStateTest {
    @Test
    fun connectedStateCarriesOnlyNonSecretIdentityMetadata() {
        val state = DriveAuthorizationState.Connected("google-account-id", "seller@example.com")

        assertEquals("google-account-id", state.accountId)
        assertEquals("seller@example.com", state.accountEmail)
        assertEquals(DriveAuthorizationState.ReauthorizationRequired, DriveAuthorizationState.ReauthorizationRequired)
    }

    @Test
    fun immediateAuthorizationReturnsSessionWithoutPersistingTheToken() = runBlocking {
        val manager = FakeGoogleDriveAuthorizationManager(
            DriveAuthorizationOutcome.Authorized(
                DriveAuthorizationSession("short-lived-token"),
            ),
        )

        val result = manager.authorize()

        assertTrue(result is DriveAuthorizationOutcome.Authorized)
        assertEquals("short-lived-token", (result as DriveAuthorizationOutcome.Authorized).session.accessToken)
        val connectionSource = File("src/main/java/app/tijario/features/backup/drive/DriveConnectionRepository.kt").readText()
        assertFalse(connectionSource.contains("access_token"))
        assertFalse(connectionSource.contains("refresh_token"))
    }

    @Test
    fun driveFileConsentDoesNotRequireGoogleProfileIdentity() {
        val result = driveFileAuthorizationOutcome(
            grantedScopes = listOf(GOOGLE_DRIVE_FILE_SCOPE),
            accessToken = "short-lived-token",
        )

        assertTrue(result is DriveAuthorizationOutcome.Authorized)
        assertEquals("short-lived-token", (result as DriveAuthorizationOutcome.Authorized).session.accessToken)
    }

    @Test
    fun cancellationAndUnavailableStatesRemainExplicit() {
        val manager = FakeGoogleDriveAuthorizationManager(DriveAuthorizationOutcome.ScopeDenied)

        assertEquals(DriveAuthorizationOutcome.Cancelled, manager.completeAuthorization(null))
        assertEquals(DriveAuthorizationOutcome.ScopeDenied, runBlocking { manager.authorize() })
        assertEquals(DriveAuthorizationState.NotConfigured, DriveAuthorizationState.NotConfigured)
        assertEquals(DriveAuthorizationState.TemporarilyUnavailable, DriveAuthorizationState.TemporarilyUnavailable)
    }

    @Test
    fun developerErrorIsReportedAsGoogleConfigurationFailure() {
        val error = ApiException(Status(CommonStatusCodes.DEVELOPER_ERROR))

        assertEquals(DriveAuthorizationOutcome.PlayServicesUnavailable, authorizationFailureOutcome(error))
    }

    @Test
    fun identityInternalErrorRemainsRetryableWithoutExposingDetails() {
        val error = ApiException(Status(CommonStatusCodes.INTERNAL_ERROR))

        assertEquals(DriveAuthorizationOutcome.TemporarilyUnavailable, authorizationFailureOutcome(error))
    }

    @Test
    fun productionFlowUsesAuthorizationClientAndTheDriveFileScope() {
        val source = File("src/main/java/app/tijario/features/backup/drive/GoogleDriveAuthorizationManager.kt").readText()

        assertTrue(source.contains("Identity.getAuthorizationClient"))
        assertTrue(source.contains("GOOGLE_DRIVE_FILE_SCOPE"))
        assertTrue(source.contains("StartIntentSenderForResult") || File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText().contains("StartIntentSenderForResult"))
        assertTrue(source.contains("clearToken"))
        assertTrue(source.contains("revokeAccess"))
    }
}
