# Backup and Drive Execution Status

## Repository Baseline

- Android source: `codex/local-first-complete` at `d641d66567c092753e5ee93a7607a3d872bc783b`.
- Android execution branch: `codex/backup-drive-production-ready`.
- Backend source: `codex/local-first-control-plane` at `27b2c02f195d8ca2470772c6dfc4c5cd5c1d3927`.
- Backend execution branch: `codex/backup-drive-control-plane-ready`.
- Scope: local implementation only. Migrations, deployment, Google Cloud configuration, and device testing are blocked external work.

## Phase Checklist

- `COMPLETE_LOCAL` Primary-device enforcement for backup key envelopes.
- `COMPLETE_LOCAL` Explicit control-plane initialization and unknown data-mode blocking.
- `COMPLETE_LOCAL` Signed entitlement backup policy and plan-safe scheduling.
- `TESTED` Streamed archive input, strict relationship validation, and temporary-file cleanup.
- `TESTED` Visible phone destination, history, restore selection, and secure sharing.
- `TESTED_LOCAL` Google Identity `AuthorizationClient` authorization request/resolution handling, transient token handling, account replacement/disconnect controls, application-scoped runtime reconstruction, and Ktor Drive v3 transport source contracts. Tokens are never persisted.
- `TESTED_LOCAL` Dedicated backup notification channel and foreground Drive-upload notification contract. Permission denial leaves backup state intact.
- `IN_PROGRESS` Real-device OAuth, Drive REST, notification, and settings UX verification.
- `NOT_STARTED` Final static review, local validation, release documentation, and post-flight status.

## Initial Findings

- Backup key envelopes are now issued only after both `device_registrations` and `user_plan.primary_device_id` confirm the active primary installation. Deployment still requires the pending control-plane migration.
- Missing or unknown Android entitlement data now resolves to `uninitialized`; operational writes fail with `ENTITLEMENT_INITIALIZATION_REQUIRED` until a signed entitlement has been persisted.
- Drive transport uses a Ktor Drive v3 implementation for list/folder/create/upload/download/delete. Upload and download operate on encrypted archive files, not plaintext records; runtime reconstruction requires reauthorization after process death because OAuth tokens remain memory-only.
- Restore currently receives archive bytes in memory; SAF and Drive inputs need bounded file streaming.
- `LogicalBackupValidator` currently skips a relationship when required columns are absent; this must fail closed.
- SAF and Drive restore now stage encrypted archives in account-scoped private files, enforce the archive limit during copy, stream decryption to a temporary payload, and stage binary assets as files instead of returning them in the in-memory logical-entry map. The temporary input and decrypted staging paths are cleaned on success or failure.
- Phone-visible copies use MediaStore on Android 10+ and a persisted SAF tree on Android 8-9. The settings screen shows the destination, permits restoring a private history item after confirmation, and shares only finalized `.tijario` files through a FileProvider, preferring Telegram when installed.

## External Blockers

- Three Local-First migrations remain unapplied.
- Production signing and backup-envelope keys are not configured.
- Google OAuth/Drive configuration and physical-device verification are not performed in this local task.
