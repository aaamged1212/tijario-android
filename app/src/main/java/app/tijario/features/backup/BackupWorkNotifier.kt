package app.tijario.features.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import app.tijario.R
import app.tijario.config.AppPreferences
import app.tijario.config.Localization
import java.util.UUID

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
    fun foregroundInfo(workId: UUID, title: String, detail: String, progress: Int? = null): ForegroundInfo {
        ensureBackupNotificationChannel(context)
        return ForegroundInfo(notificationId(workId), notification(title, detail, progress, workId))
    }

    fun notification(title: String, detail: String, progress: Int? = null, workId: UUID? = null) = NotificationCompat.Builder(context, BACKUP_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_tijario)
        .setContentTitle(title)
        .setContentText(detail)
        .setOnlyAlertOnce(true)
        .setOngoing(progress != null)
        .apply {
            workId?.let {
                addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    Localization.getString("cancel", AppPreferences.getLanguage(context)),
                    WorkManager.getInstance(context).createCancelPendingIntent(it),
                )
            }
        }
        .apply { progress?.let { setProgress(100, it.coerceIn(0, 100), false) } }
        .build()

    fun post(title: String, detail: String) {
        ensureBackupNotificationChannel(context)
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(BACKUP_NOTIFICATION_ID, notification(title, detail))
        }
    }

    fun clear(workId: UUID) {
        NotificationManagerCompat.from(context).cancel(notificationId(workId))
    }

    fun notificationsAvailable(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.getNotificationChannel(BACKUP_NOTIFICATION_CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    companion object {
        const val NOTIFICATION_ID = BACKUP_NOTIFICATION_ID
        fun notificationId(workId: UUID): Int = BACKUP_NOTIFICATION_ID + (workId.hashCode() and 0x0FFF)
    }
}
