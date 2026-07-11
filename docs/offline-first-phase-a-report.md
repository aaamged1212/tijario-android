# Phase A Audit Report - Offline-First Sync Schema & Contracts

## 1. Verified Room Database State (Version 6)
The following tables are currently active in `TijarioDatabase`:
- `business_settings_cache`
- `customers_cache`
- `products_cache`
- `documents_cache`
- `local_taxes`
- `local_payment_methods`
- `local_signatures`
- `local_terms`
- `local_document_metadata`

---

## 2. Remote / Domain / Room Mapping Table

| Room Entity (V7 Cache) | Domain Model | Supabase / Remote DTO | Nullability & Defaults |
| :--- | :--- | :--- | :--- |
| `DocumentEntity` | `CompleteDocument` | `CreateDocumentRequest` | `customerId` is Non-Null, `paymentStatus` is Nullable (quote), `amountPaid` is Nullable. |
| `DocumentItemEntity` | `DocumentItem` | `DocumentItemInput` | `productId` is Nullable. |
| `CustomerEntity` | `Customer` | `Customer` | `city` and `notes` are Nullable. |
| `ProductEntity` | `Product` | `Product` | `description` and `stockQuantity` are Nullable. |
| `BusinessSettingsEntity` | `BusinessSettings` | `BusinessSettings` | `city`, `logoUrl`, `instagramUrl`, `invoiceNote`, `termsText` are Nullable. |

---

## 3. Money Migration & Representation
- **Precision**: Money values are represented as `BigDecimal` on domain models and Room entities.
- **Conversion Strategy**: During Room `MIGRATION_6_7`, legacy `Double` columns are migrated programmatically:
  - Legacies values are read via Cursor as `Double`.
  - `null` values are preserved as `null`.
  - Non-null values are parsed using `BigDecimal.valueOf(legacyDouble).toPlainString()`.
  - Values are inserted into new tables using standard SQL parameter binding.
  - No default rounding scaling is applied during migration to preserve the exact value mapped by the database.
  - Precision lost in original `Double` columns (prior to migration) cannot be retroactively recovered.
- **Read Path**: The Version 7 mapper reads values via `BigDecimal(storedString)` directly, without automatic scale adjustment.

---

## 4. Room Relational Integrity
- **Composite Foreign Key**: Establishes a relation:
  `documents_cache(user_id, id) -> document_items_cache(user_id, document_id)`
  with a unique parent index on `documents_cache(user_id, id)`.
- **Ledger Constraints**: `local_usage_ledger` enforces composite unique constraints:
  - `unique(user_id, document_id)`
  - `unique(user_id, operation_id)`

---

## 5. Conflict Resolution Rules
If a conflict occurs, a new local record is generated under these rules:
- Fresh local UUID generated.
- Assigned state: `Draft`.
- Reference: `conflict_source_document_id` set to original document's ID.
- Assigned a temporary unique document number (or nullable) to avoid database unique index collisions.
- Display label is restricted to the UI: `نسخة تعارض — <رقم المستند الأصل>`.
- The document is not counted against plan limits or synchronized as a formal document until approved by the user.

---

## 6. Backend REST API Contracts

### Push Endpoint (`POST /api/mobile/sync/push`)
Push responses return results per operation containing status values in a separate list.
Available status values:
- `APPLIED`
- `ALREADY_PROCESSED`
- `CONFLICT`
- `REJECTED`
- `RETRYABLE_FAILURE`
- `BLOCKED_BY_PLAN`

Response payload example:
```json
{
  "operation_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "status": "APPLIED",
  "server_revision": "rev-12345678",
  "normalized_server_record": {
    "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "total": "120.50"
  },
  "stable_error_code": null,
  "authoritative_usage_snapshot": {
    "documents_used": 3,
    "ai_used": 1
  }
}
```

### Pull Endpoint (`GET /api/mobile/sync/pull`)
Typed query return structures. When pulling, the client sends its current cursor in the query parameters.
Response payload example:
```json
{
  "next_cursor": "eyJzZXJ2ZXJfdGltZXN0YW1wIjoxNzgxOTM5NDI3Mjc4fQ==",
  "has_more": false,
  "changes": {
    "documents": {
      "upserted": [
        {
          "id": "c3b7a5a8-4c91-4d37-8f5b-59d48b789123",
          "server_revision": "rev-23456"
        }
      ],
      "deleted": ["uuid-1", "uuid-2"]
    },
    "customers": {
      "upserted": [],
      "deleted": []
    },
    "products": {
      "upserted": [],
      "deleted": []
    }
  }
}
```

### Lease Endpoint (`POST /api/mobile/account/offline-lease`)
Handles quota allocations:
- Fixed unique lease row tracking `(user_id, device_id, period_month)`.
- Quotas are managed and calculated server-side; client has no hardcoded limits.
- Lease expiry (72 hours default) is checked and handled at Month rollover or Plan changes.
