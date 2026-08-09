package app.tijario.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.data.remote.UploadLogoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.net.URL

private const val MAX_LOGO_DOWNLOAD_BYTES = 5L * 1024L * 1024L
private const val LOGO_CONNECT_TIMEOUT_MS = 10_000
private const val LOGO_READ_TIMEOUT_MS = 15_000

@Composable
fun StoreLogoPicker(
    logoUrl: String?,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewUri: Uri? = null,
    size: Dp = 110.dp,
) {
    val context = LocalContext.current
    val logoBitmap = produceState<Bitmap?>(initialValue = null, previewUri, logoUrl) {
        value = loadStoreLogoBitmap(context, previewUri, logoUrl)
    }.value

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(enabled = !isUploading) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        when {
            logoBitmap != null -> Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = t("store_logo"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = t("store_logo"),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }
        }
    }
}

suspend fun loadStoreLogoBitmap(
    context: Context,
    previewUri: Uri? = null,
    logoUrl: String? = null,
): Bitmap? {
    previewUri?.let { uri ->
        loadBitmapFromUri(context, uri)?.let { return it }
    }
    logoUrl?.takeIf { it.isNotBlank() }?.let { url ->
        return loadCachedLogoBitmap(context, url)
    }
    return null
}

suspend fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }.getOrNull()
}

suspend fun loadCachedLogoBitmap(
    context: Context,
    logoUrl: String,
): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        if (!logoUrl.startsWith("https://", ignoreCase = true)) return@withContext null
        val cacheDir = File(context.filesDir, "business-logo-cache").apply { mkdirs() }
        val cacheFile = File(cacheDir, "${logoUrl.sha256()}.img")

        if (cacheFile.isUsableLogoFile()) {
            BitmapFactory.decodeFile(cacheFile.absolutePath)?.let { return@withContext it }
        }

        downloadLogo(logoUrl, cacheFile)
        cacheFile.takeIf(File::isUsableLogoFile)?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }.getOrNull()
}

private fun File.isUsableLogoFile(): Boolean = isFile && length() in 1..MAX_LOGO_DOWNLOAD_BYTES

private fun downloadLogo(logoUrl: String, destination: File) {
    val connection = (URL(logoUrl).openConnection() as? HttpURLConnection)
        ?: throw IOException("Unsupported logo connection")
    val temporary = File(destination.parentFile, "${destination.name}.download")
    try {
        connection.instanceFollowRedirects = false
        connection.connectTimeout = LOGO_CONNECT_TIMEOUT_MS
        connection.readTimeout = LOGO_READ_TIMEOUT_MS
        connection.requestMethod = "GET"
        connection.connect()
        if (connection.responseCode !in 200..299) throw IOException("Logo request failed")
        if (connection.contentType?.lowercase()?.startsWith("image/") != true) {
            throw IOException("Logo response was not an image")
        }
        if (connection.contentLengthLong > MAX_LOGO_DOWNLOAD_BYTES) throw IOException("Logo response too large")

        var written = 0L
        connection.inputStream.use { input ->
            FileOutputStream(temporary).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    written += read
                    if (written > MAX_LOGO_DOWNLOAD_BYTES) throw IOException("Logo response too large")
                    output.write(buffer, 0, read)
                }
            }
        }
        if (written == 0L) throw IOException("Empty logo response")
        if (destination.exists()) destination.delete()
        if (!temporary.renameTo(destination)) throw IOException("Unable to cache logo")
    } finally {
        temporary.delete()
        connection.disconnect()
    }
}

suspend fun clearBusinessLogoCache(
    context: Context,
) = withContext(Dispatchers.IO) {
    runCatching {
        val cacheDir = File(context.filesDir, "business-logo-cache")
        cacheDir.listFiles()?.forEach { cachedFile ->
            cachedFile.delete()
        }
    }
}

suspend fun buildLogoUploadRequest(
    context: Context,
    uri: Uri,
    language: AppLanguage,
): UploadLogoRequest = withContext(Dispatchers.IO) {
    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
    val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    require(mimeType in allowedMimeTypes) {
        Localization.getString("logo_format_error", language)
    }

    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes()
    } ?: error(Localization.getString("logo_read_error", language))

    require(bytes.isNotEmpty()) {
        Localization.getString("logo_empty_error", language)
    }
    require(bytes.size <= 2 * 1024 * 1024) {
        Localization.getString("logo_size_error", language)
    }

    UploadLogoRequest(
        fileName = "logo",
        mimeType = mimeType,
        base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
    )
}

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
