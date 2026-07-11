# Notifications Platform QA

## Automated

- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat lintDebug`
- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat bundleRelease` if signing and Firebase config are available.

## Manual

- Android 12 or lower.
- Android 13.
- Android 14.
- Android 15.
- Permission granted.
- Permission denied.
- Permission denied permanently.
- Foreground push.
- Background push.
- Killed app push.
- Arabic topic.
- English topic.
- Language switching.
- Offline inbox.
- Startup modal.
- Bell badge.
- Read receipt.
- Dismiss receipt.
- Mark all read.
- Logout/login.
- Two accounts on the same device.

## Expected Results

- No Firebase secret is present in Android.
- No Analytics, Crashlytics, or Firestore SDKs are added.
- User subscribes to one language topic only.
- Refused push permission does not block in-app announcements.
- Pending receipt operations sync after network returns.
