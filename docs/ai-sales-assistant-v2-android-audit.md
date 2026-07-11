# Tijario Android AI Sales Assistant V2 Audit

Date: 2026-06-22

## Scope

This audit covers the current Android AI reply and AI caption implementation, the current API contract layer, the ViewModel and UI wiring, and the gaps required for V2 alignment with the backend.

## Current State

Current Android AI functionality is embedded in the main UI flow and is not yet separated into a dedicated V2 domain layer.

Inspected entry points:

- `app/src/main/java/app/tijario/ui/screens/CoreScreens.kt`
- `app/src/main/java/app/tijario/ui/state/TijarioDataViewModel.kt`
- `app/src/main/java/app/tijario/data/repository/AiRepository.kt`
- `app/src/main/java/app/tijario/data/remote/ApiContracts.kt`
- `app/src/main/java/app/tijario/data/remote/BackendApiClient.kt`
- `app/src/main/java/app/tijario/data/model/TijarioModels.kt`

## Current Android Contract Problems

- The AI UI is embedded inside `CoreScreens.kt` rather than a dedicated AI ViewModel and screen architecture.
- The current screens use local mutable Compose state heavily for AI inputs and results.
- The request contracts still use V1-style fields such as `case_type` and `caption_type` instead of a shared versioned V2 request envelope.
- The current request models do not include `schema_version` or `client_request_id`.
- The current reply flow does not require a customer message unless a quick case is chosen in a strict way.
- The current UI and contract do not model refinement or reporting.
- The current API response contracts do not preserve `generation_id`.

## Current Android UX and Data Wiring

- Current AI data is not isolated from the rest of the document and dashboard state.
- The AI forms do not yet have a dedicated usage/reporting model.
- Product and customer context are not resolved through a dedicated AI context layer.
- The current AI flows do not expose a clear V2 state machine for loading, success, refinement, or report submission.

## Current Backend Alignment Gaps

- The Android request enums do not yet match the desired V2 sales-focused enums exactly.
- The current backend-facing strings are hardcoded in the app layer and may drift from the server contract.
- The app currently calls older AI endpoints through `AiRepository` and `BackendApiClient`.
- There is no versioned `/api/mobile/ai/v2/*` contract yet on the Android side.

## Current Usage and Security Observations

- `TijarioDataViewModel` refreshes plan usage after successful AI operations, but that usage model is still tied to the existing repository flow.
- The app derives server interaction from authenticated backend APIs rather than exposing secrets in the UI layer.
- The mobile client should continue to avoid any service-role key, backend secret, or privileged Supabase credential.
- The app should continue to use authenticated user context only, with ownership enforced by the backend.

## Missing V2 Capabilities

- Missing dedicated AI ViewModel and state model.
- Missing shared versioned request/response contract.
- Missing generation ID preservation.
- Missing refinement flow.
- Missing report flow.
- Missing business/customer/product context resolver on the client contract side.
- Missing explicit migration path from V1 to V2.

## Target Android Architecture

The Android AI experience should move to a dedicated AI V2 layer with:

- typed request models
- typed result models
- dedicated AI screen state
- generation ID retention
- report submission
- refinement submission
- usage refresh after successful generation or refinement
- compatibility adapters for older calls only while V1 remains enabled

## Exact Migration Approach

1. Add V2 request and response models.
2. Add V2 repository methods.
3. Add V2 API routes on the backend.
4. Move AI UI into a dedicated state flow if needed.
5. Preserve current UI until the new V2 path is verified.
6. Switch the AI forms to the V2 payloads.
7. Retire V1 screens and contracts only after parity is proven.

## Risks

- Keeping V1 and V2 side by side without a single shared contract will create drift.
- Embedding AI logic in `CoreScreens.kt` makes regression risk high.
- The current result shape is not strong enough for reporting and refinement flows.

