# Document Engine Reference Analysis

## Scope

This report records the required read-only inspection before implementing the Tijario local document engine. The external application is used only to understand architecture patterns. No proprietary source, smali, XML layout, image, font, JSON template, icon, watermark, brand, or commercial template asset may be copied into Tijario.

## Repository Baseline

- Android repository: `C:\Users\BBOY AMG\Desktop\Projects\tjario-android`
- Starting branch: `feature/native-mvp-completion`
- New child branch: `feature/native-document-engine`
- Required base commit present in history: `e3ccb62f921ddcf3db467d4c20656ef540aeec2d`
- Remote: `https://github.com/aaamged1212/tijario-android.git`

## External Root Inspected

- Requested root: `C:\Users\BBOY AMG\Desktop\invoice-maker-extracted`
- Actual extracted root: `C:\Users\BBOY AMG\Desktop\invoice-maker-extracted\Invoice Maker-invoice.invoicemaker.estimatemaker.billingapp-1.01.92.0824-10093`

## Exact Paths Checked

- `AndroidManifest.xml`: exists, binary Android manifest data.
- `assets`: exists, 234 recursive entries.
- `assets\template`: exists, 224 recursive entries.
- `assets\templates`: not found.
- `res\layout`: not found.
- `res\layout-land`: not found.
- `res\drawable*`: not found in the requested drawable folder names.
- `res\xml`: not found.
- `res\font`: not found.
- `assets\fonts`: not found.
- `sources`, `src`, `java`: not found as decompiled source roots.
- `smali`, `smali_classes2`, `smali_classes3`: not found.
- `databases`, `schemas`, `original`: not found.
- `META-INF\MANIFEST.MF` and `META-INF\ANDROID.SF`: found and list packaged `assets/template/.../data.json` entries.

## Relevant Files and Folders Found

- `assets\template` contains 109 template ID directories, including IDs such as `10001` through `10701`.
- Each inspected template directory contains `data.json`; example `assets\template\10001\data.json` defines fields such as `id`, `vip`, `style`, `themeColor`, text colors, divider colors, section options, and layout weights.
- `assets\template\template_id*.json` files exist as locale or market specific template registries.
- The binary manifest contains references to `androidx.core.content.FileProvider`, a file-provider authority, invoice/estimate preview and result activities, template input/edit activities, export/import activity, and broad legacy permissions in the reference application.

## Search Findings

Search terms included `PdfDocument`, `PrintedPdfDocument`, `PrintManager`, `PrintDocumentAdapter`, `FileProvider`, `MediaStore`, `ACTION_SEND`, `ACTION_VIEW`, `template`, `templateId`, `data.json`, `invoice`, `estimate`, `preview`, `cacheDir`, and `Downloads`.

The strongest inspectable findings were:

- Template configuration is asset-driven through many local `data.json` template files.
- The manifest references separate invoice and estimate preview/result activities.
- The manifest references a FileProvider and a file-provider paths resource.
- The application appears to package commercial/VIP template resources; these are explicitly excluded from Tijario.

## Apparent Preview Rendering Approach

Because no readable source or layout XML was present, the exact rendering code could not be inspected. The package structure and manifest activity names imply a dedicated preview/result workflow for invoice and estimate documents, backed by local template IDs and configuration assets.

## Apparent PDF Generation Approach

No readable PDF generation implementation was present in the extracted folder. The relevant architecture principle is local document generation tied to template configuration and export/result screens. Tijario will implement this independently with a local HTML/CSS renderer and Android PDF output.

## Apparent Pagination Approach

No readable pagination code was present. The reference app's template configuration suggests documents are laid out from reusable template metadata. Tijario will implement A4 page sizing, table wrapping, print CSS, and multi-page PDF output from one local renderer.

## Apparent Template Configuration Structure

The reference uses numeric template IDs and JSON metadata. Tijario will use original semantic IDs such as `tijario-classic` and local `template.json` plus `template.css` assets. The reference JSON structure will not be copied.

## Apparent Local Database or Model Relationship

No database or schema folder was found. The manifest references business, client, items, invoice, estimate, tax, terms, logo, and signature input activities. Tijario already gets authoritative saved document data from the backend and Room cache; the document engine will map that data into a canonical render model.

## FileProvider Setup

The reference binary manifest contains a FileProvider reference. Tijario already has a provider with `exported=false`, `grantUriPermissions=true`, and `@xml/file_paths`. The engine will expose only cached document PDFs from `cache/documents`.

## MediaStore or Downloads Behavior

No readable implementation was present. Tijario will use MediaStore Downloads for permanent saves on modern Android without broad storage permission.

## Printing Behavior

No readable implementation was present. Tijario will use Android Print Framework through a cached PDF adapter, ensuring print uses the same generated PDF.

## Email Behavior

No readable implementation was present. Tijario will create general email intents with `application/pdf` attachments via secure `content://` URIs and will not hardcode Gmail.

## General Sharing Behavior

No readable implementation was present. Tijario will use the Android Sharesheet for PDF sharing and keep text sharing separate.

## Performance Observations

The reference app packages local templates and metadata, which avoids network template loading. Tijario will follow that principle with local assets, deterministic cache keys, cache reuse, and no PDF generation on each keystroke.

## Useful Architectural Principles

- Keep template definitions local.
- Keep rendering, PDF output, file caching, and export lifecycle together.
- Use FileProvider for temporary sharing.
- Keep preview/result screens document-focused.
- Make template selection data-driven, not hardcoded into screen layout.

## Proprietary Elements Not Copied

No reference source, smali, template JSON, images, backgrounds, fonts, icons, commercial layouts, VIP resources, brand names, or exact visual compositions are copied. Tijario templates are original and use Tijario's own palette and legal local fonts already present in the app.

## Selected Original Tijario Strategy

Tijario will implement:

`Canonical Document Data -> Original Local HTML/CSS Template -> Secure WebView Preview -> Local PDF -> Cache -> View / Save / Print / Email / Share`

The renderer is local HTML/CSS because it supports Arabic RTL, English LTR, A4, long tables, local fonts, and ten maintainable original templates while keeping preview and PDF visually unified.
