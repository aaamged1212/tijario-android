# Architecture Decisions

## ADR-001: Native Android Only

Use Kotlin, Jetpack Compose, and Material 3. Do not use Flutter, React Native, Capacitor, Cordova, or a WebView wrapper.

## ADR-002: Arabic RTL First

The default locale and layout direction are Arabic RTL. English or LTR support can be added later only if explicitly required.

## ADR-003: Existing Backend Is Source Of Truth

The Android app is a new client for the existing Tijario system. It must reuse the same Supabase project, database, auth users, plans, usage counters, documents, and secure backend APIs.

## ADR-004: Privileged Operations Stay Server-Side

Document numbering, authoritative totals, document usage increments, AI provider calls, AI usage increments, PDF generation, service-role operations, and provider-token operations stay on the server.

## ADR-005: Simple Feature-Based Structure

Use one Android app module and feature-based packages. Avoid unnecessary multi-module or excessive Clean Architecture layers until there is a demonstrated need.
