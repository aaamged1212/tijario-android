# Agent Handoff (Android & Web Repos)

## 2026-07-26 (Onboarding backup-key decoupling, local uncommitted)
- **Branch**: `codex/backup-drive-production-ready`.
- **Root cause**: A LocalDrive onboarding bootstrap treated optional backup-key envelope preparation as a required account-initialization dependency.
- **Fix**: A valid signed entitlement and Room persistence now make onboarding ready; backup-key preparation is deferred with a safe code-only log.
- **Validation**: Focused `AccountInitializationCoordinatorTest` passed, `assemblePlayQa` passed, and the connected device opened onboarding without the retry state. No store settings were submitted.
- **Safety**: No commit, push, deployment, production migration, external-console change, or Play upload occurred.

## 2026-07-24 (Google Drive post-consent connection fix)
- **Branch**: `codex/backup-drive-production-ready`.
- **Root cause**: A valid `drive.file` authorization was incorrectly rejected when Google Identity omitted profile `id` and `email`. The runtime now resolves Drive `about.user.permissionId` after consent and uses it as the stable Drive identity; email remains optional display metadata.
- **Completed locally**: Added typed `about` transport resolution, folder verification after identity resolution, safe HTTP classification/logging, localized configuration feedback, and focused JVM coverage. OAuth scope remains only `drive.file`. Version is code `15` / name `1.1.5`.
- **Validation**: `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleRelease`, `bundleRelease`, and `git diff --check` passed locally.
- **Remaining**: Real-device consent, Drive API `about`/folder verification, upload/restore, and Play upload need explicit later approval. If Drive returns `accessNotConfigured`, enable Google Drive API in the existing OAuth project manually; no external setting was changed.
- **Safety**: No commit, push, deployment, migration, external-console action, or Google Play upload occurred.

## 2026-07-24 (Closed Testing 1.1.4 preparation)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Accepted the user-provided version-only change to code `14` / name `1.1.4`; no historical branch was merged or cherry-picked. The Release manifest receives AD_ID and AdServices permissions transitively from `facebook-core:18.3.0`; source manifest privacy flags remain false.
- **Validation**: `processReleaseMainManifest`, `signingReport`, `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, `assembleRelease`, and `bundleRelease` passed. Release AAB provenance is in `docs/release/ANDROID_1_1_4_RELEASE_PROVENANCE.md`.
- **Remaining**: Physical-device QA and an explicitly approved Google Play upload. No upload occurred during preparation.

## 2026-07-24 (API 36 CI runner repair)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Added the official Android SDK setup action before both API 36 installation steps in the permanent Android CI workflow, including explicit license acceptance and Build Tools 36.0.0. Removed the obsolete source-modifying one-shot remediation workflow.
- **Validation**: Separate `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, and `assembleRelease` commands passed locally. Permanent GitHub Actions run `30048718414` also passed: compile/unit tests in 5m47s and lint/release assembly in 14m21s. Instrumentation execution was not run because no emulator/device was attached.
- **Production blocker**: The safe Vercel Production validator returned `ENTITLEMENT_SIGNING_KEY_ID_MISSING`; no environment value was viewed or changed.

