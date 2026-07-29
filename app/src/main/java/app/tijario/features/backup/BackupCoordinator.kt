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

    private suspend fun createLocalBackupUnlocked(userId: String, allowNetwork: Boolean): BackupRecordEntity {
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
    ): BackupManifest = BackupOperationGuard.withAccountLock(userId) {
        val installationId = AppPreferences.getInstallationId(appContext)
        val keyVersion = BackupArchiveCodec.peekKeyVersion(archive)
        val restorer = LocalBackupRestorer(database, appContext.filesDir)
        val decoded = keyStore.resolve(userId, installationId, allowNetwork, keyVersion).let { key ->
            try {
                if (key.keyVersion != keyVersion) throw BackupValidationException("Backup key version does not match archive")
                restorer.validate(archive, key.keyBytes, userId)
            } finally {
                key.keyBytes.fill(0)
            }
        }
        // Preserve a restorable snapshot before replacing any account rows or assets.
        try {
            createLocalBackupUnlocked(userId, allowNetwork)
        } catch (error: Throwable) {
            throw BackupRestoreException(BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED, error)
        }
        try {
            restorer.restore(decoded, userId)
        } catch (error: BackupValidationException) {
            throw BackupRestoreException(BackupRestoreException.Code.BACKUP_RESTORE_TRANSACTION_FAILED, error)
        }
    }

    suspend fun restoreLocalBackup(
        userId: String,
        archiveFile: File,
        allowNetwork: Boolean,
    ): BackupManifest = BackupOperationGuard.withAccountLock(userId) {
        val installationId = AppPreferences.getInstallationId(appContext)
        val keyVersion = BackupArchiveCodec.peekKeyVersion(archiveFile)
        val restorer = LocalBackupRestorer(database, appContext.filesDir)
        val decoded = keyStore.resolve(userId, installationId, allowNetwork, keyVersion).let { key ->
            try {
                if (key.keyVersion != keyVersion) throw BackupValidationException("Backup key version does not match archive")
                restorer.validate(archiveFile, key.keyBytes, userId, File(appContext.cacheDir, "backup-restore/$userId"))
            } finally {
                key.keyBytes.fill(0)
            }
        }
        try {
            try {
                createLocalBackupUnlocked(userId, allowNetwork)
            } catch (error: Throwable) {
                throw BackupRestoreException(BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED, error)
            }
            try {
                restorer.restore(decoded, userId)
            } catch (error: BackupValidationException) {
                throw BackupRestoreException(BackupRestoreException.Code.BACKUP_RESTORE_TRANSACTION_FAILED, error)
            }
        } finally {
            decoded.discardStaging()
        }
    }
}
