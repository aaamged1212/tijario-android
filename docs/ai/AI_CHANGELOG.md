# AI Changelog

## 2026-08-09 - Analytics event taxonomy correction
- Added centralized typed analytics event names for customer, product, invoice, quote, AI reply, AI caption, and subscription start.
- Corrected AI caption generation to record its own event instead of the AI reply event. No content or personal data is attached to these events.

## 2026-08-09 - Read-only Android CI hardening
- Broadened primary CI coverage to `main`, `codex/**`, `fix/**`, and `feature/**` with release lint and clean-diff checks.
- Replaced branch-writing audit/validation workflows with read-only jobs that upload artifacts; removed the obsolete workflow that committed heartbeat logs back to a source branch.
- Replaced the remaining backup restore force unwrap with a typed validation failure and added malformed-value coverage.

## 2026-08-09 - Financial precision boundary hardening
- Added a `BigDecimal` money parser and plain-string normalization while retaining the existing `Double` transport compatibility facade.
- Added regression coverage for decimal precision, large totals, discount, tax, paid amount, Arabic digits, and comma decimal entry.
- Documented the local calculation, Room, and API money boundaries in `MONEY_BOUNDARY_AUDIT.md`.

## 2026-08-09 - Android document and sharing security baseline
- Escaped custom document title and signature values before HTML insertion, and added malicious-content regression coverage.
- Restricted network logo fetching to bounded HTTPS image downloads for PDF generation; preview uses only the locally cached logo.
- Disabled cleartext traffic, narrowed FileProvider backup sharing to a cache copy, and limited announcement links to HTTPS Tijario hosts.
- Focused JVM tests passed. No production or external action occurred.

## 2026-08-01 - Mobile entitlement API contract fixture
- Added a shared 12-case JSON fixture and production-serializer JVM test for Free, paid, legacy, missing-row, installation-presence, grace-period, cancellation, and expired-plan responses.
- Kept production Android logic unchanged; full JVM/build/lint gates passed and physical-device QA remains pending.

## 2026-07-30 - Backup upload, restore, destination, and notification hardening
- Separated Drive retention from a verified upload so retention cannot turn `DRIVE_UPLOADED` into `DRIVE_FAILED`.
- Added safe stage diagnostics and typed restore errors for asset and Room application phases, with schema validation before mutation and asset rollback support.
- Made the phone default `Downloads/Tijario/Backups`, retained custom SAF selection/reset behavior, and corrected notification permission/channel gating.
- Local JVM tests, Android test APK assembly, lint, and `playQa` assembly passed. Physical QA remains blocked because no device is attached.

## 2026-07-29 - Lease invalidation and deletion-recovery state gate
- Retryable rejected leases are invalidated atomically with clearing their pending creation-event assignment, preventing the same lease from being reused during recovery.
- Pending account-deletion cleanup now has explicit running/succeeded/failed startup states and a local-only retry screen that blocks routing and sync on failure.
- Focused quota/account-deletion JVM tests and `assemblePlayQa` passed; no external action occurred.

## 2026-07-24 - Closed Testing 1.1.4 release preparation
- Prepared version code `14` / version name `1.1.4`, verified dependency-provided AD_ID permissions and disabled Meta advertiser-ID collection, and generated a signed local Release AAB with provenance.

## 2026-07-24 - Android API 36 CI runner repair
- Configured the permanent Android CI jobs with the official SDK setup action, accepted SDK licenses, and installed API 36/Build Tools 36.0.0 before Gradle runs.
- Removed the obsolete one-shot workflow that could modify, commit, and push source automatically.

## 2026-07-23 - API 36 and entitlement trust anchor
- Targeted API 36 and replaced the local entitlement-key Gradle property with a committed RSA public-key registry protected by a SHA-256 fingerprint.

## 2026-07-18 - Local-First completion and release documentation
- Enforced signed, account/device-bound offline entitlements and active lease ownership before Local-Drive document creation.
- Added immutable event reconciliation, Drive backup transport/list/restore/retention controls, and bounded retry behavior.
- Hardened archive extraction with size/count limits, duplicate-ID/relationship validation, per-account operation serialization, and a pre-restore safety backup.
- Added database, key, Drive, phone QA, security, and release runbooks.
- Full Android and Web local validation passed; no external or production action occurred.

## 2026-07-18 - Offline backup archive foundation
- Added the encrypted, account-bound `.tijario` logical archive codec.
- Added authenticated encryption, per-entry checksums, required document data, and path safety validation.
- Added focused JVM security and round-trip tests; no external or production action occurred.

## 2026-07-18 - Local-First deletion and restoration
- Added active-row customer/product limit enforcement for Local-Drive accounts.
- Preserved deleted customer, product, and document rows with deletion history.
- Added repository restoration without document-credit consumption.
- Added focused behavioral tests; no production or remote action occurred.

