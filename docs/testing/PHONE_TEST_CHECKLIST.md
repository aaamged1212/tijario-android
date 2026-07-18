# Local-First Phone Test Checklist

Not executed by this local implementation. Run on a clean internal-testing build after migrations, backend configuration, and API deployment.

## Account and entitlement

- Register/login, verify one primary device, and inspect plan/usage.
- Confirm a second installation is rejected until the first device is revoked.
- Expire or invalidate a test entitlement: reads/export/backup/restore work, new document creation is blocked.
- Change plan and verify signed limits/templates/branding refresh.

## Offline operational data

- Create, edit, view, delete, restore, and export customers/products/invoices/quotes with airplane mode enabled.
- Confirm document deletion does not reduce created usage.
- Confirm restoring the same document does not create a second usage event.
- Confirm duplicate/retried operation IDs consume one credit.
- Reopen the app and verify Room remains authoritative.

## Backup and restore

- Create a backup offline and verify a `.tijario` file is finalized locally.
- Export with SAF to Downloads and another provider.
- Cancel SAF selection and revoke provider access; verify no data loss.
- Restore a valid same-account archive while offline.
- Reject a wrong-account, modified, oversized, unsupported-version, and truncated archive.
- Interrupt backup and restore; verify temporary files are cleaned and current data remains intact.
- Verify the automatic pre-restore safety backup exists.
- Verify invoices, quotes, drafts, items, snapshots, assets, and latest PDFs after restore.
- Force one PDF generation failure and verify structured data restores with regeneration pending.

## Scheduling and Drive

- Verify manual/daily/weekly unique work per account.
- Verify charging-only and Wi-Fi-only behavior.
- Verify retention after successful local and Drive backups.
- Upload, list, download, restore, open the visible Tijario folder, disconnect Drive, and confirm local data remains available.

## Lifecycle

- Normal logout preserves account-local Room data and cancels account workers.
- Account switch never displays another account's records.
- Explicit device-data removal warns clearly and removes only that account locally.
- App restart, process death, low storage, clock change, and network loss produce safe states.
