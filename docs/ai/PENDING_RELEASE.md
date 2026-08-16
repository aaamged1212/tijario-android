# Pending Release

## 2026-08-16 Android splash visual QA gate
- Cold-launch the app on Android 12 or newer in both light and dark system themes. Confirm the full Tijario mark is centered, none of its four segments is cropped, and the neutral icon surface separates both brand colors from the background.
- Cold-launch on one pre-Android-12 device or emulator to verify the SplashScreen compatibility implementation uses the same scale and background.
- Confirm the transition from the system splash to the Compose startup state does not visibly jump in logo shape, crop, or alignment.
- No backend deployment, Supabase migration, Production configuration, or Google Play action is required for this Android-only visual correction.

## 2026-08-16 settings and plan-carousel QA gate
- In Business Settings, open Currency and verify a searchable bottom sheet lists the full catalog, persists a selection, and remains readable in Arabic RTL and English LTR.
- In App Settings, select Device language, Arabic, and English; then select Device theme, Light, and Dark. Restart after each explicit choice and verify persistence, then change the device configuration while System is selected and verify the app follows it after recreation.
- Verify Settings shows the cached current plan immediately and then the profile image/name/email. Check Business, Payments & Subscriptions, Account, App, and Backup rows on compact screens; confirm options use spacing rather than separator lines.
- Open Upgrade Plan offline and online. Confirm Free, Starter, and Pro cards appear immediately, swipe horizontally, only Pro has a colored outline, Google Play prices replace the placeholder when available, and the comparison remains below.
- No backend deployment, Supabase migration, Production configuration, or Google Play action is required for this Android-only UI change.

## 2026-08-16 system-default and account UI QA gate
- Clear app data and launch once with Arabic/light, Arabic/dark, English/light, and English/dark system configurations. Confirm the first screen follows both settings, then choose a different in-app language/theme, restart, and confirm the explicit choice persists.
- On compact and large screens, verify login, registration, verification, password recovery, and onboarding in RTL and LTR with the keyboard open. Confirm controls remain reachable and all cards/text have sufficient contrast in both themes.
- Open Settings and confirm its compact rows navigate to Business, Payments & Subscriptions, Account, App, and Backup. Confirm plan/usage loading, retry, and upgrade navigation are no longer shown on the Settings home page and work from the new destination.
- No backend deployment, Supabase migration, Production configuration, or Google Play action is required for this Android-only UI change.

## 2026-08-16 AI history and picker/search QA gate
- Upgrade an existing installation with Room schema 19 data and confirm migration to schema 20 preserves customers, products, and documents. Generate multiple replies and captions, restart the app, and confirm each tab shows only its own scrollable history and that every item copies correctly.
- In onboarding, Business Settings, and customer phone entry, search countries by localized name and calling code, select one, and confirm its flag/code remain selected. In product currency selection, search by currency code and country and confirm the selected value persists.
- Enter different queries in the invoice and quotation tabs, switch between them, and confirm each query is retained and matches both document number and customer name while status filtering still applies.
- No backend deployment, Supabase migration, Production configuration, or Google Play action is required. The Room 19-to-20 instrumentation migration test is compiled but requires a connected device to execute.

## 2026-08-16 AI result isolation QA gate
- On a physical device, generate a smart reply, switch to captions, and confirm the reply result is not visible. Generate a caption, switch back, and confirm the original reply remains unchanged.
- Confirm caption output contains generated text and actions without customer intent/message analysis. On a compact screen, generate a long result and confirm its final actions are fully reachable above the bottom navigation.
- No migration, backend deployment, external configuration change, or Google Play action is required for this Android-only UI correction.

## 2026-08-16 notification branding QA gate
- On an Android 13+ device, trigger one announcement and one backup/restore notification. Confirm the expanded notification displays the supplied full-color Tijario logo and the status bar shows a clear monochrome Tijario mark on light and dark system surfaces.
- No migration, backend deployment, external configuration change, or Google Play action is required for this Android-only visual correction.

