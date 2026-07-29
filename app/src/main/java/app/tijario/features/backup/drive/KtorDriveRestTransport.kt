package app.tijario.features.backup.drive

import app.tijario.data.remote.defaultHttpClient
import android.util.Log
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
import java.io.IOException
import java.io.File
import java.time.Instant

private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
private const val DRIVE_UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
private const val DRIVE_LIST_FIELDS = "files(id,name,mimeType,parents,size,createdTime,appProperties)"
private const val DRIVE_ABOUT_FIELDS = "user(permissionId,emailAddress)"

/** Ktor-only Drive v3 transport. Archive bytes are copied from/to files, never materialized as a ByteArray. */
class KtorDriveRestTransport(
    private val httpClient: HttpClient = defaultHttpClient(),
) : DriveRestTransport {
    override suspend fun getCurrentUser(accessToken: String): DriveCurrentUser = driveRequest("about", accountIdResolved = false) {
        val response = httpClient.get("$DRIVE_API_BASE/about") {
            bearerAuth(accessToken)
            url { parameters.append("fields", DRIVE_ABOUT_FIELDS) }
        }.requireSuccess("about", accountIdResolved = false)
        val user = driveJson.decodeFromString<DriveAboutResponse>(response.bodyAsText()).user
        val permissionId = user?.permissionId?.takeIf(String::isNotBlank)
        if (permissionId == null) {
            safeDriveLog("about", 200, "missing_permission_id", accountIdResolved = false)
            throw DriveBackupException.InvalidRequest()
        }
        DriveCurrentUser(permissionId, user.emailAddress?.takeIf(String::isNotBlank))
    }

    override suspend fun list(accessToken: String, query: String): List<DriveRestFile> = driveRequest("list", accountIdResolved = true) {
        val response = httpClient.get("$DRIVE_API_BASE/files") {
            bearerAuth(accessToken)
            url {
                parameters.append("q", query)
                parameters.append("fields", DRIVE_LIST_FIELDS)
                parameters.append("orderBy", "createdTime desc")
            }
        }.requireSuccess("list", accountIdResolved = true)
        driveJson.decodeFromString<DriveListResponse>(response.bodyAsText()).files.map(DriveFileResponse::toRestFile)
    }

    override suspend fun createFolder(accessToken: String, name: String, parentId: String?): DriveRestFile = driveRequest("create_folder", accountIdResolved = true) {
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
        }.requireSuccess("create_folder", accountIdResolved = true)
        driveJson.decodeFromString<DriveFileResponse>(response.bodyAsText()).toRestFile()
    }

    override suspend fun uploadFile(
        accessToken: String,
        parentId: String,
        file: File,
        mimeType: String,
        appProperties: Map<String, String>,
        onProgress: suspend (Long, Long) -> Unit,
    ): DriveRestFile {
        if (!file.isFile) throw DriveBackupException.Permanent("Encrypted backup file is missing")
        return driveRequest("upload", accountIdResolved = true) {
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
            }.requireSuccess("upload", accountIdResolved = true)
            val uploadUrl = session.headers[HttpHeaders.Location]
                ?: throw DriveBackupException.Retryable("Drive upload session is unavailable")
            val response = httpClient.put(uploadUrl) {
                bearerAuth(accessToken)
                contentType(ContentType.parse(mimeType))
                header(HttpHeaders.ContentLength, file.length().toString())
                setBody(FileStreamingContent(file, ContentType.parse(mimeType), onProgress))
            }.requireSuccess("upload", accountIdResolved = true)
            driveJson.decodeFromString<DriveFileResponse>(response.bodyAsText()).toRestFile()
        }
    }

    override suspend fun downloadFile(accessToken: String, fileId: String, destination: File, onProgress: suspend (Long, Long) -> Unit) = driveRequest("download", accountIdResolved = true) {
        val response = httpClient.get("$DRIVE_API_BASE/files/$fileId") {
            bearerAuth(accessToken)
            url { parameters.append("alt", "media") }
        }.requireSuccess("download", accountIdResolved = true)
        val advertisedSize = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        if (advertisedSize != null && advertisedSize > MAX_DOWNLOAD_BYTES) {
            throw DriveBackupException.Permanent("Drive backup exceeds the safe download limit")
        }
        destination.parentFile?.mkdirs()
        var copied = 0L
        onProgress(0, advertisedSize ?: 0L)
        try {
            response.bodyAsChannel().let { source ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = source.readAvailable(buffer, 0, buffer.size)
                        if (read < 0) break
                        copied += read
                        onProgress(copied, advertisedSize ?: 0L)
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

    override suspend fun deleteFile(accessToken: String, fileId: String): Unit = driveRequest("delete", accountIdResolved = true) {
        httpClient.delete("$DRIVE_API_BASE/files/$fileId") { bearerAuth(accessToken) }
            .requireSuccess("delete", accountIdResolved = true)
        Unit
    }

    private suspend fun HttpResponse.requireSuccess(operation: String, accountIdResolved: Boolean): HttpResponse {
        if (status.isSuccess()) return this
        val error = parseDriveError(bodyAsText())
        safeDriveLog(operation, status.value, error.reason ?: "unknown", accountIdResolved)
        throw driveFailureFor(status.value, error.reason)
    }

    private suspend fun <T> driveRequest(
        operation: String,
        accountIdResolved: Boolean,
        block: suspend () -> T,
    ): T = try {
        block()
    } catch (error: IOException) {
        safeDriveLog(operation, null, "network", accountIdResolved)
        throw DriveBackupException.Retryable("Google Drive is temporarily unavailable", error)
    }

    companion object {
        const val MAX_DOWNLOAD_BYTES = 512L * 1024L * 1024L
        private const val DRIVE_FILE_FIELDS = "id,name,mimeType,parents,size,createdTime,appProperties"
    }
}

internal fun driveFailureFor(status: Int, reason: String?): Throwable = when (status) {
    401 -> DriveHttpException.Unauthorized(reason)
    403 -> if (reason.isDriveApiDisabled()) DriveHttpException.NotConfigured(reason)
        else DriveHttpException.PermissionDenied(reason)
    400 -> DriveHttpException.BadRequest(reason)
    404 -> DriveHttpException.NotFound(reason)
    429 -> DriveBackupException.Retryable("Google Drive rate limited the request")
    in 500..599 -> DriveBackupException.Retryable("Google Drive is temporarily unavailable")
    else -> DriveBackupException.Permanent("Google Drive rejected the request")
}

sealed class DriveHttpException(message: String, val reason: String?) : Exception(message) {
    class Unauthorized(reason: String?) : DriveHttpException("Drive authorization expired", reason)
    class NotConfigured(reason: String?) : DriveHttpException("Google Drive API is not configured", reason)
    class PermissionDenied(reason: String?) : DriveHttpException("Drive permission was denied", reason)
    class BadRequest(reason: String?) : DriveHttpException("Google Drive rejected the request", reason)
    class NotFound(reason: String?) : DriveHttpException("Drive resource was not found", reason)
}

private class FileStreamingContent(
    private val file: File,
    override val contentType: ContentType,
    private val onProgress: suspend (Long, Long) -> Unit,
) : OutgoingContent.WriteChannelContent() {
    override val contentLength: Long = file.length()

    override suspend fun writeTo(channel: ByteWriteChannel) {
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var copied = 0L
            onProgress(0, contentLength)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                channel.writeFully(buffer, 0, read)
                copied += read
                onProgress(copied, contentLength)
            }
        }
    }
}

