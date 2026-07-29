package app.tijario.features.backup

/** Safe, user-facing restore outcomes. The cause is retained only for local diagnostics. */
class BackupRestoreException(
    val code: Code,
    cause: Throwable? = null,
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
    val detail = error.message.orEmpty().lowercase()
    val code = when {
        "different account" in detail -> BackupRestoreException.Code.BACKUP_ACCOUNT_MISMATCH
        "header" in detail -> BackupRestoreException.Code.BACKUP_HEADER_INVALID
        "format" in detail || "version" in detail -> BackupRestoreException.Code.BACKUP_FORMAT_UNSUPPORTED
        "checksum" in detail || "authentication" in detail -> BackupRestoreException.Code.BACKUP_HASH_MISMATCH
        "decrypt" in detail -> BackupRestoreException.Code.BACKUP_DECRYPTION_FAILED
        "asset" in detail -> BackupRestoreException.Code.BACKUP_ASSET_RESTORE_FAILED
        else -> BackupRestoreException.Code.BACKUP_CONTENT_INVALID
    }
    return BackupRestoreException(code, error)
}
