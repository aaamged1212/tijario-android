package app.tijario.features.backup

import android.content.Context
import app.tijario.BuildConfig
import app.tijario.config.AppPreferences
import app.tijario.data.local.BackupRecordEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.remote.BackendApiClient
import java.io.File

class BackupCoordinator(
    context: Context,
    private val database: TijarioDatabase,
    backendApiClient: BackendApiClient,
) {
    private val appContext = context.applicationContext
    private val keyStore = DeviceBackupKeyStore(appContext, backendApiClient)

    suspend fun createLocalBackup(userId: String, allowNetwork: Boolean): BackupRecordEntity =
        BackupOperationGuard.withAccountLock(userId) {
            createLocalBackupUnlocked(userId, allowNetwork)
        }

    private suspend fun createLocalBackupUnlocked(
        userId: String,
        allowNetwork: Boolean,
        initialStatus: String = "LOCAL_READY",
    ): BackupRecordEntity {
        val installationId = AppPreferences.getInstallationId(appContext)
        val key = keyStore.resolve(userId, installationId, allowNetwork)
        try {
            LocalBackupPdfPreparer(appContext, database, appContext.filesDir).prepare(userId)
            val previous = database.tijarioDao().getBackupRecords(userId).maxOfOrNull { it.createdAt } ?: 0L
            val sequence = maxOf(System.currentTimeMillis(), previous + 1L)
            return LocalBackupCreator(database, appContext.filesDir).create(
                LocalBackupRequest(
                    userId = userId,
                    installationId = installationId,
                    sequence = sequence,
                    applicationVersion = BuildConfig.VERSION_NAME,
                    minimumApplicationVersion = BuildConfig.VERSION_NAME,
                    keyVersion = key.keyVersion,
                    encryptionKey = key.keyBytes,
                    initialStatus = initialStatus,
                ),
            )
        } finally {
            key.keyBytes.fill(0)
        }
    }

    suspend fun restoreLocalBackup(
        userId: String,
        archive: ByteArray,
        allowNetwork: Boolean,
        onStage: suspend (BackupRestoreStage) -> Unit = {},
    ): BackupManifest = BackupOperationGuard.withAccountLock(userId) {
        onStage(BackupRestoreStage.VALIDATING)
        BackupDiagnostics.stage("restore", "ARCHIVE_HEADER_VALIDATED")
        val installationId = AppPreferences.getInstallationId(appContext)
        val keyVersion = BackupArchiveCodec.peekKeyVersion(archive)
        val restorer = LocalBackupRestorer(database, appContext.filesDir)
        val decoded = keyStore.resolve(userId, installationId, allowNetwork, keyVersion).let { key ->
            try {
                if (key.keyVersion != keyVersion) throw BackupValidationException("Backup key version does not match archive")
                BackupDiagnostics.stage("restore", "KEY_RESOLVED")
                restorer.validate(archive, key.keyBytes, userId).also { BackupDiagnostics.stage("restore", "MANIFEST_VALIDATED") }
            } finally {
                key.keyBytes.fill(0)
            }
        }
        // Preserve a restorable snapshot before replacing any account rows or assets.
        onStage(BackupRestoreStage.CREATING_SAFETY_BACKUP)
        val safetyBackup = try {
            val record = createRestoreSafetyBackup(userId, allowNetwork)
            BackupDiagnostics.stage("restore", "SAFETY_BACKUP_CREATED")
            record
        } catch (error: Throwable) {
            throw BackupRestoreException(BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED, error)
        }
        try {
            restorer.restore(decoded, userId, onStage).also {
                discardRestoreSafetyBackup(safetyBackup)
            }
        } catch (error: BackupRestoreException) {
            throw error
        } catch (error: BackupValidationException) {
            throw restoreFailureFor(error)
        }
    }

    suspend fun restoreLocalBackup(
        userId: String,
        archiveFile: File,
        allowNetwork: Boolean,
        onStage: suspend (BackupRestoreStage) -> Unit = {},
    ): BackupManifest = BackupOperationGuard.withAccountLock(userId) {
        onStage(BackupRestoreStage.VALIDATING)
        BackupDiagnostics.stage("restore", "ARCHIVE_HEADER_VALIDATED")
        val installationId = AppPreferences.getInstallationId(appContext)
        val keyVersion = BackupArchiveCodec.peekKeyVersion(archiveFile)
        val restorer = LocalBackupRestorer(database, appContext.filesDir)
        val decoded = keyStore.resolve(userId, installationId, allowNetwork, keyVersion).let { key ->
            try {
                if (key.keyVersion != keyVersion) throw BackupValidationException("Backup key version does not match archive")
                BackupDiagnostics.stage("restore", "KEY_RESOLVED")
                restorer.validate(archiveFile, key.keyBytes, userId, File(appContext.cacheDir, "backup-restore/$userId"))
                    .also { BackupDiagnostics.stage("restore", "MANIFEST_VALIDATED") }
            } finally {
                key.keyBytes.fill(0)
            }
        }
        try {
            // Do not announce or create a safety snapshot until the archive is validated.
            onStage(BackupRestoreStage.CREATING_SAFETY_BACKUP)
            val safetyBackup = try {
                val record = createRestoreSafetyBackup(userId, allowNetwork)
                BackupDiagnostics.stage("restore", "SAFETY_BACKUP_CREATED")
                record
            } catch (error: Throwable) {
                throw BackupRestoreException(BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED, error)
            }
            try {
                restorer.restore(decoded, userId, onStage).also {
                    discardRestoreSafetyBackup(safetyBackup)
                }
            } catch (error: BackupRestoreException) {
                throw error
            } catch (error: BackupValidationException) {
                throw restoreFailureFor(error)
            }
        } finally {
            decoded.discardStaging()
        }
    }

    private suspend fun createRestoreSafetyBackup(userId: String, allowNetwork: Boolean): BackupRecordEntity =
        createLocalBackupUnlocked(userId, allowNetwork, RESTORE_SAFETY_SNAPSHOT_STATUS)

    private suspend fun discardRestoreSafetyBackup(record: BackupRecordEntity) {
        try {
            resolveBackupArchiveFile(appContext.filesDir, record.userId, record.localRelativePath)?.delete()
            database.tijarioDao().deleteBackupRecord(record.userId, record.id)
            BackupDiagnostics.stage("restore", "SAFETY_BACKUP_REMOVED")
        } catch (error: Exception) {
            // A hidden safety snapshot may remain for recovery, but restore success must not be downgraded.
            BackupDiagnostics.stage("restore", "SAFETY_BACKUP_CLEANUP_FAILED", error = error)
        }
    }

}
