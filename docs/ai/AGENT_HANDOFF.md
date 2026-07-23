# Agent Handoff (Android & Web Repos)

## 2026-07-23 (Google Drive authorization and runtime)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Added Google Identity `AuthorizationClient` flow for the minimal `drive.file` scope, Activity Result resolution, forced account selection, non-secret connected-account metadata, transient-only access tokens, account switching/disconnect, and worker-safe reauthorization handling. Added a Ktor Drive v3 transport that streams finalized encrypted archives for upload/download, a reconstructed process/Worker runtime, folder creation/reuse, Drive list/restore/delete UI, and a backup foreground notification channel.
- **Validation**: Focused Drive authorization, Ktor transport, schedule/worker, notification, and existing Drive client JVM tests passed.
- **External QA**: Google OAuth configuration and real-device authorization, Drive transfer, foreground notification, and account-switch behavior remain unverified.
- **Safety**: No push, deployment, migration apply, external-console action, or Google Play upload.

## 2026-07-23 (Plan-aware backup scheduling)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Backup settings now clamp manual/weekly/daily selection and retention to the valid persisted signed entitlement. Expired, missing, or malformed cached entitlement data permits manual backups only; a later upgrade preserves a user's manual or weekly preference.
- **Validation**: Focused policy tests, complete JVM tests, and `assembleDebugAndroidTest`/`assembleDebug` passed. `lintDebug` and `assembleRelease` exceeded the local 184-second execution window without a result and remain unverified.
- **Safety**: No push, deployment, migration apply, external-console action, or Google Play upload.

