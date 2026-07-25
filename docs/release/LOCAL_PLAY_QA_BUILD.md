# Local playQa Build

`playQa` is a local USB-debugging build. It keeps package `app.tijario`, uses the
local Play Upload Key signing configuration, is debuggable, and uses the same
BuildConfig values as Release. It disables Release shrinking for debugger-friendly
local QA. It is not a Google Play upload workflow.

## Android Studio

1. Open the project in Android Studio.
2. Open **Build Variants**.
3. Select `playQa` for the `app` module.
4. Connect a physical phone with USB debugging enabled.
5. Press **Run**.
6. Android Studio builds and installs the Upload-Key-signed APK.

Google Sign-In and Google Drive require an Android OAuth client in the existing
Google Cloud project for package `app.tijario` and Upload Key SHA-1:

`64:59:BD:F3:CC:B6:33:DF:A6:14:C6:10:52:DB:79:D7:4B:C9:36:6C`

Do not replace the existing Play App Signing OAuth client. If this Upload Key
OAuth client already exists, no Google Cloud change is required.

## Installation Limitation

A `playQa` APK cannot install over the Google Play build because Play uses a
different App Signing Key. To switch from Play to `playQa`, uninstall the Play
build first, then install `playQa`; this removes local app data.

After `playQa` is installed, later `playQa` USB builds use the same Upload Key
and normally update it without uninstalling. Use a dedicated test device/account
and avoid repeatedly switching between Play and `playQa`.
