# AI Changelog

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