## 2026-07-18 (Local-First completion and hardening)
- **Agent**: Codex
- **Branches**: Android `codex/local-first-complete`; Web `codex/local-first-control-plane`.
- **Completed**: Signed account/device entitlements, lease-bound immutable document events, event reconciliation, Drive transport abstractions/UI/workers, bounded encrypted archive parsing, record/relationship validation, per-account backup mutex, and pre-restore safety backup.
- **Validation**: Android `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, `assembleRelease`, and `git diff --check` passed as separate commands. Web 81 tests, TypeScript, lint (0 errors, 21 existing warnings), production build, and `git diff --check` passed.
- **External blockers**: Three Local-First migrations are unapplied; production signing/envelope keys and Google Drive OAuth are unconfigured; SQL runtime and physical-device QA were not performed.
- **Safety**: Local commits only. No push, deploy, production migration, external-console change, emulator/device test, or Google Play upload.

## 2026-07-23 (Streaming backup restore)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: SAF and Drive restores now use an account-scoped bounded temporary archive file. The encrypted payload is decrypted as a stream, while PDF and other binary assets remain staged files rather than aggregate byte arrays. Existing V1 archive creation and byte-array compatibility tests remain supported.
- **Validation**: Focused archive/input/UI JVM tests passed with `testDebugUnitTest`.
- **Remaining**: Visible phone destination, secure sharing, Drive authorization/REST wiring, notifications, and physical-device SAF validation.
- **Safety**: No push, deployment, migration apply, external-console action, or Google Play upload.

## 2026-07-23 (Phone backup and sharing)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: A successful private backup now also attempts a visible phone copy through scoped MediaStore storage on Android 10+; Android 8-9 uses a persisted user-selected SAF folder. Backup history has confirmed restore/share actions. Shares use content URIs and read grants, with Telegram preferred only when installed.
- **Validation**: Focused phone-backup/UI JVM contract tests completed successfully.
- **External QA**: Device validation is still required for MediaStore, SAF folder persistence, and sharesheet/Telegram target behavior.

## 2026-07-18 (Local-First full validation)
- **Agent**: Codex
- **Branches**: Android `codex/local-first-complete`; backend `codex/local-first-control-plane`.
- **Validation**: Android `testDebugUnitTest assembleDebugAndroidTest lintDebug assembleDebug assembleRelease` completed successfully in 11m 2s. Backend focused key-envelope tests, TypeScript, lint, and Next build passed earlier on the control-plane branch.
- **Remaining Blockers**: No emulator/device is available for Room migration, Android Keystore, WebView PDF, SAF, WorkManager, or full restore runtime QA. Google Drive requires OAuth/console setup. Backend migrations, environment configuration, and deployment remain unapplied/unperformed.
- **Safety Status**: No push, deployment, production migration, external-console action, or Google Play upload.

## 2026-07-18 (Local backup scheduling)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Added manual/daily/weekly backup policy controls and unique per-account WorkManager scheduling.
- **Details**: Automatic local backup does not require network and never requests immediate retry. Optional charging constraints apply to automatic work only. Daily/weekly retention removes old private archives and metadata safely. Wi-Fi remains a future Drive-upload constraint, not a local-backup blocker.
- **Validation**: Focused schedule/retention tests, `compileDebugKotlin`, and `assembleDebugAndroidTest` passed.
- **Safety Status**: Local commits only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-18 (Offline backup PDF preparation)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Connected local PDF regeneration to backup preparation.
- **Details**: Valid current-revision PDFs are reused. Missing/stale PDFs render locally without network logo fetching, persist under the account directory, and update Room metadata. A failed document is marked failed and does not abort the complete structured backup.
- **Validation**: Focused JVM contracts, `compileDebugKotlin`, and `assembleDebugAndroidTest` passed. WebView PDF rendering still needs real device/emulator QA.
- **Safety Status**: Local commits only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-18 (Encrypted backup SAF UI)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Added a localized backup/restore settings screen backed by the encrypted local archive coordinator.
- **Details**: Users can create a private local backup, export a newly created `.tijario` archive through Android Storage Access Framework, or import one after explicit confirmation. Import size is capped before authenticated account/schema validation. No Drive SDK or external console setting was added.
- **Validation**: Focused backup UI/key/archive JVM tests, `compileDebugKotlin`, and `assembleDebugAndroidTest` passed. Real SAF, Keystore, and restore QA still require a device/emulator and a deployed key-envelope backend.
- **Safety Status**: Local commits only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-18 (Session - Offline backup archive foundation)
- Added a versioned `.tijario` logical archive codec using AES-GCM authenticated encryption.
- Added per-file SHA-256 inventory verification, account binding, mandatory structured document entries, duplicate/path traversal rejection, and fail-closed parsing.
- Added JVM tests for offline round-trip, tampering, wrong-account restore, missing structured records, and unsafe paths.
- This is the container/validation layer only; Room export, temporary-database restore, PDF preparation, key-envelope storage, UI, and Drive transport remain pending.
- No push, deploy, production migration, external-console change, or Play upload.

## 2026-07-18 (Session - Local-First deletion and restoration)
- Added active customer/product Room counts and enforced persisted Local-Drive limits before create or restore.
- Changed Local-Drive customer, product, and document deletion to soft-delete plus deletion-history records.
- Added repository restoration paths that reactivate rows and never create another document creation event.
- Preserved legacy-cloud deletion behavior.
- Focused JVM tests and instrumentation-test compilation passed; restoration UI and historical customer snapshots remain pending.
- No push, deploy, production migration, external-console change, or Play upload.

## 2026-07-18 (Session - Local-First data-mode routing)
- Added explicit `legacy_cloud`, `local_drive`, and reserved `cloud_sync_future` parsing with a safe legacy default.
- Persisted control-plane entitlement fields from account usage and used them for local quota scope and limits.
- Routed Local-Drive customer, product, document, and business-setting writes to Room without operational cloud CRUD or outbox entries.
- Made operational refresh/sync no-op in Local-Drive mode and kept complete-document reads local.
- Changed normal logout/session clearing to preserve account-local Room data.
- Focused and full JVM tests passed for safe mode defaults, local Room routing, immutable creation events, and non-destructive session clearing; instrumentation and debug APK compilation passed. `lintDebug` timed out after three minutes.
- Remaining: soft deletion/restoration, historical snapshots, active-count limit enforcement, backup/restore, Drive transport, migration UI, and backend control-plane contracts.
- No push, deploy, production migration, external-console change, or Play upload.

## 2026-07-18 (Session - Local-First Room foundation)
- **Agent**: Codex
- **Branches**: Android `codex/local-first-complete`; backend `codex/local-first-control-plane`.
- **Action**: Implemented Room schema 16 foundation and immutable document creation-event persistence.
- **Details**:
  - Migrates legacy `local_usage_ledger` rows into `document_creation_events` without losing acknowledged state.
  - Document sync now marks events `ACKNOWLEDGED` instead of deleting them.
  - Added persisted entitlement/data-mode, device binding, backup settings/records/file entries, and deleted-record history tables.
  - Added migration and repository idempotency coverage.
- **Validation**: Targeted JVM tests, full `testDebugUnitTest`, and `assembleDebugAndroidTest` passed. Runtime instrumentation is blocked because `adb` is unavailable.
- **Safety**: Local work only. No push, deploy, migration apply, GitHub API, external-console change, or Play upload.
- **Next**: Preserve Room data on normal logout and route CRUD by explicit data mode.

## 2026-07-18 (Session - Cache policy remediation and Local-First planning)
- **Agent**: Codex
- **Branch**:
  - Android: `codex/full-audit-cache-policy-docs`
  - Web/backend: inspected read-only at `C:\Users\BBOY AMG\Desktop\Projects\tjario`.
- **Action**: Completed the safe remote-cache replacement policy remediation and added Local-First/Google Drive architecture planning documents.
- **Details**:
  - `TijarioRepository` bootstrap and pull-sync remote-cache replacement checks now delegate to `RemoteCacheReplacementPolicy.shouldReplace(...)` for business settings, customers, products, and documents.
  - The shared policy preserves `LOCAL_ONLY`, `PENDING_SYNC`, `PENDING_DELETE`, `CONFLICT`, `BLOCKED_BY_PLAN`, and `failed_non_retryable` rows from remote overwrite.
  - Added JVM coverage for the shared policy and repository refresh behavior.
  - Added planning-only architecture docs for local-first data ownership, encrypted backup format, quota V2, Google Drive backup, rollout, and phase boundaries.
- **Validation**:
  - Targeted `RemoteCacheReplacementPolicyTest` and `TijarioRepositoryOfflineTests` passed.
  - `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, and `assembleRelease` passed when run separately.
  - One combined Gradle baseline command timed out locally and was not counted as passing.
