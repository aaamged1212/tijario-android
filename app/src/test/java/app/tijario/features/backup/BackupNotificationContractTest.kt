package app.tijario.features.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupNotificationContractTest {
    @Test
    fun backupNotificationUsesDedicatedChannelAndDoesNotTreatPermissionDenialAsFailure() {
        val source = File("src/main/java/app/tijario/features/backup/BackupWorkNotifier.kt").readText()
        assertEquals("tijario_backup_restore", BACKUP_NOTIFICATION_CHANNEL_ID)
        assertTrue(source.contains("areNotificationsEnabled"))
        assertTrue(source.contains("Backup and Restore"))
        assertTrue(source.contains("createCancelPendingIntent"))
        assertTrue(source.contains("foregroundInfo"))
    }
}
