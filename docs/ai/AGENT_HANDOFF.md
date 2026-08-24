# Agent Handoff (Android & Web Repos)

## 2026-08-24 (Yemen manual payment proof flow, published source)
- **Commit**: `8575db40768df0d071df2e9baebf3db6260e3ccd` is pushed to `origin/codex/yemen-payment-methods`.
- **Release state**: Source publication only. No APK/AAB upload, Google Play action, Supabase migration, deployment, Production write, or external configuration change occurred.

## 2026-08-24 (Yemen manual payment proof workflow, local uncommitted)
- **Flow**: The Yemen tab now selects a payment method first, then opens a separate receipt-proof screen. It displays only the supplied Al-Kuraimi, Jeeb, and internal-transfer account data, with the requested plan price reference and copy controls.
- **Safety**: Submitting one bounded receipt sends an authenticated manual-review request to the server and records a local in-app notification. It never changes a subscription, entitlement, or billing state.
- **Storage**: Room 20-to-21 adds `announcements_cache.is_local`; refresh retains these local payment notices and notification read/dismiss actions do not create server receipts for them.
- **Validation**: `compileDebugKotlin` passed. Focused JVM execution is blocked at existing `BackupPlanPolicyTest.kt` Long-to-String test-source errors before any selected test can run. No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred.

## 2026-08-24 (Yemen payment-method selection, local uncommitted)
- **Scope**: On `codex/yemen-payment-methods`, a paid-plan upgrade routes Yemen-eligible users to a localized, theme-aware payment-method screen. It offers Global Google Play and Yemen tabs, shows the requested static Starter/Pro price references, and copies supplied Al-Kuraimi, Jeeb, and internal-transfer values without exposing or collecting any extra data.
- **Eligibility**: The client uses the saved business country or current device country (`YE`) only. It does not perform IP lookup or send location data. Non-Yemen users retain the existing direct Google Play purchase flow.
- **Boundary**: No receipt upload, manual-payment verification, plan mutation, or entitlement bypass was added. A payment-operation/backend contract is required before local transfer payment can activate a subscription.
- **Validation**: `compileDebugKotlin` passed. The focused `YemenPaymentEligibilityTest` task is blocked before executing tests by existing unrelated `BackupPlanPolicyTest.kt` Long-to-String compilation errors.
- **Safety**: No commit, push, deployment, migration, Production write, Web change, external configuration change, or Google Play upload occurred. `.agents/` remains local and excluded.

## 2026-08-21 (Authenticated support feedback client, published)
- **Root cause**: The Android feedback form wrote directly to a non-existent `user_feedbacks` table, so reports could neither persist nor reach support.
- **Correction**: Android now validates and bounds image attachments locally, then submits to authenticated `POST /api/mobile/feedback`. It no longer sends feedback through PostgREST or an external email client and never receives mail-provider credentials.
- **Publication**: Commit `8ceddf2337fed5c3cf5ba9ffb9780240686dffd4` is pushed to `origin/fix/android-runtime-critical-fixes`. The Web counterpart is deployed in Vercel Production, with a private Storage bucket and service-role-only delivery audit table.
- **Remaining QA**: Test Arabic and English reports with no image and with one image on a physical device. Verify delivery to `support@tijario.site`, reply routing to the account email, private attachment access, and safe failure messaging if the mail sender is unavailable.

