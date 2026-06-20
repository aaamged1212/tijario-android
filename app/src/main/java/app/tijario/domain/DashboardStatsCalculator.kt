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
            .filter { safeMatchDate(it.issueDate, targetPrefix) }
            .sumOf {
                PaymentAmountCalculator.calculate(
                    paymentStatus = it.paymentStatus,
                    total = java.math.BigDecimal.valueOf(it.total),
                    amountPaid = it.amountPaid?.let(java.math.BigDecimal::valueOf),
                ).paid.toDouble()
            }
    }

    fun countPaidInvoices(documents: List<DocumentSummary>, referenceDate: LocalDate): Int {
        val targetPrefix = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return documents
            .filter { it.type == DocumentType.Invoice }
            .filter { it.paymentStatus?.lowercase() == "paid" }
            .count { safeMatchDate(it.issueDate, targetPrefix) }
    }

    fun calculateCollectedInvoiceAmount(documents: List<DocumentSummary>, currency: String? = null): Double =
        documents
            .filter { it.type == DocumentType.Invoice }
            .filter { matchesCurrency(it.currency, currency) }
            .sumOf {
                PaymentAmountCalculator.calculate(
                    paymentStatus = it.paymentStatus,
                    total = java.math.BigDecimal.valueOf(it.total),
                    amountPaid = it.amountPaid?.let(java.math.BigDecimal::valueOf),
                ).paid.toDouble()
            }

    fun calculateOutstandingInvoiceAmount(documents: List<DocumentSummary>, currency: String? = null): Double =
        documents
            .filter { it.type == DocumentType.Invoice }
            .filter { matchesCurrency(it.currency, currency) }
            .sumOf {
                PaymentAmountCalculator.remainingDouble(
                    paymentStatus = it.paymentStatus,
                    total = it.total,
                    amountPaid = it.amountPaid,
                )
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

    private fun matchesCurrency(documentCurrency: String, currency: String?): Boolean =
        currency.isNullOrBlank() || documentCurrency.equals(currency, ignoreCase = true)
}
