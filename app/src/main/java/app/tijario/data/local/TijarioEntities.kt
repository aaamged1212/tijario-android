package app.tijario.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.CompleteDocument
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
    val address: String? = null,
    val email: String? = null,
    @ColumnInfo(name = "website_url")
    val websiteUrl: String? = null,
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
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    @ColumnInfo(name = "tax_name")
    val taxName: String? = null,
    @ColumnInfo(name = "tax_rate", defaultValue = "0.0")
    val taxRate: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "tax_amount", defaultValue = "0.0")
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "template_id")
    val templateId: String? = null,
    @ColumnInfo(name = "document_title")
    val documentTitle: String? = null,
    @ColumnInfo(name = "document_language", defaultValue = "ar")
    val documentLanguage: String = "ar",
    val total: BigDecimal,
    val currency: String,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,

    // V7 additions
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val discount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "discount_label")
    val discountLabel: String? = null,
    @ColumnInfo(name = "extra_fees")
    val extraFees: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "extra_fees_label")
    val extraFeesLabel: String? = null,
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
    tableName = "document_creation_events",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "document_id"], unique = true),
        Index(value = ["user_id", "operation_id"], unique = true)
    ]
)
data class DocumentCreationEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "operation_id")
    val operationId: String,
    @ColumnInfo(name = "installation_id")
    val installationId: String,
    @ColumnInfo(name = "lease_id")
    val leaseId: String?,
    @ColumnInfo(name = "plan_code")
    val planCode: String,
    @ColumnInfo(name = "quota_scope")
    val quotaScope: String,
    @ColumnInfo(name = "period_key")
    val periodKey: String,
    val status: String,
    @ColumnInfo(name = "created_at_client")
    val createdAtClient: Long,
    @ColumnInfo(name = "acknowledged_at_server")
    val acknowledgedAtServer: Long?,
    @ColumnInfo(name = "entitlement_version")
    val entitlementVersion: Long?,
    val source: String,
    @ColumnInfo(name = "migrated_baseline")
    val migratedBaseline: Boolean = false,
)

@Entity(tableName = "account_entitlements")
data class AccountEntitlementEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "plan_code")
    val planCode: String,
    @ColumnInfo(name = "data_mode")
    val dataMode: String,
    @ColumnInfo(name = "document_limit_scope")
    val documentLimitScope: String,
    @ColumnInfo(name = "document_limit")
    val documentLimit: Int?,
    @ColumnInfo(name = "documents_used")
    val documentsUsed: Int,
    @ColumnInfo(name = "customer_limit")
    val customerLimit: Int?,
    @ColumnInfo(name = "product_limit")
    val productLimit: Int?,
    @ColumnInfo(name = "allowed_template_ids_json")
    val allowedTemplateIdsJson: String,
    @ColumnInfo(name = "remove_tijario_branding")
    val removeTijarioBranding: Boolean,
    @ColumnInfo(name = "entitlement_version")
    val entitlementVersion: Long,
    @ColumnInfo(name = "verified_at")
    val verifiedAt: Long,
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long?,
    @ColumnInfo(name = "signed_payload")
    val signedPayload: String?,
    val signature: String?,
)

@Entity(tableName = "backup_settings")
data class BackupSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    val frequency: String,
    @ColumnInfo(name = "wifi_only")
    val wifiOnly: Boolean,
    @ColumnInfo(name = "charging_only")
    val chargingOnly: Boolean,
    @ColumnInfo(name = "drive_enabled")
    val driveEnabled: Boolean,
    @ColumnInfo(name = "retention_daily")
    val retentionDaily: Int,
    @ColumnInfo(name = "retention_weekly")
    val retentionWeekly: Int,
    @ColumnInfo(name = "retention_monthly")
    val retentionMonthly: Int,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "backup_records",
    indices = [Index(value = ["user_id"]), Index(value = ["created_at"])],
)
data class BackupRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "local_relative_path") val localRelativePath: String,
    @ColumnInfo(name = "format_version") val formatVersion: Int,
    val status: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    val checksum: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "uploaded_at") val uploadedAt: Long?,
    @ColumnInfo(name = "drive_file_id") val driveFileId: String?,
    @ColumnInfo(name = "last_error") val lastError: String?,
)