## 2026-07-18 - Local-First data-mode routing
- Persisted account data mode and entitlement limits from the control-plane usage response.
- Routed Local-Drive operational CRUD and numbering to Room while preserving legacy-cloud behavior by default.
- Prevented normal logout from deleting operational Room data.
- Added focused JVM coverage for mode parsing, Room-only writes, quota-event creation, and session clearing. Full JVM tests and debug/instrumentation compilation passed; lint timed out after three minutes.
- No push, deploy, migration apply, external-console change, or Play upload.

## 2026-07-18 - Cache policy remediation and Local-First architecture planning
- Centralized Android remote-cache replacement decisions through `RemoteCacheReplacementPolicy.shouldReplace(...)` in bootstrap and pull-sync ingestion paths.
- Preserved local unsynced, conflict, blocked, and terminal non-retryable states from remote overwrite.
- Added focused JVM coverage for policy behavior and repository refresh usage.
- Added planning-only Local-First, quota V2, encrypted backup, Google Drive backup, migration/rollout, data ownership, and implementation phase documents.
- Validation passed: targeted repository tests, `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, and `assembleRelease` when run separately.
- Safety Info: Local commits only. No push, GitHub API action, PR update, backend edit, deployment, Supabase migration, external console change, APK/AAB upload, or Google Play action.

## 2026-07-13 - Session 10 (Codex)
- Fixed Android document edit/export behavior on `fix/android-document-edit-pdf-email-numbering`.
- New-document draft numbers now use the Web next-number API instead of local cached numbering; saved document numbers remain exact across preview/reopen/PDF/share.
- PDF download now uses Android `DownloadManager` with bearer-authenticated `/api/mobile/documents/{id}/pdf` and waits for completion before showing saved success.
- Email export now filters to email apps via `mailto:` query targets and uses a `FileProvider` PDF attachment.
- Preserved custom document titles across language changes and added focused form/number tests.
- Validation passed: `compileDebugKotlin`, `testDebugUnitTest`, `lintDebug --no-daemon` on retry, `assembleDebug`, `:app:processReleaseMainManifest`, `assembleDebugAndroidTest`, and `git diff --check`.
- Safety Info: No commit, push, Web edit, deployment, Supabase migration apply, APK/AAB upload, version bump, or Google Play action.

## 2026-07-13
- **Agent**: Codex
- **Action**: Completed Android final source-control closeout preparation for the adaptive UI and document workflow batch.
- **Details**:
  - Verified Room schema versions `13` and `14`.
  - Added missing Room migration test coverage for `13 -> 14` local document metadata fields.
  - Re-ran the full Android validation sequence requested for commit readiness.
- **Validation**: `compileDebugKotlin`, `testDebugUnitTest`, `lintDebug --no-daemon`, `assembleDebug`, `:app:processReleaseMainManifest`, `assembleDebugAndroidTest`, and `git diff --check` passed.
- **Safety Info**: No Vercel deployment, Supabase migration, APK/AAB upload, or Google Play action.

## 2026-07-13
- **Agent**: Codex
- **Action**: Implemented Android document info and export action follow-up fixes.
- **Details**:
  - Added a calendar picker to the creation-date field while preserving current-date defaulting for new documents.
  - Generated draft invoice/quote numbers locally from cached documents with `INV-00001` / `Q-00001` style formatting.
  - Added quote-specific document title localization.
  - Changed the tax option icon to a government-building icon.
  - Added print and email to the document long-press action sheet and made non-delete icons theme foreground colored.
  - Deferred shipping, percentage discount, and multi-select document options because they require persisted server/database support.
- **Validation**: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check` passed. `lintDebug` was attempted twice and timed out before producing a pass/fail result.
- **Safety Info**: Local Android changes only. No commit, push, deployment, migration, APK/AAB upload, or Google Play action.

## 2026-07-13
- **Agent**: Codex
- **Action**: Implemented Android document form UI follow-up fixes.
- **Details**:
  - Quote document info now uses quote-specific title and number labels.
  - Document number editing keeps `INV-` / `Q-` fixed and lets users edit only the numeric suffix.
  - Draft numbers display `INV-...` or `Q-...` until the server number is available.
  - Due date field includes a calendar picker and keeps numeric keyboard editing.
  - Dashboard latest-document tabs center labels.
  - Documents, Customers, and Products FABs are positioned above the root bottom tab bar.
