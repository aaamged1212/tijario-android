# Local-First Execution Status

## Repository State

- Android branch: `codex/local-first-complete`
- Android starting commit: `d1e296ecb6fd1cdabf884661e23f00fc12f7161d`
- Backend branch: `codex/local-first-control-plane`
- Backend starting commit: `73068e8e7079f4e36121a8f3a18937e7379fcbba`
- Remote actions: prohibited; local commits only
- Production actions: prohibited

## Current Phase

`TESTED` - Explicit account data mode, Room-first operational writes, and non-destructive logout.

## Phase Checklist

- `TESTED` Replace the mutable/deletable local usage ledger with immutable document creation events while preserving existing rows.
- `TESTED` Add persisted account entitlement, data-mode, device-binding, backup settings, backup records, and backup file metadata.
- `TESTED` Add Room migration coverage and generated schema version 16.
- `TESTED` Route operational repositories by explicit `legacy_cloud` / `local_drive` mode while defaulting unknown accounts to legacy behavior.
- `TESTED` Route Local-Drive customer, product, document, and business-setting writes to Room without operational outbox or cloud CRUD.
- `TESTED` Preserve account-local Room data during normal logout and clear only transient in-memory session state.
- `TESTED` Add repository-level Local-Drive soft deletion/restoration and active customer/product limit enforcement.
- `IN_PROGRESS` Complete historical customer snapshots, restoration UI, and quota reconciliation.
- `NOT_STARTED` Implement backup/restore, Drive transport, and legacy migration.
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
- JVM tests and instrumentation compilation passed. Runtime migration execution is blocked by the unavailable Android test environment.
- No push, deploy, migration apply, GitHub API action, or external-console change occurred.

## Known Risks And Blockers

- Instrumentation execution requires an available emulator/device; compilation can still be validated locally.
- Google Drive OAuth and production credentials are external blockers and will not be configured in this task.
- Supabase migrations will be authored and tested only against a verified local instance; production will not be touched.

## Next Executable Task

Persist historical customer snapshots in documents and expose the repository restoration flow through a dedicated local-data UI.
