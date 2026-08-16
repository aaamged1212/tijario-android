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
    val planCode: String = "unknown",
    val automaticBackupAllowed: Boolean = normalize(maximumFrequency) != "manual",
    val driveBackupAllowed: Boolean = planCode.lowercase() in PAID_BACKUP_PLANS,
) {
    fun allows(frequency: String): Boolean {
        val normalized = normalize(frequency)
        if (normalized == "manual") return true
        return automaticBackupAllowed && rank(normalized) <= rank(maximumFrequency)
    }

    fun apply(settings: BackupSettingsEntity): BackupSettingsEntity = settings.copy(
        frequency = if (allows(settings.frequency)) normalize(settings.frequency) else maximumFrequency,
        driveEnabled = settings.driveEnabled && driveBackupAllowed,
        retentionDaily = retentionDaily.coerceAtLeast(0),
        retentionWeekly = retentionWeekly.coerceAtLeast(0),
        retentionMonthly = retentionMonthly.coerceAtLeast(0),
    )

    companion object {
        val ManualOnly = BackupPlanPolicy(
            maximumFrequency = "manual",
            retentionDaily = 0,
            retentionWeekly = 0,
            retentionMonthly = 0,
            planCode = "free",
            automaticBackupAllowed = false,
            driveBackupAllowed = false,
        )

        fun from(entitlement: AccountEntitlementEntity?, nowMillis: Long = System.currentTimeMillis()): BackupPlanPolicy {
            if (entitlement?.expiresAt?.let { it <= nowMillis } != false) return ManualOnly
            val payload = entitlement.signedPayload ?: return ManualOnly
            val signed = runCatching { json.decodeFromString<SignedEntitlementPayload>(payload) }.getOrNull()
                ?: return ManualOnly
            val planCode = signed.planCode.lowercase()
            if (planCode !in PAID_BACKUP_PLANS) {
                return ManualOnly.copy(planCode = planCode)
            }
            val maximumFrequency = normalize(signed.backupFrequency)
            return BackupPlanPolicy(
                maximumFrequency = maximumFrequency,
                retentionDaily = signed.backupRetentionDaily,
                retentionWeekly = signed.backupRetentionWeekly,
                retentionMonthly = signed.backupRetentionMonthly,
                planCode = planCode,
                automaticBackupAllowed = maximumFrequency != "manual",
                driveBackupAllowed = true,
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
        private val PAID_BACKUP_PLANS = setOf("starter", "pro", "business")
    }
}
