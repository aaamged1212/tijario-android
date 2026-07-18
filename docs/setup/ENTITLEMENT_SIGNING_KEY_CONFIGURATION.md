# Entitlement Signing Key Configuration

Signed offline entitlements use RS256. No real signing key is committed.

## Backend variables

- `ENTITLEMENT_SIGNING_KEY_ID`: stable identifier for the active public key.
- `ENTITLEMENT_SIGNING_PRIVATE_KEY_BASE64`: Base64 PKCS#8 DER RSA private key, minimum 2048 bits.

## Android build configuration

- `TIJARIO_ENTITLEMENT_PUBLIC_KEYS_BASE64`: Base64-encoded JSON object mapping key IDs to Base64 X.509 DER RSA public keys.
- Include the current and still-valid previous public keys during rotation.
- Public keys are not secrets, but their IDs and rollout must remain controlled.

## Verification contract

Android verifies canonical JSON, RS256 signature, key ID, account ID, installation ID, issue time, expiry, plan/data-mode claims, limits, templates, and branding policy. New quota-consuming operations fail closed when the entitlement is missing, invalid, modified, expired, or for another account/device. Read, export, backup, and restore remain available.

The expiry is derived from the trusted plan `offline_entitlement_days` value (1-30 days), not from a client value.

## Rotation

1. Publish the new public key in Android before using its key ID to sign production entitlements.
2. Deploy the backend with the new private key and key ID.
3. Keep old public keys until every entitlement signed by them has expired.
4. Remove retired keys in a later Android release.
