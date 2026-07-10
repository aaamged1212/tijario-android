# Pending Release

## Unapplied Web-Repo Migrations
- `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`

The QA migration is required before Android can persist business address/email/website, saved document language, or use the stock-aware document RPC behavior.

## Status
- **Migration Status**: NOT applied.
- **Web/API Status**: NOT deployed.
- **Android Update Status**: NOT uploaded. Android changes are local only.
- **Android QA Hotfix Status**: Customer-id document payload, language selector, snackbar errors, and document-language title behavior are implemented locally only.
- **Android Release Artifact**: `app/release/app-release.aab` remains present and git-ignored; do not upload or commit it.

## Correct Release Order
1. Apply both Supabase migrations in timestamp order.
2. Deploy compatible Web/API code from `fix/reduce-mobile-vercel-usage-sync-retries-local`.
3. Verify Vercel logs and invoice create/update/delete, quote, inventory, language, and business-profile flows.
4. Upload an Android update only after explicit approval and successful server verification.
