package app.tijario.features.backup

import app.tijario.data.local.BackupFileEntryEntity
import app.tijario.data.local.BackupRecordEntity
import app.tijario.data.local.TIJARIO_DATABASE_VERSION
import app.tijario.data.local.TijarioDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

data class LocalBackupRequest(
    val userId: String,
    val installationId: String,
    val sequence: Long,
    val applicationVersion: String,
    val minimumApplicationVersion: String,
    val keyVersion: Int,
    val encryptionKey: ByteArray,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val initialStatus: String = "LOCAL_READY",
)

class LocalBackupCreator(
    private val database: TijarioDatabase,
    private val filesRoot: File,
) {
    suspend fun create(request: LocalBackupRequest): BackupRecordEntity = withContext(Dispatchers.IO) {
        require(request.userId.isNotBlank()) { "Backup account is required" }
        require(request.installationId.isNotBlank()) { "Backup installation is required" }
        val logicalEntries = RoomLogicalBackupStore(database).exportAccount(request.userId)
        val assets = BackupAssetCollector(filesRoot).collect(request.userId, logicalEntries)
        val allEntries = logicalEntries + assets.entries
        val snapshots = logicalEntries.mapValues { LogicalBackupSnapshotCodec.decode(it.value) }
        LogicalBackupValidator.validate(snapshots.values)
        val documentCount = snapshots.getValue("data/documents.json").rows.size
        val creationEventCount = snapshots.getValue("data/document-creation-events.json").rows.size
        val manifest = BackupManifest(
            formatVersion = 1,
            roomDatabaseVersion = TIJARIO_DATABASE_VERSION,
            applicationVersion = request.applicationVersion,
            minimumApplicationVersion = request.minimumApplicationVersion,
            accountId = request.userId,
            installationId = request.installationId,
            backupSequence = request.sequence,
            createdAtEpochMillis = request.createdAtEpochMillis,
            encryptionVersion = 1,
            keyVersion = request.keyVersion,
            recordCounts = snapshots.mapValues { it.value.rows.size }.toSortedMap(),
            documentCount = documentCount,
            documentCreationEventCount = creationEventCount,
            pdfIncludedCount = assets.pdfIncludedCount,
            pdfMissingCount = assets.pdfMissingCount,
            pdfFailedGenerationCount = assets.pdfFailedGenerationCount,
        )
        val archive = BackupArchiveCodec.create(manifest, allEntries, request.encryptionKey)
        val backupId = UUID.randomUUID().toString()
        val fileName = buildFileName(request.createdAtEpochMillis, backupId)
        val stored = AtomicBackupFileStore.writeVerified(
            directory = File(filesRoot, "users/${request.userId}/backups"),
            fileName = fileName,
            archive = archive,
        ) { persisted ->
            BackupArchiveCodec.open(persisted, request.encryptionKey, request.userId)
        }
        val relativePath = stored.file.relativeTo(filesRoot).invariantSeparatorsPath
        val record = BackupRecordEntity(
            id = backupId,
            userId = request.userId,
            localRelativePath = relativePath,
            formatVersion = 1,
            status = request.initialStatus,
            sizeBytes = stored.sizeBytes,
            checksum = stored.checksum,
            createdAt = request.createdAtEpochMillis,
            uploadedAt = null,
            driveFileId = null,
            lastError = null,
            restoredAt = null,
        )
        database.withTransaction {
            database.tijarioDao().upsertBackupRecord(record)
            database.tijarioDao().upsertBackupFileEntries(
                allEntries.toSortedMap().map { (path, bytes) ->
                    BackupFileEntryEntity(
                        id = "$backupId:$path",
                        backupId = backupId,
                        relativePath = path,
                        sizeBytes = bytes.size.toLong(),
                        checksum = sha256(bytes),
                        status = "INCLUDED",
                    )
                },
            )
        }
        record
    }

    private fun buildFileName(createdAt: Long, backupId: String): String {
        val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(createdAt))
        return "Tijario-Backup-$stamp-${backupId.take(8)}.tijario"
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
