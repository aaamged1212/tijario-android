# Tijario Android Agent Instructions

This repository contains the independent native Android application for Tijario.

## Source Of Truth

- The existing Tijario web/backend repository is the source of truth for product behavior, data model, Supabase tables, RLS boundaries, and secure backend API behavior.
- The Android app will use the same Supabase project and secure backend APIs.
- Do not create a new Supabase project.

## Android Stack

- Use Kotlin.
- Use Jetpack Compose.
- Use Material 3.
- Arabic RTL is the default experience.

## Repository Boundaries

- All Android application work belongs in this repository.
- Do not copy the Tijario web project into this repository.
- Do not add `.env` files.
- Do not add backend secrets.
- Do not add service-role keys.
- Do not add Replicate tokens.
- Do not add database passwords.
- Do not add signing secrets.

## Explicit Non-Goals

- No Flutter.
- No React Native.
- No Capacitor.
- No Cordova.
- No WebView application.
- No feature creep.
- No billing implementation unless explicitly authorized later.
- No WhatsApp API integration unless explicitly authorized later.
- No automatic deployment.
- No Google Play publishing unless explicitly authorized later.
- No npm audit fix.
- No new Supabase project.
- No database migrations unless explicitly authorized later.
- No RLS changes unless explicitly authorized later.
- No destructive Git commands.

## MVP Operating Rules

- Work in isolated feature branches.
- Commit one meaningful milestone at a time.
- Keep the existing Tijario web/backend repository read-only unless a task explicitly authorizes backend changes.
- Use authenticated HTTPS APIs for privileged operations such as document creation, PDF generation, and AI provider calls.
- Use direct Supabase access only where existing RLS safely permits the operation.
- Document blockers and validation results in `PLANS.md` and `docs/implementation-status.md`.
