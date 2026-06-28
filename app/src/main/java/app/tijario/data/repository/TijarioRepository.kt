package app.tijario.data.repository

import android.content.Context
import java.io.File
import app.tijario.features.sync.SyncScheduler
import androidx.room.withTransaction
import app.tijario.config.AppPreferences
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.local.toEntity
import app.tijario.data.local.toModel
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import app.tijario.data.model.Product
import app.tijario.data.model.ProfileFullNameUpdateDto
import app.tijario.domain.DocumentNumbering
import app.tijario.data.remote.ApiResult
import app.tijario.data.remote.BackendApiClient
import app.tijario.data.remote.CreateDocumentRequest
import app.tijario.data.remote.CreateDocumentResponse
import app.tijario.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import app.tijario.data.remote.*
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

private val repositoryJson = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

data class CacheSyncState(
    val isRefreshing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
)

open class TijarioRepository(
    protected val context: Context,
    private val database: TijarioDatabase,
    private val supabaseClient: SupabaseClient,
    private val backendApiClient: BackendApiClient,
) {
    private val dao = database.tijarioDao()
    private val syncStateMutable = MutableStateFlow(CacheSyncState())
    private var lastFullRefreshUserId: String? = null
    private var lastFullRefreshAt: Long = 0L

    val syncState: StateFlow<CacheSyncState> = syncStateMutable.asStateFlow()

    fun observeBusinessSettings(userId: String): Flow<BusinessSettings?> =
        dao.observeBusinessSettings(userId).map { it?.toModel() }

    fun observeCustomers(userId: String): Flow<List<Customer>> =
        dao.observeCustomers(userId).map { rows ->
            rows.filter { !it.isDeleted }.map { it.toModel() }
        }

    fun observeProducts(userId: String): Flow<List<Product>> =
        dao.observeProducts(userId).map { rows ->
            rows.filter { !it.isDeleted }.map { it.toModel() }
        }

    fun observeDocuments(userId: String): Flow<List<DocumentSummary>> =
        combine(
            dao.observeDocuments(userId),
            dao.observeAllDocumentMetadata()
        ) { rows, metadataList ->
            val metadataMap = metadataList.associateBy { it.documentId }
            rows.filter { !it.isDeleted }.map { row ->
                val model = row.toModel()
                val meta = metadataMap[model.id]
                if (meta != null) {
                    model.copy(currency = meta.currency)
                } else {
                    model
                }
            }
        }

    fun observeDocument(userId: String, documentId: String): Flow<app.tijario.data.model.CompleteDocument?> =
        dao.observeDocument(userId, documentId).map { entity ->
            if (entity == null || entity.isDeleted) return@map null
            val customer = dao.getCustomer(userId, entity.customerId)?.toModel()
            val items = dao.getDocumentItems(userId, entity.id).map { item ->
                app.tijario.data.model.DocumentItem(
                    id = item.id,
                    documentId = item.documentId,
                    productId = item.productId,
                    name = item.name,
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice.toDouble()
                )
            }
            app.tijario.data.model.CompleteDocument(
                id = entity.id,
                userId = entity.userId,
                customerId = entity.customerId,
                type = if (entity.type == "quote") DocumentType.Quote else DocumentType.Invoice,
                documentNumber = entity.documentNumber,
                status = entity.status,
                paymentStatus = entity.paymentStatus,
                amountPaid = entity.amountPaid?.toDouble(),
                issueDate = entity.issueDate,
                subtotal = entity.subtotal.toDouble(),
                discount = entity.discount.toDouble(),
                extraFees = entity.extraFees.toDouble(),
                total = entity.total.toDouble(),
                currency = entity.currency,
                notes = entity.notes,
                termsText = entity.termsText,
                customer = customer,
                items = items
            )
        }

    open suspend fun currentUserId(): String? =
        supabaseClient.auth.currentUserOrNull()?.id

    suspend fun fetchCurrentProfileFullName(): Result<String?> = runCatching {
        val userId = requireUserId()
        withContext(Dispatchers.IO) {
            supabaseClient.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeList<app.tijario.data.model.ProfileRowDto>()
                .firstOrNull()
                ?.fullName
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    }

    suspend fun updateCurrentProfileFullName(fullName: String): Result<Unit> = runCatching {
        val normalizedName = fullName.trim()
        require(normalizedName.length in 2..80) {
            "Invalid display name"
        }

        val user = supabaseClient.auth.currentUserOrNull() ?: error("No authenticated user")
        withContext(Dispatchers.IO) {
            supabaseClient.from("profiles").update(ProfileFullNameUpdateDto(normalizedName)) {
                filter { eq("id", user.id) }
            }
        }

        supabaseClient.auth.updateUser {
            data = buildJsonObject {
                put("full_name", normalizedName)
                put("name", normalizedName)
            }
        }
    }

    suspend fun syncCurrentProfileFullNameFromMetadata(): Result<Unit> = runCatching {
        val user = supabaseClient.auth.currentUserOrNull() ?: error("No authenticated user")
        val currentProfileName = fetchCurrentProfileFullName().getOrNull().orEmpty()
        if (currentProfileName.isNotBlank()) return@runCatching

        val resolvedName = listOfNotNull(
            user.userMetadata?.get("full_name")?.toString(),
            user.userMetadata?.get("name")?.toString(),
            user.userMetadata?.get("preferred_username")?.toString(),
        )
            .map { it.replace("\"", "").trim() }
            .firstOrNull { it.isNotBlank() }
            ?: return@runCatching

        updateCurrentProfileFullName(resolvedName).getOrThrow()
    }

    suspend fun hasCachedBusinessSettings(userId: String): Boolean =
        withContext(Dispatchers.IO) {
            dao.getBusinessSettings(userId) != null
        }

    // Outbox Compaction Logic
    private suspend fun enqueueOutbox(
        userId: String,
        entityType: String,
        entityId: String,
        operation: String,
        baseServerRevision: String? = null
    ) {
        val idempotencyKey = java.util.UUID.randomUUID().toString()
        val pending = dao.getPendingOutbox(userId).filter {
            it.entityType == entityType && it.entityId == entityId && it.status == "PENDING"
        }

        if (pending.isEmpty()) {
            val entry = app.tijario.data.local.SyncOutboxEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                idempotencyKey = idempotencyKey,
                baseServerRevision = baseServerRevision,
                status = "PENDING",
                attempts = 0,
                nextRetryAt = 0L,
                processingStartedAt = null,
                lockExpiresAt = null,
                lastError = null,
                createdAt = System.currentTimeMillis(),
                deletedMinimalPayload = null
            )
            dao.upsertOutbox(entry)
            return
        }

        val first = pending.first()
        when {
            first.operation == "CREATE" && (operation == "CREATE" || operation == "UPDATE") -> {
                // CREATE + CREATE or CREATE + UPDATE -> Keep CREATE
            }
            first.operation == "UPDATE" && operation == "UPDATE" -> {
                // UPDATE + UPDATE -> Keep first UPDATE
            }
            first.operation == "UPDATE" && operation == "DELETE" -> {
                // UPDATE + DELETE -> Convert to DELETE
                dao.deleteOutbox(first.id)
                val entry = app.tijario.data.local.SyncOutboxEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    entityType = entityType,
                    entityId = entityId,
                    operation = "DELETE",
                    idempotencyKey = idempotencyKey,
                    baseServerRevision = first.baseServerRevision ?: baseServerRevision,
                    status = "PENDING",
                    attempts = 0,
                    nextRetryAt = 0L,
                    processingStartedAt = null,
                    lockExpiresAt = null,
                    lastError = null,
                    createdAt = System.currentTimeMillis(),
                    deletedMinimalPayload = null
                )
                dao.upsertOutbox(entry)
            }
            first.operation == "CREATE" && operation == "DELETE" -> {
                // CREATE + DELETE -> Cancel both
                dao.deleteOutbox(first.id)
            }
            else -> {
                val entry = app.tijario.data.local.SyncOutboxEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    entityType = entityType,
                    entityId = entityId,
                    operation = operation,
                    idempotencyKey = idempotencyKey,
                    baseServerRevision = baseServerRevision,
                    status = "PENDING",
                    attempts = 0,
                    nextRetryAt = 0L,
                    processingStartedAt = null,
                    lockExpiresAt = null,
                    lastError = null,
                    createdAt = System.currentTimeMillis(),
                    deletedMinimalPayload = null
                )
                dao.upsertOutbox(entry)
            }
        }
        SyncScheduler(context).triggerSync(userId)
    }

    // Remote Cache Ingestion Policy
    private suspend fun ingestRemoteCustomers(userId: String, remoteCustomers: List<Customer>, syncedAt: Long) {
        val entitiesToUpsert = mutableListOf<app.tijario.data.local.CustomerEntity>()
        for (remote in remoteCustomers) {
            val id = remote.id ?: continue
            val existing = dao.getCustomer(userId, id)
            if (existing != null && existing.syncStatus in listOf("LOCAL_ONLY", "PENDING_SYNC", "PENDING_DELETE", "CONFLICT")) {
                continue
            }
            entitiesToUpsert.add(
                app.tijario.data.local.CustomerEntity(
                    id = id,
                    userId = userId,
                    name = remote.name,
                    whatsappNumber = remote.whatsappNumber,
                    city = remote.city,
                    notes = remote.notes,
                    syncedAt = syncedAt,
                    syncStatus = "SYNCED",
                    localRevision = existing?.localRevision ?: 1,
                    serverRevision = remote.id,
                    serverUpdatedAt = syncedAt,
                    lastSyncedAt = syncedAt,
                    syncErrorCode = null,
                    isDeleted = false
                )
            )
        }
        if (entitiesToUpsert.isNotEmpty()) {
            dao.upsertCustomers(entitiesToUpsert)
        }
    }

    private suspend fun ingestRemoteProducts(userId: String, remoteProducts: List<Product>, syncedAt: Long) {
        val entitiesToUpsert = mutableListOf<app.tijario.data.local.ProductEntity>()
        for (remote in remoteProducts) {
            val id = remote.id ?: continue
            val existing = dao.getProduct(userId, id)
            if (existing != null && existing.syncStatus in listOf("LOCAL_ONLY", "PENDING_SYNC", "PENDING_DELETE", "CONFLICT")) {
                continue
            }
            entitiesToUpsert.add(
                app.tijario.data.local.ProductEntity(
                    id = id,
                    userId = userId,
                    kind = when (remote.kind) {
                        app.tijario.data.model.ProductKind.Product -> "product"
                        app.tijario.data.model.ProductKind.Service -> "service"
                    },
                    name = remote.name,
                    description = remote.description,
                    price = BigDecimal.valueOf(remote.price),
                    currency = remote.currency,
                    stockQuantity = remote.stockQuantity,
                    syncedAt = syncedAt,
                    syncStatus = "SYNCED",
                    localRevision = existing?.localRevision ?: 1,
                    serverRevision = remote.id,
                    serverUpdatedAt = syncedAt,
                    lastSyncedAt = syncedAt,
                    syncErrorCode = null,
                    isDeleted = false
                )
            )
        }
        if (entitiesToUpsert.isNotEmpty()) {
            dao.upsertProducts(entitiesToUpsert)
        }
    }

    private suspend fun ingestRemoteDocuments(userId: String, remoteDocs: List<DocumentSummary>, syncedAt: Long) {
        val entitiesToUpsert = mutableListOf<app.tijario.data.local.DocumentEntity>()
        for (remote in remoteDocs) {
            val existing = dao.getDocument(userId, remote.id)
            if (existing != null && existing.syncStatus in listOf("LOCAL_ONLY", "PENDING_SYNC", "PENDING_DELETE", "CONFLICT")) {
                continue
            }
            entitiesToUpsert.add(
                app.tijario.data.local.DocumentEntity(
                    id = remote.id,
                    userId = userId,
                    customerId = remote.customerId,
                    type = when (remote.type) {
                        DocumentType.Invoice -> "invoice"
                        DocumentType.Quote -> "quote"
                    },
                    documentNumber = remote.documentNumber,
                    status = remote.status,
                    paymentStatus = remote.paymentStatus,
                    amountPaid = remote.amountPaid?.let { BigDecimal.valueOf(it) },
                    issueDate = remote.issueDate,
                    total = BigDecimal.valueOf(remote.total),
                    currency = remote.currency,
                    syncedAt = syncedAt,
                    subtotal = existing?.subtotal ?: BigDecimal.ZERO,
                    discount = existing?.discount ?: BigDecimal.ZERO,
                    extraFees = existing?.extraFees ?: BigDecimal.ZERO,
                    notes = existing?.notes,
                    termsText = existing?.termsText,
                    syncStatus = "SYNCED",
                    localRevision = existing?.localRevision ?: 1,
                    serverRevision = remote.id,
                    serverUpdatedAt = syncedAt,
                    lastSyncedAt = syncedAt,
                    syncErrorCode = null,
                    isDeleted = false,
                    localPdfRelativePath = existing?.localPdfRelativePath,
                    pdfGeneratedAt = existing?.pdfGeneratedAt,
                    pdfDocumentRevision = existing?.pdfDocumentRevision,
                    pdfContentHash = existing?.pdfContentHash
                )
            )
        }
        if (entitiesToUpsert.isNotEmpty()) {
            dao.upsertDocuments(entitiesToUpsert)
        }
    }

    // Refresh Flows
    suspend fun refreshAll(force: Boolean = false): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            val now = System.currentTimeMillis()
            if (!force && lastFullRefreshUserId == userId && now - lastFullRefreshAt < FULL_REFRESH_THROTTLE_MS) {
                return@runCatching
            }

            setRefreshing(true, null)
            val snapshot = fetchRemoteSnapshot(userId)
            val syncedAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    if (snapshot.businessSettings == null) {
                        val existing = dao.getBusinessSettings(userId)
                        if (existing?.syncStatus == "SYNCED") {
                            dao.deleteBusinessSettings(userId)
                        }
                    } else {
                        val existing = dao.getBusinessSettings(userId)
                        if (existing == null || existing.syncStatus !in listOf("LOCAL_ONLY", "PENDING_SYNC", "PENDING_DELETE", "CONFLICT")) {
                            dao.upsertBusinessSettings(snapshot.businessSettings.toEntity(userId, syncedAt).copy(syncStatus = "SYNCED"))
                        }
                    }

                    // Ingest remote data safely without overwriting pending local changes
                    ingestRemoteCustomers(userId, snapshot.customers, syncedAt)
                    ingestRemoteProducts(userId, snapshot.products, syncedAt)
                    ingestRemoteDocuments(userId, snapshot.documents, syncedAt)

                    // Prune local SYNCED caches that are no longer on remote
                    val remoteCustomerIds = snapshot.customers.mapNotNull { it.id }.toSet()
                    dao.observeCustomers(userId).first().forEach { local ->
                        if (local.syncStatus == "SYNCED" && local.id !in remoteCustomerIds) {
                            dao.deleteCustomer(userId, local.id)
                        }
                    }

                    val remoteProductIds = snapshot.products.mapNotNull { it.id }.toSet()
                    dao.observeProducts(userId).first().forEach { local ->
                        if (local.syncStatus == "SYNCED" && local.id !in remoteProductIds) {
                            dao.deleteProduct(userId, local.id)
                        }
                    }

                    val remoteDocIds = snapshot.documents.map { it.id }.toSet()
                    dao.observeDocuments(userId).first().forEach { local ->
                        if (local.syncStatus == "SYNCED" && local.id !in remoteDocIds) {
                            dao.deleteDocument(userId, local.id)
                        }
                    }
                }
            }
            lastFullRefreshUserId = userId
            lastFullRefreshAt = syncedAt
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
            SyncScheduler(context).triggerSync(userId)
        }.onFailure { error ->
            setRefreshing(false, error.message ?: "تعذر تحديث البيانات الآن.")
        }

    suspend fun refreshBusinessSettings(force: Boolean = true): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            setRefreshing(true, null)
            val settings = fetchBusinessSettings(userId)
            val syncedAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                if (settings == null) {
                    val existing = dao.getBusinessSettings(userId)
                    if (existing?.syncStatus == "SYNCED") {
                        dao.deleteBusinessSettings(userId)
                    }
                } else {
                    val existing = dao.getBusinessSettings(userId)
                    if (existing == null || existing.syncStatus !in listOf("LOCAL_ONLY", "PENDING_SYNC", "PENDING_DELETE", "CONFLICT")) {
                        dao.upsertBusinessSettings(settings.toEntity(userId, syncedAt).copy(syncStatus = "SYNCED"))
                    }
                }
            }
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
        }.onFailure { error ->
            setRefreshing(false, error.message ?: "تعذر تحديث إعدادات المتجر الآن.")
        }

    suspend fun refreshCustomers(): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            setRefreshing(true, null)
            val customers = fetchCustomers(userId)
            val syncedAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    ingestRemoteCustomers(userId, customers, syncedAt)
                }
            }
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
        }.onFailure { error ->
            setRefreshing(false, error.message ?: "تعذر تحديث العملاء الآن.")
        }

    suspend fun refreshProducts(): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            setRefreshing(true, null)
            val products = fetchProducts(userId)
            val syncedAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    ingestRemoteProducts(userId, products, syncedAt)
                }
            }
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
        }.onFailure { error ->
            setRefreshing(false, error.message ?: "تعذر تحديث المنتجات الآن.")
        }

    suspend fun refreshDocuments(): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            setRefreshing(true, null)
            val documents = fetchDocuments(userId)
            val syncedAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    ingestRemoteDocuments(userId, documents, syncedAt)
                }
            }
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
        }.onFailure { error ->
            setRefreshing(false, error.message ?: "تعذر تحديث المستندات الآن.")
        }

    // Local Customer CRUD
    suspend fun createCustomerLocal(customer: Customer): Result<Customer> = runCatching {
        val userId = requireUserId()
        val generatedId = customer.id ?: java.util.UUID.randomUUID().toString()
        val localCustomer = customer.copy(id = generatedId, userId = userId)
        val entity = app.tijario.data.local.CustomerEntity(
            id = generatedId,
            userId = userId,
            name = localCustomer.name,
            whatsappNumber = localCustomer.whatsappNumber,
            city = localCustomer.city,
            notes = localCustomer.notes,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 1,
            serverRevision = null,
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )
        withContext(Dispatchers.IO) {
            database.withTransaction {
                dao.upsertCustomer(entity)
                enqueueOutbox(userId, "customer", generatedId, "CREATE")
            }
        }
        localCustomer
    }

    suspend fun updateCustomerLocal(customer: Customer): Result<Customer> = runCatching {
        val userId = requireUserId()
        val customerId = customer.id ?: error("Customer ID required for update")
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val existing = dao.getCustomer(userId, customerId) ?: error("Customer not found locally")
                val nextRev = existing.localRevision + 1
                val nextStatus = if (existing.syncStatus == "LOCAL_ONLY") "LOCAL_ONLY" else "PENDING_SYNC"
                val entity = existing.copy(
                    name = customer.name,
                    whatsappNumber = customer.whatsappNumber,
                    city = customer.city,
                    notes = customer.notes,
                    localRevision = nextRev,
                    syncStatus = nextStatus
                )
                dao.upsertCustomer(entity)
                val outboxOp = if (existing.syncStatus == "LOCAL_ONLY") "CREATE" else "UPDATE"
                enqueueOutbox(userId, "customer", customerId, outboxOp, existing.serverRevision)
            }
        }
        customer.copy(userId = userId)
    }

    suspend fun deleteCustomerLocal(customerId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val docCount = dao.countDocumentsForCustomer(customerId)
                if (docCount > 0) {
                    throw IllegalStateException("لا يمكن حذف العميل لوجود مستندات تاريخية مرتبطة به.")
                }
                val existing = dao.getCustomer(userId, customerId) ?: error("Customer not found locally")
                if (existing.syncStatus == "LOCAL_ONLY") {
                    dao.deleteCustomer(userId, customerId)
                    enqueueOutbox(userId, "customer", customerId, "DELETE")
                } else {
                    val nextRev = existing.localRevision + 1
                    val entity = existing.copy(
                        isDeleted = true,
                        localRevision = nextRev,
                        syncStatus = "PENDING_DELETE"
                    )
                    dao.upsertCustomer(entity)
                    enqueueOutbox(userId, "customer", customerId, "DELETE", existing.serverRevision)
                }
            }
        }
    }

    // Local Product CRUD
    suspend fun createProductLocal(product: Product): Result<Product> = runCatching {
        val userId = requireUserId()
        val generatedId = product.id ?: java.util.UUID.randomUUID().toString()
        val localProduct = product.copy(id = generatedId, userId = userId)
        val entity = app.tijario.data.local.ProductEntity(
            id = generatedId,
            userId = userId,
            kind = when (localProduct.kind) {
                app.tijario.data.model.ProductKind.Product -> "product"
                app.tijario.data.model.ProductKind.Service -> "service"
            },
            name = localProduct.name,
            description = localProduct.description,
            price = BigDecimal.valueOf(localProduct.price),
            currency = localProduct.currency,
            stockQuantity = localProduct.stockQuantity,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 1,
            serverRevision = null,
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )
        withContext(Dispatchers.IO) {
            database.withTransaction {
                dao.upsertProduct(entity)
                enqueueOutbox(userId, "product", generatedId, "CREATE")
            }
        }
        localProduct
    }

    suspend fun updateProductLocal(product: Product): Result<Product> = runCatching {
        val userId = requireUserId()
        val productId = product.id ?: error("Product ID required for update")
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val existing = dao.getProduct(userId, productId) ?: error("Product not found locally")
                val nextRev = existing.localRevision + 1
                val nextStatus = if (existing.syncStatus == "LOCAL_ONLY") "LOCAL_ONLY" else "PENDING_SYNC"
                val entity = existing.copy(
                    kind = when (product.kind) {
                        app.tijario.data.model.ProductKind.Product -> "product"
                        app.tijario.data.model.ProductKind.Service -> "service"
                    },
                    name = product.name,
                    description = product.description,
                    price = BigDecimal.valueOf(product.price),
                    currency = product.currency,
                    stockQuantity = product.stockQuantity,
                    localRevision = nextRev,
                    syncStatus = nextStatus
                )
                dao.upsertProduct(entity)
                val outboxOp = if (existing.syncStatus == "LOCAL_ONLY") "CREATE" else "UPDATE"
                enqueueOutbox(userId, "product", productId, outboxOp, existing.serverRevision)
            }
        }
        product.copy(userId = userId)
    }

    suspend fun deleteProductLocal(productId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val itemsUsage = dao.countDocumentItemsForProduct(productId)
                if (itemsUsage > 0) {
                    throw IllegalStateException("لا يمكن حذف المنتج لوجوده في مستندات حالية.")
                }
                val existing = dao.getProduct(userId, productId) ?: error("Product not found locally")
                if (existing.syncStatus == "LOCAL_ONLY") {
                    dao.deleteProduct(userId, productId)
                    enqueueOutbox(userId, "product", productId, "DELETE")
                } else {
                    val nextRev = existing.localRevision + 1
                    val entity = existing.copy(
                        isDeleted = true,
                        localRevision = nextRev,
                        syncStatus = "PENDING_DELETE"
                    )
                    dao.upsertProduct(entity)
                    enqueueOutbox(userId, "product", productId, "DELETE", existing.serverRevision)
                }
            }
        }
    }

    // Local Document CRUD (Atomic transactions with items)
    suspend fun createDocumentLocal(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        return try {
            val userId = requireUserId()
            val docId = java.util.UUID.randomUUID().toString()
            val dateStr = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existingDocs = dao.observeDocuments(userId).first()

            // Resolve or Create Customer inline
            val customerEntity = dao.getCustomerByWhatsapp(userId, request.customer.whatsappNumber) ?: run {
                val newCustId = java.util.UUID.randomUUID().toString()
                val newCust = app.tijario.data.local.CustomerEntity(
                    id = newCustId,
                    userId = userId,
                    name = request.customer.name,
                    whatsappNumber = request.customer.whatsappNumber,
                    city = request.customer.city,
                    notes = null,
                    syncedAt = 0L,
                    syncStatus = "LOCAL_ONLY",
                    localRevision = 1,
                    serverRevision = null,
                    serverUpdatedAt = null,
                    lastSyncedAt = null,
                    syncErrorCode = null,
                    isDeleted = false
                )
                dao.upsertCustomer(newCust)
                enqueueOutbox(userId, "customer", newCustId, "CREATE")
                newCust
            }

            // Calculate Totals using BigDecimal
            var subtotal = BigDecimal.ZERO
            val itemsEntities = request.items.mapIndexed { index, item ->
                val lineTotal = BigDecimal.valueOf(item.quantity.toLong()).multiply(BigDecimal.valueOf(item.unitPrice))
                subtotal = subtotal.add(lineTotal)
                app.tijario.data.local.DocumentItemEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    documentId = docId,
                    productId = item.productId,
                    name = item.name,
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = BigDecimal.valueOf(item.unitPrice),
                    lineTotal = lineTotal,
                    sortOrder = index
                )
            }

            val discountBig = BigDecimal.valueOf(request.discount)
            val extraFeesBig = BigDecimal.valueOf(request.extraFees)
            val total = subtotal.subtract(discountBig).add(extraFeesBig)

            val docNum = DocumentNumbering.nextDocumentNumber(
                existingDocs.map { it.documentNumber },
                request.type
            )

            val docEntity = app.tijario.data.local.DocumentEntity(
                id = docId,
                userId = userId,
                customerId = customerEntity.id,
                type = when (request.type) {
                    DocumentType.Invoice -> "invoice"
                    DocumentType.Quote -> "quote"
                },
                documentNumber = docNum,
                status = "draft",
                paymentStatus = request.paymentStatus,
                amountPaid = request.amountPaid?.let { BigDecimal.valueOf(it) },
                issueDate = dateStr,
                total = total,
                currency = request.currency ?: "SAR",
                syncedAt = 0L,
                subtotal = subtotal,
                discount = discountBig,
                extraFees = extraFeesBig,
                notes = request.notes,
                termsText = request.termsText,
                syncStatus = "LOCAL_ONLY",
                localRevision = 1,
                serverRevision = null,
                serverUpdatedAt = null,
                lastSyncedAt = null,
                syncErrorCode = null,
                isDeleted = false
            )

            withContext(Dispatchers.IO) {
                database.withTransaction {
                    dao.upsertDocument(docEntity)
                    dao.insertDocumentItems(itemsEntities)
                    reserveDocumentQuotaLedger(userId, docId)
                    enqueueOutbox(userId, "document", docId, "CREATE")
                }
            }

            ApiResult(ok = true, data = CreateDocumentResponse(documentId = docId, documentNumber = docNum))
        } catch (e: Exception) {
            ApiResult(ok = false, message = e.message ?: "Failed to save document locally.")
        }
    }

    suspend fun updateDocumentLocal(documentId: String, request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        return try {
            val userId = requireUserId()
            val existing = dao.getDocument(userId, documentId) ?: error("Document not found locally")

            // Calculate Totals using BigDecimal
            var subtotal = BigDecimal.ZERO
            val itemsEntities = request.items.mapIndexed { index, item ->
                val lineTotal = BigDecimal.valueOf(item.quantity.toLong()).multiply(BigDecimal.valueOf(item.unitPrice))
                subtotal = subtotal.add(lineTotal)
                app.tijario.data.local.DocumentItemEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    documentId = documentId,
                    productId = item.productId,
                    name = item.name,
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = BigDecimal.valueOf(item.unitPrice),
                    lineTotal = lineTotal,
                    sortOrder = index
                )
            }

            val discountBig = BigDecimal.valueOf(request.discount)
            val extraFeesBig = BigDecimal.valueOf(request.extraFees)
            val total = subtotal.subtract(discountBig).add(extraFeesBig)

            val nextRev = existing.localRevision + 1
            val nextStatus = if (existing.syncStatus == "LOCAL_ONLY") "LOCAL_ONLY" else "PENDING_SYNC"

            val docEntity = existing.copy(
                paymentStatus = request.paymentStatus,
                amountPaid = request.amountPaid?.let { BigDecimal.valueOf(it) },
                total = total,
                subtotal = subtotal,
                discount = discountBig,
                extraFees = extraFeesBig,
                notes = request.notes,
                termsText = request.termsText,
                localRevision = nextRev,
                syncStatus = nextStatus,
                // Invalidate PDF metadata since document content changed
                localPdfRelativePath = null,
                pdfGeneratedAt = null,
                pdfDocumentRevision = null,
                pdfContentHash = null
            )

            withContext(Dispatchers.IO) {
                database.withTransaction {
                    // Replace all document items atomically
                    dao.deleteDocumentItems(userId, documentId)
                    dao.insertDocumentItems(itemsEntities)
                    dao.upsertDocument(docEntity)

                    val outboxOp = if (existing.syncStatus == "LOCAL_ONLY") "CREATE" else "UPDATE"
                    enqueueOutbox(userId, "document", documentId, outboxOp, existing.serverRevision)
                }
            }

            ApiResult(ok = true, data = CreateDocumentResponse(documentId = documentId, documentNumber = existing.documentNumber))
        } catch (e: Exception) {
            ApiResult(ok = false, message = e.message ?: "Failed to update document locally.")
        }
    }

    suspend fun deleteDocumentLocal(documentId: String): ApiResult<CreateDocumentResponse> {
        return try {
            val userId = requireUserId()
            val existing = dao.getDocument(userId, documentId) ?: error("Document not found locally")
            val hasLedger = dao.getLedgerByDocId(userId, documentId) != null
            val hasPdf = existing.localPdfRelativePath != null
            val mustSoftDelete = hasLedger || hasPdf

            withContext(Dispatchers.IO) {
                database.withTransaction {
                    if (existing.syncStatus == "LOCAL_ONLY" && !mustSoftDelete) {
                        dao.deleteDocumentItems(userId, documentId)
                        dao.deleteDocument(userId, documentId)
                        enqueueOutbox(userId, "document", documentId, "DELETE")
                    } else {
                        val nextRev = existing.localRevision + 1
                        val entity = existing.copy(
                            isDeleted = true,
                            localRevision = nextRev,
                            syncStatus = "PENDING_DELETE"
                        )
                        dao.upsertDocument(entity)
                        enqueueOutbox(userId, "document", documentId, "DELETE", existing.serverRevision)
                    }
                }
            }

            ApiResult(ok = true, data = CreateDocumentResponse(documentId = documentId, documentNumber = existing.documentNumber))
        } catch (e: Exception) {
            ApiResult(ok = false, message = e.message ?: "Failed to delete document locally.")
        }
    }

    // Local Business Settings update
    suspend fun updateBusinessSettingsLocal(settings: BusinessSettings): Result<Unit> = runCatching {
        val userId = requireUserId()
        val syncedAt = System.currentTimeMillis()
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val existing = dao.getBusinessSettings(userId)
                val nextRev = (existing?.localRevision ?: 0) + 1
                val nextStatus = if (existing?.syncStatus == "LOCAL_ONLY") "LOCAL_ONLY" else "PENDING_SYNC"
                val entity = app.tijario.data.local.BusinessSettingsEntity(
                    userId = userId,
                    remoteId = existing?.remoteId ?: settings.id,
                    businessName = settings.businessName,
                    whatsappNumber = settings.whatsappNumber,
                    country = settings.country,
                    city = settings.city,
                    currency = settings.currency,
                    logoUrl = settings.logoUrl,
                    instagramUrl = settings.instagramUrl,
                    invoiceNote = settings.invoiceNote,
                    termsText = settings.termsText,
                    syncedAt = syncedAt,
                    localRevision = nextRev,
                    syncStatus = nextStatus
                )
                dao.upsertBusinessSettings(entity)
                enqueueOutbox(userId, "business_settings", userId, "UPDATE", existing?.serverId())
            }
        }
        sync(userId).onFailure {
            SyncScheduler(context).triggerSync(userId)
        }
    }

    private fun app.tijario.data.local.BusinessSettingsEntity.serverId(): String? =
        remoteId

    // Legacy Save / Cache adapters for backward compatibility
    suspend fun createCustomer(customer: Customer): Result<Unit> = runCatching {
        createCustomerLocal(customer).getOrThrow()
    }

    suspend fun updateCustomer(customer: Customer): Result<Unit> = runCatching {
        updateCustomerLocal(customer).getOrThrow()
    }

    suspend fun deleteCustomer(customerId: String): Result<Unit> = runCatching {
        deleteCustomerLocal(customerId).getOrThrow()
    }

    suspend fun createProduct(product: Product): Result<Unit> = runCatching {
        createProductLocal(product).getOrThrow()
    }

    suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        updateProductLocal(product).getOrThrow()
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        deleteProductLocal(productId).getOrThrow()
    }

    suspend fun saveBusinessSettings(settings: BusinessSettings): Result<Unit> = runCatching {
        updateBusinessSettingsLocal(settings).getOrThrow()
        val userId = currentUserId()
        if (userId != null) {
            sync(userId).getOrThrow()
        }
    }

    suspend fun cacheBusinessSettings(settings: BusinessSettings): Result<Unit> = runCatching {
        val userId = settings.userId ?: requireUserId()
        val syncedAt = System.currentTimeMillis()
        withContext(Dispatchers.IO) {
            dao.upsertBusinessSettings(settings.toEntity(userId, syncedAt))
        }
        syncStateMutable.value = syncStateMutable.value.copy(lastSyncedAt = syncedAt, errorMessage = null)
    }

    // Legacy Document Remote Bridges
    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        val result = createDocumentLocal(request)
        return result
    }

    suspend fun updateDocument(documentId: String, request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        val result = updateDocumentLocal(documentId, request)
        return result
    }

    suspend fun deleteDocument(documentId: String): ApiResult<CreateDocumentResponse> {
        val result = deleteDocumentLocal(documentId)
        return result
    }

    // Plans and usage
    suspend fun fetchUserPlanUsage(): Result<app.tijario.data.model.UserPlanUsage> =
        runCatching {
            val userId = requireUserId()
            val periodMonth = currentUtcPeriodMonth()
            withContext(Dispatchers.IO) {
                val userPlan = supabaseClient.from("user_plan")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<app.tijario.data.model.UserPlanRowDto>()
                    .firstOrNull()
                    ?: error("User plan not found.")

                val plan = supabaseClient.from("plans")
                    .select {
                        filter {
                            eq("id", userPlan.planId)
                        }
                    }
                    .decodeList<app.tijario.data.model.Plan>()
                    .firstOrNull()
                    ?: error("Plan not found.")

                val usage = supabaseClient.from("usage_counters")
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("period_month", periodMonth)
                        }
                    }
                    .decodeList<app.tijario.data.model.UsageCounterRowDto>()
                    .firstOrNull()

                val baseUsage = app.tijario.data.model.UserPlanUsage(
                    planCode = plan.code,
                    planName = plan.name,
                    periodMonth = periodMonth,
                    documentsUsed = usage?.documentsUsed ?: 0,
                    documentsLimit = plan.monthlyDocumentLimit,
                    aiUsed = usage?.aiUsed ?: 0,
                    aiLimit = plan.monthlyAiLimit
                )
                val effectiveUsage = overlayLocalUsage(userId, baseUsage)
                AppPreferences.setPlanUsage(context, userId, baseUsage)
                effectiveUsage
            }
        }

    suspend fun getCachedPlanUsage(userId: String): app.tijario.data.model.UserPlanUsage? =
        withContext(Dispatchers.IO) {
            AppPreferences.getPlanUsage(context, userId)?.let { overlayLocalUsage(userId, it) }
        }

    suspend fun fetchCompleteDocument(documentId: String): Result<app.tijario.data.model.CompleteDocument> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                val doc = dao.getDocument(userId, documentId) ?: error("Document not found locally")
                val customer = dao.getCustomer(userId, doc.customerId)?.toModel()
                val items = dao.getDocumentItems(userId, documentId).map { item ->
                    app.tijario.data.model.DocumentItem(
                        id = item.id,
                        documentId = item.documentId,
                        productId = item.productId,
                        name = item.name,
                        description = item.description,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice.toDouble()
                    )
                }

                app.tijario.data.model.CompleteDocument(
                    id = doc.id,
                    userId = doc.userId,
                    customerId = doc.customerId,
                    type = if (doc.type == "quote") DocumentType.Quote else DocumentType.Invoice,
                    documentNumber = doc.documentNumber,
                    status = doc.status,
                    paymentStatus = doc.paymentStatus,
                    amountPaid = doc.amountPaid?.toDouble(),
                    issueDate = doc.issueDate,
                    subtotal = doc.subtotal.toDouble(),
                    discount = doc.discount.toDouble(),
                    extraFees = doc.extraFees.toDouble(),
                    total = doc.total.toDouble(),
                    currency = doc.currency,
                    notes = doc.notes,
                    termsText = doc.termsText,
                    customer = customer,
                    items = items
                )
            }
        }

    suspend fun fetchDocumentPdf(documentId: String): ByteArray =
        withContext(Dispatchers.IO) {
            backendApiClient.fetchDocumentPdf(documentId)
        }

    suspend fun clearLocalCache() {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                dao.clearBusinessSettings()
                dao.clearCustomers()
                dao.clearProducts()
                dao.clearDocuments()
            }
        }
        lastFullRefreshUserId = null
        lastFullRefreshAt = 0L
        syncStateMutable.value = CacheSyncState()
    }

    suspend fun bootstrapUserData(userId: String, fullName: String?): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val deadline = System.currentTimeMillis() + 5_000L
            while (true) {
                val profileExists = supabaseClient.from("profiles")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeList<app.tijario.data.model.ProfileRowDto>()
                    .isNotEmpty()

                val userPlanExists = supabaseClient.from("user_plan")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<app.tijario.data.model.UserPlanRowDto>()
                    .isNotEmpty()

                val usageExists = supabaseClient.from("usage_counters")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<app.tijario.data.model.UsageCounterRowDto>()
                    .isNotEmpty()

                if (profileExists && userPlanExists && usageExists) return@withContext

                if (System.currentTimeMillis() >= deadline) {
                    val missing = buildList {
                        if (!profileExists) add("profiles")
                        if (!userPlanExists) add("user_plan")
                        if (!usageExists) add("usage_counters")
                    }.joinToString(",")
                    if (BuildConfig.DEBUG) {
                        android.util.Log.e(
                            "TijarioRepository",
                            "BOOTSTRAP_CHECK_FAILED step=verify_seed_data missing=$missing"
                        )
                    }
                    error("BOOTSTRAP_CHECK:$missing")
                }

                delay(250)
            }
        }
        SyncScheduler(context).triggerSync(userId)
    }

    internal fun currentUtcPeriodMonth(reference: LocalDate = LocalDate.now(ZoneOffset.UTC)): String =
        reference.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    private suspend fun fetchRemoteSnapshot(userId: String): RemoteSnapshot =
        coroutineScope {
            val settings = async { fetchBusinessSettings(userId) }
            val customers = async { fetchCustomers(userId) }
            val products = async { fetchProducts(userId) }
            val documents = async { fetchDocuments(userId) }
            RemoteSnapshot(
                businessSettings = settings.await(),
                customers = customers.await(),
                products = products.await(),
                documents = documents.await(),
            )
        }

    internal open suspend fun fetchBusinessSettings(userId: String): BusinessSettings? =
        supabaseClient.from("business_settings")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<BusinessSettings>()
            .firstOrNull()

    internal open suspend fun fetchCustomers(userId: String): List<Customer> =
        supabaseClient.from("customers")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList()

    internal open suspend fun fetchProducts(userId: String): List<Product> =
        supabaseClient.from("products")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList()

    internal open suspend fun fetchDocuments(userId: String): List<DocumentSummary> =
        supabaseClient.from("documents")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList()

    private suspend fun requireUserId(): String =
        currentUserId() ?: error("No authenticated user.")

    private fun setRefreshing(isRefreshing: Boolean, errorMessage: String?) {
        syncStateMutable.value = syncStateMutable.value.copy(
            isRefreshing = isRefreshing,
            errorMessage = errorMessage,
        )
    }

    private data class RemoteSnapshot(
        val businessSettings: BusinessSettings?,
        val customers: List<Customer>,
        val products: List<Product>,
        val documents: List<DocumentSummary>,
    )

    fun observeLocalTaxes(): Flow<List<app.tijario.data.local.LocalTaxEntity>> = dao.observeLocalTaxes()
    suspend fun upsertLocalTax(tax: app.tijario.data.local.LocalTaxEntity) = withContext(Dispatchers.IO) { dao.upsertLocalTax(tax) }
    suspend fun deleteLocalTax(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalTax(id) }

    fun observeLocalPaymentMethods(): Flow<List<app.tijario.data.local.LocalPaymentMethodEntity>> = dao.observeLocalPaymentMethods()
    suspend fun upsertLocalPaymentMethod(method: app.tijario.data.local.LocalPaymentMethodEntity) = withContext(Dispatchers.IO) { dao.upsertLocalPaymentMethod(method) }
    suspend fun deleteLocalPaymentMethod(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalPaymentMethod(id) }

    fun observeLocalSignatures(): Flow<List<app.tijario.data.local.LocalSignatureEntity>> = dao.observeLocalSignatures()
    suspend fun upsertLocalSignature(sig: app.tijario.data.local.LocalSignatureEntity) = withContext(Dispatchers.IO) { dao.upsertLocalSignature(sig) }
    suspend fun deleteLocalSignature(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalSignature(id) }

    fun observeLocalTerms(): Flow<List<app.tijario.data.local.LocalTermsEntity>> = dao.observeLocalTerms()
    suspend fun upsertLocalTerms(terms: app.tijario.data.local.LocalTermsEntity) = withContext(Dispatchers.IO) { dao.upsertLocalTerms(terms) }
    suspend fun deleteLocalTerms(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalTerms(id) }

    suspend fun getDocumentMetadata(documentId: String): app.tijario.data.local.LocalDocumentMetadataEntity? = withContext(Dispatchers.IO) {
        dao.getDocumentMetadata(documentId)
    }

    suspend fun upsertDocumentMetadata(metadata: app.tijario.data.local.LocalDocumentMetadataEntity) = withContext(Dispatchers.IO) {
        dao.upsertDocumentMetadata(metadata)
    }

    suspend fun finalizeOrVerifyQuota(documentId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        withContext(Dispatchers.IO) {
            reserveDocumentQuotaLedger(userId, documentId)
            val doc = dao.getDocument(userId, documentId) ?: error("Document not found")
            dao.upsertDocument(doc.copy(
                status = "sent",
                syncStatus = "LOCAL_ONLY"
            ))
            enqueueOutbox(userId, "document", documentId, "CREATE")
        }
    }

    open suspend fun sync(userId: String): Result<Unit> = runCatching {
        val currentToken = supabaseClient.auth.currentSessionOrNull()?.accessToken
            ?: throw IllegalStateException("SESSION_EXPIRED")
            
        withContext(Dispatchers.IO) {
            val staleTime = System.currentTimeMillis() - (5 * 60 * 1000) // 5 minutes
            val staleItems = dao.getPendingOutbox(userId).filter { 
                it.status == "PROCESSING" && (it.createdAt < staleTime) 
            }
            staleItems.forEach {
                dao.upsertOutbox(it.copy(status = "PENDING", attempts = it.attempts + 1))
            }

            val pending = dao.getPendingOutbox(userId).filter { it.status == "PENDING" }
                .sortedBy { it.createdAt }

            if (pending.isNotEmpty()) {
                val operationsDto = pending.map { op ->
                    val payloadJson: kotlinx.serialization.json.JsonElement = when (op.entityType) {
                        "customer" -> {
                            val cust = dao.getCustomer(userId, op.entityId)
                            if (cust != null) {
                                buildJsonObject {
                                    put("name", cust.name)
                                    put("whatsapp_number", cust.whatsappNumber)
                                    put("city", cust.city)
                                    put("notes", cust.notes)
                                }
                            } else {
                                buildJsonObject {}
                            }
                        }
                        "product" -> {
                            val prod = dao.getProduct(userId, op.entityId)
                            if (prod != null) {
                                buildJsonObject {
                                    put("kind", prod.kind)
                                    put("name", prod.name)
                                    put("description", prod.description)
                                    put("price", prod.price.toDouble())
                                    put("currency", prod.currency)
                                    put("stock_quantity", prod.stockQuantity)
                                }
                            } else {
                                buildJsonObject {}
                            }
                        }
                        "document" -> {
                            val doc = dao.getDocument(userId, op.entityId)
                            val items = dao.getDocumentItems(userId, op.entityId)
                            if (doc != null) {
                                buildJsonObject {
                                    put("customer_id", doc.customerId)
                                    put("type", doc.type)
                                    put("document_number", doc.documentNumber)
                                    put("status", doc.status)
                                    put("payment_status", doc.paymentStatus)
                                    put("issue_date", doc.issueDate)
                                    put("subtotal", doc.subtotal.toDouble())
                                    put("discount", doc.discount.toDouble())
                                    put("extra_fees", doc.extraFees.toDouble())
                                    put("total", doc.total.toDouble())
                                    put("currency", doc.currency)
                                    put("notes", doc.notes)
                                    put("terms_text", doc.termsText)
                                    putJsonArray("items") {
                                        items.forEach { itm ->
                                            addJsonObject {
                                                put("id", itm.id)
                                                put("name", itm.name)
                                                put("description", itm.description)
                                                put("quantity", itm.quantity)
                                                put("unit_price", itm.unitPrice.toDouble())
                                                put("line_total", itm.lineTotal.toDouble())
                                                put("sort_order", itm.sortOrder)
                                            }
                                        }
                                    }
                                }
                            } else {
                                buildJsonObject {}
                            }
                        }
                        "business_settings" -> {
                            val bs = dao.getBusinessSettings(userId)
                            if (bs != null) {
                                buildJsonObject {
                                    put("business_name", bs.businessName)
                                    put("whatsapp_number", bs.whatsappNumber)
                                    put("country", bs.country)
                                    put("city", bs.city)
                                    put("currency", bs.currency)
                                    put("instagram_url", bs.instagramUrl)
                                    put("invoice_note", bs.invoiceNote)
                                    put("terms_text", bs.termsText)
                                    put("logo_url", bs.logoUrl)
                                }
                            } else {
                                buildJsonObject {}
                            }
                        }
                        else -> buildJsonObject {}
                    }

                    dao.upsertOutbox(op.copy(status = "PROCESSING"))

                    SyncOperationDto(
                        operation_id = op.id,
                        entity_type = op.entityType,
                        entity_id = op.entityId,
                        operation = op.operation,
                        base_server_revision = op.baseServerRevision,
                        payload = payloadJson
                    )
                }

                val pushResponse = backendApiClient.pushSync(PushSyncRequest(operationsDto))
                if (pushResponse.status.value == 401) throw IllegalStateException("SESSION_EXPIRED")
                if (!pushResponse.status.isSuccess()) throw IllegalStateException("RETRYABLE_NETWORK_ERROR")

                val pushResult = repositoryJson.decodeFromString<PushSyncResponse>(pushResponse.bodyAsText())
                pushResult.results.forEach { res ->
                    val outboxItem = pending.find { it.id == res.operation_id } ?: return@forEach
                    database.withTransaction {
                        if (res.status == "APPLIED" || res.status == "ALREADY_PROCESSED") {
                            dao.deleteOutbox(res.operation_id)
                            
                            when (outboxItem.entityType) {
                                "customer" -> {
                                    val cust = dao.getCustomer(userId, outboxItem.entityId)
                                    if (cust != null) {
                                        dao.upsertCustomer(cust.copy(
                                            syncStatus = "SYNCED",
                                            serverRevision = res.server_revision,
                                            syncedAt = System.currentTimeMillis()
                                        ))
                                    }
                                }
                                "product" -> {
                                    val prod = dao.getProduct(userId, outboxItem.entityId)
                                    if (prod != null) {
                                        dao.upsertProduct(prod.copy(
                                            syncStatus = "SYNCED",
                                            serverRevision = res.server_revision,
                                            syncedAt = System.currentTimeMillis()
                                        ))
                                    }
                                }
                                "document" -> {
                                    val doc = dao.getDocument(userId, outboxItem.entityId)
                                    if (doc != null) {
                                        dao.upsertDocument(doc.copy(
                                            syncStatus = "SYNCED",
                                            serverRevision = res.server_revision,
                                            syncedAt = System.currentTimeMillis()
                                        ))
                                        dao.deleteLedgerByDocId(userId, outboxItem.entityId)
                                    }
                                }
                                "business_settings" -> {
                                    val bs = dao.getBusinessSettings(userId)
                                    if (bs != null) {
                                        dao.upsertBusinessSettings(bs.copy(
                                            syncStatus = "SYNCED",
                                            serverRevision = res.server_revision,
                                            syncedAt = System.currentTimeMillis()
                                        ))
                                    }
                                }
                            }
                        } else if (res.status == "CONFLICT") {
                            dao.upsertOutbox(outboxItem.copy(status = "CONFLICT"))
                            when (outboxItem.entityType) {
                                "customer" -> {
                                    dao.getCustomer(userId, outboxItem.entityId)?.let {
                                        dao.upsertCustomer(it.copy(syncStatus = "CONFLICT"))
                                    }
                                }
                                "product" -> {
                                    dao.getProduct(userId, outboxItem.entityId)?.let {
                                        dao.upsertProduct(it.copy(syncStatus = "CONFLICT"))
                                    }
                                }
                                "document" -> {
                                    dao.getDocument(userId, outboxItem.entityId)?.let { doc ->
                                        dao.upsertDocument(doc.copy(syncStatus = "CONFLICT"))
                                        
                                        val newId = java.util.UUID.randomUUID().toString()
                                        val newDoc = doc.copy(
                                            id = newId,
                                            documentNumber = doc.documentNumber + "-تعارض",
                                            status = "draft",
                                            syncStatus = "LOCAL_ONLY",
                                            localRevision = 1,
                                            serverRevision = null
                                        )
                                        dao.upsertDocument(newDoc)
                                        val items = dao.getDocumentItems(userId, doc.id)
                                        items.forEach { itm ->
                                            dao.insertDocumentItems(listOf(itm.copy(
                                                id = java.util.UUID.randomUUID().toString(),
                                                documentId = newId
                                            )))
                                        }
                                    }
                                }
                            }
                        } else if (res.status == "BLOCKED_BY_PLAN") {
                            dao.upsertOutbox(outboxItem.copy(status = "BLOCKED_BY_PLAN"))
                            if (outboxItem.entityType == "document") {
                                dao.getDocument(userId, outboxItem.entityId)?.let {
                                    dao.upsertDocument(it.copy(syncStatus = "BLOCKED_BY_PLAN"))
                                }
                            }
                        } else {
                            dao.upsertOutbox(outboxItem.copy(status = "PENDING", attempts = outboxItem.attempts + 1))
                        }
                    }
                }
            }

            val lastSyncedTimeStr = try {
                val defaultTime = Date(0)
                val custMax = dao.observeCustomers(userId).first().mapNotNull { it.serverRevision?.let { r -> java.time.Instant.parse(r) } }.maxOrNull()
                val prodMax = dao.observeProducts(userId).first().mapNotNull { it.serverRevision?.let { r -> java.time.Instant.parse(r) } }.maxOrNull()
                val docMax = dao.observeDocuments(userId).first().mapNotNull { it.serverRevision?.let { r -> java.time.Instant.parse(r) } }.maxOrNull()
                
                val maxInstant = listOfNotNull(custMax, prodMax, docMax).maxOrNull() ?: defaultTime.toInstant()
                maxInstant.toString()
            } catch (e: Exception) {
                Date(0).toInstant().toString()
            }

            val pullResponse = backendApiClient.pullSync(PullSyncRequest(lastSyncedTimeStr))
            if (pullResponse.status.isSuccess()) {
                val pullResult = repositoryJson.decodeFromString<PullSyncResponse>(pullResponse.bodyAsText())
                database.withTransaction {
                    pullResult.changes.customers.forEach { item ->
                        val local = dao.getCustomer(userId, item.id)
                        if (local == null || local.syncStatus == "SYNCED") {
                            dao.upsertCustomer(app.tijario.data.local.CustomerEntity(
                                id = item.id,
                                userId = userId,
                                name = item.name,
                                whatsappNumber = item.whatsapp_number,
                                city = item.city,
                                notes = item.notes,
                                syncStatus = "SYNCED",
                                serverRevision = item.updated_at,
                                syncedAt = System.currentTimeMillis()
                            ))
                        }
                    }

                    pullResult.changes.products.forEach { item ->
                        val local = dao.getProduct(userId, item.id)
                        if (local == null || local.syncStatus == "SYNCED") {
                            dao.upsertProduct(app.tijario.data.local.ProductEntity(
                                id = item.id,
                                userId = userId,
                                kind = item.kind,
                                name = item.name,
                                description = item.description,
                                price = BigDecimal.valueOf(item.price),
                                currency = item.currency,
                                stockQuantity = item.stock_quantity,
                                syncStatus = "SYNCED",
                                serverRevision = item.updated_at,
                                syncedAt = System.currentTimeMillis()
                            ))
                        }
                    }

                    pullResult.changes.business_settings.forEach { item ->
                        val local = dao.getBusinessSettings(userId)
                        if (local == null || local.syncStatus == "SYNCED") {
                            dao.upsertBusinessSettings(app.tijario.data.local.BusinessSettingsEntity(
                                userId = userId,
                                remoteId = item.id,
                                businessName = item.business_name,
                                whatsappNumber = item.whatsapp_number,
                                country = item.country,
                                city = item.city,
                                currency = item.currency,
                                logoUrl = item.logo_url,
                                instagramUrl = item.instagram_url,
                                invoiceNote = item.invoice_note,
                                termsText = item.terms_text,
                                syncStatus = "SYNCED",
                                serverRevision = item.updated_at,
                                syncedAt = System.currentTimeMillis()
                            ))
                        }
                    }

                    pullResult.changes.documents.forEach { item ->
                        val local = dao.getDocument(userId, item.id)
                        if (local == null || local.syncStatus == "SYNCED") {
                            dao.upsertDocument(app.tijario.data.local.DocumentEntity(
                                id = item.id,
                                userId = userId,
                                customerId = item.customer_id,
                                type = item.type,
                                documentNumber = item.document_number,
                                status = item.status,
                                paymentStatus = item.payment_status,
                                amountPaid = null,
                                issueDate = item.issue_date,
                                subtotal = BigDecimal.valueOf(item.subtotal),
                                discount = BigDecimal.valueOf(item.discount),
                                extraFees = BigDecimal.valueOf(item.extra_fees),
                                total = BigDecimal.valueOf(item.total),
                                currency = item.currency,
                                notes = item.notes,
                                termsText = item.terms_text,
                                syncStatus = "SYNCED",
                                serverRevision = item.updated_at,
                                syncedAt = System.currentTimeMillis()
                            ))
                        }
                    }

                    pullResult.changes.document_items.forEach { item ->
                        dao.insertDocumentItems(listOf(app.tijario.data.local.DocumentItemEntity(
                            id = item.id,
                            documentId = item.document_id,
                            userId = userId,
                            productId = null,
                            name = item.name,
                            description = item.description,
                            quantity = item.quantity,
                            unitPrice = BigDecimal.valueOf(item.unit_price),
                            lineTotal = BigDecimal.valueOf(item.line_total),
                            sortOrder = item.sort_order
                        )))
                    }

                    pullResult.changes.tombstones.forEach { tomb ->
                        when (tomb.entity_type) {
                            "customer" -> {
                                dao.deleteCustomer(userId, tomb.entity_id)
                            }
                            "product" -> {
                                dao.deleteProduct(userId, tomb.entity_id)
                            }
                            "document" -> {
                                dao.deleteDocumentItems(userId, tomb.entity_id)
                                dao.deleteDocument(userId, tomb.entity_id)
                            }
                        }
                    }
                }
            }

            val deviceId = android.os.Build.MODEL + "_" + android.os.Build.ID
            val leaseResponse = backendApiClient.requestOfflineLease(OfflineLeaseRequest(deviceId))
            if (leaseResponse.status.isSuccess()) {
                val leaseResult = repositoryJson.decodeFromString<OfflineLeaseResponse>(leaseResponse.bodyAsText())
                val periodMonth = Date().toInstant().toString().substring(0, 7) + "-01"
                dao.upsertLease(app.tijario.data.local.OfflineQuotaLeaseEntity(
                    id = leaseResult.lease_id,
                    userId = userId,
                    deviceId = deviceId,
                    planCode = "free",
                    periodMonth = periodMonth,
                    allowedLimit = leaseResult.quota_granted,
                    consumedCount = 0,
                    expiresAt = java.time.Instant.parse(leaseResult.expires_at).toEpochMilli(),
                    status = "ACTIVE"
                ))
            }
        }
    }

    suspend fun deleteAccountLocal(userId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                dao.deleteBusinessSettings(userId)
                dao.deleteCustomers(userId)
                dao.deleteProducts(userId)
                dao.deleteDocuments(userId)
                dao.deleteOutboxForUser(userId)
                dao.deleteLeasesForUser(userId)
                dao.deleteLedgerForUser(userId)
            }
            
            app.tijario.features.business.logo.LogoAssetManager(context).getLocalLogoFile(userId)?.delete()
            val userPdfDir = File(context.filesDir, "documents/pdfs/$userId")
            if (userPdfDir.exists()) {
                userPdfDir.deleteRecursively()
            }
        }
    }

    private companion object {
        const val FULL_REFRESH_THROTTLE_MS = 15_000L
    }

    private suspend fun reserveDocumentQuotaLedger(userId: String, documentId: String) {
        val doc = dao.getDocument(userId, documentId) ?: error("Document not found")
        if (doc.syncStatus == "SYNCED" || doc.localPdfRelativePath != null) return
        if (dao.getLedgerByDocId(userId, documentId) != null) return
        val periodMonth = currentUtcPeriodMonth()
        val deviceId = android.os.Build.MODEL + "_" + android.os.Build.ID
        val pendingLedgers = dao.getPendingLedger(userId).size
        val lease = dao.getLease(userId, deviceId, periodMonth)
        val cachedUsage = AppPreferences.getPlanUsage(context, userId)
        val available = when {
            lease != null && lease.expiresAt >= System.currentTimeMillis() ->
                lease.allowedLimit - lease.consumedCount - pendingLedgers
            cachedUsage != null ->
                cachedUsage.documentsLimit - cachedUsage.documentsUsed - pendingLedgers
            else -> throw IllegalStateException("QUOTA_LIMIT_EXCEEDED")
        }
        if (available <= 0) {
            throw IllegalStateException("QUOTA_LIMIT_EXCEEDED")
        }
        val opId = java.util.UUID.randomUUID().toString()
        dao.upsertLedger(app.tijario.data.local.LocalUsageLedgerEntity(
            usageEventId = java.util.UUID.randomUUID().toString(),
            userId = userId,
            documentId = documentId,
            operationId = opId,
            leaseId = lease?.id ?: "cached:$periodMonth",
            periodMonth = periodMonth,
            status = "PENDING",
            createdAt = System.currentTimeMillis(),
            syncedAt = null
        ))
    }

    private suspend fun overlayLocalUsage(
        userId: String,
        usage: app.tijario.data.model.UserPlanUsage,
    ): app.tijario.data.model.UserPlanUsage {
        val pendingDocs = dao.getPendingLedger(userId).size
        if (pendingDocs <= 0) return usage
        return usage.copy(documentsUsed = usage.documentsUsed + pendingDocs)
    }
}
