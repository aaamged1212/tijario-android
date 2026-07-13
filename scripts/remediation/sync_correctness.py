from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding="utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if new in text:
        return text
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one old block, found {count}")
    return text.replace(old, new, 1)


models_path = "app/src/main/java/app/tijario/data/model/TijarioModels.kt"
models = read(models_path)
for old, new, label in [
    (
        '''    @SerialName("terms_text") val termsText: String? = null,
)''',
        '''    @SerialName("terms_text") val termsText: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)''',
        "BusinessSettings updatedAt",
    ),
    (
        '''    val city: String? = null,
    val notes: String? = null,
)''',
        '''    val city: String? = null,
    val notes: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)''',
        "Customer updatedAt",
    ),
    (
        '''    @SerialName("stock_quantity") val stockQuantity: Int? = null,
    val category: String? = null,
)''',
        '''    @SerialName("stock_quantity") val stockQuantity: Int? = null,
    val category: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)''',
        "Product updatedAt",
    ),
    (
        '''    val total: Double,
    val currency: String,
)''',
        '''    val total: Double,
    val currency: String,
    @SerialName("updated_at") val updatedAt: String? = null,
)''',
        "DocumentSummary updatedAt",
    ),
    (
        '''    @SerialName("terms_text") val termsText: String? = null,
    val customer: Customer? = null,
    val items: List<DocumentItem> = emptyList(),
)''',
        '''    @SerialName("terms_text") val termsText: String? = null,
    val customer: Customer? = null,
    val items: List<DocumentItem> = emptyList(),
    @SerialName("updated_at") val updatedAt: String? = null,
)''',
        "CompleteDocument updatedAt",
    ),
]:
    models = replace_once(models, old, new, label)
write(models_path, models)


api_path = "app/src/main/java/app/tijario/data/remote/BackendApiClient.kt"
api = read(api_path)
api = replace_once(
    api,
    '''    private suspend inline fun <reified T> HttpResponse.decodeJsonResponseOrFallback(
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
    }''',
    '''    private suspend inline fun <reified T> HttpResponse.decodeJsonResponseOrFallback(
        crossinline fallback: suspend () -> T,
    ): T {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        val parsed = runCatching { apiJson.decodeFromString<T>(text) }.getOrNull()
        if (parsed != null) return parsed

        if (status.value in setOf(404, 405, 501)) return fallback()
        throw IllegalStateException(
            "Unexpected API response. status=${status.value}, ${responseDiagnostic(contentType, text)}",
        )
    }''',
    "AI V2 fallback policy",
)
if "bodyPreview=" in api:
    start = api.index("    private fun responseDiagnostic")
    end = api.index("\n    }", start) + len("\n    }")
    api = (
        api[:start]
        + '''    private fun responseDiagnostic(contentType: String, body: String): String =
        "contentType=${contentType.ifBlank { "<empty>" }}, bodyLength=${body.length}"
'''
        + api[end:]
    )
api = replace_once(
    api,
    '''    val status: String,
    val payment_status: String?,
    val issue_date: String,''',
    '''    val status: String,
    val payment_status: String?,
    val amount_paid: Double? = null,
    val issue_date: String,''',
    "Document amount_paid DTO",
)
api = replace_once(
    api,
    '''data class DocumentItemServerDto(
    val id: String,
    val document_id: String,
    val name: String,''',
    '''data class DocumentItemServerDto(
    val id: String,
    val document_id: String,
    val product_id: String? = null,
    val name: String,''',
    "Document item product_id DTO",
)
write(api_path, api)


entities_path = "app/src/main/java/app/tijario/data/local/TijarioEntities.kt"
entities = read(entities_path)
for old, new, label in [
    (
        '''        termsText = termsText,
        syncedAt = syncedAt,
    )''',
        '''        termsText = termsText,
        syncedAt = syncedAt,
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
    )''',
        "BusinessSettings entity revision",
    ),
    (
        '''        invoiceNote = invoiceNote,
        termsText = termsText,
    )''',
        '''        invoiceNote = invoiceNote,
        termsText = termsText,
        updatedAt = serverRevision,
    )''',
        "BusinessSettings model revision",
    ),
    (
        '''        city = city,
        notes = notes,
        syncedAt = syncedAt,
    )''',
        '''        city = city,
        notes = notes,
        syncedAt = syncedAt,
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
    )''',
        "Customer entity revision",
    ),
    (
        '''        whatsappNumber = whatsappNumber,
        city = city,
        notes = notes,
    )''',
        '''        whatsappNumber = whatsappNumber,
        city = city,
        notes = notes,
        updatedAt = serverRevision,
    )''',
        "Customer model revision",
    ),
    (
        '''        stockQuantity = stockQuantity,
        category = category,
        syncedAt = syncedAt,
    )''',
        '''        stockQuantity = stockQuantity,
        category = category,
        syncedAt = syncedAt,
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
    )''',
        "Product entity revision",
    ),
    (
        '''        currency = currency,
        stockQuantity = stockQuantity,
        category = category,
    )''',
        '''        currency = currency,
        stockQuantity = stockQuantity,
        category = category,
        updatedAt = serverRevision,
    )''',
        "Product model revision",
    ),
    (
        '''        syncedAt = syncedAt,
        discountLabel = discountLabel,
        extraFeesLabel = extraFeesLabel,
    )''',
        '''        syncedAt = syncedAt,
        discountLabel = discountLabel,
        extraFeesLabel = extraFeesLabel,
        syncStatus = "SYNCED",
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,
        lastSyncedAt = syncedAt,
    )''',
        "DocumentSummary entity revision",
    ),
    (
        '''        total = total.toDouble(),
        currency = currency,
    )''',
        '''        total = total.toDouble(),
        currency = currency,
        updatedAt = serverRevision,
    )''',
        "DocumentSummary model revision",
    ),
]:
    entities = replace_once(entities, old, new, label)