## 2026-07-23 (API 36 and committed entitlement trust)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Set compile/target SDK to 36 while retaining minSdk 26. Android now verifies signed entitlements using a committed RSA-3072 public-key resource bound to `tijario-entitlement-prod-2026-v1` and its SHA-256 fingerprint, not a local Gradle property.
- **Validation**: `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, and `assembleRelease` passed separately. Device runtime QA remains pending.
- **Release blocker**: Safe Vercel Production validation returned `ENTITLEMENT_SIGNING_KEY_ID_MISSING`; no Production value was changed.

## 2026-07-23 (Google Drive authorization and runtime)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Added Google Identity `AuthorizationClient` flow for the minimal `drive.file` scope, Activity Result resolution, forced account selection, non-secret connected-account metadata, transient-only access tokens, account switching/disconnect, and worker-safe reauthorization handling. Added a Ktor Drive v3 transport that streams finalized encrypted archives for upload/download, a reconstructed process/Worker runtime, folder creation/reuse, Drive list/restore/delete UI, and a backup foreground notification channel.
- **Validation**: Focused Drive authorization, Ktor transport, schedule/worker, notification, and existing Drive client JVM tests passed.
- **External QA**: Google OAuth configuration and real-device authorization, Drive transfer, foreground notification, and account-switch behavior remain unverified.
- **Safety**: No push, deployment, migration apply, external-console action, or Google Play upload.

## 2026-07-23 (Plan-aware backup scheduling)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: Backup settings now clamp manual/weekly/daily selection and retention to the valid persisted signed entitlement. Expired, missing, or malformed cached entitlement data permits manual backups only; a later upgrade preserves a user's manual or weekly preference.
- **Validation**: Focused policy tests, complete JVM tests, and `assembleDebugAndroidTest`/`assembleDebug` passed. Re-run with a sufficient timeout also passed: `lintDebug` in 4m26s and `assembleRelease` in 6m26s.
- **Safety**: No push, deployment, migration apply, external-console action, or Google Play upload.

## 2026-07-23 (Unknown data-mode write protection)
- **Branch**: `codex/backup-drive-production-ready`.
- **Completed locally**: The common operational-write guard now requires a known explicit data mode and a future entitlement expiry. Missing, invalid, unknown, and expired persisted entitlement state cannot fall through to legacy cloud CRUD.
- **Validation**: Focused AccountDataMode and Drive JVM tests passed.
- **Safety**: No push, deployment, migration apply, external-console action, or Google Play upload.

## 2026-07-23 (Published local Backup/Drive branch)
- **Branch**: `codex/backup-drive-production-ready`.
- **Action**: Published the reviewed local commits to the matching origin branch after explicit user approval.
- **Safety**: No merge, deployment, migration apply, external-console action, or Google Play upload.

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

## 2026-07-26 (LocalDrive entitlement bootstrap repair, uncommitted)
- **Root cause**: The LocalDrive cache-only branch ran even for forced startup/retry refreshes, so an account without an AppPreferences plan cache never called the signed entitlement endpoint.
- **Fix**: Non-forced LocalDrive refreshes still use the cached entitlement; forced initialization now reaches backend bootstrap and can persist the entitlement and lease.
- **Validation**: `PlanUsageRefreshPolicyTest` and `assemblePlayQa` passed. Physical-device retest is pending; no commit, push, deployment, migration, or Play upload occurred.

## 2026-07-26 (Signed entitlement verification compatibility, uncommitted)
- **Root cause**: Android rejected a valid RSA-signed entitlement before signature verification because it required its own Kotlin JSON property ordering to exactly match Node's serialized property order.
- **Fix**: The verifier now validates JSON and verifies the original signed payload bytes, then keeps the existing key, account, installation, and expiry checks. Canonical text equality was redundant and cross-runtime brittle.
- **Device evidence**: The connected `playQa` install received `200`, persisted one entitlement and one offline lease in Room, and preserved app data during reinstall.

## 2026-07-26 (LocalDrive document save follow-up, uncommitted)
- **Root cause**: Local typed lease/quota failures were not fully translated and displayed as a generic document error. LocalDrive save also retained guarded outbox calls inside its Room transaction.
- **Fix**: LocalDrive create/update now bypasses operational outbox calls explicitly, preserves typed entitlement/lease/limit results, and keeps plan-usage refresh cache-only for LocalDrive.
- **Validation**: Focused `LocalDocumentSave*` JVM tests and `assemblePlayQa` passed. No commit, push, deployment, migration, or Play upload occurred.

## 2026-07-26 (Published multi-installation Android branch)
- Commit `df99d4b2f8966f166ef14a8ec19c5e0c32c7289e` was pushed to `codex/backup-drive-production-ready`.
- Android remains versionCode `15` / versionName `1.1.5`. No Play upload, migration apply, deployment, or external configuration action occurred.

## 2026-07-26 (Multi-installation review publication)
- **Contract**: Android no longer validates or displays primary-device entitlement fields; LocalDrive save stays Room-only and avoids operational sync scheduling.
- **Validation**: JVM tests, test APK, lint, playQa, Release APK, and Release AAB builds passed. Physical-device QA remains pending.

## 2026-07-25 (Multi-installation Local-First readiness, uncommitted)
- **Action**: Removed normal LocalDrive document-create/update scheduling and primary-device messaging from Android. Local save now returns typed bootstrap/lease failures without exposing internal Room errors.
- **Details**: Android public purchases are limited to Starter and Pro; stale Business plan data is normalized to Pro for display. Compatible Web work has an unapplied non-exclusive installation migration.
- **Validation**: Android `testDebugUnitTest` passed. The combined Android assembly/lint command exceeded the local 10-minute execution window and is not a passing result.
- **Safety Status**: No commit, push, deployment, Production migration, data mutation, external-console change, or Play upload.

## 2026-07-25 (Local playQa physical-device build)
- **Branch**: `codex/backup-drive-production-ready`
- **Action**: Added a local-only `playQa` build type for USB QA using the existing release/upload signing configuration.
- **Details**: `playQa` keeps `app.tijario`, version `15` / `1.1.5`, production BuildConfig values, and is debuggable. It disables release shrinking only for the local QA variant. The Android Studio and OAuth prerequisite are documented in `docs/release/LOCAL_PLAY_QA_BUILD.md`.
- **Validation**: Separate unit tests, test APK assembly, lint, `assemblePlayQa`, release assembly, and release bundle passed. Gradle reported the expected Upload Key SHA-256 for `playQa`.
- **Remaining**: Verify the Upload Key Android OAuth client in Google Cloud and perform physical-device Google Sign-In/Drive QA. Gradle confirmed the configured SHA-1 and SHA-256; `apksigner` was unavailable for an additional direct APK inspection.
- **Safety Status**: No commit, push, deployment, migration apply, Play upload, or external-console change.

## 2026-07-25 (Unified 1.1.5 onboarding and backup-key hardening)
- **Branch**: `codex/backup-drive-production-ready`
- **Action**: Added single-flight entitlement initialization, onboarding gating, LocalDrive-only Room persistence, and typed backup-key primary-device failures alongside the existing Drive consent fix.
- **Validation**: `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, `assembleRelease`, and `bundleRelease` passed.
- **Remaining**: Physical-device Google signup/onboarding, primary-device conflict, offline cached-backup, and Drive consent/folder/upload/restore QA.
- **Safety Status**: No deployment, migration, console change, or Play upload.

