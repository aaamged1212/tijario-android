# Pending Release

## Unapplied Web-Repo Migrations
- `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- `supabase/migrations/20260712090000_document_numbering_counters.sql`

The counters migration is required to manage sequential document numbers without concurrency collisions.

## Status
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
