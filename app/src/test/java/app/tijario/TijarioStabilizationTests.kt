package app.tijario

import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import app.tijario.ui.state.AuthStateResolver
import app.tijario.domain.DashboardStatsCalculator
import app.tijario.domain.DocumentStatusMapper
import app.tijario.domain.ErrorMapper
import app.tijario.domain.OtpValidator
import app.tijario.domain.PaymentStatusMapper
import app.tijario.ui.state.CentralAuthState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.LocalDate

class TijarioStabilizationTests {

    // --- 1. Auth State Resolver Tests ---
    @Test
    fun testAuthStateResolver_noSession_resolvesToUnauthenticated() {
        val state = AuthStateResolver.resolve(
            sessionExists = false,
            isEmailVerified = true,
            hasBusinessSettings = true
        )
        assertEquals(CentralAuthState.Unauthenticated, state)
    }

    @Test
    fun testAuthStateResolver_unconfirmedEmail_resolvesToAwaitingVerification() {
        val state = AuthStateResolver.resolve(
            sessionExists = true,
            isEmailVerified = false,
            hasBusinessSettings = true
        )
        assertEquals(CentralAuthState.AwaitingEmailVerification, state)
    }

    @Test
    fun testAuthStateResolver_noBusinessSettings_resolvesToOnboarding() {
        val state = AuthStateResolver.resolve(
            sessionExists = true,
            isEmailVerified = true,
            hasBusinessSettings = false
        )
        assertEquals(CentralAuthState.AuthenticatedNeedsOnboarding, state)
    }

    @Test
    fun testAuthStateResolver_confirmedWithSettings_resolvesToReady() {
        val state = AuthStateResolver.resolve(
            sessionExists = true,
            isEmailVerified = true,
            hasBusinessSettings = true
        )
        assertEquals(CentralAuthState.AuthenticatedReady, state)
    }

    @Test
    fun testAuthStateResolver_withErrorMessage_resolvesToError() {
        val state = AuthStateResolver.resolve(
            sessionExists = true,
            isEmailVerified = true,
            hasBusinessSettings = true,
            errorMessage = "عطل مؤقت"
        )
        assertTrue(state is CentralAuthState.Error)
        assertEquals("عطل مؤقت", (state as CentralAuthState.Error).message)
    }

    // --- 2. OTP Validator Tests ---
    @Test
    fun testOtpValidator_sanitizationAndLimits() {
        assertEquals("12345678", OtpValidator.sanitize("123456789"))
        assertEquals("123", OtpValidator.sanitize("123abc#"))
        assertEquals("12345678", OtpValidator.sanitize("1234-5678"))
    }

    @Test
    fun testOtpValidator_validity() {
        assertFalse(OtpValidator.isValid("1234567"))
        assertFalse(OtpValidator.isValid("1234567a"))
        assertTrue(OtpValidator.isValid("12345678"))
        assertFalse(OtpValidator.isValid("123456789"))
    }

    // --- 3. Dashboard Stats Calculator Tests ---
    @Test
    fun testDashboardStatsCalculator_financialCalculations() {
        val referenceDate = LocalDate.of(2026, 6, 20)
        val docs = listOf(
            // Paid invoice this month -> Should be counted in revenue
            DocumentSummary("1", "c1", DocumentType.Invoice, "INV-001", "sent", "paid", "2026-06-15T12:00:00", 100.0, "SAR"),
            // Paid invoice this month, different day -> Should be counted
            DocumentSummary("2", "c1", DocumentType.Invoice, "INV-002", "sent", "paid", "2026-06-19T22:30:00", 150.0, "SAR"),
            // Paid invoice different month -> Ignored from monthly revenue
            DocumentSummary("3", "c1", DocumentType.Invoice, "INV-003", "sent", "paid", "2026-05-15T12:00:00", 500.0, "SAR"),
            // Unpaid invoice this month -> Ignored from revenue
            DocumentSummary("4", "c1", DocumentType.Invoice, "INV-004", "sent", "unpaid", "2026-06-10T08:00:00", 300.0, "SAR"),
            // Paid quote this month -> Quotes are never revenue
            DocumentSummary("5", "c1", DocumentType.Quote, "QTE-001", "accepted", "paid", "2026-06-15T12:00:00", 1000.0, "SAR"),
            // Malformed date -> Ignored safely
            DocumentSummary("6", "c1", DocumentType.Invoice, "INV-005", "sent", "paid", "invalid-date", 100.0, "SAR")
        )

        val totalAmount = DashboardStatsCalculator.calculateCurrentMonthEarnings(docs, referenceDate)
        assertEquals(250.0, totalAmount, 0.001)

        val paidCount = DashboardStatsCalculator.countPaidInvoices(docs, referenceDate)
        assertEquals(2, paidCount)
    }

