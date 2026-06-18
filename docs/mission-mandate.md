# Mission Mandate

## Active Goal

The pasted mission text is the governing product and engineering goal for this repository.

The repository should move toward a complete, validated native Android MVP for Tijario, not a prototype and not a planning-only artifact.

## Operating Interpretation

- Build the app as a fully native Android application.
- Use Kotlin, Jetpack Compose, Material 3, Gradle Kotlin DSL, Navigation Compose, ViewModel, StateFlow, Coroutines, Kotlin Serialization, Supabase Kotlin where appropriate, and Ktor for secure backend APIs.
- Keep Arabic RTL as the default experience.
- Use one Android app module unless a concrete technical need proves otherwise.
- Treat the existing Tijario web/backend as the source of truth.
- Keep the web/backend repository read-only unless an explicit backend task authorizes changes.
- Use direct Supabase access only where existing RLS safely permits it.
- Use authenticated HTTPS backend APIs for privileged operations.
- Never add service-role keys, Replicate tokens, database passwords, PDF secrets, signing credentials, or backend secrets to Android.
- Do not use Flutter, React Native, Expo, Capacitor, Cordova, Kotlin Multiplatform, or WebView as the application.
- Avoid feature creep outside the approved Tijario MVP.

## Completion Standard

The MVP is not complete until the Android app builds, launches, validates Arabic RTL, supports the approved flows, uses the existing backend securely, passes available validation, contains no committed secrets, and has review-ready documentation and pull request status.

## Current Practical Constraint

The active work continues inside this repository, but local build validation is blocked until a Gradle wrapper and Android SDK are available.
