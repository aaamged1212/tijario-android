package app.tijario.data.repository

import androidx.room.withTransaction
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.local.toEntity
import app.tijario.data.local.toModel
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.Product
import app.tijario.data.model.ProfileFullNameUpdateDto
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class CacheSyncState(
    val isRefreshing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
)

class TijarioRepository(
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
        dao.observeCustomers(userId).map { rows -> rows.map { it.toModel() } }

    fun observeProducts(userId: String): Flow<List<Product>> =
        dao.observeProducts(userId).map { rows -> rows.map { it.toModel() } }

    fun observeDocuments(userId: String): Flow<List<DocumentSummary>> =
        kotlinx.coroutines.flow.combine(
            dao.observeDocuments(userId),
            dao.observeAllDocumentMetadata()
        ) { rows, metadataList ->
            val metadataMap = metadataList.associateBy { it.documentId }
            rows.map { row ->
                val model = row.toModel()
                val meta = metadataMap[model.id]
                if (meta != null) {
                    model.copy(currency = meta.currency)
                } else {
                    model
                }
            }
        }

    suspend fun currentUserId(): String? =
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
                        dao.deleteBusinessSettings(userId)
                    } else {
                        dao.upsertBusinessSettings(snapshot.businessSettings.toEntity(userId, syncedAt))
                    }
                    dao.deleteCustomers(userId)
                    dao.upsertCustomers(snapshot.customers.mapNotNull { it.toEntity(userId, syncedAt) })
                    dao.deleteProducts(userId)
                    dao.upsertProducts(snapshot.products.mapNotNull { it.toEntity(userId, syncedAt) })
                    dao.deleteDocuments(userId)
                    dao.upsertDocuments(snapshot.documents.map { it.toEntity(userId, syncedAt) })
                }
            }
            lastFullRefreshUserId = userId
            lastFullRefreshAt = syncedAt
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
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
                    dao.deleteBusinessSettings(userId)
                } else {
                    dao.upsertBusinessSettings(settings.toEntity(userId, syncedAt))
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
                    dao.deleteCustomers(userId)
                    dao.upsertCustomers(customers.mapNotNull { it.toEntity(userId, syncedAt) })
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
                    dao.deleteProducts(userId)
                    dao.upsertProducts(products.mapNotNull { it.toEntity(userId, syncedAt) })
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
                    dao.deleteDocuments(userId)
                    dao.upsertDocuments(documents.map { it.toEntity(userId, syncedAt) })
                }
            }
            syncStateMutable.value = CacheSyncState(isRefreshing = false, lastSyncedAt = syncedAt)
        }.onFailure { error ->
            setRefreshing(false, error.message ?: "تعذر تحديث المستندات الآن.")
        }

    suspend fun createCustomer(customer: Customer): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                supabaseClient.from("customers").insert(customer.copy(userId = userId))
            }
            refreshCustomers().getOrThrow()
        }

    suspend fun updateCustomer(customer: Customer): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                supabaseClient.from("customers").update(customer.copy(userId = userId)) {
                    filter {
                        eq("id", customer.id!!)
                        eq("user_id", userId)
                    }
                }
            }
            refreshCustomers().getOrThrow()
        }

    suspend fun deleteCustomer(customerId: String): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            val docCount = dao.countDocumentsForCustomer(customerId)
            if (docCount > 0) {
                throw IllegalStateException("لا يمكن حذف العميل لوجود مستندات تاريخية مرتبطة به.")
            }
            withContext(Dispatchers.IO) {
                supabaseClient.from("customers").delete {
                    filter {
                        eq("id", customerId)
                        eq("user_id", userId)
                    }
                }
            }
            refreshCustomers().getOrThrow()
        }

    suspend fun createProduct(product: Product): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                supabaseClient.from("products").insert(product.copy(userId = userId))
            }
            refreshProducts().getOrThrow()
        }

    suspend fun updateProduct(product: Product): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                supabaseClient.from("products").update(product.copy(userId = userId)) {
                    filter {
                        eq("id", product.id!!)
                        eq("user_id", userId)
                    }
                }
            }
            refreshProducts().getOrThrow()
        }

    suspend fun deleteProduct(productId: String): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                supabaseClient.from("products").delete {
                    filter {
                        eq("id", productId)
                        eq("user_id", userId)
                    }
                }
            }
            refreshProducts().getOrThrow()
        }

    suspend fun saveBusinessSettings(settings: BusinessSettings): Result<Unit> =
        runCatching {
            val userId = requireUserId()
            val normalized = settings.copy(userId = settings.userId ?: userId)
            withContext(Dispatchers.IO) {
                supabaseClient.from("business_settings").upsert(normalized)
            }
            cacheBusinessSettings(normalized)
            refreshBusinessSettings().getOrThrow()
        }

    suspend fun cacheBusinessSettings(settings: BusinessSettings): Result<Unit> =
        runCatching {
            val userId = settings.userId ?: requireUserId()
            val syncedAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                dao.upsertBusinessSettings(settings.toEntity(userId, syncedAt))
            }
            syncStateMutable.value = syncStateMutable.value.copy(lastSyncedAt = syncedAt, errorMessage = null)
        }

    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        val result = backendApiClient.createDocument(request)
        if (result.ok) {
            refreshDocuments()
        }
        return result
    }

    suspend fun updateDocument(documentId: String, request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        val result = backendApiClient.updateDocument(documentId, request)
        if (result.ok) {
            refreshDocuments()
        }
        return result
    }

    suspend fun deleteDocument(documentId: String): ApiResult<CreateDocumentResponse> {
        val result = backendApiClient.deleteDocument(documentId)
        if (result.ok) {
            refreshDocuments()
        }
        return result
    }

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

                app.tijario.data.model.UserPlanUsage(
                    planCode = plan.code,
                    planName = plan.name,
                    periodMonth = periodMonth,
                    documentsUsed = usage?.documentsUsed ?: 0,
                    documentsLimit = plan.monthlyDocumentLimit,
                    aiUsed = usage?.aiUsed ?: 0,
                    aiLimit = plan.monthlyAiLimit
                )
            }
        }

    suspend fun fetchCompleteDocument(documentId: String): Result<app.tijario.data.model.CompleteDocument> =
        runCatching {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                val doc = supabaseClient.from("documents")
                    .select {
                        filter {
                            eq("id", documentId)
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<app.tijario.data.model.CompleteDocument>()
                    .firstOrNull() ?: error("Document not found.")

                val customer = supabaseClient.from("customers")
                    .select {
                        filter {
                            eq("id", doc.customerId)
                        }
                    }
                    .decodeList<app.tijario.data.model.Customer>()
                    .firstOrNull()

                val items = supabaseClient.from("document_items")
                    .select {
                        filter {
                            eq("document_id", documentId)
                        }
                    }
                    .decodeList<app.tijario.data.model.DocumentItem>()

                doc.copy(
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
                            eq("period_month", currentUtcPeriodMonth())
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

    private suspend fun fetchBusinessSettings(userId: String): BusinessSettings? =
        supabaseClient.from("business_settings")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<BusinessSettings>()
            .firstOrNull()

    private suspend fun fetchCustomers(userId: String): List<Customer> =
        supabaseClient.from("customers")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList()

    private suspend fun fetchProducts(userId: String): List<Product> =
        supabaseClient.from("products")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList()

    private suspend fun fetchDocuments(userId: String): List<DocumentSummary> =
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

    private companion object {
        const val FULL_REFRESH_THROTTLE_MS = 15_000L
    }
}
