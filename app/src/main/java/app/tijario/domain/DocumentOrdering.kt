package app.tijario.domain

import app.tijario.data.model.DocumentSummary
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun newestDocuments(
    documents: List<DocumentSummary>,
    limit: Int = documents.size,
): List<DocumentSummary> = documents
    .sortedWith(
        compareByDescending<DocumentSummary> { documentCreatedAtMillis(it) }
            .thenByDescending { it.id }
    )
    .take(limit)

private fun documentCreatedAtMillis(document: DocumentSummary): Long =
    parseDocumentTimestamp(document.createdAt)
        ?: parseDocumentIssueDate(document.issueDate)
        ?: Long.MIN_VALUE

private fun parseDocumentTimestamp(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
}

private fun parseDocumentIssueDate(value: String): Long? = runCatching {
    LocalDate.parse(value.substringBefore('T'))
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()
