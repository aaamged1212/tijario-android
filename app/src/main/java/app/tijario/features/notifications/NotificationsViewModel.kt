package app.tijario.features.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.config.Supabase
import app.tijario.data.local.TijarioDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val userId: String? = null,
    val items: List<Announcement> = emptyList(),
    val unreadCount: Int = 0,
    val startupAnnouncement: Announcement? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

class NotificationsViewModel(
    private val repository: NotificationsRepository,
    private val topicManager: NotificationTopicManager,
) : ViewModel() {
    private val stateMutable = MutableStateFlow(NotificationsUiState())
    private var collectionJob: Job? = null
    private var unreadJob: Job? = null

    val state: StateFlow<NotificationsUiState> = stateMutable.asStateFlow()

    fun start(userId: String) {
        if (stateMutable.value.userId == userId) return
        collectionJob?.cancel()
        unreadJob?.cancel()
        stateMutable.value = NotificationsUiState(userId = userId, isLoading = true)
        collectionJob = viewModelScope.launch {
            repository.observeAnnouncements(userId).collect { items ->
                stateMutable.update { it.copy(items = items, isLoading = false) }
            }
        }
        unreadJob = viewModelScope.launch {
            repository.observeUnreadCount(userId).collect { count ->
                stateMutable.update { it.copy(unreadCount = count) }
            }
        }
        refresh(userId)
    }

    fun refresh(userId: String = stateMutable.value.userId.orEmpty()) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            stateMutable.update { it.copy(isLoading = true, errorMessage = null) }
            repository.refresh(userId)
                .onSuccess {
                    val startup = repository.startupAnnouncement(userId)
                    stateMutable.update {
                        it.copy(isLoading = false, isOffline = false, startupAnnouncement = startup)
                    }
                }
                .onFailure { error ->
                    val startup = repository.startupAnnouncement(userId)
                    stateMutable.update {
                        it.copy(
                            isLoading = false,
                            isOffline = true,
                            startupAnnouncement = startup,
                            errorMessage = error.message,
                        )
                    }
                }
        }
    }

    fun markSeen(announcementId: String, openedFrom: String = "startup") {
        val userId = stateMutable.value.userId ?: return
        viewModelScope.launch {
            repository.markSeen(userId, announcementId, openedFrom)
        }
    }

    fun markRead(announcementId: String, openedFrom: String = "inbox") {
        val userId = stateMutable.value.userId ?: return
        viewModelScope.launch {
            repository.markRead(userId, announcementId, openedFrom)
        }
    }

    fun dismissStartup(announcementId: String) {
        val userId = stateMutable.value.userId ?: return
        stateMutable.update { it.copy(startupAnnouncement = null) }
        viewModelScope.launch {
            repository.dismiss(userId, announcementId)
        }
    }

    fun clearStartup() {
        stateMutable.update { it.copy(startupAnnouncement = null) }
    }

    fun markAllRead() {
        val userId = stateMutable.value.userId ?: return
        viewModelScope.launch {
            repository.markAllRead(userId)
        }
    }

    fun syncTopic(language: app.tijario.config.AppLanguage) {
        viewModelScope.launch {
            runCatching { topicManager.syncForLanguage(language) }
        }
    }

    fun logout() {
        val userId = stateMutable.value.userId
        collectionJob?.cancel()
        unreadJob?.cancel()
        stateMutable.value = NotificationsUiState()
        viewModelScope.launch {
            runCatching { topicManager.unsubscribeCurrent() }
            if (!userId.isNullOrBlank()) {
                repository.clearForUser(userId)
            }
        }
    }
}

class NotificationsViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            val appContext = context.applicationContext
            return NotificationsViewModel(
                repository = NotificationsRepository(
                    context = appContext,
                    database = TijarioDatabase.getInstance(appContext),
                    backendApiClient = Supabase.apiClient,
                ),
                topicManager = NotificationTopicManager(appContext),
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
