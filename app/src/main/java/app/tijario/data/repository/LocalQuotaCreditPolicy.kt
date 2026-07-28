package app.tijario.data.repository

/** Pure lease-capacity rule used while the repository serializes Room reservations. */
internal fun hasLeaseCredit(
    allowedCount: Int,
    consumedCount: Int,
    pendingReservations: Int,
): Boolean = allowedCount >= 0 && consumedCount >= 0 && pendingReservations >= 0 &&
    consumedCount + pendingReservations < allowedCount
