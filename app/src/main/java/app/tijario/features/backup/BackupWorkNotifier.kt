package app.tijario.features.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.tijario.R

const val BACKUP_NOTIFICATION_CHANNEL_ID = "tijario_backup_restore"
private const val BACKUP_NOTIFICATION_ID = 4101

fun ensureBackupNotificationChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java) ?: return
    if (manager.getNotificationChannel(BACKUP_NOTIFICATION_CHANNEL_ID) != null) return
    manager.createNotificationChannel(
        NotificationChannel(
            BACKUP_NOTIFICATION_CHANNEL_ID,
            "Backup and Restore | النسخ والاستعادة",
            NotificationManager.IMPORTANCE_LOW,
        ),
    )
}

class BackupWorkNotifier(private val context: Context) {
    fun notification(title: String, detail: String, progress: Int? = null) = NotificationCompat.Builder(context, BACKUP_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_tijario)
        .setContentTitle(title)
        .setContentText(detail)
        .setOnlyAlertOnce(true)
        .setOngoing(progress != null)
        .apply { progress?.let { setProgress(100, it.coerceIn(0, 100), false) } }
        .build()

    fun post(title: String, detail: String) {
        ensureBackupNotificationChannel(context)
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(BACKUP_NOTIFICATION_ID, notification(title, detail))
        }
    }

    companion object {
        const val NOTIFICATION_ID = BACKUP_NOTIFICATION_ID
    }
}
