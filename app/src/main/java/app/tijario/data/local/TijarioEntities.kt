package app.tijario.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind

@Entity(tableName = "business_settings_cache")
data class BusinessSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "remote_id")
    val remoteId: String?,
    @ColumnInfo(name = "business_name")
    val businessName: String,
    @ColumnInfo(name = "whatsapp_number")
    val whatsappNumber: String,
    val country: String,
    val city: String?,
    val currency: String,
    @ColumnInfo(name = "logo_url")
    val logoUrl: String?,
    @ColumnInfo(name = "instagram_url")
    val instagramUrl: String?,
    @ColumnInfo(name = "invoice_note")
    val invoiceNote: String?,
    @ColumnInfo(name = "terms_text")
    val termsText: String?,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,
)

@Entity(tableName = "customers_cache")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id", index = true)
    val userId: String,
    val name: String,
    @ColumnInfo(name = "whatsapp_number")
    val whatsappNumber: String,
    val city: String?,
    val notes: String?,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,
)

@Entity(tableName = "products_cache")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id", index = true)
    val userId: String,
    val kind: String,
    val name: String,
    val description: String?,
    val price: Double,
    val currency: String,
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int?,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,
)

@Entity(tableName = "documents_cache")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id", index = true)
    val userId: String,
    @ColumnInfo(name = "customer_id")
    val customerId: String,
    val type: String,
    @ColumnInfo(name = "document_number")
    val documentNumber: String,
    val status: String,
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String?,
    @ColumnInfo(name = "amount_paid")
    val amountPaid: Double?,
    @ColumnInfo(name = "issue_date")
    val issueDate: String,
    val total: Double,
    val currency: String,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,
)

fun BusinessSettings.toEntity(userIdFallback: String, syncedAt: Long = System.currentTimeMillis()): BusinessSettingsEntity =
    BusinessSettingsEntity(
        userId = userId ?: userIdFallback,
        remoteId = id,
        businessName = businessName,
        whatsappNumber = whatsappNumber,
        country = country,
        city = city,
        currency = currency,
        logoUrl = logoUrl,
        instagramUrl = instagramUrl,
        invoiceNote = invoiceNote,
        termsText = termsText,
        syncedAt = syncedAt,
    )

fun BusinessSettingsEntity.toModel(): BusinessSettings =
    BusinessSettings(
        id = remoteId,
        userId = userId,
        businessName = businessName,
        whatsappNumber = whatsappNumber,
        country = country,
        city = city,
        currency = currency,
        logoUrl = logoUrl,
        instagramUrl = instagramUrl,
        invoiceNote = invoiceNote,
        termsText = termsText,
    )

fun Customer.toEntity(userIdFallback: String, syncedAt: Long = System.currentTimeMillis()): CustomerEntity? {
    val remoteId = id ?: return null
    return CustomerEntity(
        id = remoteId,
        userId = userId ?: userIdFallback,
        name = name,
        whatsappNumber = whatsappNumber,
        city = city,
        notes = notes,
        syncedAt = syncedAt,
    )
}

fun CustomerEntity.toModel(): Customer =
    Customer(
        id = id,
        userId = userId,
        name = name,
        whatsappNumber = whatsappNumber,
        city = city,
        notes = notes,
    )

fun Product.toEntity(userIdFallback: String, syncedAt: Long = System.currentTimeMillis()): ProductEntity? {
    val remoteId = id ?: return null
    return ProductEntity(
        id = remoteId,
        userId = userId ?: userIdFallback,
        kind = kind.toCacheValue(),
        name = name,
        description = description,
        price = price,
        currency = currency,
        stockQuantity = stockQuantity,
        syncedAt = syncedAt,
    )
}

fun ProductEntity.toModel(): Product =
    Product(
        id = id,
        userId = userId,
        kind = kind.toProductKind(),
        name = name,
        description = description,
        price = price,
        currency = currency,
        stockQuantity = stockQuantity,
    )

fun DocumentSummary.toEntity(userId: String, syncedAt: Long = System.currentTimeMillis()): DocumentEntity =
    DocumentEntity(
        id = id,
        userId = userId,
        customerId = customerId,
        type = type.toCacheValue(),
        documentNumber = documentNumber,
        status = status,
        paymentStatus = paymentStatus,
        amountPaid = amountPaid,
        issueDate = issueDate,
        total = total,
        currency = currency,
        syncedAt = syncedAt,
    )

fun DocumentEntity.toModel(): DocumentSummary =
    DocumentSummary(
        id = id,
        customerId = customerId,
        type = type.toDocumentType(),
        documentNumber = documentNumber,
        status = status,
        paymentStatus = paymentStatus,
        amountPaid = amountPaid,
        issueDate = issueDate,
        total = total,
        currency = currency,
    )

private fun ProductKind.toCacheValue(): String =
    when (this) {
        ProductKind.Product -> "product"
        ProductKind.Service -> "service"
    }

private fun String.toProductKind(): ProductKind =
    if (equals("service", ignoreCase = true)) ProductKind.Service else ProductKind.Product

private fun DocumentType.toCacheValue(): String =
    when (this) {
        DocumentType.Invoice -> "invoice"
        DocumentType.Quote -> "quote"
    }

private fun String.toDocumentType(): DocumentType =
    if (equals("invoice", ignoreCase = true)) DocumentType.Invoice else DocumentType.Quote

@Entity(tableName = "local_taxes")
data class LocalTaxEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val rate: Double
)

@Entity(tableName = "local_payment_methods")
data class LocalPaymentMethodEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val details: String? = null
)

@Entity(tableName = "local_signatures")
data class LocalSignatureEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String
)

@Entity(tableName = "local_terms")
data class LocalTermsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String
)

@Entity(tableName = "local_document_metadata")
data class LocalDocumentMetadataEntity(
    @PrimaryKey
    val documentId: String,
    val currency: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String?,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?
)

