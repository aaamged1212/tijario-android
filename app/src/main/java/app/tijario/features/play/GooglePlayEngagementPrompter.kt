package app.tijario.features.play

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Coordinates official Google Play update and review prompts without storing rating data. */
class GooglePlayEngagementPrompter(
    private val activity: ComponentActivity,
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest>,
) {
    private val preferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val reviewManager = ReviewManagerFactory.create(activity)
    private val _flexibleUpdateDownloaded = MutableStateFlow(false)
    val flexibleUpdateDownloaded: StateFlow<Boolean> = _flexibleUpdateDownloaded.asStateFlow()

    private var updateFlowInProgress = false
    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            _flexibleUpdateDownloaded.value = true
        }
    }

    init {
        appUpdateManager.registerListener(installStateListener)
    }

    fun maybeStartFlexibleUpdate() {
        if (updateFlowInProgress) return

        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            val now = System.currentTimeMillis()
            val canStart = GooglePlayPromptPolicy.shouldStartFlexibleUpdate(
                nowMillis = now,
                lastAttemptMillis = preferences.getLong(KEY_LAST_UPDATE_ATTEMPT, 0L),
                updateAvailable = updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE,
                flexibleUpdateAllowed = updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
            )
            if (!canStart) return@addOnSuccessListener

            preferences.edit().putLong(KEY_LAST_UPDATE_ATTEMPT, now).apply()
            updateFlowInProgress = true
            try {
                appUpdateManager.startUpdateFlowForResult(
                    updateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                )
            } catch (_: Exception) {
                // The update check is optional; the app remains usable if Google Play cannot launch it.
            } finally {
                updateFlowInProgress = false
            }
        }
    }

    fun maybeRequestReview(hasMeaningfulUse: Boolean) {
        val now = System.currentTimeMillis()
        if (!GooglePlayPromptPolicy.shouldRequestReview(
                nowMillis = now,
                lastAttemptMillis = preferences.getLong(KEY_LAST_REVIEW_ATTEMPT, 0L),
                hasMeaningfulUse = hasMeaningfulUse,
            )
        ) return

        // Record an attempt only. Google Play intentionally does not reveal whether a review was submitted.
        preferences.edit().putLong(KEY_LAST_REVIEW_ATTEMPT, now).apply()
        startReviewFlow()
    }

    fun requestReviewFromUserAction() {
        startReviewFlow()
    }

    private fun startReviewFlow() {
        reviewManager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
            reviewManager.launchReviewFlow(activity, reviewInfo)
        }
    }

    fun completeFlexibleUpdate() {
        _flexibleUpdateDownloaded.value = false
        appUpdateManager.completeUpdate()
    }

    fun dispose() {
        appUpdateManager.unregisterListener(installStateListener)
    }

    private companion object {
        const val PREFERENCES_NAME = "tijario_app_preferences"
        const val KEY_LAST_UPDATE_ATTEMPT = "last_play_update_attempt_time"
        const val KEY_LAST_REVIEW_ATTEMPT = "last_play_review_attempt_time"
    }
}
