# Pricing And Entitlements

Android reads plan usage from the backend and caches it for display only. Backend/Supabase remains the source of truth for all limits.

## Plans

- Free: 5 documents/month, 10 AI/month, 10 customers, 10 products, basic green/white template only.
- Pro: 50 documents/month, 100 AI/month, unlimited customers/products, all current templates.
- Business: 200 documents/month, 500 AI/month, unlimited customers/products, all current templates, priority support.

Documents means quotes plus invoices.

## Google Play Product IDs

Create subscription products/base plans in Google Play Console:

- `tijario_pro_monthly`
- `tijario_pro_yearly`
- `tijario_business_monthly`
- `tijario_business_yearly`

Do not hardcode Google Play prices as the final source. Purchase results from the device must be verified by the backend before a paid plan is granted.

## Current Integration

- Account usage is fetched from `/api/mobile/account/usage`.
- Sync bootstrap/pull include expanded `plan_usage`.
- Template choice sends `template_id` to the backend.
- Locked template state is driven by `allowed_template_ids` when plan usage is available.

## Manual Testing

- Free document 1-5 succeeds, 6 is blocked by backend.
- Free customer/product 11 is blocked by backend.
- Deleting a Free customer/product frees a slot.
- Free paid-template attempts are rejected by backend.
- Pro and Business can use all current templates.