- **Safety Status**: Local commits only. No push, GitHub API action, PR update, deployment, Supabase migration, external console change, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Emulator/device instrumentation remains externally blocked.
  - Backend/Supabase Local-First V2 contracts remain documentation-only.
  - Google Drive OAuth/API setup remains an external manual blocker.

## 2026-07-13 (Session 12 - Local PDF downloads and document option presets)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-document-edit-pdf-email-numbering`
  - Web: unchanged/read-only.
- **Action**: Implemented Android-only fixes for PDF download location, manual document-number persistence, and reusable discount/extra-fee options.
- **Details**:
  - PDF download now uses the locally generated in-app document PDF again, not the Web/server PDF, and saves to the public Downloads collection through MediaStore on Android 10+.
  - Pre-Android 10 fallback writes to the public Downloads directory instead of the app-specific `Android/data/.../Downloads` directory.
  - Manual `document_number` is sent in the mobile payload and also preserved in the local complete-document cache after save/update so reopened in-app preview/PDF shows the edited number.
  - Discount and extra-fee bottom sheets now save the last used amount/reason as local presets and show saved presets as quick-select chips.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain --no-daemon` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain --no-daemon` passed.
  - `.\gradlew.bat assembleDebug --console plain --no-daemon` passed.
  - `git diff --check` passed with only CRLF warnings.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, version bump, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Real-device QA for PDF visibility in the phone Downloads folder, manual number persistence after save/reopen, and discount/extra-fee preset selection.

## 2026-07-13 (Session 11 - PDF fallback, local numbering, title/tax defaults)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-document-edit-pdf-email-numbering`
  - Web: unchanged/read-only.
- **Action**: Implemented Android-only follow-up fixes for reported document PDF/title/number/tax-default issues.
- **Details**:
  - PDF download still tries the authenticated server PDF DownloadManager path first, then falls back to an in-app authenticated PDF fetch and writes the bytes to Downloads through MediaStore on Android 10+.
  - New invoice/quote draft numbers are calculated immediately from cached local documents with `INV-` / `Q-` prefixes and five-digit suffixes, starting at `00001`; the info dialog now updates when the form number changes and preserves edited zero padding.
  - Android now includes the visible `document_number` in the mobile create/update request payload; current Web create schema was inspected read-only and may still allocate/return the server number unless Web/API is updated to honor client-provided numbers.
  - After create/update, cached complete documents keep the request document title/language so edited Arabic/English titles do not revert in local preview/PDF/reopen flows.
  - The latest selected local tax name/rate is stored and applied automatically to future new documents.
  - Added focused unit coverage for local type-specific document numbering.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `git diff --check` passed with only CRLF warnings.
  - `.\gradlew.bat lintDebug --console plain` and `.\gradlew.bat lintDebug --console plain --no-daemon` both timed out before a pass/fail result.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, version bump, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Real-device QA for PDF download fallback, edit-title persistence, new invoice/quote number display, and automatic tax defaults.
  - Re-run `lintDebug` when Gradle lint is responsive.

