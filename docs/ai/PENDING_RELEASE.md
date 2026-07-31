# Pending Release

## Unapplied Web-Repo Migrations
- `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- `supabase/migrations/20260712090000_document_numbering_counters.sql`

The counters migration is required to manage sequential document numbers without concurrency collisions.

## Status
- **2026-07-30 backup candidate**: Do not release or commit this local candidate until a disposable account/device verifies: first Drive upload without Retry, verified Drive restore with rows/assets, `Downloads/Tijario/Backups` default and custom/reset folders, notification permission/channel behavior, and exact notification cancellation. Automated JVM, Android test APK, lint, and `playQa` checks pass; `adb devices` currently reports no device.
- **Backup/restore work status**: The branch `codex/fix-backup-destinations-drive-restore-notifications` now uses foreground WorkManager work for Drive upload and Drive/SAF/local restore, with progress/cancel handling. Before a release, validate a connected Google account, an actual SAF restore, notification permission/channel disabled behavior, and a cancel action on a physical Android 13+ device. No migration or external configuration change is required by this source change.
- **Release-blocker follow-up**: Local uncommitted code on `codex/fix-production-release-blockers` invalidates retryable rejected leases before later event reassignment and blocks authenticated startup after pending account-deletion local cleanup fails. Confirm these flows on a disposable account/device before any release action; no migration is required or applied.
- **Android 1.1.5 Google Drive fix**: Local uncommitted code `15` / name `1.1.5` resolves Drive identity through `about.user.permissionId` after minimal `drive.file` consent. Local unit tests, Android test APK assembly, lint, release assembly, and AAB bundling passed. Real-device consent/folder/upload/restore QA is pending. If the exact Drive error reason is `accessNotConfigured`, the Google Drive API must be enabled manually in the existing OAuth Google Cloud project; no console change occurred.
- **Android 1.1.4 Closed Testing**: Local Release AAB for code `14` / name `1.1.4` passed unit tests, instrumentation APK assembly, lint, debug/release assembly, and bundle generation. Provenance: `docs/release/ANDROID_1_1_4_RELEASE_PROVENANCE.md`. Upload and physical-device QA remain pending; no Play action occurred.
- **Production Crypto Contract**: Blocked. The safe Vercel Production validator returned `ENTITLEMENT_SIGNING_KEY_ID_MISSING`; configure the expected server-only key ID and rerun the validator before any release. No value was viewed or changed.
- **Android CI Status**: Permanent workflow now bootstraps the Android SDK and installs API 36/Build Tools 36.0.0. Local Gradle validation and GitHub Actions run `30048718414` passed.
- **Migration Status**: NOT applied.
- **Web/API Status**: NOT deployed.
- **Android Update Status**: NOT uploaded. Android document edit/export fixes are implemented locally and need device QA/source-control handoff.
- **Current Local Remediation Status**: `RemoteCacheReplacementPolicy.shouldReplace(...)` is integrated into Android remote-cache ingestion and pull-sync replacement decisions on local branch `codex/full-audit-cache-policy-docs`; architecture documents for Local-First/Drive backup are planning-only and do not change runtime behavior.
- **Local-First Android Status**: Room migrations through schema 17, encrypted backup/restore with SAF UI, offline PDF preparation, and local scheduling exist only on local branch `codex/local-first-complete`; they have not been uploaded or released. The required backup-key backend migrations/configuration are also unapplied.
- **Local-First Completion Status**: Signed entitlement/lease enforcement, immutable event reconciliation, Drive transport abstractions, bounded archive validation, pre-restore safety backup, and release runbooks are complete locally. Backend migrations `20260718090000`, `20260718100000`, and `20260718110000` remain unapplied; signing/envelope secrets and Google OAuth remain unconfigured.
- **Batch A status**: Document numbering idempotency and operation_id sync propagation are completed locally.
- **Adaptive UI QA Status**: Batch B UI recovery plus the Android UI polish follow-up are implemented locally and Gradle-verified. Dashboard/Documents now use `newestDocuments`, quick actions are adaptive, Customer/Product/Document cards have localized long-press sheets, document forms use searchable Customer/Product pickers, and the reported label/icon/phone/quote-number/product-card polish issues are addressed in code. The Android local document-options follow-up adds local shipping, percentage discount, and multi-select taxes/payment/terms without Web/API or Supabase changes. The document edit/export follow-up keeps edit saves on update, uses local cached documents for immediate draft numbers, preserves edited title/language and manual document numbers in local cache/list/detail/export paths, saves locally generated PDFs to public Downloads with legacy storage permission handling on API 28 and below, orders documents by newest creation time, reuses latest selected tax for new documents, adds local discount/extra-fee presets, and restricts email export to email apps.
- **Validation Status**: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check` passed. `lintDebug` previously timed out twice before a pass/fail result.
- **Manual QA Pending**: Verify next-document number previews, edit-mode invoice item changes, PDF visibility in the phone Downloads folder, edited title/manual number persistence in list/detail/PDF, newest-first document ordering, automatic tax defaults, discount/extra-fee preset selection, email-app chooser filtering, sync idempotency, form state restoration after screen rotation, local document option bottom sheets, shipping/discount preview/PDF output, and visual QA on real compact/common Android devices in Arabic and English, including light/dark settings icons and phone country-code selectors.
- **Android Release Artifact**: `app/release/app-release.aab` remains present and git-ignored; do not upload or commit it.
- **Backup/Drive hardening**: Local Google Identity authorization, Ktor Drive file transport, transient-token runtime, Worker handling, and notification contracts require the pending Local-First migrations, server-only signing/envelope configuration, Google Drive OAuth configuration, and device QA before any release.

