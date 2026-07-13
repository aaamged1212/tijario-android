# Tijario Android Audit Remediation

Branch: `fix/full-audit-remediation`
Base: `main`

## Safety rules
- Never commit directly to `main`.
- Never merge this branch automatically.
- Keep each remediation phase in a separate, reviewable commit.
- Preserve existing product behavior unless a verified defect requires a change.
- Run compile, unit tests, lint, and debug/release assembly after each phase.
- Backend, Supabase production, Google Cloud, Firebase, Meta, and Play Console changes remain external blockers until verified in their own environments.

## Phase 0 — Baseline and CI
- [x] Create isolated remediation branch.
- [x] Add Android CI workflow.
- [ ] Record clean baseline CI result.

## Phase 1 — Release blockers
- [ ] Fix Room migration 6 -> 7 (`next_retry_at`).
- [ ] Add migration tests for historical upgrade paths.
- [ ] Make server-returned document number authoritative in Android cache.
- [ ] Add tests for document number reconciliation.

## Phase 2 — Account isolation
- [ ] Scope local taxes, payment methods, signatures, terms, and document metadata by user.
- [ ] Add Room migration for user-scoped local data.
- [ ] Clear all account-owned local data on logout/account deletion.
- [ ] Add two-account isolation tests.

## Phase 3 — Sync correctness
- [ ] Schedule WorkManager for the first outbox operation.
- [ ] Unify the `serverRevision` contract.
- [ ] Preserve `amountPaid` and `productId` during pull sync.
- [ ] Add cache freshness/revision validation.
- [ ] Harden worker retry and authentication recovery.

## Phase 4 — Usage and AI idempotency
- [ ] Replace model/build-based device ID with installation UUID.
- [ ] Deny offline document creation without a valid lease.
- [ ] Add idempotency to AI generation and restrict legacy fallback.
- [ ] Return authoritative usage values after fallback/retry.

## Phase 5 — Security and compliance
- [ ] Remove real review credentials from public documentation if active.
- [ ] Align Google Play Data Safety documentation with actual SDKs and permissions.
- [ ] Review Meta auto events and advertising ID collection defaults.
- [ ] Document Firebase API-key restriction checks.
- [ ] Prepare verified App Links migration plan.

## Phase 6 — Maintainability
- [ ] Update README to match the current application.
- [ ] Reduce overly broad R8 keep rules where verified safe.
- [ ] Remove response-body previews from user-facing diagnostics.
- [ ] Replace unsafe force unwraps in reachable flows.
- [ ] Split large screen/repository files incrementally after behavioral tests exist.

## External dependencies
The following cannot be completed safely inside this Android repository alone:
- Applying production Supabase migrations.
- Deploying the compatible Web/API implementation.
- Verifying production RLS and API revision semantics.
- Changing Google Cloud/Firebase API-key restrictions.
- Updating Play Console Data Safety and review credentials.
- Updating Meta developer-console configuration.

Android changes that depend on these systems must remain guarded and must not assume an undeployed backend contract.
