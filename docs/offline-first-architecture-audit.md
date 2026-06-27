# Offline-First Architecture & Sync Audit

## 1. Current Local Cache State (Room)
Currently, Room caches:
- `business_settings_cache`
- `customers_cache`
- `products_cache`
- `documents_cache` (Basic metadata/summary only)
- Custom local metadata (`local_taxes`, `local_payment_methods`, `local_signatures`, `local_terms`, `local_document_metadata`)

## 2. Remote Database Only (Supabase)
The following tables and data live solely in Supabase:
- `document_items`: No local representation exists, preventing offline invoice/quote item viewing or editing.
- Detailed document fields (`subtotal`, `discount`, `extra_fees`, `notes`, `terms_text`, `pdf_url`, `shared_at`).
- `plans`, `user_plan` (Plan/Limits info).
- `usage_counters` (Active server-side consumption).

## 3. Gaps Preventing Offline Flow
- **No Document Items Local Storage**: Items are not saved locally.
- **Write Actions Are Remote-First**: Repositories directly call Supabase or the backend HTTPS API.
- **No Sync Outbox Queue**: Offline edits are lost on app restart or not captured when connectivity is lost.
- **No Offline Quota Lease**: The app cannot verify if the user is within limits before generating invoice PDFs offline.
- **Destructive Database Fallback**: Code currently calls `.fallbackToDestructiveMigration()`, which violates data integrity.

## 4. Proposed Database Changes (Room Version 7)
- **Modify `documents_cache`**: Add columns `subtotal`, `discount`, `extra_fees`, `notes`, `terms_text`, `pdf_url`, `shared_at`, and `sync_status`.
- **Add `document_items_cache`**: Store invoice/quote items with columns `id`, `document_id`, `product_id`, `name`, `description`, `quantity`, `unit_price`, `line_total`, `sort_order`.
- **Add `sync_outbox`**: Queue operations (`CREATE`, `UPDATE`, `DELETE`) with columns `id` (UUID), `entity_type`, `entity_id`, `operation_type`, `payload`, `attempts`, `created_at`, `status`.
- **Add `offline_quota_lease`**: Cache backend issued allowances with columns `id`, `user_id`, `device_id`, `plan_code`, `period_month`, `allowed_limit`, `consumed_count`, `expires_at`, `signature`.
- **Add `local_usage_ledger`**: Track offline created documents against the lease with columns `id`, `document_id`, `period_month`, `lease_id`, `created_at`, `synced`.

## 5. Migration and Data Preservation Strategy
- Update Room Database version to `7`.
- Create a migration path:
  ```sql
  -- Alter documents_cache table to add fields
  -- Create document_items_cache table
  -- Create sync_outbox table
  -- Create offline_quota_lease table
  -- Create local_usage_ledger table
  ```
- Remove `.fallbackToDestructiveMigration()` and write a test case to verify migration safety.
