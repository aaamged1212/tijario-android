# Project State (Android & Web Repos)

- **Android Repo Branch**: `codex/local-first-complete`
- **Web Repo Branch**: `codex/local-first-control-plane` reserved for local control-plane implementation.
- **Current Local-First Phase**: Room foundation, data-mode routing, repository soft deletion/restoration, active-limit enforcement, historical customer snapshots, and the encrypted archive codec are implemented locally. Room export/restore, UI, quota reconciliation, and Drive remain.

## Tijario status:
- Google Play Closed Testing is active.
- Current local remediation branch centralizes remote-cache replacement decisions in `RemoteCacheReplacementPolicy.shouldReplace(...)` for business settings, customers, products, and documents.
- Local architecture docs now define the future Local-First direction as planning only: Room as operational source of truth, Supabase as control plane, Google Drive as encrypted backup/restore transport, and document usage based on immutable creation events.
- Room schema 16 implements the first Local-First foundation: `document_creation_events`, `account_entitlements`, `backup_settings`, `backup_records`, `backup_file_entries`, `device_bindings`, and `deleted_record_history`.
- Legacy `local_usage_ledger` rows migrate into immutable creation events. Successful document sync changes `PENDING` to `ACKNOWLEDGED` and no longer deletes the event.
- Explicit `local_drive` accounts now use Room for operational customer, product, document, and business-setting writes and do not enqueue those writes for cloud synchronization. Missing or unknown modes remain `legacy_cloud`.
- Account usage persists data mode, quota scope, limits, template policy, and entitlement version locally. Local-Drive document creation records an immutable lifetime or billing-cycle creation event.
- Normal logout clears transient session state without deleting account-local Room data; destructive device-data removal remains a separate explicit path.
- Local-Drive deletion retains customer, product, and document rows plus deletion history. Repository restore reactivates the same identity and does not create a second document usage event.
- Room schema 17 stores customer name/WhatsApp/city snapshots on documents, explicit document deletion timestamps, and PDF generation status so historical documents do not silently change when the customer record changes.
- The offline `.tijario` codec uses AES-GCM and SHA-256 to authenticate a versioned logical archive and blocks cross-account restore and unsafe archive paths. Room logical export/restore is connected at the data layer; archive files, assets/PDFs, key handling, UI, and Google Drive are not yet connected.
- Account-scoped Room logical export and transactional restore now serialize approved tables as typed JSON, reject cross-account rows or unsupported columns before writes, and roll back database replacement on SQL failure. Portable keys, missing-PDF generation, asset restoration, UI, and Drive remain pending.
- Offline backup creation now collects existing account logo, product images, and document PDFs, records missing/failed PDF counts, verifies the encrypted archive, and atomically finalizes it in private storage. Portable key recovery, missing-PDF generation, asset restoration, UI, scheduling, and Drive remain pending.
- Local work exists to reduce Vercel usage and prevent retry loops.
- Product decision: allow duplicate customer WhatsApp numbers.
- Customer identity must be customer.id, not whatsapp_number.
- Supabase migration for duplicate WhatsApp exists locally in Web repo:
  `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- QA migration exists locally in Web repo:
  `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- Both migrations are NOT applied.
- Document Numbering Counters migration exists locally in Web repo:
  `supabase/migrations/20260712090000_document_numbering_counters.sql` (NOT applied).
- Android does not decrement stock locally; document writes remain online-first through the Web API.
- Android document create/update now sends selected `customer.id`; WhatsApp remains a contact field only.
- Android local fallback document creation reuses a selected customer id and does not create a duplicate customer for the same WhatsApp number.
- Android document language selection uses a mobile-safe bottom sheet and default document titles follow the selected document language unless the user entered a custom title.
- Android document save/update validation and server errors surface through Snackbar instead of inline card text.
- Android product forms now require available stock only for product rows; service stock remains optional.
- Android invoice forms now pre-block saved product quantities above editable stock; edit mode uses `current stock + original quantity from the same invoice`, services/manual lines are excluded, and server-side inventory remains authoritative.
- Android onboarding keeps the existing WhatsApp field and composes it from an MVP dial-code selector plus local phone number.
- **Document Numbering**: Batch A implemented, ensuring client-side operation UUID persistence and server-side Next.js route API forwarding to DB RPC for sync idempotency.
- **Adaptive UI & Latest Document Ordering**:
  - `created_at` latest document ordering is defined/tested in `DocumentOrdering.kt` and integrated into Dashboard/Documents ordering.
  - Root app layout adapts to the current container width at 300, 360, and 480 dp boundaries. Compact bottom navigation avoids clipped labels.
  - Adaptive Dashboard quick actions, Product/Customer cards, Document list ordering, localized action sheets, and in-form searchable Customer/Product pickers are implemented locally.
  - Follow-up UI polish local fixes cover localized customer/PDF labels, phone country-code fields, Store Settings badge removal, Customer/Product card hierarchy, AI online-only text placement, create-quote next-number display, discount/extra-fee row ordering, theme-aware settings icons, and AI-matched background/bottom-nav accent.
  - 2026-07-13 cleanup fixes cover App Settings notification row alignment, numeric keyboards, Store Settings phone-country synchronization, AI light-mode palette selection, root bottom-tab spacing, compact Dashboard financial metrics, Dashboard invoice/quote latest-document tabs, and current-date/non-misleading draft document previews.
  - Document form follow-up fixes cover quote-specific info labels, fixed `INV-`/`Q-` document number prefixes with editable numeric suffixes, due-date calendar picking, centered Dashboard latest-document chips, and FAB positioning above the bottom tab bar.
  - Document info/export action follow-up fixes cover creation-date calendar picking, local five-digit draft numbering from cached documents, quote title localization, government-building tax icon, and print/email actions in the document long-press sheet.
  - Android local document-options follow-up supports local shipping, fixed/percentage discount entry, multi-select local taxes/payment methods/terms, bottom-sheet option editing, negative discount display, and saved/reopened/PDF rendering through local document metadata. These fields are Android-local and will not restore cross-device after clearing app data unless Web/API/Supabase persistence is added later.
  - Android document edit/export follow-up fixes keep edit saves on the update path, use local cached documents for immediate five-digit `INV-` / `Q-` draft numbers, preserve exact saved document numbers in the local cache/list/detail/export paths, download locally generated PDFs into the public Downloads collection, request legacy storage permission on API 28 and below, restrict email export to email apps, preserve edited document title/language in the local cache, reuse the latest selected tax for future new documents, and provide local quick-select presets for discount and extra-fee amount/reason pairs.
  - Documents list ordering now prioritizes newest document `created_at`, then issue date/sync fallback, instead of update/sync time first.
  - Room schema files `13.json` and `14.json` are present; migration tests cover `12 -> 13` and `13 -> 14`.
  - Latest local Gradle validation passed: `compileDebugKotlin`, `testDebugUnitTest`, and `assembleDebug`; `lintDebug` previously timed out twice before a pass/fail result.
  - Device/emulator visual QA is still pending.
- Correct release order:
  migrations -> Web/API deploy -> verify logs and document flows -> Android update later.
- Do not touch production until approved.
- Android AAB artifact `app/release/app-release.aab` is present in the workspace but is correctly git-ignored. Do not upload it unless approved.