## 2026-07-25 (Physical QA findings, open)
- **Branch**: `codex/backup-drive-production-ready`
- **Evidence**: Email/password authentication works from Yemen without VPN. Local Upload-Key Google sign-in remains unresolved even with the exact local 1.1.4 source, while the Play-signed 1.1.4 build works.
- **Open defects**: Current-plan retry has no visible result, Business appears as a stale fourth plan, Google Play purchase synchronization does not update the plan, and document deletion returns a generic error.
- **Documentation**: Exact reproduction steps and investigation boundaries are in `docs/release/ANDROID_1_1_5_PHYSICAL_QA.md`.
- **Safety Status**: Findings only. No production, console, deployment, migration, or Play change.

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

## 2026-07-26 (Local backup key Android Keystore repair, uncommitted)
- **Branch**: `codex/backup-drive-production-ready`.
- **Root cause**: Android Keystore rejected a caller-supplied AES-GCM IV while sealing the installation RSA private key, yielding `BACKUP_DEVICE_KEY_INVALID` before a backup could be created.
- **Fix**: The Keystore AES key now generates its own random encryption IV. The IV remains inside the authenticated local envelope; the account key stays server-wrapped with RSA-OAEP-256 and no primary-device rule is introduced.
- **Validation**: Focused `BackupKey*` JVM tests passed; `assemblePlayQa` passed; the rebuilt QA APK was installed through ADB and created a new encrypted local backup on the connected phone.
- **Remaining**: Drive account selection/consent, Drive API `about`/folder verification, upload, and restore remain real-device QA. No account was selected by the agent.
- **Safety Status**: No commit, push, deployment, production migration, external-console action, or Play upload occurred.

