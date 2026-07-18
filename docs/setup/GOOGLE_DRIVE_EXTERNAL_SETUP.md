# Google Drive External Setup

The repository contains Drive transport interfaces, REST abstraction, upload/download/list/retention logic, workers, and UI states. Real OAuth configuration is intentionally absent.

## External actions required

1. Configure an Android OAuth client for the production package and signing certificate.
2. Request least privilege, preferably `drive.file`.
3. Wire the authenticated token provider to `GoogleDriveRestClient`.
4. Keep `DriveBackupRuntime` unavailable until authentication is configured.
5. Verify consent-screen text and privacy disclosures.

## Folder contract

- Root: `Tijario | تجاريو`
- Child: `Backups | النسخ الاحتياطية`

Only encrypted `.tijario` archives may be uploaded. The Google identity is transport identity and must not be compared to the Tijario UUID. Archive metadata remains bound to the authenticated Tijario account ID.

## Runtime behavior

- Local backup creation works without Drive or network.
- Offline Drive requests leave an encrypted local backup pending for later upload.
- Upload retries are bounded to three attempts with WorkManager backoff.
- Downloads are checksum-verified before account-bound archive validation.
- Disconnecting Drive never removes or blocks local business data.

Real OAuth, upload, download, and folder-visibility validation require an approved physical-device test and external console access.
