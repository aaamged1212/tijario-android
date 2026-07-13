# Project State (Android & Web Repos)

- **Android Repo Branch**: `fix/android-adaptive-ui-qa-v1-1-2`
- **Web Repo Branch**: `fix/document-number-allocation-collision`
- **Current Uncommitted Files Summary**: Batch A (Document Numbering Safety & Idempotence), Batch B Android UI Recovery, the Android UI polish follow-up, the 2026-07-13 UI cleanup batch, the document form UI follow-up, the document info/export action follow-up, and the Android local document-options follow-up are implemented locally and fully Gradle-verified.

## Tijario status:
- Google Play Closed Testing is active.
- Local work exists to reduce Vercel usage and prevent retry loops.
- Product decision: allow duplicate customer WhatsApp numbers.
- Customer identity must be customer.id, not whatsapp_number.
- Supabase migration for duplicate WhatsApp exists locally in Web repo:
  `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- QA migration exists locally in Web repo:
  `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- Both migrations are NOT applied.
- Document Numbering Counters migration exists locally in Web repo:
  `supabase/migrations/20260712090000_document_numbering_counters.sql` (NOT applied).
- Android does not decrement stock locally; document writes remain online-first through the Web API.
- Android document create/update now sends selected `customer.id`; WhatsApp remains a contact field only.
- Android local fallback document creation reuses a selected customer id and does not create a duplicate customer for the same WhatsApp number.
- Android document language selection uses a mobile-safe bottom sheet and default document titles follow the selected document language unless the user entered a custom title.
- Android document save/update validation and server errors surface through Snackbar instead of inline card text.
- Android product forms now require available stock only for product rows; service stock remains optional.
- Android invoice forms now pre-block saved product quantities above editable stock; edit mode uses `current stock + original quantity from the same invoice`, services/manual lines are excluded, and server-side inventory remains authoritative.
- Android onboarding keeps the existing WhatsApp field and composes it from an MVP dial-code selector plus local phone number.
- **Document Numbering**: Batch A implemented, ensuring client-side operation UUID persistence and server-side Next.js route API forwarding to DB RPC for sync idempotency.
- **Adaptive UI & Latest Document Ordering**:
  - `created_at` latest document ordering is defined/tested in `DocumentOrdering.kt` and integrated into Dashboard/Documents ordering.
  - Root app layout adapts to the current container width at 300, 360, and 480 dp boundaries. Compact bottom navigation avoids clipped labels.
  - Adaptive Dashboard quick actions, Product/Customer cards, Document list ordering, localized action sheets, and in-form searchable Customer/Product pickers are implemented locally.
  - Follow-up UI polish local fixes cover localized customer/PDF labels, phone country-code fields, Store Settings badge removal, Customer/Product card hierarchy, AI online-only text placement, create-quote next-number display, discount/extra-fee row ordering, theme-aware settings icons, and AI-matched background/bottom-nav accent.
  - 2026-07-13 cleanup fixes cover App Settings notification row alignment, numeric keyboards, Store Settings phone-country synchronization, AI light-mode palette selection, root bottom-tab spacing, compact Dashboard financial metrics, Dashboard invoice/quote latest-document tabs, and current-date/non-misleading draft document previews.
  - Document form follow-up fixes cover quote-specific info labels, fixed `INV-`/`Q-` document number prefixes with editable numeric suffixes, due-date calendar picking, centered Dashboard latest-document chips, and FAB positioning above the bottom tab bar.
  - Document info/export action follow-up fixes cover creation-date calendar picking, local five-digit draft numbering from cached documents, quote title localization, government-building tax icon, and print/email actions in the document long-press sheet.
  - Android local document-options follow-up supports local shipping, fixed/percentage discount entry, multi-select local taxes/payment methods/terms, bottom-sheet option editing, negative discount display, and saved/reopened/PDF rendering through local document metadata. These fields are Android-local and will not restore cross-device after clearing app data unless Web/API/Supabase persistence is added later.
  - Room schema files `13.json` and `14.json` are present; migration tests cover `12 -> 13` and `13 -> 14`.
  - Local Gradle validation passed: `compileDebugKotlin`, `testDebugUnitTest`, `lintDebug --no-daemon`, `assembleDebug`, `:app:processReleaseMainManifest`, and `assembleDebugAndroidTest`.
  - Device/emulator visual QA is still pending.
- Correct release order:
  migrations -> Web/API deploy -> verify logs and document flows -> Android update later.
- Do not touch production until approved.
- Android AAB artifact `app/release/app-release.aab` is present in the workspace but is correctly git-ignored. Do not upload it unless approved.
