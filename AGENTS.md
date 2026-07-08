# Tijario Agent Protocol

This repo is part of Tijario / تجاريو.

Every AI coding agent working in this repo must automatically perform the PRE-FLIGHT protocol before modifying files and the POST-FLIGHT protocol after finishing work, even if the user does not explicitly ask.

## Product context
Tijario is an Arabic-first SaaS/PWA/native Android app for WhatsApp and Instagram sellers.

Priorities:
- simplicity
- fast MVP
- revenue
- mobile-first UX
- Arabic RTL
- Supabase backend
- no WhatsApp API in MVP
- no complex CRM
- no feature creep

## Android Stack & Architecture Guidelines
- Use Kotlin, Jetpack Compose, and Material 3.
- Arabic RTL is the default experience.
- The existing Tijario web/backend repository is the source of truth for product behavior, data model, Supabase tables, RLS boundaries, and secure backend API behavior.
- Use the local Room cache as the first-render source for list, settings, and document summary data; refresh from Supabase in the background. UI screens must remain cache-first. New data features must flow through Room entities/DAO, `TijarioRepository`, and shared ViewModels instead of calling Supabase directly from Composables.
- Use authenticated HTTPS APIs for privileged operations such as document creation, PDF generation, and AI provider calls.
- Use direct Supabase access only where existing RLS safely permits the operation.
- Do not add `.env` files, database passwords, backend secrets, or service-role keys.

## Current safety context
- Google Play Closed Testing is active.
- Do not upload Android builds unless explicitly approved.
- Do not deploy Vercel unless explicitly approved.
- Do not push to GitHub unless explicitly approved.
- Do not apply Supabase migrations unless explicitly approved.
- Do not change production environment variables.
- Do not change billing, Facebook SDK, or Google Play settings unless explicitly approved.

## Mandatory pre-flight before every task
Before editing any file, automatically:
1. Read AGENTS.md.
2. Read docs/ai/PROJECT_STATE.md.
3. Read docs/ai/AGENT_HANDOFF.md.
4. Read docs/ai/DECISIONS.md.
5. Read docs/ai/PENDING_RELEASE.md.
6. Read docs/ai/DO_NOT_TOUCH.md.
7. Read docs/ai/WORKFLOW_PROTOCOL.md.
8. Run:
   - git status --short
   - git branch --show-current
   - git log --oneline -8
   - git diff --stat
   - git diff --name-only
9. Identify uncommitted and untracked files.
10. Report understanding before modifying source code.

If there are unexpected dirty files, do not overwrite them. Ask/report first.

## Mandatory post-flight after every task
After finishing any task, automatically:
1. Update docs/ai/AGENT_HANDOFF.md.
2. Append a short entry to docs/ai/AI_CHANGELOG.md.
3. Update docs/ai/PROJECT_STATE.md if project state changed.
4. Update docs/ai/PENDING_RELEASE.md if migration/deploy/release status changed.
5. Update docs/ai/ACTIVE_AGENT.md.
6. Run relevant tests/builds if code changed.
7. Run git diff --check.
8. Report:
   - files changed
   - tests/builds run
   - remaining risks
   - pending release steps
   - explicit confirmation that no push/deploy/migration/Google Play upload happened unless approved.

## Customer identity decision
customer.id is the identity of a customer.
whatsapp_number is only a contact field.
Multiple customers under the same seller may share the same WhatsApp number.
Do not resolve or merge customers by whatsapp_number.
Do not link documents to customers by whatsapp_number.
Documents must link by customer_id or local-to-server customer ID mapping.

## Mobile sync decision
Mobile sync must not enter retry loops.
Validation/duplicate/non-retryable errors must not be retried endlessly.
Retryable failures must use max retries and backoff.
Vercel calls must be minimized.

## Release decision
Pending migrations must not be applied alone.
Correct release order for duplicate WhatsApp change:
1. Apply Supabase migration.
2. Deploy compatible Web/API code.
3. Verify Vercel logs.
4. Android update later if needed.

## Forbidden actions without explicit approval
- git push
- Vercel deploy
- Supabase production migration
- Google Play upload
- Google Play closed testing changes
- production env changes
- service-role exposure
- adding paid infrastructure
- adding WhatsApp API
- adding complex CRM features
