package app.tijario.features.backup

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.tijario.config.Supabase
import app.tijario.data.local.TijarioDatabase
import app.tijario.features.backup.drive.DriveBackupFile
import app.tijario.features.backup.drive.DriveBackupRepository
import app.tijario.features.backup.drive.DriveBackupRuntime
import java.io.File
import java.security.MessageDigest

/** Runs all destructive restore work off the UI thread and exposes one cancellable WorkInfo. */
class BackupRestoreWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString(USER_ID_KEY)?.takeIf(String::isNotBlank) ?: return Result.failure()
        val source = inputData.getString(SOURCE_KEY) ?: return Result.failure()
        val database = TijarioDatabase.getInstance(applicationContext)
        val coordinator = BackupCoordinator(applicationContext, database, Supabase.apiClient)
        val notifier = BackupWorkNotifier(applicationContext)
        var stagedFile: File? = null
        var stagedInput: BackupArchiveInputStager.StagedArchive? = null
        var releaseFileUriPermission = false
        var sourceBackupId: String? = null
        var sourceDriveFileId: String? = null
        return try {
            val archive = when (source) {
                SOURCE_DRIVE -> {
                    val remote = remoteFromInput() ?: return Result.failure()
                    sourceBackupId = remote.backupId
                    sourceDriveFileId = remote.id
                    stage(notifier, "backup_downloading", "Downloading from Google Drive", 0)
                    File(applicationContext.cacheDir, "backup-restore/${id}.tijario").also { destination ->
                        stagedFile = destination
                        DriveBackupRepository(database, applicationContext.filesDir, DriveBackupRuntime.client(applicationContext, userId))
                            .download(userId, remote, destination) { transferred, total ->
                                stage(notifier, "backup_downloading", "Downloading from Google Drive", percent(transferred, total))
                            }
                    }
                }
                SOURCE_FILE_URI -> {
                    val uri = inputData.getString(FILE_URI_KEY)?.let(Uri::parse) ?: return Result.failure()
                    stage(notifier, "backup_validating", "Validating encrypted backup")
                    val input = try {
                        applicationContext.contentResolver.openInputStream(uri)
                    } catch (error: SecurityException) {
                        throw BackupRestoreException(BackupRestoreException.Code.RESTORE_FILE_PERMISSION_LOST, error)
                    } ?: throw BackupRestoreException(BackupRestoreException.Code.RESTORE_FILE_PERMISSION_LOST)
                    input.use {
                        BackupArchiveInputStager.copyToPrivateFile(input, applicationContext.filesDir, userId)
                            .also { stagedInput = it }
                            .file
                    }.also { releaseFileUriPermission = true }
                }
                SOURCE_LOCAL_RECORD -> {
                    val backupId = inputData.getString(BACKUP_ID_KEY) ?: return Result.failure()
                    sourceBackupId = backupId
                    val record = database.tijarioDao().getBackupRecords(userId).firstOrNull { it.id == backupId }
                        ?: return Result.failure()
                    resolveBackupArchiveFile(applicationContext.filesDir, userId, record.localRelativePath)
                        ?: return Result.failure()
                }
                else -> return Result.failure()
            }
            stagedFile = archive
            BackupDiagnostics.stage("restore", "SOURCE_STAGED")
            val manifest = coordinator.restoreLocalBackup(
                userId = userId,
                archiveFile = archive,
                // A missing exact-version key may be fetched after any trusted restore source.
                allowNetwork = true,
            ) { restoreStage ->
                when (restoreStage) {
                    BackupRestoreStage.VALIDATING -> stage(notifier, restoreStage.progressKey, "Validating encrypted backup")
                    BackupRestoreStage.CREATING_SAFETY_BACKUP -> stage(notifier, restoreStage.progressKey, "Creating safety backup")
                    BackupRestoreStage.RESTORING_RECORDS -> stage(notifier, restoreStage.progressKey, "Restoring customers and documents")
                    BackupRestoreStage.RESTORING_FILES -> stage(notifier, restoreStage.progressKey, "Restoring files and images")
                }
            }
            try {
                val record = buildRestoreCompletionRecord(
                    records = database.tijarioDao().getBackupRecords(userId),
                    userId = userId,
                    sourceBackupId = sourceBackupId,
                    archiveChecksum = sha256(archive),
                    archiveSize = archive.length(),
                    manifest = manifest,
                    driveFileId = sourceDriveFileId,
                    completedAt = System.currentTimeMillis(),
                )
                database.tijarioDao().upsertBackupRecord(record)
                BackupDiagnostics.stage("restore", "HISTORY_RECORDED")
            } catch (error: Exception) {
                // Restored business data is already committed; history failure must not trigger a second restore.
                BackupDiagnostics.stage("restore", "HISTORY_RECORD_FAILED", error = error)
            }
            setProgress(workDataOf(PROGRESS_STAGE_KEY to "backup_restored_success", PROGRESS_PERCENT_KEY to 100))
            notifier.post("Restore completed", "Backup restore completed")
            Result.success()
        } catch (error: Throwable) {
            val mapped = restoreFailureFor(error)
            BackupDiagnostics.stage("restore", "WORK_FAILED_${mapped.code.name}", error = error)
            setProgress(workDataOf(ERROR_CODE_KEY to mapped.code.name, PROGRESS_STAGE_KEY to "backup_restore_failed"))
            notifier.post("Restore failed", "Backup restore failed")
            Result.failure(workDataOf(ERROR_CODE_KEY to mapped.code.name))
        } finally {
            if (releaseFileUriPermission && source == SOURCE_FILE_URI) {
                inputData.getString(FILE_URI_KEY)?.let(Uri::parse)?.let { uri ->
                    runCatching {
                        applicationContext.contentResolver.releasePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                }
            }
            if (stagedInput != null) {
                stagedInput?.delete()
            } else if (source != SOURCE_LOCAL_RECORD) {
                stagedFile?.delete()
            }
            notifier.clear(id)
        }
    }

    private suspend fun stage(notifier: BackupWorkNotifier, key: String, detail: String, percent: Int? = null) {
        BackupDiagnostics.stage("restore", key)
        setProgress(
            workDataOf(
                PROGRESS_STAGE_KEY to key,
                PROGRESS_PERCENT_KEY to (percent ?: -1),
            ),
        )
        setForeground(notifier.foregroundInfo(id, "Tijario backup", detail, percent ?: 0))
    }

    private fun remoteFromInput(): DriveBackupFile? {
        val id = inputData.getString(REMOTE_ID_KEY)?.takeIf(String::isNotBlank) ?: return null
        val checksum = inputData.getString(REMOTE_CHECKSUM_KEY)?.takeIf(String::isNotBlank) ?: return null
        val accountId = inputData.getString(REMOTE_ACCOUNT_KEY)?.takeIf(String::isNotBlank) ?: return null
        val backupId = inputData.getString(REMOTE_BACKUP_ID_KEY)?.takeIf(String::isNotBlank) ?: return null
        val size = inputData.getLong(REMOTE_SIZE_KEY, -1L).takeIf { it >= 0L } ?: return null
        return DriveBackupFile(
            id = id,
            name = inputData.getString(REMOTE_NAME_KEY).orEmpty(),
            sizeBytes = size,
            checksum = checksum,
            accountId = accountId,
            backupId = backupId,
            createdAt = inputData.getLong(REMOTE_CREATED_AT_KEY, 0L),
        )
    }

    private fun percent(transferred: Long, total: Long): Int =
        if (total <= 0L) 0 else ((transferred * 100L) / total).toInt().coerceIn(0, 100)

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val USER_ID_KEY = "userId"
        const val SOURCE_KEY = "source"
        const val SOURCE_DRIVE = "drive"
        const val SOURCE_FILE_URI = "file_uri"
        const val SOURCE_LOCAL_RECORD = "local_record"
        const val BACKUP_ID_KEY = "backupId"
        const val FILE_URI_KEY = "fileUri"
        const val REMOTE_ID_KEY = "remoteId"
        const val REMOTE_NAME_KEY = "remoteName"
        const val REMOTE_SIZE_KEY = "remoteSize"
        const val REMOTE_CHECKSUM_KEY = "remoteChecksum"
        const val REMOTE_ACCOUNT_KEY = "remoteAccount"
        const val REMOTE_BACKUP_ID_KEY = "remoteBackupId"
        const val REMOTE_CREATED_AT_KEY = "remoteCreatedAt"
        const val PROGRESS_STAGE_KEY = "progressStage"
        const val PROGRESS_PERCENT_KEY = "progressPercent"
        const val ERROR_CODE_KEY = "errorCode"
    }
}
