package app.tijario.domain

import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DashboardStatsRangeTests {
    @Test
    fun presetRangesUseUtcBoundaries() {
        val reference = LocalDate.of(2026, 7, 4)
        val thisMonth = DashboardStatsCalculator.resolvePresetRange(DashboardDateRangePreset.ThisMonth, reference)
        val lastMonth = DashboardStatsCalculator.resolvePresetRange(DashboardDateRangePreset.LastMonth, reference)
        val last7 = DashboardStatsCalculator.resolvePresetRange(DashboardDateRangePreset.Last7Days, reference)

        assertEquals(LocalDate.of(2026, 7, 1), thisMonth.startDate)
        assertEquals(LocalDate.of(2026, 7, 31), thisMonth.endDate)
        assertEquals(LocalDate.of(2026, 6, 1), lastMonth.startDate)
        assertEquals(LocalDate.of(2026, 6, 30), lastMonth.endDate)
        assertEquals(LocalDate.of(2026, 6, 28), last7.startDate)
        assertEquals(LocalDate.of(2026, 7, 4), last7.endDate)
    }

    @Test
    fun rangeFilteringIsInclusiveAndIgnoresMalformedDates() {
        val docs = listOf(
            doc("2026-07-01"),
            doc("2026-07-15T12:30:00Z"),
            doc("2026-07-31T23:59:59Z"),
            doc("2026-06-30T23:59:59Z"),
            doc("invalid"),
        )

        val filtered = DashboardStatsCalculator.filterByRange(
            docs,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
        )

        assertEquals(3, filtered.size)
        assertTrue(filtered.all { it.issueDate.startsWith("2026-07") })
    }

    @Test
    fun collectedAmountsRespectSelectedRange() {
        val docs = listOf(
            doc("2026-07-01", total = 100.0, paymentStatus = "paid"),
            doc("2026-07-02", total = 150.0, paymentStatus = "partial", amountPaid = 50.0),
            doc("2026-06-30", total = 999.0, paymentStatus = "paid"),
            quote("2026-07-03", total = 80.0, status = "draft"),
            quote("2026-06-29", total = 45.0, status = "sent"),
        )

        val collected = DashboardStatsCalculator.calculateCollectedInvoiceAmount(
            docs,
            currency = "SAR",
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 31),
        )
        val outstanding = DashboardStatsCalculator.calculateOutstandingInvoiceAmount(
            docs,
            currency = "SAR",
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 31),
        )
        val openQuotes = DashboardStatsCalculator.calculateOpenQuotesAmount(
            docs,
            currency = "SAR",
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 31),
        )
        val documentCount = DashboardStatsCalculator.countDocuments(
            docs,
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 31),
        )

        assertEquals(150.0, collected, 0.001)
        assertEquals(100.0, outstanding, 0.001)
        assertEquals(80.0, openQuotes, 0.001)
        assertEquals(3, documentCount)
    }

    private fun doc(
        issueDate: String,
        total: Double = 10.0,
        paymentStatus: String? = "paid",
        amountPaid: Double? = total,
    ) = DocumentSummary(
        id = issueDate,
        customerId = "c1",
        type = DocumentType.Invoice,
        documentNumber = "INV-$issueDate",
        status = "sent",
        paymentStatus = paymentStatus,
        amountPaid = amountPaid,
        issueDate = issueDate,
        total = total,
        currency = "SAR",
    )

    private fun quote(
        issueDate: String,
        total: Double = 10.0,
        status: String = "draft",
        currency: String = "SAR",
    ) = DocumentSummary(
        id = "quote-$issueDate",
        customerId = "c1",
        type = DocumentType.Quote,
        documentNumber = "QT-$issueDate",
        status = status,
        paymentStatus = null,
        amountPaid = null,
        issueDate = issueDate,
        total = total,
        currency = currency,
    )
}
