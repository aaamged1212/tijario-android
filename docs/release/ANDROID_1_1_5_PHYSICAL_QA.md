# Android 1.1.5 Physical QA Findings

Status: OPEN. These findings are confirmed from physical-device testing and are not fixed by the 1.1.5 backup, onboarding, and `playQa` changes.

## Environment

- Production Supabase authentication was reachable from Yemen without a VPN.
- Local `playQa` builds use the Upload Key. The Google Play Internal Testing build uses the Play App Signing key.
- The local 1.1.4 Upload-Key build was rebuilt from commit `0ab59dc48f6693e4a47e3017955ae8afc890e770` for the Google authentication comparison.

## Confirmed Working: Email And Password Authentication

1. Open the app from Yemen without a VPN.
2. Sign in with an email address and password.
3. Continue through normal app startup.

Result: authentication and normal startup succeed. Supabase reachability is not the current blocker.

## OPEN: Local Google Native Sign-In

1. Install the local Upload-Key-signed `playQa` APK.
2. Open Login or Register and start Google sign-in.
3. Complete the Google account selection and consent flow.

Result: native Google sign-in remains unresolved locally. The same behavior occurs with the exact local 1.1.4 source signed by the Upload Key, while the Google Play-distributed 1.1.4 build works.

Next investigation: compare the Upload-Key Android OAuth client, SHA-1 registration, OAuth propagation, and Play App Signing client configuration. Do not conclude that this is caused by 1.1.5 code.

## OPEN: Current Plan Card

1. Sign in and open Settings.
2. Observe the current-plan card.
3. Tap Retry.

Result: the card reports that plan data could not be updated and Retry has no visible result.

Next investigation: establish the entitlement request, response, verification, and UI-state path before changing plan logic.

## OPEN: Upgrade Catalog

1. Open the Upgrade screen.
2. Inspect the displayed plan cards.

Result: Free, Starter, Pro, and Business are displayed. The expected current catalog is Free, Starter, and Pro only.

Next investigation: identify the catalog source and remove stale Business data only after contract confirmation.

## OPEN: Purchase Synchronization

1. Complete a Google Play purchase until Play reports confirmation.
2. Return to Tijario.
3. Observe the plan refresh result and current-plan card.

Result: Android reports that the plan could not be updated and the current plan does not change.

Next investigation: verify backend purchase verification, billing mapping, `user_plan`, and signed-entitlement refresh. Do not attribute this only to `playQa`.

## OPEN: Document Deletion

1. Open an existing invoice or document.
2. Delete the document.

Result: Android displays a generic unexpected-error message.

Next investigation: trace the deletion request, server response, and localized error mapping before changing quota or deletion behavior.

## QA Completion

Physical QA is incomplete. Google sign-in, Drive consent/folder/upload/restore, plan refresh, purchase synchronization, and document deletion require separate diagnosis and validation.
