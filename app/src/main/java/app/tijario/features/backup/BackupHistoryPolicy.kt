package app.tijario.features.backup

import app.tijario.data.local.BackupRecordEntity
import java.util.UUID

internal const val RESTORE_SAFETY_SNAPSHOT_STATUS = "RESTORE_SAFETY_SNAPSHOT"
internal const val RESTORE_COMPLETED_STATUS = "RESTORE_COMPLETED"

internal fun latestUsableBackup(records: List<BackupRecordEntity>): BackupRecordEntity? =
    records.firstOrNull {
        it.status != RESTORE_SAFETY_SNAPSHOT_STATUS && it.localRelativePath.isNotBlank()
    }

internal fun completedBackupHistory(records: List<BackupRecordEntity>): List<BackupRecordEntity> =
    records
        .filter {
            it.status == "PHONE_SAVED" ||
                it.status == "DRIVE_UPLOADED" ||
                it.restoredAt != null
        }
        .sortedByDescending(::backupHistoryActivityAt)

internal fun backupCompletedAt(record: BackupRecordEntity): Long? = when (record.status) {
    "PHONE_SAVED" -> record.createdAt
    "DRIVE_UPLOADED" -> record.uploadedAt ?: record.createdAt
    RESTORE_COMPLETED_STATUS -> record.createdAt
    else -> null
}

internal fun backupHistoryActivityAt(record: BackupRecordEntity): Long =
    maxOf(backupCompletedAt(record) ?: Long.MIN_VALUE, record.restoredAt ?: Long.MIN_VALUE)

internal fun buildRestoreCompletionRecord(
    records: List<BackupRecordEntity>,
    userId: String,
    sourceBackupId: String?,
    archiveChecksum: String,
    archiveSize: Long,
    manifest: BackupManifest,
    driveFileId: String?,
    completedAt: Long,
): BackupRecordEntity {
    val existing = records.firstOrNull { it.userId == userId && it.id == sourceBackupId }
        ?: records.firstOrNull { it.userId == userId && it.checksum == archiveChecksum }
    if (existing != null) {
        val completedStatus = existing.status.takeIf { it == "PHONE_SAVED" || it == "DRIVE_UPLOADED" }
            ?: RESTORE_COMPLETED_STATUS
        return existing.copy(status = completedStatus, restoredAt = completedAt, lastError = null)
    }

    val deterministicId = UUID.nameUUIDFromBytes("$userId:$archiveChecksum".encodeToByteArray())
    return BackupRecordEntity(
        id = "restored-$deterministicId",
        userId = userId,
        localRelativePath = "",
        formatVersion = manifest.formatVersion,
        status = RESTORE_COMPLETED_STATUS,
        sizeBytes = archiveSize,
        checksum = archiveChecksum,
        createdAt = manifest.createdAtEpochMillis,
        uploadedAt = null,
        driveFileId = driveFileId,
        lastError = null,
        restoredAt = completedAt,
    )
}
