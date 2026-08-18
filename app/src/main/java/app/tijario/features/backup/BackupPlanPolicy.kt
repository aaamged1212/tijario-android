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

        fun from(
            entitlement: AccountEntitlementEntity?,
            activePlanCode: String? = null,
            nowMillis: Long = System.currentTimeMillis()
        ): BackupPlanPolicy {
            val resolvedPlanCode = activePlanCode?.lowercase() ?: "free"
            val isPaidPlan = resolvedPlanCode != "free" && resolvedPlanCode != "unknown"
            
            val basePolicy = if (entitlement?.expiresAt?.let { it <= nowMillis } != false) {
                if (isPaidPlan) null else return ManualOnly
            } else {
                val payload = entitlement.signedPayload ?: if (isPaidPlan) null else return ManualOnly
                val signed = payload?.let { runCatching { json.decodeFromString<SignedEntitlementPayload>(it) }.getOrNull() }
                if (signed == null && !isPaidPlan) return ManualOnly
                signed?.let {
                    val planCode = it.planCode.lowercase()
                    if (planCode !in PAID_BACKUP_PLANS) {
                        ManualOnly.copy(planCode = planCode)
                    } else {
                        val maximumFrequency = normalize(it.backupFrequency)
                        BackupPlanPolicy(
                            maximumFrequency = maximumFrequency,
                            retentionDaily = it.backupRetentionDaily,
                            retentionWeekly = it.backupRetentionWeekly,
                            retentionMonthly = it.backupRetentionMonthly,
                            planCode = planCode,
                            automaticBackupAllowed = maximumFrequency != "manual",
                            driveBackupAllowed = true,
                        )
                    }
                }
            }

            if (basePolicy != null) return basePolicy

            return if (resolvedPlanCode == "pro" || resolvedPlanCode == "business") {
                BackupPlanPolicy(
                    maximumFrequency = "daily",
                    retentionDaily = 7,
                    retentionWeekly = 4,
                    retentionMonthly = 3,
                    planCode = resolvedPlanCode,
                    automaticBackupAllowed = true,
                    driveBackupAllowed = true,
                )
            } else if (isPaidPlan) {
                BackupPlanPolicy(
                    maximumFrequency = "weekly",
                    retentionDaily = 0,
                    retentionWeekly = 4,
                    retentionMonthly = 0,
                    planCode = resolvedPlanCode,
                    automaticBackupAllowed = true,
                    driveBackupAllowed = true,
                )
            } else {
                ManualOnly
            }
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
