# Android 1.1.4 Closed-Testing Release Provenance

## Release identity

- Version code: `14`
- Version name: `1.1.4`
- Source branch: `codex/backup-drive-production-ready`
- Starting commit: `f29a42dbf7fea08547c4acdd5e55eff536274b25`
- Final release-preparation commit: the commit that adds this provenance document; its SHA is recorded in the release handoff.
- Previous Google Play boundary: code `13`, name `1.1.3`
- Historical reference commit: `f30dbc3eccdcec4bcd576a8a900e3cebd8933ecf`
- Historical reference handling: inspected only; not merged or cherry-picked.

## Manifest and privacy audit

- The merged Release manifest includes `com.google.android.gms.permission.AD_ID` and `android.permission.ACCESS_ADSERVICES_AD_ID` from `com.facebook.android:facebook-core:18.3.0`.
- `AndroidManifest.xml` was not modified for AD_ID.
- `com.facebook.sdk.AutoLogAppEventsEnabled=false` in source and merged Release manifest.
- `com.facebook.sdk.AdvertiserIDCollectionEnabled=false` in source and merged Release manifest.
- Google Play Advertising ID declaration must remain aligned with the final merged manifest. Data Safety answers still require manual confirmation against actual runtime collection behavior; the Meta advertiser-ID collection flag remains disabled.

## Build and signing evidence

- compileSdk: `36`
- targetSdk: `36`
- Release signing configuration: present and used by `assembleRelease` and `bundleRelease`.
- Local upload-key comparison: not independently verifiable because no expected certificate fingerprint is versioned in source.
- Release certificate SHA-1: `64:59:BD:F3:CC:B6:33:DF:A6:14:C6:10:52:DB:79:D7:4B:C9:36:6C`
- Release certificate SHA-256: `43:34:26:AE:65:FF:C7:48:56:7C:D7:D7:4A:BD:7F:4D:B2:C9:16:F1:90:E9:DB:28:78:3E:CD:BD:F3:55:7B:A3`

## Generated artifact

- AAB: `app/build/outputs/bundle/release/app-release.aab`
- Size: `10,883,067` bytes
- Generated UTC: `2026-07-24T02:03:46.6131439Z`
- SHA-256: `A7D9ECDD22D313AA113BBDD7316A1C51BC462F428EFBE3207287E2C2162719BC`
- Package and version verification: the paired signed release APK metadata and `aapt dump badging` report `app.tijario`, version code `14`, version name `1.1.4`, and target SDK `36`. The AAB has no directly readable output metadata file.

## Validation

- `:app:processReleaseMainManifest --console plain --no-daemon`: passed
- `:app:signingReport --console plain --no-daemon`: passed
- `testDebugUnitTest --console plain --no-daemon`: passed
- `assembleDebugAndroidTest --console plain --no-daemon`: passed
- `lintDebug --console plain --no-daemon`: passed
- `assembleDebug --console plain --no-daemon`: passed
- `assembleRelease --console plain --no-daemon`: passed
- `bundleRelease --console plain --no-daemon`: passed

## Release boundary

No Google Play upload was performed. No Supabase, Vercel, OAuth, Firebase, or production configuration was changed. Physical-device QA remains pending.
