# Project State (Android & Web Repos)

- **Google Drive post-consent fix**: Prepared work on `codex/backup-drive-production-ready` resolves the stable Drive account identity through `about.user.permissionId` rather than optional Google profile data. Version is code `15` / name `1.1.5`; real-device OAuth/Drive validation remains pending.

- **CI status**: The permanent Android API 36 workflow now initializes the Android SDK before installing API 36. Local unit tests, instrumentation APK assembly, lint, debug assembly, and release assembly pass. GitHub Actions run `30048718414` also passed both compile/unit and lint/release jobs.
- **Production crypto status**: The safe Vercel production validator reports `ENTITLEMENT_SIGNING_KEY_ID_MISSING`. No production configuration was modified.
- **Android Closed Testing preparation**: Version code `14` / version name `1.1.4` has a locally validated signed AAB on `codex/backup-drive-production-ready`. Google Play upload and physical-device QA remain pending.
- **Android Repo Branch**: `codex/local-first-complete`
- **Web Repo Branch**: `codex/local-first-control-plane` reserved for local control-plane implementation.
- **Current Local-First Phase**: Local code is complete for Room operational ownership, signed entitlements, lease-bound immutable usage, encrypted backup/restore, Drive transport abstractions, PDF/assets, scheduling, and release runbooks. Production rollout and physical-device QA remain blocked.

## Tijario status:
- Google Play Closed Testing is active.
- Current local remediation branch centralizes remote-cache replacement decisions in `RemoteCacheReplacementPolicy.shouldReplace(...)` for business settings, customers, products, and documents.
- Local architecture docs now define the future Local-First direction as planning only: Room as operational source of truth, Supabase as control plane, Google Drive as encrypted backup/restore transport, and document usage based on immutable creation events.
- Room schema 16 implements the first Local-First foundation: `document_creation_events`, `account_entitlements`, `backup_settings`, `backup_records`, `backup_file_entries`, `device_bindings`, and `deleted_record_history`.
- Legacy `local_usage_ledger` rows migrate into immutable creation events. Successful document sync changes `PENDING` to `ACKNOWLEDGED` and no longer deletes the event.
- Explicit `local_drive` accounts now use Room for operational customer, product, document, and business-setting writes and do not enqueue those writes for cloud synchronization. Missing, unknown, invalid, or expired entitlements fail closed before operational writes.
- The shared operational-write guard now also requires a future `expires_at`; explicit `legacy_cloud` remains valid only when represented by a trusted, unexpired persisted entitlement.
- Account usage persists data mode, quota scope, limits, template policy, and entitlement version locally. Local-Drive document creation records an immutable lifetime or billing-cycle creation event.
- Normal logout clears transient session state without deleting account-local Room data; destructive device-data removal remains a separate explicit path.
- Local-Drive deletion retains customer, product, and document rows plus deletion history. Repository restore reactivates the same identity and does not create a second document usage event.
- Room schema 17 stores customer name/WhatsApp/city snapshots on documents, explicit document deletion timestamps, and PDF generation status so historical documents do not silently change when the customer record changes.
- The offline `.tijario` codec uses AES-GCM and SHA-256 to authenticate a versioned logical archive and blocks cross-account restore and unsafe archive paths. Room export/restore, account assets, local PDF regeneration, versioned wrapped keys, local archive files, and SAF UI are connected locally; Google Drive transport is not.
- Account-scoped Room logical export and transactional restore serialize approved tables as typed JSON, reject cross-account rows or unsupported columns before writes, and roll back database/assets on failure. Scheduling, Drive, and device runtime QA remain pending.
- Offline backup creation collects existing account logo, product images, and document PDFs, records missing/failed PDF counts, verifies the encrypted archive, and atomically finalizes it in private storage. The settings UI can export/import through SAF; backend key provisioning is still local/unreleased.
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
- Local Backup/Drive hardening is in progress on `codex/backup-drive-production-ready`; control-plane claims and restore relationship validation are fail-closed. Google Identity authorization, Ktor Drive REST, transient token runtime, and Worker reconstruction are implemented and JVM-tested locally; real OAuth/Drive/device verification remains pending.
- Backup scheduling now derives its maximum frequency and retention counts from the locally persisted, signed entitlement payload. A downgrade clamps an existing schedule; an upgrade keeps a user's less-frequent choice.
- Android AAB artifact `app/release/app-release.aab` is present in the workspace but is correctly git-ignored. Do not upload it unless approved.
- Android `1.1.5` includes onboarding/backup-key hardening: verified entitlement initialization is single-flight, onboarding saves wait for Room-ready claims, LocalDrive saves avoid cloud outbox writes, and primary-device backup-key failures are typed. Full local Gradle validation passed; device QA remains pending.
- A local-only debuggable `playQa` build type now uses the release/upload signing config with package `app.tijario` and version `15` / `1.1.5`. The Upload Key Android OAuth client must be verified manually before physical Google Sign-In/Drive QA.
- Physical QA evidence is recorded in `docs/release/ANDROID_1_1_5_PHYSICAL_QA.md`; unresolved Google sign-in, plan, purchase synchronization, and document deletion defects are not fixed by the 1.1.5 implementation.

