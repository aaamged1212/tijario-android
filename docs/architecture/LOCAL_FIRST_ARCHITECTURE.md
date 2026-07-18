# Local-First Architecture

Status: planning only. No production behavior is implemented by this document.

## Decision

Tijario Android must become local-first for operational business data. Room is the primary source of truth for:

- business settings
- customers
- products and services
- invoices, quotations, drafts, and document items
- taxes
- payment methods
- signatures
- terms and conditions
- images and local assets
- document metadata
- immutable document creation events

Normal document work must not require internet. Users must be able to create, view, edit, delete, export, and restore local documents while offline.

Supabase remains the control plane for:

- authentication and account profiles
- subscriptions and billing verification
- signed plan entitlements
- immutable document creation usage events
- device registration and offline quota leases
- AI usage
- announcements
- encrypted backup-key envelopes

Google Drive V1 is backup and restore only. It is not live multi-device synchronization. V1 supports one primary Android device per Tijario account.

Legacy cloud business-data tables must not be deleted during the initial transition.

## Current Behavior

The Android app already has Room caches and local entities for business settings, customers, products, documents, document items, taxes, payment methods, signatures, terms, local metadata, offline leases, and local usage ledger entries.

The current UI and repository still contain online-first write paths for major operations. That is acceptable for the current remediation branch but not the approved future architecture.

## Target Data Flow

1. UI writes business data to Room first.
2. Room updates the visible app state immediately.
3. A local operation record is created when synchronization or usage acknowledgement is needed.
4. WorkManager sends eligible control-plane events when connectivity exists.
5. Server acknowledgement updates local event status from `PENDING` to `ACKNOWLEDGED`.
6. Local documents remain usable if the server, Google Drive, or internet is unavailable.

## Offline Rules

- Creating a customer, product, invoice, quotation, draft, local tax, payment method, signature, term, or business setting must work offline.
- Editing existing local data must work offline.
- Deleting local documents must work offline and must leave tombstone/history needed for backup and recovery.
- PDF export must work offline from structured local data.
- AI, billing verification, new entitlement refresh, announcement refresh, and Google Drive upload require internet.

## Account and Device Rules

- One account may have one primary Android device in V1.
- Normal logout must not silently delete operational Room data.
- Local data removal must be a separate explicit action with a destructive warning.
- A backup is account-bound. Restoring a backup from another account is blocked by default.
- Switching accounts must never copy or mix one user data set into another.

## Threat Model

- Local row counts can be tampered with and must not be authoritative for plan usage.
- Device clocks can drift; signed entitlement and lease data need server-issued timestamps and expiry.
- Backups may be copied; they must be encrypted and account-bound.
- A failed restore must not replace the active database.
- A compromised or stale Google Drive connection must not affect local access.

## Proposed Room Additions

Future Room migrations should add or formalize:

- `document_creation_events`
- `account_entitlements`
- `backup_settings`
- `backup_records`
- `device_binding`
- `customer_snapshots`
- document asset/PDF references if current metadata is not sufficient

These additions belong in later implementation branches, not this remediation branch.

## Proposed Supabase Control-Plane Additions

Documentation-only future contracts:

- `document_creation_events`
- `account_usage_totals`
- `device_registrations`
- `offline_quota_leases_v2`
- `signed_entitlements`
- `backup_key_envelopes`

RPC/API contracts should support idempotent event acknowledgement, entitlement refresh, lease renewal, and backup-key envelope management.

