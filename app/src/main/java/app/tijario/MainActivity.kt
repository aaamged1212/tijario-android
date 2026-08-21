package app.tijario

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.tijario.config.AuthDeepLinkPolicy
import app.tijario.config.AppLanguage
import app.tijario.config.AppRuntimeState
import app.tijario.config.LocalLanguage
import app.tijario.features.affiliate.GoMarketMeAffiliate
import app.tijario.features.notifications.NotificationDeepLinkState
import app.tijario.features.notifications.ensureAnnouncementNotificationChannel
import app.tijario.features.backup.ensureBackupNotificationChannel
import app.tijario.features.play.GooglePlayEngagementPrompter
import app.tijario.ui.TijarioApp
import app.tijario.ui.theme.TijarioTheme

class MainActivity : ComponentActivity() {
    private lateinit var playStorePrompter: GooglePlayEngagementPrompter
    private val updateActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppRuntimeState.restorePreferences(applicationContext)
        ensureAnnouncementNotificationChannel(applicationContext)
        ensureBackupNotificationChannel(applicationContext)
        app.tijario.analytics.TijarioAnalytics.initialize(applicationContext)
        GoMarketMeAffiliate.initialize(applicationContext)
        NotificationDeepLinkState.handleUri(intent?.data)
        handleAuthDeepLink(intent)
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        playStorePrompter = GooglePlayEngagementPrompter(this, updateActivityResultLauncher)

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
                    TijarioApp(playStorePrompter)
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
        AuthDeepLinkPolicy.resolveTarget(intent?.data?.toString())?.let(AppRuntimeState::updateAuthDeepLinkTarget)
    }

    override fun onDestroy() {
        if (::playStorePrompter.isInitialized) {
            playStorePrompter.dispose()
        }
        super.onDestroy()
    }
}
