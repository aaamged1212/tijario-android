package app.tijario.features.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences

fun ensureAnnouncementNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (manager.getNotificationChannel(ANNOUNCEMENT_CHANNEL_ID) != null) return

    val channelName = when (AppPreferences.getLanguage(context)) {
        AppLanguage.AR -> "إعلانات تجاريو"
        AppLanguage.EN -> "Tijario Announcements"
    }

    manager.createNotificationChannel(
        NotificationChannel(
            ANNOUNCEMENT_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
    )
}
