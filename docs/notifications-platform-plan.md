# Tijario Notifications Platform Plan

## Current-State Audit

- Repository: `tijario-android`
- Base branch selected: `feature/ai-sales-assistant-v3`
- Working branch: `feature/notifications-platform-android`
- Base commit: `1125dd3 inoice name change`
- Local status at start: clean working tree.
- Android package name: `app.tijario`.
- Real Firebase config file exists at `app/google-services.json`; this task will not create or replace it.
- Existing app uses Jetpack Compose, Ktor, Room v8, WorkManager, and Supabase auth.
- Existing backend API client is `app/src/main/java/app/tijario/data/remote/BackendApiClient.kt`.
- Existing Room database is `TijarioDatabase` with exported schemas and explicit migrations.
- Existing top-level app shell is `TijarioApp.kt`.

## Existing Architecture

- UI reads core data from Room-backed state where possible.
- Network calls go through Ktor and `BackendApiClient`.
- Background sync uses WorkManager.
- Auth state is centralized in view models and Supabase session handling.
- Localization uses `Localization.kt` with Arabic and English maps.

## Database Plan

- Add Room entities for cached announcements, local user announcement state, and pending receipt sync operations.
- Add a forward Room migration from the current version.
- Do not use destructive migrations.
- Keep announcement state isolated by `user_id`.

## Backend Plan

- The Android app will call the backend mobile APIs:
  - `GET /api/mobile/announcements/bootstrap`
  - `POST /api/mobile/announcements/receipt`
  - `POST /api/mobile/announcements/read-all` if implemented.
- The backend remains the source of truth and validates ownership server-side.

## Android Plan

- Add Firebase Messaging dependencies using the existing version catalog.
- Add Google Services plugin without Analytics, Crashlytics, or Firestore.
- Add `android.permission.POST_NOTIFICATIONS`.
- Add `TijarioFirebaseMessagingService`.
- Add `NotificationTopicManager`.
- Add notifications repository and view model under `features/notifications`.
- Add a bell icon with unread badge beside settings in authenticated app UI.
- Add notifications inbox and detail UI.
- Add startup announcement modal once per announcement.
- Add settings controls for notification permission/push preference.
- Add deep link handling for `tijario://announcements/{announcement_id}`.

## Firebase Setup

- Subscribe to one topic according to app language:
  - Arabic: `tijario_announcements_ar`
  - English: `tijario_announcements_en`
- On language change, unsubscribe from the previous topic and subscribe to the new one.
- On logout, unsubscribe from the current topic.
- Do not send FCM tokens to backend in this MVP.

## Security Model

- No Firebase service account or server secret is added to Android.
- FCM payload contains only safe announcement metadata.
- Deep links are parsed defensively and limited to trusted internal destinations.
- Cached read state is scoped by user id.

## Localization Plan

- Add all new strings to the existing localization map.
- Keep Arabic RTL and English LTR behavior.
- Store both Arabic and English announcement content locally so language switching works offline.

## Testing Plan

- Run:
  - `.\gradlew.bat testDebugUnitTest`
  - `.\gradlew.bat lintDebug`
  - `.\gradlew.bat assembleDebug`
- Run release bundle only if signing and Firebase config are available.
- Manual QA must cover notification permission states, foreground/background/killed push states, offline inbox, startup modal, language switching, logout/login, and multiple accounts.

## Rollback Plan

- Revert Android notification commits.
- Release a build without topic subscription if push must be disabled.
- Keep backend announcements stored but archive active announcements if needed.

## Manual External Setup Required

- Firebase project is already created as `tijario-1`.
- Android app package is already configured as `app.tijario`.
- Keep `app/google-services.json` local and real.
- Backend still needs Firebase Admin env vars:
  - `FIREBASE_PROJECT_ID`
  - `FIREBASE_CLIENT_EMAIL`
  - `FIREBASE_PRIVATE_KEY`
- Real-device FCM topic delivery must be verified after backend env vars are configured.
