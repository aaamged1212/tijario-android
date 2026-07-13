package app.tijario.config

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Process-level UI state that must not be owned by an Activity companion object.
 * Persistent preferences are restored whenever the process creates MainActivity.
 */
object AppRuntimeState {
    var currentLanguage by mutableStateOf(AppLanguage.AR)
    var isDarkMode by mutableStateOf(false)

    var authDeepLinkTarget by mutableStateOf<String?>(null)
        private set

    fun restorePreferences(context: Context) {
        val appContext = context.applicationContext
        currentLanguage = AppPreferences.getLanguage(appContext)
        isDarkMode = AppPreferences.getDarkMode(appContext)
    }

    fun setAuthDeepLinkTarget(target: String?) {
        authDeepLinkTarget = target
    }

    fun consumeAuthDeepLinkTarget() {
        authDeepLinkTarget = null
    }
}
