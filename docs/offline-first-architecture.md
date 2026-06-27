# Offline-First Architecture (Android)

This document provides a high-level overview of the offline-first implementation of the Tijario Android application.

## Key Design Principles

1. **Cache-First Observables**: The UI and ViewModel layers subscribe to reactive data streams (`Flow`s) that fetch directly from the local Room database cache.
2. **Local Write Transactions**: Every CRUD operation is executed inside local Room database transactions, instantly updating the UI before synchronization occurs.
3. **Mappers & DTOs**: Entity schemas are isolated:
   - **Local Database Entities**: (`CustomerEntity`, `ProductEntity`, `DocumentEntity`, `DocumentItemEntity`)
   - **Domain/UI Models**: (`Customer`, `Product`, `DocumentSummary`)
   - **Remote Synchronization DTOs**: (`SyncPushRequest`, `SyncPullResponse`)
4. **FIFO Outbox & Compaction**: Synchronous outbox compaction rules prevent redundant sync payloads (e.g. `CREATE` + `DELETE` cancels both).
5. **Worker Integration**: Background replication is handled asynchronously by the `SyncWorker` task configured inside Google's `WorkManager` framework.
