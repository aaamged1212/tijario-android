package app.tijario.features.backup.drive

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/** Session-only OAuth token store. Process death intentionally requires reauthorization. */
class DriveTokenProvider {
    private val tokens = ConcurrentHashMap<String, String>()

    suspend fun get(userId: String): String? = tokens[userId]
    fun has(userId: String): Boolean = !tokens[userId].isNullOrBlank()
    suspend fun put(userId: String, token: String) {
        if (userId.isNotBlank() && token.isNotBlank()) tokens[userId] = token
    }
    suspend fun clear(userId: String): String? = tokens.remove(userId)
}

data class DriveRuntime(
    val connectionRepository: DriveConnectionRepository,
    val authorizationManager: GoogleDriveAuthorizationManager,
    val transport: DriveRestTransport,
    val client: DriveBackupClient,
)

/** Application-scoped runtime used both by backup screens and WorkManager after process recreation. */
object BackupDriveContainer {
    private val tokenProvider = DriveTokenProvider()
    private val authorizationMutexes = ConcurrentHashMap<String, Mutex>()

    fun runtime(context: Context, userId: String): DriveRuntime {
        val applicationContext = context.applicationContext
        val connections = DriveConnectionRepository(applicationContext)
        val authorization = ProductionGoogleDriveAuthorizationManager(applicationContext)
        val transport = KtorDriveRestTransport()
        val metadata = connections.get(userId)
        val client = when {
            userId.isBlank() -> UnavailableDriveBackupClient
            metadata == null -> StateDriveBackupClient(DriveConnectionState.Disconnected)
            metadata.requiresReauthorization || !tokenProvider.has(userId) -> StateDriveBackupClient(DriveConnectionState.ReauthorizationRequired)
            else -> GoogleDriveRestClient(
                tokenProvider = { tokenProvider.get(userId) },
                driveAccountIdProvider = { connections.get(userId)?.accountId },
                transport = transport,
                onAuthorizationInvalid = {
                    tokenProvider.clear(userId)
                    connections.markReauthorizationRequired(userId)
                },
            )
        }
        return DriveRuntime(connections, authorization, transport, client)
    }

    suspend fun authorizationState(context: Context, userId: String): DriveConnectionState {
        val metadata = DriveConnectionRepository(context.applicationContext).get(userId) ?: return DriveConnectionState.Disconnected
        if (metadata.requiresReauthorization) return DriveConnectionState.ReauthorizationRequired
        return if (tokenProvider.get(userId).isNullOrBlank()) {
            DriveConnectionState.ReauthorizationRequired
        } else {
            DriveConnectionState.Connected(metadata.accountId, metadata.accountEmail)
        }
    }

    suspend fun beginAuthorization(context: Context, userId: String, changeAccount: Boolean): DriveAuthorizationOutcome =
        authorizationMutexes.getOrPut(userId) { Mutex() }.withLock {
            runtime(context, userId).authorizationManager.authorize(forceAccountSelection = changeAccount)
        }

    suspend fun completeAuthorization(context: Context, userId: String, resultIntent: Intent?): DriveConnectionState {
        val runtime = runtime(context, userId)
        return persistOutcome(userId, runtime, runtime.authorizationManager.completeAuthorization(resultIntent))
    }

    suspend fun persistAuthorization(context: Context, userId: String, outcome: DriveAuthorizationOutcome): DriveConnectionState {
        val runtime = runtime(context, userId)
        return persistOutcome(userId, runtime, outcome)
    }

