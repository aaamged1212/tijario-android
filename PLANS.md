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
9. **[Completed]** Stabilization phase:
   - Structured navigation around authenticated and unauthenticated graphs.
   - Centralized authentication state coordinator and state resolver.
   - Fixed dashboard stats revenue logic.
   - Enforced 8-digit OTP verification constraint.
   - Separate document and payment status mappers.
   - Unified ErrorMapper to handle network, timeouts, and ApiResult codes.
   - Validation via Gradle compilation, unit tests, and lint checks.

## Verification Requirements

- `.\gradlew.bat assembleDebug` -> Passed.
- `.\gradlew.bat testDebugUnitTest` -> Passed.
- `.\gradlew.bat lintDebug` -> Passed.
- Git secret scan -> Passed.
