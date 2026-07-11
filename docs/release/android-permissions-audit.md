# Android Permissions Audit

This document lists the permissions and tracking identifiers present in the release version of the Tijario Android app.

## Explicit App Permissions (Declared in Manifest)
- `android.permission.INTERNET`: Required to communicate with the online Supabase backend, fetch cached data, and send transactional requests.
- `android.permission.POST_NOTIFICATIONS`: Required to display local and Firebase Cloud Messaging push notifications for announcements.

## Merged Dependency Permissions
- `com.google.android.gms.permission.AD_ID`: Required by Meta/Facebook SDK and advertising services to read the Google Advertising Identifier (Advertising ID) for app install attribution and campaign measurement.
- `android.permission.ACCESS_ADSERVICES_AD_ID`: Required for Google Privacy Sandbox adservices API tracking and attribution where available.

## Facebook SDK Metadata Configuration
- `com.facebook.sdk.ApplicationId`: `@string/facebook_app_id` (Facebook App ID)
- `com.facebook.sdk.ClientToken`: `@string/facebook_client_token` (Facebook Client Token)
- `com.facebook.sdk.AutoLogAppEventsEnabled`: `true`
- `com.facebook.sdk.AdvertiserIDCollectionEnabled`: `true` (Enabled for campaign measurement/attribution)
- `com.facebook.internal.FacebookInitProvider`: Integrated ContentProvider for automated SDK initialization.

## Unused Permissions (Negative Audit)
- No `android.permission.CAMERA` (Photos/logo uploads use system picker content URI flow).
- No `android.permission.READ_CONTACTS` / `WRITE_CONTACTS`.
- No location permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).
- No microphone / SMS / Storage permissions.

## Play Console Compliance Implications
1. **Advertising ID Declaration**: Must select **YES** in the Google Play Console Advertising ID declaration.
2. **Data Safety**: Must disclose the collection of "Device or other IDs" (specifically Advertising ID) and share it with "Advertising or Marketing" partners (Meta/Facebook).
3. **Privacy Policy**: Disclose the use of Meta/Facebook App Events and collection of Advertising ID.
