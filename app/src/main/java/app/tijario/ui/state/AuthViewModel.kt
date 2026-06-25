package app.tijario.ui.state

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.data.AppContainer
import app.tijario.data.repository.TijarioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val supabaseClient: SupabaseClient,
    private val repository: TijarioRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<CentralAuthState>(CentralAuthState.Initializing)
    val authState: StateFlow<CentralAuthState> = _authState.asStateFlow()

    init {
        checkCurrentSession()
    }

    fun checkCurrentSession() {
        viewModelScope.launch {
            _authState.value = CentralAuthState.Initializing
            try {
                supabaseClient.auth.awaitInitialization()
                val session = supabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    val userId = session.user?.id
                    // Email verification is required if user exists but has unconfirmed email.
                    // If confirmedAt is null, user must verify.
                    val isEmailVerified = session.user?.emailConfirmedAt != null
                    val hasSettings = if (isEmailVerified && userId != null) {
                        resolveBusinessSettingsPresence(userId).getOrThrow()
                    } else {
                        false
                    }

                    _authState.value = AuthStateResolver.resolve(
                        sessionExists = true,
                        isEmailVerified = isEmailVerified,
                        hasBusinessSettings = hasSettings
                    )
                } else {
                    _authState.value = CentralAuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = CentralAuthState.Error(e.message ?: "فشل فحص حالة الجلسة")
            }
        }
    }

    fun setAwaitingVerification() {
        _authState.value = CentralAuthState.AwaitingEmailVerification
    }

    fun handleVerificationSuccess() {
        viewModelScope.launch {
            try {
                val session = supabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    val userId = session.user?.id
                    val hasSettings = userId?.let { resolveBusinessSettingsPresence(it).getOrThrow() } ?: false
                    _authState.value = AuthStateResolver.resolve(
                        sessionExists = true,
                        isEmailVerified = true,
                        hasBusinessSettings = hasSettings
                    )
                } else {
                    _authState.value = CentralAuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = CentralAuthState.Error(e.message ?: "حدث خطأ أثناء فحص البيانات بعد التحقق.")
            }
        }
    }

    fun handleLoginSuccess() {
        checkCurrentSession()
    }

    private suspend fun resolveBusinessSettingsPresence(userId: String): Result<Boolean> =
        runCatching {
            if (repository.hasCachedBusinessSettings(userId)) {
                return@runCatching true
            }

            repository.refreshBusinessSettings(force = true).getOrThrow()
            repository.hasCachedBusinessSettings(userId)
        }

    fun logout() {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
            } catch (e: Exception) {
                // Safe handling of signOut error - no-op/logged
            }
            repository.clearLocalCache()
            _authState.value = CentralAuthState.Unauthenticated
        }
    }
}

class AuthViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val repo = AppContainer.repository(context.applicationContext)
            return AuthViewModel(app.tijario.config.Supabase.client, repo) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
