# Backup Format V1

Status: partially implemented locally. The encrypted archive codec, logical Room export/transactional restore, staged asset restore/rollback, local asset collection and missing-PDF regeneration, verified local-file finalization, Android Keystore device wrapping, and SAF export/import UI exist. Backend migrations/deployment, scheduling, Drive transport, and device QA remain incomplete.

## Key Handling

- Android generates an RSA-2048 OAEP key pair inside Android Keystore per account installation.
- The backend stores only an AES-256-GCM server-encrypted random account backup key.
- Android receives that account key wrapped to its device public key with RSA-OAEP-SHA256 and caches only the wrapped envelope per key version.
- Plaintext account backup keys exist only transiently in memory and are zeroed by the coordinator after archive work.
- Archive restore reads the key version from the authenticated header and resolves that exact cached/server version, preserving restore after key rotation.
- A new device requires authentication and network access once to obtain its device-wrapped account key. After that, local creation/restore can work offline.

## File Type

Backups use a versioned logical archive with a `.tijario` extension:

`Tijario-Backup-YYYY-MM-DD-HHmm.tijario`

The archive must be encrypted. Do not export or upload an unencrypted SQLite database.

## Creation Rules

- A backup must be creatable completely offline.
- Backup generation must not require Google Drive or internet.
- Write to a temporary file first.
- Finalize only after encryption and checksum verification succeed.
- If one PDF cannot be generated, keep the structured data and record the PDF failure in the manifest.

## Restore Rules

- A local `.tijario` file must be restorable offline.
- Restore into a temporary database or staging area first.
- Validate archive version, account ownership, required records, checksums, and manifest counts before replacing active data.
- Keep the active database unchanged if any restore validation fails.
- Stage assets first and keep rollback copies. If asset application or the Room transaction fails, restore the previous files and database state.
- Restoring a backup never resets or decreases account usage.

## Required Archive Contents

- `manifest.json`
- `checksums.json`
- business settings
- customers
- customer snapshots
- products and services
- invoices
- quotations
- drafts
- document items
- document metadata
- document creation events
- deleted-record history or tombstones
- taxes
- payment methods
- signatures
- terms and conditions
- latest local entitlement display state needed offline
- backup settings
- assets/business-logo
- assets/product-images
- assets/document-pdfs

Structured document data and PDF files are both mandatory. PDF-only backup is not acceptable because restored documents must remain editable. Structured-data-only backup is not sufficient because users must retain generated document files.

## Document Fields To Preserve

Every document backup entry must include:

- document ID
- document number
- document type
- document title
- document language
- issue date
- creation date
- status
- payment status
- amount paid
- customer reference
- customer snapshot
- items and product references
- subtotal
- discount and discount label
- tax name, tax rate, and tax amount
- extra fees and extra-fee label
- shipping amount and label where supported
- total
- currency
- notes
- terms
- signature reference or snapshot
- payment-method reference or snapshot
- template ID
- local metadata
- revision information
- deletion state
- PDF-generation state
- local PDF path
- immutable document creation event

## Manifest Fields

The manifest must contain:

- backup format version
- Room database version
- app version
- minimum supported app version
- account ID
- installation/device ID
- backup sequence number
- backup creation time
- encryption version
- key version
- record counts
- document count
- document creation-event count
- PDF included count
- PDF missing count
- PDF failed-generation count
- archive checksum
- account ownership information

## PDF Handling

For each document:

1. Include complete structured data.
2. Include the latest available PDF.
3. Include a checksum for the PDF.
4. Record the PDF generation revision.
5. Record PDF generation status in the manifest.

If no PDF exists:

- attempt local PDF generation during backup preparation
- do not require internet
- do not fail the full backup only because one PDF failed
- mark the document as requiring PDF regeneration after restore
- allow local PDF regeneration after restore
