package app.tijario.features.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.tijario.MainActivity
import app.tijario.R
import app.tijario.config.AppPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TijarioFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val announcementId = message.data["announcement_id"]?.trim().orEmpty()
        if (announcementId.isBlank()) return

        ensureNotificationChannel()

        val title = message.notification?.title ?: message.data["title"].orEmpty().ifBlank { "Tijario" }
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        val deepLink = message.data["deep_link"]?.takeIf { it.startsWith("tijario://announcements") }
            ?: "tijario://announcements/$announcementId"

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse(deepLink)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            announcementId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, ANNOUNCEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(this).notify(announcementId.hashCode(), notification)
        }
    }

    override fun onNewToken(token: String) {
        serviceScope.launch {
            val language = AppPreferences.getLanguage(applicationContext)
            NotificationTopicManager(applicationContext).syncForLanguage(language)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(ANNOUNCEMENT_CHANNEL_ID)
        if (existing != null) return

        val channelName = if (AppPreferences.getLanguage(this) == app.tijario.config.AppLanguage.AR) {
            "إعلانات تجاريو"
        } else {
            "Tijario Announcements"
        }

        manager.createNotificationChannel(
            NotificationChannel(
                ANNOUNCEMENT_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }
}
