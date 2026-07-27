package app.tijario.features.documents.model

/** Keeps generated document HTML local when a remote business logo is unavailable. */
fun resolveDocumentLogoForRender(
    logoUrl: String?,
    cachedLogoDataUrl: String?,
): String? {
    if (!cachedLogoDataUrl.isNullOrBlank()) return cachedLogoDataUrl
    return logoUrl?.takeUnless { it.startsWith("http", ignoreCase = true) }
}
