package app.tijario.data.repository

enum class AccountDataMode(val value: String) {
    Uninitialized("uninitialized"),
    LegacyCloud("legacy_cloud"),
    LocalDrive("local_drive"),
    CloudSyncFuture("cloud_sync_future");

    val allowsOperationalWrites: Boolean
        get() = this != Uninitialized

    companion object {
        fun from(value: String?): AccountDataMode = entries.firstOrNull { it.value == value } ?: Uninitialized
    }
}
