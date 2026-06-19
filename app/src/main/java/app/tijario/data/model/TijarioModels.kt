package app.tijario.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessSettings(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("business_name") val businessName: String,
    @SerialName("whatsapp_number") val whatsappNumber: String,
    val country: String,
    val city: String? = null,
    val currency: String = "SAR",
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("instagram_url") val instagramUrl: String? = null,
    @SerialName("invoice_note") val invoiceNote: String? = null,
    @SerialName("terms_text") val termsText: String? = null,
)

@Serializable
data class Customer(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    @SerialName("whatsapp_number") val whatsappNumber: String,
    val city: String? = null,
    val notes: String? = null,
)

@Serializable
data class Product(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val kind: ProductKind,
    val name: String,
    val description: String? = null,
    val price: Double,
    val currency: String = "SAR",
)

@Serializable
enum class ProductKind {
    @SerialName("product") Product,
    @SerialName("service") Service,
}

@Serializable
data class DocumentSummary(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    val type: DocumentType,
    @SerialName("document_number") val documentNumber: String,
    val status: String,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("issue_date") val issueDate: String,
    val total: Double,
    val currency: String,
)

@Serializable
enum class DocumentType {
    @SerialName("quote") Quote,
    @SerialName("invoice") Invoice,
}

@Serializable
data class UsageCounter(
    @SerialName("documents_used") val documentsUsed: Int,
    @SerialName("ai_used") val aiUsed: Int,
)

@Serializable
data class Plan(
    val code: String,
    val name: String,
    @SerialName("monthly_document_limit") val monthlyDocumentLimit: Int,
    @SerialName("monthly_ai_limit") val monthlyAiLimit: Int,
)
