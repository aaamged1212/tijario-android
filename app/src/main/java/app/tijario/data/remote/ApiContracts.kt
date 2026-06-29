package app.tijario.data.remote

import app.tijario.data.model.DocumentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResult<T>(
    val ok: Boolean,
    val code: String? = null,
    val message: String? = null,
    val data: T? = null,
) {
    val displayMessage: String
        get() = message ?: "حدث خطأ غير متوقع. حاول مرة أخرى."
}

@Serializable
data class DocumentItemInput(
    val name: String,
    @SerialName("product_id") val productId: String? = null,
    val description: String? = null,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
)

@Serializable
data class DocumentCustomerInput(
    val name: String,
    @SerialName("whatsapp_number") val whatsappNumber: String,
    val city: String? = null,
)

@Serializable
data class CreateDocumentRequest(
    val type: DocumentType,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("amount_paid") val amountPaid: Double? = null,
    val customer: DocumentCustomerInput,
    val items: List<DocumentItemInput>,
    val discount: Double = 0.0,
    @SerialName("extra_fees") val extraFees: Double = 0.0,
    val notes: String? = null,
    @SerialName("terms_text") val termsText: String? = null,
    val currency: String? = null,
)

@Serializable
data class CreateDocumentResponse(
    @SerialName("document_id") val documentId: String,
    @SerialName("document_number") val documentNumber: String,
)

@Serializable
data class AiReplyRequest(
    @SerialName("case_type") val caseType: String,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_message") val customerMessage: String? = null,
    val dialect: String,
    val tone: String,
    val length: String,
    @SerialName("extra_note") val extraNote: String? = null,
)

@Serializable
data class AiImageInput(
    @SerialName("mime_type") val mimeType: String,
    val base64: String,
)

@Serializable
data class AiCaptionRequest(
    @SerialName("caption_type") val captionType: String,
    val platform: String,
    val dialect: String,
    val tone: String,
    val length: String,
    @SerialName("product_or_service") val productOrService: String,
    val offer: String? = null,
    @SerialName("product_image") val productImage: AiImageInput? = null,
    @SerialName("extra_note") val extraNote: String? = null,
)

@Serializable
data class AiReplyResponse(
    val replies: Map<String, String> = emptyMap(),
)

@Serializable
data class AiCaptionVariant(
    val caption: String,
    val cta: String,
    val hashtags: List<String> = emptyList(),
)

@Serializable
data class AiCaptionResponse(
    val captions: Map<String, AiCaptionVariant> = emptyMap(),
)

@Serializable
data class AiV2ReplyRequest(
    @SerialName("schema_version") val schemaVersion: Int = 2,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("customer_message") val customerMessage: String? = null,
    @SerialName("quick_case") val quickCase: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val goal: String = "auto",
    val dialect: String = "auto",
    val tone: String = "auto",
    val length: String = "short",
    @SerialName("extra_context") val extraContext: String? = null,
    val language: String = "ar",
)

@Serializable
data class AiV2CaptionRequest(
    @SerialName("schema_version") val schemaVersion: Int = 2,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("product_or_service") val productOrService: String,
    @SerialName("caption_type") val captionType: String,
    val platform: String,
    val dialect: String = "auto",
    val tone: String = "auto",
    val length: String = "short",
    val offer: String? = null,
    @SerialName("product_image") val productImage: AiImageInput? = null,
    @SerialName("extra_context") val extraContext: String? = null,
    val language: String = "ar",
)

@Serializable
data class AiV2Variant(
    val id: String,
    val label: String,
    val text: String,
)

@Serializable
data class AiV2Usage(
    val used: Int,
    val limit: Int,
    val remaining: Int,
)

@Serializable
data class AiV2Detected(
    val intent: String,
    val mood: String,
    @SerialName("recommended_tone") val recommendedTone: String,
)

@Serializable
data class AiV2ResponseData(
    @SerialName("generation_id") val generationId: String,
    @SerialName("schema_version") val schemaVersion: Int,
    val detected: AiV2Detected? = null,
    @SerialName("missing_information") val missingInformation: List<String> = emptyList(),
    val variants: List<AiV2Variant> = emptyList(),
    val usage: AiV2Usage,
)