## 2026-08-15 document update item-preservation QA gate
- On a device in airplane mode, edit an invoice and a quote with multiple items, save each, reopen each, and confirm the edited item count, quantities, prices, totals, and metadata remain intact.
- For any document already affected by the prior item-loss defect, reconnect once and open it to recover the last complete server snapshot. Confirm the restored document has items before editing it again. The previous locally deleted item edits cannot be reconstructed.
- No migration, backend deployment, or external configuration change is required for this Android-only correction.

## 2026-08-15 document detail and picker QA gate
- On a device, create a new invoice and quote after several existing documents and confirm the initial number is the next type-specific Room number.
- With airplane mode enabled, open and edit locally-created documents. For an older cloud-created document whose Room cache has no item rows, open it online once to hydrate the local snapshot, then confirm it opens and edits offline. Confirm protected unsynced documents are not replaced by that hydration path.
- From both populated customer and product pickers, create a new record and confirm it is selected in the active document without reopening the picker. Verify search and selection in country calling-code and product-currency sheets.
- No migration, backend deployment, or external configuration change is required for these Android-only tests.

## 2026-08-13 Room-first operational storage QA gate
- Before any release action, verify on one historical `legacy_cloud` account after one online entitlement initialization: create/edit/delete a customer, create/edit/delete a product or service, create/edit an invoice and quote, and confirm airplane-mode saves never invoke cloud operational CRUD.
- Verify the next invoice and quote numbers derive from current Room history, custom valid numbers remain stable after edit, and cached plan limits reject the next customer/product/document locally.
- Verify Business Settings save succeeds offline with the local currency used by the next document, then reconnect and confirm one best-effort mirror does not replace operational Room data. No backend migration or deploy is required for this Android-only behavior.

## 2026-08-12 Document-number API compatibility gate
- Do not release this Android branch before the compatible Web API and corrected pending migration are applied through the approved release sequence.
- On a physical device, verify an invalid requested invoice/quote number and a duplicate requested number show their new localized message, while a valid unique number saves unchanged.

## 2026-08-11 AI context and document-number compatibility gate
- Do not publish `fix/android-runtime-critical-fixes` until the compatible Web/API branch `fix/mobile-runtime-critical-backend` is deployed with `20260811123000_preserve_requested_document_numbers.sql` applied in the approved release sequence.
- Before release, test on a physical device with LocalDrive-only customer/product records: reply and caption generation must retain the selected values, malformed snapshots must show the localized validation message, and provider timeout must show the localized retryable message without technical provider text.
- Android source is published at `08b0d85cf38202a03585b70490b4e0d21dbd8f01`; it remains blocked on compatible API deployment and physical-device QA.

## 2026-08-09 analytics verification follow-up
- Confirm the provider dashboard receives distinct reply and caption event counts only after a future authorized app release; no analytics provider configuration was changed.

## 2026-08-09 CI validation follow-up
- The hardened workflows have only been reviewed locally. When the hardening branch is authorized for push, inspect the first GitHub Actions runs for active branch matching, artifact upload, and no write-back commits.

## 2026-08-09 money boundary follow-up
- The remote numeric API contracts still use `Double`; they are not an authoritative local calculation path. Any migration to decimal strings or minor units requires a coordinated compatible Web/API release and is intentionally deferred.

## 2026-08-09 hardening follow-up
- The first local security hardening change is on `fix/android-full-hardening`; it is not a release candidate. Before any release, rerun the full Android verification set and physically verify document sharing, PDF logo fallback, and valid Tijario announcement links. No production/external change is required or has been performed.

