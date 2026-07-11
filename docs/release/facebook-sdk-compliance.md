# Facebook SDK Compliance & Campaign Integration

This document details the configuration and tracking events for the Facebook/Meta SDK integrated into the Tijario Android app.

## SDK Dependency
- **Artifact**: `com.facebook.android:facebook-core:18.3.0`

## Manifest Metadata Configuration
- `com.facebook.sdk.ApplicationId`: `@string/facebook_app_id` (Mapped to Facebook app registration)
- `com.facebook.sdk.ClientToken`: `@string/facebook_client_token` (Mapped to Client Token in Meta App Dashboard)
- `com.facebook.sdk.AutoLogAppEventsEnabled`: `true`
- `com.facebook.sdk.AdvertiserIDCollectionEnabled`: `true` (Enables Advertiser ID retrieval for attribution/App Events tracking)

## Merged Providers
- `com.facebook.internal.FacebookInitProvider` is active to auto-start SDK tracking on application launch.

## Integrated Operational Events
The app tracks key actions as App Events using `AppEventsLogger`:
1. `tijario_invoice_created`: Triggered upon invoice generation.
2. `tijario_quote_created`: Triggered upon quote generation.
3. `tijario_customer_created`: Triggered when a new customer is created.
4. `tijario_ai_reply_generated`: Triggered when using AI reply/caption generator.
5. `tijario_subscription_started`: Triggered when initiating a subscription flow.

## Compliance Requirements
1. **Advertising ID declaration**: Must be enabled as `YES` on Google Play.
2. **Privacy Disclosures**: Disclosed in the Privacy Policy (/privacy) and Terms (/terms).
3. **Data safety**: Disclose device identifiers sharing with Meta.
