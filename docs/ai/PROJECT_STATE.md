# Project State (Android Repo)

- **Repo Name**: Android Repo
- **Current Branch**: `fix/reduce-mobile-vercel-usage-sync-retries-local`
- **Latest Commit**: `50e8890 fix: enforce online-only writes for Android MVP`
- **Current Uncommitted Files Summary**: Modified repository writes to ONLINE-ONLY, audited large QA bugfix batch.

## Tijario status:
- Google Play Closed Testing is active.
- Vercel usage previously spiked due to mobile sync retry loops.
- Local work exists to reduce Vercel usage and prevent retry loops.
- Product decision: allow duplicate customer WhatsApp numbers.
- Customer identity must be customer.id, not whatsapp_number.
- Supabase migration for duplicate WhatsApp exists locally in Web repo:
  `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- QA migration exists locally in Web repo:
  `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- Both migrations are NOT applied.
- Android does not decrement stock locally; document writes remain online-first through the Web API.
- Saved document language is cached and controls reopened detail/preview/PDF output.
- Correct release order:
  migrations -> Web/API deploy -> verify logs and document flows -> Android update later.
- Do not touch production until approved.
- Android AAB artifact `app/release/app-release.aab` is present in the workspace but is correctly git-ignored. Do not upload it unless approved.