## 2026-07-13 (Session 10 - Document edit/export fixes)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-document-edit-pdf-email-numbering`
  - Web: inspected read-only on `main`; no Web files changed.
- **Action**: Implemented Android-only fixes for document edit/export/number/title behavior.
- **Details**:
  - Document edit save path remains update-only when `editDocumentId` exists; Web audit confirmed `update_document_with_usage` does not update `usage_counters`.
  - New-document draft number now comes from `/api/mobile/documents/next-number` instead of cached local numbering; saved/reopened/PDF/share paths keep the server `document_number` exactly.
  - PDF download now uses Android `DownloadManager` against `/api/mobile/documents/{id}/pdf` with bearer auth and only shows saved success after DownloadManager reports completion.
  - Email export now filters to real email apps via `ACTION_SENDTO mailto:` handlers, attaches the PDF through `FileProvider`, and shows a localized no-email-app message.
  - Default document title updates only for blank/default titles; custom titles remain preserved.
  - Added focused tests for edit form product merge behavior, title preservation, and exact document-number display.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat lintDebug --console plain --no-daemon` passed on retry after an initial timeout.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `.\gradlew.bat :app:processReleaseMainManifest --console plain` passed.
  - `.\gradlew.bat assembleDebugAndroidTest --console plain` passed.
  - `git diff --check` passed with only CRLF warnings.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, version bump, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Real-device QA for editing invoices with existing items, adding a sixth item, DownloadManager PDF completion, and email-app chooser filtering.

## 2026-07-13 (Session 9 - Android final source-control closeout prep)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Completed final local validation prep for committing the accumulated Android adaptive UI and document workflow changes.
- **Details**:
  - Confirmed Room schema files `13.json` and `14.json` exist and are valid Room schema JSON versions.
  - Added focused `13 -> 14` Room migration coverage for local document metadata option fields.
  - Re-ran the full requested Android validation sequence after the test update.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat lintDebug --console plain --no-daemon` passed.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `.\gradlew.bat :app:processReleaseMainManifest --console plain` passed.
  - `.\gradlew.bat assembleDebugAndroidTest --console plain` passed.
  - `git diff --check` passed with only CRLF normalization warnings.
- **Safety Status**: No deployment, Supabase migration, APK/AAB upload, or Google Play action. Local `.agents/skills/` and `.agents/skill-backups/` remain untracked and excluded from the intended commit.
- **Remaining Risks**:
  - Device/emulator visual QA is still required for adaptive layouts, bottom sheets, and document preview/PDF output.
  - Android-local document options remain local-only until Web/API/Supabase persistence is explicitly added later.

## 2026-07-13 (Session 8 - Android local document options follow-up)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Implemented the requested Android-only local document option behavior.
- **Details**:
  - Added local shipping amount/reason to document form state, Room metadata, migration `13 -> 14`, draft/saved render mappers, and HTML/PDF totals.
  - Added fixed/percentage discount mode; Android calculates the final discount amount before sending document save/update and stores the original local discount value/type in metadata for reopen/edit.
  - Kept official local calculation order: subtotal - discount + extra fees, then tax, then shipping.
  - Discount now renders with a minus sign in preview/PDF/saved document output.
  - Converted discount, extra fees, shipping, currency, taxes, payment methods, terms, and signatures to bottom-sheet surfaces; signatures remain single-select because the renderer supports one signature image.
  - Local taxes, payment methods, and terms support multi-select and render as ordered line-separated text in the document.
  - Added focused document calculation/render tests for percentage discount, shipping, and negative discount output.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `git diff --check` passed with only existing CRLF warnings.
  - `.\gradlew.bat lintDebug --console plain` timed out after 4 minutes without a pass/fail result.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, APK/AAB upload, or Google Play action.
- **Remaining Risks**:
  - Device/emulator visual QA is still required for the bottom sheets and document preview/PDF.
  - Shipping and discount-type metadata are local-only; they will not restore after app data is cleared unless backend persistence is added later.

