package app.tijario.features.notifications

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object NotificationDeepLinkState {
    var pendingAnnouncementId by mutableStateOf<String?>(null)
        private set

    fun handleUri(uri: Uri?) {
        pendingAnnouncementId = extractAnnouncementId(uri) ?: pendingAnnouncementId
    }

    fun consumeAnnouncementId(): String? {
        val value = pendingAnnouncementId
        pendingAnnouncementId = null
        return value
    }

    fun safeAnnouncementDeepLink(announcementId: String): Uri =
        Uri.parse("tijario://announcements/$announcementId")

    fun extractAnnouncementId(uri: Uri?): String? {
        if (uri == null) return null
        if (uri.scheme != "tijario") return null
        if (uri.host != "announcements") return null
        val id = uri.pathSegments.firstOrNull()?.trim().orEmpty()
        return id.takeIf { it.matches(Regex("[A-Za-z0-9_-]{8,64}")) }
    }
}
