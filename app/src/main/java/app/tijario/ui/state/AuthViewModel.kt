package app.tijario.ui.state

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.MainActivity
import app.tijario.BuildConfig
import app.tijario.config.Localization
import app.tijario.data.AppContainer
import app.tijario.data.repository.TijarioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(
    private val supabaseClient: SupabaseClient,
    private val repository: TijarioRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<CentralAuthState>(CentralAuthState.Initializing)
    val authState: StateFlow<CentralAuthState> = _authState.asStateFlow()

    var signUpEmail: String? = null
    var signUpFullName: String? = null

    suspend fun bootstrapUserAfterVerification(userId: String, fullName: String?): Result<Unit> {
        return repository.bootstrapUserData(userId, fullName)
    }

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
                    runCatching { repository.syncCurrentProfileFullNameFromMetadata() }
                        .onFailure {
                            if (BuildConfig.DEBUG) {
                                android.util.Log.w("AuthViewModel", "Profile name sync skipped", it)
                            }
                        }
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
                _authState.value = CentralAuthState.Error(
                    e.message ?: Localization.getString("error_session_check_failed", MainActivity.currentLanguage)
                )
            }
        }
    }

    fun handleGoogleSignInResult(result: io.github.jan.supabase.compose.auth.composable.NativeSignInResult) {
        viewModelScope.launch {
            when (result) {
                is io.github.jan.supabase.compose.auth.composable.NativeSignInResult.Success -> {
                    supabaseClient.auth.awaitInitialization()
                    val sessionReady = awaitGoogleSession()
                    if (sessionReady) {
                        checkCurrentSession()
                    } else {
                        _authState.value = CentralAuthState.Error(
                            Localization.getString("error_after_verification_check", MainActivity.currentLanguage)
                        )
                    }
                }
                is io.github.jan.supabase.compose.auth.composable.NativeSignInResult.Error -> {
                    if (app.tijario.BuildConfig.DEBUG) {
                        android.util.Log.d("AuthViewModel", "Google Sign-In Error: ${result.message}")
                    }
                    _authState.value = CentralAuthState.Error(
                        Localization.getString("google_login_error", MainActivity.currentLanguage)
                    )
                }
                is io.github.jan.supabase.compose.auth.composable.NativeSignInResult.ClosedByUser -> {
                    _authState.value = CentralAuthState.Unauthenticated
                }
                is io.github.jan.supabase.compose.auth.composable.NativeSignInResult.NetworkError -> {
                    _authState.value = CentralAuthState.Error(
                        Localization.getString("error_network", MainActivity.currentLanguage)
                    )
                }
            }
        }
    }

    private suspend fun awaitGoogleSession(timeoutMs: Long = 5_000L): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (supabaseClient.auth.currentSessionOrNull() != null) return true
            delay(250)
        }
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    fun setAwaitingVerification() {
        _authState.value = CentralAuthState.AwaitingEmailVerification
    }

    fun handleVerificationSuccess() {
        viewModelScope.launch {
            try {
                supabaseClient.auth.awaitInitialization()
                val sessionReady = awaitGoogleSession()
                val session = if (sessionReady) supabaseClient.auth.currentSessionOrNull() else null
                if (session != null) {
                    val userId = session.user?.id
                    runCatching { repository.syncCurrentProfileFullNameFromMetadata() }
                        .onFailure {
                            if (BuildConfig.DEBUG) {
                                android.util.Log.w("AuthViewModel", "Profile name sync skipped after verification", it)
                            }
                        }
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
                _authState.value = CentralAuthState.Error(
                    e.message ?: Localization.getString("error_after_verification_check", MainActivity.currentLanguage)
                )
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
            } catch (_: Exception) {
                // Ignore sign-out errors.
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