@Serializable
private data class DriveListResponse(val files: List<DriveFileResponse> = emptyList())

@Serializable
private data class DriveAboutResponse(val user: DriveAboutUser? = null)

@Serializable
private data class DriveAboutUser(
    val permissionId: String? = null,
    val emailAddress: String? = null,
)

@Serializable
private data class DriveErrorEnvelope(val error: DriveErrorPayload? = null)

@Serializable
private data class DriveErrorPayload(
    val message: String? = null,
    val errors: List<DriveErrorItem> = emptyList(),
)

@Serializable
private data class DriveErrorItem(val reason: String? = null)

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

private data class SafeDriveError(val reason: String?, val message: String?)

private fun parseDriveError(body: String): SafeDriveError = runCatching {
    val error = driveJson.decodeFromString<DriveErrorEnvelope>(body).error
    SafeDriveError(error?.errors?.firstOrNull()?.reason, error?.message)
}.getOrDefault(SafeDriveError(null, null))

private fun String?.isDriveApiDisabled(): Boolean = this.equals("accessNotConfigured", ignoreCase = true) ||
    this.equals("serviceDisabled", ignoreCase = true)

private fun safeDriveLog(operation: String, status: Int?, reason: String, accountIdResolved: Boolean) {
    Log.d("TijarioDrive", "operation=$operation status=${status ?: "network"} reason=$reason accountIdResolved=$accountIdResolved")
}
