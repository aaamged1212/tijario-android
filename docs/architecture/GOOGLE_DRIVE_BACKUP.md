# Google Drive Backup

Status: transport interfaces, repositories, worker, fake client, and UI states are implemented locally. Real Google OAuth/REST wiring and device QA remain external blockers.

## Decision

Google Drive V1 is a transport for encrypted backup and restore. It is not live sync.

Local data must remain available if Google Drive is disconnected, unauthorized, offline, rate-limited, or unavailable.

## Folder Layout

The app should create a visible folder:

`Tijario | تجاريو`

Inside it, create:

`Backups | النسخ الاحتياطية`

## User Actions

- `Back up now`: create the local encrypted backup first, then upload it directly to the Tijario backup folder if internet is available.
- `Export backup to...`: use Android Storage Access Framework so the user can choose Google Drive, local storage, or another provider.
- `Restore from file`: use Storage Access Framework to select a local `.tijario` file and restore through local staging.
- `Restore from Google Drive`: download the encrypted archive first, then run the same local restore flow.
- `Open Tijario Drive folder`: convenience link after Drive is connected.

## Offline Behavior

- Manual local backup creation works offline.
- If Drive upload is requested while offline, create the backup locally, mark upload as pending, and schedule upload when internet returns.
- Drive download requires internet, but the downloaded archive must be restorable locally.

## Authorization

Use least privilege, preferably `drive.file`.

Do not request broad Drive access unless a later requirement proves `drive.file` cannot meet folder/file lifecycle needs.

## Policies

Support:

- manual backup
- weekly automatic backup
- daily automatic backup
- Wi-Fi-only upload
- charging-only execution
- retention policy
- manual restore
- import from local `.tijario`
- restore from Drive

Track:

- backup status
- size
- checksum
- created time
- uploaded time
- Drive file ID
- last error

## External Console Blockers

Before implementation:

- enable Google Drive API
- configure OAuth consent
- add Android OAuth client and SHA fingerprints
- approve least-privilege Drive scope
- test internal/closed accounts
- verify production OAuth status

These are external manual actions and must not be changed by repository work.
