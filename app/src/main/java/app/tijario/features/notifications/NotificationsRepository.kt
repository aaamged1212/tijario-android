package app.tijario.features.notifications

import android.content.Context
import androidx.room.withTransaction
import app.tijario.data.local.AnnouncementReceiptOutboxEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.remote.AnnouncementReceiptRequest
import app.tijario.data.remote.BackendApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class NotificationsRepository(
    private val context: Context,
    private val database: TijarioDatabase,
    private val backendApiClient: BackendApiClient,
) {
    private val dao = database.notificationsDao()

    fun observeAnnouncements(userId: String): Flow<List<Announcement>> =
        dao.observeAnnouncements(userId).map { rows -> rows.map { it.toAnnouncement() } }

    fun observeUnreadCount(userId: String): Flow<Int> = dao.observeUnreadCount(userId)

    suspend fun refresh(userId: String): Result<Unit> = runCatching {
        val response = backendApiClient.fetchAnnouncementsBootstrap()
        if (!response.ok || response.data == null) {
            error(response.message ?: "notifications_refresh_failed")
        }

        val syncedAt = System.currentTimeMillis()
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val entities = response.data.items.map { dto ->
                    dto.toEntity(userId, dao.getAnnouncement(userId, dto.id), syncedAt)
                }
                dao.upsertAnnouncements(entities)
                if (entities.isNotEmpty()) {
                    dao.pruneMissingAnnouncements(userId, entities.map { it.id })
                } else {
                    dao.deleteAnnouncementsForUser(userId)
                }
            }
        }
        syncPendingReceipts(userId)
    }

    suspend fun startupAnnouncement(userId: String): Announcement? =
        withContext(Dispatchers.IO) { dao.getStartupAnnouncement(userId)?.toAnnouncement() }

    suspend fun markSeen(userId: String, announcementId: String, openedFrom: String = "startup") {
        withContext(Dispatchers.IO) { dao.markSeenLocal(userId, announcementId) }
        sendReceiptOrQueue(userId, announcementId, "seen", openedFrom)
    }

    suspend fun markRead(userId: String, announcementId: String, openedFrom: String = "inbox") {
        withContext(Dispatchers.IO) { dao.markReadLocal(userId, announcementId) }
        sendReceiptOrQueue(userId, announcementId, "read", openedFrom)
    }

    suspend fun dismiss(userId: String, announcementId: String) {
        withContext(Dispatchers.IO) { dao.markDismissedLocal(userId, announcementId) }
        sendReceiptOrQueue(userId, announcementId, "dismissed", "startup")
    }

    suspend fun markAllRead(userId: String) {
        withContext(Dispatchers.IO) { dao.markAllReadLocal(userId) }
        runCatching { backendApiClient.markAllAnnouncementsRead() }
            .onFailure { NotificationReceiptSyncScheduler(context).trigger(userId) }
    }

    suspend fun syncPendingReceipts(userId: String): Result<Unit> = runCatching {
        val pending = withContext(Dispatchers.IO) { dao.getPendingReceiptOperations(userId) }
        pending.forEach { item ->
            val result = runCatching {
                backendApiClient.sendAnnouncementReceipt(
                    AnnouncementReceiptRequest(
                        announcementId = item.announcementId,
                        event = item.event,
                        openedFrom = item.openedFrom,
                        clientRequestId = item.id,
                    )
                )
            }

            val apiResult = result.getOrNull()
            if (result.isSuccess && apiResult?.ok == true) {
                withContext(Dispatchers.IO) { dao.deleteReceiptOutbox(item.id) }
            } else {
                withContext(Dispatchers.IO) {
                    dao.markReceiptOutboxFailed(item.id, apiResult?.code ?: result.exceptionOrNull()?.message)
                }
            }
        }
    }

    suspend fun clearForUser(userId: String) {
        withContext(Dispatchers.IO) {
            dao.deleteAnnouncementsForUser(userId)
            dao.deleteReceiptOutboxForUser(userId)
        }
    }

    private suspend fun sendReceiptOrQueue(
        userId: String,
        announcementId: String,
        event: String,
        openedFrom: String,
    ) {
        val clientRequestId = UUID.randomUUID().toString()
        val response = runCatching {
            backendApiClient.sendAnnouncementReceipt(
                AnnouncementReceiptRequest(
                    announcementId = announcementId,
                    event = event,
                    openedFrom = openedFrom,
                    clientRequestId = clientRequestId,
                )
            )
        }.getOrNull()

        if (response?.ok == true) return

        withContext(Dispatchers.IO) {
            dao.upsertReceiptOutbox(
                AnnouncementReceiptOutboxEntity(
                    id = clientRequestId,
                    userId = userId,
                    announcementId = announcementId,
                    event = event,
                    openedFrom = openedFrom,
                    status = "PENDING",
                    attempts = 0,
                    createdAt = System.currentTimeMillis(),
                    lastError = response?.code,
                )
            )
        }
        NotificationReceiptSyncScheduler(context).trigger(userId)
    }
}
