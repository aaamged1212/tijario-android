package app.tijario.features.backup.drive

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val GOOGLE_DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"

/** A token is deliberately session-only and is never persisted with Drive metadata. */
data class DriveAuthorizationSession(
    val accessToken: String,
    val accountId: String,
    val accountEmail: String?,
)

sealed interface DriveAuthorizationOutcome {
    data class Authorized(val session: DriveAuthorizationSession) : DriveAuthorizationOutcome
    data class ResolutionRequired(val intentSender: IntentSender) : DriveAuthorizationOutcome
    data object Cancelled : DriveAuthorizationOutcome
    data object ScopeDenied : DriveAuthorizationOutcome
    data object PlayServicesUnavailable : DriveAuthorizationOutcome
    data object TemporarilyUnavailable : DriveAuthorizationOutcome
}

interface GoogleDriveAuthorizationManager {
    suspend fun authorize(forceAccountSelection: Boolean = false): DriveAuthorizationOutcome
    fun completeAuthorization(resultIntent: Intent?): DriveAuthorizationOutcome
    suspend fun clearCachedToken(accessToken: String)
    suspend fun revokeAccess(accountId: String): Result<Unit>
}

/** Production adapter for Google Identity AuthorizationClient; it never persists OAuth credentials. */
class ProductionGoogleDriveAuthorizationManager(context: Context) : GoogleDriveAuthorizationManager {
    private val authorizationClient: AuthorizationClient = Identity.getAuthorizationClient(context.applicationContext)

    override suspend fun authorize(forceAccountSelection: Boolean): DriveAuthorizationOutcome = try {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(GOOGLE_DRIVE_FILE_SCOPE)))
            .apply {
                if (forceAccountSelection) {
                    setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
                }
            }
            .build()
        toOutcome(authorizationClient.authorize(request).awaitResult())
    } catch (error: Exception) {
        error.toAuthorizationOutcome()
    }

    override fun completeAuthorization(resultIntent: Intent?): DriveAuthorizationOutcome = try {
        if (resultIntent == null) DriveAuthorizationOutcome.Cancelled
        else toOutcome(authorizationClient.getAuthorizationResultFromIntent(resultIntent))
    } catch (error: Exception) {
        error.toAuthorizationOutcome()
    }

    override suspend fun clearCachedToken(accessToken: String) {
        runCatching {
            authorizationClient.clearToken(ClearTokenRequest.builder().setToken(accessToken).build()).awaitResult()
        }
    }

    override suspend fun revokeAccess(accountId: String): Result<Unit> = runCatching {
        val request = RevokeAccessRequest.builder()
            .setScopes(listOf(Scope(GOOGLE_DRIVE_FILE_SCOPE)))
            .build()
        authorizationClient.revokeAccess(request).awaitResult()
    }

    private fun toOutcome(result: AuthorizationResult): DriveAuthorizationOutcome {
        if (result.hasResolution()) {
            return result.pendingIntent?.intentSender?.let(DriveAuthorizationOutcome::ResolutionRequired)
                ?: DriveAuthorizationOutcome.TemporarilyUnavailable
        }
        val scopesGranted = result.grantedScopes.orEmpty().any { it == GOOGLE_DRIVE_FILE_SCOPE }
        if (!scopesGranted) return DriveAuthorizationOutcome.ScopeDenied
        val token = result.accessToken?.takeIf(String::isNotBlank) ?: return DriveAuthorizationOutcome.TemporarilyUnavailable
        val account = result.toGoogleSignInAccount()
        val accountId = account?.id?.takeIf(String::isNotBlank)
            ?: account?.email?.takeIf(String::isNotBlank)
            ?: return DriveAuthorizationOutcome.TemporarilyUnavailable
        return DriveAuthorizationOutcome.Authorized(DriveAuthorizationSession(token, accountId, account?.email))
    }
}

/** Deterministic test double. No production token or Google API behavior is faked by this class. */
class FakeGoogleDriveAuthorizationManager(
    private var next: DriveAuthorizationOutcome = DriveAuthorizationOutcome.TemporarilyUnavailable,
) : GoogleDriveAuthorizationManager {
    private var resolutionOutcome: DriveAuthorizationOutcome = next
    var clearedTokens = 0
        private set
    var revocations = 0
        private set

    fun setNext(outcome: DriveAuthorizationOutcome) {
        next = outcome
    }

    fun setResolutionOutcome(outcome: DriveAuthorizationOutcome) {
        resolutionOutcome = outcome
    }

    override suspend fun authorize(forceAccountSelection: Boolean): DriveAuthorizationOutcome = next

    override fun completeAuthorization(resultIntent: Intent?): DriveAuthorizationOutcome =
        if (resultIntent == null) DriveAuthorizationOutcome.Cancelled else resolutionOutcome

    override suspend fun clearCachedToken(accessToken: String) {
        clearedTokens += 1
    }

    override suspend fun revokeAccess(accountId: String): Result<Unit> {
        revocations += 1
        return Result.success(Unit)
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value -> if (continuation.isActive) continuation.resume(value) }
    addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
    addOnCanceledListener { continuation.cancel() }
}

private fun Exception.toAuthorizationOutcome(): DriveAuthorizationOutcome = when {
    javaClass.name.contains("ApiException") && message.orEmpty().contains("CANCELED", ignoreCase = true) ->
        DriveAuthorizationOutcome.Cancelled
    javaClass.name.contains("GooglePlayServices") -> DriveAuthorizationOutcome.PlayServicesUnavailable
    else -> DriveAuthorizationOutcome.TemporarilyUnavailable
}
