package app.tijario.features.backup.drive

import app.tijario.data.remote.defaultHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
private const val DRIVE_UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
private const val DRIVE_LIST_FIELDS = "files(id,name,mimeType,parents,size,createdTime,appProperties)"

/** Ktor-only Drive v3 transport. Archive bytes are copied from/to files, never materialized as a ByteArray. */
class KtorDriveRestTransport(
    private val httpClient: HttpClient = defaultHttpClient(),
) : DriveRestTransport {
    override suspend fun list(accessToken: String, query: String): List<DriveRestFile> {
        val response = httpClient.get("$DRIVE_API_BASE/files") {
            bearerAuth(accessToken)
            url {
                parameters.append("q", query)
                parameters.append("fields", DRIVE_LIST_FIELDS)
                parameters.append("orderBy", "createdTime desc")
            }
        }.requireSuccess()
        return driveJson.decodeFromString<DriveListResponse>(response.bodyAsText()).files.map(DriveFileResponse::toRestFile)
    }

    override suspend fun createFolder(accessToken: String, name: String, parentId: String?): DriveRestFile {
        val request = DriveCreateRequest(
            name = name,
            mimeType = GoogleDriveRestClient.FOLDER_MIME_TYPE,
            parents = parentId?.let(::listOf),
        )
        val response = httpClient.post("$DRIVE_API_BASE/files") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            url { parameters.append("fields", DRIVE_FILE_FIELDS) }
            setBody(driveJson.encodeToString(request))
        }.requireSuccess()
        return driveJson.decodeFromString<DriveFileResponse>(response.bodyAsText()).toRestFile()
    }

    override suspend fun uploadFile(
        accessToken: String,
        parentId: String,
        file: File,
        mimeType: String,
        appProperties: Map<String, String>,
    ): DriveRestFile {
        if (!file.isFile) throw DriveBackupException.Permanent("Encrypted backup file is missing")
        val metadata = DriveCreateRequest(
            name = file.name,
            mimeType = mimeType,
            parents = listOf(parentId),
            appProperties = appProperties,
        )
        val session = httpClient.post("$DRIVE_UPLOAD_BASE/files") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            header("X-Upload-Content-Type", mimeType)
            header("X-Upload-Content-Length", file.length().toString())
            url { parameters.append("uploadType", "resumable") }
            setBody(driveJson.encodeToString(metadata))
        }.requireSuccess()
        val uploadUrl = session.headers[HttpHeaders.Location]
            ?: throw DriveBackupException.Retryable("Drive upload session is unavailable")
        val response = httpClient.put(uploadUrl) {
            bearerAuth(accessToken)
            contentType(ContentType.parse(mimeType))
            header(HttpHeaders.ContentLength, file.length().toString())
            setBody(FileStreamingContent(file, ContentType.parse(mimeType)))
        }.requireSuccess()
        return driveJson.decodeFromString<DriveFileResponse>(response.bodyAsText()).toRestFile()
    }

    override suspend fun downloadFile(accessToken: String, fileId: String, destination: File) {
        val response = httpClient.get("$DRIVE_API_BASE/files/$fileId") {
            bearerAuth(accessToken)
            url { parameters.append("alt", "media") }
        }.requireSuccess()
        val advertisedSize = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        if (advertisedSize != null && advertisedSize > MAX_DOWNLOAD_BYTES) {
            throw DriveBackupException.Permanent("Drive backup exceeds the safe download limit")
        }
        destination.parentFile?.mkdirs()
        var copied = 0L
        try {
            response.bodyAsChannel().let { source ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = source.readAvailable(buffer, 0, buffer.size)
                        if (read < 0) break
                        copied += read
                        if (copied > MAX_DOWNLOAD_BYTES) {
                            throw DriveBackupException.Permanent("Drive backup exceeds the safe download limit")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
        } catch (error: Throwable) {
            destination.delete()
            throw error
        }
    }

    override suspend fun deleteFile(accessToken: String, fileId: String) {
        httpClient.delete("$DRIVE_API_BASE/files/$fileId") { bearerAuth(accessToken) }.requireSuccess()
    }

    private suspend fun HttpResponse.requireSuccess(): HttpResponse {
        if (status.isSuccess()) return this
        when (status.value) {
            401 -> throw DriveHttpException.Unauthorized
            403 -> throw DriveHttpException.Forbidden
            404 -> throw DriveHttpException.NotFound
            429 -> throw DriveBackupException.Retryable("Google Drive rate limited the request")
            in 500..599 -> throw DriveBackupException.Retryable("Google Drive is temporarily unavailable")
            else -> throw DriveBackupException.Permanent("Google Drive rejected the request")
        }
    }

    companion object {
        const val MAX_DOWNLOAD_BYTES = 512L * 1024L * 1024L
        private const val DRIVE_FILE_FIELDS = "id,name,mimeType,parents,size,createdTime,appProperties"
    }
}

sealed class DriveHttpException(message: String) : Exception(message) {
    data object Unauthorized : DriveHttpException("Drive authorization expired")
    data object Forbidden : DriveHttpException("Drive permission was denied")
    data object NotFound : DriveHttpException("Drive resource was not found")
}

private class FileStreamingContent(
    private val file: File,
    override val contentType: ContentType,
) : OutgoingContent.WriteChannelContent() {
    override val contentLength: Long = file.length()

    override suspend fun writeTo(channel: ByteWriteChannel) {
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                channel.writeFully(buffer, 0, read)
            }
        }
    }
}

@Serializable
private data class DriveListResponse(val files: List<DriveFileResponse> = emptyList())

@Serializable
private data class DriveFileResponse(
    val id: String,
    val name: String = "",
    val mimeType: String = "",
    val parents: List<String> = emptyList(),
    val size: String? = null,
    val createdTime: String? = null,
    val appProperties: Map<String, String> = emptyMap(),
) {
    fun toRestFile() = DriveRestFile(
        id = id,
        name = name,
        mimeType = mimeType,
        parents = parents,
        sizeBytes = size?.toLongOrNull() ?: 0L,
        createdAt = createdTime?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrDefault(0L) } ?: 0L,
        appProperties = appProperties,
    )
}

@Serializable
private data class DriveCreateRequest(
    val name: String,
    val mimeType: String,
    val parents: List<String>? = null,
    val appProperties: Map<String, String> = emptyMap(),
)

private val driveJson = Json { ignoreUnknownKeys = true; explicitNulls = false }
