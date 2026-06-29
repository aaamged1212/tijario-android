package app.tijario.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import java.math.BigDecimal

@Entity(
    tableName = "business_settings_cache",
    indices = [Index(value = ["user_id"])]
)
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

    // Sync Metadata
    @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY",
    @ColumnInfo(name = "local_revision") val localRevision: Long = 1,
    @ColumnInfo(name = "server_revision") val serverRevision: String? = null,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long? = null,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "sync_error_code") val syncErrorCode: String? = null,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "customers_cache",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["whatsapp_number"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val name: String,
    @ColumnInfo(name = "whatsapp_number")
    val whatsappNumber: String,
    val city: String?,
    val notes: String?,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,

    // Sync Metadata
    @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY",
    @ColumnInfo(name = "local_revision") val localRevision: Long = 1,
    @ColumnInfo(name = "server_revision") val serverRevision: String? = null,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long? = null,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "sync_error_code") val syncErrorCode: String? = null,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "products_cache",
    indices = [Index(value = ["user_id"])]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val kind: String,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val currency: String,
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int?,
    val category: String? = null,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,

    // Sync Metadata
    @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY",
    @ColumnInfo(name = "local_revision") val localRevision: Long = 1,
    @ColumnInfo(name = "server_revision") val serverRevision: String? = null,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long? = null,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "sync_error_code") val syncErrorCode: String? = null,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "documents_cache",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "document_number"], unique = true),
        Index(value = ["user_id", "id"], unique = true) // Required for Composite Foreign Key referencing
    ]
)
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
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
    val amountPaid: BigDecimal?,
    @ColumnInfo(name = "issue_date")
    val issueDate: String,
    val total: BigDecimal,
    val currency: String,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,

    // V7 additions
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val discount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "extra_fees")
    val extraFees: BigDecimal = BigDecimal.ZERO,
    val notes: String? = null,
    @ColumnInfo(name = "terms_text")
    val termsText: String? = null,

    // Sync Metadata
    @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY",
    @ColumnInfo(name = "local_revision") val localRevision: Long = 1,
    @ColumnInfo(name = "server_revision") val serverRevision: String? = null,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long? = null,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "sync_error_code") val syncErrorCode: String? = null,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,

    // Local PDF Metadata
    @ColumnInfo(name = "local_pdf_relative_path") val localPdfRelativePath: String? = null,
    @ColumnInfo(name = "pdf_generated_at") val pdfGeneratedAt: Long? = null,
    @ColumnInfo(name = "pdf_document_revision") val pdfDocumentRevision: Long? = null,
    @ColumnInfo(name = "pdf_content_hash") val pdfContentHash: String? = null
)

@Entity(
    tableName = "document_items_cache",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["document_id"]),
        Index(value = ["product_id"]),
        Index(value = ["user_id", "document_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["user_id", "id"],
            childColumns = ["user_id", "document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DocumentItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "product_id")
    val productId: String?,
    val name: String,
    val description: String?,
    val quantity: Int,
    @ColumnInfo(name = "unit_price")
    val unitPrice: BigDecimal,
    @ColumnInfo(name = "line_total")
    val lineTotal: BigDecimal,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int
)

@Entity(
    tableName = "sync_state",
    indices = [Index(value = ["user_id"])]
)
data class SyncStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "opaque_cursor")
    val opaqueCursor: String?,
    @ColumnInfo(name = "bootstrap_state")
    val bootstrapState: String,
    @ColumnInfo(name = "last_successful_sync")
    val lastSuccessfulSync: Long?,
    @ColumnInfo(name = "sync_schema_version")
    val syncSchemaVersion: Int
)

@Entity(
    tableName = "sync_outbox",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["status"]),
        Index(value = ["next_retry_at"]),
        Index(value = ["idempotency_key"], unique = true)
    ]
)
data class SyncOutboxEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    val operation: String,
    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String,
    @ColumnInfo(name = "base_server_revision")
    val baseServerRevision: String?,
    val status: String,
    val attempts: Int,
    @ColumnInfo(name = "next_retry_at")
    val nextRetryAt: Long,
    @ColumnInfo(name = "processing_started_at")
    val processingStartedAt: Long?,
    @ColumnInfo(name = "lock_expires_at")
    val lockExpiresAt: Long?,
    @ColumnInfo(name = "last_error")
    val lastError: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "deleted_minimal_payload")
    val deletedMinimalPayload: String?
)

@Entity(
    tableName = "offline_quota_lease",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "device_id", "period_month"], unique = true)
    ]
)
data class OfflineQuotaLeaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "plan_code")
    val planCode: String,
    @ColumnInfo(name = "period_month")
    val periodMonth: String,
    @ColumnInfo(name = "allowed_limit")
    val allowedLimit: Int,
    @ColumnInfo(name = "consumed_count")
    val consumedCount: Int,
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long,
    val status: String
)

@Entity(
    tableName = "local_usage_ledger",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "document_id"], unique = true),
        Index(value = ["user_id", "operation_id"], unique = true)
    ]
)
data class LocalUsageLedgerEntity(
    @PrimaryKey
    @ColumnInfo(name = "usage_event_id")
    val usageEventId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "operation_id")
    val operationId: String,
    @ColumnInfo(name = "lease_id")
    val leaseId: String,
    @ColumnInfo(name = "period_month")
    val periodMonth: String,
    val status: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long?
)

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
    val paymentMethod: String?,
    @ColumnInfo(name = "tax_rate", defaultValue = "0.0")
    val taxRate: Double = 0.0,
    @ColumnInfo(name = "tax_name", defaultValue = "Tax")
    val taxName: String = "Tax"
)

// Mapping extensions
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
        price = BigDecimal.valueOf(price),
        currency = currency,
        stockQuantity = stockQuantity,
        category = category,
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
        price = price.toDouble(),
        currency = currency,
        stockQuantity = stockQuantity,
        category = category,
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
        amountPaid = amountPaid?.let { BigDecimal.valueOf(it) },
        issueDate = issueDate,
        total = BigDecimal.valueOf(total),
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
        amountPaid = amountPaid?.toDouble(),
        issueDate = issueDate,
        total = total.toDouble(),
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
