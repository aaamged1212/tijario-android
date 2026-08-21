package app.tijario.features.play

/**
 * Local pacing for Google Play prompts. Google Play remains the authority on
 * whether a review dialog is actually displayed.
 */
internal object GooglePlayPromptPolicy {
    const val MIN_PROMPT_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L

    fun shouldStartFlexibleUpdate(
        nowMillis: Long,
        lastAttemptMillis: Long,
        updateAvailable: Boolean,
        flexibleUpdateAllowed: Boolean,
    ): Boolean = updateAvailable &&
        flexibleUpdateAllowed &&
        isOutsidePromptInterval(nowMillis, lastAttemptMillis)

    fun shouldRequestReview(
        nowMillis: Long,
        lastAttemptMillis: Long,
        hasMeaningfulUse: Boolean,
    ): Boolean = hasMeaningfulUse && isOutsidePromptInterval(nowMillis, lastAttemptMillis)

    private fun isOutsidePromptInterval(nowMillis: Long, lastAttemptMillis: Long): Boolean =
        lastAttemptMillis <= 0L || nowMillis - lastAttemptMillis >= MIN_PROMPT_INTERVAL_MILLIS
}