## 2026-07-13 (Session 7 - Document info and export action follow-up)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Implemented the safe Android-only subset of the latest document UI follow-up locally.
- **Details**:
  - Creation date in the document info dialog now has a calendar picker while keeping the current-date default for new documents.
  - New invoice/quote draft numbers are generated locally from cached app documents with fixed `INV-` / `Q-` prefixes and five-digit suffixes, starting at `00001`.
  - Quote document title field now uses a quote-specific localized label.
  - Tax option uses a government-building icon.
  - Documents long-press actions now include print and email alongside share/download/edit/delete, with normal icons following theme foreground color and delete remaining red.
  - Shipping, percentage discount, and multi-select payment/terms/tax options were not implemented as Android-only preview features because they need persisted Web/API/Supabase fields before they can survive save/reopen/PDF.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `git diff --check` passed with only existing CRLF warnings.
  - `.\gradlew.bat lintDebug --console plain` was attempted twice and timed out before producing a pass/fail result.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Device/emulator visual QA for creation-date picker, local next-number display, and document long-press export actions.
  - Web/API/Supabase contract work is required before shipping, discount type, and multi-select document options can be implemented without becoming preview-only.

## 2026-07-13 (Session 6 - Document form UI follow-up)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Implemented the requested Android document/UI follow-up locally.
- **Details**:
  - Document info dialog now receives the document type and shows Quote Info / quote number labels for quotes.
  - New document number editing keeps the `INV-` or `Q-` prefix fixed and editable input is limited to the numeric suffix.
  - Draft document cards/previews now show `INV-...` or `Q-...` until the server next-number response fills the full number.
  - Due date field keeps numeric editing and includes a calendar picker that writes a `yyyy-MM-dd` value.
  - Dashboard latest-document invoice/quote chips center their labels.
  - Floating add buttons on Documents, Customers, and Products are raised above the root bottom tab bar.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `git diff --check` passed with only existing CRLF warnings.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Device/emulator visual QA for document info dialog number editing, due-date picker, centered dashboard tabs, and FAB position above the tab bar.

## 2026-07-13 (Session 5 - UI issue cleanup)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Implemented the requested Android UI cleanup batch locally.
- **Details**:
  - App Settings header no longer shows the extra settings icon; the language/theme divider was removed.
  - Notifications in App Settings now renders as a normal settings row with aligned icon and switch, without description or system-settings button.
  - Numeric document/product fields now request numeric or decimal keyboards.
  - Store Settings phone edit dialog no longer uses the customer-edit title, and changing the store phone country code updates the store country selection.
  - Onboarding phone country-code changes update the selected country.
  - Tijario AI light-mode colors now follow the app dark-mode preference.
  - Root pager content no longer receives global bottom padding above the bottom tab bar.
  - Dashboard financial secondary metrics wrap on compact screens.
  - Dashboard latest documents now has invoice/quote tabs and opens the matching Documents tab from "view all".
  - Create-document preview no longer invents `INV-0001`/`Q-0001`; it shows `...` until the server next-number result is available, and date fallback uses the current date.
  - Added missing UI localization keys for invoice/quote dashboard tabs, PDF export success, and invalid document totals.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed after updating the draft preview test to expect `...`.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `.\gradlew.bat lintDebug --console plain` passed.
  - `git diff --check` passed with only existing CRLF warnings.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Device/emulator visual QA for the updated App Settings row alignment, Dashboard compact metrics, latest-document tabs, AI light mode, and bottom-tab spacing.

## 2026-07-12 (Session 4 - UI polish batch)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Implemented the requested Android UI polish batch locally.
- **Details**:
  - Added localized labels for customer selection and PDF download actions.
  - Removed the "verified store" badge from the Store Settings account card.
  - Added plus badges to non-AI quick action icons.
  - Changed notification and settings icons to theme-aware black/white tokens.
  - Restored phone country-code selectors in customer, store settings, and onboarding flows, including store-based defaults for customer creation.
  - Adjusted Customer and Product cards for clearer hierarchy and full-price rendering.
  - Moved the AI online-only note under the Tijario AI title and removed the offline-app sentence.
  - Prevented create-quote UI from showing a fixed first quote number while waiting for the server next-number response.
  - Reordered discount and extra-fee fields so each amount sits next to its reason.
  - Aligned app background and selected bottom navigation accent with the AI screen palette.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat assembleDebug --console plain` passed.
  - `.\gradlew.bat lintDebug --console plain` passed on retry after the first run timed out.
  - `git diff --check` passed with only existing CRLF warnings.
- **Safety Status**: Local Android changes only. No commit, push, deployment, Supabase migration, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Device/emulator visual QA for the updated light/dark theme, Arabic/English, customer/product cards, and document form flows.

## 2026-07-12 (Session 3 - Batch B)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: unchanged/read-only.
- **Action**: Implemented Batch B Android UI Recovery & Final Interface Refinement locally.
- **Details**:
  - Restored Dashboard adaptive padding, quick-action grid, and latest-document ordering through `newestDocuments`.
  - Updated Documents ordering and tabs to use localized labels and newest-document logic.
  - Moved customer/product actions to long-press bottom sheets while keeping normal click for edit/select behavior.
  - Added searchable in-form customer and product bottom sheets for document forms using local Room-backed state.
  - Adjusted top app bar and bottom navigation icon colors to theme `onSurface` tokens.
  - Added localization keys for recovered UI labels and a focused localization test.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain` passed.
  - `.\gradlew.bat lintDebug --console plain` passed on retry after first command timed out.
  - `.\gradlew.bat assembleDebug :app:processReleaseMainManifest assembleDebugAndroidTest --console plain` passed.
