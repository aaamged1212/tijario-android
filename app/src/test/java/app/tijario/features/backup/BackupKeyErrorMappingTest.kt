package app.tijario.features.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupKeyErrorMappingTest {
    @Test
    fun primaryDeviceConflictIsNotCollapsedIntoOfflineKeyMessage() {
        assertEquals(
            "backup_device_not_primary",
            backupMessageKeyFor(BackupKeyException("backup_device_not_primary")),
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
