package app.tijario.features.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tijario.config.Supabase
import app.tijario.data.local.TijarioDatabase

open class LocalBackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString(USER_ID_KEY)?.takeIf(String::isNotBlank) ?: return Result.failure()
        val database = TijarioDatabase.getInstance(applicationContext)
        val dao = database.tijarioDao()
        val persistedSettings = dao.getBackupSettings(userId) ?: return Result.success()
        val planPolicy = BackupPlanPolicy.from(dao.getAccountEntitlement(userId))
        val settings = planPolicy.apply(persistedSettings)
        if (settings != persistedSettings) {
            dao.upsertBackupSettings(settings)
            BackupScheduler.apply(applicationContext, settings)
        }
        if (!planPolicy.automaticBackupAllowed || BackupScheduler.intervalDays(settings.frequency) == null) {
            return Result.success()
        }
        val notifier = BackupWorkNotifier(applicationContext)
        setForeground(notifier.foregroundInfo(id, "Tijario backup", "Creating encrypted phone backup", 0))

        return runCatching {
            val record = BackupCoordinator(applicationContext, database, Supabase.apiClient)
                .createLocalBackup(userId, allowNetwork = false)
            runCatching { PhoneBackupRepository(applicationContext).saveVisibleCopy(userId, record, applicationContext.filesDir) }
            BackupRetentionPruner(database, applicationContext.filesDir).prune(userId, settings)
            if (settings.driveEnabled && planPolicy.driveBackupAllowed) {
                database.tijarioDao().upsertBackupRecord(record.copy(status = "DRIVE_PENDING"))
                BackupScheduler.enqueueDriveUpload(applicationContext, settings, record.id)
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() },
        ).also { notifier.clear(id) }
    }

    companion object {
        const val USER_ID_KEY = "userId"
    }
}

@Deprecated("Kept only so already-enqueued work can finish after an app update")
class BackupWorker(context: Context, params: WorkerParameters) : LocalBackupWorker(context, params)
