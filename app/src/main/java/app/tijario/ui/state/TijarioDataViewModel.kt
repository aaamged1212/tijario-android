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
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
) {
    val hasCachedData: Boolean
        get() = businessSettings != null || customers.isNotEmpty() || products.isNotEmpty() || documents.isNotEmpty()
}

class TijarioDataViewModel(
    private val repository: TijarioRepository,
) : ViewModel() {
    private val uiStateMutable = MutableStateFlow(TijarioDataUiState())
    private var cacheCollectionJob: Job? = null
    private var refreshJob: Job? = null

    val uiState: StateFlow<TijarioDataUiState> = uiStateMutable.asStateFlow()

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
        }
    }

    suspend fun hasCachedBusinessSettingsForCurrentUser(): Boolean {
        val userId = repository.currentUserId() ?: return false
        return repository.hasCachedBusinessSettings(userId)
    }

    suspend fun refreshBusinessSettings() {
        repository.refreshBusinessSettings()
    }

    fun refreshAll(force: Boolean = false) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            repository.refreshAll(force = force)
            uiStateMutable.update { it.copy(isInitialLoading = false) }
        }
    }

    suspend fun createCustomer(customer: Customer): Result<Unit> =
        repository.createCustomer(customer)

    suspend fun createProduct(product: Product): Result<Unit> =
        repository.createProduct(product)

    suspend fun saveBusinessSettings(settings: BusinessSettings): Result<Unit> =
        repository.saveBusinessSettings(settings)

    suspend fun cacheBusinessSettings(settings: BusinessSettings): Result<Unit> =
        repository.cacheBusinessSettings(settings)

    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        repository.createDocument(request)

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
                TijarioDataUiState(
                    userId = userId,
                    businessSettings = settings,
                    customers = customers,
                    products = products,
                    documents = documents,
                    isInitialLoading = false,
                    isRefreshing = syncState.isRefreshing,
                    lastSyncedAt = syncState.lastSyncedAt,
                    errorMessage = syncState.errorMessage,
                )
            }.collect { state ->
                uiStateMutable.value = state
            }
        }
}

class TijarioDataViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TijarioDataViewModel::class.java)) {
            return TijarioDataViewModel(AppContainer.repository(context.applicationContext)) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
