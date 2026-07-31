package app.tijario.features.backup

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import app.tijario.config.AppPreferences
import app.tijario.data.local.BackupRecordEntity
import java.io.File

enum class PhoneBackupDestinationMode { DEFAULT_DOWNLOADS, CUSTOM_SAF_TREE }

internal val DEFAULT_PHONE_BACKUP_RELATIVE_PATH = "${Environment.DIRECTORY_DOWNLOADS}/Tijario/Backups/"

internal fun resolvePhoneBackupDestinationMode(storedMode: String?, hasCustomTree: Boolean): PhoneBackupDestinationMode =
    storedMode?.let { runCatching { PhoneBackupDestinationMode.valueOf(it) }.getOrNull() }
        ?: if (hasCustomTree) PhoneBackupDestinationMode.CUSTOM_SAF_TREE else PhoneBackupDestinationMode.DEFAULT_DOWNLOADS

data class PhoneBackupFile(
    val uri: Uri,
    val displayName: String,
    val destinationKey: String,
)

/** Creates a user-visible encrypted copy without requesting broad storage access. */
class PhoneBackupRepository(private val context: Context) {
    companion object {
        private const val LOG_TAG = "TijarioBackup"
    }

    fun saveVisibleCopy(userId: String, record: BackupRecordEntity, filesRoot: File): PhoneBackupFile {
        val source = resolveBackupArchiveFile(filesRoot, userId, record.localRelativePath)
        if (source == null) {
            Log.w(LOG_TAG, "visible_backup_source_invalid")
            throw BackupValidationException("Backup file is unavailable")
        }
        Log.i(LOG_TAG, "visible_backup_start")
        val selectedTree = AppPreferences.getPhoneBackupTreeUri(context, userId)
        return when {
            destinationMode(userId) == PhoneBackupDestinationMode.CUSTOM_SAF_TREE && selectedTree != null -> saveToTree(source, selectedTree)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveToMediaStore(source)
            else -> saveToLegacyDownloads(source)
        }
    }

    fun rememberTree(userId: String, uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
        AppPreferences.setPhoneBackupTreeUri(context, userId, uri.toString())
        AppPreferences.setPhoneBackupTreeName(context, userId, treeDisplayName(uri))
        AppPreferences.setPhoneBackupDestinationMode(context, userId, PhoneBackupDestinationMode.CUSTOM_SAF_TREE.name)
    }

    fun useDefaultDestination(userId: String) {
        AppPreferences.getPhoneBackupTreeUri(context, userId)?.let { tree ->
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    tree,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
        }
        AppPreferences.setPhoneBackupTreeUri(context, userId, null)
        AppPreferences.setPhoneBackupTreeName(context, userId, null)
        AppPreferences.setPhoneBackupDestinationMode(context, userId, PhoneBackupDestinationMode.DEFAULT_DOWNLOADS.name)
    }

    fun destinationMode(userId: String): PhoneBackupDestinationMode =
        resolvePhoneBackupDestinationMode(
            AppPreferences.getPhoneBackupDestinationMode(context, userId),
            AppPreferences.getPhoneBackupTreeUri(context, userId) != null,
        )

    fun requiresLegacyWritePermission(userId: String): Boolean =
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            destinationMode(userId) == PhoneBackupDestinationMode.DEFAULT_DOWNLOADS &&
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

    fun destinationKey(userId: String): String =
        if (destinationMode(userId) == PhoneBackupDestinationMode.CUSTOM_SAF_TREE && AppPreferences.getPhoneBackupTreeUri(context, userId) != null) {
            "backup_phone_folder_selected"
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "backup_phone_folder_downloads"
        } else "backup_phone_folder_downloads"

    fun restoreInitialUri(userId: String): Uri =
        AppPreferences.getPhoneBackupTreeUri(context, userId)
            ?: Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2FTijario%2FBackups")

    fun destinationDisplayName(userId: String): String? =
        AppPreferences.getPhoneBackupTreeName(context, userId)

    private fun treeDisplayName(treeUri: Uri): String? = runCatching {
        val root = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        context.contentResolver.query(
            root,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0)?.trim()?.takeIf(String::isNotEmpty) else null
        }
    }.getOrNull()

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(source: File): PhoneBackupFile =
        runCatching {
            saveToMediaStore(source, DEFAULT_PHONE_BACKUP_RELATIVE_PATH, "backup_phone_folder_downloads")
        }.getOrElse { error ->
            Log.w(LOG_TAG, "visible_backup_default_folder_failed error=${error.javaClass.simpleName}")
            throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.DEFAULT_FOLDER_UNAVAILABLE, error)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(source: File, relativePath: String, destinationKey: String): PhoneBackupFile {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        var uri: Uri? = null
        var stage = "insert"
        try {
            val createdUri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
                ?: throw BackupValidationException("Phone backup destination is unavailable")
            uri = createdUri
            stage = "write"
            resolver.openOutputStream(createdUri, "w")?.use { output -> source.inputStream().use { it.copyTo(output) } }
                ?: throw BackupValidationException("Phone backup destination is unavailable")
            stage = "finalize"
            resolver.update(createdUri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            return PhoneBackupFile(createdUri, source.name, destinationKey)
        } catch (error: Exception) {
            Log.w(
                LOG_TAG,
                "visible_backup_failed stage=$stage error=${error.javaClass.simpleName}",
            )
            uri?.let { resolver.delete(it, null, null) }
            if (error is BackupValidationException) throw error
            throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.DEFAULT_FOLDER_UNAVAILABLE, error)
        }
    }

    private fun saveToTree(source: File, treeUri: Uri): PhoneBackupFile {
        val resolver = context.contentResolver
        val root = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        val outputUri = try {
            DocumentsContract.createDocument(resolver, root, "application/octet-stream", source.name)
                ?: throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.PERMISSION_LOST)
        } catch (error: SecurityException) {
            throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.PERMISSION_LOST, error)
        }
        try {
            resolver.openOutputStream(outputUri, "w")?.use { output -> source.inputStream().use { it.copyTo(output) } }
                ?: throw BackupValidationException("Phone backup destination is unavailable")
            return PhoneBackupFile(outputUri, source.name, "backup_phone_folder_selected")
        } catch (error: Exception) {
            resolver.delete(outputUri, null, null)
            if (error is BackupValidationException) throw error
            throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.PERMISSION_LOST, error)
        }
    }

    private fun saveToLegacyDownloads(source: File): PhoneBackupFile {
        if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.PERMISSION_REQUIRED)
        }
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Tijario/Backups")
        if (!directory.exists() && !directory.mkdirs()) {
            throw PhoneBackupDestinationException(PhoneBackupDestinationException.Code.DEFAULT_FOLDER_UNAVAILABLE)
        }
        val destination = File(directory, source.name)
        source.copyTo(destination, overwrite = false)
        return PhoneBackupFile(Uri.fromFile(destination), source.name, "backup_phone_folder_downloads")
    }
}

class PhoneBackupDestinationException(
    val code: Code,
    cause: Throwable? = null,
) : BackupValidationException(code.name, cause) {
    enum class Code {
        DEFAULT_FOLDER_UNAVAILABLE,
        PERMISSION_LOST,
        PERMISSION_REQUIRED,
    }
}
