package app.tijario.data.repository

/**
 * A document number returned by the backend is authoritative.
 *
 * Android may show a local preview before saving, but it must never overwrite the
 * number allocated atomically by the server when caching the saved document.
 */
@Suppress("UNUSED_PARAMETER")
internal fun resolveCachedDocumentNumber(
    serverNumber: String,
    requestedPreviewNumber: String?,
): String = serverNumber