- **Validation**: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check` passed.
- **Safety Info**: Local Android changes only. No commit, push, deployment, migration, APK/AAB upload, or Google Play action.

## 2026-07-13
- **Agent**: Codex
- **Action**: Implemented Android UI cleanup for App Settings, numeric inputs, Dashboard, AI light mode, store phone country handling, and create-quote preview.
- **Details**:
  - Removed the extra App Settings title icon and language/theme divider.
  - Reworked notification settings into an aligned single row.
  - Applied numeric keyboards to amount, price, quantity, stock, and tax-rate fields.
  - Fixed Store Settings phone dialog title and synced phone country-code changes to country.
  - Fixed AI light-mode palette detection.
  - Removed global bottom padding above root bottom tabs.
  - Added invoice/quote tabs to Dashboard latest documents and routed "view all" to the matching Documents tab.
  - Replaced misleading draft document number fallback with `...` and current-date fallback.
  - Added missing UI localization keys.
- **Validation**: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, `lintDebug`, and `git diff --check` passed.
- **Safety Info**: Local Android changes only. No commit, push, deployment, migration, APK/AAB upload, or Google Play action.

## 2026-07-12
- **Agent**: Codex
- **Action**: Implemented Android UI polish fixes for the reported app screens.
- **Details**:
  - Localized customer picker and PDF download labels.
  - Removed the Store Settings verified-store badge.
  - Added plus badges to non-AI quick actions.
  - Changed notification/settings icons to theme-aware foreground colors.
  - Restored country-code phone fields in customer, store settings, and onboarding flows.
  - Improved Customer/Product card hierarchy and full-price display.
  - Moved the AI online-only note under the AI title.
  - Prevented create-quote UI from showing a fixed first number while waiting for next-number resolution.
  - Placed discount and extra-fee reason fields beside their amount fields.
  - Aligned app background and selected bottom navigation accent with the AI screen palette.
- **Validation**: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, `lintDebug`, and `git diff --check` pass.
- **Safety Info**: Local Android changes only. No commit, push, deployment, migration, APK/AAB upload, or Google Play action.

## 2026-07-12
- **Agent**: Antigravity (Google DeepMind)
- **Action**: Implemented Batch A (Document Numbering Safety & Idempotency P0):
  - Fixed sync push API (`push/route.ts`) to validate and forward `operation_id` to Supabase `create_document_with_usage` RPC.
  - Implemented client-side deterministic fallback UUID for legacy or invalid operations.
  - Enforced server-side `operation_id` validation as UUID format in Next.js Server Actions.
  - Fixed Android `DocumentFormStateSaver` to persist and restore `operationId` across process recreation and screen rotations.
  - Added Javascript regex-based tests for numbering constraints and Android Kotlin unit tests.
  - Web UI improvements and Android Adaptive UI changes remain pending (audit completed in Session 1).
- **Safety Info**: Local modifications only. No push, deploy, migrations applied, or Play Store uploads.

## 2026-07-10
- **Agent**: Antigravity / Gemini
- **Action**: Finalized release compliance for Android:
  - Enabled Facebook Advertiser ID collection (`AdvertiserIDCollectionEnabled = true`) inside `AndroidManifest.xml`.
  - Processed and verified merged release manifest for AD_ID and Meta configs.
  - Created Android permissions audit, Play Store Data Safety draft, Facebook compliance doc, and Closed Testing release checklist.
  - **Safety Info**: Local modifications only. No git pushes, APK/AAB uploads, or Google Play store changes.

## 2026-07-10
- **Agent**: Codex
- **Action**: Implemented Android QA batch for conditional product stock, live invoice stock validation, onboarding dial-code normalization, recent-document ordering, and auth safe-area fixes.
- **Details**:
  - `ProductKind.Product` requires positive stock; `Service` stock remains optional.
  - Invoice item editing and submit block saved-product quantities above available stock, excluding services and manual lines.
  - Invoice edit validation now adds original quantities from the same loaded invoice and aggregates duplicate product rows.
  - Onboarding now composes WhatsApp from an MVP dial-code selector and local phone input.
  - Recent document ordering now prefers sync/update timestamps over issue date.
  - Auth/onboarding layouts use status/navigation/IME padding and constrained card widths.
  - Onboarding language toggle now lives inside scroll content instead of overlaying fields.
  - Added focused tests for validation, phone normalization, invoice stock rules, and document ordering source.
- **Validation**: `testDebugUnitTest` and `assembleDebug` pass.
- **Safety Info**: Local Android changes only. No push, Web source edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-10
- **Agent**: Codex
- **Action**: Implemented Android-only document QA hotfixes for selected-customer identity, document language selection, document save error presentation, and default title behavior.
- **Details**:
  - `DocumentCustomerInput` now serializes the selected customer `id`.
  - `DocumentFormScreen` sends `form.customerId` and keeps customer identity based on `customer.id`, not WhatsApp.
  - Local fallback `createDocumentLocal()` reuses the requested customer id and does not enqueue duplicate customer creation.
  - Document language now uses a `ModalBottomSheet` selector.
  - Document validation/server errors show through `SnackbarHost` instead of inline payment-card text.
  - Default invoice/quote titles follow `documentLanguage`; custom titles are preserved.
- **Validation**: `testDebugUnitTest` and `assembleDebug` pass.
- **Safety Info**: Local Android changes only. No push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-09
- **Agent**: Codex
- **Action**: Implemented Android parity for optional business contact fields, persisted document language, server-authoritative inventory errors/cache refresh, responsive document actions, focused notes scrolling, and aligned document headers.
- **Storage**: Added Room migration `11 -> 12` for business fields and `document_language`.
- **Validation**: Kotlin compilation, Android unit tests, and `assembleDebug` pass.
- **Safety Info**: Local modifications only. No commit, push, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-08
- **Agent**: Antigravity / Gemini
- **Action**: Created permanent AI handoff protocol system (docs/ai/* and root instruction files).
- **Repos Inspected**: Web (`tjario`) and Android (`tjario-android`).
- **Safety Info**: No push/deploy/migration/upload was executed. Source behavior was not modified.

## 2026-07-08
- **Agent**: Antigravity / Gemini
- **Action**: Diagnosed and implemented local fixes for 4 reported issues:
  1. Partial Paid Amount: Added input form fields, live previews, and invoice template support for Paid Amount and Balance Due.
  2. Dashboard Metrics Update: Added `revalidatePath` on document mutations.
  3. Template Localization: Added `upgrade_required` localization strings mapping in the Android app.
  4. Standalone Customers Sync: Added immediate sync triggers on customer/product database operations in `TijarioRepository.kt`.
- **Safety Info**: Local modifications only. No git pushes, Vercel deployments, Supabase migrations applied, or Play Console uploads.
- **Action**: Refactored Android MVP write model to Online-Only writes + cached reads (no offline CRUD queue, direct Supabase writes).
- **Action**: Performed a strict final audit and local validation check of the uncommitted QA batch (63 files changed total). Verified builds and test suites successfully.
- **Action**: Created the complete production migration and release runbook (docs/release/production-migration-runbook.md) outlining safety checklists, rollback strategies, and sequential migration scripts order.

## 2026-07-12 - Session 1 (Codex)
- Added shared container-width layout classes and adaptive root navigation/dashboard/list/form/settings behavior.
- Persisted document `created_at` in Room v13 and used it only for Dashboard latest-document ordering, with legacy fallback.
- Unified E.164 phone entry, added logout confirmation, and hardened linked-customer deletion feedback.
- Added focused unit tests and a Room 12-to-13 migration test; all local validation passed.

## 2026-07-12 - Session 2 (Antigravity)
- Added unified long-press `ModalBottomSheet` action sheets for Documents (Share PDF, Download PDF, Edit, Delete).
- Removed redundant "الأحدث" filter chip and changed document tab labels to Arabic.
- Fixed list-FAB overlap by adding `contentPadding` to Document, Customer, and Product lists.
- Added Delete Customer action inside the Customer Edit screen with link-checking protection.
- Fixed Web Next-Number API ESLint warning.

## 2026-07-12 - Session 3 (Codex)
- Completed Android UI recovery and interface refinement locally on `fix/android-adaptive-ui-qa-v1-1-2`.
- Integrated `newestDocuments(...)` into Dashboard and Documents ordering.
- Restored adaptive Dashboard quick actions, improved Customer/Product/Document cards, and added localized long-press action sheets.
- Added in-form searchable Customer/Product bottom sheets for document creation/editing.
- Adjusted top/bottom navigation icon colors and added focused localization coverage.
- Validation passed: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `:app:processReleaseMainManifest`, and `assembleDebugAndroidTest`.
- Safety Info: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-13 - Session 8 (Codex)
- Implemented Android local document options for shipping, percentage/flat discount, bottom-sheet option editing, and multi-select taxes/payment methods/terms.
- Updated document calculations so tax is calculated after discount and extra fees, while shipping is added after tax.
- Persisted local option metadata in Room v14 and rendered shipping/negative discount/multi-line options in preview/PDF/saved documents.
- Added focused document calculation/render tests.
- Validation passed: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check`; `lintDebug` timed out after 4 minutes.
- Safety Info: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-13 - Session 11 (Codex)
- Added Android PDF download fallback: try authenticated DownloadManager first, then authenticated in-app PDF fetch written to Downloads through MediaStore.
- Switched new invoice/quote draft number display back to immediate local five-digit `INV-` / `Q-` numbering from cached documents and kept exact zero padding in the info dialog.
- Added `document_number` to Android mobile document save payload; Web/API may still need a separate change to honor it authoritatively.
- Preserved edited document title/language in local cache after create/update so reopened preview/PDF does not revert to the previous title.
- Stored the latest selected local tax and reused it automatically for future new documents.
- Added focused local numbering unit coverage.
- Validation passed: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check`; `lintDebug` timed out twice without a final result.
- Safety Info: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-13 - Session 12 (Codex)
- Reverted Android PDF download back to the locally generated app document PDF and changed the save destination to the public Downloads collection/directory.
- Preserved manually edited document numbers in the local complete-document cache after save/update.
- Added local quick-select presets for discount and extra-fee amount/reason pairs inside the existing bottom sheets.
- Validation passed: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check`.
- Safety Info: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-13 - Session 13 (Codex)
- Tightened PDF download to use the local in-app PDF and save it through the public Downloads MediaStore collection/directory.
- Applied manual document number/title overrides to the Documents list, document detail, preview, and export model.
- Changed Room document ordering to newest `created_at` first with `issue_date` and `synced_at` fallbacks.
- Validation passed: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check`.
- Safety Info: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.

## 2026-07-13 - Session 14 (Codex)
- Fixed legacy Android public Downloads saving by adding API 28-and-below write permission and runtime permission requests before local PDF save.
- Added focused source coverage for the legacy permission contract.
- Validation passed: `compileDebugKotlin`, `testDebugUnitTest`, and `assembleDebug`.
- Safety Info: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.
# 2026-07-18 - Local-First Room foundation
- Added Room schema 16 with immutable document creation events and Local-First entitlement, device, backup, and deletion metadata.
- Migrated legacy usage ledger rows and changed successful sync from event deletion to acknowledgement.
- Added migration compilation and JVM idempotency coverage.
- No push, deploy, migration apply, external-console change, or Play upload.

## 2026-07-18 - Historical document snapshots
- Upgraded Room to schema 17 with customer snapshot, deletion timestamp, and PDF generation-state columns on cached documents.
- Local document rendering now prefers the immutable customer snapshot over the mutable customer directory record.
- Added JVM snapshot coverage and compiled the Room migration instrumentation suite.
- No push, deploy, migration apply, external-console change, or Play upload.

## 2026-07-18 - Logical Room backup and restore
- Added deterministic typed-JSON export for all approved account-scoped Room tables.
- Added preflight account/schema validation and transactionally atomic replacement of only the restored account's rows.
- Added JVM coverage for typed round-trip, cross-account rejection, malformed rows, and required logical inventory.
- No push, deploy, migration apply, external-console change, or Play upload.

## 2026-07-18 - Offline local backup files
- Added safe collection of existing account logo, product images, and document PDFs with missing/failed PDF accounting.
- Added temporary-file creation, archive revalidation, fsync, checksum, and atomic finalization for private local `.tijario` backups.
- Added local backup/file history persistence and focused JVM tests.
- No push, deploy, migration apply, external-console change, or Play upload.

## 2026-07-18 - Restore quota authority
- Changed restore policy so operational records are replaced but immutable document creation events are only merged without overwrite.
- Preserved current entitlement, device-binding, and offline-lease authority instead of reactivating stale backup state.
- Added focused policy coverage; no remote or production action occurred.

## 2026-07-18 - Staged backup asset restore
- Added account-bound PDF/product-image/logo staging with document/product identity validation.
- Added rollback copies so asset failures or Room restore failures preserve previous local files.
- Added focused apply, rollback, completion, and unknown-identity tests; no remote action occurred.

## 2026-07-18 - Device-wrapped backup keys
- Added Android Keystore RSA-OAEP key generation and authenticated backend key-envelope contract DTO/client.
- Cached only device-wrapped key material by version and resolved the exact archive key version during restore.
- Added a coordinator that zeroes transient account keys after backup operations; no secret or external configuration was added.
# 2026-07-18 - Encrypted backup SAF UI
- Added a localized settings entry for encrypted local backup and restore.
- Added native SAF export/import with restore confirmation, bounded archive reads, and safe user-facing outcomes.
- Verified focused JVM contracts, Kotlin compilation, and Android test APK assembly locally.
# 2026-07-18 - Offline backup PDF preparation
- Regenerate missing/stale document PDFs locally before encrypted archive creation.
- Disable remote logo fetching for backup PDF rendering and isolate per-document failures.
- Persist generated PDF revision/hash/status so backup manifests reflect actual files.
# 2026-07-18 - Local backup scheduling
- Added manual/daily/weekly local backup policies backed by unique WorkManager jobs.
- Applied optional charging constraints without requiring internet or adding retry loops.
- Added frequency-specific local retention and behavior tests.
# 2026-07-18 - Local-First full validation
- Passed the complete local Android JVM, instrumentation-compilation, lint, debug, and release assembly baseline.
- Recorded external device, backend rollout, and Google Drive blockers without claiming runtime completion.

# 2026-07-23 - Backup control-plane fail-closed hardening
- Blocked operational customer, product, document, and business-setting writes until entitlement initialization succeeds.
- Bound Android verification to signed offline/backup policy values and rejected malformed logical relationship schemas.

# 2026-07-23 - Streaming backup restore
- Replaced SAF and Drive restore byte-array loading with bounded account-scoped temporary archive files.
- Streamed encrypted archive decryption and staged binary assets to files while preserving the existing V1 format and transactional restore path.

# 2026-07-23 - Visible phone backups and secure sharing
- Added scoped MediaStore phone copies, persisted SAF backup-folder selection for Android 8-9, and history restore/share controls.
- Added FileProvider content-URI sharing and Telegram preference with sharesheet fallback; no broad storage permission was added.

# 2026-07-23 - Drive connection state foundation
- Added account-scoped non-secret Drive connection metadata and explicit authorization states. Tokens remain transient and are not stored.

# 2026-07-23 - Google Drive authorization and streamed REST runtime
- Added Google Identity AuthorizationClient handling for `drive.file`, Activity Result resolution, account change/disconnect, session-only OAuth tokens, and process-safe reauthorization states.
- Added Ktor Drive v3 file streaming, folder/list/upload/download/delete paths, runtime reconstruction for Workers, and backup progress notification contracts.

# 2026-07-23 - Plan-aware backup settings
- Restricted backup frequency controls and retained archive counts to valid locally verified entitlement claims.
- Downgrades clamp invalid schedules; upgrades retain a user's less-frequent choice. Missing or expired claims fail closed to manual backups.

# 2026-07-23 - Fail-closed operational entitlement guard
- Blocked operational writes unless the persisted entitlement has an explicit trusted data mode and future expiry.

# 2026-07-24 - Google Drive post-consent identity resolution
- Replaced the invalid Google profile-ID requirement with Drive `about.user.permissionId` resolution after valid `drive.file` consent.
- Added safe Drive error classification, localized configuration feedback, and focused JVM coverage; code `15` / name `1.1.5` is prepared for review.

# 2026-07-25 - 1.1.5 onboarding and backup-key typed failures
- Account initialization is single-flight per user and installation, verifies and persists the signed entitlement before LocalDrive onboarding saves are enabled, and reports typed initialization failures.
- LocalDrive business settings now use one local Room transaction with `LOCAL_ONLY` state and no legacy cloud outbox.
- Backup-key envelope failures preserve primary-device, unavailable-key, invalid-device-key, server configuration, unauthenticated, and true offline/no-cache outcomes without exposing secrets.

# 2026-07-25 - Local playQa physical-device build
- Added a debuggable `playQa` variant that keeps the production package, version, endpoint BuildConfig, and release/upload signing configuration for local USB QA.
- Added the Android Studio workflow and the required Upload Key Android OAuth client documentation. No external Google Cloud change was made.

# 2026-07-25 - Physical QA findings recorded
- Recorded open Google sign-in, plan refresh/catalog, purchase synchronization, and document deletion findings without speculative fixes.

# 2026-07-25 - Multi-installation LocalDrive readiness (uncommitted)
- Removed the LocalDrive document-create scheduler trigger and prevented direct operational outbox enqueueing from local quota finalization.
- Kept Room transactions authoritative for local create/update and returned typed initialization/lease failures instead of raw local exceptions.
- Removed Business from new Google Play product queries and normalized legacy Business display data to Pro.

# 2026-07-26 - Removed legacy primary-device entitlement fields
- Removed `max_primary_devices` and unreachable device-conflict UI from the signed entitlement path; retained local historical binding storage.

# 2026-07-26 - Published multi-installation Android branch
- Pushed `df99d4b2f8966f166ef14a8ec19c5e0c32c7289e` to `codex/backup-drive-production-ready`; no Play upload or external action occurred.

# 2026-07-26 - LocalDrive document save follow-up (uncommitted)
- Made LocalDrive document saves bypass operational outbox calls, remain cache-only for usage refresh, and expose typed entitlement, lease, and document-limit feedback.

# 2026-07-26 - LocalDrive forced entitlement bootstrap repair (uncommitted)
- Forced startup and retry refreshes no longer return `ENTITLEMENT_INITIALIZATION_REQUIRED` from an empty local cache. Non-forced LocalDrive refreshes remain cache-only after bootstrap.

# 2026-07-26 - Signed entitlement JSON compatibility (uncommitted)
- Removed the redundant canonical JSON text-equality gate. Android now verifies the server-signed payload bytes directly while retaining JSON parsing, RSA verification, key ID, user, installation, and expiry validation.

# 2026-07-26 - Onboarding no longer depends on backup-key availability (uncommitted)
- Deferred optional backup-key envelope preparation after a valid LocalDrive entitlement bootstrap; account setup and business-settings onboarding no longer fail solely because backup encryption is unavailable.

# 2026-07-26 - Android Keystore local backup repair (uncommitted)
- Replaced caller-supplied AES-GCM IV generation with Android Keystore-generated IVs while sealing per-installation RSA private material.
- Preserved RSA-OAEP-256 account-key envelopes, no-primary multi-installation behavior, and safe stage-only diagnostics. A connected Android 13 device created a local encrypted backup successfully.

# 2026-07-26 - Google Drive authorization diagnosis (uncommitted)
- Added safe Google Identity authorization failure diagnostics. The connected QA device returned `INTERNAL_ERROR` while completing Google Drive consent, before any Drive REST request or folder resolution.
- Device QA verified the shown Android OAuth client matches package `app.tijario` and the local `playQa` APK SHA-1. The remaining external checks are the Drive scope/Audience configuration and Google Identity service behavior. No external setting was changed.

# 2026-07-27 - Local document render parity and settings mirror (uncommitted)
- Saved document detail and list export now resolve the document's persisted template ID, so the local Room-backed document uses the same template and item model for preview and PDF output.
- LocalDrive business settings remain Room-authoritative and mirror to `business_settings` only after a successful local save. A per-account payload fingerprint prevents duplicate unchanged writes; no outbox, scheduler, retry loop, or error log is added.

# 2026-07-27 - Document preview memory and local presentation recovery (uncommitted)
- Replaced scaled full-A4 WebView preview surfaces with viewport-sized WebViews to avoid Chromium tile-memory exhaustion and blank saved-document previews.
- Local preview/PDF logo resolution now uses cached logo data or initials, never an unresolved remote URL.
- Added empty customer/product picker create actions, dial-code-only display for customer forms, and English local-backup folder path display.

# 2026-07-27 - Local document quota and automatic backup recovery (uncommitted)
- LocalDrive document creation now enforces the verified plan limit rather than treating an exhausted temporary offline-lease batch as the account limit; lease-less local events remain pending for later reconciliation.
- Saved document detail keeps one persisted-template WebView and avoids identical HTML reloads to prevent Chromium surface exhaustion after save.
- Automatic local backups target `Downloads/Tijario/Backup` through MediaStore without opening the export file chooser, with a documented provider fallback when nested MediaStore paths are refused.

# 2026-07-27 - Local PDF layout and compact document/settings UI (uncommitted)
- Local PDF export now lays out the `WebView` before HTML load and before visual-state capture, then invalidates old `pdfv3` cached exports with `pdfv4`; this addresses blank saved PDFs caused by drawing an unmeasured WebView.
- Document detail actions remain reachable on narrow screens, invoice/quote empty states remain independent, customer dial-code selection persists, and settings use compact title-only rows with a smaller plan card.

# 2026-07-27 - Vector local PDF export (uncommitted)
- Replaced the raster `WebView.draw(canvas)` export with the native `WebView.createPrintDocumentAdapter` write path. New `pdfv5` cache entries prevent blurred `pdfv4` exports from being reused.
- Full local gates subsequently passed: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assemblePlayQa`, and signed `bundleRelease`.

