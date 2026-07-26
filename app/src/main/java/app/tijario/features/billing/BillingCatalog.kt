package app.tijario.features.billing

import java.security.MessageDigest

object BillingCatalog {
    const val PLAN_STARTER = "starter"
    const val PLAN_PRO = "pro"

    const val PRODUCT_STARTER = "tijario_starter"
    const val PRODUCT_PRO = "tijario_pro"
    private const val LEGACY_PLAN_BUSINESS = "business"
    private const val LEGACY_PRODUCT_BUSINESS = "tijario_business"

    const val INTERVAL_MONTHLY = "monthly"
    const val INTERVAL_YEARLY = "yearly"

    val paidProductIds = listOf(PRODUCT_STARTER, PRODUCT_PRO)
    val supportedIntervals = listOf(INTERVAL_MONTHLY, INTERVAL_YEARLY)

    fun productIdForPlan(planCode: String): String? =
        when (planCode.lowercase()) {
            PLAN_STARTER -> PRODUCT_STARTER
            PLAN_PRO -> PRODUCT_PRO
            else -> null
        }

    fun planCodeForProduct(productId: String): String? =
        when (productId) {
            PRODUCT_STARTER -> PLAN_STARTER
            PRODUCT_PRO -> PLAN_PRO
            LEGACY_PRODUCT_BUSINESS -> LEGACY_PLAN_BUSINESS
            else -> null
        }

    fun publicPlanCode(planCode: String): String? =
        when (planCode.lowercase()) {
            "free", PLAN_STARTER, PLAN_PRO -> planCode.lowercase()
            LEGACY_PLAN_BUSINESS -> PLAN_PRO
            else -> null
        }

    fun offerKey(planCode: String, interval: String): String =
        "${planCode.lowercase()}:${interval.lowercase()}"

    fun obfuscatedAccountId(userId: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(userId.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
