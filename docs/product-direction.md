# Tijario Android Product Direction

## Purpose

Tijario Android is a native Android client for the Tijario commerce workflow. It should help Arabic-first small businesses manage customers, products and services, quotes, invoices, documents, PDF sharing, AI replies, AI captions, account status, and usage limits.

## Approved Direction

- Auth and session handling against the existing Tijario Supabase project.
- Onboarding for business settings required by documents.
- Dashboard focused on fast access to the core commerce tasks.
- Customer management.
- Product and service management.
- Quote creation and document management.
- Invoice creation and document management.
- PDF retrieval through secure backend APIs.
- WhatsApp share using Android intents, not the WhatsApp Business API.
- AI Reply and AI Caption through secure backend APIs, never direct Replicate calls from Android.
- Account and usage visibility based on existing plans and counters.

## Product Principles

- Arabic RTL first.
- Native Android interactions, not a wrapped website.
- Simple commerce workflows before advanced automation.
- Preserve the existing backend model and security boundaries.

## Forbidden Feature Creep

- No marketplaces.
- No inventory system beyond the existing product/service list.
- No accounting ledger.
- No tax engine unless explicitly authorized.
- No payment processing or billing implementation.
- No WhatsApp API automation.
- No new AI providers or direct client-side AI tokens.
- No new Supabase project.
- No schema migrations or RLS changes unless explicitly authorized.
- No web project copying.
