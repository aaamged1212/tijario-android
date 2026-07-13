# External release actions

These actions are intentionally not performed from `tijario-android` or from PR #3. They affect production systems, external consoles, or the private backend repository and require their own reviewed deployment/change record.

## 1. Supabase production

Apply the pending migrations in timestamp order after taking a recoverable backup:

1. `20260708184437_allow_duplicate_customer_whatsapp.sql`
2. `20260709000137_qa_business_stock_document_language.sql`
3. `20260712090000_document_numbering_counters.sql`

Then verify:

- `document_number_counters` exists and has the expected RLS posture.
- `documents.operation_id` and its per-user uniqueness index exist.
- `create_document_with_usage` returns the server-authoritative document number.
- Current RLS policies isolate customers, products, documents, settings, usage, and plans by authenticated user.

## 2. Web/API deployment

Deploy a backend version compatible with the reviewed private repository contract. Before Android distribution, verify:

- Pull sync returns `updated_at`, `amount_paid`, and `product_id`.
- Push sync treats `base_server_revision` as the previous `updated_at` value.
- Operation IDs are idempotent across retries.
- Official document numbers are allocated only on the server.
- Offline quota leases reject overuse and duplicate operation consumption.
- AI request IDs do not consume quota twice on retries.
- Vercel/API logs contain no unresolved validation, conflict-loop, or duplicate-operation errors.

## 3. Local document options backend contract

Shipping, discount configuration, selected taxes, payment methods, terms, signatures, and related document metadata must receive an approved server schema and API contract before they can be restored across devices. Do not silently claim cross-device persistence until this contract is deployed and Android/Web both consume it.

## 4. Google Cloud and Firebase

- Restrict the Firebase/Google API key to package `app.tijario` and approved release/debug signing certificate fingerprints.
- Restrict enabled APIs to those actually required.
- Verify Firebase project permissions, messaging configuration, and App Check where applicable.
- Confirm no service credentials or signing files are stored in either repository.

## 5. OAuth and verified App Links

A verified HTTPS callback requires coordinated configuration:

- Host the callback on a Tijario-owned HTTPS domain.
- Publish a valid `/.well-known/assetlinks.json` containing package `app.tijario` and the production SHA-256 signing fingerprint.
- Add the HTTPS callback to Supabase and Google OAuth redirect allowlists.
- Validate sign-in, cancellation, recovery, and process recreation on real devices before removing custom schemes.

## 6. Google Play Console

- Rotate any reviewer credential that may have been active while present in Git history.
- Store the reviewer account only in the protected App Access field.
- Update Data Safety against the final manifest and dependency list.
- Confirm the privacy policy describes FCM, billing verification, AI inputs, account deletion, and any analytics that are actually enabled.
- Increment `versionCode` and record the exact source commit used for the AAB.
- Retain the R8 mapping and native debug symbols for the release.

## 7. Meta developer console

Android automatic event logging and Advertising ID collection are disabled in the branch. Before enabling explicit analytics in a future release:

- Define the lawful/consent basis and user-facing privacy flow.
- Update the privacy policy and Play Data Safety form.
- Verify Meta application and client-token configuration.
- Enable events only through the application consent gate.

## 8. Real-device release QA

Complete Arabic and English testing on compact and common devices, including Android 9 and Android 15:

- Login, Google login, logout, account deletion, and account switching.
- Create/edit/delete quote and invoice.
- Server number reconciliation and retry idempotency.
- PDF rendering, multi-page layout, printing, Downloads visibility, email, and share intents.
- Rotation/process recreation, light/dark mode, RTL/LTR, permissions, and notification deep links.
- Offline lease expiration, network loss, retry, conflict, and return-online behavior.
- Google Play Billing purchase, pending purchase, restore, upgrade/downgrade, and cancellation.

No item in this file authorizes a production change. Each external action requires an explicit deployment or console change outside PR #3.
