package app.tijario.features.notifications

import app.tijario.config.AppLanguage
import app.tijario.data.local.AnnouncementEntity
import app.tijario.data.remote.AnnouncementDto

const val ANNOUNCEMENT_TOPIC_AR = "tijario_announcements_ar"
const val ANNOUNCEMENT_TOPIC_EN = "tijario_announcements_en"
const val ANNOUNCEMENT_CHANNEL_ID = "tijario_announcements"

data class Announcement(
    val id: String,
    val titleAr: String,
    val bodyAr: String,
    val titleEn: String,
    val bodyEn: String,
    val actionLabelAr: String?,
    val actionLabelEn: String?,
    val deepLink: String?,
    val priority: Int,
    val publishedAt: String?,
    val expiresAt: String?,
    val isRead: Boolean,
    val isSeen: Boolean,
    val isDismissed: Boolean,
) {
    fun title(language: AppLanguage): String = if (language == AppLanguage.AR) titleAr else titleEn
    fun body(language: AppLanguage): String = if (language == AppLanguage.AR) bodyAr else bodyEn
    fun actionLabel(language: AppLanguage): String? = if (language == AppLanguage.AR) actionLabelAr else actionLabelEn
}

fun AnnouncementEntity.toAnnouncement(): Announcement =
    Announcement(
        id = id,
        titleAr = titleAr,
        bodyAr = bodyAr,
        titleEn = titleEn,
        bodyEn = bodyEn,
        actionLabelAr = actionLabelAr,
        actionLabelEn = actionLabelEn,
        deepLink = deepLink,
        priority = priority,
        publishedAt = publishedAt,
        expiresAt = expiresAt,
        isRead = isRead,
        isSeen = isSeen,
        isDismissed = isDismissed,
    )

fun AnnouncementDto.toEntity(userId: String, existing: AnnouncementEntity?, syncedAt: Long): AnnouncementEntity =
    AnnouncementEntity(
        userId = userId,
        id = id,
        titleAr = titleAr,
        bodyAr = bodyAr,
        titleEn = titleEn,
        bodyEn = bodyEn,
        actionLabelAr = actionLabelAr,
        actionLabelEn = actionLabelEn,
        deepLink = deepLink,
        priority = priority,
        publishedAt = publishedAt,
        expiresAt = expiresAt,
        isRead = existing?.isRead == true || isRead,
        isSeen = existing?.isSeen == true || isSeen,
        isDismissed = existing?.isDismissed == true || isDismissed,
        lastSyncedAt = syncedAt,
    )

fun topicForLanguage(language: AppLanguage): String =
    if (language == AppLanguage.AR) ANNOUNCEMENT_TOPIC_AR else ANNOUNCEMENT_TOPIC_EN
