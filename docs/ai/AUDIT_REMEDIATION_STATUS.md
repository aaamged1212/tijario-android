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
- Current inspected branch head before this tracker update: `e500065765463f8ba5d3bea8036f442c466e38e9`.
- PR #3 remains open, mergeable, and draft against `main`.
- Android CI run 155 exposed the first actionable compiler failure: a missing `enqueueOutbox` block boundary caused the remainder of `TijarioRepository` to be parsed as local functions.
- Commit `a04ce2c78adcf0e6c3cdc89410b5d3d4c0cd585c` restores that boundary without changing outbox behavior.
- Android CI run 164 then confirmed an independent Kotlin expression error in the product-image persistence block of `FormScreens.kt`: an Elvis expression used an `if` without `else`.
- Commit `a8709548351bda0e57c23d95ca3af0143b35948b` replaces that expression with explicit replacement/deletion control flow and adds `ProductImagePersistenceTest` covering replacement precedence over deletion.
- Android CI run 176 reached Gradle and exposed a JVM signature clash in `AppRuntimeState`: the generated private setter for `authDeepLinkTarget` collided with `setAuthDeepLinkTarget(String?)`.
- Commits `80d2ca0023e86b51b5c6a336429425e6d9934f1b` and `cca62887906d274fb3f7bef6ffd1b871c18e8862` rename the explicit mutator to `updateAuthDeepLinkTarget` and update its only inspected caller without changing deep-link behavior.
- Commit `dc77a2ebcc0dc8c6935ab8e3bc252552fd80bf8e` adds `AppRuntimeStateTest`, covering update and one-time consumption of the pending auth target.
- Android CI run 188 identified the two failing JVM tests: `createDocumentLocal_doesNotMergeCustomerByWhatsapp` and `createDocumentLocal_usesSelectedCustomerIdWithoutCreatingDuplicateCustomer`. Both retained pre-quota fixtures that returned no offline lease, so the repository correctly rejected creation before their customer assertions.
- The affected fixtures now provide a valid active lease and retain their original customer identity assertions; production quota enforcement is unchanged.
- Commit `a1eeadd203c5c34dba1b19673cd29aafa3788bcb` preserves JUnit XML/HTML reports and failing-test entries for subsequent validation.
- Commit `b5ae608eb74c7c8be8d672f25b4ba97dfc761fac` removed the completed one-shot fixture-remediation workflow after applying the test repair.
- Android CI run 193 completed `compileDebugKotlin`, all JVM unit tests, and `assembleDebugAndroidTest` successfully.
- Run 193 also completed `lintDebug` and `assembleDebug`, but `assembleRelease` failed only at `packageRelease` because CI intentionally has no production `keystore.properties`; the release signing config was still created in an incomplete state.
- Commit `63b68336b2af1ac4707780270f7e49a262fc5ee8` now creates and assigns the release signing config only when `keystore.properties` exists. Production signing behavior is preserved when the file is present, while CI can verify the unsigned release artifact without embedding or fabricating credentials.
- Android CI run 197 confirms host compile, all JVM unit tests, and AndroidTest assembly pass. Its release-verification job progressed to `lintDebug` and found one remaining error: `MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)` requires API 29 while the project minSdk is 26.
- Commit `c76009082d237cd68fed96c869bd3dd9035f716e` marks the MediaStore downloads implementation as API 29+ and centralizes the Android 10 scoped-storage boundary without changing the existing legacy download path.
- Commit `e53e94026cf186e8fa14234a6868070558dd354c` adds `DocumentDownloadManagerTest`, covering API 28 legacy routing and API 29+ MediaStore routing.
- Android CI run 203 confirms compile, JVM tests, and AndroidTest assembly pass, but Lint could not infer that the helper predicate proves the API 29 boundary at its call site.
- Commit `0a4af216c81dade1920ffc494a981a7a30f33979` annotates the tested SDK predicate with `@ChecksSdkIntAtLeast`, preserving behavior while making the existing API guard visible to Android Lint.
- Android CI run 207 is the first clean full host baseline on the permanent workflow: `compileDebugKotlin`, all JVM unit tests, `assembleDebugAndroidTest`, `lintDebug`, `assembleDebug`, and unsigned `assembleRelease` all completed successfully.
- Deterministic source inspection confirms `MainActivity` no longer exposes the former process-wide compatibility facade; runtime language, theme, and auth deep-link state are owned directly by `AppRuntimeState`.
- Source audit currently reports no Kotlin force unwraps, no package-level PrintHelper usage, and no inline stdout/stacktrace usage.
- Commit `e500065765463f8ba5d3bea8036f442c466e38e9` restores `.github/workflows/android-ci.yml` to read-only verification after an unexecuted self-modifying cache-extraction job temporarily changed permissions to `contents: write`. The permanent workflow again contains only compile/test and lint/assembly jobs.
- The proposed `RemoteCacheReplacementPolicy` extraction has not been applied to source yet; no GitHub Actions run was associated with trigger commit `617df78415e90a01e7930020d73a4d28a12a482b`, so Phase 6 remains open rather than being credited without evidence.

## Phase 0 — Baseline and CI
- [x] Create isolated remediation branch.
- [x] Add Android CI workflow.
- [x] Make Gradle failures diagnosable in CI with focused plain-console output.
- [x] Persist focused Gradle failure diagnostics as downloadable workflow artifacts.
- [x] Persist JVM unit-test XML/HTML reports and failing-test entries when tests fail.
- [x] Record clean baseline CI result (Android CI run 207).

## Phase 1 — Release blockers
- [x] Fix Room migration 6 -> 7 (`next_retry_at` column and index).
- [x] Add migration test for the 6 -> 7 retry-column/index defect.
- [x] Make server-returned document number authoritative in Android cache.
- [x] Add document-number reconciliation unit tests.
- [ ] Execute migration instrumentation tests on an emulator/device. **BLOCKED:** the permanent GitHub Actions workflow currently has no emulator/device execution environment; host compile, JVM tests, lint, AndroidTest assembly, debug assembly, and release assembly are clean in run 207.

## Phase 2 — Account isolation
- [x] Scope local taxes, payment methods, signatures, terms, and document metadata by user.
- [x] Add Room migration for user-scoped local data (database version 15).
- [x] Add account-owned preference cleanup support.
- [x] Add migration/account-isolation instrumentation coverage.
- [ ] Execute two-account instrumentation coverage on an emulator/device. **BLOCKED:** no emulator/device job is currently available.

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
- [ ] Split large screen/repository files incrementally while preserving behavior. **NEXT:** apply the already-inspected remote-cache replacement-policy extraction directly to source with focused JVM coverage; do not rely on self-modifying CI.
- [x] Remove remediation-only workflows/scripts after final host validation; the branch retains only the permanent read-only Android CI workflow and product/test assets.

## Remaining external dependencies
The following cannot be completed safely inside this Android repository alone:
- Running emulator/device instrumentation coverage.
- Applying production Supabase migrations.
- Deploying the compatible Web/API implementation.
- Verifying production RLS, revision, offline-quota, and AI-idempotency semantics.
- Changing Google Cloud/Firebase API-key restrictions.
- Updating Play Console Data Safety, review credentials, and App Links association.
- Updating Meta developer-console configuration.

No production configuration has been changed, `main` has not been modified, and PR #3 must remain draft.
