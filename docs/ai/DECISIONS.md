# Architecture Decisions

## 1. Customer Identity
- `customer.id` is the primary identity.
- `whatsapp_number` is not unique.
- Multiple customers may share the same WhatsApp number.
- No auto-merge by WhatsApp number.
- No document linking by WhatsApp number.

## 2. Mobile Sync
- No infinite retry loops.
- Max retries with backoff.
- Non-retryable errors are not retried.
- `operation_id` idempotency.

## 3. Vercel Usage
- Reduce startup calls.
- Avoid unnecessary usage/offline lease/announcement/sync calls.
- Vercel remains for AI/PDF/billing/server-only logic.
- Supabase may be used directly only where RLS is safe.

## 4. Closed Testing
- No Google Play upload unless approved.
- No disruptive production changes unless approved.
## 5. Android MVP Write Model
- **Online-Only Writes**: Creating, updating, and deleting documents, customers, products, and business settings require internet and must persist remotely before showing final success.
- **Cached Reads**: Local SQLite Room cache is used strictly for reading/displaying previously synced data.
- **No Offline CRUD Queue**: No local-only success states. If remote save fails or the device is offline, show an error without caching or pretending the data was saved remotely.

## 6. Inventory Ownership
- Invoice inventory is mutated only by trusted Web/database RPCs.
- Android sends stable product IDs and quantities, maps stock errors, and refreshes product cache after successful document mutations.
- Quotes and manual document lines do not affect inventory.

## 7. Persisted Document Presentation
- Saved `document_language` controls reopened preview/PDF language independently from the current app language.
- Optional business address, email, and website are cached locally and rendered only when present.
