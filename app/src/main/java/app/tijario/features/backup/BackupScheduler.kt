package app.tijario.features.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.tijario.data.local.BackupSettingsEntity
import java.util.concurrent.TimeUnit
import java.util.UUID

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

    fun enqueueDriveUpload(context: Context, settings: BackupSettingsEntity, backupId: String, userInitiated: Boolean = false): UUID? {
        if (backupId.isBlank() || (!settings.driveEnabled && !userInitiated)) return null
        val networkType = if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .apply {
                if (!userInitiated) {
                    setRequiresCharging(settings.chargingOnly)
                    setRequiresBatteryNotLow(true)
                    setRequiresStorageNotLow(true)
                }
            }
            .build()
        val request = OneTimeWorkRequestBuilder<DriveUploadWorker>()
            .addTag(driveAccountTag(settings.userId))
            .setInputData(
                workDataOf(
                    DriveUploadWorker.USER_ID_KEY to settings.userId,
                    DriveUploadWorker.BACKUP_ID_KEY to backupId,
                ),
            )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .apply {
                if (userInitiated) {
                    setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                }
            }
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            driveWorkName(settings.userId, backupId),
            if (userInitiated) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
        return request.id
    }

    fun enqueueDriveRetention(context: Context, settings: BackupSettingsEntity): UUID? {
        if (!settings.driveEnabled) return null
        val networkType = if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val request = OneTimeWorkRequestBuilder<DriveRetentionWorker>()
            .addTag(driveAccountTag(settings.userId))
            .setInputData(workDataOf(DriveRetentionWorker.USER_ID_KEY to settings.userId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType).setRequiresBatteryNotLow(true).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            driveRetentionWorkName(settings.userId),
            ExistingWorkPolicy.KEEP,
            request,
        )
        return request.id
    }

    fun cancelAccountWork(context: Context, userId: String) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(workName(userId))
        workManager.cancelAllWorkByTag(driveAccountTag(userId))
    }

    fun cancelDriveUploads(context: Context, userId: String) {
        WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(driveAccountTag(userId))
    }

    fun enqueueDriveRestore(context: Context, userId: String, remote: app.tijario.features.backup.drive.DriveBackupFile): UUID? =
        enqueueRestore(
            context = context,
            userId = userId,
            source = BackupRestoreWorker.SOURCE_DRIVE,
            extras = workDataOf(
                BackupRestoreWorker.REMOTE_ID_KEY to remote.id,
                BackupRestoreWorker.REMOTE_NAME_KEY to remote.name,
                BackupRestoreWorker.REMOTE_SIZE_KEY to remote.sizeBytes,
                BackupRestoreWorker.REMOTE_CHECKSUM_KEY to remote.checksum,
                BackupRestoreWorker.REMOTE_ACCOUNT_KEY to remote.accountId,
                BackupRestoreWorker.REMOTE_BACKUP_ID_KEY to remote.backupId,
                BackupRestoreWorker.REMOTE_CREATED_AT_KEY to remote.createdAt,
            ),
            requiresNetwork = true,
        )

    fun enqueueFileRestore(context: Context, userId: String, uri: String): UUID? =
        enqueueRestore(context, userId, BackupRestoreWorker.SOURCE_FILE_URI, workDataOf(BackupRestoreWorker.FILE_URI_KEY to uri), false)

    fun enqueueLocalRestore(context: Context, userId: String, backupId: String): UUID? =
        enqueueRestore(context, userId, BackupRestoreWorker.SOURCE_LOCAL_RECORD, workDataOf(BackupRestoreWorker.BACKUP_ID_KEY to backupId), false)

    private fun enqueueRestore(
        context: Context,
        userId: String,
        source: String,
        extras: androidx.work.Data,
        requiresNetwork: Boolean,
    ): UUID? {
        if (userId.isBlank()) return null
        val request = OneTimeWorkRequestBuilder<BackupRestoreWorker>()
            .addTag(restoreAccountTag(userId))
            .setInputData(
                androidx.work.Data.Builder()
                    .putAll(extras)
                    .putString(BackupRestoreWorker.USER_ID_KEY, userId)
                    .putString(BackupRestoreWorker.SOURCE_KEY, source)
                    .build(),
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(if (requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context.applicationContext).enqueue(request)
        return request.id
    }

    internal fun intervalDays(frequency: String): Long? = when (frequency.lowercase()) {
        "daily" -> 1L
        "weekly" -> 7L
        else -> null
    }

    internal fun driveRetentionCount(settings: BackupSettingsEntity): Int = when (settings.frequency.lowercase()) {
        "daily" -> settings.retentionDaily
        "weekly" -> settings.retentionWeekly
        else -> settings.retentionMonthly
    }.coerceAtLeast(1)

    private fun workName(userId: String): String = "TijarioBackup:$userId"

    internal fun driveWorkName(userId: String, backupId: String) = "TijarioDriveUpload:$userId:$backupId"
    internal fun driveRetentionWorkName(userId: String) = "TijarioDriveRetention:$userId"
    internal fun driveAccountTag(userId: String) = "TijarioDriveAccount:$userId"
    internal fun restoreAccountTag(userId: String) = "TijarioRestoreAccount:$userId"
}
