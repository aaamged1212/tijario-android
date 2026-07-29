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
    ): BackupManifest = withContext(Dispatchers.IO) {
        if (decoded.manifest.accountId != expectedUserId) {
            throw BackupValidationException("Backup belongs to a different account")
        }
        val assets = BackupAssetRestorer(filesRoot).stage(expectedUserId, decoded.entries, decoded.stagedAssetFiles)
        val appliedAssets = assets.apply()
        try {
            RoomLogicalBackupStore(database).restoreAccount(expectedUserId, decoded.entries)
            appliedAssets.complete()
            decoded.manifest
        } catch (error: Exception) {
            appliedAssets.rollback()
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup restore failed", error)
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
