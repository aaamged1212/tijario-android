from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
path = ROOT / "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt"
source = path.read_text(encoding="utf-8")


def replace_once(old: str, new: str, label: str) -> None:
    global source
    if new in source:
        return
    count = source.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one old block, found {count}")
    source = source.replace(old, new, 1)


replace_once(
    '''            val requestedCustomerId = request.customer.id?.takeIf { it.isNotBlank() }
            val documentCustomerId = if (requestedCustomerId != null) {
                val existingCustomer = dao.getCustomer(userId, requestedCustomerId)
                if (existingCustomer != null) {
                    dao.upsertCustomer(
                        existingCustomer.copy(
                            name = request.customer.name,
                            whatsappNumber = request.customer.whatsappNumber,
                            city = request.customer.city,
                        ),
                    )
                }
                requestedCustomerId
            } else {
                val newCustId = java.util.UUID.randomUUID().toString()
                val customerEntity = app.tijario.data.local.CustomerEntity(
                    id = newCustId,
                    userId = userId,
                    name = request.customer.name,
                    whatsappNumber = request.customer.whatsappNumber,
                    city = request.customer.city,
                    notes = null,
                    syncedAt = 0L,
                    syncStatus = "LOCAL_ONLY",
                    localRevision = 1,
                    serverRevision = null,
                    serverUpdatedAt = null,
                    lastSyncedAt = null,
                    syncErrorCode = null,
                    isDeleted = false
                )
                dao.upsertCustomer(customerEntity)
                enqueueOutbox(userId, "customer", newCustId, "CREATE")
                newCustId
            }''',
    '''            val requestedCustomerId = request.customer.id?.takeIf { it.isNotBlank() }
            val existingCustomer = requestedCustomerId?.let { dao.getCustomer(userId, it) }
            val documentCustomerId = requestedCustomerId ?: java.util.UUID.randomUUID().toString()
            val customerChanged = existingCustomer != null && (
                existingCustomer.name != request.customer.name ||
                    existingCustomer.whatsappNumber != request.customer.whatsappNumber ||
                    existingCustomer.city != request.customer.city
                )
            val customerEntityToUpsert = existingCustomer?.copy(
                name = request.customer.name,
                whatsappNumber = request.customer.whatsappNumber,
                city = request.customer.city,
                localRevision = if (customerChanged) existingCustomer.localRevision + 1 else existingCustomer.localRevision,
                syncStatus = when {
                    !customerChanged -> existingCustomer.syncStatus
                    existingCustomer.syncStatus == "LOCAL_ONLY" -> "LOCAL_ONLY"
                    else -> "PENDING_SYNC"
                },
            ) ?: app.tijario.data.local.CustomerEntity(
                id = documentCustomerId,
                userId = userId,
                name = request.customer.name,
                whatsappNumber = request.customer.whatsappNumber,
                city = request.customer.city,
                notes = null,
                syncedAt = 0L,
                syncStatus = if (requestedCustomerId == null) "LOCAL_ONLY" else "SYNCED",
                localRevision = 1,
                serverRevision = null,
                serverUpdatedAt = null,
                lastSyncedAt = null,
                syncErrorCode = null,
                isDeleted = false,
            )
            val customerOutboxOperation = when {
                requestedCustomerId == null -> "CREATE"
                customerChanged && existingCustomer?.syncStatus == "LOCAL_ONLY" -> "CREATE"
                customerChanged -> "UPDATE"
                else -> null
            }''',
    "atomic document customer preparation",
)

replace_once(
    '''                database.withTransaction {
                    dao.upsertDocument(docEntity)
                    dao.insertDocumentItems(itemsEntities)
                    reserveDocumentQuotaLedger(userId, docId)
                    enqueueOutbox(userId, "document", docId, "CREATE")

                }''',
    '''                database.withTransaction {
                    dao.upsertCustomer(customerEntityToUpsert)
                    customerOutboxOperation?.let { operation ->
                        enqueueOutbox(
                            userId = userId,
                            entityType = "customer",
                            entityId = documentCustomerId,
                            operation = operation,
                            baseServerRevision = existingCustomer?.serverRevision,
                        )
                    }
                    dao.upsertDocument(docEntity)
                    dao.insertDocumentItems(itemsEntities)
                    reserveDocumentQuotaLedger(userId, docId)
                    enqueueOutbox(userId, "document", docId, "CREATE")
                }''',
    "atomic customer and document transaction",
)

old_success = '''                                            syncStatus = "SYNCED",
                                            serverRevision = res.server_revision,
                                            syncedAt = System.currentTimeMillis()'''
new_success = '''                                            syncStatus = "SYNCED",
                                            serverRevision = res.server_revision,
                                            serverUpdatedAt = parseServerInstantOrNull(res.server_revision.orEmpty())?.toEpochMilli()
                                                ?: System.currentTimeMillis(),
                                            lastSyncedAt = System.currentTimeMillis(),
                                            syncErrorCode = null,
                                            syncedAt = System.currentTimeMillis()'''
if new_success not in source:
    count = source.count(old_success)
    if count != 4:
        raise RuntimeError(f"push-success metadata: expected four blocks, found {count}")
    source = source.replace(old_success, new_success)

replace_once(
    '''                }
            }
            }

            val deviceId = AppPreferences.getInstallationId(context)''',
    '''                }
            } else {
                when {
                    pullResponse.status.value == 401 || pullResponse.status.value == 403 ->
                        error("SESSION_EXPIRED")
                    pullResponse.status.value == 429 || pullResponse.status.value >= 500 ->
                        error("RETRYABLE_NETWORK_ERROR")
                    else -> error("PULL_SYNC_FAILED_${pullResponse.status.value}")
                }
            }
            }

            val deviceId = AppPreferences.getInstallationId(context)''',
    "pull response failure handling",
)

replace_once(
    '''                    ))
                }
            }
            if (
                dao.getPendingOutbox(userId).any {''',
    '''                    ))
                } else {
                    when {
                        leaseResponse.status.value == 401 || leaseResponse.status.value == 403 ->
                            error("SESSION_EXPIRED")
                        leaseResponse.status.value == 429 || leaseResponse.status.value >= 500 ->
                            error("RETRYABLE_NETWORK_ERROR")
                        else -> error("OFFLINE_LEASE_REQUEST_FAILED_${leaseResponse.status.value}")
                    }
                }
            }
            if (
                dao.getPendingOutbox(userId).any {''',
    "lease response failure handling",
)

path.write_text(source, encoding="utf-8")
print("final sync atomicity remediation applied")
