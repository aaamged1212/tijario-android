# Data Ownership Matrix

Status: planning only. No schema change is applied by this document.

| Entity | Primary owner in V1 | Local offline access | Supabase role | Google Drive backup | Notes |
| --- | --- | --- | --- | --- | --- |
| Account identity | Supabase Auth | read cached session only | source of truth | manifest account binding | Required before restore. |
| Profile | Supabase | cached display | source of truth | optional metadata | Do not trust backup to change account identity. |
| Plan entitlement | Supabase signed payload | cached signed entitlement | source of truth | include latest display state | Used for offline gating until expiry. |
| Billing subscription | Supabase/backend | display only | source of truth | no sensitive provider payload | Android must not own billing truth. |
| Device registration | Supabase + Room | yes | validates primary device | manifest device ID | V1 uses one primary device. |
| Offline quota lease | Supabase + Room | yes | issues/renews lease | include lease/event state | Lease is not a usage counter. |
| Document creation event | Supabase + Room | yes | authoritative after acknowledgement | mandatory | Immutable and never deleted on successful sync. |
| Business settings | Room | full CRUD | control-plane bridge during migration | mandatory | Legacy cloud rows retained initially. |
| Customers | Room | full CRUD | migration/legacy bridge | mandatory | `whatsapp_number` is contact only, not identity. |
| Customer snapshots | Room | full CRUD | proposed future table if needed | mandatory | Preserve historical document accuracy. |
| Products/services | Room | full CRUD | migration/legacy bridge | mandatory | Product images included as assets. |
| Documents | Room | full CRUD | usage-event acknowledgement only in final V1 | mandatory | Invoices, quotations, and drafts remain editable offline. |
| Document items | Room | full CRUD | legacy bridge during migration | mandatory | Restore must preserve item order and product references. |
| Document numbers | Room + control plane sequence rules | full | proposed idempotent allocator/reconciler | mandatory | Backup restore must not reset usage or duplicate counted operations. |
| Taxes | Room | full CRUD | no required V1 ownership | mandatory | Local document calculation input. |
| Payment methods | Room | full CRUD | no required V1 ownership | mandatory | Snapshot/reference stored on document. |
| Signatures | Room + local assets | full CRUD | no required V1 ownership | mandatory | Include asset and metadata. |
| Terms | Room | full CRUD | no required V1 ownership | mandatory | Snapshot/reference stored on document. |
| Business logo | Room metadata + local asset | yes | optional legacy URL | mandatory | Include asset bytes and checksum. |
| Product images | Room metadata + local asset | yes | optional legacy URL | mandatory | Include asset bytes and checksum. |
| Generated PDFs | local file store | yes | optional server generation remains legacy | mandatory | PDF-only backup is not enough. |
| AI prompts/results | Supabase/backend for usage | limited cache | source of truth for usage | optional user-visible cache | AI requires internet. |
| Announcements | Supabase | cache only | source of truth | optional | Receipt sync remains network-bound. |
| Backup records | Room | yes | optional envelope/status metadata | Drive transport status | Must track size, checksum, created/uploaded time, file ID, last error. |

## Non-Identity Fields

- `whatsapp_number` is never a customer identity.
- Document links use `customer_id` or a local-to-server ID map during migration.
- Product references use product IDs, not names or prices.

## Anti-Tampering Boundaries

- Local cache row counts are not authoritative usage.
- Backup restore cannot lower server usage.
- Replayed `operation_id` must be idempotent.
- Same user/document and user/operation combinations must not be counted twice.

