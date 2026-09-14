package app.tijario.analytics

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.withTransaction
import app.tijario.BuildConfig
import app.tijario.config.Supabase
import app.tijario.data.local.AnalyticsPendingDailyEntity
import app.tijario.data.local.AnalyticsPendingErrorEntity
import app.tijario.data.local.AnalyticsPendingSessionEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.config.AppPreferences
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * Persists coarse analytics locally and sends a single authenticated RPC batch.
 * It deliberately contains no seller data, document data, customer data, or AI text.
 */
object MobileAnalyticsTracker : DefaultLifecycleObserver {
    private const val ACTIVE_FLUSH_INTERVAL_MS = 5 * 60 * 1000L
    private const val FOREGROUND_DELAY_MS = 15_000L
    private const val MAX_SESSION_SECONDS = 4 * 60 * 60
    private const val MAX_QUEUE_AGE_MS = 7 * 24 * 60 * 60 * 1000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queueMutex = Mutex()
    private lateinit var appContext: Context
    private lateinit var database: TijarioDatabase
    private var initialized = false
    private var isForeground = false
    private var activeSession: ActiveSession? = null
    private var openedForUserId: String? = null
    private var activeFlushJob: Job? = null

    private data class ActiveSession(
        val userId: String,
        val installationId: String,
        val sessionId: String,
        val startedAt: Long,
    )

