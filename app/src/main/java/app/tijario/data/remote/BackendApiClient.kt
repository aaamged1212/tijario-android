package app.tijario.data.remote

import app.tijario.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BackendApiClient(
    private val config: AppConfig,
    private val accessTokenProvider: suspend () -> String? ,
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

    suspend fun generateAiReplyV2(request: AiV2ReplyRequest): AiV2Response =
        authorizedPost("api/mobile/ai/v2/reply", request).decodeJsonResponseOrFallback(
            fallback = { generateAiReply(request.toLegacyReplyRequest()).toV2ReplyResponse(request.clientRequestId) },
        )

    suspend fun generateAiCaptionV2(request: AiV2CaptionRequest): AiV2Response =
        authorizedPost("api/mobile/ai/v2/caption", request).decodeJsonResponseOrFallback(
            fallback = { generateAiCaption(request.toLegacyCaptionRequest()).toV2CaptionResponse(request.clientRequestId) },
        )

    suspend fun reportAiV2(request: AiV2ReportRequest): ApiResult<Unit> =
        authorizedPost("api/mobile/ai/v2/reports", request).decodeApiResultOrFallback(
            fallback = {
                authorizedPost("api/mobile/ai/reports", request.toLegacyReportRequest()).decodeApiResult()
            },
        )

    suspend fun generateAiReplyV3(request: AiV3ReplyRequest): AiV3Response =
        authorizedPost("api/mobile/ai/v3/reply", request).decodeJsonResponse()

    suspend fun generateAiCaptionV3(request: AiV3CaptionRequest): AiV3Response =
        authorizedPost("api/mobile/ai/v3/caption", request).decodeJsonResponse()

    suspend fun refineAiV3(request: AiV3RefineRequest): AiV3Response =
        authorizedPost("api/mobile/ai/v3/refine", request).decodeJsonResponse()

    suspend fun reportAiV3(request: AiV3ReportRequest): AiV3ReportResponse =
        authorizedPost("api/mobile/ai/v3/reports", request).decodeJsonResponse()

    suspend fun fetchAccountUsageV2(): AiV2UsageResponse =
        authorizedGet("api/mobile/account/usage").decodeJsonResponse()

    suspend fun uploadBusinessLogo(request: UploadLogoRequest): ApiResult<UploadLogoResponse> =
        authorizedPost("api/mobile/business-settings/logo", request).decodeApiResult()

    suspend fun requestPasswordReset(request: ResetPasswordRequest): ApiResult<ResetPasswordResponse> =
        publicPost("api/mobile/auth/reset-password", request).decodeApiResult()

    suspend fun deleteAccount(): ApiResult<Unit> =
        authorizedPostNoBody("api/mobile/account/delete").decodeApiResult()

    suspend fun fetchDocumentPdf(documentId: String): ByteArray =
        authorizedGet("api/mobile/documents/$documentId/pdf").body()

    suspend fun pushSync(request: PushSyncRequest): HttpResponse =
        authorizedPost("api/mobile/sync/push", request)

    suspend fun pullSync(request: PullSyncRequest): HttpResponse =
        authorizedPost("api/mobile/sync/pull", request)

    suspend fun bootstrapSync(request: BootstrapSyncRequest): HttpResponse =
        authorizedPost("api/mobile/sync/bootstrap", request)

    suspend fun requestOfflineLease(request: OfflineLeaseRequest): HttpResponse =
        authorizedPost("api/mobile/account/offline-lease", request)

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

    private suspend fun authorizedPostNoBody(path: String): HttpResponse =
        httpClient.post(url(path)) {
            accept(ContentType.Application.Json)
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

        return runCatching {
            apiJson.decodeFromString<ApiResult<T>>(text)
        }.getOrElse {
            ApiResult(
                ok = false,
                code = if (contentType.contains("application/json", ignoreCase = true)) {
                    "invalid_api_response"
                } else {
                    "unexpected_api_response"
                },
                message = when {
                    contentType.contains("application/json", ignoreCase = true) ->
                        "تعذر قراءة رد الخادم. حاول مرة أخرى بعد قليل."
                    text.trimStart().startsWith("<") ->
                        "الخادم أرسل صفحة HTML بدل JSON. تأكد من رابط الـ API."
                    else ->
                        "رد غير متوقع من الخادم. ${responseDiagnostic(contentType, text)}"
                },
            )
        }
    }

    private suspend inline fun <reified T> HttpResponse.decodeJsonResponse(): T {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        return runCatching {
            apiJson.decodeFromString<T>(text)
        }.getOrElse { error ->
            throw IllegalStateException(
                "Unexpected API response content type. ${responseDiagnostic(contentType, text)}",
                error,
            )
        }
    }

    private suspend inline fun <reified T> HttpResponse.decodeJsonResponseOrFallback(
        crossinline fallback: suspend () -> T,
    ): T {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        val parsed = runCatching { apiJson.decodeFromString<T>(text) }.getOrNull()
        if (parsed != null) return parsed

        return if (contentType.contains("application/json", ignoreCase = true)) {
            throw IllegalStateException(
                "Unexpected API response content type. ${responseDiagnostic(contentType, text)}",
            )
        } else {
            fallback()
        }
    }

    private suspend inline fun <reified T> HttpResponse.decodeApiResultOrFallback(
        crossinline fallback: suspend () -> ApiResult<T>,
    ): ApiResult<T> {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        return runCatching {
            apiJson.decodeFromString<ApiResult<T>>(text)
        }.getOrElse {
            if (contentType.contains("application/json", ignoreCase = true)) {
                ApiResult(
                    ok = false,
                    code = "invalid_api_response",
                    message = "تعذر قراءة رد الخادم. حاول مرة أخرى بعد قليل.",
                )
            } else {
                fallback()
            }
        }
    }

    private suspend fun io.ktor.client.request.HttpRequestBuilder.attachBearerToken() {
        val token = accessTokenProvider()
        require(!token.isNullOrBlank()) { "Missing authenticated Supabase access token." }
        bearerAuth(token)
    }

    private fun url(path: String): String =
        "${config.apiBaseUrl.trimEnd('/')}/${path.trimStart('/')}"

    private fun responseDiagnostic(contentType: String, body: String): String {
        val preview = body.trim()
            .replace(Regex("\\s+"), " ")
            .take(120)
        return "contentType=${contentType.ifBlank { "<empty>" }}, bodyPreview=${preview.ifBlank { "<empty>" }}"
    }
}

