package app.tijario.features.backup

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import app.tijario.features.backup.drive.DriveBackupException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupRestoreBehaviorTest {
    @Test
    fun everyTypedWorkerFailureMapsToLocalizedSafeMessage() {
        val expected = mapOf(
            BackupRestoreException.Code.DRIVE_AUTH_REQUIRED to "backup_drive_reauthorization_required",
            BackupRestoreException.Code.DRIVE_FILE_NOT_FOUND to "backup_drive_file_not_found",
            BackupRestoreException.Code.DRIVE_DOWNLOAD_FAILED to "backup_drive_download_failed",
            BackupRestoreException.Code.DRIVE_FILE_EMPTY to "backup_restore_file_empty",
            BackupRestoreException.Code.DRIVE_FILE_SIZE_MISMATCH to "backup_restore_file_size_mismatch",
            BackupRestoreException.Code.BACKUP_HASH_MISMATCH to "backup_hash_mismatch",
            BackupRestoreException.Code.BACKUP_HEADER_INVALID to "backup_restore_header_invalid",
            BackupRestoreException.Code.BACKUP_FORMAT_UNSUPPORTED to "backup_restore_format_unsupported",
            BackupRestoreException.Code.BACKUP_ACCOUNT_MISMATCH to "backup_account_mismatch",
            BackupRestoreException.Code.BACKUP_KEY_VERSION_UNAVAILABLE to "backup_key_unavailable",
            BackupRestoreException.Code.BACKUP_DEVICE_KEY_INVALID to "backup_device_key_invalid",
            BackupRestoreException.Code.BACKUP_DECRYPTION_FAILED to "backup_restore_decryption_failed",
            BackupRestoreException.Code.BACKUP_CONTENT_INVALID to "backup_restore_content_invalid",
            BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED to "backup_restore_safety_backup_failed",
            BackupRestoreException.Code.BACKUP_RESTORE_TRANSACTION_FAILED to "backup_restore_transaction_failed",
            BackupRestoreException.Code.BACKUP_ASSET_RESTORE_FAILED to "backup_restore_assets_failed",
            BackupRestoreException.Code.RESTORE_FILE_PERMISSION_LOST to "backup_restore_file_permission_lost",
            BackupRestoreException.Code.RESTORE_ASSET_STAGE_FAILED to "backup_restore_asset_stage_failed",
            BackupRestoreException.Code.RESTORE_ASSET_APPLY_FAILED to "backup_restore_asset_apply_failed",
            BackupRestoreException.Code.RESTORE_DB_SCHEMA_INCOMPATIBLE to "backup_restore_schema_incompatible",
            BackupRestoreException.Code.RESTORE_DB_DELETE_FAILED to "backup_restore_db_delete_failed",
            BackupRestoreException.Code.RESTORE_DB_INSERT_FAILED to "backup_restore_db_insert_failed",
            BackupRestoreException.Code.RESTORE_DB_CONSTRAINT_FAILED to "backup_restore_db_constraint_failed",
            BackupRestoreException.Code.RESTORE_DB_FOREIGN_KEY_FAILED to "backup_restore_db_foreign_key_failed",
            BackupRestoreException.Code.RESTORE_DB_COMMIT_FAILED to "backup_restore_db_commit_failed",
            BackupRestoreException.Code.RESTORE_ROLLBACK_FAILED to "backup_restore_rollback_failed",
        )

        expected.forEach { (code, messageKey) ->
            assertEquals(messageKey, restoreErrorMessageKeyFor(code.name))
            assertFalse(Localization.getString(messageKey, AppLanguage.AR) == messageKey)
            assertFalse(Localization.getString(messageKey, AppLanguage.EN) == messageKey)
        }
        assertEquals("backup_restore_failed", restoreErrorMessageKeyFor("UNKNOWN"))
    }

    @Test
    fun driveFailuresKeepSafeTypedOutcomes() {
        assertEquals(
            BackupRestoreException.Code.DRIVE_AUTH_REQUIRED,
            restoreFailureFor(DriveBackupException.ReauthorizationRequired()).code,
        )
        assertEquals(
            BackupRestoreException.Code.DRIVE_FILE_NOT_FOUND,
            restoreFailureFor(DriveBackupException.Permanent("Drive backup was not found")).code,
        )
        assertEquals(
            BackupRestoreException.Code.DRIVE_FILE_EMPTY,
            restoreFailureFor(DriveBackupException.Permanent("Drive backup is empty")).code,
        )
        assertEquals(
            BackupRestoreException.Code.DRIVE_DOWNLOAD_FAILED,
            restoreFailureFor(DriveBackupException.Retryable("temporary")).code,
        )
    }

    @Test
    fun existingTypedRestoreFailureIsNotCollapsedToGenericTransactionFailure() {
        val typed = BackupRestoreException(BackupRestoreException.Code.RESTORE_DB_CONSTRAINT_FAILED)
        assertEquals(typed, restoreFailureFor(typed))
    }

    @Test
    fun restoreContractsPersistSafPermissionUseDataSyncAndValidateBeforeSafetyBackup() {
        val notifier = File("src/main/java/app/tijario/features/backup/BackupWorkNotifier.kt").readText()
        val viewModel = File("src/main/java/app/tijario/features/backup/BackupViewModel.kt").readText()
        val worker = File("src/main/java/app/tijario/features/backup/BackupRestoreWorker.kt").readText()
        val coordinator = File("src/main/java/app/tijario/features/backup/BackupCoordinator.kt").readText()

        assertTrue(notifier.contains("FOREGROUND_SERVICE_TYPE_DATA_SYNC"))
        assertTrue(viewModel.contains("takePersistableUriPermission"))
        assertTrue(viewModel.contains("backup_restore_file_permission_lost"))
        assertTrue(worker.contains("releasePersistableUriPermission"))
        assertTrue(worker.contains("allowNetwork = true"))
        assertTrue(worker.contains("RESTORE_FILE_PERMISSION_LOST"))

        val validation = coordinator.indexOf("restorer.validate(archiveFile")
        val safety = coordinator.indexOf("onStage(BackupRestoreStage.CREATING_SAFETY_BACKUP)", validation)
        assertTrue(validation >= 0)
        assertTrue(safety > validation)
    }
}
