package app.tijario.features.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tijario.data.local.TijarioDatabase
import app.tijario.features.backup.drive.DriveBackupRepository
import app.tijario.features.backup.drive.DriveBackupRuntime

/** Retention is independent best-effort work and cannot change upload success. */
class DriveRetentionWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString(USER_ID_KEY)?.takeIf(String::isNotBlank) ?: return Result.failure()
        val database = TijarioDatabase.getInstance(applicationContext)
        val settings = database.tijarioDao().getBackupSettings(userId) ?: return Result.success()
        return runCatching {
            BackupDiagnostics.stage("upload", "RETENTION_STARTED")
            DriveBackupRepository(database, applicationContext.filesDir, DriveBackupRuntime.client(applicationContext, userId))
                .prune(userId, BackupScheduler.driveRetentionCount(settings))
            Result.success()
        }.getOrElse { error ->
            BackupDiagnostics.stage("upload", "RETENTION_FAILED", error = error)
            if (runAttemptCount + 1 >= MAX_ATTEMPTS) Result.failure() else Result.retry()
        }
    }

    companion object {
        const val USER_ID_KEY = "userId"
        private const val MAX_ATTEMPTS = 3
    }
}
