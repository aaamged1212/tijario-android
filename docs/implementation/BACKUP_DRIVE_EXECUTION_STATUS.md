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
- `IN_PROGRESS` Streamed archive input, strict relationship validation, and temporary-file cleanup.
- `NOT_STARTED` Visible phone destination, history, restore selection, and secure sharing.
- `NOT_STARTED` Google Identity authorization, runtime wiring, and production Drive REST transport.
- `NOT_STARTED` Foreground backup progress, notifications, and final settings UX.
- `NOT_STARTED` Final static review, local validation, release documentation, and post-flight status.

## Initial Findings

- Backup key envelopes are now issued only after both `device_registrations` and `user_plan.primary_device_id` confirm the active primary installation. Deployment still requires the pending control-plane migration.
- Missing or unknown Android entitlement data now resolves to `uninitialized`; operational writes fail with `ENTITLEMENT_INITIALIZATION_REQUIRED` until a signed entitlement has been persisted.
- Drive transport has a fake/client boundary but no persistent authorization runtime or concrete REST transport.
- Restore currently receives archive bytes in memory; SAF and Drive inputs need bounded file streaming.
- `LogicalBackupValidator` currently skips a relationship when required columns are absent; this must fail closed.

## External Blockers

- Three Local-First migrations remain unapplied.
- Production signing and backup-envelope keys are not configured.
- Google OAuth/Drive configuration and physical-device verification are not performed in this local task.
