package app.tijario.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "announcements_cache",
    primaryKeys = ["user_id", "id"],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "published_at"]),
        Index(value = ["user_id", "is_read"]),
        Index(value = ["user_id", "is_dismissed"])
    ]
)
data class AnnouncementEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    val id: String,
    @ColumnInfo(name = "title_ar") val titleAr: String,
    @ColumnInfo(name = "body_ar") val bodyAr: String,
    @ColumnInfo(name = "title_en") val titleEn: String,
    @ColumnInfo(name = "body_en") val bodyEn: String,
    @ColumnInfo(name = "action_label_ar") val actionLabelAr: String?,
    @ColumnInfo(name = "action_label_en") val actionLabelEn: String?,
    @ColumnInfo(name = "deep_link") val deepLink: String?,
    val priority: Int,
    @ColumnInfo(name = "published_at") val publishedAt: String?,
    @ColumnInfo(name = "expires_at") val expiresAt: String?,
    @ColumnInfo(name = "is_read") val isRead: Boolean,
    @ColumnInfo(name = "is_seen") val isSeen: Boolean,
    @ColumnInfo(name = "is_dismissed") val isDismissed: Boolean,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long,
)

@Entity(
    tableName = "announcement_receipt_outbox",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "announcement_id"]),
        Index(value = ["status"])
    ]
)
data class AnnouncementReceiptOutboxEntity(
    @androidx.room.PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "announcement_id") val announcementId: String,
    val event: String,
    @ColumnInfo(name = "opened_from") val openedFrom: String?,
    val status: String,
    val attempts: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_error") val lastError: String?,
)
