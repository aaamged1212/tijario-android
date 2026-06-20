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
