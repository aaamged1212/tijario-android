package app.tijario.domain

import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DashboardStatsCalculator {
    fun calculateCurrentMonthEarnings(documents: List<DocumentSummary>, referenceDate: LocalDate): Double {
        val targetPrefix = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return documents
            .filter { it.type == DocumentType.Invoice }
            .filter { it.paymentStatus?.lowercase() == "paid" }
            .filter { safeMatchDate(it.issueDate, targetPrefix) }
            .sumOf { it.total }
    }

    fun countPaidInvoices(documents: List<DocumentSummary>, referenceDate: LocalDate): Int {
        val targetPrefix = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return documents
            .filter { it.type == DocumentType.Invoice }
            .filter { it.paymentStatus?.lowercase() == "paid" }
            .count { safeMatchDate(it.issueDate, targetPrefix) }
    }

    fun countPendingQuotes(documents: List<DocumentSummary>): Int {
        // Confirmed Tijario rule: draft and sent quotations only
        return documents
            .filter { it.type == DocumentType.Quote }
            .count { it.status.lowercase() == "draft" || it.status.lowercase() == "sent" }
    }

    private fun safeMatchDate(dateString: String, yearMonthPrefix: String): Boolean {
        return try {
            // Check if it starts with the prefix yyyy-MM
            if (dateString.length >= 7) {
                val prefix = dateString.substring(0, 7)
                // Check if the formatting matches yyyy-MM pattern (e.g., must contain a dash and numbers)
                val isPatternMatch = prefix.matches(Regex("^\\d{4}-\\d{2}$"))
                isPatternMatch && prefix == yearMonthPrefix
            } else {
                false
            }
        } catch (e: Exception) {
            false // safe ignore malformed dates without crashing
        }
    }
}
