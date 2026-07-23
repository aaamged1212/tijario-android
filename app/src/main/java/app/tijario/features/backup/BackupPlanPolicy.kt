package app.tijario.features.backup

import app.tijario.data.local.AccountEntitlementEntity
import app.tijario.data.local.BackupSettingsEntity
import app.tijario.domain.SignedEntitlementPayload
import kotlinx.serialization.json.Json

/** Applies only the already verified, locally persisted backup policy claims. */
data class BackupPlanPolicy(
    val maximumFrequency: String,
    val retentionDaily: Int,
    val retentionWeekly: Int,
    val retentionMonthly: Int,
) {
    fun allows(frequency: String): Boolean = rank(normalize(frequency)) <= rank(maximumFrequency)

    fun apply(settings: BackupSettingsEntity): BackupSettingsEntity = settings.copy(
        frequency = if (allows(settings.frequency)) normalize(settings.frequency) else maximumFrequency,
        retentionDaily = retentionDaily.coerceAtLeast(0),
        retentionWeekly = retentionWeekly.coerceAtLeast(0),
        retentionMonthly = retentionMonthly.coerceAtLeast(0),
    )

    companion object {
        val ManualOnly = BackupPlanPolicy("manual", 0, 0, 0)

        fun from(entitlement: AccountEntitlementEntity?, nowMillis: Long = System.currentTimeMillis()): BackupPlanPolicy {
            if (entitlement?.expiresAt?.let { it <= nowMillis } != false) return ManualOnly
            val payload = entitlement.signedPayload ?: return ManualOnly
            val signed = runCatching { json.decodeFromString<SignedEntitlementPayload>(payload) }.getOrNull()
                ?: return ManualOnly
            return BackupPlanPolicy(
                maximumFrequency = normalize(signed.backupFrequency),
                retentionDaily = signed.backupRetentionDaily,
                retentionWeekly = signed.backupRetentionWeekly,
                retentionMonthly = signed.backupRetentionMonthly,
            )
        }

        fun normalize(value: String): String = when (value.lowercase()) {
            "daily" -> "daily"
            "weekly" -> "weekly"
            else -> "manual"
        }

        private fun rank(value: String): Int = when (normalize(value)) {
            "daily" -> 2
            "weekly" -> 1
            else -> 0
        }

        private val json = Json { ignoreUnknownKeys = false }
    }
}
