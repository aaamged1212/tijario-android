package app.tijario.features.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupKeyErrorMappingTest {
    @Test
    fun invalidCachedEnvelopeRefreshesOnlyWhenNetworkIsAllowed() {
        assertEquals(true, shouldRefreshInvalidCachedBackupKey(allowNetwork = true))
        assertEquals(false, shouldRefreshInvalidCachedBackupKey(allowNetwork = false))
    }

    @Test
    fun unregisteredInstallationDoesNotUsePrimaryDeviceLanguage() {
        assertEquals(
            "backup_installation_not_registered",
            backupMessageKeyFor(BackupKeyException("backup_installation_not_registered")),
        )
    }

    @Test
    fun onlyOfflineMissingKeyUsesConnectOnceMessage() {
        assertEquals(
            "backup_create_failed",
            backupMessageKeyFor(BackupKeyException("OFFLINE_KEY_UNAVAILABLE")),
        )
        assertEquals(
            "backup_key_unavailable",
            backupMessageKeyFor(BackupKeyException("backup_key_unavailable")),
        )
    }
}
