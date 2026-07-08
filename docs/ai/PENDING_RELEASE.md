# Pending Release

- **Pending Migration Path**: `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql` (in Web Repo)
- **Migration Status**: NOT applied.
- **Web/API Status**: NOT deployed.
- **Android Update Status**: NOT uploaded. Android changes are local only.
- **Android Release Artifact**: `app/release/app-release.aab` must not be uploaded or committed to git.

## Correct Release Order:
1. Apply Supabase migration.
2. Deploy compatible Web/API code (branch `fix/reduce-mobile-vercel-usage-sync-retries-local`).
3. Verify Vercel logs to confirm:
   - No `/api/mobile/sync/push` errors.
   - No `document_create_failed`.
   - No 500 spikes.
4. Android update uploaded later if needed.
