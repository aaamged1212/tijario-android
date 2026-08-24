package app.tijario.features.notifications

import android.content.Context
import androidx.room.withTransaction
import app.tijario.config.AppPreferences
import app.tijario.data.local.AnnouncementReceiptOutboxEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.remote.AnnouncementReceiptRequest
import app.tijario.data.remote.BackendApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import java.time.Instant

class NotificationsRepository(
    private val context: Context,
    private val database: TijarioDatabase,
    private val backendApiClient: BackendApiClient,
) {
    private val dao = database.notificationsDao()

    fun observeAnnouncements(userId: String): Flow<List<Announcement>> =
        dao.observeAnnouncements(userId).map { rows -> rows.map { it.toAnnouncement() } }

    fun observeUnreadCount(userId: String): Flow<Int> = dao.observeUnreadCount(userId)

    suspend fun refresh(userId: String, force: Boolean = true): Result<Unit> = runCatching {
        if (!force && AppPreferences.isAnnouncementsFresh(context, userId, ANNOUNCEMENTS_TTL_MS)) {
            syncPendingReceipts(userId).getOrThrow()
            return@runCatching
        }

        val response = backendApiClient.fetchAnnouncementsBootstrap()
        if (!response.ok || response.data == null) {
            error(response.code ?: "notifications_refresh_failed")
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
                    dao.pruneMissingAnnouncements(userId, listOf("__no_remote_announcements__"))
                }
            }
        }
        AppPreferences.setAnnouncementsSynced(context, userId)
        syncPendingReceipts(userId)
    }

    suspend fun startupAnnouncement(userId: String): Announcement? =
        withContext(Dispatchers.IO) { dao.getStartupAnnouncement(userId)?.toAnnouncement() }

    suspend fun markSeen(userId: String, announcementId: String, openedFrom: String = "startup") {
        val isLocal = withContext(Dispatchers.IO) { dao.getAnnouncement(userId, announcementId)?.isLocal == true }
        withContext(Dispatchers.IO) { dao.markSeenLocal(userId, announcementId) }
        if (isLocal) return
        sendReceiptOrQueue(userId, announcementId, "seen", openedFrom)
    }

    suspend fun markRead(userId: String, announcementId: String, openedFrom: String = "inbox") {
        val isLocal = withContext(Dispatchers.IO) { dao.getAnnouncement(userId, announcementId)?.isLocal == true }
        withContext(Dispatchers.IO) { dao.markReadLocal(userId, announcementId) }
        if (isLocal) return
        sendReceiptOrQueue(userId, announcementId, "read", openedFrom)
    }

    suspend fun dismiss(userId: String, announcementId: String) {
        val isLocal = withContext(Dispatchers.IO) { dao.getAnnouncement(userId, announcementId)?.isLocal == true }
        withContext(Dispatchers.IO) { dao.markDismissedLocal(userId, announcementId) }
        if (isLocal) return
        sendReceiptOrQueue(userId, announcementId, "dismissed", "startup")
    }

    suspend fun recordManualPaymentRequest(
        userId: String,
        planName: String,
        paymentMethod: String,
        interval: String,
    ) {
        val now = Instant.now().toString()
        withContext(Dispatchers.IO) {
            dao.upsertAnnouncement(
                app.tijario.data.local.AnnouncementEntity(
                    userId = userId,
                    id = "manual-payment-${UUID.randomUUID()}",
                    titleAr = "تم رفع طلب الدفع",
                    bodyAr = "تم استلام إثبات الدفع لخطة $planName (${if (interval.equals("yearly", true)) "سنوية" else "شهرية"}) عبر $paymentMethod. تستغرق المراجعة والتفعيل عادةً من ساعة إلى ساعتين خلال أوقات الدوام. سيصلك إشعار التفعيل داخل التطبيق وعبر البريد الإلكتروني.",
                    titleEn = "Payment request submitted",
                    bodyEn = "Your $planName ${if (interval.equals("yearly", true)) "yearly" else "monthly"} payment proof via $paymentMethod was received. Verification and activation usually take one to two hours during business hours. You will be notified in the app and by email.",
                    actionLabelAr = null,
                    actionLabelEn = null,
                    deepLink = null,
                    priority = 0,
                    publishedAt = now,
                    expiresAt = null,
                    isRead = false,
                    isSeen = true,
                    isDismissed = false,
                    isLocal = true,
                    lastSyncedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun markAllRead(userId: String) {
        withContext(Dispatchers.IO) { dao.markAllReadLocal(userId) }
        runCatching { backendApiClient.markAllAnnouncementsRead() }
            .onFailure { NotificationReceiptSyncScheduler(context).trigger(userId) }
    }

    suspend fun syncPendingReceipts(userId: String): Result<Unit> = runCatching {
        val pending = withContext(Dispatchers.IO) { dao.getPendingReceiptOperations(userId) }
        var hasFailure = false
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
                hasFailure = true
                withContext(Dispatchers.IO) {
                    dao.markReceiptOutboxFailed(item.id, apiResult?.code ?: result.exceptionOrNull()?.message ?: "notifications_refresh_failed")
                }
            }
        }
        if (hasFailure) {
            error("receipt_sync_pending")
        }
    }

    suspend fun clearForUser(userId: String) {
        withContext(Dispatchers.IO) {
            dao.deleteAnnouncementsForUser(userId)
            dao.deleteReceiptOutboxForUser(userId)
        }
        AppPreferences.clearAnnouncementsSynced(context, userId)
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

    private companion object {
        const val ANNOUNCEMENTS_TTL_MS = 24 * 60 * 60 * 1000L
    }
}
