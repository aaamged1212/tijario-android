# Project State (Android & Web Repos)

## 2026-08-24 - Yemen manual payment proof workflow (source published)
- Commit `8575db40768df0d071df2e9baebf3db6260e3ccd` is pushed to `origin/codex/yemen-payment-methods`.
- The publication did not create or upload an Android artifact, change Google Play, deploy Web/API, apply a Supabase migration, or change external configuration. Physical device QA remains pending.

## 2026-08-24 - Yemen manual payment proof workflow (local)
- The Yemen payment tab now shows only the supported payment methods. Selecting Al-Kuraimi, Jeeb, or internal transfer opens a dedicated proof screen with the requested Starter/Pro price reference, copyable account details, receipt-image selection, and a confirm action.
- A successful request calls the authenticated server-only manual-payment support endpoint and adds a durable local notification stating that verification is in progress. It does not alter the plan, grant entitlement, or call a billing webhook.
- Room schema version 21 keeps locally-created payment-request notices through announcement refreshes and prevents them from producing remote announcement receipts.
- `compileDebugKotlin` passed. Focused JVM task remains blocked before execution by three unrelated `BackupPlanPolicyTest.kt` Long-to-String errors. No commit, push, deployment, migration, external configuration change, or Google Play action occurred.

## 2026-08-24 - Yemen payment-method selection (local)
- The Android upgrade experience now has a local Yemen-only payment-method route. A user with saved business country Yemen or a Yemen device country sees Global Google Play and Yemen payment tabs; all other users continue directly to the existing Google Play purchase flow.
- The Yemen page is fully Arabic/English and Material-theme aware. It provides the requested static price references and copyable payment details without storing a receipt, changing a plan, or making a backend request for transfer payment.
- `compileDebugKotlin` passed. Focused unit-test execution is currently blocked before test execution by existing `BackupPlanPolicyTest.kt` Long-to-String mismatches. Physical RTL/LTR and Yemen/non-Yemen device QA remain pending.
- No Web/API, Supabase, migration, deployment, external configuration, or Google Play action occurred.

## 2026-08-21 - Authenticated mobile feedback delivery
- The Android feedback form now calls the authenticated support API rather than writing directly to PostgREST. Images are locally bounded before submission; support recipient, user identity lookup, private Storage access, and mail delivery remain server-side.
- Android commit `8ceddf2337fed5c3cf5ba9ffb9780240686dffd4` is published on `fix/android-runtime-critical-fixes`; compatible Web API and database contract are deployed separately.
- `assembleDebug` passed. The focused feedback contract test cannot start because three unrelated `BackupPlanPolicyTest` type errors fail test source compilation first. Physical delivery verification remains pending; no Android artifact was uploaded to Google Play.

## 2026-08-16 - Android startup branding
- The Android system splash no longer renders the full-bleed launcher bitmap. It uses the approved transparent brand mark inside the platform's 160dp safe area with an icon background that preserves contrast.
- Startup background and system-bar contrast follow Android day/night resources. The Compose fallback splash uses the same source asset without clipping and follows the active Material color scheme.
- Focused splash tests and Debug APK assembly passed. No backend, migration, deployment, push, merge, external configuration, or Play action occurred; physical startup QA remains pending.

## 2026-08-16 - Settings selection and pricing presentation
- App preference persistence now distinguishes System, Light, and Dark appearance while remaining compatible with the legacy dark-mode boolean. Language similarly distinguishes device language from explicit Arabic or English.
- Settings displays cached plan/profile context before compact navigation rows. Business currency and app preference choices use bottom sheets; settings action rows use spacing instead of divider lines.
- Pricing remains server/Google Play authoritative for billing and localized prices, but safe Free/Starter/Pro display definitions render synchronously so the horizontal plan carousel never starts empty. Only Pro is visually outlined with the primary color, and the existing detailed comparison remains below.
- Kotlin compilation, focused JVM coverage, Debug/AndroidTest assembly, and clean-diff validation passed. No backend, migration, deployment, push, merge, external configuration, or Play action occurred. Physical visual QA remains pending.

