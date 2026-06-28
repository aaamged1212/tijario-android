package app.tijario.domain

import app.tijario.data.model.DocumentType
import java.util.Locale

object DocumentNumbering {
    private const val DIGIT_WIDTH = 4

    fun firstDocumentNumber(type: DocumentType): String = format(type, 1)

    fun nextDocumentNumber(existingNumbers: Iterable<String>, type: DocumentType): String {
        val next = existingNumbers.asSequence()
            .mapNotNull { extractSequence(it, type) }
            .maxOrNull()
            ?.plus(1)
            ?: 1
        return format(type, next)
    }

    private fun format(type: DocumentType, sequence: Int): String {
        val prefix = if (type == DocumentType.Invoice) "INV-" else "QT-"
        return prefix + sequence.toString().padStart(DIGIT_WIDTH, '0')
    }

    private fun extractSequence(value: String, type: DocumentType): Int? {
        val normalized = value.trim().uppercase(Locale.US)
        val prefix = if (type == DocumentType.Invoice) "INV" else "QT"
        val match = Regex("^$prefix[-_\\s]?(\\d+)").find(normalized) ?: return null
        return match.groupValues.getOrNull(1)?.toIntOrNull()
    }
}
