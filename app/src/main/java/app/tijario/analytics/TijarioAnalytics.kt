package app.tijario.analytics

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger

object TijarioAnalytics {
    private var logger: AppEventsLogger? = null

    fun initialize(context: Context) {
        try {
            logger = AppEventsLogger.newLogger(context)
        } catch (e: Exception) {
            // Safe fallback to prevent crashes if Facebook SDK is missing or fails to init
        }
    }

    fun logEvent(name: String, params: Bundle? = null) {
        try {
            logger?.logEvent(name, params)
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
