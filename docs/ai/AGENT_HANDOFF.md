# Agent Handoff (Android Repo)

- **Last Agent**: Codex
- **Date/Time**: 2026-07-10 04:50:53 +03:00
- **Repo**: Android Repo (`tjario-android`)
- **Branch**: `fix/reduce-mobile-vercel-usage-sync-retries-local`
- **Latest Commit At Start**: `a87fe05 fix: prevent Android document customer duplication and improve form feedback`
- **Current Uncommitted Files**:
  - app/src/main/java/app/tijario/config/Localization.kt
  - app/src/main/java/app/tijario/data/local/TijarioDao.kt
  - app/src/main/java/app/tijario/domain/PhoneNumber.kt
  - app/src/main/java/app/tijario/ui/screens/AuthScreens.kt
  - app/src/main/java/app/tijario/ui/screens/FormScreens.kt
  - app/src/main/java/app/tijario/ui/state/FormState.kt
  - app/src/test/java/app/tijario/data/local/DocumentOrderingSourceTests.kt
  - app/src/test/java/app/tijario/ui/screens/DocumentFormEditFlowTests.kt
  - app/src/test/java/app/tijario/ui/state/ProductFormStateValidationTests.kt
  - docs/ai/AGENT_HANDOFF.md
  - docs/ai/AI_CHANGELOG.md
  - docs/ai/PROJECT_STATE.md
  - docs/ai/PENDING_RELEASE.md
  - docs/ai/ACTIVE_AGENT.md

## Summary
- Android product stock validation now requires positive stock only for `ProductKind.Product`; services may keep stock empty.
- Android invoice create/edit now validates saved-product quantities against available stock before submit, while services and manual items are excluded.
- Android invoice edit stock validation now compares aggregated requested quantity by product against `current stock + original quantity from this invoice`.
- Onboarding now uses an MVP dial-code selector plus local phone input and normalizes the existing WhatsApp field.
- Recent documents now sort by synced/update timestamps first, with issue date as fallback; no Room migration was needed because existing columns are reused.
- Auth/onboarding screens now apply status/navigation/IME padding and width constraints to avoid language button/content overlap on compact devices.
- Onboarding no longer uses an overlaid language toggle; the toggle sits inside the scroll content and the long form is top-aligned.
- The onboarding logo picker already accepts loading state and remains disabled/spinner-covered during save.
- Added focused tests for product stock validation, phone normalization, invoice stock validation, and DAO recent-document ordering.

## Validation
- `.\gradlew.bat testDebugUnitTest` passed.
- `.\gradlew.bat assembleDebug` passed.
- `git diff --check` passed before doc updates; rerun after final docs.

## Safety Status
- No push.
- No Google Play upload.
- No closed-testing changes.
- No Supabase migration apply.
- No Web source changes from Android repo.
