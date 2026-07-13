# Tijario Android

Native Android client for Tijario, built with Kotlin, Jetpack Compose, Material 3, Room, WorkManager, Supabase authentication, and the secure Tijario mobile API.

## Product status

The application is implemented and used for controlled testing. It supports Arabic RTL and English, customers, products and services, quotes, invoices, local PDF export, AI reply/caption tools, notifications, subscriptions, and a partially offline-capable synchronization layer.

The Android repository is not the source of truth for privileged business rules. The private `aaamged1212/tijario` web/backend repository owns the production API contracts, Supabase schema, entitlements, document numbering, billing verification, and server-side secrets.

## Build requirements

- JDK 21
- Android SDK 35
- Gradle wrapper included in the repository
- Android Studio compatible with the configured Android Gradle Plugin

Public client configuration is supplied through Gradle properties. Never commit service-role keys, database passwords, signing credentials, Google Play service-account files, Replicate credentials, or backend secrets.

## Verification

Run the same checks used by CI:

```bash
./gradlew --no-daemon compileDebugKotlin testDebugUnitTest assembleDebugAndroidTest
./gradlew --no-daemon lintDebug assembleDebug assembleRelease
```

Room schemas are exported under `app/schemas` and historical migrations must be covered by instrumentation tests before changing the database version.

## Architecture boundaries

- `data/remote`: authenticated mobile API and Supabase clients.
- `data/local`: Room entities, DAO, schema migrations, and local cache.
- `data/repository`: cache, remote orchestration, synchronization, and usage reconciliation.
- `features`: Compose screens and feature-specific services.
- `domain`: financial calculations and reusable business helpers.

The server is authoritative for official document numbers, entitlement decisions, purchase verification, usage counters, and conflict revisions. Android may show local previews, but it must reconcile them with server responses.

## Release process

1. Apply and verify compatible backend/Supabase changes first.
2. Confirm API contract compatibility and idempotency.
3. Pass compile, unit tests, Room migration tests, lint, debug assembly, and release assembly.
4. Complete real-device QA in Arabic and English.
5. Build the AAB from a known commit and record its SHA.
6. Update Google Play Data Safety, SDK declarations, app-access credentials, and release notes.

Do not upload an existing local AAB whose source commit cannot be verified.

## Security rules

- Never commit `.env`, `local.properties`, keystores, or signing passwords.
- Keep account-owned Room data scoped by authenticated user.
- Clear account-owned local data and generated files during logout/account deletion.
- Do not expose response bodies or tokens in user-facing diagnostics or production logs.
- The backend repository may be reviewed for contracts, but production changes require a separate reviewed deployment.
