package app.tijario.data.remote

import app.tijario.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BackendApiClient(
    private val config: AppConfig,
    private val accessTokenProvider: suspend () -> String?,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        authorizedPost("api/mobile/documents", request).decodeApiResult()

    suspend fun updateDocument(documentId: String, request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        authorizedPut("api/mobile/documents/$documentId", request).decodeApiResult()

    suspend fun deleteDocument(documentId: String): ApiResult<CreateDocumentResponse> =
        authorizedDelete("api/mobile/documents/$documentId").decodeApiResult()

    suspend fun generateAiReply(request: AiReplyRequest): ApiResult<AiReplyResponse> =
        authorizedPost("api/mobile/ai/reply", request).decodeApiResult()

    suspend fun generateAiCaption(request: AiCaptionRequest): ApiResult<AiCaptionResponse> =
        authorizedPost("api/mobile/ai/caption", request).decodeApiResult()

    suspend fun uploadBusinessLogo(request: UploadLogoRequest): ApiResult<UploadLogoResponse> =
        authorizedPost("api/mobile/business-settings/logo", request).decodeApiResult()

    suspend fun requestPasswordReset(request: ResetPasswordRequest): ApiResult<ResetPasswordResponse> =
        publicPost("api/mobile/auth/reset-password", request).decodeApiResult()

    suspend fun fetchDocumentPdf(documentId: String): ByteArray =
        authorizedGet("api/mobile/documents/$documentId/pdf").body()

    private suspend inline fun <reified T : Any> authorizedPost(path: String, body: T): HttpResponse =
        httpClient.post(url(path)) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            attachBearerToken()
            setBody(body)
        }

    private suspend inline fun <reified T : Any> authorizedPut(path: String, body: T): HttpResponse =
        httpClient.put(url(path)) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            attachBearerToken()
            setBody(body)
        }

    private suspend inline fun <reified T : Any> publicPost(path: String, body: T): HttpResponse =
        httpClient.post(url(path)) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend fun authorizedGet(path: String): HttpResponse =
        httpClient.get(url(path)) {
            attachBearerToken()
        }

    private suspend fun authorizedDelete(path: String): HttpResponse =
        httpClient.delete(url(path)) {
            accept(ContentType.Application.Json)
            attachBearerToken()
        }

    private suspend inline fun <reified T> HttpResponse.decodeApiResult(): ApiResult<T> {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        if (!contentType.contains("application/json", ignoreCase = true)) {
            return ApiResult(
                ok = false,
                code = "unexpected_api_response",
                message = "خدمة تيجاريو على الخادم تحتاج تحديثاً. انشر آخر نسخة من مشروع الويب ثم حاول مرة أخرى.",
            )
        }

        return runCatching {
            apiJson.decodeFromString<ApiResult<T>>(text)
        }.getOrElse {
            ApiResult(
                ok = false,
                code = "invalid_api_response",
                message = "تعذر قراءة رد الخادم. حاول مرة أخرى بعد قليل.",
            )
        }
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
private val apiJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
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
            json(apiJson)
        }
    }
