package app.tijario.features.backup

import app.tijario.data.local.BackupRecordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupHistoryPolicyTest {
    @Test
    fun historyContainsOnlyCompletedBackupAndRestoreOperations() {
        val records = listOf(
            record("local", "LOCAL_READY"),
            record("pending", "DRIVE_PENDING"),
            record("uploading", "DRIVE_UPLOADING"),
            record("failed", "DRIVE_FAILED"),
            record("safety", RESTORE_SAFETY_SNAPSHOT_STATUS),
            record("phone", "PHONE_SAVED", createdAt = 20),
            record("drive", "DRIVE_UPLOADED", createdAt = 10, uploadedAt = 30),
            record("restored", RESTORE_COMPLETED_STATUS, createdAt = 5, restoredAt = 40, path = ""),
        )

        assertEquals(listOf("restored", "drive", "phone"), completedBackupHistory(records).map { it.id })
    }

    @Test
    fun restoreCompletionUpdatesTheExactArchiveInsteadOfCreatingADuplicate() {
        val original = record("backup-1", "DRIVE_UPLOADED", uploadedAt = 20)
        val completed = buildRestoreCompletionRecord(
            records = listOf(original),
            userId = "user-1",
            sourceBackupId = "backup-1",
            archiveChecksum = "checksum-1",
            archiveSize = 100,
            manifest = manifest(createdAt = 10),
            driveFileId = "drive-1",
            completedAt = 30,
        )

        assertEquals("backup-1", completed.id)
        assertEquals("DRIVE_UPLOADED", completed.status)
        assertEquals(30L, completed.restoredAt)
        assertEquals(1, completedBackupHistory(listOf(completed)).size)
    }

    @Test
    fun importedArchiveRestoreUsesAStableChecksumBoundRecord() {
        val first = buildRestoreCompletionRecord(
            records = emptyList(),
            userId = "user-1",
            sourceBackupId = null,
            archiveChecksum = "checksum-1",
            archiveSize = 100,
            manifest = manifest(createdAt = 10),
            driveFileId = null,
            completedAt = 20,
        )
        val repeated = buildRestoreCompletionRecord(
            records = listOf(first),
            userId = "user-1",
            sourceBackupId = null,
            archiveChecksum = "checksum-1",
            archiveSize = 100,
            manifest = manifest(createdAt = 10),
            driveFileId = null,
            completedAt = 30,
        )

        assertEquals(first.id, repeated.id)
        assertEquals(RESTORE_COMPLETED_STATUS, repeated.status)
        assertEquals(10L, backupCompletedAt(repeated))
        assertTrue(repeated.localRelativePath.isBlank())
        assertEquals(30L, repeated.restoredAt)
    }

    @Test
    fun restoreNeverPublishesAnIntermediateLocalStatusAsACompletedBackup() {
        val intermediate = record("backup-1", "LOCAL_READY")
        val completed = buildRestoreCompletionRecord(
            records = listOf(intermediate),
            userId = "user-1",
            sourceBackupId = "backup-1",
            archiveChecksum = intermediate.checksum.orEmpty(),
            archiveSize = intermediate.sizeBytes,
            manifest = manifest(createdAt = intermediate.createdAt),
            driveFileId = null,
            completedAt = 30,
        )

        assertEquals(RESTORE_COMPLETED_STATUS, completed.status)
        assertEquals(listOf("backup-1"), completedBackupHistory(listOf(completed)).map { it.id })
    }

    @Test
    fun latestBackupSkipsSafetyAndRestoreOnlyRows() {
        val safety = record("safety", RESTORE_SAFETY_SNAPSHOT_STATUS)
        val restoreOnly = record("restore", RESTORE_COMPLETED_STATUS, path = "", restoredAt = 30)
        val usable = record("usable", "DRIVE_FAILED")

        assertEquals("usable", latestUsableBackup(listOf(safety, restoreOnly, usable))?.id)
        assertNull(latestUsableBackup(listOf(safety, restoreOnly)))
    }

    private fun record(
        id: String,
        status: String,
        createdAt: Long = 1,
        uploadedAt: Long? = null,
        restoredAt: Long? = null,
        path: String = "users/user-1/backups/$id.tijario",
    ) = BackupRecordEntity(
        id = id,
        userId = "user-1",
        localRelativePath = path,
        formatVersion = 1,
        status = status,
        sizeBytes = 100,
        checksum = "checksum-$id",
        createdAt = createdAt,
        uploadedAt = uploadedAt,
        driveFileId = null,
        lastError = null,
        restoredAt = restoredAt,
    )

    private fun manifest(createdAt: Long) = BackupManifest(
        formatVersion = 1,
        roomDatabaseVersion = 19,
        applicationVersion = "test",
        minimumApplicationVersion = "test",
        accountId = "user-1",
        installationId = "install-1",
        backupSequence = createdAt,
        createdAtEpochMillis = createdAt,
        encryptionVersion = 1,
        keyVersion = 1,
        recordCounts = emptyMap(),
        documentCount = 0,
        documentCreationEventCount = 0,
        pdfIncludedCount = 0,
        pdfMissingCount = 0,
        pdfFailedGenerationCount = 0,
    )
}