## 2026-07-26 (Google Drive consent diagnosis, uncommitted)
- **Branch**: `codex/backup-drive-production-ready`.
- **Observed on device**: Completing consent for the user-approved test Google account returned Google Identity `ApiException` status `INTERNAL_ERROR`; no `Drive about` HTTP request occurred.
- **Code**: Authorization failures now record only the operation, named Google status, exception class, and account-resolution flag. No tokens, account details, authorization codes, or key material are logged.
- **External gate**: Device QA verified the shown Google Cloud Android OAuth client matches package `app.tijario` and the local `playQa` APK SHA-1. Verify the configured `drive.file` data-access scope and the OAuth Audience/test-user state; Google Drive API enablement has not yet been reached or evaluated.
- **Safety Status**: No commit, push, deployment, production migration, external-console action, or Play upload occurred.

## 2026-07-27 (Local document render parity and store-settings mirror, uncommitted)
- **Branch**: `codex/backup-drive-production-ready`.
- **Document fix**: Detail and list export no longer substitute the app-wide template preference for a document's persisted `template_id`. `TijarioDocumentMapper.fromSaved` now defaults to the saved template, so the local Room items and selected template feed the same renderer for preview and PDF.
- **Settings fix**: LocalDrive store settings commit to Room first. The same changed payload is mirrored once to the existing `business_settings` table only after the transaction succeeds. A per-account SHA-256 payload fingerprint records only successful mirrors, so unchanged saves do not issue repeated calls; failures remain local and do not start outbox/scheduler retries.
- **Validation**: `DocumentEngineTests` (26 tests) and `LocalDocumentSavePolicyTest` (5 tests) passed. `assemblePlayQa` passed.
- **Remaining**: Physical offline invoice/quote preview/detail/PDF parity and one online store-settings persistence check. No migration, deployment, push, external-console change, or Play upload occurred.

## 2026-07-27 (Document rendering memory fix and picker recovery, uncommitted)
- **Root cause**: Device logcat confirms Room save transactions complete while Chromium reports `tile memory limits exceeded`; several full A4 WebViews were allocated and then scaled in the saved-document screen.
- **Fix**: Preview WebViews now render at their visible viewport size. An unavailable remote logo falls back to initials in local HTML. Empty customer/product document pickers expose create actions beside search; customer forms show only the dial code; the default local-backup path is English in both languages.
- **Validation**: Focused document, backup-path, and picker JVM tests passed; `compileDebugKotlin` and `assemblePlayQa` passed.
- **Remaining**: Physical online/offline invoice and quote detail/PDF verification. No commit, push, deployment, migration, external-console action, or Play upload occurred.

## 2026-07-27 (Local save quota, detail rendering, and automatic backup follow-up, uncommitted)
- **Root causes**: A LocalDrive save treated a two-credit offline lease batch as the Free plan's five-document limit. Completed Room saves could then display blank because the detail screen allocated template-picker WebViews alongside the saved-document WebView, and the main WebView reloaded unchanged HTML on recomposition.
- **Fix**: LocalDrive saves now use the verified entitlement limit as the local creation guard and attach a lease only when it has capacity. Saved-document detail uses the document's persisted template without an extra picker, and unchanged HTML is not reloaded. Automatic local backups use MediaStore under `Downloads/Tijario/Backup` and do not open a file picker.
- **Validation**: Focused repository, document rendering, and backup contract JVM tests passed; `assemblePlayQa` passed.
- **Remaining**: A disconnected device prevented physical online/offline detail, PDF, five-document Free-limit, and MediaStore path verification. Events created without an active lease remain locally pending until the backend lease/reconciliation contract can acknowledge them. No commit, push, deployment, migration, external-console action, or Play upload occurred.

