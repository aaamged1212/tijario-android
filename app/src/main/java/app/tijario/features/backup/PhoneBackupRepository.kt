package app.tijario.features.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import app.tijario.config.AppPreferences
import app.tijario.data.local.BackupRecordEntity
import java.io.File

data class PhoneBackupFile(
    val uri: Uri,
    val displayName: String,
    val destinationKey: String,
)

/** Creates a user-visible encrypted copy without requesting broad storage access. */
class PhoneBackupRepository(private val context: Context) {
    fun saveVisibleCopy(userId: String, record: BackupRecordEntity, filesRoot: File): PhoneBackupFile {
        val source = File(filesRoot, record.localRelativePath).canonicalFile
        if (!source.isFile || !source.name.endsWith(".tijario")) {
            throw BackupValidationException("Backup file is unavailable")
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(source)
        } else {
            val treeUri = AppPreferences.getPhoneBackupTreeUri(context, userId)
                ?: throw BackupValidationException("Phone backup folder selection is required")
            saveToTree(source, treeUri)
        }
    }

    fun rememberTree(userId: String, uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
        AppPreferences.setPhoneBackupTreeUri(context, userId, uri.toString())
    }

    fun destinationKey(userId: String): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "backup_phone_folder_documents"
        } else if (AppPreferences.getPhoneBackupTreeUri(context, userId) != null) {
            "backup_phone_folder_selected"
        } else {
            "backup_phone_folder_required"
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(source: File): PhoneBackupFile {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Tijario | تجاريو/Backups | النسخ الاحتياطية")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
            ?: throw BackupValidationException("Phone backup destination is unavailable")
        try {
            resolver.openOutputStream(uri, "w")?.use { output -> source.inputStream().use { it.copyTo(output) } }
                ?: throw BackupValidationException("Phone backup destination is unavailable")
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            return PhoneBackupFile(uri, source.name, "backup_phone_folder_documents")
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup could not be saved on the phone", error)
        }
    }

    private fun saveToTree(source: File, treeUri: Uri): PhoneBackupFile {
        val resolver = context.contentResolver
        val root = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        val outputUri = DocumentsContract.createDocument(resolver, root, "application/octet-stream", source.name)
            ?: throw BackupValidationException("Phone backup destination is unavailable")
        try {
            resolver.openOutputStream(outputUri, "w")?.use { output -> source.inputStream().use { it.copyTo(output) } }
                ?: throw BackupValidationException("Phone backup destination is unavailable")
            return PhoneBackupFile(outputUri, source.name, "backup_phone_folder_selected")
        } catch (error: Exception) {
            resolver.delete(outputUri, null, null)
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup could not be saved on the phone", error)
        }
    }
}
