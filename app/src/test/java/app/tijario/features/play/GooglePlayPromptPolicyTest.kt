package app.tijario.features.play

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GooglePlayPromptPolicyTest {
    private val now = 2 * GooglePlayPromptPolicy.MIN_PROMPT_INTERVAL_MILLIS

    @Test
    fun flexibleUpdatePromptsWhenAvailableAndAllowedAfterThePacingInterval() {
        assertTrue(
            GooglePlayPromptPolicy.shouldStartFlexibleUpdate(
                nowMillis = now,
                lastAttemptMillis = now - GooglePlayPromptPolicy.MIN_PROMPT_INTERVAL_MILLIS,
                updateAvailable = true,
                flexibleUpdateAllowed = true,
            ),
        )
    }

    @Test
    fun flexibleUpdateDoesNotPromptAgainWithinOneDayOrWhenUnsupported() {
        assertFalse(
            GooglePlayPromptPolicy.shouldStartFlexibleUpdate(
                nowMillis = now,
                lastAttemptMillis = now - 1L,
                updateAvailable = true,
                flexibleUpdateAllowed = true,
            ),
        )
        assertFalse(
            GooglePlayPromptPolicy.shouldStartFlexibleUpdate(
                nowMillis = now,
                lastAttemptMillis = 0L,
                updateAvailable = true,
                flexibleUpdateAllowed = false,
            ),
        )
    }

    @Test
    fun reviewRequestNeedsMeaningfulUseAndIsPacedForOneDay() {
        assertFalse(
            GooglePlayPromptPolicy.shouldRequestReview(
                nowMillis = now,
                lastAttemptMillis = 0L,
                hasMeaningfulUse = false,
            ),
        )
        assertFalse(
            GooglePlayPromptPolicy.shouldRequestReview(
                nowMillis = now,
                lastAttemptMillis = now - 1L,
                hasMeaningfulUse = true,
            ),
        )
        assertTrue(
            GooglePlayPromptPolicy.shouldRequestReview(
                nowMillis = now,
                lastAttemptMillis = now - GooglePlayPromptPolicy.MIN_PROMPT_INTERVAL_MILLIS,
                hasMeaningfulUse = true,
            ),
        )
    }
}
