# Project State (Android Repo)

- **Repo Name**: Android Repo
- **Current Branch**: `fix/reduce-mobile-vercel-usage-sync-retries-local`
- **Latest Commit**: `4a4b997 fix: stop resolving customers by WhatsApp in offline documents`
- **Current Uncommitted Files Summary**: Working tree clean.

## Tijario status:
- Google Play Closed Testing is active.
- Vercel usage previously spiked due to mobile sync retry loops.
- Local work exists to reduce Vercel usage and prevent retry loops.
- Product decision: allow duplicate customer WhatsApp numbers.
- Customer identity must be customer.id, not whatsapp_number.
- Supabase migration for duplicate WhatsApp exists locally in Web repo:
  `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- Migration is NOT applied.
- Correct release order:
  migration -> Web/API deploy -> verify logs -> Android update later.
- Do not touch production until approved.
- Android AAB artifact `app/release/app-release.aab` is present in the workspace but is correctly git-ignored. Do not upload it unless approved.