## 2026-08-16 - System-aware authentication and settings organization
- On first launch without stored preferences, Android now initializes Arabic only for an Arabic system locale, English otherwise, and mirrors the system light/dark setting. User-selected language and appearance continue to persist as explicit overrides.
- Authentication, verification, recovery, and onboarding surfaces now use the active Material theme with a consistent brand treatment and responsive light/dark behavior.
- Settings is now a compact grouped list. Business configuration is labeled `Business` / `النشاط التجاري`, while current plan, usage limits, retry, and upgrade are isolated under `Payments & Subscriptions`.
- Focused JVM tests and Debug/AndroidTest compilation passed. No backend, migration, deployment, push, merge, external configuration, or Play action occurred. Physical visual QA remains pending.

## 2026-08-16 - Local AI history, catalog pickers, and document search
- Android Room schema is version 20. New account-scoped AI generation history stores reply and caption variants under separate types, is deleted with that account's local data, and powers a scrollable copy-enabled bottom sheet in each AI tab.
- Country and calling-code selection now uses one full searchable catalog with flags and purpose-specific hints. Product currency selection searches an expanded ISO currency catalog by code or country. Business Settings and onboarding keep the phone calling code synchronized with the selected country.
- Invoice and quotation lists keep independent compact search text and filter by document number or customer name without changing the existing date ordering or payment-status filters.
- Focused JVM coverage and Debug/AndroidTest compilation passed. No Web/API contract, Supabase schema, deployment, push, merge, external configuration, or Play action occurred. Physical migration and UI QA remain pending.

## 2026-08-16 - AI tool result isolation
- Smart replies and captions now use distinct local UI result states. Switching tabs no longer exposes a prior reply under captions or the reverse, and caption results exclude customer intent/message analysis.
- Each tab retains its own scroll position and scrolls to a newly completed result. AI content includes a bottom inset when shown inside the root shell so the final card remains reachable above the navigation bar.
- Focused state tests and `assembleDebug` passed. No backend, migration, deployment, push, merge, external configuration, or Play action occurred. Physical visual QA remains pending.

## 2026-08-16 - Official notification branding asset
- Announcement and backup/restore notifications now use the supplied official Tijario logo as their full-color large icon. Android continues to use the monochrome status-bar resource required by the platform for the small icon.
- `assembleDebug` passed. Physical visual validation on an Android device remains pending. No backend, migration, deployment, push, merge, external configuration, or Play action occurred.

## 2026-08-15 - Document update item-loss correction
- A Room update ordering defect was identified: `upsertDocument` uses `REPLACE`, so writing new document items before the parent replacement cascaded those items away. The local update transaction now replaces the document parent before deleting and inserting the item set.
- A server-backed document left itemless by the prior defect is eligible to hydrate the last complete server snapshot when online. Purely local documents remain protected and report the typed missing-items state instead of silently replacing local data.
- Focused repository tests and `assembleDebug` passed. Source is committed locally as `b8465e9346b515071037f90a3aee78ebbb82f367`; no device was connected and no backend, migration, deployment, push, merge, external configuration, or Play action occurred.

## 2026-08-15 - Local document detail cache recovery
- On `fix/android-runtime-critical-fixes`, document detail loading now treats a complete Room snapshot as the offline source of truth. A replaceable synced summary missing item rows may hydrate once through the existing mobile detail contract and atomically replace its item cache; local-only, pending, conflict, and plan-blocked records remain protected.
- New invoice/quote form numbering reads current Room history rather than an already-observed UI list. New customers/products created from an active document picker are returned to that document immediately. Calling-code and product-currency selection now support searchable bottom sheets.
- Focused JVM coverage (59 tests) and `assembleDebug` passed. Physical QA is pending. No backend, migration, deployment, push, merge, external configuration, or Play action occurred.

## 2026-08-13 - Room-first operational storage for every account
- Android operational data is now Room-authoritative for `legacy_cloud`, `local_drive`, and future entitlement modes: customers, products/services, documents, document items, local metadata, taxes, payment methods, signatures, and local terms follow the local path. Historical Supabase operational records are neither imported nor deleted.
- Business settings are hybrid and local-first. Room is the immediate UI source; a background best-effort mirror may update Supabase. The one allowed read is initial settings hydration when the current user's Room settings are absent.
- Cached signed entitlement data remains authoritative for plan limits while Room supplies active customer/product counts and document creation events. Invoices and quotes receive their next number from Room history and `DocumentNumbering`.
- Focused JVM tests (47) and `assembleDebug` passed locally. Version remains `1.1.9` / code `19`; physical offline QA is pending. No backend, migration, deployment, push, merge, or Play action occurred.

