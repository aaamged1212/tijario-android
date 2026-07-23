# Local-First Database Migration Runbook

Status: documentation only. None of these migrations were applied by this work.

Applied production migration history ends at `20260712103000`. The three
Local-First migrations below are pending and must be applied together only in
the documented order.

## Required order

1. Back up the production database and record the current migration history.
2. Apply `20260718090000_local_first_control_plane.sql`.
3. Apply `20260718100000_backup_key_envelope_account_scope.sql`.
4. Apply `20260718110000_local_first_control_plane_completion.sql`.
5. Run the read-only checks in `docs/testing/DATABASE_POST_DEPLOY_CHECKLIST.md`.
6. Configure backend signing and backup-key environment variables.
7. Deploy the compatible Web/API commit.
8. Verify one internal test account before enabling `local_drive` for more accounts.

Do not apply only the completion migration. It depends on tables and columns from the first two files.

## Pre-application checks

- Confirm all three migration files are byte-for-byte identical to the reviewed branch.
- Confirm `public.plans`, `public.user_plan`, `public.resolve_usage_cycle(uuid)`, and authentication defaults exist.
- Confirm no existing table named `document_creation_events` has an incompatible shape.
- Confirm the service role is the only caller of the new control-plane RPCs.
- Confirm a rollback window and a database backup exist.

## Expected schema changes

- Plan policy columns for lifetime/billing-cycle quota, offline entitlement, credit batches, one primary device, and backup defaults.
- User-plan data mode, entitlement version, and primary installation binding.
- `device_registrations`, `offline_quota_leases_v2`, `document_creation_events`, `account_usage_totals`, and `backup_key_envelopes`.
- Service-role-only device, lease, event reconciliation, and account-cleanup RPCs.
- The legacy `refund_document_usage(uuid)` symbol becomes a service-role-only no-op.

## Rollback posture

Do not drop the legacy operational tables during rollout. If verification fails:

1. Stop issuing `local_drive` entitlements.
2. Roll affected accounts back to `legacy_cloud` only after reconciling pending creation events.
3. Keep immutable event and usage-total rows for audit.
4. Roll back the API deployment before removing any new schema.
5. Prefer a forward corrective migration over destructive rollback.

## Explicit prohibitions

- Never reset or decrement usage counters.
- Never delete creation events as part of document deletion or restore.
- Never expose the service-role key to Android.
- Never apply these migrations from a mobile client.
