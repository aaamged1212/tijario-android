# Current Project Baseline

This file is the authoritative implementation baseline for the Tijario Android repository.
Historical notes in other AI documents remain useful context, but they must not override this
current state without a newer dated entry.

## Repository

- Authoritative branch: `main`
- Current commit: `a826ca7`
- Remote state: `main` and `origin/main` are synchronized.
- Working tree at baseline creation: clean.
- Android project path: `C:\Users\BBOY AMG\Desktop\Projects\tjario-android`
- Web/API and Supabase code are maintained in their own repository and are not represented by
  Android Git state alone.

## Android baseline

- Kotlin and Jetpack Compose with Material 3.
- Arabic-first RTL experience with English LTR support.
- Room is the first-render and local operational source for supported offline data.
- Remote sync is authenticated, bounded, idempotent, and must not retry non-retryable failures.
- Database version: `23`.
- App version: `0.0.23` / versionCode `23`.
- Room migration 6 -> 7 has been repaired and validated on an emulator.
- Billing refresh and Google Play purchase acknowledgement handling are hardened.
- Local action analytics uses a bounded Room outbox and does not upload business-record contents.

## Verified checks

- `:app:assembleDebug`: passed.
- `:app:testDebugUnitTest`: passed in the latest external-terminal run.
- Android instrumentation suite: `OK (12 tests)`.
- `git diff --check`: passed before the latest commits.

## Release and external gates

These are not implied by a successful build:

- Physical-device QA for Arabic/English, RTL/LTR, offline/online transitions, Billing, backups,
  notifications, and Yemen payment flows.
- Compatible Web/API deployment and Supabase migration verification.
- Release AAB generation, signing verification, R8 mapping/native symbols retention, and Play
  Console submission when explicitly approved.
- End-to-end analytics verification from local event creation through authenticated upload and
  aggregate visibility.

## Source-of-truth rule

When code, Git history, and AI notes disagree, verify the repository and current test output first.
Then update this file and the required handoff/changelog files in the same change before claiming
the task complete.