old_complete_revision = '''        syncStatus = "SYNCED",
        localRevision = 1,
        serverRevision = id,
        serverUpdatedAt = syncedAt,'''
new_complete_revision = '''        syncStatus = "SYNCED",
        localRevision = 1,
        serverRevision = updatedAt,
        serverUpdatedAt = syncedAt,'''
if old_complete_revision in entities:
    entities = entities.replace(old_complete_revision, new_complete_revision, 1)
elif new_complete_revision not in entities:
    raise RuntimeError("CompleteDocument revision mapping not found")
write(entities_path, entities)


repository_path = "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt"
repository = read(repository_path)
repository = repository.replace("serverRevision = remote.id,", "serverRevision = remote.updatedAt,", 3)
repository = repository.replace(
    '''                    serverRevision = remote.updatedAt,
                    serverUpdatedAt = syncedAt,
                    lastSyncedAt = syncedAt,''',
    '''                    serverRevision = remote.updatedAt,
                    serverUpdatedAt = parseServerInstantOrNull(remote.updatedAt.orEmpty())?.toEpochMilli() ?: syncedAt,
                    lastSyncedAt = syncedAt,''',
    3,
)
repository = repository.replace(
    '''                            serverRevision = document.id,
                            serverUpdatedAt = System.currentTimeMillis(),''',
    '''                            serverRevision = customer.updatedAt,
                            serverUpdatedAt = parseServerInstantOrNull(customer.updatedAt.orEmpty())?.toEpochMilli()
                                ?: System.currentTimeMillis(),''',
    1,
)
repository = replace_once(
    repository,
    '''            customer = customer,
            items = items,
        )''',
    '''            customer = customer,
            items = items,
            updatedAt = doc.serverRevision,
        )''',
    "Local complete-document revision",
)
repository = replace_once(
    repository,
    '''                val localDoc = dao.getDocument(userId, documentId)
                val localItems = dao.getDocumentItems(userId, documentId)
                if (localDoc != null && localItems.isNotEmpty()) {
                    return@withContext buildLocalCompleteDocument(userId, localDoc)
                }

                val remote = backendApiClient.fetchCompleteDocument(documentId)
                if (!remote.ok || remote.data == null) {
                    error(remote.message ?: remote.code ?: "document_not_found")
                }

                cacheCompleteDocumentSnapshot(remote.data)
                remote.data''',
    '''                val localDoc = dao.getDocument(userId, documentId)
                val localItems = dao.getDocumentItems(userId, documentId)
                val localSnapshot = localDoc?.takeIf { localItems.isNotEmpty() }
                val hasPendingLocalChanges = localSnapshot?.syncStatus?.let { it != "SYNCED" } == true
                val cacheIsFresh = localSnapshot?.lastSyncedAt?.let {
                    System.currentTimeMillis() - it < PULL_SYNC_TTL_MS
                } == true
                if (localSnapshot != null && (hasPendingLocalChanges || cacheIsFresh)) {
                    return@withContext buildLocalCompleteDocument(userId, localSnapshot)
                }

                val remote = runCatching { backendApiClient.fetchCompleteDocument(documentId) }.getOrNull()
                if (remote?.ok != true || remote.data == null) {
                    if (localSnapshot != null) return@withContext buildLocalCompleteDocument(userId, localSnapshot)
                    error(remote?.message ?: remote?.code ?: "document_not_found")
                }

                cacheCompleteDocumentSnapshot(remote.data)
                remote.data''',
    "Complete-document cache freshness",
)
repository = repository.replace(
    "amountPaid = null,",
    "amountPaid = item.amount_paid?.let { BigDecimal.valueOf(it) },",
    1,
)
repository = repository.replace("productId = null,", "productId = item.product_id,", 1)
repository = repository.replace(
    '''                                serverRevision = item.updated_at,
                                syncedAt = System.currentTimeMillis()''',
    '''                                serverRevision = item.updated_at,
                                serverUpdatedAt = parseServerInstantOrNull(item.updated_at)?.toEpochMilli(),
                                lastSyncedAt = System.currentTimeMillis(),
                                syncedAt = System.currentTimeMillis()''',
    4,
)
repository = repository.replace(
    'android.os.Build.MODEL + "_" + android.os.Build.ID',
    "AppPreferences.getInstallationId(context)",
)
repository = replace_once(
    repository,
    '''        val lease = dao.getLease(userId, deviceId, periodMonth)
        val available = when {
            lease != null && lease.expiresAt >= System.currentTimeMillis() ->
                lease.allowedLimit - lease.consumedCount - pendingLedgers
            else -> Int.MAX_VALUE
        }
        if (available <= 0) {
            throw IllegalStateException("QUOTA_LIMIT_EXCEEDED")
        }''',
    '''        val lease = dao.getLease(userId, deviceId, periodMonth)
            ?.takeIf { it.status == "ACTIVE" && it.expiresAt >= System.currentTimeMillis() }
            ?: throw IllegalStateException("OFFLINE_LEASE_REQUIRED")
        val available = lease.allowedLimit - lease.consumedCount - pendingLedgers
        if (available <= 0) {
            throw IllegalStateException("QUOTA_LIMIT_EXCEEDED")
        }''',
    "Offline lease requirement",
)
repository = repository.replace(
    'leaseId = lease?.id ?: "cached:$periodMonth",',
    "leaseId = lease.id,",
    1,
)
write(repository_path, repository)

print("sync correctness remediation applied")
