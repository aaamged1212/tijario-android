# Release Readiness

## 2026-06-20 Local Document Engine Update

- Android document preview and export now use a local canonical renderer.
- Backend document creation and official totals remain authoritative.
- The backend PDF endpoint remains available but is no longer the primary Android renderer.
- FileProvider scope is limited to cached document PDFs and app-owned exported document files.
- Permanent save uses MediaStore Downloads on Android 10+ and an app-owned external downloads fallback on API 26-28 without broad storage permission.
- Ten original Tijario templates are included locally.
- Unit coverage validates renderer safety, template registry integrity, cache invalidation, filename sanitization, and contact wording rules.

### Remaining Release Validation

- Open generated PDFs on a real device or emulator.
- Confirm preview/PDF/print visual equivalence for long documents.
- Confirm sharesheet and email attachment behavior with installed target apps.
- Confirm MediaStore save behavior across supported Android versions.

Ready for staging. The Core MVP Completion phase is fully implemented.

## Validation Results (Core MVP Completion Phase)

- **Gradle Build**: Successful (`./gradlew assembleDebug` passed).
- **Unit Tests**: Successful (`./gradlew testDebugUnitTest` passed).
- **Lint Check**: Successful (`./gradlew lintDebug` passed).
- **Secret Scan**: Checked. No API tokens, backend keys, database passwords, or signing secrets are present.
- **Backend Sync status**: Web/Backend repository was not modified by this Android task. Current local web status is not clean and contains pre-existing local changes in `src/app/(auth)/actions.ts` and `src/app/(auth)/verify-email/`.

## Validation Results (Local Document Engine Phase)

- **Kotlin Compile**: Successful (`./gradlew compileDebugKotlin` passed).
- **Unit Tests**: Successful (`./gradlew testDebugUnitTest` passed).
- **Lint Check**: Successful (`./gradlew lintDebug` passed).
- **Debug APK Build**: Successful (`./gradlew assembleDebug` passed).
- **Device Smoke Test**: Successful (`adb install -r app-debug.apk` passed; launching `app.tijario/.MainActivity` showed no immediate `AndroidRuntime` fatal crash in the first logcat sample).
- **Secret Scan**: Checked. No API tokens, backend keys, database passwords, signing secrets, `.env` files, `.jks`, `.keystore`, or `keystore.properties` files are tracked.

## Completed Core MVP Features
- **Multiple Document Items**: Full addition, deletion, and binding of items in `DocumentFormScreen` powered by precise local `BigDecimal` calculations.
- **Saved Document Detail**: Direct secure API query resolving complete nested document metrics by `documentId`.
- **Native PDF Workflow**: Ktor streaming cache downloads, `FileProvider` URI generation, share intents, and fallback viewers.
- **Customer & Product CRUD**: Authed client-scoped insertion, update, and restricted constraints verification blocking destructive actions.
- **Plan Usage Surface**: Direct parsing of current active `period_month` records matching client buttons disabling policies.
- **Structured AI Generation**: Styled forms with platforms/tones configurations and clipboards copies confirmation indicators.
- **Feature Packages Boundaries**: Logic files separated cleanly out of monolithic core composable files.
