package app.tijario.features.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tijario.data.local.TijarioDatabase
import app.tijario.features.backup.drive.DriveBackupException
import app.tijario.features.backup.drive.DriveBackupRepository
import app.tijario.features.backup.drive.DriveBackupRuntime

class DriveUploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString(USER_ID_KEY)?.takeIf(String::isNotBlank) ?: return Result.failure()
        val backupId = inputData.getString(BACKUP_ID_KEY)?.takeIf(String::isNotBlank) ?: return Result.failure()
        val database = TijarioDatabase.getInstance(applicationContext)
        val dao = database.tijarioDao()
        val record = dao.getBackupRecords(userId).firstOrNull { it.id == backupId } ?: return Result.failure()
        dao.upsertBackupRecord(record.copy(status = "DRIVE_UPLOADING", lastError = null))

        return try {
            DriveBackupRepository(database, applicationContext.filesDir, DriveBackupRuntime.client)
                .upload(userId, backupId)
            Result.success()
        } catch (error: DriveBackupException.Retryable) {
            val terminal = runAttemptCount + 1 >= MAX_ATTEMPTS
            dao.upsertBackupRecord(
                record.copy(
                    status = if (terminal) "DRIVE_FAILED" else "DRIVE_PENDING",
                    lastError = if (terminal) "drive_retry_exhausted" else "drive_retryable",
                ),
            )
            if (terminal) Result.failure() else Result.retry()
        } catch (error: DriveBackupException) {
            dao.upsertBackupRecord(record.copy(status = "DRIVE_FAILED", lastError = safeCode(error)))
            Result.failure()
        } catch (_: Exception) {
            val terminal = runAttemptCount + 1 >= MAX_ATTEMPTS
            dao.upsertBackupRecord(
                record.copy(
                    status = if (terminal) "DRIVE_FAILED" else "DRIVE_PENDING",
                    lastError = if (terminal) "drive_retry_exhausted" else "drive_retryable",
                ),
            )
            if (terminal) Result.failure() else Result.retry()
        }
    }

    private fun safeCode(error: DriveBackupException): String = when (error) {
        is DriveBackupException.NotConfigured -> "drive_not_configured"
        is DriveBackupException.NotConnected -> "drive_not_connected"
        is DriveBackupException.AccountMismatch -> "drive_account_mismatch"
        is DriveBackupException.IntegrityFailure -> "drive_integrity_failed"
        is DriveBackupException.Permanent -> "drive_upload_failed"
        is DriveBackupException.Retryable -> "drive_retryable"
    }

    companion object {
        const val USER_ID_KEY = "userId"
        const val BACKUP_ID_KEY = "backupId"
        const val MAX_ATTEMPTS = 3
    }
}