@Serializable
data class AiV2Response(
    val ok: Boolean,
    val data: AiV2ResponseData? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class AiV2ReportRequest(
    @SerialName("schema_version") val schemaVersion: Int = 2,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("generation_id") val generationId: String,
    @SerialName("issue_type") val issueType: String,
    val note: String? = null,
)

@Serializable
data class AiReportRequest(
    @SerialName("generation_type") val generationType: String,
    @SerialName("generation_id") val generationId: String? = null,
    @SerialName("model_type") val modelType: String,
    @SerialName("report_reason") val reportReason: String,
    @SerialName("user_note") val userNote: String? = null,
    @SerialName("variant_id") val variantId: String? = null,
    @SerialName("content_snapshot") val contentSnapshot: String? = null,
)

@Serializable
data class AiV2UsageResponse(
    val ok: Boolean,
    val data: AiV2Usage? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class AiV3ReplyRequest(
    @SerialName("schema_version") val schemaVersion: Int = 3,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("customer_message") val customerMessage: String? = null,
    @SerialName("quick_case") val quickCase: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    val goal: String = "auto",
    val dialect: String = "auto",
    val tone: String = "auto",
    val length: String = "short",
    @SerialName("extra_context") val extraContext: String? = null,
    val language: String = "ar",
)

@Serializable
data class AiV3CaptionRequest(
    @SerialName("schema_version") val schemaVersion: Int = 3,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_or_service") val productOrService: String? = null,
    @SerialName("primary_benefit") val primaryBenefit: String? = null,
    val offer: String? = null,
    val platform: String = "instagram",
    @SerialName("caption_type") val captionType: String = "product_post",
    val dialect: String = "auto",
    val length: String = "short",
    val style: String = "sales",
    val language: String = "ar",
)

@Serializable
data class AiV3RefineRequest(
    @SerialName("schema_version") val schemaVersion: Int = 3,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("generation_id") val generationId: String,
    @SerialName("variant_id") val variantId: String,
    val preset: String,
    val dialect: String? = null,
    val language: String = "ar",
)

@Serializable
data class AiV3ReportRequest(
    @SerialName("schema_version") val schemaVersion: Int = 3,
    @SerialName("client_request_id") val clientRequestId: String,
    @SerialName("generation_type") val generationType: String,
    @SerialName("generation_id") val generationId: String,
    @SerialName("variant_id") val variantId: String,
    @SerialName("issue_type") val issueType: String,
    val note: String? = null,
)

@Serializable
data class AiV3Variant(
    val id: String,
    val label: String,
    val text: String,
)

@Serializable
data class AiV3Usage(
    val used: Int,
    val limit: Int,
    val remaining: Int,
)

@Serializable
data class AiV3Analysis(
    val intent: String = "unknown",
    val mood: String = "unknown",
    @SerialName("recommended_tone") val recommendedTone: String = "unknown",
    @SerialName("buying_stage") val buyingStage: String = "unknown",
    val objection: String? = null,
    @SerialName("risk_flags") val riskFlags: List<String> = emptyList(),
)

@Serializable
data class AiV3ResponseData(
    @SerialName("generation_id") val generationId: String,
    @SerialName("schema_version") val schemaVersion: Int = 3,
    val analysis: AiV3Analysis = AiV3Analysis(),
    @SerialName("missing_information") val missingInformation: List<String> = emptyList(),
    val variants: List<AiV3Variant> = emptyList(),
    val usage: AiV3Usage,
)

@Serializable
data class AiV3Response(
    val ok: Boolean,
    val data: AiV3ResponseData? = null,
    val code: String? = null,
    val message: String? = null,
    val retryable: Boolean? = null,
)

@Serializable
data class AiV3ReportResponse(
    val ok: Boolean,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class UploadLogoRequest(
    @SerialName("file_name") val fileName: String,
    @SerialName("mime_type") val mimeType: String,
    val base64: String,
)

@Serializable
data class UploadLogoResponse(
    @SerialName("logo_url") val logoUrl: String?,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordResponse(
    val sent: Boolean = false,
)

@Serializable
data class AnnouncementDto(
    val id: String,
    @SerialName("title_ar") val titleAr: String,
    @SerialName("body_ar") val bodyAr: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("body_en") val bodyEn: String,
    @SerialName("action_label_ar") val actionLabelAr: String? = null,
    @SerialName("action_label_en") val actionLabelEn: String? = null,
    @SerialName("deep_link") val deepLink: String? = null,
    val priority: Int = 0,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("is_seen") val isSeen: Boolean = false,
    @SerialName("is_dismissed") val isDismissed: Boolean = false,
)

@Serializable
data class AnnouncementsBootstrapData(
    val items: List<AnnouncementDto> = emptyList(),
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("startup_announcement") val startupAnnouncement: AnnouncementDto? = null,
    @SerialName("server_time") val serverTime: String,
)

@Serializable
data class AnnouncementReceiptRequest(
    @SerialName("announcement_id") val announcementId: String,
    val event: String,
    @SerialName("opened_from") val openedFrom: String? = null,
    @SerialName("client_request_id") val clientRequestId: String,
)

@Serializable
data class AnnouncementReceiptResponse(
    @SerialName("updated_count") val updatedCount: Int? = null,
)
