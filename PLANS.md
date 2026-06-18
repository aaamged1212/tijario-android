# Tijario Android Living Plan

## Current Objective

Build the native Android Tijario MVP as a Kotlin, Jetpack Compose, Material 3 app that reuses the existing Tijario web/backend as the source of truth.

## Ordered Milestones

1. Repository and Android foundation.
2. Backend contract documentation and secure configuration boundaries.
3. Auth, session restoration, and onboarding.
4. Dashboard, account, plan, and usage surfaces.
5. Business settings and customer CRUD.
6. Documents list/detail, quote/invoice submission contracts, PDF retrieval, and WhatsApp share.
7. AI Reply and AI Caption through secure backend APIs.
8. Validation, repair, release-readiness documentation, and PR preparation.

## Dependencies

- Existing Tijario Supabase project public URL and publishable/anon key, supplied outside Git.
- Public Tijario API base URL, supplied outside Git.
- Secure backend endpoints for authoritative document, AI, and PDF operations.
- Local or Cloud Android toolchain: JDK, Android SDK, and Gradle.

## Risk Level

Medium. The Android client can be built independently, but true end-to-end completion depends on secure backend endpoints and an Android SDK toolchain.

## Acceptance Criteria

- Native Android project builds and launches.
- Arabic RTL is default.
- No server secrets are committed or embedded.
- Direct Supabase operations stay within existing RLS.
- Privileged operations use authenticated HTTPS backend APIs.
- Approved MVP routes exist and handle loading, empty, success, and error states.

## Validation Requirements

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:lintDebug`
- Git secret scan over tracked files.
- Manual Android UI pass for RTL navigation and core flows when an emulator/device is available.

## Completed Milestones

- Initial independent repository created on `main`.
- Milestone 1 started on `feature/native-mvp-foundation`.
- Android foundation files added: Gradle Kotlin DSL, app module, Compose theme, RTL shell, root navigation, MVP screen surfaces, data models, API contracts, and live documentation.
- Milestone 2 started: secure backend API client and repositories added for document, PDF, AI Reply, and AI Caption calls.
- Milestone 3 started: auth/onboarding/customer/business/document form surfaces and validation added, with Navigation Compose routes connected.
- Mission mandate recorded from the pasted text in `docs/mission-mandate.md`.
- Shared form state models added for auth, onboarding, customer, business settings, document, and AI forms.

## Incomplete Milestones

- Android toolchain validation.
- Supabase integration implementation.
- Backend API integration implementation.
- End-to-end product flows.

## Current Blocker

`java` and `gradle` are not on PATH. Android Studio JBR exists at `C:\Program Files\Android\Android Studio\jbr`, but no Android SDK path was found in common local locations. No Gradle wrapper exists yet, so Android build validation cannot run in this environment.

## Next Autonomous Action

Continue independent data/repository/UI work while toolchain setup remains blocked, then run the first compile pass and repair build errors as soon as Gradle wrapper and Android SDK are available.
