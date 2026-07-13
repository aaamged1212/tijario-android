package app.tijario.features.documents.export

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.pdf.PdfCacheManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DocumentDownloadManager(
    private val context: Context,
    private val cacheManager: PdfCacheManager = PdfCacheManager(context),
) {
    companion object {
        fun needsLegacyWritePermission(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_GRANTED
    }

    fun save(pdfFile: File, model: DocumentRenderModel): Uri {
        val displayName = cacheManager.displayName(model)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStoreDownloads(pdfFile, displayName)
        } else {
            saveToPublicDownloads(pdfFile, displayName)
        }
    }

    private fun saveToMediaStoreDownloads(pdfFile: File, displayName: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values)
            ?: error("download_insert_failed")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                FileInputStream(pdfFile).use { input -> input.copyTo(output) }
            } ?: error("download_open_failed")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun saveToPublicDownloads(pdfFile: File, displayName: String): Uri {
        @Suppress("DEPRECATION")
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?: error("download_directory_unavailable")
        if (!directory.exists()) directory.mkdirs()
        val file = uniqueFile(directory, displayName)
        FileInputStream(pdfFile).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return Uri.fromFile(file)
    }

    private fun uniqueFile(directory: File, displayName: String): File {
        val base = displayName.substringBeforeLast('.', displayName)
        val extension = displayName.substringAfterLast('.', "")
            .takeIf { it.isNotBlank() }
            ?.let { ".$it" }
            .orEmpty()
        var candidate = File(directory, displayName)
        var suffix = 1
        while (candidate.exists()) {
            candidate = File(directory, "$base ($suffix)$extension")
            suffix += 1
        }
        return candidate
    }
}
