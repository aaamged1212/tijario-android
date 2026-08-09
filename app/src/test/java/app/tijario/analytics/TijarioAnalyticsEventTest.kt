package app.tijario.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TijarioAnalyticsEventTest {
    @Test
    fun generatedReplyAndCaptionUseDifferentPrivacySafeEventNames() {
        assertEquals("tijario_ai_reply_generated", TijarioAnalyticsEvent.AiReplyGenerated.wireName)
        assertEquals("tijario_ai_caption_generated", TijarioAnalyticsEvent.AiCaptionGenerated.wireName)
        assertNotEquals(
            TijarioAnalyticsEvent.AiReplyGenerated.wireName,
            TijarioAnalyticsEvent.AiCaptionGenerated.wireName,
        )
    }

    @Test
    fun operationalEventsAreCentralizedWithoutPayloadContent() {
        assertEquals("tijario_customer_created", TijarioAnalyticsEvent.CustomerCreated.wireName)
        assertEquals("tijario_product_created", TijarioAnalyticsEvent.ProductCreated.wireName)
        assertEquals("tijario_invoice_created", TijarioAnalyticsEvent.InvoiceCreated.wireName)
        assertEquals("tijario_quote_created", TijarioAnalyticsEvent.QuoteCreated.wireName)
        assertEquals("tijario_subscription_started", TijarioAnalyticsEvent.SubscriptionStarted.wireName)
    }
}
