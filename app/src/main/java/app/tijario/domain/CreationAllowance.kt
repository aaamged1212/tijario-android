package app.tijario.domain

/** Targets that create a new billable or plan-limited business record. */
enum class CreationTarget {
    Customer,
    Product,
    Invoice,
    Quote,
}

/**
 * Shared result used by navigation preflight and save-time error presentation.
 * Limits remain server/entitlement owned; only the used count can be local-first.
 */
sealed interface CreationAllowance {
    data object Allowed : CreationAllowance

    data class LimitReached(
        val target: CreationTarget,
        val used: Int,
        val limit: Int,
    ) : CreationAllowance

    data object PlanUnavailable : CreationAllowance
    data object Retryable : CreationAllowance
}

fun CreationTarget.limitErrorCode(): String = when (this) {
    CreationTarget.Customer -> "CUSTOMER_LIMIT_REACHED"
    CreationTarget.Product -> "PRODUCT_LIMIT_REACHED"
    CreationTarget.Invoice,
    CreationTarget.Quote,
    -> "DOCUMENT_LIMIT_REACHED"
}

fun creationTargetForErrorCode(code: String?): CreationTarget? = when (code?.uppercase()) {
    "CUSTOMER_LIMIT_REACHED" -> CreationTarget.Customer
    "PRODUCT_LIMIT_REACHED" -> CreationTarget.Product
    "DOCUMENT_LIMIT_REACHED", "QUOTA_LIMIT_EXCEEDED", "QUOTA_LIMIT_REACHED" -> CreationTarget.Invoice
    else -> null
}