## Correct Release Order
1. Preserve existing migration state and apply the three Local-First migrations only in the documented approved order.
2. Configure server-only signing/envelope keys and deploy the compatible Web/API branch.
3. Run database post-deploy checks and the physical-phone checklist, including Drive OAuth behavior.
4. Upload Android only after explicit approval and successful staged verification.

## Android 1.1.5 Unified Local Fix
- Prepared for review. Includes Drive post-consent identity resolution, typed backup-key primary-device errors, and onboarding entitlement gating. It is not approved for Play upload.
- Required manual QA: first Google signup/onboarding, transient initialization retry, second-installation primary-device conflict, offline backup after online key preparation, and Drive consent/folder/upload/restore.
- **Local QA variant**: `playQa` is available on this branch, signed with the existing Upload Key configuration, debuggable, and intended for USB testing only. Verify or create the Upload Key Android OAuth client manually before Google Sign-In/Drive testing; do not upload this APK to Play.
- **Physical QA findings**: `docs/release/ANDROID_1_1_5_PHYSICAL_QA.md` records unresolved local Google sign-in, current-plan refresh, stale Business catalog, purchase synchronization, and document deletion defects. These require separate diagnosis before release.

## Pending Multi-installation release gate
- `20260725123000_multi_installation_local_first.sql` is created locally in the Web repository and is **not applied**.
- Required order: review/apply the migration with explicit approval, deploy compatible Web API, then verify two installations for one account can bootstrap entitlement, receive separate wrapped envelopes for the same account key, and receive bounded account-wide offline leases.
- Do not ship the Android source change before the compatible Web migration/API release is approved.

- Apply migration, deploy compatible Web API, then release Android. This order establishes installation authorization before either app requests the new contract.
- **2026-07-26**: Source commit `df99d4b2f8966f166ef14a8ec19c5e0c32c7289e` is pushed. Do not upload Android until the Web migration and API are explicitly approved and verified.
- **2026-07-26**: LocalDrive document-save follow-up is uncommitted and requires physical offline invoice/quote create and edit verification before any release.

## 2026-07-26 LocalDrive bootstrap follow-up
- A local fix lets forced account initialization request the signed entitlement instead of failing from an empty local plan cache. Rebuild/install `playQa` and verify the plan card plus invoice/quote create and edit before release action.
- The current physical diagnosis also fixed a signed-payload JSON-order compatibility check. The verifier retains RSA and claim validation; manually verify plan-card rendering and an invoice save before release.
- Local onboarding now defers backup-key preparation after entitlement success. Manually complete a newly registered account's store settings, then verify the backup screen reports any key issue independently.

