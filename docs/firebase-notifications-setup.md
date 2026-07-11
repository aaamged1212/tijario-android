# Firebase Notifications Setup

## Project

- Firebase project ID: `tijario-1`
- Android package: `app.tijario`
- FCM API V1: enabled

## Android File

The real file must stay at:

`app/google-services.json`

Do not create a fake file. Do not add Firebase Admin service account JSON to Android.

## Dependencies

The app uses:

- Google Services Gradle plugin
- Firebase BOM
- Firebase Messaging

It does not use Firebase Analytics, Crashlytics, or Firestore.

## Topics

- Arabic: `tijario_announcements_ar`
- English: `tijario_announcements_en`

The app subscribes to only one topic at a time based on the current app language.

## Backend Env

The backend needs server-only variables:

- `FIREBASE_PROJECT_ID`
- `FIREBASE_CLIENT_EMAIL`
- `FIREBASE_PRIVATE_KEY`

Do not put those values in Android, Git, or public env vars.
