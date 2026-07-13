package app.tijario.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import app.tijario.BuildConfig
import com.facebook.appevents.AppEventsLogger

object TijarioAnalytics {
    @Volatile
    private var enabled: Boolean = false
    private var logger: AppEventsLogger? = null

    fun initialize(context: Context) {
        runCatching {
            logger = AppEventsLogger.newLogger(context.applicationContext)
        }.onFailure { error ->
            if (BuildConfig.DEBUG) Log.w("TijarioAnalytics", "Meta logger initialization failed", error)
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun logEvent(name: String, params: Bundle? = null) {
        if (!enabled) return
        runCatching {
            logger?.logEvent(name, params)
        }.onFailure { error ->
            if (BuildConfig.DEBUG) Log.w("TijarioAnalytics", "Analytics event failed: $name", error)
        }
    }
}
