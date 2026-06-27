package app.tijario.ui.state

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.data.AppContainer
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.Product
import app.tijario.data.remote.ApiResult
import app.tijario.data.remote.CreateDocumentRequest
import app.tijario.data.remote.CreateDocumentResponse
import app.tijario.data.remote.AiV2CaptionRequest
import app.tijario.data.remote.AiV2ReplyRequest
import app.tijario.data.remote.AiV2ReportRequest
import app.tijario.data.remote.AiV2Response
import app.tijario.data.repository.TijarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TijarioDataUiState(
    val userId: String? = null,
    val businessSettings: BusinessSettings? = null,
    val customers: List<Customer> = emptyList(),
    val products: List<Product> = emptyList(),
    val documents: List<DocumentSummary> = emptyList(),
    val planUsage: app.tijario.data.model.UserPlanUsage? = null,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
) {
    val hasCachedData: Boolean
        get() = businessSettings != null || customers.isNotEmpty() || products.isNotEmpty() || documents.isNotEmpty()
}

sealed interface PlanUsageState {
    data object Idle : PlanUsageState
    data object Loading : PlanUsageState
    data class Success(val value: app.tijario.data.model.UserPlanUsage) : PlanUsageState
    data class Error(val message: String) : PlanUsageState
}

class TijarioDataViewModel(
    private val repository: TijarioRepository,
    private val aiRepository: app.tijario.data.repository.AiRepository,
) : ViewModel() {
    private val uiStateMutable = MutableStateFlow(TijarioDataUiState())
    private val planUsageStateMutable = MutableStateFlow<PlanUsageState>(PlanUsageState.Idle)
    private var cacheCollectionJob: Job? = null
    private var refreshJob: Job? = null

    val uiState: StateFlow<TijarioDataUiState> = uiStateMutable.asStateFlow()
    val planUsageState: StateFlow<PlanUsageState> = planUsageStateMutable.asStateFlow()

    fun startForCurrentUser(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val userId = repository.currentUserId()
            if (userId == null) {
                cacheCollectionJob?.cancel()
                uiStateMutable.value = TijarioDataUiState()
                return@launch
            }

            if (uiStateMutable.value.userId != userId) {
                cacheCollectionJob?.cancel()
                uiStateMutable.value = TijarioDataUiState(userId = userId, isInitialLoading = true)
                cacheCollectionJob = collectCache(userId)
            }

            refreshAll(force = forceRefresh)
            refreshPlanUsage()
        }
    }

    suspend fun hasCachedBusinessSettingsForCurrentUser(): Boolean {
        val userId = repository.currentUserId() ?: return false
        return repository.hasCachedBusinessSettings(userId)
    }

    suspend fun fetchCurrentProfileFullName(): String? =
        repository.fetchCurrentProfileFullName().getOrNull()

    suspend fun updateCurrentProfileFullName(fullName: String): Result<Unit> =
        repository.updateCurrentProfileFullName(fullName)

    suspend fun refreshBusinessSettings() {
        repository.refreshBusinessSettings()
    }

    fun refreshAll(force: Boolean = false) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            repository.refreshAll(force = force)
            refreshPlanUsage()
            uiStateMutable.update { it.copy(isInitialLoading = false) }
        }
    }

    fun refreshPlanUsage() {
        viewModelScope.launch {
            planUsageStateMutable.value = PlanUsageState.Loading
            repository.fetchUserPlanUsage().onSuccess { usage ->
                planUsageStateMutable.value = PlanUsageState.Success(usage)
                uiStateMutable.update { it.copy(planUsage = usage) }
            }.onFailure { error ->
                planUsageStateMutable.value = PlanUsageState.Error(error.message ?: "تعذر تحميل بيانات الخطة.")
            }
        }
    }

    suspend fun createCustomer(customer: Customer): Result<Unit> =
        repository.createCustomer(customer)

    suspend fun updateCustomer(customer: Customer): Result<Unit> =
        repository.updateCustomer(customer)

    suspend fun deleteCustomer(customerId: String): Result<Unit> =
        repository.deleteCustomer(customerId)

    suspend fun createProduct(product: Product): Result<Unit> =
        repository.createProduct(product)

    suspend fun updateProduct(product: Product): Result<Unit> =
        repository.updateProduct(product)

    suspend fun deleteProduct(productId: String): Result<Unit> =
        repository.deleteProduct(productId)

    suspend fun saveBusinessSettings(settings: BusinessSettings): Result<Unit> =
        repository.saveBusinessSettings(settings)

    suspend fun cacheBusinessSettings(settings: BusinessSettings): Result<Unit> =
        repository.cacheBusinessSettings(settings)

    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> {
        val result = repository.createDocument(request)
        if (result.ok) {
            refreshPlanUsage()
        }
        return result
    }

    suspend fun updateDocument(documentId: String, request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        repository.updateDocument(documentId, request)

    suspend fun deleteDocument(documentId: String): ApiResult<CreateDocumentResponse> =
        repository.deleteDocument(documentId)

    suspend fun fetchCompleteDocument(documentId: String): Result<app.tijario.data.model.CompleteDocument> =
        repository.fetchCompleteDocument(documentId)

    suspend fun fetchDocumentPdf(documentId: String): ByteArray =
        repository.fetchDocumentPdf(documentId)

    suspend fun generateAiReply(request: app.tijario.data.remote.AiReplyRequest): ApiResult<app.tijario.data.remote.AiReplyResponse> {
        val result = aiRepository.generateReply(request)
        if (result.ok) {
            refreshPlanUsage()
        }
        return result
    }

    suspend fun generateAiReplyV2(request: AiV2ReplyRequest): AiV2Response =
        aiRepository.generateReplyV2(request)

    suspend fun generateAiCaption(request: app.tijario.data.remote.AiCaptionRequest): ApiResult<app.tijario.data.remote.AiCaptionResponse> {
        val result = aiRepository.generateCaption(request)
        if (result.ok) {
            refreshPlanUsage()
        }
        return result
    }

    suspend fun generateAiCaptionV2(request: AiV2CaptionRequest): AiV2Response =
        aiRepository.generateCaptionV2(request)

    suspend fun reportAiGenerationV2(request: AiV2ReportRequest): ApiResult<Unit> =
        aiRepository.reportV2(request)

    fun clearSessionCache() {
        viewModelScope.launch {
            cacheCollectionJob?.cancel()
            repository.clearLocalCache()
            uiStateMutable.value = TijarioDataUiState()
        }
    }

    private fun collectCache(userId: String): Job =
        viewModelScope.launch {
            combine(
                repository.observeBusinessSettings(userId),
                repository.observeCustomers(userId),
                repository.observeProducts(userId),
                repository.observeDocuments(userId),
                repository.syncState,
            ) { settings, customers, products, documents, syncState ->
                uiStateMutable.update {
                    it.copy(
                        userId = userId,
                        businessSettings = settings,
                        customers = customers,
                        products = products,
                        documents = documents,
                        isRefreshing = syncState.isRefreshing,
                        lastSyncedAt = syncState.lastSyncedAt,
                        errorMessage = syncState.errorMessage,
                    )
                }
            }.collect {}
        }

    fun observeLocalTaxes(): kotlinx.coroutines.flow.Flow<List<app.tijario.data.local.LocalTaxEntity>> = repository.observeLocalTaxes()
    suspend fun upsertLocalTax(tax: app.tijario.data.local.LocalTaxEntity) = repository.upsertLocalTax(tax)
    suspend fun deleteLocalTax(id: String) = repository.deleteLocalTax(id)

    fun observeLocalPaymentMethods(): kotlinx.coroutines.flow.Flow<List<app.tijario.data.local.LocalPaymentMethodEntity>> = repository.observeLocalPaymentMethods()
    suspend fun upsertLocalPaymentMethod(method: app.tijario.data.local.LocalPaymentMethodEntity) = repository.upsertLocalPaymentMethod(method)
    suspend fun deleteLocalPaymentMethod(id: String) = repository.deleteLocalPaymentMethod(id)

    fun observeLocalSignatures(): kotlinx.coroutines.flow.Flow<List<app.tijario.data.local.LocalSignatureEntity>> = repository.observeLocalSignatures()
    suspend fun upsertLocalSignature(sig: app.tijario.data.local.LocalSignatureEntity) = repository.upsertLocalSignature(sig)
    suspend fun deleteLocalSignature(id: String) = repository.deleteLocalSignature(id)

    fun observeLocalTerms(): kotlinx.coroutines.flow.Flow<List<app.tijario.data.local.LocalTermsEntity>> = repository.observeLocalTerms()
    suspend fun upsertLocalTerms(terms: app.tijario.data.local.LocalTermsEntity) = repository.upsertLocalTerms(terms)
    suspend fun deleteLocalTerms(id: String) = repository.deleteLocalTerms(id)

    suspend fun getDocumentMetadata(documentId: String): app.tijario.data.local.LocalDocumentMetadataEntity? = repository.getDocumentMetadata(documentId)
    suspend fun upsertDocumentMetadata(metadata: app.tijario.data.local.LocalDocumentMetadataEntity) = repository.upsertDocumentMetadata(metadata)
}

class TijarioDataViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TijarioDataViewModel::class.java)) {
            val repo = AppContainer.repository(context.applicationContext)
            val aiRepo = app.tijario.data.repository.AiRepository(app.tijario.config.Supabase.apiClient)
            return TijarioDataViewModel(repo, aiRepo) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