- **Safety Status**: Local changes only. No commit, push, deploy, Supabase migration, version bump, APK/AAB upload, or Google Play action.
- **Pending Tasks**:
  - Device/emulator visual QA for 280/320/360/412dp, Arabic/English, light/dark, and 130% font scale.
  - Apply/deploy pending Web/Supabase work only in the approved release order.

## 2026-07-12 (Session 2 - Batch A)
- **Agent**: Antigravity (Google DeepMind)
- **Branch**:
  - Android: `fix/android-adaptive-ui-qa-v1-1-2`
  - Web: `fix/document-number-allocation-collision`
- **Action**: Implemented Batch A — Document Numbering Safety & Idempotency P0.
- **Details**:
  - Web: Fixed sync push endpoint (`push/route.ts`) to validate and forward `operation_id` to the database RPC `create_document_with_usage`.
  - Web: Added `getDeterministicUuid` helper to ensure that missing or legacy operations receive a stable, deterministic UUID fallback.
  - Web: Enforced server-side `operation_id` validation as a UUID format inside Next.js Server Actions (`actions.ts`).
  - Web: Created `document-numbering-safety.test.mjs` containing 8 tests to assert the database and API behaviors.
  - Android: Fixed `DocumentFormStateSaver` in `FormScreens.kt` to save and restore `operationId` across process recreation or screen rotations.
  - Android: Added `DocumentNumberingIdempotencyTests.kt` verifying the stability of the UUID generation and model properties.
- **Safety Status**: Safe. No git pushes, Vercel deployments, Supabase production migrations applied, or Google Play uploads.
- **Pending Tasks**:
  - Applying database migrations.
  - UI Recovery for Phase 1.1 (Adaptive Dashboard, Cards, Product/Customer long-press sheets).

## 2026-07-13 (Session 13 - PDF downloads and document list identity)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-document-edit-pdf-email-numbering`
  - Web: unchanged/read-only.
