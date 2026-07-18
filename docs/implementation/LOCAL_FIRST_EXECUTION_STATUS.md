# Local-First Execution Status

## Repository State

- Android branch: `codex/local-first-complete`
- Android starting commit: `d1e296ecb6fd1cdabf884661e23f00fc12f7161d`
- Backend branch: `codex/local-first-control-plane`
- Backend starting commit: `73068e8e7079f4e36121a8f3a18937e7379fcbba`
- Remote actions: prohibited; local commits only
- Production actions: prohibited

## Current Phase

`TESTED` - Offline local encrypted archive creation foundation.

## Phase Checklist

- `TESTED` Replace the mutable/deletable local usage ledger with immutable document creation events while preserving existing rows.
- `TESTED` Add persisted account entitlement, data-mode, device-binding, backup settings, backup records, and backup file metadata.
- `TESTED` Add Room migration coverage and generated schema version 16.
- `TESTED` Route operational repositories by explicit `legacy_cloud` / `local_drive` mode while defaulting unknown accounts to legacy behavior.
- `TESTED` Route Local-Drive customer, product, document, and business-setting writes to Room without operational outbox or cloud CRUD.
- `TESTED` Preserve account-local Room data during normal logout and clear only transient in-memory session state.
- `TESTED` Add repository-level Local-Drive soft deletion/restoration and active customer/product limit enforcement.
- `TESTED` Preserve historical customer snapshots and explicit document deletion/PDF-generation state in Room 17.
- `IN_PROGRESS` Complete restoration UI and quota reconciliation.
- `TESTED` Implement the offline encrypted `.tijario` archive codec with authenticated encryption, logical-file checksums, account binding, and path validation.
- `TESTED` Connect deterministic, account-scoped Room logical export and prevalidated transactional restore to the archive codec data contract.
- `TESTED` Collect existing account logo, product images, and document PDFs; record missing/failed PDF counts without failing structured backup.
- `TESTED` Finalize verified encrypted archives through temporary files and atomic replacement, then persist local backup/file metadata.
- `TESTED` Preserve current immutable usage events, entitlement authority, device bindings, and leases during restore; backup events merge without deleting newer credits.
- `TESTED` Stage and validate account-owned PDF/image/logo assets, apply them with rollback copies, and roll back assets when Room restore fails.
- `TESTED` Add Android Keystore RSA-OAEP device keys, versioned wrapped-key cache, exact archive key-version resolution, and transient key zeroing.
- `IN_PROGRESS` Connect local PDF regeneration and backup/restore UI.
- `NOT_STARTED` Implement Drive transport and legacy migration UI.
- `NOT_STARTED` Implement backend control-plane V2 migrations and contracts locally without applying them.

## Preserved Local Files

- `.agents/skills/` and `.agents/skill-backups/` are local-only and untouched.
- `app/schemas/app.tijario.data.local.TijarioDatabase/15.json` existed untracked at pre-flight and is preserved for Room migration validation.

## Commands Executed

- Repository identity, branch, status, HEAD, log, and diff checks for Android and backend.
- Existing architecture, Room schema, repository, sync, entitlement, migration, and test inventory reads.
- Combined `testDebugUnitTest assembleDebugAndroidTest` timed out and was not counted.
- `testDebugUnitTest` passed.
- `assembleDebugAndroidTest` passed.
- Targeted `TijarioRepositoryOfflineTests` passed.
- Targeted `AccountDataModeTest` and `TijarioRepositoryOfflineTests` passed after data-mode routing.
- Full `testDebugUnitTest` passed after data-mode routing.
- `assembleDebugAndroidTest assembleDebug` passed after data-mode routing.
- `lintDebug` exceeded the three-minute local command timeout and is not counted as passed.
- `adb devices` was blocked because `adb` is unavailable.
- Focused historical snapshot JVM tests and `assembleDebugAndroidTest` passed for Room 17.
- Focused logical backup snapshot JVM tests and `assembleDebugAndroidTest` passed.
- Focused asset collection and atomic backup-file JVM tests passed; Android test APK compilation passed.
- Focused staged asset apply/rollback JVM tests and Android test APK compilation passed.
- Focused backup key contract/header JVM tests and Android Keystore source compilation passed.

## Actual Outcomes

- Both repository remotes matched the required GitHub repository identities.
- Both execution branches were created locally from the required starting commits.
- Room schema advanced from 15 to 16 and generated schema files are present.
- Existing `local_usage_ledger` rows migrate into `document_creation_events`; successful sync acknowledges events instead of deleting them.
- Account usage now persists the server-provided data mode, limit scope, entitlement version, limits, and template policy in Room.
- Local-Drive public CRUD entry points use Room and do not enqueue operational cloud mutations; refresh/sync entry points are no-ops for operational data.
- Local-Drive document creation checks the persisted entitlement and records a lifetime or billing-cycle creation event without requiring an online lease.
- Normal sign-out no longer deletes Room customers, products, documents, settings, or creation events.
- Local-Drive customer, product, and document deletion now preserves rows and records deletion history; repository restore clears the deletion marker without creating a new document event.
- Customer and product limits count only active local rows, so deletion frees a slot and restoration rechecks the limit.
- Focused repository tests and `assembleDebugAndroidTest` passed for soft deletion, restoration, limit failures, and no-credit document restore.
- Backup codec JVM tests passed for encrypted offline round-trip, cross-account rejection, tamper rejection, required structured data, and path traversal protection.
- Room logical backup exports all approved account-scoped operational/control-plane cache tables as typed JSON. Restore validates table identity, schema columns, and row ownership before replacing only that account's rows in one database transaction.
- Local backup creation collects available account assets, records absent/failed PDFs in the manifest, authenticates the completed archive before finalization, and stores it under the account's private app directory without requiring network access.
- Restore policy replaces operational rows, merges immutable creation events without overwrite, preserves current entitlement authority, and never reactivates archived device bindings or quota leases.
- Restore accepts assets only for document/product IDs in the same archive, stages them below the account directory, and can roll back previously existing files if database restore fails.
- Android never stores a plaintext account backup key. It caches device-wrapped key versions, resolves the exact archive version, and zeroes decrypted key bytes after create/restore.
- JVM tests and instrumentation compilation passed. Runtime migration execution is blocked by the unavailable Android test environment.
- No push, deploy, migration apply, GitHub API action, or external-console change occurred.

## Known Risks And Blockers

- Instrumentation execution requires an available emulator/device; compilation can still be validated locally.
- Google Drive OAuth and production credentials are external blockers and will not be configured in this task.
- Supabase migrations will be authored and tested only against a verified local instance; production will not be touched.
- Existing PDF/assets, local archive lifecycle, rollback-capable restore, and Android key client are implemented, but the backend migrations/configuration are unapplied and missing-PDF regeneration/UI are incomplete; no user backup should be advertised yet.

## Next Executable Task

Add backup/restore UI with Storage Access Framework export/import while keeping Drive API integration blocked.