## 2026-08-12 - Android document-number contract mapping ready locally
- Android now maps the compatible backend's `DOCUMENT_NUMBER_INVALID` and `DOCUMENT_NUMBER_DUPLICATE` save responses to localized Arabic/English messages.
- Full JVM tests, both lint variants, Debug assembly, and Release assembly passed. Version remains `1.1.9` / code `19`; `.agents` remains local and excluded.
- The matching Web migration/API remains a separate release gate. No migration, deployment, push, merge, or Google Play upload occurred.

## 2026-08-11 - Runtime-critical Local-First AI contract ready locally
- Android on `fix/android-runtime-critical-fixes` now submits the selected local customer/product IDs with a bounded context snapshot rather than converting the selection to unstructured prompt text. The snapshot contains no contact fields, entitlement information, or secrets.
- The compatible Web/API work and document-number preservation migration are isolated on `fix/mobile-runtime-critical-backend`. The Android change is not production-ready independently: apply the migration and deploy the compatible API before releasing an Android build that relies on this contract.
- Local validation passed: focused snapshot tests, full JVM tests, `lintDebug`, `lintRelease`, Debug/Release/AndroidTest/PlayQa assembly, and `bundleRelease`. Physical device AI validation remains pending because no device/emulator was connected.
- The branch is published at `08b0d85cf38202a03585b70490b4e0d21dbd8f01`, unmerged and without a Play upload.

## 2026-08-09 - Analytics taxonomy corrected
- Analytics event names are centralized and typed. AI caption generation is distinct from AI reply generation; operational event calls carry no sensitive content.

## 2026-08-09 - CI and backup restore hardening
- Android CI now covers `main`, `codex/**`, `fix/**`, and `feature/**`; audit and full validation jobs use `contents: read` and artifact uploads instead of automated write-back commits.
- A malformed non-null logical backup value now fails with a typed backup validation error before any Room binding. GitHub workflow execution remains pending until a future authorized branch push.

## 2026-08-09 - Financial precision boundary reviewed
- Local money normalization and document totals use `BigDecimal`; focused regression coverage now includes decimal precision, large values, tax, discount, paid amount, and Arabic/comma decimal input.
- Existing `Double` API/model contracts are intentionally retained as explicit compatibility boundaries and are documented in `docs/ai/MONEY_BOUNDARY_AUDIT.md`.

## 2026-08-09 - Hardening branch started
- `main` was reconciled through `36bf6d3` and now contains the validated versionCode `19` / versionName `1.1.9` backup recovery source.
- `fix/android-full-hardening` starts from that exact main SHA. Its first scoped security change is local only and has focused JVM coverage; no release action occurred.

- **2026-08-01 entitlement contract verification**: Production Android source is unchanged. A shared 12-case API fixture is parsed with the real `AccountUsageResponse` serializer, and full JVM, instrumentation compilation, lint, and `playQa` assembly gates pass. Compatible Web migrations/API remain unapplied and undeployed; physical-device QA remains pending.

- **2026-08-01 Drive first-upload correction**: Resumable uploads now request complete verification metadata and recover a partial create response by resolving the exact remote backup before validation. Missing immediate list visibility is a bounded automatic retry, not a user-visible failure requiring Retry. Backup JVM tests and `assemblePlayQa` pass; physical Drive verification remains pending by request.

- **2026-07-31 restore correction**: Device logcat proved encrypted archive validation succeeds through `MANIFEST_VALIDATED`; the failure is an over-strict logical reference check before Room writes, not Drive or backup-key failure. Local code preserves valid historical references, retains only real Room item/document integrity checks, hides internal restore-safety snapshots from normal history, and maps any true required relationship failure to a specific localized error. Focused JVM tests and `assemblePlayQa` passed; release metadata is `18` / `1.1.8` and physical retest is pending.

