# Backup Key Configuration

The backend envelope API requires `BACKUP_KEY_ENCRYPTION_KEY`. No production key is included in source, examples, tests, or this document.

## Contract

- Value: Base64 encoding of exactly 32 random bytes.
- Purpose: server-side encryption of account backup keys at rest.
- Android receives only an account key wrapped to its Android Keystore public key.
- Plaintext account backup keys must never be stored in Supabase or logs.
- Historical key versions remain retrievable for existing archives until an approved retention policy removes them.

## Safe setup

1. Generate the value in an approved secret manager, not in the repository.
2. Store it only in the backend production environment.
3. Validate Base64 decoding and the 32-byte length before enabling the endpoint.
4. Keep a recoverable, access-controlled copy. Losing this key can make stored envelopes unusable.
5. Restart/deploy the backend only during the approved release window.

## Rotation

- Introduce a new envelope-encryption key version without deleting the old key immediately.
- Re-wrap account keys transactionally or retain the old server key until all required envelopes have migrated.
- Do not rotate Android archive key versions by rewriting existing archives.
- Verify old `.tijario` archives before retiring any historical key material.

Missing or malformed configuration must make the key-envelope endpoint unavailable with a safe error; it must not fall back to a hardcoded key.
