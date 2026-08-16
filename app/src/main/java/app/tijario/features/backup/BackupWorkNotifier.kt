package app.tijario.features.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import app.tijario.R
import app.tijario.config.AppPreferences
import app.tijario.config.Localization
import java.util.UUID

const val BACKUP_NOTIFICATION_CHANNEL_ID = "tijario_backup_restore"
private const val BACKUP_NOTIFICATION_ID = 4101

internal fun backupNotificationsAvailable(appEnabled: Boolean, channelImportance: Int?): Boolean =
    appEnabled && channelImportance != null && channelImportance != NotificationManager.IMPORTANCE_NONE

fun ensureBackupNotificationChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java) ?: return
    if (manager.getNotificationChannel(BACKUP_NOTIFICATION_CHANNEL_ID) != null) return
    manager.createNotificationChannel(
        NotificationChannel(
            BACKUP_NOTIFICATION_CHANNEL_ID,
            "Backup and Restore | النسخ والاستعادة",
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
    )
}

class BackupWorkNotifier(private val context: Context) {
    fun foregroundInfo(workId: UUID, title: String, detail: String, progress: Int? = null): ForegroundInfo {
        ensureBackupNotificationChannel(context)
        val foregroundNotification = notification(title, detail, progress, workId)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId(workId),
                foregroundNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(notificationId(workId), foregroundNotification)
        }
    }

    fun notification(title: String, detail: String, progress: Int? = null, workId: UUID? = null) = NotificationCompat.Builder(context, BACKUP_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_tijario)
        .setContentTitle(title)
        .setContentText(detail)
        .apply {
            ContextCompat.getDrawable(context, R.drawable.tijario_notification_logo)
                ?.let { drawable ->
                    setLargeIcon(drawable.toBitmap(width = 128, height = 128))
                }
        }
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
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (permissionGranted && notificationsAvailable()) {
            try {
                NotificationManagerCompat.from(context).notify(BACKUP_NOTIFICATION_ID, notification(title, detail))
            } catch (_: SecurityException) {
                // The user can revoke notification permission between the explicit check and posting.
            }
        }
    }

    fun clear(workId: UUID) {
        NotificationManagerCompat.from(context).cancel(notificationId(workId))
    }

    fun notificationsAvailable(): Boolean {
        ensureBackupNotificationChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return backupNotificationsAvailable(
            hasNotificationPermission() && NotificationManagerCompat.from(context).areNotificationsEnabled(),
            manager.getNotificationChannel(BACKUP_NOTIFICATION_CHANNEL_ID)?.importance,
        )
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun settingsIntent(): Intent {
        ensureBackupNotificationChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        return if (manager?.getNotificationChannel(BACKUP_NOTIFICATION_CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) {
            Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, BACKUP_NOTIFICATION_CHANNEL_ID)
        } else {
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }

    companion object {
        const val NOTIFICATION_ID = BACKUP_NOTIFICATION_ID
        fun notificationId(workId: UUID): Int = BACKUP_NOTIFICATION_ID + (workId.hashCode() and 0x0FFF)
    }
}
