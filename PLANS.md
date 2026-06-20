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
9. **[Completed]** Stabilization phase.
10. **[Completed]** Core MVP Completion phase:
    - Feature-oriented structure refactoring (documents, customers, products, ai, account).
    - Support multiple items in quotes/invoices with BigDecimal math.
    - Full document detail screen and PDF sharing/viewing flows.
    - Stable edit and owner-scoped deletion of customers and products.
    - Authentic limits enforcement and usage checks from period_month.
    - Structured AI Reply and Caption workflows with copy confirmations.

## Verification Requirements

- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat lintDebug`
- Git secret scan over tracked files
