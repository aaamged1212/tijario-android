package app.tijario.features.documents.pdf

object PdfFileNameSanitizer {
    private val invalid = Regex("""[\\/:*?"<>|\p{Cntrl}]""")

    fun sanitize(value: String, fallback: String = "document"): String {
        val cleaned = value
            .replace(invalid, "-")
            .replace("..", "-")
            .trim('-', '.', ' ')
            .take(80)
        return cleaned.ifBlank { fallback }
    }
}
