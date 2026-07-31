package app.tijario.features.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupWorkerBehaviorTest {
    @Test
    fun retentionFailureCannotDowngradeAVerifiedUpload() {
        assertTrue(DriveUploadWorker.isVerifiedUpload("DRIVE_UPLOADED", "remote-file"))
        assertFalse(DriveUploadWorker.isVerifiedUpload("DRIVE_UPLOADED", null))
        assertFalse(DriveUploadWorker.isVerifiedUpload("DRIVE_PENDING", "remote-file"))
    }

    @Test
    fun staleFailureDoesNotOverwriteTheFreshlyPersistedUploadRecord() {
        val uploaded = app.tijario.data.local.BackupRecordEntity(
            id = "backup",
            userId = "user",
            localRelativePath = "backups/backup.tijario",
            formatVersion = 1,
            status = "DRIVE_UPLOADED",
            sizeBytes = 1,
            checksum = "checksum",
            createdAt = 1,
            uploadedAt = 2,
            driveFileId = "remote-file",
            lastError = null,
        )

        assertNull(DriveUploadWorker.failureRecord(uploaded, "DRIVE_FAILED", "drive_retryable"))
    }

    @Test
    fun defaultPhoneBackupPathUsesTheVisiblePluralBackupsFolder() {
        assertTrue(DEFAULT_PHONE_BACKUP_RELATIVE_PATH.endsWith("Tijario/Backups/"))
        assertFalse(DEFAULT_PHONE_BACKUP_RELATIVE_PATH.contains("Tijario/Backup/"))
    }

    @Test
    fun freshPreferencesUseDefaultDownloadsUntilTheUserChoosesASafTree() {
        assertTrue(resolvePhoneBackupDestinationMode(null, false) == PhoneBackupDestinationMode.DEFAULT_DOWNLOADS)
        assertTrue(resolvePhoneBackupDestinationMode(null, true) == PhoneBackupDestinationMode.CUSTOM_SAF_TREE)
        assertTrue(resolvePhoneBackupDestinationMode("DEFAULT_DOWNLOADS", true) == PhoneBackupDestinationMode.DEFAULT_DOWNLOADS)
    }

    @Test
    fun retentionWorkIsIndependentFromTheUploadingBackupWork() {
        assertFalse(BackupScheduler.driveRetentionWorkName("user") == BackupScheduler.driveWorkName("user", "backup"))
    }
}
