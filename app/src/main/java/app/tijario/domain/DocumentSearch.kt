package app.tijario.domain

import app.tijario.data.model.DocumentSummary

fun filterDocumentsBySearch(
    documents: List<DocumentSummary>,
    customerNamesById: Map<String, String>,
    query: String,
): List<DocumentSummary> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return documents
    return documents.filter { document ->
        document.documentNumber.contains(normalizedQuery, ignoreCase = true) ||
            customerNamesById[document.customerId]
                ?.contains(normalizedQuery, ignoreCase = true) == true
    }
}
