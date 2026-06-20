package app.tijario.data.remote

import app.tijario.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class BackendApiClient(
    private val config: AppConfig,
    private val accessTokenProvider: suspend () -> String?,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        authorizedPost("api/mobile/documents", request).body()

    suspend fun updateDocument(documentId: String, request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        authorizedPut("api/mobile/documents/$documentId", request).body()

    suspend fun deleteDocument(documentId: String): ApiResult<CreateDocumentResponse> =
        authorizedDelete("api/mobile/documents/$documentId").body()

    suspend fun generateAiReply(request: AiReplyRequest): ApiResult<AiReplyResponse> =
        authorizedPost("api/mobile/ai/reply", request).body()

    suspend fun generateAiCaption(request: AiCaptionRequest): ApiResult<AiCaptionResponse> =
        authorizedPost("api/mobile/ai/caption", request).body()

    suspend fun uploadBusinessLogo(request: UploadLogoRequest): ApiResult<UploadLogoResponse> =
        authorizedPost("api/mobile/business-settings/logo", request).body()

    suspend fun requestPasswordReset(request: ResetPasswordRequest): ApiResult<ResetPasswordResponse> =
        publicPost("api/mobile/auth/reset-password", request).body()

    suspend fun fetchDocumentPdf(documentId: String): ByteArray =
        authorizedGet("api/mobile/documents/$documentId/pdf").body()

    private suspend inline fun <reified T : Any> authorizedPost(path: String, body: T): HttpResponse =
        httpClient.post(url(path)) {
            contentType(ContentType.Application.Json)
            attachBearerToken()
            setBody(body)
        }

    private suspend inline fun <reified T : Any> authorizedPut(path: String, body: T): HttpResponse =
        httpClient.put(url(path)) {
            contentType(ContentType.Application.Json)
            attachBearerToken()
            setBody(body)
        }

    private suspend inline fun <reified T : Any> publicPost(path: String, body: T): HttpResponse =
        httpClient.post(url(path)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend fun authorizedGet(path: String): HttpResponse =
        httpClient.get(url(path)) {
            attachBearerToken()
        }

    private suspend fun authorizedDelete(path: String): HttpResponse =
        httpClient.delete(url(path)) {
            attachBearerToken()
        }

    private suspend fun io.ktor.client.request.HttpRequestBuilder.attachBearerToken() {
        val token = accessTokenProvider()
        require(!token.isNullOrBlank()) { "Missing authenticated Supabase access token." }
        bearerAuth(token)
    }

    private fun url(path: String): String =
        "${config.apiBaseUrl.trimEnd('/')}/${path.trimStart('/')}"
}

@OptIn(ExperimentalSerializationApi::class)
fun defaultHttpClient(): HttpClient =
    HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 45_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 45_000
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                },
            )
        }
    }
