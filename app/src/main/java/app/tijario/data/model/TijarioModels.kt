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
    val currency: String,
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
    @SerialName("stock_quantity") val stockQuantity: Int? = null,
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
    @SerialName("amount_paid") val amountPaid: Double? = null,
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

@Serializable
data class UserPlanRowDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("plan_id") val planId: String,
)

@Serializable
data class UsageCounterRowDto(
    @SerialName("documents_used") val documentsUsed: Int,
    @SerialName("ai_used") val aiUsed: Int,
)

data class UserPlanUsage(
    val planCode: String,
    val planName: String,
    val periodMonth: String,
    val documentsUsed: Int,
    val documentsLimit: Int,
    val aiUsed: Int,
    val aiLimit: Int,
)

@Serializable
data class DocumentItem(
    val id: String,
    @SerialName("document_id") val documentId: String,
    @SerialName("product_id") val productId: String? = null,
    val name: String,
    val description: String? = null,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
)

@Serializable
data class CompleteDocument(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("customer_id") val customerId: String,
    val type: DocumentType,
    @SerialName("document_number") val documentNumber: String,
    val status: String,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("amount_paid") val amountPaid: Double? = null,
    @SerialName("issue_date") val issueDate: String,
    val subtotal: Double,
    val discount: Double,
    @SerialName("extra_fees") val extraFees: Double,
    val total: Double,
    val currency: String,
    val notes: String? = null,
    @SerialName("terms_text") val termsText: String? = null,
    val customer: Customer? = null,
    val items: List<DocumentItem> = emptyList(),
)