- **2026-07-30 backup candidate**: Local uncommitted work on `codex/fix-backup-destinations-drive-restore-notifications` separates verified Drive upload success from bounded retention cleanup, adds typed transactional-restore phases, defaults phone copies to `Downloads/Tijario/Backups`, and correctly gates backup work on both notification permission and channel state. Full JVM tests, Android test APK compilation, lint, and `assemblePlayQa` pass. Device QA is pending because ADB currently has no connected device.

- **Backup hardening**: On `codex/fix-backup-destinations-drive-restore-notifications`, Drive upload and all restore sources now report real WorkManager progress and provide cancelable foreground work. Phone backup shows the actual selected SAF directory name. Physical Drive/notification/SAF QA is pending; focused JVM tests, lint, and `assemblePlayQa` passed locally.
- **Restore completion**: SAF restore now holds a persistable read grant only until the archive is privately staged, reports typed localized worker errors, and can resolve a missing exact-version key online for Drive, SAF, or local restore sources. Existing `dataSync` foreground-service declarations are used by all backup workers. A Room/assets/FakeDrive integration test compiles; device execution is pending.

- **Release-blocker local correction**: On `codex/fix-production-release-blockers`, retryable lease reconciliation now atomically invalidates the rejected exact lease while preserving the pending event for reassignment. Startup account-deletion cleanup uses explicit success/failure state and blocks authenticated routing, notifications, and sync after local cleanup failure. Focused tests and `assemblePlayQa` passed; no commit or external action occurred.

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
- **2026-07-29**: Retryable lease failures now release only pending lease assignments for later reconciliation. Pending account-deletion cleanup now recovers before authenticated routing using local-only cleanup; no external action occurred.

## 2026-07-29 Backup destination/restore release candidate
- Android backup hardening is isolated on `codex/fix-backup-destinations-drive-restore-notifications` at version `16` / `1.1.6`.
- Phone backup no longer falls back to shallower Downloads locations. Google Drive backup is a distinct manual operation, with remote account/size/checksum verification before it is marked uploaded.
- Device QA is still required for Android storage-provider behavior, Drive upload/restore, cancellation, and notification permission behavior. No production change has been made.

## 2026-07-31 Backup restore/history follow-up
- The local Room schema is now 19. `backup_records.restored_at` ties a successful restore to the exact archive record without presenting restore preparation as a completed backup.
- Logical archive creation uses one SQLite read transaction. Restore relationship enforcement occurs in Room's atomic insert/foreign-key-check transaction rather than a duplicated pre-validator.
- User-visible history contains only completed phone copies, verified Drive uploads, and completed restores. Internal safety snapshots and intermediate/failure states remain hidden.
- Manual Drive uploads are expedited and do not inherit automatic charging/battery/storage constraints; Wi-Fi-only remains honored.
- Physical local/Drive restore and first-attempt Drive upload QA remain pending. Android version remains `18` / `1.1.8`.

## 2026-08-09 Android full-hardening branch
- `main` was reconciled and validated at `36bf6d3`; focused hardening work is local on `fix/android-full-hardening` and has not been pushed or merged.
- Authentication callbacks now use an allowlisted deep-link policy and the app declares HTTPS App Links for both Tijario hosts. Legacy custom schemes remain supported.
- The remaining release gate for App Links is external verification of the live `assetlinks.json` certificate relationship and an end-to-end release-device callback test.
- Backend API and Drive now use distinct bounded timeout profiles. Diagnostics in the hardened paths omit response text, tokens, and throwable stack traces.
- Document HTML now constrains payment-status classes and embedded image data; draft preview calculations preserve decimal input as `BigDecimal` until a compatibility boundary.
- Cache refresh failures now publish only localized safe messages. Backend error codes remain mapped specifically; raw technical text is not user-visible.
- Analytics now accepts only typed central events and no free-form Bundle payload. Existing event wire names remain unchanged and carry no user-generated content.
- The application root now observes only a distinct shell projection instead of full lists and transient cache details, reducing unrelated recomposition pressure without changing screen-level data observation.
- Document-preview expand/close accessibility labels now resolve through Arabic/English localization instead of an Arabic-only literal.
- The app manifest explicitly enables Android's modern back callback while preserving existing Compose back handling.
- Auth callback decoding now remains compatible with minSdk 26, and `lintDebug` passes without errors after the full hardening changes.
- Full local verification now passes: `clean`, full JVM tests, `lintDebug`, `lintRelease`, Debug/Release/AndroidTest/PlayQa assembly, and `bundleRelease`. The hardening branch is published at `f6c32b4` and is not merged to `main`; GitHub Actions Source Audit Scan and Android CI run `31327114193` both passed.
- No physical device/emulator is connected. Release QA remains required for documents/PDF/share, backup/Google Drive, notifications, and verified App Links. The current Release manifest has transitive advertising-ID declarations from an existing dependency; no analytics/Facebook SDK configuration was changed by this work.
- Android CI has a completed reliability follow-up after a documentation-only push hit a non-diagnostic `:app:packageDebug` failure despite the preceding green source run. The workflow now separates lint, Debug, and Release Gradle gates with non-parallel execution and stack traces; GitHub Actions run `31337272388` passed both jobs.

