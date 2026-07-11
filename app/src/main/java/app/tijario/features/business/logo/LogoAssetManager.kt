package app.tijario.features.business.logo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

@Serializable
data class LogoMetadata(
    val remoteUrl: String,
    val localRelativePath: String,
    val contentHash: String,
    val downloadedAt: Long,
    val mimeType: String,
    val fileSize: Long
)

class LogoAssetManager(private val context: Context) {
    
    private fun getUserLogoDir(userId: String): File {
        return File(context.filesDir, "users/$userId/business/logo").apply { mkdirs() }
    }
    
    private fun getMetadataFile(userId: String): File {
        return File(getUserLogoDir(userId), "metadata.json")
    }
    
    fun getLocalLogoFile(userId: String): File? {
        val metaFile = getMetadataFile(userId)
        if (!metaFile.exists()) return null
        return try {
            val meta = Json.decodeFromString<LogoMetadata>(metaFile.readText())
            val logoFile = File(context.filesDir, meta.localRelativePath)
            if (logoFile.exists() && logoFile.length() == meta.fileSize) {
                logoFile
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun downloadAndCacheLogo(userId: String, remoteUrl: String): Boolean = withContext(Dispatchers.IO) {
        if (remoteUrl.isBlank()) return@withContext false
        val userLogoDir = getUserLogoDir(userId)
        val tempFile = File(userLogoDir, "temp_logo_${System.currentTimeMillis()}")
        
        try {
            val url = URL(remoteUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            val size = tempFile.length()
            if (size <= 0) {
                tempFile.delete()
                return@withContext false
            }
            
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = tempFile.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
                digest.digest()
            }
            val contentHash = hashBytes.joinToString("") { "%02x".format(it) }
            
            val mimeType = connection.contentType ?: "image/png"
            
            val finalFile = File(userLogoDir, "logo_${contentHash}")
            val finalMoved = if (tempFile.renameTo(finalFile)) {
                true
            } else {
                try {
                    tempFile.copyTo(finalFile, overwrite = true)
                    tempFile.delete()
                    true
                } catch (e: Exception) {
                    false
                }
            }
            
            if (finalMoved) {
                val relativePath = finalFile.relativeTo(context.filesDir).path
                val metadata = LogoMetadata(
                    remoteUrl = remoteUrl,
                    localRelativePath = relativePath,
                    contentHash = contentHash,
                    downloadedAt = System.currentTimeMillis(),
                    mimeType = mimeType,
                    fileSize = size
                )
                getMetadataFile(userId).writeText(Json.encodeToString(LogoMetadata.serializer(), metadata))
                
                userLogoDir.listFiles()?.forEach { file ->
                    if (file.name != "metadata.json" && file.name != finalFile.name && !file.name.startsWith("temp_logo")) {
                        file.delete()
                    }
                }
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            false
        }
    }
}
