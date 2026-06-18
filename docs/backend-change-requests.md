# Backend Change Requests

## Open Requests

The Android client needs secure mobile-friendly JSON endpoints if the current web project exposes only server actions for these workflows:

- `POST /api/mobile/documents` for quote/invoice creation.
- `PUT /api/mobile/documents/{id}` for document update.
- `GET /api/mobile/documents/{id}/pdf` for PDF retrieval.
- `POST /api/mobile/ai/reply` for AI Reply.
- `POST /api/mobile/ai/caption` for AI Caption.

## Requirements

- Accept `Authorization: Bearer <Supabase access token>`.
- Verify user server-side.
- Reuse existing Tijario business logic.
- Never expose service-role or provider credentials.
- Return stable JSON contracts.
- Preserve existing web behavior.

## Status

Not yet implemented in this Android repository. The web/backend repository remains read-only unless explicitly authorized in a separate backend task.
