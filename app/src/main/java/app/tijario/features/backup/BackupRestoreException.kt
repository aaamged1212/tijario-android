package app.tijario.features.backup

import app.tijario.features.backup.drive.DriveBackupException

/** Safe, user-facing restore outcomes. The cause is retained only for local diagnostics. */
class BackupRestoreException(
    val code: Code,
    cause: Throwable? = null,
    val table: String? = null,
) : Exception(code.name, cause) {
    enum class Code {
        DRIVE_AUTH_REQUIRED,
        DRIVE_FILE_NOT_FOUND,
        DRIVE_DOWNLOAD_FAILED,
        DRIVE_FILE_EMPTY,
        DRIVE_FILE_SIZE_MISMATCH,
        BACKUP_HASH_MISMATCH,
        BACKUP_HEADER_INVALID,
        BACKUP_FORMAT_UNSUPPORTED,
        BACKUP_ACCOUNT_MISMATCH,
        BACKUP_KEY_VERSION_UNAVAILABLE,
        BACKUP_DEVICE_KEY_INVALID,
        BACKUP_DECRYPTION_FAILED,
        BACKUP_CONTENT_INVALID,
        PRE_RESTORE_SAFETY_BACKUP_FAILED,
        BACKUP_RESTORE_TRANSACTION_FAILED,
        BACKUP_ASSET_RESTORE_FAILED,
        RESTORE_FILE_PERMISSION_LOST,
        RESTORE_ASSET_STAGE_FAILED,
        RESTORE_ASSET_APPLY_FAILED,
        RESTORE_DB_SCHEMA_INCOMPATIBLE,
        RESTORE_DB_DELETE_FAILED,
        RESTORE_DB_INSERT_FAILED,
        RESTORE_DB_CONSTRAINT_FAILED,
        RESTORE_DB_FOREIGN_KEY_FAILED,
        RESTORE_DB_COMMIT_FAILED,
        RESTORE_ROLLBACK_FAILED,
    }
}

enum class BackupRestoreStage(val progressKey: String) {
    VALIDATING("backup_validating"),
    CREATING_SAFETY_BACKUP("backup_creating_safety_backup"),
    RESTORING_FILES("backup_restoring_files"),
    RESTORING_RECORDS("backup_restoring"),
}

internal fun restoreFailureFor(error: Throwable): BackupRestoreException {
    if (error is BackupRestoreException) return error
    if (error is BackupKeyException) {
        return when (error.code) {
            "BACKUP_DEVICE_KEY_INVALID" -> BackupRestoreException(BackupRestoreException.Code.BACKUP_DEVICE_KEY_INVALID, error)
            else -> BackupRestoreException(BackupRestoreException.Code.BACKUP_KEY_VERSION_UNAVAILABLE, error)
        }
    }
    if (error is DriveBackupException) {
        val code = when (error) {
            is DriveBackupException.ReauthorizationRequired -> BackupRestoreException.Code.DRIVE_AUTH_REQUIRED
            is DriveBackupException.AccountMismatch -> BackupRestoreException.Code.BACKUP_ACCOUNT_MISMATCH
            is DriveBackupException.IntegrityFailure -> BackupRestoreException.Code.BACKUP_HASH_MISMATCH
            is DriveBackupException.Retryable -> BackupRestoreException.Code.DRIVE_DOWNLOAD_FAILED
            else -> when {
                "not found" in error.message.orEmpty().lowercase() -> BackupRestoreException.Code.DRIVE_FILE_NOT_FOUND
                "empty" in error.message.orEmpty().lowercase() -> BackupRestoreException.Code.DRIVE_FILE_EMPTY
                "size" in error.message.orEmpty().lowercase() -> BackupRestoreException.Code.DRIVE_FILE_SIZE_MISMATCH
                else -> BackupRestoreException.Code.DRIVE_DOWNLOAD_FAILED
            }
        }
        return BackupRestoreException(code, error)
    }
    val detail = error.message.orEmpty().lowercase()
    val code = when {
        "different account" in detail -> BackupRestoreException.Code.BACKUP_ACCOUNT_MISMATCH
        "header" in detail -> BackupRestoreException.Code.BACKUP_HEADER_INVALID
        "format" in detail || "version" in detail -> BackupRestoreException.Code.BACKUP_FORMAT_UNSUPPORTED
        "checksum" in detail || "authentication" in detail -> BackupRestoreException.Code.BACKUP_HASH_MISMATCH
        "decrypt" in detail -> BackupRestoreException.Code.BACKUP_DECRYPTION_FAILED
        "relationship" in detail -> BackupRestoreException.Code.RESTORE_DB_FOREIGN_KEY_FAILED
        "asset" in detail -> BackupRestoreException.Code.BACKUP_ASSET_RESTORE_FAILED
        else -> BackupRestoreException.Code.BACKUP_CONTENT_INVALID
    }
    return BackupRestoreException(code, error)
}
