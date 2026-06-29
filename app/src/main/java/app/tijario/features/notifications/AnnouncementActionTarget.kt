package app.tijario.features.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Immutable
import app.tijario.config.AppLanguage
import java.net.URI

private val allowedExternalHosts = setOf("tijario.site", "www.tijario.site")

@Immutable
data class AnnouncementActionUiState(
    val label: String,
    val target: String,
)

fun Announcement.actionUiState(language: AppLanguage): AnnouncementActionUiState? {
    val label = actionLabel(language)?.trim().orEmpty().takeIf { it.isNotBlank() } ?: return null
    val target = normalizeAnnouncementActionTarget(deepLink) ?: return null
    return AnnouncementActionUiState(label = label, target = target)
}

fun normalizeAnnouncementActionTarget(rawTarget: String?): String? {
    val value = rawTarget?.trim().orEmpty()
    if (value.isBlank()) return null

    val uri = runCatching { URI(value) }.getOrNull() ?: return null
    return when (uri.scheme?.lowercase()) {
        "tijario" -> normalizeInternalAnnouncementTarget(uri)
        "https" -> normalizeAllowedHttpsTarget(uri, value)
        else -> null
    }
}

fun launchAnnouncementAction(context: Context, target: String): Boolean {
    val intent = runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }.getOrNull() ?: return false

    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

private fun normalizeInternalAnnouncementTarget(uri: URI): String? {
    if (uri.host?.lowercase() != "announcements") return null
    val id = uri.path?.trim('/').orEmpty()
    if (!id.matches(Regex("[A-Za-z0-9_-]{8,64}"))) return null
    return "tijario://announcements/$id"
}

private fun normalizeAllowedHttpsTarget(uri: URI, original: String): String? {
    val host = uri.host?.lowercase() ?: return null
    if (host !in allowedExternalHosts) return null
    return original
}