    enum class Event {
        OnboardingCompleted,
        InvoiceCreatedLocal,
        QuoteCreatedLocal,
        PdfPreviewed,
        ShareClicked,
        WhatsappShareClicked,
        AiReplyGeneratedSuccess,
        AiCaptionGeneratedSuccess,
        UpgradeScreenOpened,
        PlanLimitReached,
    }

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            appContext = context.applicationContext
            database = TijarioDatabase.getInstance(appContext)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            initialized = true
        }
    }

    /** Invoked after authentication resolves so anonymous launches never create analytics rows. */
    fun onAuthenticated() {
        if (!initialized) return
        scope.launch {
            queueMutex.withLock {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@withLock
                recoverOpenSessions(userId)
                if (isForeground) beginSessionIfNeeded(userId)
                flushNow(userId)
            }
        }
    }

    fun track(event: Event) {
        if (!initialized) return
        scope.launch {
            queueMutex.withLock {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@withLock
                if (isForeground) beginSessionIfNeeded(userId)
                mutateDaily(userId) { daily ->
                    when (event) {
                        Event.OnboardingCompleted -> daily.copy(onboardingCompletedCount = daily.onboardingCompletedCount + 1)
                        Event.InvoiceCreatedLocal -> daily.copy(invoiceCreatedLocalCount = daily.invoiceCreatedLocalCount + 1)
                        Event.QuoteCreatedLocal -> daily.copy(quoteCreatedLocalCount = daily.quoteCreatedLocalCount + 1)
                        Event.PdfPreviewed -> daily.copy(pdfPreviewedCount = daily.pdfPreviewedCount + 1)
                        Event.ShareClicked -> daily.copy(shareClickedCount = daily.shareClickedCount + 1)
                        Event.WhatsappShareClicked -> daily.copy(whatsappShareClickedCount = daily.whatsappShareClickedCount + 1)
                        Event.AiReplyGeneratedSuccess -> daily.copy(aiReplySuccessCount = daily.aiReplySuccessCount + 1)
                        Event.AiCaptionGeneratedSuccess -> daily.copy(aiCaptionSuccessCount = daily.aiCaptionSuccessCount + 1)
                        Event.UpgradeScreenOpened -> daily.copy(upgradeScreenOpenedCount = daily.upgradeScreenOpenedCount + 1)
                        Event.PlanLimitReached -> daily.copy(planLimitReachedCount = daily.planLimitReachedCount + 1)
                    }
                }
            }
        }
    }

    fun recordClientError(errorCode: String, errorArea: String) {
        if (!initialized) return
        scope.launch {
            queueMutex.withLock {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@withLock
                val code = normalizeToken(errorCode, 80) ?: return@withLock
                val area = normalizeToken(errorArea, 80)
                val installationId = AppPreferences.getInstallationId(appContext)
                val day = utcDay()
                val fingerprint = sha256("$area:$code").take(32)
                database.withTransaction {
                    mutateDailyInTransaction(userId) { it.copy(clientErrorCount = it.clientErrorCount + 1) }
                    val existing = database.tijarioDao().getPendingAnalyticsError(userId, installationId, day, fingerprint)
                    database.tijarioDao().upsertPendingAnalyticsError(
                        AnalyticsPendingErrorEntity(
                            userId = userId,
                            installationId = installationId,
                            day = day,
                            errorFingerprint = fingerprint,
                            errorCode = code,
                            errorArea = area,
                            count = (existing?.count ?: 0) + 1,
                            nextRetryAt = 0,
                        ),
                    )
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!initialized) return
        isForeground = true
        scope.launch {
            queueMutex.withLock {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@withLock
                recoverOpenSessions(userId)
                beginSessionIfNeeded(userId)
                startActiveFlushLoop(userId)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (!initialized) return
        isForeground = false
        activeFlushJob?.cancel()
        activeFlushJob = null
        scope.launch {
            queueMutex.withLock {
                finishActiveSession("background")
                Supabase.client.auth.currentUserOrNull()?.id?.let { userId -> flushNow(userId) }
            }
        }
    }

    private suspend fun beginSessionIfNeeded(userId: String) {
        if (activeSession?.userId == userId) return
        val now = System.currentTimeMillis()
        val installationId = AppPreferences.getInstallationId(appContext)
        val session = ActiveSession(userId, installationId, UUID.randomUUID().toString(), now)
        database.withTransaction {
            database.tijarioDao().upsertPendingAnalyticsSession(
                AnalyticsPendingSessionEntity(
                    sessionId = session.sessionId,
                    userId = userId,
                    installationId = installationId,
                    platform = "android",
                    appVersion = BuildConfig.VERSION_NAME,
                    appBuild = BuildConfig.VERSION_CODE.toString(),
                    startedAt = now,
                ),
            )
            mutateDailyInTransaction(userId) { daily ->
                daily.copy(
                    appOpenCount = daily.appOpenCount + if (openedForUserId == userId) 0 else 1,
                    sessionCount = daily.sessionCount + 1,
                )
            }
        }
        openedForUserId = userId
        activeSession = session
    }

    private suspend fun finishActiveSession(reason: String) {
        val session = activeSession ?: return
        activeSession = null
        val endedAt = System.currentTimeMillis()
        val durationSeconds = durationSeconds(session.startedAt, endedAt)
        database.withTransaction {
            database.tijarioDao().upsertPendingAnalyticsSession(
                AnalyticsPendingSessionEntity(
                    sessionId = session.sessionId,
                    userId = session.userId,
                    installationId = session.installationId,
                    platform = "android",
                    appVersion = BuildConfig.VERSION_NAME,
                    appBuild = BuildConfig.VERSION_CODE.toString(),
                    startedAt = session.startedAt,
                    endedAt = endedAt,
                    durationSeconds = durationSeconds,
                    endedReason = reason,
                ),
            )
            mutateDailyInTransaction(session.userId) { it.copy(totalForegroundSeconds = it.totalForegroundSeconds + durationSeconds) }
        }
    }

    private suspend fun recoverOpenSessions(userId: String) {
        val now = System.currentTimeMillis()
        database.tijarioDao().getOpenAnalyticsSessions(userId).forEach { session ->
            val durationSeconds = durationSeconds(session.startedAt, now)
            database.tijarioDao().upsertPendingAnalyticsSession(
                session.copy(
                    endedAt = min(now, session.startedAt + MAX_SESSION_SECONDS * 1000L),
                    durationSeconds = durationSeconds,
                    endedReason = "recovered",
                ),
            )
            mutateDaily(userId) { it.copy(totalForegroundSeconds = it.totalForegroundSeconds + durationSeconds) }
        }
    }

    private fun startActiveFlushLoop(userId: String) {
        activeFlushJob?.cancel()
        activeFlushJob = scope.launch {
            delay(FOREGROUND_DELAY_MS)
            while (isForeground && Supabase.client.auth.currentUserOrNull()?.id == userId) {
                queueMutex.withLock {
                    mutateDaily(userId) { it.copy(heartbeatCount = it.heartbeatCount + 1) }
                    flushNow(userId)
                }
                delay(ACTIVE_FLUSH_INTERVAL_MS)
            }
        }
    }

    private suspend fun mutateDaily(userId: String, transform: (AnalyticsPendingDailyEntity) -> AnalyticsPendingDailyEntity) {
        database.withTransaction { mutateDailyInTransaction(userId, transform) }
    }

    private suspend fun mutateDailyInTransaction(
        userId: String,
        transform: (AnalyticsPendingDailyEntity) -> AnalyticsPendingDailyEntity,
    ) {
        val dao = database.tijarioDao()
        val installationId = AppPreferences.getInstallationId(appContext)
        val day = utcDay()
        val existing = dao.getPendingAnalyticsDaily(userId, installationId, day)
        val base = existing ?: AnalyticsPendingDailyEntity(
            userId = userId,
            installationId = installationId,
            day = day,
            batchId = UUID.randomUUID().toString(),
            platform = "android",
            appVersion = BuildConfig.VERSION_NAME,
            appBuild = BuildConfig.VERSION_CODE.toString(),
            countryCode = null,
            planCode = dao.getAccountEntitlement(userId)?.planCode,
            updatedAt = System.currentTimeMillis(),
        )
        dao.upsertPendingAnalyticsDaily(transform(base).copy(updatedAt = System.currentTimeMillis(), attempts = 0, nextRetryAt = 0))
    }

    private suspend fun flushNow(userId: String) {
        val dao = database.tijarioDao()
        val now = System.currentTimeMillis()
        val daily = dao.getFlushableAnalyticsDaily(userId, now) ?: run {
            val session = dao.getAnyFlushableAnalyticsSession(userId, now) ?: return
            val synthetic = AnalyticsPendingDailyEntity(
                userId = userId,
                installationId = session.installationId,
                day = utcDay(),
                batchId = UUID.randomUUID().toString(),
                platform = "android",
                appVersion = session.appVersion,
                appBuild = session.appBuild,
                countryCode = null,
                planCode = dao.getAccountEntitlement(userId)?.planCode,
                updatedAt = now,
            )
            dao.upsertPendingAnalyticsDaily(synthetic)
            synthetic
        }
        val sessions = dao.getFlushableAnalyticsSessions(userId, daily.installationId, now)
        val errors = dao.getFlushableAnalyticsErrors(userId, daily.installationId, daily.day, now)
        val payload = buildJsonObject {
            put("batch_id", daily.batchId)
            put("installation_id", daily.installationId)
            put("platform", daily.platform)
            put("app_version", daily.appVersion)
            put("app_build", daily.appBuild)
            daily.countryCode?.let { put("country_code", it) }
            daily.planCode?.let { put("plan_code", it) }
            put("day", daily.day)
            put("increments", buildJsonObject {
                put("app_open_count", daily.appOpenCount)
                put("session_count", daily.sessionCount)
                put("total_foreground_seconds", daily.totalForegroundSeconds)
                put("heartbeat_count", daily.heartbeatCount)
                put("onboarding_completed_count", daily.onboardingCompletedCount)
                put("invoice_created_local_count", daily.invoiceCreatedLocalCount)
                put("quote_created_local_count", daily.quoteCreatedLocalCount)
                put("pdf_previewed_count", daily.pdfPreviewedCount)
                put("share_clicked_count", daily.shareClickedCount)
                put("whatsapp_share_clicked_count", daily.whatsappShareClickedCount)
                put("ai_reply_success_count", daily.aiReplySuccessCount)
                put("ai_caption_success_count", daily.aiCaptionSuccessCount)
                put("upgrade_screen_opened_count", daily.upgradeScreenOpenedCount)
                put("plan_limit_reached_count", daily.planLimitReachedCount)
                put("client_error_count", daily.clientErrorCount)
            })
            sessions.firstOrNull()?.let { session ->
                put("session", buildJsonObject {
                    put("session_id", session.sessionId)
                    put("started_at", Instant.ofEpochMilli(session.startedAt).toString())
                    session.endedAt?.let { put("ended_at", Instant.ofEpochMilli(it).toString()) }
                    put("duration_seconds", session.durationSeconds)
                    session.endedReason?.let { put("ended_reason", it) }
                })
            }
            put("errors", buildJsonArray {
                errors.forEach { error ->
                    add(buildJsonObject {
                        put("error_fingerprint", error.errorFingerprint)
                        put("error_code", error.errorCode)
                        error.errorArea?.let { put("error_area", it) }
                        put("count", error.count)
                    })
                }
            })
        }

        try {
            Supabase.client.postgrest.rpc(
                "record_mobile_analytics_batch",
                buildJsonObject { put("p_payload", payload) },
            )
            database.withTransaction {
                dao.deletePendingAnalyticsDaily(userId, daily.installationId, daily.day, daily.batchId)
                if (sessions.isNotEmpty()) dao.deletePendingAnalyticsSessions(sessions.map { it.sessionId })
                if (errors.isNotEmpty()) dao.deletePendingAnalyticsErrors(userId, daily.installationId, daily.day, errors.map { it.errorFingerprint })
                pruneExpiredQueue(now)
            }
        } catch (_: Throwable) {
            // Analytics is strictly best-effort. Keep the same batch id and retry later.
            val retryAt = now + retryDelayMs(daily.attempts + 1)
            dao.upsertPendingAnalyticsDaily(daily.copy(attempts = daily.attempts + 1, nextRetryAt = retryAt))
        }
    }

    private suspend fun pruneExpiredQueue(now: Long) {
        val dao = database.tijarioDao()
        dao.deleteExpiredPendingAnalyticsDaily(now - MAX_QUEUE_AGE_MS)
        dao.deleteExpiredPendingAnalyticsSessions(now - MAX_QUEUE_AGE_MS)
        dao.deleteExpiredPendingAnalyticsErrors(LocalDate.ofInstant(Instant.ofEpochMilli(now - MAX_QUEUE_AGE_MS), ZoneOffset.UTC).toString())
    }

    private fun durationSeconds(startedAt: Long, endedAt: Long): Int =
        min(MAX_SESSION_SECONDS.toLong(), max(0L, (endedAt - startedAt) / 1000L)).toInt()

    private fun retryDelayMs(attempt: Int): Long = min(5 * 60 * 1000L, (1L shl min(attempt, 5)) * 30_000L)

    private fun utcDay(): String = LocalDate.now(ZoneOffset.UTC).toString()

    private fun normalizeToken(value: String, maxLength: Int): String? =
        value.lowercase().replace(Regex("[^a-z0-9_-]"), "").take(maxLength).takeIf { it.length >= 2 }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
