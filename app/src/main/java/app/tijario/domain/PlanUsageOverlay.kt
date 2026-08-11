package app.tijario.domain

import app.tijario.data.model.UserPlanUsage

/**
 * Limits are entitlement-owned. LocalDrive may overlay only active local customer/product
 * counts and pending immutable document-creation events.
 */
internal fun effectivePlanUsage(
    usage: UserPlanUsage,
    isLocalDrive: Boolean,
    activeCustomers: Int = usage.customersUsed,
    activeProducts: Int = usage.productsUsed,
    pendingDocumentEvents: Int = 0,
): UserPlanUsage = if (isLocalDrive) {
    usage.copy(
        customersUsed = activeCustomers,
        productsUsed = activeProducts,
        documentsUsed = usage.documentsUsed + pendingDocumentEvents,
    )
} else if (pendingDocumentEvents > 0) {
    usage.copy(documentsUsed = usage.documentsUsed + pendingDocumentEvents)
} else {
    usage
}
