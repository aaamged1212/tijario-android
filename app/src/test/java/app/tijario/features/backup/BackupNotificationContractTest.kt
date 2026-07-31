package app.tijario.features.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupNotificationContractTest {
    @Test
    fun notificationIdsRemainSpecificToTheExactWorkRequest() {
        val first = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")
        val second = java.util.UUID.fromString("00000000-0000-0000-0000-000000000002")

        assertTrue(BACKUP_NOTIFICATION_CHANNEL_ID.isNotBlank())
        assertTrue(BackupWorkNotifier.notificationId(first) != BackupWorkNotifier.notificationId(second))
    }

    @Test
    fun notificationsRequireBothAppPermissionAndAnEnabledBackupChannel() {
        assertTrue(backupNotificationsAvailable(true, android.app.NotificationManager.IMPORTANCE_DEFAULT))
        assertFalse(backupNotificationsAvailable(false, android.app.NotificationManager.IMPORTANCE_DEFAULT))
        assertFalse(backupNotificationsAvailable(true, android.app.NotificationManager.IMPORTANCE_NONE))
    }
}