# 2026-07-27 - Authorized branch publication
- Pushed the validated Android branch to `origin/codex/backup-drive-production-ready`; no Play upload or deployment occurred.

# 2026-07-29 - Reconciliable LocalDrive quota and account deletion
- LocalDrive creates reserve a validated lease credit under a mutex and persist the document, items, customer changes, and creation event in one Room transaction.
- Legacy lease-less events are assigned a safe lease before reconciliation or explicitly blocked on a terminal quota result.
- Account deletion now retains local data until the unified server deletion endpoint confirms success, then cancels backup, sync, and notification jobs.

# 2026-07-29 - Release-blocker contract correction
- Lease consumption now occurs exactly once with the acknowledgement transaction, legacy lease-less events retain pending state when capacity is unavailable, and failed local account cleanup remains retryable after server confirmation.
- Focused JVM tests and `assemblePlayQa` passed locally. Version remains `15` / `1.1.5`; branch publication is authorized, with no deploy, migration, or Play action.

# 2026-07-29 - Lease retry and deletion startup recovery
- Reconciliation now releases only stale pending lease assignments for the three retryable lease errors; unknown failures remain pending and permanent invalid payload failures reject.
- Added local-only startup recovery for a pending account-deletion cleanup marker, including a no-network auth-session clear after successful cleanup.
- Focused quota/account-deletion JVM tests and `assemblePlayQa` passed. No external action occurred.