## 2026-08-13
- **Current task**: Local Android follow-up for offline document edits, chronological document ordering, global country/currency catalogs, and invoice stock controls on `fix/android-runtime-critical-fixes`.
- **State**: Saved-document updates stay inside the Room-first local path and no longer require an entitlement lookup. New records retain a full creation timestamp for chronological ordering. Country/phone and currency selectors share catalog data across onboarding, business settings, customers, and products.
- **Validation**: Six focused JVM suites and `assembleDebug` passed. `lintDebug` did not complete before the local command timeout and is pending. Device QA remains required.
- **Safety**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-10 Runtime-critical local remediation
- Uncommitted work on `fix/android-runtime-critical-fixes` makes Settings/child-route back navigation fall back safely to the main screen, centralizes creation-plan checks before navigation and at save time, and derives LocalDrive usage from Room without changing signed limits.
- New documents default to the saved business currency until the user overrides it. Local invoice and quote numbers are type-specific, preserve valid user-entered suffixes and leading zeroes, reject exact local duplicates before Room writes, and never change on edit.
- AI reply/caption flows do not submit local-only customer/product identifiers. They send a bounded generation context without WhatsApp, email, or local IDs; the compatible local backend contract now accepts the snapshot, pending separate authorized publication and deployment.
- The build remains version `1.1.9` / code `19`. Full JVM tests, both lint variants, Debug/Release/AndroidTest/PlayQa assembly, and `bundleRelease` completed successfully locally. No connected ADB device is available for physical QA.
- No commit, push, deployment, migration, Production write, external configuration change, or Play upload occurred. `.agents` remains untracked and excluded.

## 2026-08-13 LocalDrive offline CRUD recovery
- Local invoice/quote creation no longer requires a network lease refresh. With a valid cached signed entitlement, Room validates the local document count and writes the document, items, customer snapshot, and pending creation event atomically; it attaches an active compatible lease only when one is already cached.
- Customer, product/service, and document create/update remain `LOCAL_ONLY` in LocalDrive and do not enqueue operational cloud sync. Missing or expired entitlements remain blocked with typed errors; plan limits remain locally enforced.
- Focused JVM coverage (47 tests) and Debug assembly passed. Physical offline QA is still required before any release action. No backend, migration, deployment, or external change occurred.

## 2026-08-16 Preview, Currency, and AI Follow-up
- **State**: Local uncommitted Android changes make the draft preview use the same cached business-logo source as the PDF path. Document item selection and saving consistently reject cross-currency products, and the picker shows remaining tracked stock after current invoice reservations. The bounded AI V3 snapshot now includes product category and its actual currency.
- **Validation**: 58 focused JVM tests and `assembleDebug` passed. Physical device verification remains pending.
- **Safety**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred.

## 2026-08-16 Menu and Paid Backup Automation Follow-up
- **State**: The current Android change set restores profile avatar editing, renames Settings to Menu, adds language-aware menu motion, compacts pricing/search/filter UI, and adds notification pull-to-refresh.
- **Backup policy**: Automatic local scheduling and Google Drive are enforced for verified paid plans; Free is normalized to manual local backup and Drive disabled. Scheduled local archives remain offline-capable and Drive stays a separate network transport.
- **Validation**: Focused JVM tests, Debug Kotlin compilation, `assemblePlayQa`, `lintDebug`, and `git diff --check` passed. Physical UI and real elapsed WorkManager schedule verification remain pending.
- **Safety**: Included in the requested local Android commit. No push, deployment, migration, Production write, Web change, external configuration change, or Google Play upload occurred.

