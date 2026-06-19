# Security Boundaries

## Client Trust Model

Everything shipped inside an APK is extractable. Android must contain only public configuration and user session tokens managed by the auth client.

## Server-Only Secrets

Never add these to Android code, resources, local committed files, BuildConfig constants, native libraries, or encoded strings:

- `SUPABASE_SERVICE_ROLE_KEY`
- `REPLICATE_API_TOKEN`
- database passwords
- JWT secrets
- PDF secrets
- application secrets
- signing credentials

## Authorization

The Android app must not send or trust client-supplied `user_id` for authorization. Server APIs must verify the bearer token and derive the user from the authenticated Supabase session.

## Git Hygiene

Before any release-readiness claim, scan tracked files for:

- `.env`
- `local.properties`
- keystores
- signing properties
- service-role keys
- provider tokens
- database URLs and passwords