## 2026-07-25 Multi-installation follow-up (local, uncommitted)
- Android remains versionCode `15` / versionName `1.1.5`.
- LocalDrive document create/update uses Room and does not schedule operational cloud sync after a local save. Missing initial entitlement or offline lease is a typed error state.
- The multi-installation backend migration is not applied; no release path may use the updated entitlement/backup RPCs until that migration is explicitly approved and deployed with compatible Web code.

- Runtime Android entitlement validation is active-installation based and no longer depends on primary-device fields. Version remains `15` / `1.1.5`.
- **2026-07-26**: Multi-installation Android source is published at `df99d4b2f8966f166ef14a8ec19c5e0c32c7289e` on `codex/backup-drive-production-ready`; it remains release-blocked on the Web migration/API rollout.
- **2026-07-26**: Uncommitted LocalDrive document-save follow-up keeps create/update Room-only, returns typed entitlement/lease/limit codes, and avoids a post-save usage network refresh.
- **2026-07-26**: The LocalDrive forced plan-refresh deadlock is fixed locally and covered by a focused JVM test. Physical verification that the current-plan card bootstraps and document save succeeds is still required.
- **2026-07-26**: The connected device proved that `/api/mobile/account/entitlement` returns a valid response, but Android rejected its cross-runtime JSON serialization before RSA verification. The local verifier is corrected; entitlement and lease rows now persist on the device.
- **2026-07-26**: Local onboarding is no longer blocked by optional backup-key envelope initialization after a valid entitlement bootstrap. Focused JVM coverage and connected-device ready-state inspection passed; completing real store settings remains manual QA.
- **2026-07-26**: Connected-device backup QA found Android Keystore rejected a caller-provided AES-GCM IV while sealing the installation key material. The local envelope now uses a Keystore-generated IV; focused JVM tests and `assemblePlayQa` passed, and a new encrypted local backup was created on the phone. Google Drive account consent/folder/upload/restore remains manual device QA.
- **2026-07-26**: Google Drive device QA now has a precise boundary: Google Identity returns `INTERNAL_ERROR` on consent completion before `drive/v3/about`. The shown Android OAuth package/SHA-1 matches the local `playQa` APK; the app records only safe status metadata and Drive scope/Audience verification remains an external Google Cloud action.
- **2026-07-27**: Local uncommitted follow-up resolves saved document rendering from persisted `template_id` in both detail and list export paths, preserving local Room item/template data through PDF generation. LocalDrive business settings now mirror changed payloads to the existing `business_settings` table after the Room write; the payload fingerprint prevents duplicate unchanged calls and failures never invalidate the local save.
- **2026-07-27**: Connected-device logcat confirmed blank saved documents are a Chromium tile-memory rendering failure, not a Room save failure. Preview surfaces now render at the actual viewport size and uncached remote logos fall back to initials. Focused JVM tests, `compileDebugKotlin`, and `assemblePlayQa` pass; physical detail/PDF QA remains pending.
- **2026-07-27**: Current uncommitted LocalDrive follow-up separates the signed Free-plan limit from the two-credit lease batch, keeps saved document detail to one persisted-template WebView, and writes automatic local backups through MediaStore to `Downloads/Tijario/Backup`. Physical device confirmation remains pending because no device is connected.
- **2026-07-27**: Current uncommitted PDF follow-up corrects the local manual `WebView` layout/load/draw order and increments the export cache key to `pdfv4`, targeting blank saved PDFs. Focused JVM tests and `assemblePlayQa` pass; a physical preview/detail/PDF comparison remains pending.
- **2026-07-27**: Current uncommitted export follow-up replaces the manual raster draw path with WebView's native print adapter and uses `pdfv5`. Focused document tests and `assemblePlayQa` pass; physical zoom verification remains pending. `assembleRelease` exceeded local three- and seven-minute command limits and is not a release gate pass.
- **2026-07-27**: A later cached signed `bundleRelease` completed successfully, alongside full JVM tests, `lintDebug`, `assembleDebug`, and `assemblePlayQa`. The only open PDF gate is physical zoom/render verification.
- **2026-07-27**: The validated Android branch is published to `origin/codex/backup-drive-production-ready`; no release deployment or Play upload occurred.

## 2026-07-29 Release-blocker branch
- `codex/fix-production-release-blockers` is locally validated for lease-backed LocalDrive creation-event reconciliation and safe post-confirmation account cleanup.
- The compatible Web migrations/API are not deployed. Device-based multi-installation quota and disposable-account deletion verification remain pending.
- Local reconciliation now requires an exact lease cycle, consumes a credit only with the first acknowledgement, and retains unassignable legacy events as pending. Account cleanup persists a retry marker after server confirmation. These changes are local only.
