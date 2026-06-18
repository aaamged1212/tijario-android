# Backend Contract

## Confirmed Supabase Tables

- `profiles`
- `business_settings`
- `customers`
- `products`
- `documents`
- `document_items`
- `ai_generations`
- `usage_counters`
- `plans`
- `user_plan`

## Confirmed RLS Boundaries

- Users can select, insert, and update their own profile.
- Users can select, insert, and update their own business settings.
- Users can select, insert, update, and delete their own customers.
- Users can select, insert, update, and delete their own products.
- Users can select their own documents and document items.
- Document and document item writes are scoped to the authenticated user and related owned customer/product rows.
- Users can select their own AI generations and usage counters.
- Active plans are visible to anonymous and authenticated clients.
- Users can select their own user plan.

## Android-Public Configuration

Android may receive these values through ignored local configuration or secure CI injection:

- Supabase project URL.
- Supabase publishable key or anon key.
- Tijario public API base URL.

## Forbidden Android Secrets

- Supabase service-role key.
- Replicate token.
- Database password.
- JWT signing secret.
- PDF secret.
- Application secret.
- Vercel server secrets.
- Signing credentials.

## Direct Supabase Operations

Allowed where existing RLS permits:

- Auth session actions.
- Profile read/update.
- Business settings read/update.
- Customer CRUD.
- Product CRUD.
- Documents and document items read.
- Plan and usage read.

## Privileged Secure API Operations

These require authenticated HTTPS APIs with `Authorization: Bearer <Supabase access token>`:

- Quote creation.
- Invoice creation.
- Document update when authoritative totals or usage rules apply.
- PDF generation/retrieval.
- AI Reply.
- AI Caption.
- Usage-limit enforcement and usage increments.

## Required API Shape

The Android app expects stable JSON responses:

- `ok: Boolean`
- `code: String?`
- `message: String?`
- resource payload fields for success cases

Arabic user-facing errors should be returned when the server can determine a business-rule failure.