## 2026-07-26 Local backup-key repair follow-up
- The connected Android 13 phone now creates encrypted local backups after the Keystore IV repair. Re-run local backup creation, export through SAF, and restore from the generated archive before release.
- Google Drive remains release-blocked on a user-selected real account completing consent, the Drive `about` identity request, folder resolution, upload, and restore. No Google account or external setting was changed during QA.
- **2026-07-26 Google Identity gate**: Device consent completion returned `INTERNAL_ERROR` before the Drive `about` call. The shown Android OAuth package/SHA-1 matches the local `playQa` APK. Before another Drive QA run, verify the Google Cloud `drive.file` data-access scope and OAuth Audience/test-user configuration. Do not enable or change external services without approval.
- **2026-07-27 Local document/settings follow-up**: Uncommitted local code resolves the persisted document template before detail/list PDF rendering and sends one best-effort direct mirror of changed LocalDrive business settings after Room commits. Verify an offline invoice/quote preview, detail, and downloaded PDF match, then verify one online store-settings save appears in `business_settings` without a second unchanged save call. No migration is required or applied.
- **2026-07-27 Document-preview follow-up**: Rebuild/install the local `playQa` APK and verify invoice and quote content immediately after save, after reopening, and after PDF export online and offline. The code now addresses the observed Chromium tile-memory failure; no migration or backend release is involved.
- **2026-07-27 Local quota/backup follow-up**: On a device, create five Free-plan documents offline and verify the sixth is rejected by the signed plan limit, then verify each saved invoice/quote detail and PDF matches preview. Confirm the actual MediaStore output path is `Downloads/Tijario/Backup`; provider-specific fallback to a shallower Downloads path must be shown to the user if the device refuses nested folders. Lease-less local creation events require a compatible server reconciliation/lease policy before release.
- **2026-07-27 PDF layout follow-up**: Rebuild/install the local `playQa` APK, create one invoice and one quote both online and offline, then compare preview, reopened detail, and exported PDF. The renderer invalidates prior `pdfv3` output, so do not use an existing downloaded PDF as evidence.
- **2026-07-27 Vector PDF follow-up**: Verify the newly generated `pdfv5` download with zoom in a PDF viewer: text and table lines must remain sharp rather than scale as one image. Also rerun `assembleRelease` without the local command timeout before any release decision.
- **2026-07-27 Validation update**: The signed `bundleRelease` now passes locally. Keep only physical online/offline preview, reopened-detail, and PDF zoom comparison as the PDF release gate.
- **2026-07-27**: GitHub branch publication completed. Physical PDF, Google Drive, and other release QA remain required; no Play upload occurred.

## 2026-07-29 Pending release blockers
- Do not ship LocalDrive quota hardening until the matching Web migrations and API are explicitly approved, applied, and deployed in order.
- Physical Closed Testing must verify two installations cannot overspend one lease credit, legacy pending event recovery, and complete deletion on a disposable account.
- Re-run Google Drive, Google native sign-in, and PDF zoom QA separately; they are out of scope for this change.
- Re-run the same-device acknowledgement/retry path: the first accepted event must consume one lease credit, duplicate acknowledgement must not consume another, and lease-less legacy excess must remain pending until reconciliation can safely assign it.
- Validate on a device that each retryable lease response clears its pending lease reference, later reassigns a compatible lease, and does not block or reject the event. Also validate startup cleanup after intentionally interrupting local account-deletion cleanup.

## 2026-07-29 Backup destination and restore candidate
- Before any Play action, use disposable data to test Phone backup to a selected SAF folder, automatic default `Downloads/Tijario/Backups`, denied/lost folder permission, and restore picker starting location.
- Verify a manual Drive backup creates no phone-visible duplicate, Wi-Fi-only pauses upload, Drive upload verifies metadata, and Drive restore rejects a backup from another Tijario account without altering current local data.
- Validate foreground progress/notification behavior and cancellation on a real Android 13+ device. No Play upload is authorized by this note.
- Run the compiled Room/assets/FakeDrive round-trip test on an emulator or device, including delayed SAF restore after process recreation and a missing cached key on a second installation. Confirm file-permission loss shows the specific localized message and that the temporary persisted URI grant is released after private staging.
