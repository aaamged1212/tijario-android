# Google Play Store Readiness Document

This document summarizes the steps taken to prepare the **Tijario** Android application for Google Play Console publication and the manual actions required by the owner.

---

## 1. Data Safety & Privacy Policy

### Privacy Policy URL
- **URL**: `https://tijario.site/privacy`
- This link is embedded in the application's **App Settings** screen.

### Account Deletion URL
- **URL**: `https://tijario.site/delete-account`
- This link is embedded in the application's **Account Settings** screen to comply with Google Play's policy requiring a web-based path to delete accounts.
- Users can also trigger immediate data and account deletion directly from the mobile app, which safely deletes their profile and auth record via backend secure APIs.

### Play Store Data Safety Questionnaire Answers
When filling the Play Console Data Safety questionnaire, declare the following:
1. **Personal Information**:
   - **Name**: Collected for profile setup (Optional/User provided).
   - **Email Address**: Collected and linked to the account for authentication (Required).
2. **Financial Information**:
   - *None* (No billing or payment details are collected directly in the app).
3. **App Info and Performance**:
   - **Diagnostics / Crash Logs**: Checked if using third-party crash reporting SDKs (not currently active, but good to declare if Firebase Crashlytics is added later).
4. **Data Handling**:
   - **Data in Transit**: All data is sent over secure HTTPS connection.
   - **Data Deletion**: Users can request their data to be deleted from both within the app and via the web deletion URL.

---

## 2. App Permissions & SDKs

### App Permissions (AndroidManifest.xml)
Only the absolute minimum permissions are declared:
- `android.permission.INTERNET`: Required to communicate with Supabase and Tijario APIs.
- `android.permission.POST_NOTIFICATIONS`: Required on Android 13+ only after the user accepts the notification explanation prompt.

### Firebase Cloud Messaging
- Firebase Cloud Messaging is used for general Tijario announcements.
- Firebase may process Firebase installation identifiers, device or other identifiers, app version, and push delivery metadata.
- Firebase Analytics, Crashlytics, and Firestore are not enabled.
- Users can deny push notification permission and still see in-app announcements from the backend.

### Google Play Billing
- Paid Android subscriptions must use Google Play Billing only.
- The official package name is `app.tijario`.
- Subscription products must be created after uploading a billing-enabled AAB:
  - `tijario_starter`
  - `tijario_pro`
  - `tijario_business`
- Each product must include base plans:
  - `monthly`
  - `yearly`
- The app displays localized prices from Google Play `ProductDetails`.
- The backend verifies purchase tokens before entitlement is granted.
- The app acknowledges a purchase only after backend verification succeeds.
- Google Play service-account credentials must never be added to the Android repository.

### FileProvider Configurations
- A secure FileProvider is configured in `@xml/file_paths` using `cache-path` (for cached PDFs) and `external-files-path` under restricted names to prevent directory traversal attacks or exposing private app folders.

### WebView Security
- WebView debugging is conditionally disabled in release builds using `WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)` in `MainActivity` to prevent inspection of loaded templates.

---

## 3. App Access & Review Instructions
When submitting the app for review in the Play Console under **App Access**:
1. Select **"All or some parts of my app are restricted"**.
2. Provide a working test account:
   - **Username/Email**: `test-reviewer@tijario.site` (or any valid test email you configure in Supabase)
   - **Password**: `TestReviewer123!` (or the password you configure)
   - **Instructions**: "Log in using the email authentication screen to access the billing dashboard, local document generation, and AI reply/caption helpers."

---

## 4. Play Store Listing Metadata Checklist

Prepare the following assets for the store listing:
- **App Icon**: 512 x 512 px PNG (Max 1MB).
- **Feature Graphic**: 1024 x 500 px PNG or JPEG.
- **Phone Screenshots**: At least 2 screenshots, 16:9 or 9:16 aspect ratio (Min 320px, Max 3840px).
- **Short Description**: (Max 80 characters) E.g., *"توليد الفواتير الذكية والردود التسويقية لمتجرك باستخدام الذكاء الاصطناعي."*
- **Full Description**: (Max 4000 characters) E.g., *"تجاريو هو مساعدك الذكي المخصص لمتاجر التجزئة والتجارة الإلكترونية لتوليد فواتير المبيعات باحترافية، وصياغة الردود الجاهزة للعملاء، وكتابة الكابشنات الإعلانية لمنصات التواصل الاجتماعي باستخدام أفضل نماذج الذكاء الاصطناعي."*
- **Category**: Business / Productivity.
- **Contact Email**: `support@tijario.site`

---

## 5. What has been Done & Console Checklists

### Completed Tasks
- [x] Set targetSdk & compileSdk to 35 (Play Store compliant).
- [x] Configured versionName to `"1.0.0"` and enabled minification / resource shrinking (R8).
- [x] Implemented ProGuard rules (`proguard-rules.pro`) for Room, Ktor, Supabase, and Serialization.
- [x] Disabled WebView debugging in production release.
- [x] Configured Privacy Policy web link in App Settings.
- [x] Added Web-based deletion request option in Account Settings.
- [x] Translated "Report this content" to Arabic and wired AI report generation API calls.

### Actions Needed inside Play Console (Manual Setup)
1. **Create Play Console App**: Set up the app as an "App" and "Free".
2. **Set up Store Presence**: Upload App Icon, Feature Graphic, Screenshots, and Descriptions.
3. **Fill questionnaires**:
   - Provide the Privacy Policy link (`https://tijario.site/privacy`).
   - Declare Data Safety answers (Email, Name).
   - Complete App Access credentials.
4. **App Signing**: Opt-in to Google Play App Signing (Play Console will generate/manage the production release key).
5. **Upload Bundle**: Run `.\gradlew.bat bundleRelease` to generate the `.aab` file from Android Studio or command-line, then upload it to your internal testing track.
