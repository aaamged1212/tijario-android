# Android App Links

## Android-side support
- `https://tijario.site/auth/callback` and `https://www.tijario.site/auth/callback` are declared with `android:autoVerify="true"`.
- Existing `tijario://auth/callback` and `com.tijario.app://auth/callback` schemes remain supported for backwards compatibility.
- The app accepts only exact callback paths and only internal relative `next` paths. Scheme-relative, host-bearing, user-info, and malformed targets fall back to `/login`.

## Required external verification before release
1. Serve `/.well-known/assetlinks.json` from both HTTPS hosts without redirects.
2. Confirm the file lists `app.tijario` and the active Google Play app-signing SHA-256 certificate, not only a local/upload certificate.
3. Install a release-signed build, run `adb shell pm get-app-links app.tijario`, and verify both hosts are approved.
4. Test a real password-reset and OAuth callback. Keep the legacy custom scheme as fallback until this succeeds on supported Android versions.

No hosting, DNS, Google Play, or web changes were made by this Android-side change.
