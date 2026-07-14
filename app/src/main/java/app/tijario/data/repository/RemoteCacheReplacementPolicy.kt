package app.tijario.data.repository

internal object RemoteCacheReplacementPolicy {
    private val protectedLocalStatuses = setOf(
        "LOCAL_ONLY",
        "PENDING_SYNC",
        "PENDING_DELETE",
        "CONFLICT",
    )

    fun shouldReplace(localSyncStatus: String?): Boolean =
        localSyncStatus == null || localSyncStatus !in protectedLocalStatuses
}
