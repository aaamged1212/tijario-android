package app.tijario.features.backup

import app.tijario.data.local.TijarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalBackupRestorer(
    private val database: TijarioDatabase,
    private val filesRoot: File,
    private val currentRoomVersion: Int = 18,
) {
    suspend fun validate(
        archive: ByteArray,
        encryptionKey: ByteArray,
        expectedUserId: String,
    ): DecodedBackup = withContext(Dispatchers.IO) {
        val decoded = BackupArchiveCodec.open(archive, encryptionKey, expectedUserId)
        BackupDiagnostics.stage("restore", "ARCHIVE_DECRYPTED")
        if (decoded.manifest.roomDatabaseVersion > currentRoomVersion) {
            throw BackupValidationException("Backup requires a newer application version")
        }
        validateManifestCounts(decoded)
        decoded
    }

    suspend fun validate(
        archiveFile: File,
        encryptionKey: ByteArray,
        expectedUserId: String,
        temporaryRoot: File,
    ): DecodedBackup = withContext(Dispatchers.IO) {
        val decoded = BackupArchiveCodec.open(archiveFile, encryptionKey, expectedUserId, temporaryRoot)
        BackupDiagnostics.stage("restore", "ARCHIVE_DECRYPTED")
        try {
            if (decoded.manifest.roomDatabaseVersion > currentRoomVersion) {
                throw BackupValidationException("Backup requires a newer application version")
            }
            validateManifestCounts(decoded)
            decoded
        } catch (error: Exception) {
            decoded.discardStaging()
            throw error
        }
    }

    suspend fun restore(
        decoded: DecodedBackup,
        expectedUserId: String,
        onStage: suspend (BackupRestoreStage) -> Unit = {},
    ): BackupManifest = withContext(Dispatchers.IO) {
        if (decoded.manifest.accountId != expectedUserId) {
            throw BackupValidationException("Backup belongs to a different account")
        }
        BackupDiagnostics.stage("restore", "ACCOUNT_VALIDATED")
        onStage(BackupRestoreStage.RESTORING_FILES)
        val assets = try {
            BackupDiagnostics.stage("restore", "ASSETS_STAGED")
            BackupAssetRestorer(filesRoot).stage(expectedUserId, decoded.entries, decoded.stagedAssetFiles)
        } catch (error: Exception) {
            BackupDiagnostics.stage("restore", "RESTORE_ASSET_STAGE_FAILED", error = error)
            throw BackupRestoreException(BackupRestoreException.Code.RESTORE_ASSET_STAGE_FAILED, error)
        }
        var appliedAssets: AppliedBackupAssets? = null
        try {
            onStage(BackupRestoreStage.RESTORING_RECORDS)
            RoomLogicalBackupStore(database).restoreAccount(expectedUserId, decoded.entries) {
                try {
                    BackupDiagnostics.stage("restore", "ASSETS_APPLIED")
                    appliedAssets = assets.apply()
                } catch (error: Exception) {
                    BackupDiagnostics.stage("restore", "RESTORE_ASSET_APPLY_FAILED", error = error)
                    throw BackupRestoreException(BackupRestoreException.Code.RESTORE_ASSET_APPLY_FAILED, error)
                }
            }
            appliedAssets?.complete()
            BackupDiagnostics.stage("restore", "RESTORE_COMPLETED")
            decoded.manifest
        } catch (error: Exception) {
            if (appliedAssets == null) assets.discard()
            val rollbackFailure = runCatching { appliedAssets?.rollback() }.exceptionOrNull()
            if (rollbackFailure != null) {
                BackupDiagnostics.stage("restore", "ROLLBACK_FAILED", error = rollbackFailure)
                throw BackupRestoreException(BackupRestoreException.Code.RESTORE_ROLLBACK_FAILED, rollbackFailure)
            }
            BackupDiagnostics.stage("restore", "ROLLBACK_COMPLETED", error = error)
            if (error is BackupRestoreException) throw error
            if (error is BackupValidationException) throw error
            throw BackupRestoreException(BackupRestoreException.Code.BACKUP_RESTORE_TRANSACTION_FAILED, error)
        }
    }

    private fun validateManifestCounts(decoded: DecodedBackup) {
        val snapshots = RoomLogicalBackupStore.tableSpecs.associate { spec ->
            val snapshot = LogicalBackupSnapshotCodec.decode(
                decoded.entries[spec.archivePath]
                    ?: throw BackupValidationException("Backup table is missing: ${spec.table}"),
            )
            spec.archivePath to snapshot
        }
        snapshots.forEach { (path, snapshot) ->
            if (decoded.manifest.recordCounts[path] != snapshot.rows.size) {
                throw BackupValidationException("Backup manifest record counts are invalid")
            }
        }
        if (decoded.manifest.documentCount != snapshots.getValue("data/documents.json").rows.size ||
            decoded.manifest.documentCreationEventCount != snapshots.getValue("data/document-creation-events.json").rows.size
        ) {
            throw BackupValidationException("Backup manifest totals are invalid")
        }
    }
}
