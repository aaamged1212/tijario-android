# Document Template Visual QA

## Status

This pass completed static rendering tests, unit tests, Kotlin compilation, lint, debug build validation, debug APK installation, and a basic device launch smoke test. Full manual visual QA of actual generated PDFs was not completed because it requires creating/opening generated PDFs for real document fixtures inside the Android runtime.

## Fixtures Covered Programmatically

Unit fixtures cover:

- draft invoice
- saved invoice
- saved quotation
- paid invoice
- quotation with payment status input omitted from output
- no copied external template assets
- Arabic RTL labels
- English labels
- HTML escaping with script-like strings
- discount and extra fee visibility
- zero discount omission
- zero extra-fee omission
- cache invalidation by template, locale, and revision
- safe filename generation

## Device Smoke Check

- `adb devices` detected device `ce071717b91bec12047e`.
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` completed with `Success`.
- `adb shell am start -n app.tijario/.MainActivity` started the app.
- The first short `logcat` sample after launch did not show an immediate `FATAL EXCEPTION` or `AndroidRuntime` crash for `app.tijario`.

## Manual Visual QA Matrix

| Fixture | Templates | Pages | Result |
| --- | --- | --- | --- |
| one/two item invoice | 10 templates | unverified | Pending device/PDF viewer inspection |
| quotation | 10 templates | unverified | Pending device/PDF viewer inspection |
| long document 30+ items | 10 templates | unverified | Pending device/PDF viewer inspection |
| long Arabic text | 10 templates | unverified | Pending device/PDF viewer inspection |
| mixed Arabic/English | 10 templates | unverified | Pending device/PDF viewer inspection |

## Programmatic Checks Passed

- Registry has exactly ten templates.
- Template IDs are unique.
- Template layout families are unique.
- Template assets exist.
- Generated Arabic output contains `الرقم`.
- Generated English output contains `Number`.
- Generated document output does not include WhatsApp wording.
- Quotation output omits visible payment badge.
- HTML escaping works for `<`, `>`, `&`, `"`, `'`, and script-like strings.

## Remaining Limitations

- Actual PDF pagination needs device/emulator inspection.
- Repeated table headers on later pages need visual verification.
- Print output equivalence needs Android print framework inspection.
- Email and sharesheet UX need real-device app availability testing.

## Final QA State

Pass for static and unit validation. Manual visual QA remains an external-runtime validation item, not a code blocker for local compilation and unit tests.
