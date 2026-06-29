package app.tijario

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import app.tijario.ui.TijarioApp
import app.tijario.ui.theme.TijarioTheme
import app.tijario.features.notifications.NotificationDeepLinkState

import android.webkit.WebView
import app.tijario.BuildConfig

class MainActivity : ComponentActivity() {
    companion object {
        var currentLanguage by mutableStateOf(AppLanguage.AR)
        var isDarkMode by mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        NotificationDeepLinkState.handleUri(intent?.data)
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        currentLanguage = AppPreferences.getLanguage(applicationContext)
        isDarkMode = AppPreferences.getDarkMode(applicationContext)
        setContent {
            TijarioTheme(darkTheme = isDarkMode, language = currentLanguage) {
                val layoutDirection = if (currentLanguage == AppLanguage.AR) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection,
                    LocalLanguage provides currentLanguage
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
    }
}
