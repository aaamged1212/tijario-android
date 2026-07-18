package app.tijario.data.repository

enum class AccountDataMode(val value: String) {
    LegacyCloud("legacy_cloud"),
    LocalDrive("local_drive"),
    CloudSyncFuture("cloud_sync_future");

    companion object {
        fun from(value: String?): AccountDataMode = entries.firstOrNull { it.value == value } ?: LegacyCloud
    }
}
