# Migration and Rollout

Status: planning only. No migration is applied by this document.

## Principles

- Existing users require a staged and reversible migration.
- Do not delete legacy cloud business-data tables during the initial transition.
- Do not switch all users at once.
- Do not make Google Drive a prerequisite for using existing local data.

## Proposed Data Modes

```text
legacy_cloud
local_drive
cloud_sync_future
```

`legacy_cloud` is the current bridge model.

`local_drive` is the approved V1 target: Room for operational data, Supabase control plane, Google Drive backup/restore.

`cloud_sync_future` is reserved for later paid live multi-device sync.

## Existing User Migration

For each account:

1. Require online login.
2. Download current cloud business data.
3. Write data into Room.
4. Validate customers, products, documents, document items, business settings, and metadata counts.
5. Create baseline local `document_creation_events`.
6. Create a local encrypted backup.
7. Upload to Drive only if Drive is configured and user has enabled it.
8. Mark `local_first_migrated_at`.
9. Switch account data mode to `local_drive`.
10. Keep legacy cloud rows for a retention period.

## Free Plan Transition

Free-plan document usage becomes lifetime-scoped after V2. For existing users, avoid guessing deleted historical documents. Use current trustworthy data plus existing operation IDs/events. If product decides to grant transition credits, record that as an explicit migration event.

## Rollback

Rollback must be possible during the retention window:

- switch data mode back to `legacy_cloud`
- keep Room data intact
- keep backups intact
- do not lower server usage
- do not delete acknowledged document creation events

## Testing Matrix

- fresh install, new account
- existing cloud account migration
- account with no business data
- account with large customers/products/documents
- account with deleted documents
- account with pending local events
- failed backup creation
- failed restore validation
- account switch
- logout without data deletion
- device replacement
- free-plan lifetime quota
- paid billing-cycle quota
- duplicate document
- quote-to-invoice as separate document
- retry same `operation_id`

## Rollout Phases

- internal QA feature flag
- small closed-testing cohort
- staged percentage rollout
- monitor backup failures, restore failures, usage mismatches, Drive auth failures, migration failures, backup size, and export time
- expand only when recovery paths are verified

## Production Blockers

- Supabase V2 tables/RPCs
- compatible Web/API deployment
- signed entitlement issuing
- Google Drive OAuth setup
- Play Console disclosure updates if backup behavior changes privacy answers
- device/emulator coverage for restore and backup flows