    private suspend fun persistOutcome(
        userId: String,
        runtime: DriveRuntime,
        outcome: DriveAuthorizationOutcome,
    ): DriveConnectionState = when (outcome) {
        is DriveAuthorizationOutcome.Authorized -> {
            tokenProvider.put(userId, outcome.session.accessToken)
            try {
                val metadata = resolveDriveConnectionMetadata(runtime.transport, outcome.session)
                runtime.connectionRepository.save(userId, metadata)
                val verifiedClient = GoogleDriveRestClient(
                    tokenProvider = { tokenProvider.get(userId) },
                    driveAccountIdProvider = { metadata.accountId },
                    transport = runtime.transport,
                    onAuthorizationInvalid = {
                        tokenProvider.clear(userId)
                        runtime.connectionRepository.markReauthorizationRequired(userId)
                    },
                )
                DriveFolderRepository(verifiedClient).resolve().also { folders ->
                    runtime.connectionRepository.updateFolders(userId, folders.rootId, folders.backupsId)
                }
                DriveConnectionState.Connected(metadata.accountId, metadata.accountEmail)
            } catch (error: Exception) {
                tokenProvider.clear(userId)
                if (error is DriveBackupException.ReauthorizationRequired) {
                    runtime.connectionRepository.markReauthorizationRequired(userId)
                }
                driveConnectionStateFor(error)
            }
        }
        is DriveAuthorizationOutcome.ResolutionRequired -> DriveConnectionState.Authorizing
        DriveAuthorizationOutcome.Cancelled -> DriveConnectionState.Disconnected
        DriveAuthorizationOutcome.ScopeDenied -> DriveConnectionState.AuthorizationRequired
        DriveAuthorizationOutcome.PlayServicesUnavailable -> DriveConnectionState.NotConfigured
        DriveAuthorizationOutcome.TemporarilyUnavailable -> DriveConnectionState.TemporarilyUnavailable
    }

    suspend fun disconnect(context: Context, userId: String, revoke: Boolean): DriveConnectionState {
        val runtime = runtime(context, userId)
        val token = tokenProvider.clear(userId)
        if (revoke && token != null) runtime.authorizationManager.clearCachedToken(token)
        if (revoke) runCatching { runtime.authorizationManager.revokeAccess(runtime.connectionRepository.get(userId)?.accountId.orEmpty()) }
        BackupSchedulerBridge.cancelDriveWork(context, userId)
        runtime.connectionRepository.clear(userId)
        return DriveConnectionState.Disconnected
    }
}

internal suspend fun resolveDriveConnectionMetadata(
    transport: DriveRestTransport,
    session: DriveAuthorizationSession,
): DriveConnectionMetadata {
    val user = transport.getCurrentUser(session.accessToken)
    if (user.permissionId.isBlank()) throw DriveBackupException.InvalidRequest()
    return DriveConnectionMetadata(
        accountId = user.permissionId,
        accountEmail = user.emailAddress,
        connectedAt = System.currentTimeMillis(),
    )
}

internal fun driveConnectionStateFor(error: Throwable): DriveConnectionState = when (error) {
    is DriveHttpException.Unauthorized,
    is DriveBackupException.ReauthorizationRequired -> DriveConnectionState.ReauthorizationRequired
    is DriveHttpException.NotConfigured,
    is DriveBackupException.NotConfigured -> DriveConnectionState.NotConfigured
    is DriveHttpException.PermissionDenied,
    is DriveBackupException.PermissionDenied,
    is DriveBackupException.NotConnected,
    is DriveBackupException.AccountMismatch -> DriveConnectionState.AuthorizationRequired
    is DriveHttpException.BadRequest,
    is DriveBackupException.InvalidRequest,
    is DriveBackupException.Permanent -> DriveConnectionState.InvalidConfiguration
    is DriveBackupException.Retryable -> DriveConnectionState.TemporarilyUnavailable
    else -> DriveConnectionState.TemporarilyUnavailable
}

/** Keeps Drive code decoupled from WorkManager internals for unit tests. */
internal object BackupSchedulerBridge {
    fun cancelDriveWork(context: Context, userId: String) = app.tijario.features.backup.BackupScheduler.cancelDriveUploads(context, userId)
}

private class StateDriveBackupClient(private val state: DriveConnectionState) : DriveBackupClient by UnavailableDriveBackupClient {
    override suspend fun connectionState(): DriveConnectionState = state
}
