# AI Changelog

## 2026-07-08
- **Agent**: Antigravity / Gemini
- **Action**: Created permanent AI handoff protocol system (docs/ai/* and root instruction files).
- **Repos Inspected**: Web (`tjario`) and Android (`tjario-android`).
- **Safety Info**: No push/deploy/migration/upload was executed. Source behavior was not modified.

## 2026-07-08
- **Agent**: Antigravity / Gemini
- **Action**: Diagnosed and implemented local fixes for 4 reported issues:
  1. Partial Paid Amount: Added input form fields, live previews, and invoice template support for Paid Amount and Balance Due.
  2. Dashboard Metrics Update: Added `revalidatePath` on document mutations.
  3. Template Localization: Added `upgrade_required` localization strings mapping in the Android app.
  4. Standalone Customers Sync: Added immediate sync triggers on customer/product database operations in `TijarioRepository.kt`.
- **Safety Info**: Local modifications only. No git pushes, Vercel deployments, Supabase migrations applied, or Play Console uploads.
- **Action**: Refactored Android MVP write model to Online-Only writes + cached reads (no offline CRUD queue, direct Supabase writes).