- **Action**: Completed the Android follow-up for local PDF downloads, manual document number display, and newest document ordering.
- **Details**:
  - PDF download now writes the locally generated in-app document PDF to the public Downloads MediaStore collection/directory.
  - Documents list, document detail, preview, and export paths now use the locally saved manual document number/title override when present.
  - Room document list ordering now uses newest `created_at` first, with `issue_date` and `synced_at` as fallbacks.
  - Updated source coverage for the Room ordering query.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain --no-daemon` passed.
  - `.\gradlew.bat testDebugUnitTest --console plain --no-daemon` passed.
  - `.\gradlew.bat assembleDebug --console plain --no-daemon` passed.
  - `git diff --check` passed with existing CRLF warnings only.
- **Safety Status**: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.
- **Pending Tasks**:
  - Real-device QA for saving a PDF into the visible Downloads folder and verifying manual document number persistence after save/edit.

## 2026-07-13 (Session 14 - Legacy Downloads permission)
- **Agent**: Codex
- **Branch**:
  - Android: `fix/android-document-edit-pdf-email-numbering`
  - Web: unchanged/read-only.
- **Action**: Fixed the remaining PDF download failure path shown in device logs.
- **Details**:
  - Added legacy `WRITE_EXTERNAL_STORAGE` permission limited to API 28 and below.
  - Added a shared legacy permission check in `DocumentDownloadManager`.
  - Documents list and document detail now request the legacy write permission before saving to public Downloads on pre-Android 10 devices, then retry the same local PDF save.
  - Android 10+ MediaStore Downloads path remains unchanged.
- **Validation**:
  - `.\gradlew.bat compileDebugKotlin --console plain --no-daemon` passed after fixing a local function ordering compile error.
  - `.\gradlew.bat testDebugUnitTest --console plain --no-daemon` passed.
  - `.\gradlew.bat assembleDebug --console plain --no-daemon` passed.
- **Safety Status**: Local Android changes only. No commit, push, Web edit, Supabase migration apply, APK/AAB upload, or closed-testing change.
- **Pending Tasks**:
  - Real-device QA: tap Download PDF, accept storage permission if prompted, and confirm the generated app PDF appears in the public Downloads folder.

## 2026-07-18 (Local-First document snapshots)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Added Room 17 historical customer snapshots and explicit document deletion/PDF-generation state.
- **Details**:
  - New local documents capture the selected customer's name, WhatsApp number, and city.
  - Reopened documents render the stored snapshot instead of silently adopting later customer edits.
  - Document soft-delete/restore records and clears `deleted_at`; document edits invalidate the cached PDF state.
- **Validation**:
  - Focused JVM snapshot test passed.
  - Android test APK compilation passed with `assembleDebugAndroidTest`.
  - Migration execution still requires an emulator/device because `adb` is unavailable.
- **Safety Status**: Local Android work only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-23 (Backup control-plane fail-closed hardening)
- **Branch**: `codex/backup-drive-production-ready`
- **Implemented**: Unknown data modes now block operational writes until a signed entitlement is persisted; signed policy fields are verified; logical backup relationship columns fail closed.
- **Validation**: Focused JVM Gradle task passed. Device/emulator execution was not run.
- **Safety Status**: Local commits only; no remote or production action occurred.

## 2026-07-18 (Logical Room backup and restore)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Implemented account-scoped logical Room export and transactional restore foundation.
- **Details**:
  - Approved Room tables serialize to deterministic typed JSON entries inside the existing `.tijario` contract.
  - Restore validates table identity, live columns, row shape, and account ownership before any write.
  - Account rows are replaced in foreign-key-safe order inside one SQLite transaction; failures preserve the active database.
- **Validation**: Focused JVM tests and `assembleDebugAndroidTest` passed.
- **Remaining**: PDF/assets, local archive-file lifecycle, keys, UI, Drive transport, and device runtime QA.
- **Safety Status**: Local Android work only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-18 (Offline local backup files)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Added offline account asset collection and verified atomic `.tijario` file creation.
- **Details**: Existing PDFs, product images, and account logo files are included when safe and available; absent/failed PDFs are counted without discarding structured data. The encrypted archive is re-opened for authentication before atomic finalization and backup history persistence.
- **Validation**: Asset-collector and atomic-file JVM tests passed; `assembleDebugAndroidTest` passed.
- **Remaining**: Portable key recovery, missing-PDF generation, asset restore, UI/scheduling, and Drive.
- **Safety Status**: Local Android work only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-18 (Staged asset restore)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Added account-bound backup asset staging and rollback around transactional Room restore.
- **Details**: PDF and product-image archive identities must match structured document/product IDs. Existing files move to rollback storage before replacements; Room failure restores those files.
- **Validation**: Focused JVM tests and `assembleDebugAndroidTest` passed.
- **Remaining**: Portable key recovery, missing-PDF generation, UI/scheduling, Drive, and device runtime QA.
- **Safety Status**: Local Android work only. No push, deployment, migration apply, external-console change, or Play upload.

## 2026-07-18 (Device-wrapped backup keys)
- **Agent**: Codex
- **Branch**: `codex/local-first-complete`
- **Action**: Connected the authenticated backup key-envelope contract to Android Keystore.
- **Details**: Per-installation RSA private keys remain in Keystore; wrapped account keys are cached by version. Backup create uses the newest available version and restore requests the exact header version. Plaintext key bytes are zeroed after operations.
- **Validation**: Focused JVM tests and `assembleDebugAndroidTest` passed. Real Keystore behavior still requires a device/emulator.
- **Remaining**: Unapplied backend migrations/configuration, backup UI/SAF, missing-PDF generation, scheduling, Drive, and device QA.
- **Safety Status**: Local Android work only. No push, deployment, migration apply, external-console change, or Play upload.
