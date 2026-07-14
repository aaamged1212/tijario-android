# Tijario Android Audit Remediation

Branch: `fix/full-audit-remediation`
Base: `main`

## Safety rules
- Never commit directly to `main`.
- Never merge this branch automatically.
- Keep the pull request in draft state.
- Preserve unrelated product behavior unless a verified defect requires a change.
- Backend repository `aaamged1212/tijario` is read-only for contract/schema review.
- Backend, Supabase production, Google Cloud, Firebase, Meta, Play Console, and Web/API deployment changes remain external.

## Status notation
- `[x]` implemented in the Android repository and covered by a committed test or deterministic source check.
- `[ ]` not yet implemented or not yet verified.
- `BLOCKED` requires an external action or an executable CI/device environment before it can be closed.

## Current validation state
- Current source head before this tracker update: `6100501913521f151a69b012858e6c95b3ae30bd`.
- PR #3 remains open, mergeable, and draft against `main`.
- Android CI run 155 exposed the first actionable compiler failure: a missing `enqueueOutbox` block boundary caused the remainder of `TijarioRepository` to be parsed as local functions.
- Commit `a04ce2c78adcf0e6c3cdc89410b5d3d4c0cd585c` restores that boundary without changing outbox behavior.
- Android CI run 164 then confirmed an independent Kotlin expression error in the product-image persistence block of `FormScreens.kt`: an Elvis expression used an `if` without `else`.
- Commit `a8709548351bda0e57c23d95ca3af0143b35948b` replaces that expression with explicit replacement/deletion control flow and adds `ProductImagePersistenceTest` covering replacement precedence over deletion.
- Android CI run 172 for the bot-authored repair commit ended as `action_required` before jobs executed.
- Commit `6100501913521f151a69b012858e6c95b3ae30bd` removes the completed self-modifying remediation job, restores read-only workflow permissions, and leaves only permanent compile/test/lint/assembly verification. Android CI run 174 is queued for this head.
- No clean compile/unit/lint/debug/release result is claimed until run 174 or a later executable run completes successfully.
- Deterministic source inspection confirms `MainActivity` no longer exposes the former process-wide compatibility facade; runtime language, theme, and auth deep-link state are owned directly by `AppRuntimeState`.
- Source audit currently reports no Kotlin force unwraps, no package-level PrintHelper usage, and no inline stdout/stacktrace usage.

## Phase 0 — Baseline and CI
- [x] Create isolated remediation branch.
- [x] Add Android CI workflow.
- [x] Make Gradle failures diagnosable in CI with focused plain-console output.
- [x] Persist focused Gradle failure diagnostics as downloadable workflow artifacts.
- [ ] Record clean baseline CI result. **IN PROGRESS:** product-image compile repair and its behavioral test are committed; permanent verification run 174 is queued.

## Phase 1 — Release blockers
- [x] Fix Room migration 6 -> 7 (`next_retry_at` column and index).
- [x] Add migration test for the 6 -> 7 retry-column/index defect.
- [x] Make server-returned document number authoritative in Android cache.
- [x] Add document-number reconciliation unit tests.
- [ ] Execute migration instrumentation tests and complete full build verification. **IN PROGRESS:** host verification must complete cleanly before device execution.

## Phase 2 — Account isolation
- [x] Scope local taxes, payment methods, signatures, terms, and document metadata by user.
- [x] Add Room migration for user-scoped local data (database version 15).
- [x] Add account-owned preference cleanup support.
- [x] Add migration/account-isolation instrumentation coverage.
- [ ] Execute two-account instrumentation coverage on an emulator/device. **BLOCKED:** no emulator/device job is currently available after host build verification.

## Phase 3 — Sync correctness
- [x] Schedule WorkManager when the first outbox operation is queued.
- [x] Normalize nullable/opaque server revision handling in Android contracts.
- [x] Preserve `amountPaid` and `productId` during pull-sync mapping.
- [x] Add cache freshness/revision guards and worker retry/auth recovery hardening.
- [ ] Validate sync behavior against the deployed backend contract. **BLOCKED:** production/Web/API deployment and production contract verification are external.

## Phase 4 — Usage and AI idempotency
- [x] Replace model/build-derived device identity with installation-scoped UUID storage.
- [x] Guard offline document creation behind a valid quota lease.
- [x] Add Android idempotency/fallback guards for AI generation paths.
- [x] Preserve authoritative usage values returned after retry/fallback paths.
- [ ] Verify production quota and AI idempotency semantics end-to-end. **BLOCKED:** compatible backend deployment is external.

## Phase 5 — Security and compliance
- [x] Remove tracked production Supabase/API/client values from `gradle.properties`; provide placeholders in `gradle.properties.example`.
- [x] Update Google Play readiness documentation to reflect SDKs, permissions, and external console checks.
- [x] Review Android-side Meta/Firebase analytics initialization defaults and document remaining console checks.
- [x] Document verified App Links migration prerequisites.
- [ ] Rotate/remove any still-active review credentials in external systems. **BLOCKED:** Play Console/backend ownership action required.
- [ ] Apply Firebase/Google API-key restrictions and Meta console changes. **BLOCKED:** external consoles.

## Phase 6 — Maintainability
- [x] Update README to describe the current Android application.
- [x] Reduce broad R8 keep rules where safely verifiable from source.
- [x] Remove response-body previews from user-facing diagnostics.
- [x] Replace reachable Kotlin force unwraps; source audit reports none remaining.
- [x] Move process-wide language/theme/deep-link ownership to `AppRuntimeState`.
- [x] Remove the remaining `MainActivity` compatibility-facade properties and call sites; deterministic source inspection shows `MainActivity` now only owns Android lifecycle/deep-link dispatch.
- [x] Replace package-level PrintHelper dependency and direct stdout/stacktrace diagnostics.
- [ ] Split large screen/repository files incrementally after behavioral tests exist and pass. **BLOCKED:** broad structural refactoring is unsafe until compile and behavioral tests execute cleanly.
- [ ] Remove remediation-only workflows/scripts after final validation so the branch retains only permanent CI and product assets. **IN PROGRESS:** the completed form-image self-modifying job has been removed; remaining remediation-only assets must be inventoried after clean validation.

## Remaining external dependencies
The following cannot be completed safely inside this Android repository alone:
- Running emulator/device instrumentation after host compile, tests, lint, and assembly are clean.
- Applying production Supabase migrations.
- Deploying the compatible Web/API implementation.
- Verifying production RLS, revision, offline-quota, and AI-idempotency semantics.
- Changing Google Cloud/Firebase API-key restrictions.
- Updating Play Console Data Safety, review credentials, and App Links association.
- Updating Meta developer-console configuration.

No production configuration has been changed, `main` has not been modified, and PR #3 must remain draft.
