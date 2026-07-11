# Notifications Platform Architecture

## Overview

Tijario uses two channels:

- Supabase announcements database as the source of truth.
- Firebase Cloud Messaging as the push alert channel.

Users still see announcements in the app if push permission is denied, a push is missed, or the app is opened later.

## Android Flow

1. Authenticated user reaches the app after onboarding.
2. App asks for notification permission with explanation.
3. If enabled, app subscribes to one topic for the current language.
4. App reads notifications from Room first.
5. App refreshes from `/api/mobile/announcements/bootstrap`.
6. Seen/read/dismissed events update Room immediately.
7. Offline receipt events are queued and synced later with WorkManager.

## Local Tables

- `announcements_cache`
- `announcement_receipt_outbox`

Data is scoped by `user_id` so another signed-in user does not inherit read state.

## Deep Links

Allowed announcement deep link:

`tijario://announcements/{announcement_id}`

Unsupported schemes are ignored.
