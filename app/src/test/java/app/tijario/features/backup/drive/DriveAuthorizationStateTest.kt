package app.tijario.features.backup.drive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveAuthorizationStateTest {
    @Test
    fun connectedStateCarriesOnlyNonSecretIdentityMetadata() {
        val state = DriveAuthorizationState.Connected("google-account-id", "seller@example.com")

        assertEquals("google-account-id", state.accountId)
        assertEquals("seller@example.com", state.accountEmail)
        assertTrue(DriveAuthorizationState.ReauthorizationRequired is DriveAuthorizationState)
    }
}