## 2026-08-16 Navigation and Settings UI Follow-up
- **State**: Local uncommitted Android UI changes apply Tijario branding to core headers, center child tab titles, separate personal profile editing from account security, add plan-aware upgrade controls, and make language/theme choices explicit and brand-colored.
- **Backup and splash**: Backup actions are grouped into compact sections without changing repositories or workers. The system and Compose splash layers now share the exact transparent brand asset on a dark background with no white icon plate.
- **Validation**: 17 focused JVM tests, `assembleDebugAndroidTest`, `assembleDebug`, and `lintDebug` passed. Physical RTL/LTR and cold-start visual QA remains pending.
- **Safety**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred.

## 2026-08-16 Arabic Copy and Document Branding Follow-up
- **State**: Local uncommitted Android changes normalize Arabic fathatan placement, refine business-information terminology and alignment, simplify the appearance icon, and make the plan banner more conversion-focused.
- **Documents and navigation**: Free-plan PDF output includes compact linked Tijario branding, dashboard View All follows RTL/LTR correctly, and AI history is colocated with generation without changing persistence or generation behavior.
- **Validation**: Focused JVM tests, `assemblePlayQa`, `lintDebug`, and `git diff --check` passed. Physical RTL/LTR and generated-PDF hyperlink QA remain pending.
- **Safety**: No commit, push, deployment, migration, Production write, Web change, external configuration change, or Google Play upload occurred.

## 2026-08-21 GoMarketMe Affiliate Attribution
- **State**: Android now has an optional GoMarketMe SDK sidecar initialized from `MainActivity`. It is independent of login, Room, LocalDrive CRUD, business settings, AI, and entitlement decisions.
- **Purchase flow**: Only a server-verified Google Play purchase requests one best-effort asynchronous SDK transaction sync. Failure cannot block acknowledgement or Tijario purchase completion.
- **Validation**: Billing resolves to `9.1.0`; forced Debug assembly passed. The focused JVM task is currently blocked by pre-existing compile errors in `BackupPlanPolicyTest.kt`.
- **Release gate**: Google Play Data Safety review and a physical test purchase/attribution check are required before release. No external system changed.

## 2026-08-21 Feedback and dashboard light-theme follow-up
- **Current state**: Feedback uses Tijario-specific copy, compacts optional attachments, and sends a bearer-authenticated request to the mobile backend. Customer metric labels are compact, and dashboard quick-action colors honor the selected app theme.
- **Delivery contract**: Android cannot and does not use a user's mailbox or provider credential. The compatible Web backend identifies the account from the bearer token and sets the authenticated email as the support message reply-to.
- **Validation**: Debug assembly passed. New focused UI-contract coverage is pending execution until the existing `BackupPlanPolicyTest.kt` compile mismatches are resolved.
- **Release dependency**: The Android feedback path requires the compatible Web route, the pending feedback storage migration, and server-only email configuration before it can deliver support messages.
- **Safety**: Local source only; no commit, push, deployment, migration, Production write, external configuration change, or Google Play upload.

## 2026-08-21 Google Play engagement prompts
- **Current task**: Local Android implementation of native Google Play flexible updates and in-app review on `fix/android-runtime-critical-fixes`.
- **State**: Flexible updates are checked only for authenticated users and are locally paced to one attempt per 24 hours. Review requests occur after meaningful dashboard use or an explicit Settings action; Google Play owns display and review submission.
- **Validation**: `assembleDebug` passed. Focused JVM execution is pending because the existing Debug test source set does not compile due to three unrelated `BackupPlanPolicyTest.kt` type mismatches.
- **Safety**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains untracked and excluded.

## 2026-08-22 Android version preparation
- **Version**: The current Android source is version `0.0.21` / code `21` and includes the completed native Google Play flexible-update and review prompt work.
- **Remaining QA**: Verify official dialogs only from a Play-installed test-track build; a sideloaded Debug APK cannot prove that integration.