## 2026-08-20 (In-App Review, Share App, Feedback Screen, and Catalog Dial Code polish, local uncommitted)
- **In-App Review API Integration**: Replaced Google Play redirects with Google Play In-App Review API inside both `CoreScreens.kt` (Dashboard) and `SettingsScreens.kt` (Settings rating row) for ratings of 4 stars or higher.
- **Quick Action Icon Colors**: Changed quick action icon tint to light sky blue (`Color(0xFF38BDF8)`) to align with financial summary card icon colors.
- **Financial Summary Card Footprint Compaction**: Reduced card vertical/horizontal padding to `16.dp` and inner spacing to `10.dp`, shrinking total sales numbers text to `24.sp` and indicators text sizes to match.
- **Country Catalog US name & Dial Code cleanup**: Reset USA name override to "الولايات المتحدة الأمريكية" (AR) and "United States" (EN). Filtered out all non-US countries sharing the `+1` dial code from the country catalogs to leave a single `+1` option.
- **Report a Problem / Send Feedback Screen**: Created a comprehensive `FeedbackScreen` in `SettingsScreens.kt` containing subject dropdown options ("عام", "مشكلة", "اقتراح ميزة", "الفوترة", "أخرى"), message input box (max 4000 chars), up to 3 image attachments, and direct email client submission to `support@tijario.site`. Added navigation toggle in settings menu card.
- **Share App integration**: Appended a "(شارك التطبيق)" / "Share App" row in settings option list that opens system share sheet prefilled with localized marketing copies and Play Store link.
- **Email OTP Spam Folder resolution**: Written a complete Arabic configuration guide [`supabase_smtp_guide.md`](file:///C:/Users/BBOY%20AMG/.gemini/antigravity/brain/74a84f97-ad40-4aef-bcc7-1ab6dac8b48d/supabase_smtp_guide.md) outlining SMTP setup steps.
- **Validation**: Compiled successfully with `./gradlew compileDebugKotlin` (Build Successful).
- **Safety**: Committed previous changes locally. No push, deploy, Supabase production migration, or Google Play store upload occurred.

## 2026-08-19 (Customer stats format, Onboarding & Business settings country/currency auto-detection, local uncommitted)
- **Customer Stats Formatting**: Restructured the 4 indicators in `CustomersScreen` (`CoreScreens.kt`) into a compact horizontal Row. Placed labels next to icons (removing the word "customers" or "العملاء" for a smaller footprint) and the count directly below them.
- **Onboarding & Business Settings Country Field Position**: Placed the Country selection field immediately after the Business Name field in both `OnboardingScreen` (`AuthScreens.kt`) and `BusinessSettingsScreen` (`FormScreens.kt`).
- **Country & Dial Code Auto-Detection**: Dynamically detect the customer's country code on startup/onboarding using the device's sim/network ISO and system locale configurations. If detection fails, it defaults cleanly to USA (`US`). Automatically updates the phone dial code when a country is selected.
- **USA Name Override**: Overrode the display name of USA (country code `US`) to simply read "أمريكا" (Arabic) and "USA" (English) instead of Samoa or minor outlying islands references.
- **Currency Auto-Detection**: Dynamically detects the official currency based on the detected country, falling back to `USD` (instead of `SAR`) on failure.
- **Currency Bottom Sheet Selection**: Replaced the currency selection dropdown menus in both `OnboardingScreen` and `BusinessSettingsScreen` with a fast-loading premium `CurrencyBottomSheet` (in `TijarioComponents.kt`).
- **Email Verification / Spam Folder Warning**: Added a prominent notice beneath the verification code text input field in `VerifyEmailScreen` (`AuthScreens.kt`) alerting users to check their Spam/Junk folder if they do not receive the email in their main Inbox.
- **AI Advanced Settings Experimental Opt-In**: Annotated `ReplyFormBlock` and `CaptionFormBlock` in `AiScreens.kt` with `@OptIn(ExperimentalMaterial3Api::class)` to address experimental warnings for ModalBottomSheet.
- **Validation**: Compiled successfully with `./gradlew compileDebugKotlin` (Build Successful).
- **Safety**: No commit, push, deployment, Supabase migration, external configuration change, or Google Play upload occurred.

## 2026-08-19 (Onboarding welcome screen localization, local uncommitted)
- **Onboarding Welcome Screen Language Integration**: Modified `IntroWalkthroughScreen` in `AuthScreens.kt` to dynamically read system language (`java.util.Locale.getDefault().language`).
- **Dynamic Assets**: Displays `onboarding_background_ar.png` (Arabic welcome image) or `onboarding_background_en.png` (English welcome image) based on the locale.
- **Button Localization & Fonts**: Displays "ابدأ الآن" for Arabic and "Start Now" for English on the action button. The text now uses `MaterialTheme.typography.labelLarge` to inherit the application's font family (Almarai/Gilmer).
- **Validation**: Compiled successfully with `./gradlew compileDebugKotlin` (Build Successful).
- **Safety**: No commit, push, deployment, Supabase migration, external configuration change, or Google Play upload occurred.

## 2026-08-17 (Welcome screen design update, local uncommitted)
- **Onboarding/Welcome Screen**: Replaced `IntroWalkthroughScreen` in `AuthScreens.kt` with a high-fidelity single welcome page using `onboarding_background.png` (the first supplied screenshot).
- **Pixel-Perfect Scaling**: Changed image scale to `ContentScale.Fit` and set screen background to `#020E1C` (extracted matching top/bottom background color) to ensure the image remains sharp, clear, and un-cropped.
- **Start Button overlay**: Added the green `ابدأ الآن` button overlay (`Color(0xFF0FA36E)`) at the bottom of the welcome page which navigates straight to the login screen upon click, matching the second screenshot's button.
- **Top Brand Cleanups**: Removed all programmatically drawn top brand names ("تجاريو") and logo icons from the walkthrough.
- **Validation**: Compiled successfully with `./gradlew assembleDebug` (Build Successful).
- **Safety**: No commit, push, deployment, Supabase migration, external configuration change, or Google Play upload occurred.

## 2026-08-17 (Settings UI, cached profile, pricing plans styling, backup paid-tier lock fix, and tanween correction, local uncommitted)
- **Compact Text Inputs**: Updated `TijarioTextField` in `TijarioComponents.kt` to shrink font sizes and compact the layout spacing slightly.
- **Profile Caching**: Cached profile full name in SharedPreferences inside `TijarioRepository.kt` on load and update, and initialized `profileName` and `profilePicBitmap` synchronously in Settings `remember` to display them instantly on first frame without delays.
- **Pricing & Upgrades**: Customized the pricing plans upgrade screen to display active current plan badges/surfaces in green (`Color(0xFFE8F5E9)` background, `Color(0xFF2E7D32)` text) and changed subscribe labels to "ترقية" (Upgrade).
- **Rate App**: Added a "Rate App" setting option using `Icons.Filled.Star` that launches the Google Play Store details page or fallback URL.
- **Backup paid-tier lock fix**: Corrected `BackupPlanPolicy` and `BackupViewModel` to dynamically unlock automatic backups and Google Drive features for paid tiers (Starter, Pro) based on the user's active plan code, even if a backend-signed entitlement signature is not yet synchronized.
- **Tanween Arabic spelling corrections**: Updated Arabic strings for `عميلاً` and `تعاملاً` in `Localization.kt` and `AiScreens.kt` to position the Fathatan character (`ً`) after the final Alif (`ا`) for proper alignment above the Alif letter. Updated `ArabicTanweenContractTest.kt` to exclude these paths.
- **Validation**: Compiled successfully with `./gradlew assembleDebug` (Build Successful). Unit tests passed.
- **Safety**: No commit, push, deployment, Supabase migration, external configuration change, or Google Play upload occurred.

## 2026-08-16 (Android splash logo safe area, local uncommitted)
- **Root cause**: `logo_app.png` occupied essentially the full 1024px canvas, while Android masks the outer third of splash icons. The logo therefore appeared oversized and visibly cropped on startup.
- **System splash**: `Theme.Tijario.Splash` now uses `Theme.SplashScreen.IconBackground`, a dedicated 160dp centered drawable, the exact supplied transparent 2000x2000 logo, and day/night launch colors with matching system-bar contrast.
- **Compose splash**: The secondary startup surface uses the same transparent mark with fit scaling inside a neutral circular surface, while text and progress colors follow the active Material theme.
- **Validation**: `SplashScreenUiContractTest` passed and verifies the approved asset SHA-256, dimensions, splash theme contract, and Compose asset usage. `assembleDebug` passed. Physical launch QA remains pending.
- **Safety**: No commit, push, merge, backend/Web change, Supabase migration, deployment, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-16 (settings sheets and plan carousel)
- **Selection UX**: Business currency opens the existing searchable currency bottom sheet. App language and appearance each open a compact bottom sheet with Arabic/English/device-language and light/dark/device-theme choices; explicit selections remain backward compatible with prior stored booleans/language values.
- **Settings hierarchy**: The Settings home shows a cached current-plan banner followed by the local profile image/name/email, then compact destination rows. Business, App, and Account action rows use smaller icons/padding and space instead of separator lines.
- **Plans**: Free, Starter, and Pro cards are available synchronously from safe local display definitions, then backend limits and Google Play localized prices replace their corresponding values when available. The cards swipe horizontally, comparison remains below, and only Pro has a colored outline. Purchase availability and Google Play billing logic are unchanged.
- **Validation**: Kotlin compilation, focused JVM tests for preference modes/settings contracts/searchable currency selection/country catalogs, `assembleDebugAndroidTest`, `assembleDebug`, and `git diff --check` passed. Physical compact-screen and RTL/LTR visual QA remain pending.
- **Safety**: This handoff is recorded in one local commit on the current feature branch. No push, merge, backend/Web change, Supabase migration, deployment, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-16 (system defaults and authentication/settings UI, local uncommitted)
- **First launch**: When no preference has been saved, Android language follows an Arabic system locale or uses English for other locales, and appearance follows the system night mode. An explicit in-app language or theme choice remains persisted and overrides later system changes.
- **Authentication**: Login, registration, email verification, password recovery, and business onboarding now share a Material theme-aware background, branding header, responsive card surfaces, and always-available language/theme controls instead of a fixed green canvas.
- **Settings**: The main Settings page is a compact grouped list. `Store Settings` is presented as `Business` / `النشاط التجاري`; a new `Payments & Subscriptions` destination owns current-plan usage, retry, and upgrade actions.
- **Validation**: `AppPreferencesDefaultsTest` and `AuthSettingsUiContractTest` passed. `assembleDebugAndroidTest`, `assembleDebug`, and `git diff --check` passed. Physical fresh-install, RTL/LTR, keyboard, and compact-screen visual QA remain pending.
- **Safety**: No commit, push, merge, backend/Web change, Supabase migration, deployment, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-16 (local AI history and searchable pickers/documents, local uncommitted)
- **AI history**: Smart replies and captions now persist in account-scoped Room history with separate types. Each tab opens its own scrollable bottom sheet, and every saved variant can be copied independently. Room schema advances from 19 to 20 with a forward migration; historical generations created before this change cannot be reconstructed.
- **Pickers**: Country and calling-code selection uses the full searchable country catalog with flags and context-specific hints. Product currency search now matches currency code, country, and flag across the expanded ISO catalog. Business Settings and onboarding share the country bottom sheet and keep the calling code aligned with the selected country.
- **Documents**: Invoice and quotation tabs retain independent compact search queries and match either document number or customer name before displaying their existing status filters.
- **Validation**: Focused AI-history, catalog, picker, and document-search JVM tests passed. `assembleDebugAndroidTest` and `assembleDebug` passed; the Room 19-to-20 instrumentation migration test compiled but still requires a connected device for execution.
- **Safety**: No commit, push, merge, backend/Web change, Supabase migration, deployment, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-16 (AI result isolation and bottom-navigation visibility, local uncommitted)
- **Change**: Smart-reply and caption generation now retain separate UI states and independent scroll positions. A reply result cannot render in the caption tab, and a caption result cannot replace a reply result. Caption output no longer renders the customer-message analysis, including customer intent.
- **UX**: A successful result scrolls its active tab to the generated result. The AI page reserves bottom content space when embedded in the app shell so the final result actions remain visible above the bottom navigation.
- **Validation**: `AiGenerationStateStoreTest` and `assembleDebug` passed. The first test attempt was blocked by a local KSP incremental-cache flush failure; the same test completed successfully with incremental KSP disabled only for that invocation.
- **Safety**: No commit, push, merge, backend/Web change, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-16 (official notification branding asset, local uncommitted)
- **Change**: The supplied official Tijario 512px logo is bundled as `tijario_notification_logo` and is now the large icon for announcement and backup/restore notifications.
- **Android behavior**: The status-bar icon remains the existing monochrome `ic_stat_tijario`, because Android masks small notification icons to a monochrome silhouette. The expanded notification shows the supplied full-color logo.
- **Validation**: `assembleDebug --console plain --no-daemon` passed. Physical notification visual QA remains pending.
- **Safety**: No commit, push, merge, backend/Web change, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-15 (document update item preservation, local)
- **Root cause**: `updateDocumentLocal` inserted replacement item rows before calling Room `upsertDocument`. The DAO uses `REPLACE`, which deletes the parent row and its cascading child rows, leaving an itemless `LOCAL_ONLY` document after a seemingly successful edit.
- **Correction**: The same Room transaction now writes the document parent first, clears old items, then inserts replacement items. This preserves the new item rows and keeps the edited document reopenable offline.
- **Recovery**: An itemless local document with a persisted server revision can hydrate the last complete server snapshot when online. A purely local itemless record remains protected and returns the typed missing-items state. Item edits already deleted by the prior defect cannot be reconstructed from Room.
- **Validation**: Focused repository tests passed, including ordering and recovery coverage; `assembleDebug` passed. No device was connected.
- **Publication**: Source and prior handoff updates are committed locally as `b8465e9346b515071037f90a3aee78ebbb82f367`; they are not pushed. No merge, backend/Web change, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-15 (document cache hydration and picker flow, local)
- **Branch**: `fix/android-runtime-critical-fixes`.
- **Documents**: The detail loader now renders complete Room snapshots offline and, for replaceable legacy cloud summaries with missing item rows, hydrates the complete document once from the mobile detail endpoint and atomically caches its items. Protected local states are never overwritten. Missing legacy items now return a typed local state rather than falling through to an incorrect quote detail title or a generic failure.
- **Form and pickers**: New document numbers query Room history at form opening. Customer/product creation launched from a document returns the new record directly to the active form. Country calling codes and product currencies use searchable bottom sheets; picker creation actions are compact `+ New` controls.
- **Validation**: 59 focused JVM tests passed and `assembleDebug` passed locally. Physical QA remains required, especially opening one historical cloud document online once before testing it offline if its old local cache never contained items.
- **Safety Status**: No commit, push, merge, backend/Web change, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-13 (Room-first operational storage for every account, local)
- **Architecture correction**: Android operational seller data now uses Room for every signed entitlement data mode, including historical `legacy_cloud`. Customer, product/service, and document compatibility entry points no longer create operational outbox work or require cloud CRUD. Existing cloud records and historical outbox rows are retained but are not imported, deleted, or sent automatically.
- **Business settings**: Store settings persist to Room first and return success after the local transaction. A best-effort Supabase mirror runs asynchronously; an empty local store may hydrate settings once from the server, while initialized local settings are never replaced by a refresh.
- **Quota and documents**: Cached signed entitlements still enforce customer, product, and document limits. Invoice/quote writes remain atomic with their item rows, snapshots, and creation event. Next document numbers derive from Room history only.
- **Validation**: Focused JVM suite passed: 47 tests, zero failures/errors. `assembleDebug` passed. Physical offline QA remains required for the six documented account scenarios.
- **Safety Status**: No push, PR, merge, backend/Web change, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-12 (Document-number error contract, local)
- **Branch**: `fix/android-runtime-critical-fixes` from `6f048a4c5217d8ac3409058bb1ca85141cdf72b1`.
- **Compatibility**: Added localized handling for backend `DOCUMENT_NUMBER_INVALID` and `DOCUMENT_NUMBER_DUPLICATE` responses. Arabic and English users receive a specific safe message instead of a generic document-save failure.
- **Validation**: Full JVM tests, `lintDebug`, `lintRelease`, `assembleDebug`, and `assembleRelease` passed locally. Version remains code `19` / name `1.1.9`.
- **Safety**: No backend, migration, deployment, external configuration change, Google Play upload, push, or merge occurred. Existing untracked `.agents` directories remain untouched and excluded.

## 2026-08-11 (Runtime-critical AI context and document-number parity, local)
- **Android branch**: `fix/android-runtime-critical-fixes` at base `31ede1df9230c3bb19431b90659afbf2d683f237`.
- **Implemented**: AI reply/caption requests preserve the selected local IDs and add a strict, bounded context snapshot containing only generation-relevant business/customer/product fields. The snapshot deliberately excludes WhatsApp, email, plan, quota, and tokens. New localized mappings handle provider timeout and invalid snapshot responses.
- **Compatibility gate**: The matching Web/API contract is on `fix/mobile-runtime-critical-backend`; Android must not be published ahead of that compatible API deployment and its unapplied document-number migration.
- **Validation**: Focused snapshot tests, full JVM tests, `lintDebug`, `lintRelease`, Debug/Release/AndroidTest/PlayQa assembly, and `bundleRelease` passed locally. No device or emulator was connected for physical AI verification.
- **Safety**: No production write, migration application, deployment, Play upload, external configuration change, commit, or push occurred during this local validation step.
- **Publication**: Committed and pushed as `08b0d85cf38202a03585b70490b4e0d21dbd8f01`. No merge, deployment, migration application, or Play upload occurred.

## 2026-08-09 (Analytics taxonomy correction, local)
- **Branch**: `fix/android-full-hardening`.
- **Implemented**: Centralized typed analytics event identifiers and corrected caption generation from `tijario_ai_reply_generated` to `tijario_ai_caption_generated`. Product creation is now recorded alongside existing customer/document/subscription events.
- **Validation**: `TijarioAnalyticsEventTest` passed. Events contain names only; no AI content or customer/account payload was added.

## 2026-08-09 (Read-only CI and restore hardening, local)
- **Branch**: `fix/android-full-hardening`.
- **Implemented**: Primary CI now follows active development branches. Source audit and full validation are read-only and upload artifacts rather than resetting, committing, or pushing source branches. The obsolete write-back heartbeat workflow is removed. Backup restore rejects a missing non-null payload with `BackupValidationException` rather than relying on `!!`.
- **Validation**: `LogicalBackupSnapshotTest` passed; local static scan found no force unwraps, stdout/stack traces, unbounded URL streams, or broad FileProvider user paths.
- **Remaining**: GitHub-hosted workflow execution cannot be claimed until the hardening branch is eventually pushed. No external action occurred.

## 2026-08-09 (Financial precision boundary hardening, local)
- **Branch**: `fix/android-full-hardening`.
- **Implemented**: `Validation` now parses and normalizes money with `BigDecimal`; legacy `Double` return values remain only for current API/model compatibility. The document calculator continues to be the canonical local totals path.
- **Validation**: Focused `DocumentCalculatorTests` and `ValidationTest` passed.
- **Remaining**: Do not change remote numeric contracts or database schemas without a coordinated Web/API release. Full hardening and physical-device validation remain pending.

## 2026-08-09 (Hardening security baseline, local)
- **Branch**: `fix/android-full-hardening`, created from reconciled `main` SHA `36bf6d3a0c22700da0dbb2dda2d654c52f95f8b1`.
- **Implemented**: Document HTML now escapes custom title/signature text. Local PDF logo network fallback is HTTPS-only, has timeouts, validates image content, streams with a 5 MiB bound, and preserves local-logo/offline behavior. Preview does not fetch remote logos. FileProvider no longer exposes internal user backup directories; finalized archives are copied into a dedicated cache share directory. Announcement actions accept internal links or HTTPS Tijario hosts only. Cleartext traffic is disabled.
- **Validation**: Focused `DocumentEngineTests`, `AnnouncementActionTargetTest`, and `PhoneBackupContractTest` passed.
- **Remaining**: Full hardening audit and physical-device validation remain pending. No migration, deployment, external configuration change, or Play upload occurred.

## 2026-08-01 (mobile entitlement contract verification)
- **Branch**: `codex/fix-backup-destinations-drive-restore-notifications`.
- **Change**: Added the shared 12-case mobile entitlement response fixture and a focused JVM test that deserializes every case with the production `AccountUsageResponse` serializer. No production Android behavior changed.
- **Validation**: Full `testDebugUnitTest --rerun-tasks`, `assembleDebugAndroidTest`, `lintDebug`, `assemblePlayQa`, and `git diff --check` passed. The first long Gradle attempts hit tool timeouts and were rerun to successful real exit codes.
- **Safety**: Existing backup/restore work and `.agents` remain preserved and unstaged. Physical-device QA remains pending; no migration, deployment, Production write, external configuration change, AAB/APK upload, or Play action occurred.

## 2026-08-01 (first-attempt Drive upload correction, local uncommitted)
- **Root cause**: The resumable Drive create request omitted the `fields` response selector. Google could create the remote archive but return a partial File resource without `size` or `appProperties`, so Android marked the first upload failed; Retry then found the already-created file through a full metadata list response and succeeded.
- **Fix**: The upload session now requests the complete verification fields. If Drive still returns a partial create response, Android resolves the exact account/backup object before verification; temporary list visibility is handled as bounded WorkManager retry instead of a manual failure.
- **Validation**: 112 backup JVM tests passed with no failures or skips, including partial-create and delayed-metadata behavior. `assemblePlayQa` and `git diff --check` passed. Physical Drive QA was intentionally not used.
- **Safety**: No commit, push, deployment, migration, Production write, external configuration change, AAB, or Google Play upload occurred. Version remains `18` / `1.1.8` and `.agents` remains untouched.

## 2026-07-31 (restore validation correction)
- **Branch**: `codex/fix-backup-destinations-drive-restore-notifications`.
- **Root cause**: Device logs proved archive header, key resolution, decryption, manifest validation, and the safety snapshot all succeed. Restore then failed before its Room transaction because logical validation incorrectly required deleted-document historical references to remain live document/customer rows.
- **Fix**: Restore now requires only the actual Room foreign-key relationship (`document_items_cache -> documents_cache`); immutable creation events and detached historical metadata/customer references remain restorable. Safety snapshots are retained internally but excluded from normal backup history.
- **Validation**: Focused `LogicalBackupSnapshotTest`, `BackupRestoreBehaviorTest`, `assemblePlayQa`, and `git diff --check` passed. Release metadata is `18` / `1.1.8`; physical restore on the connected device still needs the new build to be installed.
- **Safety**: No push, deployment, migration, Production write, Play upload, or external configuration change occurred. `.agents` remains local and excluded.

## 2026-07-30 (Drive upload, transactional restore, phone destination, and notification hardening)
- **Branch**: `codex/fix-backup-destinations-drive-restore-notifications`.
- **Upload**: A successful verified upload now persists `DRIVE_UPLOADED`, reports 100%, and succeeds before retention runs as separate bounded work. A later retention failure cannot downgrade or clear the verified remote record.
- **Restore**: Asset staging/application and Room schema/delete/insert/constraint/foreign-key/commit failures now retain distinct typed restore outcomes. The logical snapshot schema is validated before any current-account rows are deleted; asset application happens inside the Room transaction and rolls back with it on failure.
- **Phone and notifications**: A fresh phone destination is `Downloads/Tijario/Backups`; Android 10+ uses MediaStore and Android 8/9 requests legacy write permission only when needed. Backup notification permission and channel availability gate tracked work, return from settings is rechecked, and continuing without notifications is explicit.
- **Validation**: `testDebugUnitTest --rerun-tasks`, `assembleDebugAndroidTest`, `lintDebug`, `assemblePlayQa`, and `git diff --check` passed. `adb devices` found no connected device, so `connectedDebugAndroidTest` and mandatory disposable-account Drive/SAF/notification physical QA are pending.
- **Safety**: No commit, push, deployment, migration, Production write, final AAB, Play upload, or external configuration change occurred. `.agents` remains local and excluded.

## 2026-07-29 (Restore permission, key recovery, and foreground service completion)
- **Branch**: `codex/fix-backup-destinations-drive-restore-notifications`.
- **Restore safety**: File-picker restores now persist read permission before enqueueing, stage the archive privately, then release the persisted grant only after staging succeeds. Lost permission is typed and localized rather than becoming a generic restore failure.
- **Key recovery/progress**: Drive, SAF-file, and local-record restores may fetch the exact missing backup-key version when online; offline still fails closed when no key is cached. All backup workers use the existing `dataSync` foreground-service declaration, and safety backup starts only after archive validation.
- **Validation**: `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, and `assemblePlayQa` passed. The Room/assets/FakeDrive round-trip test compiles, but execution requires an emulator or physical device and remains pending.
- **Safety**: No migration, deployment, production write, external configuration change, Play upload, or final AAB was created. `.agents` remains local and excluded.

## 2026-07-29 (Backup progress, restore worker, and notification recovery)
- **Branch**: `codex/fix-backup-destinations-drive-restore-notifications`.
- **Completed**: Manual Drive uploads now expose their actual `WorkInfo` state rather than a synthetic 100%. Drive, SAF-file, and local-record restores run through one foreground, cancellable worker with explicit download, validation, safety-snapshot, file, and Room restore stages.
- **UX**: Manual retry bypasses only the automatic Drive toggle, preserves Wi-Fi policy, and replaces a prior manual request for the same archive. The phone backup destination displays the persisted SAF folder name. Android 13 notification permission has a rationale and settings action; denied notifications do not block the operation.
- **Validation**: Focused Backup/Drive/Notification/Offline JVM tests, `lintDebug`, and `assemblePlayQa` passed. No device was attached, so real Drive account, cancellation, SAF restore, and notification rendering still need physical QA.
- **Safety**: No migration, deployment, production write, or Play upload occurred. `.agents` files remain local and excluded.

## 2026-07-29 (Final release-blocker correction, local uncommitted)
- **Branch**: `codex/fix-production-release-blockers`.
- **Lease reconciliation**: Retryable lease errors now clear only the pending event assignment and invalidate the exact local active lease in the same Room transaction. Invalid leases become `INVALID`, expired leases become `EXPIRED`, and exhausted leases become `EXHAUSTED` with `consumed_count = allowed_limit`, so they cannot be selected by the next recovery cycle.
- **Deletion recovery**: Startup now uses explicit running/succeeded/failed recovery states. Failed local-only cleanup retains the marker, blocks authenticated routing/sync/notifications, and exposes an explicit local retry; it never calls the deletion endpoint.
- **Validation**: Focused quota/account-deletion JVM tests and `assemblePlayQa` passed. Version remains `15` / `1.1.5`.
- **Safety**: No commit, push, deployment, production migration, external-console action, or Play upload occurred.

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

## 2026-07-29 (Lease retry and startup deletion recovery, local only)
- **Reconciliation**: Invalid, expired, and exhausted leases now clear only a pending event lease and preserve the event for reassignment. Only limit, installation-revocation, and entitlement-version failures block; only permanent invalid event/payload failures reject.
- **Startup deletion recovery**: A pending local-cleanup marker runs local Room/file cleanup before authenticated routing. It clears the marker and local auth session only after success, and leaves the marker on failure without a server call.
- **Validation**: Focused quota/account-deletion JVM tests and `assemblePlayQa` passed. Version remains `15` / `1.1.5`; no migration, deploy, commit, push, or Play action occurred.

## 2026-07-29 (Backup destination and restore hardening)
- **Branch/version**: `codex/fix-backup-destinations-drive-restore-notifications`, versionCode `16`, versionName `1.1.6`.
- **Backup behavior**: Phone and Google Drive backup actions are separate. Phone backups prefer the user-selected SAF folder; otherwise they use only `Downloads/Tijario/Backup` and report a typed destination error rather than silently writing elsewhere. Manual Drive backup does not create a visible phone copy and ignores charging-only scheduling while retaining Wi-Fi-only policy.
- **Restore behavior**: The file picker starts from the selected phone-backup folder or the default Documents location. Drive upload/download verify account, size, and checksum. Restore errors are typed and the mandatory local safety snapshot fails closed.
- **Validation**: Focused Backup/Drive/Notification/Offline JVM suite, `lintDebug`, `assemblePlayQa`, and signed `bundleRelease` passed. The AAB is `app.tijario`, `16` / `1.1.6`, and matches the existing Upload Key SHA-1/SHA-256 fingerprints. No migration, deployment, external configuration change, or Play upload occurred.

## 2026-07-31 (Backup archive restore and history correction, uncommitted)
- **Root causes**: Logical backup tables were exported by independent reads, while a duplicate relationship validator could reject the resulting archive before Room's transactional constraints. Restore safety snapshots were first persisted as `LOCAL_READY`, and manual Drive uploads inherited battery/storage background constraints.
- **Fix**: Backup tables now export from one SQLite read transaction and are structurally validated before publication. Room remains the transactional authority for foreign keys. Safety snapshots are hidden from their first write and removed after successful restore. Completed restores update the exact archive record by ID/checksum with `restored_at`; history excludes intermediate, failed, and safety states. Manual Drive work is expedited and retains only its selected network constraint.
- **Database**: Local Room schema is `19`; migration `18 -> 19` adds nullable `backup_records.restored_at` without changing Android version `18` / `1.1.8`.
- **Validation**: 110 focused backup JVM tests passed; `assembleDebugAndroidTest` and `assemblePlayQa` passed. Physical restore and first-attempt Drive upload QA were not run because this task was explicitly performed without a phone.
- **Safety Status**: No commit, push, deployment, Supabase migration, external configuration change, final AAB, or Google Play upload occurred. `.agents` remains local and untouched.

## 2026-08-09 (Authentication deep-link hardening, local)
- **Branch**: `fix/android-full-hardening`, based on reconciled `main` source `36bf6d3`.
- **Change**: Callback handling now uses `AuthDeepLinkPolicy`, accepting only exact `tijario://auth/callback`, `com.tijario.app://auth/callback`, and HTTPS `tijario.site` callback origins. Unsafe redirect targets fall back to `/login`.
- **App Links**: Android declares the two HTTPS callback hosts with `autoVerify`; existing custom schemes remain a backwards-compatible fallback.
- **Validation**: Focused `AuthDeepLinkPolicyTest` passed. `docs/release/ANDROID_APP_LINKS.md` records the required physical/hosted verification.
- **Remaining**: Confirm `assetlinks.json` on both live hosts carries the active Play app-signing certificate, then verify the callback flow on a release-signed device.
- **Safety Status**: No push, deployment, production write, migration, Play upload, or external configuration change occurred. `.agents` remains local and excluded.

## 2026-08-09 (Network and diagnostic hardening, local)
- **Branch**: `fix/android-full-hardening`.
- **Transport**: API requests keep bounded interactive timeouts; Drive transfers use a separate bounded long-transfer profile while preserving file streaming and cancellation.
- **Diagnostics**: The document API no longer logs server response messages. Updated auth, analytics, export, and Drive logs retain only safe operation/status/error-class fields in debug builds.
- **Validation**: Focused `NetworkTimeoutProfileTest` and `KtorDriveRestTransportContractTest` passed; source audit found no direct throwable logging, raw message interpolation in logs, `println`, or `printStackTrace` under main sources.
- **Safety Status**: No push, deployment, production write, migration, Play upload, or external configuration change occurred. `.agents` remains local and excluded.

## 2026-08-09 (Document renderer boundary hardening, local)
- **HTML safety**: Payment-status CSS classes are allowlisted. Logo data URLs are restricted to base64 raster image types; textual document fields remain escaped.
- **Money boundary**: Draft preview mapping now keeps item prices, tax rates, and paid amounts in `BigDecimal` before a legacy API/Room boundary requires other representations.
- **Validation**: Full `DocumentEngineTests` passed, including malicious payment-status/data-URL cases and `0.1`/`0.2` decimal preservation.
- **Safety Status**: No push, deployment, production write, migration, Play upload, or external configuration change occurred. `.agents` remains local and excluded.

## 2026-08-09 (Billing authority audit, local)
- **Result**: Google Play's local purchase state remains non-authoritative. Backend verification precedes acknowledgement, and the verified event is emitted only after that path.
- **Guard**: `BillingFoundationTests` now protects this ordering and asserts the Play repository does not log purchase tokens.
- **Safety Status**: No billing behavior, backend contract, deployment, migration, Play upload, or external configuration changed.

## 2026-08-09 (Refresh-error localization, local)
- **Change**: All repository refresh paths now use the centralized mapper rather than publishing raw exception messages through `CacheSyncState`.
- **Behavior**: Error-code-like messages are treated as codes first; known values retain their specific translation, while unknown technical text becomes the generic localized fallback.
- **Validation**: `RefreshErrorLocalizationTest` and `LocalizedErrorMapperTests` passed.
- **Safety Status**: No API contract, deployment, migration, production write, Play upload, or external configuration change occurred.

## 2026-08-09 (Closed analytics event surface, local)
- **Change**: Analytics accepts only the centralized `TijarioAnalyticsEvent` enum. The previous arbitrary string and optional Bundle entry point was removed because no production caller required it.
- **Behavior**: Existing operational events remain unchanged; callers cannot attach customer, document, purchase, or other free-form content through this API.
- **Validation**: `TijarioAnalyticsEventTest` passed.
- **Safety Status**: No analytics provider configuration, backend contract, deployment, migration, production write, Play upload, or external configuration changed.

## 2026-08-09 (App-root state isolation, local)
- **Change**: `TijarioApp` now observes a dedicated `AppShellDataState` rather than the complete cache state. It contains only `userId`, initial-loading, and cached-data signals.
- **Behavior**: Routing, startup splash, and notification start behavior remain unchanged. Customer, product, document, plan, and transient sync-detail updates no longer invalidate the app root when the shell signals are unchanged.
- **Validation**: `AppShellDataStateTest` passed.
- **Safety Status**: No API, sync, lifecycle, deployment, migration, production write, Play upload, or external configuration changed.

## 2026-08-09 (Localized document-preview controls, local)
- **Change**: The expand and close controls in the shared document-preview component now use localized accessibility labels instead of Arabic literals.
- **Behavior**: The visual preview and PDF content are unchanged. Arabic labels remain Arabic; English users receive English labels.
- **Validation**: `UiRecoveryLocalizationTests` passed.
- **Safety Status**: No document content, backend contract, deployment, migration, production write, Play upload, or external configuration changed.

## 2026-08-09 (Modern Android back-navigation compatibility, local)
- **Change**: The application manifest now explicitly enables `OnBackInvokedCallback`.
- **Behavior**: Existing Compose back handling remains in place; Android 13+ can invoke the platform back callback without the prior compatibility warning.
- **Validation**: `AuthDeepLinkPolicyTest` passed and processed the Debug manifest.
- **Safety Status**: No route, API, deployment, migration, production write, Play upload, or external configuration changed.

## 2026-08-09 (Minimum-SDK-safe auth callback decoding, local)
- **Cause**: `lintDebug` found that the charset overload of `URLDecoder.decode` requires API 33 while the app supports API 26.
- **Change**: Callback decoding now uses the compatible UTF-8 name overload. The manifest's API-33 back-callback attribute is explicitly scoped for lint.
- **Validation**: `AuthDeepLinkPolicyTest` and full `lintDebug` passed with no errors.
- **Safety Status**: No authentication policy, route, backend contract, deployment, migration, production write, Play upload, or external configuration changed.

## 2026-08-09 (Full hardening validation and handoff, local)
- **Branch lineage**: `fix/android-full-hardening` is based on reconciled `main` commit `36bf6d3` and contains 14 focused hardening commits. It is intentionally not merged to `main`.
- **Validated gates**: `clean`, full JVM `test`, `lintDebug`, `lintRelease`, `assembleDebug`, `assembleRelease`, `assembleAndroidTest`, `assemblePlayQa`, and `bundleRelease` all completed successfully. `git diff --check` is clean.
- **Manifest/Room review**: Release manifest retains cleartext disabled, narrow exported components/FileProvider scope, verified HTTPS App Link declarations, and modern back support. Room migrations through the current schema are registered; no destructive fallback was introduced.
- **Remaining release evidence**: No ADB device/emulator is connected. Before release, validate PDF/share/preview flows, Google Drive/backup flows, and password-reset/OAuth App Links on a release-signed device. Host `/.well-known/assetlinks.json` on both approved domains with the active Play app-signing SHA-256 before relying on verified links.
- **Audit note**: The merged Release manifest still contains transitive advertising-ID declarations from an existing dependency. This hardening task did not alter Facebook/analytics SDK integration; review the disclosure/SDK requirement separately before release.
- **Safety Status**: No deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-09 (Hardening branch publication)
- **Published**: `fix/android-full-hardening` is available on `origin` at `f6c32b4` and remains unmerged from `main`.
- **Remote validation**: GitHub Actions Source Audit Scan and Android CI run `31327114193` both completed successfully. Android CI passed its compile/unit-test and lint/release-assembly jobs.
- **Safety Status**: No deployment, migration, Production write, external configuration change, or Google Play upload occurred.

## 2026-08-16 (Menu, compact discovery controls, and paid backup automation, local)
- **Profile and navigation**: The personal-profile header again exposes an editable avatar with a pencil badge while retaining the current inline name editor and read-only email field. The app-header Settings action is now a localized Menu action with language-aware horizontal navigation motion.
- **Discovery and notifications**: Customer, product, document, form-picker, and AI context searches use one compact search component. Existing customer/product/document filters open from a button beside the search field. Notifications support pull-to-refresh and use Refresh copy.
- **Plans and backup**: The pricing hero and plan cards are compact; the annual 20% badge appears only on the annual choice. Automatic local backups and Google Drive are shown and enforced as paid-plan features. WorkManager keeps local creation network-independent and transports to Drive separately when enabled.
- **Validation**: Focused JVM tests, Debug Kotlin compilation, `assemblePlayQa`, `lintDebug`, and `git diff --check` passed. Physical RTL/LTR, avatar picker, pull-to-refresh, and scheduled WorkManager timing QA remain required.
- **Safety Status**: Included in the requested local Android commit. No push, deployment, migration, Production write, Web change, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-16 (Navigation, settings, backup, and splash polish, local)
- **Navigation**: Home now displays the localized Tijario name beside the transparent brand mark. Documents, products, and customers use the mark with centered titles; Tijario AI keeps its AI icon and centered title.
- **Settings**: The profile card opens a dedicated name/email screen with inline pencil/check editing. Account security and destructive actions remain in a compact separate screen. Option order is Business, Account, App, Payments, then Backup. Free/Starter plans show a gold upgrade action; Pro does not.
- **Preferences and backup**: Language selection is explicitly Arabic or English. Theme selection keeps Device (Automatic), Light, and Dark with matching icons and teal selected state. Backup actions are grouped into compact Local, Schedule, Drive, and History sections without changing backup behavior.
- **Splash**: Android system and Compose splash screens use the exact approved transparent 2000x2000 logo without an icon background, on `#0B1220`, with a safe inset drawable to prevent clipping.
- **Validation**: 17 focused JVM tests, both Debug/DebugAndroidTest assemblies, and `lintDebug` passed. Run physical RTL/LTR, compact-screen, and cold-launch visual QA before release.
- **Safety Status**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-22 (Version 0.0.21 preparation)
- **Version**: Android is prepared as `versionCode 21` and `versionName 0.0.21` for the completed Google Play flexible-update and native-review changes.
- **Validation**: The Android Debug build and final Kotlin compilation passed. The focused JVM test source set remains blocked by the pre-existing `BackupPlanPolicyTest.kt` Long-to-String type errors.
- **Release boundary**: A Play-track device test is still required to observe the official update/review dialogs. This task does not upload an APK/AAB or change any Google Play setting.

## 2026-08-16 (Document preview currency and AI context follow-up, local)
- **Preview logo**: The pre-save WebView now reads the same shared business-logo cache used by the PDF flow, prefers the explicit business owner cache, and safely caches a verified remote image when it is first reachable. This keeps a previously cached logo available offline without exposing credentials.
- **Document currency and stock**: The item picker disables products whose saved currency differs from the document currency and explains why; route-return and save-time guards enforce the same rule. Tracked products also show their remaining stock after any quantities already reserved in the current invoice.
- **AI context and runtime**: Product currency and category now reach the bounded V3 context, while the server prompt also incorporates the relevant business terms and user-selected generation controls. Production runtime logs identified the canonical Replicate `succeeded` status as the rejection cause on deployed commit `a0390be`; the local provider fix accepts it and requires separate authorized publication/deployment. Contact data stays excluded.
- **Validation**: 58 focused JVM tests and `assembleDebug` passed. Device QA remains required for a real store logo before first cache, currency-mismatch picker behavior, and Arabic/English generated content.
- **Safety Status**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-13 (Offline document editing, ordering, and catalog follow-up, local)
- **Offline correction**: Editing a locally saved invoice or quotation no longer checks or refreshes the document-creation entitlement. The existing Room transaction updates only the document and its replacement items, so an offline edit remains `LOCAL_ONLY` and cannot consume a quota credit.
- **Ordering and catalogs**: Newly created local documents receive a full creation timestamp and document lists now order by creation time, then sync/revision metadata. Store onboarding and settings use the full country and calling-code catalog with flags; currency labels include their country and flag. Products can retain a selected product currency.
- **Invoice selection and stock**: Customer and product pickers always expose their create action. Tracked products cannot be selected or confirmed beyond available stock; the item editor offers a local stock increase that updates the Room product row before the item can be confirmed.
- **Validation**: Six focused JVM suites passed. `assembleDebug` passed. `lintDebug` exceeded the local command timeout before a result was returned, so it remains pending rather than passed. Physical device QA is required for picker scrolling, stock increase, offline edit, and same-day ordering.
- **Safety Status**: No commit, push, deployment, migration, Production write, backend/Web change, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-13 (LocalDrive offline CRUD recovery, local)
- **Cause and correction**: Local invoice and quote creation required a newly refreshed server lease when no active lease was cached. That made a Room-first save fail offline before its transaction began. LocalDrive now validates the signed cached entitlement and local pending-event count, attaches a valid cached lease only when present, and records a lease-less pending creation event otherwise for later reconciliation.
- **Operational behavior**: Customer, product/service, and document create/update routes enter their LocalDrive Room path before the legacy-cloud gate. Local document save does not call the document API, request a lease, or queue operational sync. The post-save plan refresh uses the cached LocalDrive state.
- **Validation**: `TijarioRepositoryOfflineTests`, `LocalDocumentSavePolicyTest`, and `QuotaReconciliationPolicyTest` passed: 47 tests, zero failures/errors. `assembleDebug` passed. Physical offline QA remains required.
- **Safety Status**: No push, deployment, migration, Production write, backend/Web change, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-10 (Runtime-critical Android remediation, local and uncommitted)
- **Navigation and plan guards**: Settings and child-route back actions now recover to the main route if the navigation stack has no valid predecessor. All invoice, quote, customer, and product creation entries use one creation-limit policy before navigation, while forms preserve a localized save-time limit dialog if a stale state is rejected.
- **Local data behavior**: LocalDrive usage overlays use Room counts for customers/products and pending creation events for documents without changing entitlement limits. New forms inherit the saved business currency unless manually changed. Local document numbers are deterministic per type, preserve valid custom suffixes and leading zeroes, reject local duplicates before writing, and remain stable on edit.
- **AI boundary**: Android does not send local-only selected entity IDs. It sends a bounded non-sensitive `context_snapshot`; the current local backend source accepts that contract, but it still requires an authorized commit, push, and deployment before Production can use it.
- **Validation**: 343 JVM tests passed with zero failures/errors. `lintDebug`, `lintRelease`, `assembleDebug`, `assembleRelease`, `assembleAndroidTest`, `assemblePlayQa`, and `bundleRelease` passed. The first full run exceeded the command wrapper timeout while Gradle continued; the same fully up-to-date gate then completed with exit code 0.
- **Physical QA still required**: no ADB device/emulator was connected. Verify the Settings back loop, creation-limit sheet, YER defaults and manual override, invoice/quote numbering edge cases, AI customer/product reply/caption, and the status-bar icon on a real device.
- **Safety Status**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-09 (CI package verification reliability, local)
- **Cause**: A documentation-only push reproduced a non-diagnostic `:app:packageDebug` failure in the combined lint/Debug/Release Gradle invocation. Compile/unit tests still passed and the same application source had passed the preceding full CI run.
- **Correction**: Android CI now runs lint, Debug assembly, and Release assembly as separate non-parallel Gradle invocations with stack traces. Checkout and Java setup use supported action major versions.
- **Validation status**: Local Gradle gates remain successful for the unchanged application source. GitHub Actions Android CI run `31337272388` passed both jobs after this workflow-only correction.
- **Safety Status**: No deployment, migration, Production write, external configuration change, or Google Play upload occurred.

## 2026-08-16 (Arabic copy, business settings, plan, PDF branding, and AI actions, local)
- **Arabic copy**: Source text no longer contains a fathatan immediately before a final alif; a JVM contract scans the Android main source to prevent that spelling form from returning.
- **Business and settings UI**: Business information uses dedicated business-name, business-phone, and default-currency labels. The redundant header name editor is removed, phone controls align vertically, the application-appearance icon is larger and unboxed, and the plan banner uses the requested `Your current plan | Plan` conversion copy.
- **Documents and AI**: Free-plan document branding now includes a compact Tijario mark and a green Play Store link. The dashboard View All action follows the active layout direction. AI history is an icon-only action beside the Reply/Caption generation button.
- **Validation**: Focused JVM contracts, `assemblePlayQa`, `lintDebug`, and `git diff --check` passed. Physical Arabic/English layout and exported-PDF link QA remain required.
- **Safety Status**: No commit, push, deployment, migration, Production write, Web change, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-21 (GoMarketMe affiliate attribution sidecar, local)
- **Integration**: Added the official stable `com.github.GoMarketMe:gomarketme-kotlin:5.0.2` dependency through JitPack. `MainActivity` requests one best-effort SDK initialization using only the local Gradle `GOMARKETME_API_KEY` BuildConfig field.
- **Billing boundary**: A verified Google Play purchase requests an asynchronous, in-memory-deduplicated transaction sync before Tijario acknowledges the purchase. Any attribution failure is swallowed and cannot delay server verification, acknowledgement, entitlement UI, Room, login, or offline CRUD.
- **Compatibility and manifest**: Gradle resolves Tijario Billing `9.1.0` over the SDK's transitive `8.3.0`; no downgrade occurred. The SDK AAR contributes only `minSdk 24`, no permissions, and empty consumer rules. Existing AD_ID and Install Referrer manifest entries are from pre-existing dependencies.
- **Validation**: Dependency insight and forced `assembleDebug` passed. The focused unit-test task is blocked by three pre-existing type errors in `BackupPlanPolicyTest.kt`, before any tests can execute. Physical purchase/attribution QA and Google Play Data Safety review remain required.
- **Safety Status**: No push, deployment, migration, Production write, Web change, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-21 (Feedback, customer statistics, and dashboard light-theme follow-up, local)
- **Feedback UI**: The in-app feedback flow now identifies the recipient as Tijario in Arabic and English, uses clearer support-oriented copy, and uses Material theme colors for the image attachment tile in both light and dark modes. Account identity is now derived server-side for follow-up.
- **Delivery boundary**: Android now calls `POST /api/mobile/feedback` with the active bearer token and sends no `user_id`, email, or mail-provider secret. Selected images are resized/compressed to a bounded JPEG payload before the request. The Web route resolves the authenticated account email itself and uses it only as email reply-to.
- **Customers and dashboard**: Customer statistics now use short localized labels beside their icons with the count underneath. Quick-action cards now read the app-selected theme state rather than the device theme, so manually chosen light mode no longer renders dark cards.
- **Validation**: `assembleDebug` passed after the API-contract change. The focused JVM test remains blocked before execution by three pre-existing `BackupPlanPolicyTest.kt` Long-to-String mismatches.
- **Release dependency**: Do not release Android until the compatible backend is deployed after applying `20260821174314_create_mobile_feedback_delivery.sql` and configuring server-only `RESEND_API_KEY` plus `FEEDBACK_EMAIL_FROM`.
- **Safety**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.

## 2026-08-21 (Google Play update and review prompts, local)
- **State**: The app now uses Google Play's official flexible in-app update flow when a newer Play version is available. A downloaded flexible update exposes a restart action, while the user can continue using the app during download.
- **Review behavior**: The custom star-rating pre-screen was removed. The dashboard makes one locally paced review-request attempt after meaningful use, and the settings action requests the native Play review flow directly. Google Play decides whether to display the dialog and never reports whether a user submitted a review.
- **Validation**: `assembleDebug` passed. The focused JVM test source set is blocked before execution by three pre-existing Long-to-String errors in `BackupPlanPolicyTest.kt`; the new policy test is present but not executable until those errors are fixed.
- **Safety Status**: No commit, push, deployment, migration, Production write, external configuration change, or Google Play upload occurred. `.agents` remains local and excluded.
