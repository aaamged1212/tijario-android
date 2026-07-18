# Local-First Security Review

## Trust boundaries

- Room owns operational data but is not authoritative for plan limits already consumed on other installations.
- Supabase service-role RPCs own device registration, leases, immutable usage reconciliation, and key envelopes.
- Android accepts offline authority only through an RS256 entitlement bound to user and installation.
- Drive is an untrusted encrypted-file transport.

## Implemented controls

- Service-role-only control-plane tables/RPCs with RLS and revoked anon/authenticated access.
- Immutable document-event uniqueness by user/document and user/operation.
- Payload hash mismatch rejection and no usage refund path.
- One active primary device per account.
- Expiring, bounded offline leases bound to account, installation, entitlement version, scope, and period.
- AES-GCM `.tijario` archives with authenticated header, checksums, account binding, version checks, safe relative paths, duplicate-entry rejection, entry/count/decompressed-size limits, and record-count limits.
- Duplicate row-ID and core document relationship validation before Room writes.
- Per-account backup/restore mutex, pre-restore safety backup, staged assets, transactional Room restore, and rollback.
- Android Keystore RSA-OAEP wrapping and key-byte zeroing after use.
- Drive checksum and Tijario-account metadata checks; no comparison between Google account ID and Tijario UUID.

## Threat outcomes

- Modified entitlement: signature verification fails; new quota-consuming actions stop.
- Replayed operation: existing event is returned without increment.
- Same operation with different payload: rejected.
- Malicious archive/zip bomb: bounded parser rejects before Room replacement.
- Cross-account archive: rejected before staging.
- Storage/restore interruption: existing data is preserved by safety backup, transactional restore, and asset rollback; device QA remains required.
- Stolen Drive file: encrypted payload; confidentiality depends on account key and envelope key custody.

## Residual/external risks

- Production secrets, OAuth, API deployment, and migrations are not configured/applied.
- Physical-device verification of Keystore, SAF, WorkManager, WebView PDF generation, low storage, and interrupted restore is pending.
- Server SQL has static tests only until an approved local/staging Supabase runtime executes the SQL tests.
