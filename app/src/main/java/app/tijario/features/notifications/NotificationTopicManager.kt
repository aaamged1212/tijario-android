package app.tijario.features.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NotificationTopicManager(
    private val context: Context,
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) {
    fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    suspend fun syncForLanguage(language: AppLanguage) {
        if (!AppPreferences.isPushEnabled(context) || !hasNotificationPermission()) {
            unsubscribeCurrent()
            return
        }

        val targetTopic = topicForLanguage(language)
        val currentTopic = AppPreferences.getSubscribedTopic(context)
        if (currentTopic == targetTopic) return

        if (!currentTopic.isNullOrBlank()) {
            runCatching { messaging.unsubscribeFromTopic(currentTopic).awaitTask() }
        }

        messaging.subscribeToTopic(targetTopic).awaitTask()
        AppPreferences.setSubscribedTopic(context, targetTopic)
    }

    suspend fun unsubscribeCurrent() {
        val currentTopic = AppPreferences.getSubscribedTopic(context) ?: return
        runCatching { messaging.unsubscribeFromTopic(currentTopic).awaitTask() }
        AppPreferences.setSubscribedTopic(context, null)
    }
}

private suspend fun Task<Void>.awaitTask() {
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(task.exception ?: IllegalStateException("Firebase task failed"))
            }
        }
    }
}