# 2026-07-29 - Backup destinations and restore integrity
- Split explicit Phone and Google Drive backup actions. Phone backup honors SAF selection first and otherwise targets only `Downloads/Tijario/Backup`; failures are typed instead of silently falling back.
- Added restore-picker initial location, transfer progress callbacks, strict Drive metadata verification, and typed restore/safety-snapshot errors. Room backup metadata follows the current database version.
- Raised the Android release version to `16` / `1.1.6`. No migration, deployment, external configuration change, or Play upload occurred.

# 2026-07-29 - WorkManager backup progress and restoration
- Replaced the manual Drive upload success shortcut with observed WorkManager state. Manual retries now run even when automatic Drive upload is off, while retaining network and charging policy for automatic work.
- Drive, SAF-file, and local-record restore paths run in a cancellable foreground worker and report explicit download, validation, safety backup, file restore, and Room restore stages. The selected SAF folder name is persisted and displayed.
- Focused Backup/Drive/Notification/Offline JVM tests, `lintDebug`, and `assemblePlayQa` passed. Physical Drive, notification, cancel, and SAF restore QA remain pending; no external action occurred.

# 2026-07-29 - Restore permission and key-recovery hardening
- SAF-file restore persists only a temporary read grant through worker staging, reports a typed localized lost-permission error, and releases the grant after the encrypted archive is staged privately.
- All restore sources can obtain the exact missing backup-key envelope when online, retain fail-closed offline behavior, and use the existing `dataSync` foreground-service type. Restore validates before creating a safety snapshot.
- Added JVM coverage for typed restore outcomes and an Android Room/assets/FakeDrive round-trip integration test. JVM, Android-test APK assembly, lint, and Play QA assembly passed; integration execution remains device-gated.

