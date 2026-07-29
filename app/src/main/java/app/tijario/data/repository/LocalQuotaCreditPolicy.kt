package app.tijario.data.repository

/** Pure lease-capacity rule used while the repository serializes Room reservations. */
internal fun hasLeaseCredit(
    allowedCount: Int,
    consumedCount: Int,
    pendingReservations: Int,
): Boolean = allowedCount >= 0 && consumedCount >= 0 && pendingReservations >= 0 &&
    consumedCount + pendingReservations < allowedCount

internal fun leaseMatchesPeriod(leasePeriod: String, expectedPeriod: String): Boolean =
    leasePeriod == expectedPeriod

internal fun assignableLegacyEventCount(
    allowedCount: Int,
    consumedCount: Int,
    pendingReservations: Int,
    legacyEventCount: Int,
): Int = (allowedCount - consumedCount - pendingReservations).coerceIn(0, legacyEventCount)

internal fun isTerminalDocumentCreationEventFailure(errorCode: String?): Boolean = errorCode in setOf(
    "DOCUMENT_LIMIT_REACHED",
    "INSTALLATION_REVOKED",
    "ENTITLEMENT_VERSION_MISMATCH",
)

internal fun isRetryableLeaseReconciliationFailure(errorCode: String?): Boolean = errorCode in setOf(
    "OFFLINE_LEASE_INVALID",
    "OFFLINE_LEASE_EXPIRED",
    "OFFLINE_LEASE_EXHAUSTED",
)

internal fun isPermanentDocumentCreationEventFailure(errorCode: String?): Boolean = errorCode in setOf(
    "INVALID_EVENT_PAYLOAD",
    "INVALID_PAYLOAD_HASH",
    "OPERATION_PAYLOAD_MISMATCH",
)
