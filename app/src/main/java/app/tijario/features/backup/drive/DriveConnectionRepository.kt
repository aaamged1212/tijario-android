package app.tijario.features.backup.drive

import android.content.Context

sealed interface DriveAuthorizationState {
    data object NotConfigured : DriveAuthorizationState
    data object Disconnected : DriveAuthorizationState
    data object AuthorizationRequired : DriveAuthorizationState
    data object Authorizing : DriveAuthorizationState
    data class Connected(val accountId: String, val accountEmail: String?) : DriveAuthorizationState
    data object ReauthorizationRequired : DriveAuthorizationState
    data object TemporarilyUnavailable : DriveAuthorizationState
}

data class DriveConnectionMetadata(
    val accountId: String,
    val accountEmail: String?,
    val connectedAt: Long,
    val rootFolderId: String? = null,
    val backupsFolderId: String? = null,
    val requiresReauthorization: Boolean = false,
)

/** Persists only non-secret Drive connection metadata; access tokens never enter preferences. */
class DriveConnectionRepository(context: Context) {
    private val preferences = context.getSharedPreferences("tijario_drive_connection", Context.MODE_PRIVATE)

    fun get(userId: String): DriveConnectionMetadata? {
        val prefix = prefix(userId)
        val accountId = preferences.getString("${prefix}account_id", null) ?: return null
        return DriveConnectionMetadata(
            accountId = accountId,
            accountEmail = preferences.getString("${prefix}account_email", null),
            connectedAt = preferences.getLong("${prefix}connected_at", 0L),
            rootFolderId = preferences.getString("${prefix}root_folder_id", null),
            backupsFolderId = preferences.getString("${prefix}backups_folder_id", null),
            requiresReauthorization = preferences.getBoolean("${prefix}reauthorization_required", false),
        )
    }

    fun save(userId: String, metadata: DriveConnectionMetadata) {
        require(userId.isNotBlank() && metadata.accountId.isNotBlank())
        val prefix = prefix(userId)
        preferences.edit()
            .putString("${prefix}account_id", metadata.accountId)
            .putString("${prefix}account_email", metadata.accountEmail)
            .putLong("${prefix}connected_at", metadata.connectedAt)
            .putString("${prefix}root_folder_id", metadata.rootFolderId)
            .putString("${prefix}backups_folder_id", metadata.backupsFolderId)
            .putBoolean("${prefix}reauthorization_required", metadata.requiresReauthorization)
            .apply()
    }

    fun markReauthorizationRequired(userId: String) {
        get(userId)?.let { save(userId, it.copy(requiresReauthorization = true)) }
    }

    fun clear(userId: String) {
        val prefix = prefix(userId)
        preferences.edit().apply {
            listOf("account_id", "account_email", "connected_at", "root_folder_id", "backups_folder_id", "reauthorization_required")
                .forEach { remove("$prefix$it") }
        }.apply()
    }

    private fun prefix(userId: String) = "${userId}_"
}