# 2026-07-31 - Restore validation correction
- Restores no longer reject immutable creation events, historical customer references, or detached local metadata merely because the original document/customer was deleted; only Room's document-item relationship remains mandatory.
- Restore safety snapshots remain internal and no longer appear in normal backup history. A genuinely invalid item/document relationship maps to the specific localized foreign-key restore error.
- Focused JVM tests and `assemblePlayQa` passed; release metadata was raised to `18` / `1.1.8` and physical restore retest is pending.

# 2026-07-31 - Atomic backup snapshots and accurate operation history
- Backup creation now reads all Room tables in one transaction and validates bounded structure before publishing an archive; foreign-key integrity remains enforced inside the atomic Room restore.
- Added Room schema 19 with `backup_records.restored_at`. Successful restores update the source archive by ID/checksum, while history hides local preparation, pending/uploading/failure, and internal safety states.
- Restore safety snapshots are hidden from their initial insert and removed after success. User-initiated Drive uploads are expedited without background battery/storage gates. Backup JVM tests, Android-test assembly, and `playQa` assembly passed; no device or external action was used.

# 2026-08-01 - First-attempt Google Drive upload verification
- Resumable upload creation now requests the complete Drive File response needed for size, checksum, account, and backup identity verification.
- A partial create response is resolved through the exact remote backup query; delayed metadata visibility remains a bounded automatic retry instead of requiring the user to press Retry.
- All 112 backup JVM tests and `assemblePlayQa` passed. No external action occurred.

