# Implementation Phases

Status: historical PR-boundary plan. The local completion branch implements the Android foundation and local backend contracts; production rollout remains separate and blocked.

## Branch Boundaries

The intended review boundaries remain:

1. `feature/local-first-room-foundation`
2. `feature/document-quota-v2`
3. `feature/offline-entitlements`
4. `feature/offline-backup-engine`
5. `feature/backup-restore-ui`
6. `feature/google-drive-backup`
7. `feature/legacy-data-migration`

Do not create these branches until explicitly instructed.

## Phase 1: Local-First Room Foundation

- Add missing Room entities and migrations.
- Preserve existing business data.
- Keep UI behavior unchanged until migration tests pass.
- Add account-isolation and migration tests.

Acceptance:

- Room contains all operational document data.
- Local CRUD tests pass.
- No external service required for normal local operations.

## Phase 2: Document Quota V2

- Add immutable local document creation events.
- Add proposed Supabase V2 event tables/RPCs in backend repository only when that work is approved.
- Implement idempotent event acknowledgement.
- Stop using document row counts as usage truth.

Acceptance:

- create consumes one credit
- edit/delete/restore/retry do not consume another credit
- duplicate and quote-to-invoice consume a new credit
- free lifetime and paid billing-cycle scopes are enforced

## Phase 3: Offline Entitlements

- Store signed entitlements locally.
- Validate signature and expiry offline.
- Keep server as entitlement authority.
- Block only new creation when entitlement is missing/expired; keep read/export available.

## Phase 4: Offline Backup Engine

- Build encrypted `.tijario` archive writer.
- Include manifest, checksums, structured data, assets, and PDFs.
- Add restore staging and validation.
- Keep active database unchanged on restore failure.

## Phase 5: Backup and Restore UI

- Add backup settings.
- Add manual backup/export/import/restore UI.
- Show clear backup status and last error.
- Keep destructive local data deletion separate from logout.

## Phase 6: Google Drive Backup

- Add Drive authorization after external console setup.
- Create Tijario folders.
- Upload encrypted backups.
- Support manual/daily/weekly policies, Wi-Fi-only, charging-only, retention, and pending upload.

## Phase 7: Legacy Data Migration

- Migrate existing cloud data to Room.
- Create baseline creation events.
- Create local backup.
- Switch to `local_drive` mode behind feature flag.
- Keep rollback available during retention.

## What Not To Mix

- Do not mix Google Drive implementation into quota V2.
- Do not mix backend schema changes into Android-only remediation.
- Do not delete legacy cloud data in initial local-first rollout.
- Do not implement live multi-device sync in V1.