@Entity(
    tableName = "backup_file_entries",
    indices = [Index(value = ["backup_id"]), Index(value = ["backup_id", "relative_path"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = BackupRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["backup_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class BackupFileEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "backup_id") val backupId: String,
    @ColumnInfo(name = "relative_path") val relativePath: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    val checksum: String,
    val status: String,
)

@Entity(
    tableName = "device_bindings",
    primaryKeys = ["user_id", "installation_id"],
    indices = [Index(value = ["user_id"]), Index(value = ["installation_id"])],
)
data class DeviceBindingEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "installation_id") val installationId: String,
    @ColumnInfo(name = "device_name") val deviceName: String?,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean,
    val status: String,
    @ColumnInfo(name = "registered_at") val registeredAt: Long,
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: Long?,
    @ColumnInfo(name = "revoked_at") val revokedAt: Long?,
)

@Entity(
    tableName = "deleted_record_history",
    indices = [Index(value = ["user_id"]), Index(value = ["user_id", "entity_type", "entity_id"], unique = true)],
)
data class DeletedRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long,
    @ColumnInfo(name = "local_revision") val localRevision: Long,
    @ColumnInfo(name = "payload_json") val payloadJson: String?,
)

@Entity(
    tableName = "local_taxes",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalTaxEntity(
    val id: String,
    val name: String,
    val rate: Double,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_payment_methods",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalPaymentMethodEntity(
    val id: String,
    val name: String,
    val details: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_signatures",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalSignatureEntity(
    val id: String,
    val name: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_terms",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalTermsEntity(
    val id: String,
    val title: String,
    val content: String,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_document_metadata",
    primaryKeys = ["user_id", "documentId"],
    indices = [Index(value = ["user_id"])],
)
data class LocalDocumentMetadataEntity(
    val documentId: String,
    val currency: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String?,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?,
    @ColumnInfo(name = "tax_rate", defaultValue = "0.0")
    val taxRate: Double = 0.0,
    @ColumnInfo(name = "tax_name", defaultValue = "Tax")
    val taxName: String = "Tax",
    @ColumnInfo(name = "discount_type", defaultValue = "fixed")
    val discountType: String = "fixed",
    @ColumnInfo(name = "discount_value")
    val discountValue: String? = null,
    @ColumnInfo(name = "shipping_amount", defaultValue = "0.0")
    val shippingAmount: Double = 0.0,
    @ColumnInfo(name = "shipping_label")
    val shippingLabel: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
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
        address = address,
        email = email,
        websiteUrl = websiteUrl,
        currency = currency,
        logoUrl = logoUrl,
        instagramUrl = instagramUrl,
        invoiceNote = invoiceNote,
        termsText = termsText,
        syncedAt = syncedAt,
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
    )

fun BusinessSettingsEntity.toModel(): BusinessSettings =
    BusinessSettings(
        id = remoteId,
        userId = userId,
        businessName = businessName,
        whatsappNumber = whatsappNumber,
        country = country,
        city = city,
        address = address,
        email = email,
        websiteUrl = websiteUrl,
        currency = currency,
        logoUrl = logoUrl,
        instagramUrl = instagramUrl,
        invoiceNote = invoiceNote,
        termsText = termsText,
        updatedAt = serverRevision,
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
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
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
        updatedAt = serverRevision,
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
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
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
        updatedAt = serverRevision,
    )

fun DocumentSummary.toEntity(userId: String, syncedAt: Long = System.currentTimeMillis()): DocumentEntity =
    DocumentEntity(
        id = id,
        userId = userId,
        customerId = customerId,
        type = type.toCacheValue(),
        documentNumber = documentNumber,
        templateId = templateId,
        documentTitle = documentTitle,
        documentLanguage = documentLanguage,
        status = status,
        paymentStatus = paymentStatus,
        amountPaid = amountPaid?.let { BigDecimal.valueOf(it) },
        issueDate = issueDate,
        createdAt = createdAt,
        taxName = taxName,
        taxRate = BigDecimal.valueOf(taxRate),
        taxAmount = BigDecimal.valueOf(taxAmount),
        total = BigDecimal.valueOf(total),
        currency = currency,
        syncedAt = syncedAt,
        discountLabel = discountLabel,
        extraFeesLabel = extraFeesLabel,
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
    )

fun DocumentEntity.toModel(): DocumentSummary =
    DocumentSummary(
        id = id,
        customerId = customerId,
        type = type.toDocumentType(),
        documentNumber = documentNumber,
        documentTitle = documentTitle,
        documentLanguage = documentLanguage,
        status = status,
        paymentStatus = paymentStatus,
        amountPaid = amountPaid?.toDouble(),
        issueDate = issueDate,
        createdAt = createdAt,
        discountLabel = discountLabel,
        extraFeesLabel = extraFeesLabel,
        taxName = taxName,
        taxRate = taxRate.toDouble(),
        taxAmount = taxAmount.toDouble(),
        templateId = templateId,
        total = total.toDouble(),
        currency = currency,
        updatedAt = serverRevision,
    )

fun CompleteDocument.toEntity(userId: String, syncedAt: Long = System.currentTimeMillis()): DocumentEntity =
    DocumentEntity(
        id = id,
        userId = userId,
        customerId = customerId,
        type = type.toCacheValue(),
        documentNumber = documentNumber,
        templateId = templateId,
        documentTitle = documentTitle,
        documentLanguage = documentLanguage,
        status = status,
        paymentStatus = paymentStatus,
        amountPaid = amountPaid?.let { BigDecimal.valueOf(it) },
        issueDate = issueDate,
        createdAt = createdAt,
        taxName = taxName,
        taxRate = BigDecimal.valueOf(taxRate),
        taxAmount = BigDecimal.valueOf(taxAmount),
        total = BigDecimal.valueOf(total),
        currency = currency,
        syncedAt = syncedAt,
        subtotal = BigDecimal.valueOf(subtotal),
        discount = BigDecimal.valueOf(discount),
        discountLabel = discountLabel,
        extraFees = BigDecimal.valueOf(extraFees),
        extraFeesLabel = extraFeesLabel,
        notes = notes,
        termsText = termsText,
        syncStatus = "SYNCED",
        localRevision = 1,
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
        syncErrorCode = null,
        isDeleted = false,
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