# 2026-08-09 - Authentication deep-link hardening
- Added a single callback policy for legacy custom schemes and verified HTTPS App Links. It accepts only exact Tijario callback origins and falls back to `/login` for unsafe `next` values, preventing open redirects and arbitrary scheme handling.
- Declared `https://tijario.site/auth/callback` and `https://www.tijario.site/auth/callback` with Android App Link verification and documented the external certificate/hosting validation required before release.
- Focused `AuthDeepLinkPolicyTest` passed. No hosting, Play, deployment, migration, or external configuration change occurred.

# 2026-08-09 - Network timeout and safe diagnostic hardening
- Interactive backend calls retain bounded 15/45-second connect/request/socket limits, while streamed Google Drive transfers use bounded 30-second connect and five-minute request/socket limits.
- Document API diagnostics now log path, status, code, and document-ID presence only. Auth, analytics, Drive, and export diagnostics avoid raw server messages and throwable stack traces.
- Focused network-profile and Drive transport tests passed. No endpoint, retry policy, external setting, or production action changed.

# 2026-08-09 - Document render input and decimal-boundary hardening
- Payment status now maps to a fixed CSS class allowlist, and inline logo data accepts only base64 PNG/JPEG/WebP images. This prevents stored values from influencing generated HTML attributes.
- Draft render mapping preserves prices, tax rates, and paid amounts as `BigDecimal` through preview calculation instead of converting them through `Double`.
- Full `DocumentEngineTests` passed after adding injection and exact-decimal regression coverage.

# 2026-08-09 - Billing authority regression guard
- Added coverage verifying that Google Play purchase acknowledgement occurs only after the backend verification response succeeds and before a verified purchase event is emitted.
- The same guard asserts the Android billing repository contains no logging call or token-bearing diagnostic. No billing behavior or external configuration changed.

# 2026-08-09 - Safe refresh-error localization
- Repository refresh failures no longer place raw exception messages into the UI state. Known backend codes remain mapped through `LocalizedErrorMapper`; unknown text becomes the localized safe fallback.
- Added Arabic and English regression coverage for unknown technical text and `DOCUMENT_LIMIT_REACHED`.

# 2026-08-09 - Closed analytics event surface
- `TijarioAnalytics` now accepts only `TijarioAnalyticsEvent`; arbitrary event-name strings and optional Bundles are no longer public API.
- The central enum retains the existing operational events without collecting user, document, or purchase content. Focused JVM coverage verifies the closed typed interface and safe wire-name format.
