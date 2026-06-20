# Tijario Local Document Engine Architecture

## Objective

Android now owns local visual rendering for documents while the backend remains authoritative for authentication, ownership, RLS, document creation, numbering, official dates, official totals, status, payment status, limits, and usage increments.

## Flow

`Canonical Document Data -> Original Local HTML/CSS Template -> Secure WebView Preview -> Local PDF -> Cache -> View / Save / Print / Email / Share`

The Android app does not use the server PDF endpoint as the primary document renderer. The endpoint remains available for backend/web fallback compatibility.

## Production Packages

- Model: `app/src/main/java/app/tijario/features/documents/model`
- Mapping: `app/src/main/java/app/tijario/features/documents/mapper`
- Template rendering: `app/src/main/java/app/tijario/features/documents/template`
- Preview: `app/src/main/java/app/tijario/features/documents/preview`
- PDF/cache: `app/src/main/java/app/tijario/features/documents/pdf`
- Export: `app/src/main/java/app/tijario/features/documents/export`
- UI helpers: `app/src/main/java/app/tijario/features/documents/ui`

## Assets

- Base HTML: `app/src/main/assets/documents/base/document.html`
- Common CSS: `app/src/main/assets/documents/base/common.css`
- Print CSS: `app/src/main/assets/documents/base/print.css`
- Template registry marker: `app/src/main/assets/documents/templates.json`
- Ten original template folders: `app/src/main/assets/documents/templates/*`

## Canonical Model

`DocumentRenderModel` contains identity, type, number, issue date, revision, localized direction, template identity/version, business party, customer party, stable item lines, official totals, status, payment status, notes, terms, locale, and formatting version.

Draft preview maps `DocumentFormState` through `DraftDocumentRenderMapper` and calculates provisional totals locally for immediate UX.

Saved preview maps `CompleteDocument` through `SavedDocumentRenderMapper` and uses authoritative saved values. Missing critical saved fields fail fast before rendering.

## Renderer

`DocumentHtmlRenderer` loads only local assets and injects escaped values into one local HTML shell. JavaScript is disabled. Values are escaped through `HtmlEscaper`; inserted text cannot become executable markup.

The document contact wording is intentionally neutral:

- Arabic business contact: `الرقم`
- Arabic customer contact: `الرقم`
- English contact: `Number`

Document output must not include WhatsApp wording.

## PDF and Cache

`LocalPdfGenerator` renders the same HTML into a local PDF. `PdfCacheManager` stores PDFs in `cache/documents` and rejects missing, zero-byte, or non-PDF files by checking the `%PDF` signature.

Cache keys include:

- document ID or `draft`
- revision/issue date
- template ID
- template version
- locale
- formatting version

Template, locale, or revision changes invalidate the cache.

## FileProvider

The manifest uses `${applicationId}.fileprovider`, `exported=false`, and `grantUriPermissions=true`. `file_paths.xml` exposes only `cache-path name="cached_documents" path="documents/"`.

No `file://` URI is used for sharing.

## Export

`DocumentExportManager` reuses the cached/generated PDF for:

- view PDF
- permanent save to Downloads through MediaStore
- Android print framework
- email attachment
- general PDF sharesheet

Text sharing remains separate and does not imply a PDF attachment.

## Performance

Preview uses HTML generated from the canonical model and does not generate a PDF on each keystroke. PDF generation happens only on export/view/save/print/email/share and reuses cache when valid.

Measured timing has not been recorded on a physical mid-range device in this pass.

## Security

- JavaScript disabled.
- DOM storage disabled.
- External navigation blocked.
- Local assets only.
- No token or API URL is inserted into HTML.
- No service-role key or backend secret is used.
- No broad storage permission added.

## Known Limitations

- PDF visual QA requires opening generated PDFs on a device or emulator.
- The local PDF generator uses WebView drawing into A4 pages; table header repetition depends on the rendered WebView layout and should be visually verified on long documents.