private fun AiV2ReplyRequest.toLegacyReplyRequest(): AiReplyRequest =
    AiReplyRequest(
        caseType = quickCase ?: "general_inquiry",
        customerName = customerName,
        customerMessage = customerMessage,
        dialect = dialect,
        tone = tone,
        length = length,
        extraNote = extraContext,
    )

private fun AiV2CaptionRequest.toLegacyCaptionRequest(): AiCaptionRequest =
    AiCaptionRequest(
        captionType = captionType,
        platform = platform,
        dialect = dialect,
        tone = tone,
        length = length,
        productOrService = productOrService,
        offer = offer,
        productImage = productImage,
        extraNote = extraContext,
    )

private fun AiV2ReportRequest.toLegacyReportRequest(): AiReportRequest =
    AiReportRequest(
        generationType = if (generationId.isNotBlank()) "reply" else "caption",
        generationId = generationId,
        modelType = if (generationId.isNotBlank()) "reply" else "caption",
        reportReason = when (issueType) {
            "wrong_answer" -> "incorrect"
            "irrelevant" -> "irrelevant"
            "unsafe" -> "unsafe"
            "off_brand" -> "off_brand"
            else -> "other"
        },
        userNote = note,
        variantId = clientRequestId,
        contentSnapshot = null,
    )

private fun ApiResult<AiReplyResponse>.toV2ReplyResponse(clientRequestId: String): AiV2Response =
    AiV2Response(
        ok = ok,
        code = code,
        message = message,
        data = data?.let { response ->
            val variants = response.replies.entries
                .take(3)
                .mapIndexed { index, entry ->
                    AiV2Variant(
                        id = "${clientRequestId}-legacy-$index",
                        label = entry.key,
                        text = entry.value,
                    )
                }
            AiV2ResponseData(
                generationId = clientRequestId,
                schemaVersion = 2,
                detected = null,
                missingInformation = emptyList(),
                variants = variants,
                usage = AiV2Usage(used = 0, limit = 0, remaining = 0),
            )
        },
    )