## 2026-07-27 (PDF layout ordering and compact document/settings UI, uncommitted)
- **Root cause**: The manual local PDF renderer loaded document HTML before its `WebView` had an A4 width, then drew it after late layout. This can capture an empty surface. Old empty output also remained eligible through the `pdfv3` cache key.
- **Fix**: The renderer now lays out at A4 width before load, relays out after content height is known, waits for visual completion, then draws. `pdfv4` invalidates previously generated files. Detail actions are above the preview, settings rows are compact/title-only, document empty states are tab-specific, and the customer dial-code selector preserves the chosen code.
- **Validation**: Focused JVM tests and `assemblePlayQa` passed. Physical online/offline preview-to-PDF comparison remains required because no device is connected.
- **Safety Status**: No commit, push, deployment, production migration, external-console action, or Play upload occurred.

## 2026-07-27 (Vector local PDF export, uncommitted)
- **Root cause**: The replacement PDF renderer called `webView.draw(canvas)` into `PrintedPdfDocument`. That flattens the Chromium page to pixels, so the saved document becomes blurry when zoomed.
- **Fix**: Local export now keeps the A4 layout/load/visual-state ordering and sends the `WebView` through `createPrintDocumentAdapter`, which writes Chromium's native vector PDF output. The cache key is `pdfv5` so raster `pdfv4` files are not reused.
- **Validation**: `DocumentEngineTests`, full `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assemblePlayQa`, and `bundleRelease` passed. Physical PDF zoom verification remains required.
- **Safety Status**: No commit, push, deployment, production migration, external-console action, or Play upload occurred.

## 2026-07-27 (Authorized branch publication)
- **Published**: The validated LocalDrive/document/PDF commits through `1f8f54b2e3302752d954758ae21e05f96bc43077` were pushed to `origin/codex/backup-drive-production-ready`.
- **Safety**: No deployment, production migration, external-console action, or Play upload occurred. Local `.agents` files remain excluded.

## 2026-07-29 (Production release blockers, validated locally)
- **Quota contract**: LocalDrive document creation serializes quota-credit preparation, refreshes a lease at most once, and refuses atomic Room persistence with `OFFLINE_QUOTA_UNAVAILABLE` when no server-issued credit is available. New creation events always contain a lease ID.
- **Legacy events**: Lease-less pending events are recovered before reconciliation. They receive a valid compatible lease when capacity exists or become `BLOCKED` for a verified terminal limit; transient failures remain pending without being silently filtered.
- **Account deletion**: Android calls the unified mobile endpoint first and cleans Room/files/account-scoped workers only after server success.
- **Validation**: Full JVM tests, debug Android-test assembly, lint, PlayQa, signed Release APK, and signed AAB passed. Version remains `15` / `1.1.5`.

## 2026-07-29 (Release-blocker contract correction, local only)
- **Lease accounting**: A LocalDrive event consumes its lease credit only in the same Room transaction that changes that event from pending to acknowledged. Reconciliation uses the lease's exact usage-cycle period.
- **Legacy events**: Lease-less events are assigned only up to current compatible capacity; unassignable excess stays pending for later reconciliation rather than being incorrectly marked blocked.
- **Deletion retry**: A successful server deletion records a local pending-cleanup marker. Retrying after a local cleanup failure performs only the local cleanup, and cleanup aborts before job cancellation if a scoped file cannot be deleted.
- **Validation**: Focused `LocalDocumentSave`, quota, and account-deletion JVM tests plus `assemblePlayQa` passed. Version remains `15` / `1.1.5`; branch publication is authorized, with no migration, deploy, or Play action.
