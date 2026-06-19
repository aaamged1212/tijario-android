# Android Environment Setup

## Required Tools

- JDK 17 or newer. Android Studio JBR is acceptable.
- Android SDK with API 37 platform and build tools.
- Gradle 9.4.1 or Gradle wrapper generated from a trusted official Gradle distribution.

## Current Local Findings

- Android Studio JBR exists at `C:\Program Files\Android\Android Studio\jbr`.
- `java` is not on PATH.
- `gradle` is not on PATH.
- Android SDK was not found in common local paths.
- An attempt to download/run the official Gradle 9.4.1 distribution exceeded the local command timeout. No wrapper files were created.

## Local Validation Commands

When the toolchain is available:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew :app:assembleDebug
.\gradlew :app:testDebugUnitTest
.\gradlew :app:lintDebug
```

Do not commit `local.properties`, SDK paths, or signing credentials.
