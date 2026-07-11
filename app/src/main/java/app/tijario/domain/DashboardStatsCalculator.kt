package app.tijario.domain

import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class DashboardDateRange(
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

enum class DashboardDateRangePreset {
    ThisMonth,
    LastMonth,
    Last7Days,
    Last30Days,
    Custom,
}

object DashboardStatsCalculator {
    fun resolvePresetRange(
        preset: DashboardDateRangePreset,
        referenceDate: LocalDate = LocalDate.now(ZoneOffset.UTC),
    ): DashboardDateRange =
        when (preset) {
            DashboardDateRangePreset.ThisMonth -> {
                val month = YearMonth.from(referenceDate)
                DashboardDateRange(month.atDay(1), month.atEndOfMonth())
            }
            DashboardDateRangePreset.LastMonth -> {
                val month = YearMonth.from(referenceDate).minusMonths(1)
                DashboardDateRange(month.atDay(1), month.atEndOfMonth())
            }
            DashboardDateRangePreset.Last7Days -> DashboardDateRange(referenceDate.minusDays(6), referenceDate)
            DashboardDateRangePreset.Last30Days -> DashboardDateRange(referenceDate.minusDays(29), referenceDate)
            DashboardDateRangePreset.Custom -> DashboardDateRange(null, null)
        }

    fun filterByRange(
        documents: List<DocumentSummary>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): List<DocumentSummary> =
        documents.filter { document ->
            val issueDate = parseUtcDate(document.issueDate) ?: return@filter false
            if (startDate != null && issueDate.isBefore(startDate)) return@filter false
            if (endDate != null && issueDate.isAfter(endDate)) return@filter false
            true
        }

    fun calculateCurrentMonthEarnings(documents: List<DocumentSummary>, referenceDate: LocalDate): Double =
        calculateCollectedInvoiceAmount(
            documents,
            currency = null,
            startDate = resolvePresetRange(DashboardDateRangePreset.ThisMonth, referenceDate).startDate,
            endDate = resolvePresetRange(DashboardDateRangePreset.ThisMonth, referenceDate).endDate,
        )

    fun countPaidInvoices(documents: List<DocumentSummary>, referenceDate: LocalDate): Int =
        countPaidInvoices(
            documents = documents,
            startDate = resolvePresetRange(DashboardDateRangePreset.ThisMonth, referenceDate).startDate,
            endDate = resolvePresetRange(DashboardDateRangePreset.ThisMonth, referenceDate).endDate,
        )

    fun countPaidInvoices(
        documents: List<DocumentSummary>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Int =
        filterByRange(documents, startDate, endDate)
            .filter { it.type == DocumentType.Invoice }
            .count { it.paymentStatus?.lowercase() == "paid" }

    fun calculateCollectedInvoiceAmount(
        documents: List<DocumentSummary>,
        currency: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Double =
        filterByRange(documents, startDate, endDate)
            .filter { it.type == DocumentType.Invoice }
            .filter { matchesCurrency(it.currency, currency) }
            .sumOf {
                PaymentAmountCalculator.calculate(
                    paymentStatus = it.paymentStatus,
                    total = BigDecimal.valueOf(it.total),
                    amountPaid = it.amountPaid?.let(BigDecimal::valueOf),
                ).paid.toDouble()
            }

    fun calculateOutstandingInvoiceAmount(
        documents: List<DocumentSummary>,
        currency: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Double =
        filterByRange(documents, startDate, endDate)
            .filter { it.type == DocumentType.Invoice }
            .filter { matchesCurrency(it.currency, currency) }
            .sumOf {
                PaymentAmountCalculator.remainingDouble(
                    paymentStatus = it.paymentStatus,
                    total = it.total,
                    amountPaid = it.amountPaid,
                )
            }

    fun calculateOpenQuotesAmount(
        documents: List<DocumentSummary>,
        currency: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Double =
        filterByRange(documents, startDate, endDate)
            .filter { it.type == DocumentType.Quote }
            .filter { it.status.lowercase() == "draft" || it.status.lowercase() == "sent" }
            .filter { matchesCurrency(it.currency, currency) }
            .sumOf { it.total }

    fun countDocuments(
        documents: List<DocumentSummary>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        type: DocumentType? = null,
    ): Int =
        filterByRange(documents, startDate, endDate)
            .let { filtered -> if (type == null) filtered else filtered.filter { it.type == type } }
            .size

    fun countPendingQuotes(documents: List<DocumentSummary>): Int =
        documents.filter { it.type == DocumentType.Quote }
            .count { it.status.lowercase() == "draft" || it.status.lowercase() == "sent" }

    fun monthKeyForRange(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): String? {
        val effectiveStart = startDate ?: endDate ?: return null
        return YearMonth.from(effectiveStart).format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    private fun parseUtcDate(dateString: String): LocalDate? {
        val trimmed = dateString.trim()
        if (trimmed.isBlank()) return null
        return runCatching {
            when {
                trimmed.length >= 10 -> LocalDate.parse(trimmed.substring(0, 10))
                else -> LocalDate.parse(trimmed)
            }
        }.getOrNull()
    }

    private fun matchesCurrency(documentCurrency: String, currency: String?): Boolean =
        currency.isNullOrBlank() || documentCurrency.equals(currency, ignoreCase = true)
}
