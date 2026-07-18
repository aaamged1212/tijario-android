package app.tijario.features.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.tijario.data.local.BackupSettingsEntity
import java.util.concurrent.TimeUnit

object BackupScheduler {
    fun apply(context: Context, settings: BackupSettingsEntity) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val workName = workName(settings.userId)
        val intervalDays = intervalDays(settings.frequency)
        if (intervalDays == null) {
            workManager.cancelUniqueWork(workName)
            return
        }

        val request = PeriodicWorkRequestBuilder<LocalBackupWorker>(intervalDays, TimeUnit.DAYS)
            .setInitialDelay(intervalDays, TimeUnit.DAYS)
            .setInputData(workDataOf(LocalBackupWorker.USER_ID_KEY to settings.userId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(settings.chargingOnly)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun enqueueDriveUpload(context: Context, settings: BackupSettingsEntity, backupId: String) {
        if (!settings.driveEnabled || backupId.isBlank()) return
        val networkType = if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val request = OneTimeWorkRequestBuilder<DriveUploadWorker>()
            .addTag(driveAccountTag(settings.userId))
            .setInputData(
                workDataOf(
                    DriveUploadWorker.USER_ID_KEY to settings.userId,
                    DriveUploadWorker.BACKUP_ID_KEY to backupId,
                ),
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(networkType)
                    .setRequiresCharging(settings.chargingOnly)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            driveWorkName(settings.userId, backupId),
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelAccountWork(context: Context, userId: String) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(workName(userId))
        workManager.cancelAllWorkByTag(driveAccountTag(userId))
    }

    internal fun intervalDays(frequency: String): Long? = when (frequency.lowercase()) {
        "daily" -> 1L
        "weekly" -> 7L
        else -> null
    }

    private fun workName(userId: String): String = "TijarioBackup:$userId"

    internal fun driveWorkName(userId: String, backupId: String) = "TijarioDriveUpload:$userId:$backupId"
    internal fun driveAccountTag(userId: String) = "TijarioDriveAccount:$userId"
}
