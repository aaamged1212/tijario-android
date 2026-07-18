# Pending Release

## Unapplied Web-Repo Migrations
- `supabase/migrations/20260708184437_allow_duplicate_customer_whatsapp.sql`
- `supabase/migrations/20260709000137_qa_business_stock_document_language.sql`
- `supabase/migrations/20260712090000_document_numbering_counters.sql`

The counters migration is required to manage sequential document numbers without concurrency collisions.

## Status
- **Migration Status**: NOT applied.
- **Web/API Status**: NOT deployed.
- **Android Update Status**: NOT uploaded. Android document edit/export fixes are implemented locally and need device QA/source-control handoff.
- **Current Local Remediation Status**: `RemoteCacheReplacementPolicy.shouldReplace(...)` is integrated into Android remote-cache ingestion and pull-sync replacement decisions on local branch `codex/full-audit-cache-policy-docs`; architecture documents for Local-First/Drive backup are planning-only and do not change runtime behavior.
- **Local-First Android Status**: Room migrations through schema 17 and encrypted backup/restore with SAF UI exist only on local branch `codex/local-first-complete`; they have not been uploaded or released. The required backup-key backend migrations/configuration are also unapplied.
- **Batch A status**: Document numbering idempotency and operation_id sync propagation are completed locally.
- **Adaptive UI QA Status**: Batch B UI recovery plus the Android UI polish follow-up are implemented locally and Gradle-verified. Dashboard/Documents now use `newestDocuments`, quick actions are adaptive, Customer/Product/Document cards have localized long-press sheets, document forms use searchable Customer/Product pickers, and the reported label/icon/phone/quote-number/product-card polish issues are addressed in code. The Android local document-options follow-up adds local shipping, percentage discount, and multi-select taxes/payment/terms without Web/API or Supabase changes. The document edit/export follow-up keeps edit saves on update, uses local cached documents for immediate draft numbers, preserves edited title/language and manual document numbers in local cache/list/detail/export paths, saves locally generated PDFs to public Downloads with legacy storage permission handling on API 28 and below, orders documents by newest creation time, reuses latest selected tax for new documents, adds local discount/extra-fee presets, and restricts email export to email apps.
- **Validation Status**: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, and `git diff --check` passed. `lintDebug` previously timed out twice before a pass/fail result.
- **Manual QA Pending**: Verify next-document number previews, edit-mode invoice item changes, PDF visibility in the phone Downloads folder, edited title/manual number persistence in list/detail/PDF, newest-first document ordering, automatic tax defaults, discount/extra-fee preset selection, email-app chooser filtering, sync idempotency, form state restoration after screen rotation, local document option bottom sheets, shipping/discount preview/PDF output, and visual QA on real compact/common Android devices in Arabic and English, including light/dark settings icons and phone country-code selectors.
- **Android Release Artifact**: `app/release/app-release.aab` remains present and git-ignored; do not upload or commit it.

## Correct Release Order
1. Apply all three Supabase migrations in timestamp order.
2. Deploy compatible Web/API code from `fix/document-number-allocation-collision`.
3. Verify Vercel logs and sync idempotency under retries.
4. Upload an Android update only after explicit approval and successful server verification.
