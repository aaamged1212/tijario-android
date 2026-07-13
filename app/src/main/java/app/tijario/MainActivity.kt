package app.tijario

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.tijario.config.AppLanguage
import app.tijario.config.AppRuntimeState
import app.tijario.config.LocalLanguage
import app.tijario.features.notifications.NotificationDeepLinkState
import app.tijario.features.notifications.ensureAnnouncementNotificationChannel
import app.tijario.ui.TijarioApp
import app.tijario.ui.theme.TijarioTheme

class MainActivity : ComponentActivity() {
    /**
     * Compatibility facade for existing screens. Runtime values are owned by
     * AppRuntimeState rather than the Activity companion/lifecycle.
     */
    companion object {
        var currentLanguage: AppLanguage
            get() = AppRuntimeState.currentLanguage
            set(value) {
                AppRuntimeState.currentLanguage = value
            }

        var isDarkMode: Boolean
            get() = AppRuntimeState.isDarkMode
            set(value) {
                AppRuntimeState.isDarkMode = value
            }

        val authDeepLinkTarget: String?
            get() = AppRuntimeState.authDeepLinkTarget

        fun consumeAuthDeepLinkTarget() {
            AppRuntimeState.consumeAuthDeepLinkTarget()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppRuntimeState.restorePreferences(applicationContext)
        ensureAnnouncementNotificationChannel(applicationContext)
        app.tijario.analytics.TijarioAnalytics.initialize(applicationContext)
        NotificationDeepLinkState.handleUri(intent?.data)
        handleAuthDeepLink(intent)
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        setContent {
            TijarioTheme(
                darkTheme = AppRuntimeState.isDarkMode,
                language = AppRuntimeState.currentLanguage,
            ) {
                val layoutDirection = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection,
                    LocalLanguage provides AppRuntimeState.currentLanguage,
                ) {
                    TijarioApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        NotificationDeepLinkState.handleUri(intent.data)
        handleAuthDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val isSupportedScheme = uri.scheme == "tijario" || uri.scheme == "com.tijario.app"
        val isAuthCallback = uri.host == "auth" && uri.path.orEmpty().startsWith("/callback")
        if (!isSupportedScheme || !isAuthCallback) return

        AppRuntimeState.setAuthDeepLinkTarget(
            uri.getQueryParameter("next")?.takeIf { it.startsWith("/") } ?: "/login",
        )
    }
}
