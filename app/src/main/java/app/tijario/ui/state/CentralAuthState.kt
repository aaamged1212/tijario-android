package app.tijario.ui.state

sealed interface CentralAuthState {
    object Initializing : CentralAuthState
    object Unauthenticated : CentralAuthState
    object AwaitingEmailVerification : CentralAuthState
    object AuthenticatedNeedsOnboarding : CentralAuthState
    object AuthenticatedReady : CentralAuthState
    data class Error(val message: String) : CentralAuthState
}

object AuthStateResolver {
    fun resolve(
        sessionExists: Boolean,
        isEmailVerified: Boolean,
        hasBusinessSettings: Boolean,
        errorMessage: String? = null
    ): CentralAuthState {
        if (errorMessage != null) {
            return CentralAuthState.Error(errorMessage)
        }
        if (!sessionExists) {
            return CentralAuthState.Unauthenticated
        }
        if (!isEmailVerified) {
            return CentralAuthState.AwaitingEmailVerification
        }
        if (!hasBusinessSettings) {
            return CentralAuthState.AuthenticatedNeedsOnboarding
        }
        return CentralAuthState.AuthenticatedReady
    }
}
