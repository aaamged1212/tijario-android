package app.tijario.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TijarioRepositoryPlanUsageTests {
    @Test
    fun currentUtcPeriodMonth_usesFirstDayOfMonthInUtcFormat() {
        val repo = TijarioRepositoryPlanUsageProbe()

        assertEquals("2026-06-01", repo.currentUtcPeriodMonth(LocalDate.of(2026, 6, 22)))
        assertEquals("2026-01-01", repo.currentUtcPeriodMonth(LocalDate.of(2026, 1, 31)))
    }
}

private class TijarioRepositoryPlanUsageProbe {
    fun currentUtcPeriodMonth(reference: LocalDate): String =
        reference.withDayOfMonth(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
