# Local Data Architecture

Tijario Android is cache-first for user-facing business data.

## Rule

Screens must not fetch list, settings, or document summary data directly from Supabase. Composables read state from `TijarioDataViewModel`, which observes the local Room cache through `TijarioRepository`.

## Flow

1. The app checks the Supabase auth session.
2. `TijarioDataViewModel` starts observing Room for the authenticated user.
3. Cached data renders immediately.
4. `TijarioRepository.refreshAll()` syncs Supabase data in the background.
5. Room updates emit to the UI through `StateFlow`.

## Current Cached Data

- Business settings
- Customers
- Products and services
- Document summaries

## Write Path

- Customer and product creates go through `TijarioDataViewModel` and `TijarioRepository`.
- Business settings writes go through `TijarioDataViewModel` and update Room after a successful server save.
- Document creation stays on the secure backend API, then refreshes cached document summaries.

## Future Feature Checklist

- Add a Room entity and DAO queries for new persistent data.
- Add repository observe/refresh/write methods.
- Expose state through a ViewModel.
- Keep Composables focused on rendering and user events.
- Do not add direct Supabase table reads inside UI screens.
- Do not store service-role keys, Replicate tokens, database passwords, or signing secrets locally.
