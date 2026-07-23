# Google Play Store Readiness

This document tracks the Android release metadata and the manual Google Play Console actions required for Tijario. It must be reviewed against the exact AAB commit before every submission.

## 1. Current Android build

- Package: `app.tijario`
- `compileSdk`: 36
- `targetSdk`: 36
- Current source configuration: `versionCode 11`, `versionName 1.1.1`
- Release minification and resource shrinking: enabled
- WebView debugging: enabled only for debug builds

Do not reuse these version values after a new release commit. Confirm them directly from `app/build.gradle.kts` before upload.

## 2. Privacy and deletion

- Privacy policy: `https://tijario.site/privacy`
- Account deletion page: `https://tijario.site/delete-account`
- The app also exposes authenticated account deletion through the secure backend API.

The privacy policy and Data Safety form must describe the SDKs and data behavior of the exact release build, not planned future SDKs.

## 3. Data Safety review

At minimum, review and declare:

- Name and email used for account creation and authentication.
- Business profile, customers, products, quotes, invoices, and user-generated AI inputs stored for core app functionality.
- Firebase Cloud Messaging installation/device identifiers and push-delivery metadata.
- Google Play Billing purchase/subscription identifiers processed by Google Play and verified by the Tijario backend.
- Diagnostics only when a diagnostics or crash SDK is actually enabled in the release.

Firebase Analytics, Crashlytics, and Firestore are not currently declared as active application features. Do not mark diagnostics merely because they may be added later.

The Meta SDK is present, but automatic app-event logging and Advertising ID collection are disabled in the Android manifest. Any future explicit analytics event collection must be reflected in the privacy policy, consent flow, and Data Safety form before release.

## 4. Permissions

The current manifest declares:

- `android.permission.INTERNET` for Supabase and Tijario APIs.
- `android.permission.POST_NOTIFICATIONS` for Android 13+ notifications after user permission.
- `android.permission.WRITE_EXTERNAL_STORAGE` with `maxSdkVersion=28` for legacy PDF export on Android 9 and older.

Users can deny notification permission and continue using in-app announcements.

## 5. SDK and security checklist

### Android 16 / API 36 phone QA

- Confirm edge-to-edge content and bottom navigation do not overlap status or navigation bars.
- Verify Compose back handling, RTL sheets/dialogs, split-screen resizing, sharing intents, FileProvider PDF sharing, and Google Identity activity-result flows.
- Verify foreground backup work and notification permission behavior on Android 13+ and Android 16.
- Do not add broad storage permissions or `MANAGE_EXTERNAL_STORAGE`; scoped MediaStore and SAF remain the supported export paths.

### Firebase Cloud Messaging

- Used for Tijario announcements.
- Firebase may process installation/device identifiers, app version, and delivery metadata.
- Verify that the Firebase API key is restricted to the Android package and approved signing certificate fingerprints in Google Cloud Console.
- Verify Firebase rules and App Check configuration where applicable.

### Google Play Billing

- Android paid subscriptions use Google Play Billing.
- Product IDs:
  - `tijario_starter`
  - `tijario_pro`
  - `tijario_business`
- Expected base plans: `monthly` and `yearly`.
- Prices are displayed from Google Play `ProductDetails`.
- Purchase tokens are verified by the backend before entitlement is granted.
- Service-account credentials must never be committed to this repository.

### FileProvider

The provider is non-exported and grants temporary URI permissions for configured PDF paths only.

### OAuth callback

The current build still supports custom URL schemes for authentication callbacks. Migrating authentication to a verified HTTPS Android App Link requires:

1. A stable HTTPS callback under a Tijario-owned domain.
2. `assetlinks.json` containing the production package and signing SHA-256 fingerprint.
3. Matching Supabase/Google OAuth redirect configuration.
4. Real-device login and recovery testing before removing the custom schemes.

This remains a coordinated external configuration task and must not be changed only in Android.

## 6. App Access for review

In Play Console, select that some parts of the app require authentication and enter a working reviewer account directly in the protected Play Console field.

Repository files must contain placeholders only:

- Email: `REVIEW_ACCOUNT_EMAIL`
- Password: `REVIEW_ACCOUNT_PASSWORD`
- Instructions: Sign in with email/password to review documents, customers, products, AI tools, subscriptions, and account settings.

Never commit the active reviewer password. Rotate any previously active credential that appeared in Git history.

## 7. Store listing assets

Prepare and verify:

- 512 × 512 PNG app icon.
- 1024 × 500 PNG/JPEG feature graphic.
- At least two current phone screenshots in Arabic or English.
- Short description within 80 characters.
- Full description within 4,000 characters.
- Category: Business or Productivity.
- Support contact: `support@tijario.site`.

Screenshots and descriptions must match the functionality in the submitted build.

## 8. Release gate

Before uploading an AAB:

- [ ] Compatible Supabase migrations applied and verified.
- [ ] Compatible Web/API version deployed and smoke-tested.
- [ ] Android compile and unit tests pass.
- [ ] Historical Room migration tests pass.
- [ ] Android lint passes.
- [ ] Debug and release assemblies pass.
- [ ] Real-device QA completed in Arabic and English.
- [ ] Document numbering, sync retries, PDF export, billing, login, logout, and account deletion verified.
- [ ] Data Safety answers reviewed against the final manifest and dependency list.
- [ ] Privacy policy reviewed against active SDK behavior.
- [ ] Reviewer account configured only in Play Console and confirmed working.
- [ ] `versionCode` incremented.
- [ ] AAB source commit SHA recorded.
- [ ] ProGuard/R8 mapping retained for the release.

Generate the bundle from the reviewed commit with:

```bash
./gradlew --no-daemon bundleRelease
```

Upload only the artifact produced from the recorded commit. Do not upload an older local AAB whose source cannot be identified.
