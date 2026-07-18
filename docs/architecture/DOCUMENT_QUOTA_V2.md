# Document Quota V2

Status: partially implemented locally. Android Room immutable creation events and restore-safe merge behavior exist; the local backend control-plane migration is authored but unapplied. Signed entitlements, lease V2 client integration, and production reconciliation remain incomplete.

## Product Rules

- Every logical creation of a new invoice or quotation consumes exactly one document credit.
- Deleting a document never refunds a credit.
- Deleting a document never reduces created-document usage.
- Restoring the same deleted document never consumes another credit.
- Editing an existing document never consumes another credit.
- Retrying the same `operation_id` never consumes another credit.
- Reinstalling the app never resets account usage.
- Restoring a backup never resets or decreases account usage.
- Duplicating a document creates a new document and consumes a new credit.
- Creating an invoice from a quotation as a separate record consumes a new credit.
- Free-plan document quota is lifetime-scoped.
- Paid-plan document quota is billing-cycle scoped.

Local document table row counts must never be authoritative for usage.

Restore merges immutable `document_creation_events` using the existing user/document and user/operation uniqueness. It never replaces or deletes newer events. Archived device registrations and offline leases never reactivate authority; the current control-plane-approved device and lease state is preserved.

## Immutable Events

Future Room and Supabase designs should use immutable `document_creation_events`.

Required uniqueness:

- unique `(user_id, document_id)`
- unique `(user_id, operation_id)`

Required state transition:

- `PENDING -> ACKNOWLEDGED`

Successful synchronization must never delete the creation event.

## Proposed Room Entity

Documentation-only shape:

```text
document_creation_events
- id
- user_id
- document_id
- operation_id
- document_type
- source_action
- quota_scope
- billing_period_key
- plan_code
- device_id
- lease_id
- status
- created_at
- acknowledged_at
- last_error_code
```

Allowed `source_action` values:

- `CREATE_INVOICE`
- `CREATE_QUOTATION`
- `DUPLICATE_DOCUMENT`
- `CONVERT_QUOTATION_TO_INVOICE`
- `RESTORE_EXISTING_DOCUMENT`

Only the first four consume a new credit. Restoring the same document does not.

## Proposed Supabase Tables

Documentation-only future tables:

```text
document_creation_events
account_usage_totals
device_registrations
offline_quota_leases_v2
signed_entitlements
```

`account_usage_totals` stores durable aggregate usage. It is derived from immutable events and never from current document row count.

## Proposed RPC/API Contracts

Documentation-only contracts:

- `register_device(device_id, app_version)`
- `issue_signed_entitlement(device_id)`
- `issue_offline_quota_lease(device_id, period_key)`
- `acknowledge_document_creation_event(event_payload)`
- `reconcile_document_creation_events(events[])`

Each write must be idempotent by `operation_id`.

## Current Gap

The existing backend is monthly-usage oriented and includes `usage_counters`, `offline_quota_leases`, and document write RPCs. The future V2 must keep legacy data during migration and add immutable event accounting before Android becomes fully local-first.
