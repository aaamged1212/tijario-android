package app.tijario.config

import java.net.URI
import java.net.URLDecoder

internal object AuthDeepLinkPolicy {
    private const val DEFAULT_TARGET = "/login"
    private val trustedWebHosts = setOf("tijario.site", "www.tijario.site")

    fun resolveTarget(rawUri: String?): String? {
        val uri = rawUri?.let { value -> runCatching { URI(value) }.getOrNull() } ?: return null
        if (!isSupportedCallback(uri)) return null
        val next = decodeNext(uri.rawQuery)
        return next?.takeIf(::isSafeInAppPath) ?: DEFAULT_TARGET
    }

    private fun isSupportedCallback(uri: URI): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return false
        val host = uri.host?.lowercase() ?: return false
        return when (scheme) {
            "tijario", "com.tijario.app" -> host == "auth" && uri.path == "/callback"
            "https" -> host in trustedWebHosts && uri.path == "/auth/callback" && uri.userInfo == null && (uri.port == -1 || uri.port == 443)
            else -> false
        }
    }

    private fun decodeNext(rawQuery: String?): String? = rawQuery
        ?.split('&')
        ?.firstOrNull { it.substringBefore('=') == "next" }
        ?.substringAfter('=', missingDelimiterValue = "")
        ?.let { encoded -> runCatching { URLDecoder.decode(encoded, "UTF-8") }.getOrNull() }
        ?.takeIf { it.isNotBlank() }

    private fun isSafeInAppPath(value: String): Boolean {
        if (!value.startsWith('/') || value.startsWith("//") || value.startsWith("/\\")) return false
        val uri = runCatching { URI(value) }.getOrNull() ?: return false
        return uri.scheme == null && uri.host == null && uri.userInfo == null
    }
}
