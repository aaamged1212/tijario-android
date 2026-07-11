# Tijario Document Template Specification

## Templates

The engine includes exactly ten original Tijario templates:

1. `tijario-classic` - Tijario Classic
2. `tijario-modern` - Tijario Modern
3. `tijario-minimal` - Tijario Minimal
4. `tijario-compact` - Tijario Compact
5. `tijario-corporate` - Tijario Corporate
6. `tijario-retail` - Tijario Retail
7. `tijario-elegant` - Tijario Elegant
8. `tijario-bold` - Tijario Bold
9. `tijario-service` - Tijario Service
10. `tijario-premium` - Tijario Premium

Each template has:

- `template.json`
- `template.css`
- registry entry in `DocumentTemplateRegistry`

## Design Differences

- Classic: split header, bordered cards, traditional balance.
- Modern: solid deep teal header band.
- Minimal: quiet monochrome treatment and reduced borders.
- Compact: tighter padding and dense table spacing.
- Corporate: blue structured business layout and squared cards.
- Retail: side accent and alternating item rows.
- Elegant: centered identity and softer quote-friendly hierarchy.
- Bold: assertive title, red accent, emphasized final total.
- Service: single-column party treatment for service businesses.
- Premium: warm premium top rule and tinted metadata surfaces.

The differences are layout and hierarchy differences, not color-only variations.

## Required Content

Every template renders:

- business logo fallback mark
- business name
- business contact label as `الرقم` or `Number`
- city/country when present
- document title
- document number
- issue date
- document status when present
- invoice payment status only for invoices
- customer section
- item table
- totals
- non-empty notes and terms
- footer

## Arabic Labels

- Document title: `فاتورة` or `عرض سعر`
- Customer section: `بيانات العميل`
- Item columns: `البيان`, `الكمية`, `سعر الوحدة`, `الإجمالي`
- Totals: `المجموع الفرعي`, `الخصم`, `الرسوم الإضافية`, `الإجمالي النهائي`
- Footer: `تم إنشاء هذا المستند عبر تجاريو`

## Forbidden Document Wording

The generated document must not use:

- `واتساب`
- `رقم واتساب`
- `WhatsApp`
- WhatsApp icons

This rule applies only inside document output and does not remove WhatsApp sharing features elsewhere.

## Adding a New Original Template

1. Create `app/src/main/assets/documents/templates/<id>/template.json`.
2. Create `app/src/main/assets/documents/templates/<id>/template.css`.
3. Add a `DocumentTemplateDefinition` entry to `DocumentTemplateRegistry`.
4. Ensure the new template has a distinct layout family and a positive version.
5. Add or update tests proving registry validity and asset existence.
6. Do not copy commercial templates or assets.