private fun ApiResult<AiCaptionResponse>.toV2CaptionResponse(clientRequestId: String): AiV2Response =
    AiV2Response(
        ok = ok,
        code = code,
        message = message,
        data = data?.let { response ->
            val variants = response.captions.entries
                .take(3)
                .mapIndexed { index, entry ->
                    AiV2Variant(
                        id = "${clientRequestId}-legacy-$index",
                        label = entry.key,
                        text = buildString {
                            append(entry.value.caption)
                            if (entry.value.cta.isNotBlank()) {
                                appendLine()
                                appendLine()
                                append(entry.value.cta)
                            }
                            if (entry.value.hashtags.isNotEmpty()) {
                                appendLine()
                                appendLine()
                                append(entry.value.hashtags.joinToString(" "))
                            }
                        }.trim(),
                    )
                }
            AiV2ResponseData(
                generationId = clientRequestId,
                schemaVersion = 2,
                detected = null,
                missingInformation = emptyList(),
                variants = variants,
                usage = AiV2Usage(used = 0, limit = 0, remaining = 0),
            )
        },
    )

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

@Serializable
data class SyncOperationDto(
    val operation_id: String,
    val entity_type: String,
    val entity_id: String,
    val operation: String,
    val base_server_revision: String? = null,
    val payload: kotlinx.serialization.json.JsonElement
)

@Serializable
data class PushSyncRequest(
    val operations: List<SyncOperationDto>
)

@Serializable
data class SyncOperationResultDto(
    val operation_id: String,
    val status: String,
    val server_revision: String? = null,
    val message: String? = null
)

@Serializable
data class PushSyncResponse(
    val ok: Boolean,
    val results: List<SyncOperationResultDto> = emptyList()
)

@Serializable
data class PullSyncRequest(
    val last_synced_at: String?
)

@Serializable
data class BootstrapSyncRequest(
    val last_synced_at: String? = null
)

@Serializable
data class CustomerServerDto(
    val id: String,
    val name: String,
    val whatsapp_number: String,
    val city: String?,
    val notes: String?,
    val updated_at: String
)

@Serializable
data class ProductServerDto(
    val id: String,
    val kind: String,
    val name: String,
    val description: String?,
    val price: Double,
    val currency: String,
    val stock_quantity: Int?,
    val updated_at: String
)

@Serializable
data class DocumentServerDto(
    val id: String,
    val customer_id: String,
    val type: String,
    val document_number: String,
    val status: String,
    val payment_status: String?,
    val issue_date: String,
    val subtotal: Double,
    val discount: Double,
    val extra_fees: Double,
    val total: Double,
    val currency: String,
    val notes: String?,
    val terms_text: String?,
    val updated_at: String
)

@Serializable
data class DocumentItemServerDto(
    val id: String,
    val document_id: String,
    val name: String,
    val description: String?,
    val quantity: Int,
    val unit_price: Double,
    val line_total: Double,
    val sort_order: Int,
    val updated_at: String
)

@Serializable
data class BusinessSettingsServerDto(
    val id: String? = null,
    val user_id: String,
    val business_name: String,
    val whatsapp_number: String,
    val country: String,
    val city: String?,
    val currency: String,
    val logo_url: String? = null,
    val instagram_url: String?,
    val invoice_note: String?,
    val terms_text: String?,
    val updated_at: String
)

@Serializable
data class TombstoneServerDto(
    val id: String,
    val entity_type: String,
    val entity_id: String,
    val deleted_at: String
)

@Serializable
data class SyncChangesDto(
    val customers: List<CustomerServerDto> = emptyList(),
    val products: List<ProductServerDto> = emptyList(),
    val documents: List<DocumentServerDto> = emptyList(),
    val document_items: List<DocumentItemServerDto> = emptyList(),
    val business_settings: List<BusinessSettingsServerDto> = emptyList(),
    val tombstones: List<TombstoneServerDto> = emptyList()
)

@Serializable
data class PlanUsageDto(
    val plan_code: String,
    val monthly_document_limit: Int,
    val documents_used: Int
)

@Serializable
data class PullSyncResponse(
    val ok: Boolean,
    val changes: SyncChangesDto,
    val plan_usage: PlanUsageDto,
    val next_cursor: String
)

@Serializable
data class OfflineLeaseRequest(
    val device_id: String
)

@Serializable
data class OfflineLeaseResponse(
    val ok: Boolean,
    val lease_id: String,
    val quota_granted: Int,
    val expires_at: String
)
