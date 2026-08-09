package app.tijario.analytics

import android.content.Context
import android.util.Log
import app.tijario.BuildConfig
import com.facebook.appevents.AppEventsLogger

enum class TijarioAnalyticsEvent(val wireName: String) {
    CustomerCreated("tijario_customer_created"),
    ProductCreated("tijario_product_created"),
    InvoiceCreated("tijario_invoice_created"),
    QuoteCreated("tijario_quote_created"),
    AiReplyGenerated("tijario_ai_reply_generated"),
    AiCaptionGenerated("tijario_ai_caption_generated"),
    SubscriptionStarted("tijario_subscription_started"),
}

object TijarioAnalytics {
    @Volatile
    private var enabled: Boolean = false
    private var logger: AppEventsLogger? = null

    fun initialize(context: Context) {
        runCatching {
            logger = AppEventsLogger.newLogger(context.applicationContext)
        }.onFailure { error ->
            if (BuildConfig.DEBUG) Log.w("TijarioAnalytics", "operation=meta_logger_initialize result=failed error=${error.javaClass.simpleName}")
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun logEvent(event: TijarioAnalyticsEvent) {
        if (!enabled) return
        runCatching {
            logger?.logEvent(event.wireName)
        }.onFailure { error ->
            if (BuildConfig.DEBUG) Log.w("TijarioAnalytics", "operation=analytics_event result=failed event=${event.wireName} error=${error.javaClass.simpleName}")
        }
    }
}