    @Test
    fun testDashboardStatsCalculator_pendingQuotesCount() {
        val docs = listOf(
            DocumentSummary("1", "c1", DocumentType.Quote, "Q1", "draft", null, "2026-06-20", 100.0, "SAR"),
            DocumentSummary("2", "c1", DocumentType.Quote, "Q2", "sent", null, "2026-06-20", 200.0, "SAR"),
            DocumentSummary("3", "c1", DocumentType.Quote, "Q3", "accepted", null, "2026-06-20", 300.0, "SAR"),
            DocumentSummary("4", "c1", DocumentType.Quote, "Q4", "cancelled", null, "2026-06-20", 400.0, "SAR"),
            DocumentSummary("5", "c1", DocumentType.Invoice, "I1", "draft", null, "2026-06-20", 500.0, "SAR")
        )

        // Confirmed Tijario rule: draft and sent quotes only
        val pendingQuotes = DashboardStatsCalculator.countPendingQuotes(docs)
        assertEquals(2, pendingQuotes)
    }

    // --- 4. Status Mapping Tests ---
    @Test
    fun testDocumentStatusMapper() {
        assertEquals("مسودة", DocumentStatusMapper.getStatusText("draft"))
        assertEquals("مرسلة", DocumentStatusMapper.getStatusText("sent"))
        assertEquals("مقبولة", DocumentStatusMapper.getStatusText("accepted"))
        assertEquals("ملغاة", DocumentStatusMapper.getStatusText("cancelled"))
        assertEquals("حالة غير معروفة", DocumentStatusMapper.getStatusText("random"))
    }

    @Test
    fun testPaymentStatusMapper() {
        assertEquals("مدفوعة", PaymentStatusMapper.getStatusText("paid"))
        assertEquals("غير مدفوعة", PaymentStatusMapper.getStatusText("unpaid"))
        assertEquals("غير مدفوعة", PaymentStatusMapper.getStatusText(null))
        assertEquals("حالة غير معروفة", PaymentStatusMapper.getStatusText("random"))
    }

    // --- 5. Error Mapping Tests ---
    @Test
    fun testErrorMapper_exceptions() {
        val networkErr = ConnectException()
        val timeoutErr = SocketTimeoutException()
        val ioErr = IOException()
        val arbitraryErr = RuntimeException("rate limit exceeded")

        assertTrue(ErrorMapper.map(networkErr).contains("الاتصال بالخادم"))
        assertTrue(ErrorMapper.map(timeoutErr).contains("مهلة الاتصال"))
        assertTrue(ErrorMapper.map(ioErr).contains("الشبكة"))
        assertTrue(ErrorMapper.map(arbitraryErr).contains("حد إرسال الرموز") || ErrorMapper.map(arbitraryErr).contains("الحد الشهري"))
    }

    @Test
    fun testErrorMapper_apiCodes() {
        assertEquals("لقد تجاوزت الحد الشهري المسموح به للعمليات.", ErrorMapper.mapApiCode("LIMIT_EXCEEDED"))
        assertEquals("انتهت صلاحية الجلسة. يرجى تسجيل الدخول مرة أخرى.", ErrorMapper.mapApiCode("SESSION_EXPIRED"))
        assertEquals("البريد الإلكتروني أو كلمة المرور غير صحيحة.", ErrorMapper.mapApiCode("INVALID_CREDENTIALS"))
        assertEquals("هذا العميل مضاف بالفعل.", ErrorMapper.mapApiCode("DUPLICATE_CUSTOMER"))
        assertEquals("حدث خطأ في النظام (RANDOM_CODE).", ErrorMapper.mapApiCode("RANDOM_CODE"))
    }
}
