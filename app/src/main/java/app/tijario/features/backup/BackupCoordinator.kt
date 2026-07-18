package app.tijario.features.backup

import android.content.Context
import app.tijario.BuildConfig
import app.tijario.config.AppPreferences
import app.tijario.data.local.BackupRecordEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.remote.BackendApiClient

class BackupCoordinator(
    context: Context,
    private val database: TijarioDatabase,
    backendApiClient: BackendApiClient,
) {
    private val appContext = context.applicationContext
    private val keyStore = DeviceBackupKeyStore(appContext, backendApiClient)

    suspend fun createLocalBackup(userId: String, allowNetwork: Boolean): BackupRecordEntity {
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
    ): BackupManifest {
        val installationId = AppPreferences.getInstallationId(appContext)
        val keyVersion = BackupArchiveCodec.peekKeyVersion(archive)
        val key = keyStore.resolve(userId, installationId, allowNetwork, keyVersion)
        try {
            if (key.keyVersion != keyVersion) throw BackupValidationException("Backup key version does not match archive")
            return LocalBackupRestorer(database, appContext.filesDir).restore(archive, key.keyBytes, userId)
        } finally {
            key.keyBytes.fill(0)
        }
    }
}
