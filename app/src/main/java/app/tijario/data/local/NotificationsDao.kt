package app.tijario.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsDao {
    @Query("SELECT * FROM announcements_cache WHERE user_id = :userId ORDER BY priority DESC, published_at DESC")
    fun observeAnnouncements(userId: String): Flow<List<AnnouncementEntity>>

    @Query("SELECT COUNT(*) FROM announcements_cache WHERE user_id = :userId AND is_read = 0 AND is_dismissed = 0")
    fun observeUnreadCount(userId: String): Flow<Int>

    @Query("SELECT * FROM announcements_cache WHERE user_id = :userId AND id = :announcementId LIMIT 1")
    suspend fun getAnnouncement(userId: String, announcementId: String): AnnouncementEntity?

    @Query("SELECT * FROM announcements_cache WHERE user_id = :userId AND is_seen = 0 AND is_read = 0 AND is_dismissed = 0 ORDER BY priority DESC, published_at DESC LIMIT 1")
    suspend fun getStartupAnnouncement(userId: String): AnnouncementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnnouncements(items: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements_cache WHERE user_id = :userId AND id NOT IN (:remoteIds)")
    suspend fun pruneMissingAnnouncements(userId: String, remoteIds: List<String>)

    @Query("DELETE FROM announcements_cache WHERE user_id = :userId")
    suspend fun deleteAnnouncementsForUser(userId: String)

    @Query("UPDATE announcements_cache SET is_seen = 1 WHERE user_id = :userId AND id = :announcementId")
    suspend fun markSeenLocal(userId: String, announcementId: String)

    @Query("UPDATE announcements_cache SET is_seen = 1, is_read = 1 WHERE user_id = :userId AND id = :announcementId")
    suspend fun markReadLocal(userId: String, announcementId: String)

    @Query("UPDATE announcements_cache SET is_seen = 1, is_dismissed = 1 WHERE user_id = :userId AND id = :announcementId")
    suspend fun markDismissedLocal(userId: String, announcementId: String)

    @Query("UPDATE announcements_cache SET is_seen = 1, is_read = 1 WHERE user_id = :userId")
    suspend fun markAllReadLocal(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReceiptOutbox(item: AnnouncementReceiptOutboxEntity)

    @Query("SELECT * FROM announcement_receipt_outbox WHERE user_id = :userId AND status = 'PENDING' ORDER BY created_at ASC")
    suspend fun getPendingReceiptOperations(userId: String): List<AnnouncementReceiptOutboxEntity>

    @Query("DELETE FROM announcement_receipt_outbox WHERE id = :id")
    suspend fun deleteReceiptOutbox(id: String)

    @Query("UPDATE announcement_receipt_outbox SET attempts = attempts + 1, last_error = :error WHERE id = :id")
    suspend fun markReceiptOutboxFailed(id: String, error: String?)

    @Query("DELETE FROM announcement_receipt_outbox WHERE user_id = :userId")
    suspend fun deleteReceiptOutboxForUser(userId: String)
}
