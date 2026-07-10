# Agent Handoff (Android Repo)

- **Last Agent**: Codex
- **Date/Time**: 2026-07-10 02:30:20 +03:00
- **Repo**: Android Repo (`tjario-android`)
- **Branch**: `fix/reduce-mobile-vercel-usage-sync-retries-local`
- **Latest Commit At Start**: `b20ea29 docs: record production migration runbook handoff`
- **Current Uncommitted Files**:
  - app/src/main/java/app/tijario/data/remote/ApiContracts.kt
  - app/src/main/java/app/tijario/data/repository/TijarioRepository.kt
  - app/src/main/java/app/tijario/ui/screens/FormScreens.kt
  - app/src/test/java/app/tijario/data/remote/DocumentTaxContractTests.kt
  - app/src/test/java/app/tijario/data/repository/TijarioRepositoryOfflineTests.kt
  - app/src/test/java/app/tijario/ui/screens/DocumentFormEditFlowTests.kt
  - docs/ai/AGENT_HANDOFF.md
  - docs/ai/AI_CHANGELOG.md
  - docs/ai/PROJECT_STATE.md
  - docs/ai/ACTIVE_AGENT.md

## Summary
- Added `customer.id` to Android document create/update payloads so selected existing customers remain linked by `customer.id`.
- Updated local fallback `createDocumentLocal()` to use an existing customer id when present and avoid creating/enqueueing a duplicate customer.
- Kept duplicate WhatsApp numbers allowed; no WhatsApp-based merge/linking was added.
- Replaced document-language inline dropdown with a `ModalBottomSheet` selector.
- Moved document validation/save/update errors to a bottom `SnackbarHost`; removed the inline error text from the payment/status card.
- Added document-title helpers so default invoice/quote titles follow the selected document language while preserving custom titles.

## Validation
- `.\gradlew.bat testDebugUnitTest` passed.
- `.\gradlew.bat assembleDebug` passed.
- `git diff --check` passed before doc updates; rerun after final docs.

## Safety Status
- No push.
- No Google Play upload.
- No closed-testing changes.
- No Supabase migration apply.
- No Web source changes.
