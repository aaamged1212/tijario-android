# Release Readiness

## Current Recommendation

Not release-ready.

## Missing Before Release

- Android SDK and Gradle validation.
- Debug APK build.
- Supabase configuration supplied securely outside Git.
- Secure mobile backend endpoints confirmed or implemented in web/backend repository.
- End-to-end auth validation.
- End-to-end onboarding, customer, document, PDF, WhatsApp share, AI, account, and usage validation.
- Secret scan over tracked files.
- Device or emulator RTL UI pass.

## Last Known Environment

- Android Studio JBR: available.
- Java on PATH: unavailable.
- Gradle on PATH: unavailable.
- Android SDK: not found in common local paths.
- Gradle wrapper setup: attempted with official Gradle 9.4.1, but the command timed out before wrapper files were created.
