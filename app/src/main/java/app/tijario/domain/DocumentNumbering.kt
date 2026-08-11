package app.tijario.domain

import app.tijario.data.model.DocumentType
import java.util.Locale

object DocumentNumbering {
    const val DEFAULT_DIGIT_WIDTH = 5

    private data class ParsedNumber(
        val sequence: Long,
        val width: Int,
    )

    fun firstDocumentNumber(type: DocumentType): String = format(type, 1L)

    fun nextDocumentNumber(existingNumbers: Iterable<String>, type: DocumentType): String {
        val latest = existingNumbers.asSequence()
            .mapNotNull { extractSequence(it, type) }
            .maxByOrNull { it.sequence }
        return format(
            type = type,
            sequence = (latest?.sequence ?: 0L) + 1L,
            width = latest?.width ?: DEFAULT_DIGIT_WIDTH,
        )
    }

    /**
     * A valid user-entered number is authoritative for the document being created.
     * The number is normalized only enough to keep the canonical type prefix.
     */
    fun resolveForCreate(
        requestedNumber: String?,
        existingNumbers: Iterable<String>,
        type: DocumentType,
    ): String = requestedNumber
        ?.trim()
        ?.takeIf { extractSequence(it, type) != null }
        ?: nextDocumentNumber(existingNumbers, type)

    fun canonicalPrefix(type: DocumentType): String =
        if (type == DocumentType.Invoice) "INV-" else "Q-"

    fun editableDigits(number: String, type: DocumentType): String {
        val parsed = extractSequence(number, type) ?: return number
        return parsed.sequence.toString().padStart(parsed.width, '0')
    }

    private fun format(type: DocumentType, sequence: Long, width: Int = DEFAULT_DIGIT_WIDTH): String {
        val digits = sequence.toString()
        return canonicalPrefix(type) + digits.padStart(maxOf(width, digits.length), '0')
    }

    private fun extractSequence(value: String, type: DocumentType): ParsedNumber? {
        val normalized = value.trim().uppercase(Locale.US)
        val prefixes = if (type == DocumentType.Invoice) {
            "INV"
        } else {
            "(?:Q|QT)"
        }
        val match = Regex("^$prefixes[-_\\s]?(\\d+)$").find(normalized) ?: return null
        val digits = match.groupValues.getOrNull(1).orEmpty()
        val sequence = digits.toLongOrNull() ?: return null
        return ParsedNumber(sequence = sequence, width = digits.length)
    }
}
