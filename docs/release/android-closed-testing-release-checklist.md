# Android Closed Testing Release Checklist

Checklist to follow before uploading a release bundle to Google Play Closed Testing:

- `[ ]` Confirm `versionCode` (currently `11`) and `versionName` (currently `1.1.1`) inside `app/build.gradle.kts`.
- `[ ]` Run unit tests: `.\gradlew.bat testDebugUnitTest`
- `[ ]` Assemble debug build: `.\gradlew.bat assembleDebug`
- `[ ]` process release manifest: `.\gradlew.bat :app:processReleaseMainManifest`
- `[ ]` Verify `com.google.android.gms.permission.AD_ID` is present in generated manifest.
- `[ ]` Verify `android.permission.ACCESS_ADSERVICES_AD_ID` is present in generated manifest.
- `[ ]` Verify `com.facebook.sdk.AdvertiserIDCollectionEnabled` value is `true`.
- `[ ]` Generate release App Bundle (AAB):
  ```powershell
  .\gradlew.bat :app:bundleRelease
  ```
- `[ ]` Complete Google Play Advertising ID declaration as **YES** in Play Console.
- `[ ]` Update Play Store Data Safety Questionnaire using `docs/release/google-play-data-safety.md`.
- `[ ]` Ensure Privacy Policy URL in Play Console is set to:
  `https://tijario.site/privacy` (Web production URL).
