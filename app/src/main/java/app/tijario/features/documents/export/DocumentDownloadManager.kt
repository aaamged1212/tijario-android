package app.tijario.features.documents.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.pdf.PdfCacheManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DocumentDownloadManager(
    private val context: Context,
    private val cacheManager: PdfCacheManager = PdfCacheManager(context),
) {
    fun save(pdfFile: File, model: DocumentRenderModel): Uri {
        val displayName = cacheManager.displayName(model)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStoreDownloads(pdfFile, displayName)
        } else {
            saveToAppExternalDownloads(pdfFile, displayName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStoreDownloads(pdfFile: File, displayName: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: error("Unable to create download entry")
        resolver.openOutputStream(uri)?.use { output ->
            FileInputStream(pdfFile).use { input -> input.copyTo(output) }
        } ?: error("Unable to write PDF")
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    private fun saveToAppExternalDownloads(pdfFile: File, displayName: String): Uri {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.cacheDir.resolve("documents/downloads")
        directory.mkdirs()
        val destination = uniqueFile(directory, displayName)
        FileInputStream(pdfFile).use { input ->
            FileOutputStream(destination).use { output -> input.copyTo(output) }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destination,
        )
    }

    private fun uniqueFile(directory: File, displayName: String): File {
        val baseName = displayName.substringBeforeLast('.', displayName)
        val extension = displayName.substringAfterLast('.', "pdf")
        var candidate = directory.resolve("$baseName.$extension")
        var index = 2
        while (candidate.exists()) {
            candidate = directory.resolve("$baseName-$index.$extension")
            index += 1
        }
        return candidate
    }
}
