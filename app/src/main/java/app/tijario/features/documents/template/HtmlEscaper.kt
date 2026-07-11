package app.tijario.features.documents.template

object HtmlEscaper {
    fun escape(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return buildString(value.length) {
            value.forEach { char ->
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(char)
                }
            }
        }
    }
}
