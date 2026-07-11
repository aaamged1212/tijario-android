# Android Pricing And Entitlements

Android displays plan and usage data from the Tijario backend. Supabase remains the source of truth for all limits and entitlement decisions.

## Plans

- Free: 5 documents/month, 10 AI/month, 5 customers, 5 products, basic template only, Tijario branding shown.
- Starter: 50 documents/month, 150 AI/month, 30 customers, 30 products, all current templates, branding removed.
- Pro: 200 documents/month, 500 AI/month, unlimited customers/products, all current templates, branding removed.
- Business: 300 documents/month, 800 AI/month, unlimited customers/products, all current templates, branding removed, priority support.

Monthly and yearly subscriptions grant the same feature limits. Usage resets monthly for both intervals.

## Google Play Products

Create these subscription products in Google Play Console:

- `tijario_starter`
- `tijario_pro`
- `tijario_business`

Each product must have these base plan IDs:

- `monthly`
- `yearly`

The app queries `ProductDetails` and displays Google Play localized `formattedPrice`. Do not hardcode checkout prices in Android.

## Purchase Flow

- Android starts Google Play Billing only for paid products returned by Google Play.
- Purchases send only `product_id` and `purchase_token` to `/api/mobile/billing/google-play/verify`.
- The backend verifies package `app.tijario`, product ID, purchase token, base plan, state, expiry, and the authenticated Tijario account.
- Android acknowledges the purchase only after backend verification succeeds.
- Restore purchases re-verifies active Google Play subscriptions.

## Current Integration

- Billing status is fetched from `/api/mobile/billing/status`.
- Account usage remains available through `/api/mobile/account/usage`.
- Sync bootstrap/pull include expanded `plan_usage`.
- Template choice remains enforced by backend entitlements.

## Manual Testing

- Free document 1-5 succeeds, 6 is blocked by backend.
- Free customer/product 1-5 succeeds, 6 is blocked by backend.
- Deleting a Free customer/product frees a slot.
- Starter accepts 30 customers/products and blocks the 31st.
- Pro and Business do not block customer/product count.
- Monthly and yearly base plans display localized Google Play prices.
- Purchase is not acknowledged until backend verification succeeds.
- Restore purchases re-syncs entitlement after reinstall/login.