## Unapplied Web-Repo Migrations
- `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- `supabase/migrations/20260712090000_document_numbering_counters.sql`

The counters migration is required to manage sequential document numbers without concurrency collisions.

## Status
- **2026-08-01 entitlement contract gate**: The production Android serializer accepts all 12 shared entitlement response cases. Release still requires the five compatible unapplied Web migrations/API rollout in timestamp order and physical verification; no Android runtime behavior or version changed in this optimization task.
- **2026-08-01 Drive upload follow-up**: Code and deterministic tests now cover first-attempt resumable uploads whose create response omits verification metadata. Before Play release, perform one real Drive upload and confirm it reaches `DRIVE_UPLOADED` without pressing Retry; no migration or external configuration change is required.
- **2026-07-31 archive/history follow-up**: Room schema 19 adds local-only `backup_records.restored_at`. Before any Play action, install the new `playQa` build and verify one phone archive and one Drive archive restore completely, history shows only the matching completed restore, and a manual Drive backup uploads on its first attempt when its selected network policy is satisfied.
- **2026-07-31 restore correction**: Version `18` / `1.1.8` requires physical restore QA before any Play action. Install the current build on the connected device, then restore one existing phone archive and the same Drive archive. Both must pass through Room restore without creating a visible history record for the internal pre-restore safety snapshot. No external change is required.
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

## 2026-08-09 App Link release gate
- Before releasing `fix/android-full-hardening`, serve `/.well-known/assetlinks.json` without redirects from both `tijario.site` and `www.tijario.site`.
- Confirm it lists `app.tijario` and the active Google Play app-signing SHA-256 certificate. A local/upload certificate alone is insufficient.
- On a release-signed device, verify both hosts through `pm get-app-links` and complete password-reset and OAuth callback flows. Keep the existing custom schemes as fallback until this passes.

## 2026-08-13 LocalDrive offline CRUD QA gate
- Install the local Debug APK and verify customer create/edit, product/service create/edit, invoice/quote create, and document edit while airplane mode is enabled after one successful online account initialization.
- Verify the stored plan limits still reject the next customer, product, or document offline. A missing or expired cached entitlement is expected to show its typed setup/entitlement error rather than a generic failure.
- No migration, backend deployment, or Google Play upload is authorized by this local source correction.

## 2026-08-13 Offline document and catalog QA gate
- After one successful online account initialization, enable airplane mode and edit a saved invoice and quote. Confirm both save, reopen with their changed items/totals, and remain `LOCAL_ONLY` without a generic error.
- Create same-day invoices or quotes with intentionally non-sequential display numbers and confirm the newest creation time appears first in document and dashboard lists.
- Verify country flag, calling-code selection, country-to-phone synchronization, and currency labels in onboarding and store settings. Confirm a product-specific currency survives save and reopen.
- On a tracked product, verify an over-stock product is disabled in the picker, the item editor blocks confirmation, and an explicit stock increase updates the saved product before allowing the item.

## 2026-08-16 Preview and Currency QA Gate
- On a device, set a store logo, open a new invoice and quote preview before saving, and verify the same logo is visible in the preview and exported PDF. Reopen while offline after the logo has been cached.
- Attempt to add a product with a different saved currency from the document. It must be disabled with the localized explanation and final save must reject any stale mismatched item.
- Confirm the product picker shows the exact remaining stock for tracked products after adding the same product to another invoice row.
- Check reply and caption output in Arabic and English for customer/product, tone, dialect, platform, offer, benefit, and length controls. Confirm private contact data is never sent or displayed.

## 2026-08-16 Navigation and Splash Visual QA Gate
- Cold-start the app in light and dark device modes and confirm the transparent Tijario mark is fully visible on the dark splash background with no white plate or clipped edges.
- Verify Arabic and English headers: Home stays at the start edge with `تجاريو / Tijario`; Documents, AI, Products, and Customers remain centered on compact screens.
- Verify the profile card opens only name/email, inline pencil/check editing saves correctly, and Account Settings still exposes password, subscription sync, logout, and deletion.
- Verify Free and Starter show the gold upgrade action, Pro does not, and tapping the surrounding plan card opens the current-plan page.
- Verify Arabic/English and Device (Automatic)/Light/Dark bottom sheets, plus local/Drive backup actions, restore, sharing, scheduling, and history on a physical device.
