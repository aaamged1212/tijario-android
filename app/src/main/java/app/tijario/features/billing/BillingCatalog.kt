package app.tijario.features.billing

import java.security.MessageDigest

object BillingCatalog {
    const val PLAN_STARTER = "starter"
    const val PLAN_PRO = "pro"
    const val PLAN_BUSINESS = "business"

    const val PRODUCT_STARTER = "tijario_starter"
    const val PRODUCT_PRO = "tijario_pro"
    const val PRODUCT_BUSINESS = "tijario_business"

    const val INTERVAL_MONTHLY = "monthly"
    const val INTERVAL_YEARLY = "yearly"

    val paidProductIds = listOf(PRODUCT_STARTER, PRODUCT_PRO, PRODUCT_BUSINESS)
    val supportedIntervals = listOf(INTERVAL_MONTHLY, INTERVAL_YEARLY)

    fun productIdForPlan(planCode: String): String? =
        when (planCode.lowercase()) {
            PLAN_STARTER -> PRODUCT_STARTER
            PLAN_PRO -> PRODUCT_PRO
            PLAN_BUSINESS -> PRODUCT_BUSINESS
            else -> null
        }

    fun planCodeForProduct(productId: String): String? =
        when (productId) {
            PRODUCT_STARTER -> PLAN_STARTER
            PRODUCT_PRO -> PLAN_PRO
            PRODUCT_BUSINESS -> PLAN_BUSINESS
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
