package app.tijario.features.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.tijario.data.local.TijarioDatabase
import app.tijario.features.backup.drive.DriveBackupException
import app.tijario.features.backup.drive.DriveBackupRepository
import app.tijario.features.backup.drive.DriveBackupRuntime
import app.tijario.features.backup.drive.BackupDriveContainer

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
        BackupDiagnostics.stage("upload", "AUTHORIZATION_CHECK")
        if (BackupDriveContainer.authorizationState(applicationContext, userId) !is app.tijario.features.backup.drive.DriveConnectionState.Connected) {
            dao.upsertBackupRecord(record.copy(status = "DRIVE_REAUTH_REQUIRED", lastError = "drive_reauthorization_required"))
            return Result.failure()
        }
        val notifier = BackupWorkNotifier(applicationContext)
        setForeground(notifier.foregroundInfo(id, "Uploading to Google Drive", "Uploading backup - 0%", 0))
        dao.upsertBackupRecord(record.copy(status = "DRIVE_UPLOADING", lastError = null))

        return try {
            val repository = DriveBackupRepository(database, applicationContext.filesDir, DriveBackupRuntime.client(applicationContext, userId))
            repository.upload(userId, backupId) { transferred, total ->
                val percent = percent(transferred, total)
                setProgress(workDataOf(PROGRESS_PERCENT_KEY to percent, PROGRESS_STAGE_KEY to "backup_drive_uploading"))
                setForeground(
                    notifier.foregroundInfo(
                        id,
                        "Uploading to Google Drive",
                        "Uploading backup - $percent%",
                        percent,
                    ),
                )
            }
            setProgress(workDataOf(PROGRESS_PERCENT_KEY to 100, PROGRESS_STAGE_KEY to "backup_drive_uploaded"))
            BackupDiagnostics.stage("upload", "REMOTE_UPLOAD_VERIFIED")
            dao.getBackupSettings(userId)?.let { settings ->
                runCatching { BackupScheduler.enqueueDriveRetention(applicationContext, settings) }
                    .onFailure { BackupDiagnostics.stage("upload", "RETENTION_FAILED", error = it) }
            }
            notifier.post("Upload completed", "Backup uploaded to Google Drive")
            BackupDiagnostics.stage("upload", "WORK_SUCCEEDED")
            Result.success()
        } catch (error: DriveBackupException.Retryable) {
            val terminal = runAttemptCount + 1 >= MAX_ATTEMPTS
            updateFailureRecord(dao, record, if (terminal) "DRIVE_FAILED" else "DRIVE_PENDING", if (terminal) "drive_retry_exhausted" else "drive_retryable")
            notifier.post("Upload failed", "Could not upload backup")
            if (terminal) Result.failure() else Result.retry()
        } catch (error: DriveBackupException) {
            updateFailureRecord(dao, record, "DRIVE_FAILED", safeCode(error))
            notifier.post("Upload failed", "Could not upload backup")
            Result.failure()
        } catch (_: Exception) {
            val terminal = runAttemptCount + 1 >= MAX_ATTEMPTS
            updateFailureRecord(dao, record, if (terminal) "DRIVE_FAILED" else "DRIVE_PENDING", if (terminal) "drive_retry_exhausted" else "drive_retryable")
            notifier.post("Upload failed", "Could not upload backup")
            if (terminal) Result.failure() else Result.retry()
        } finally {
            notifier.clear(id)
        }
    }

    private fun safeCode(error: DriveBackupException): String = when (error) {
        is DriveBackupException.NotConfigured -> "drive_not_configured"
        is DriveBackupException.NotConnected -> "drive_not_connected"
        is DriveBackupException.AccountMismatch -> "drive_account_mismatch"
        is DriveBackupException.IntegrityFailure -> "drive_integrity_failed"
        is DriveBackupException.ReauthorizationRequired -> "drive_reauthorization_required"
        is DriveBackupException.PermissionDenied -> "drive_permission_denied"
        is DriveBackupException.InvalidRequest -> "drive_invalid_request"
        is DriveBackupException.Permanent -> "drive_upload_failed"
        is DriveBackupException.Retryable -> "drive_retryable"
    }

    private suspend fun updateFailureRecord(
        dao: app.tijario.data.local.TijarioDao,
        initialRecord: app.tijario.data.local.BackupRecordEntity,
        status: String,
        errorCode: String,
    ) {
        val latest = dao.getBackupRecords(initialRecord.userId).firstOrNull { it.id == initialRecord.id } ?: initialRecord
        val failure = failureRecord(latest, status, errorCode)
        if (failure == null) {
            BackupDiagnostics.stage("upload", "RETENTION_FAILED")
            return
        }
        dao.upsertBackupRecord(failure)
        BackupDiagnostics.stage("upload", "WORK_FAILED")
    }

    companion object {
        const val USER_ID_KEY = "userId"
        const val BACKUP_ID_KEY = "backupId"
        const val MAX_ATTEMPTS = 3
        const val PROGRESS_PERCENT_KEY = "progressPercent"
        const val PROGRESS_STAGE_KEY = "progressStage"

        internal fun percent(transferred: Long, total: Long): Int =
            if (total <= 0L) 0 else ((transferred * 100L) / total).toInt().coerceIn(0, 100)

        internal fun isVerifiedUpload(status: String, driveFileId: String?): Boolean =
            status == "DRIVE_UPLOADED" && !driveFileId.isNullOrBlank()

        internal fun failureRecord(
            current: app.tijario.data.local.BackupRecordEntity,
            failureStatus: String,
            errorCode: String,
        ): app.tijario.data.local.BackupRecordEntity? =
            if (isVerifiedUpload(current.status, current.driveFileId)) null else current.copy(status = failureStatus, lastError = errorCode)
    }
}
