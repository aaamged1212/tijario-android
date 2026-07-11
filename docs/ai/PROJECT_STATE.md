# Project State (Android Repo)

- **Repo Name**: Android Repo
- **Current Branch**: `fix/reduce-mobile-vercel-usage-sync-retries-local`
- **Latest Commit**: `a87fe05 fix: prevent Android document customer duplication and improve form feedback`
- **Current Uncommitted Files Summary**: Android QA batch for product stock validation, edit-aware invoice stock validation, onboarding phone country code selector, document ordering, safe-area fixes, and docs/ai handoff updates.

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
- Android document create/update now sends selected `customer.id`; WhatsApp remains a contact field only.
- Android local fallback document creation reuses a selected customer id and does not create a duplicate customer for the same WhatsApp number.
- Android document language selection uses a mobile-safe bottom sheet and default document titles follow the selected document language unless the user entered a custom title.
- Android document save/update validation and server errors surface through Snackbar instead of inline card text.
- Android product forms now require available stock only for product rows; service stock remains optional.
- Android invoice forms now pre-block saved product quantities above editable stock; edit mode uses `current stock + original quantity from the same invoice`, services/manual lines are excluded, and server-side inventory remains authoritative.
- Android onboarding keeps the existing WhatsApp field and composes it from an MVP dial-code selector plus local phone number; no new DB column was added.
- Android recent documents now order by existing sync/update timestamps first and issue date as fallback; no Room migration was needed.
- Android auth/onboarding screens apply status bar, navigation bar, and IME padding to avoid overlap on compact devices; onboarding's language toggle is inside scroll content rather than overlaid.
- Correct release order:
  migrations -> Web/API deploy -> verify logs and document flows -> Android update later.
- Do not touch production until approved.
- Android AAB artifact `app/release/app-release.aab` is present in the workspace but is correctly git-ignored. Do not upload it unless approved.
