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
        val settings = database.tijarioDao().getBackupSettings(userId) ?: return Result.success()
        if (BackupScheduler.intervalDays(settings.frequency) == null) return Result.success()

        return runCatching {
            val record = BackupCoordinator(applicationContext, database, Supabase.apiClient)
                .createLocalBackup(userId, allowNetwork = false)
            BackupRetentionPruner(database, applicationContext.filesDir).prune(userId, settings)
            if (settings.driveEnabled) {
                database.tijarioDao().upsertBackupRecord(record.copy(status = "DRIVE_PENDING"))
                BackupScheduler.enqueueDriveUpload(applicationContext, settings, record.id)
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() },
        )
    }

    companion object {
        const val USER_ID_KEY = "userId"
    }
}

@Deprecated("Kept only so already-enqueued work can finish after an app update")
class BackupWorker(context: Context, params: WorkerParameters) : LocalBackupWorker(context, params)
