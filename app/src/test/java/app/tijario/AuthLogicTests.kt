package app.tijario

import app.tijario.ui.state.AuthStateResolver
import app.tijario.ui.state.CentralAuthState
import app.tijario.ui.state.RegisterFormState
import app.tijario.config.AppLanguage
import app.tijario.domain.ErrorMapper
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthLogicTests {

    @Test
    fun testAuthStateResolver_allCases() {
        // 1. Success case
        assertEquals(
            CentralAuthState.AuthenticatedReady,
            AuthStateResolver.resolve(true, true, true)
        )

        // 2. No Session case
        assertEquals(
            CentralAuthState.Unauthenticated,
            AuthStateResolver.resolve(false, true, true)
        )

        // 3. Email not verified case
        assertEquals(
            CentralAuthState.AwaitingEmailVerification,
            AuthStateResolver.resolve(true, false, true)
        )

        // 4. Needs Onboarding case
        assertEquals(
            CentralAuthState.AuthenticatedNeedsOnboarding,
            AuthStateResolver.resolve(true, true, false)
        )

        // 5. Error case
        val errorState = AuthStateResolver.resolve(true, true, true, "Test Error")
        assertTrue(errorState is CentralAuthState.Error)
        assertEquals("Test Error", (errorState as CentralAuthState.Error).message)
    }

    /**
     * Note: Testing AuthViewModel directly requires mocking SupabaseClient and Coroutines.
     * These tests focus on the logical mapping which AuthViewModel uses.
     */
    @Test
    fun testNativeSignInResultMapping_Logic() {
        // We simulate the logic inside AuthViewModel.handleGoogleSignInResult
        
        fun mapResultToState(result: NativeSignInResult, sessionExists: Boolean): Any {
            return when (result) {
                is NativeSignInResult.Success -> {
                    if (sessionExists) "PROCEED_TO_CHECK" else "ERROR_NO_SESSION"
                }
                is NativeSignInResult.Error -> "SHOW_ARABIC_ERROR"
                is NativeSignInResult.ClosedByUser -> "RESET_TO_UNAUTHENTICATED"
                is NativeSignInResult.NetworkError -> "SHOW_NETWORK_ERROR"
            }
        }

        assertEquals("PROCEED_TO_CHECK", mapResultToState(NativeSignInResult.Success, true))
        assertEquals("ERROR_NO_SESSION", mapResultToState(NativeSignInResult.Success, false))
        assertEquals("SHOW_ARABIC_ERROR", mapResultToState(NativeSignInResult.Error("some error"), true))
        assertEquals("RESET_TO_UNAUTHENTICATED", mapResultToState(NativeSignInResult.ClosedByUser, true))
        assertEquals("SHOW_NETWORK_ERROR", mapResultToState(NativeSignInResult.NetworkError("network error"), true))
    }

    @Test
    fun testRegisterFormState_confirmPasswordValidation() {
        val matching = RegisterFormState(
            fullName = "Test User",
            email = "test@example.com",
            password = "secret123",
            confirmPassword = "secret123",
            lang = AppLanguage.EN,
        )
        assertEquals(null, matching.confirmPasswordError)
        assertEquals(true, matching.canSubmit)

        val mismatch = matching.copy(confirmPassword = "different")
        assertEquals("Passwords do not match", mismatch.confirmPasswordError)
        assertEquals(false, mismatch.canSubmit)
    }

    @Test
    fun testErrorMapper_emailNotRegistered() {
        val mapped = ErrorMapper.map(RuntimeException("User not found"), AppLanguage.EN)
        assertEquals("This email is not registered. Please sign up first.", mapped)
    }

    @Test
    fun testOtpValidator_sanitizationAndValidation() {
        // Test sanitization (removes spaces, letters, trims, takes 6)
        assertEquals("123456", app.tijario.domain.OtpValidator.sanitize(" 12 3 4567 "))
        assertEquals("123456", app.tijario.domain.OtpValidator.sanitize("123-456abc"))

        // Test isValid
        assertTrue(app.tijario.domain.OtpValidator.isValid("123456"))
        assertTrue(app.tijario.domain.OtpValidator.isValid(" 123 456 "))
        org.junit.Assert.assertFalse(app.tijario.domain.OtpValidator.isValid("12345"))
        org.junit.Assert.assertFalse(app.tijario.domain.OtpValidator.isValid("1234567"))
        org.junit.Assert.assertFalse(app.tijario.domain.OtpValidator.isValid("123a56"))
        org.junit.Assert.assertFalse(app.tijario.domain.OtpValidator.isValid(""))
    }

    @Test
    fun testOtpVerificationFlow_simulation() {
        // Simulate OTP verification and Bootstrap result handling
        var otpVerified = false
        var bootstrapSucceeded = false
        var displayedError: String? = null
        var isButtonEnabled = true
        var isLoading = false

        fun onVerifyClicked(code: String, email: String?, simulateOtpSuccess: Boolean, simulateBootstrapSuccess: Boolean) {
            if (isLoading || !isButtonEnabled) return // double tap prevention check
            isLoading = true
            isButtonEnabled = false

            val normalizedCode = code.trim().replace(" ", "")
            if (normalizedCode.length != 6 || !normalizedCode.all { it.isDigit() }) {
                displayedError = "Verification code must be 6 digits"
                isLoading = false
                isButtonEnabled = true
                return
            }

            if (email.isNullOrEmpty()) {
                displayedError = "Email not found"
                isLoading = false
                isButtonEnabled = true
                return
            }

            // Step 1: OTP Check
            if (simulateOtpSuccess) {
                otpVerified = true
                // Step 2: Bootstrap Check
                if (simulateBootstrapSuccess) {
                    bootstrapSucceeded = true
                } else {
                    displayedError = "Verification succeeded but account setup failed"
                }
            } else {
                displayedError = "Invalid or expired code"
            }

            isLoading = false
            isButtonEnabled = true
        }

        // Test Case 1: Incorrect/Expired code
        onVerifyClicked("12345", "test@example.com", simulateOtpSuccess = false, simulateBootstrapSuccess = false)
        assertEquals("Verification code must be 6 digits", displayedError)
        org.junit.Assert.assertFalse(otpVerified)

        // Test Case 2: Expired or Incorrect OTP
        displayedError = null
        onVerifyClicked("111222", "test@example.com", simulateOtpSuccess = false, simulateBootstrapSuccess = false)
        assertEquals("Invalid or expired code", displayedError)
        org.junit.Assert.assertFalse(otpVerified)

        // Test Case 3: Empty/Lost email
        displayedError = null
        onVerifyClicked("111222", "", simulateOtpSuccess = true, simulateBootstrapSuccess = true)
        assertEquals("Email not found", displayedError)
        org.junit.Assert.assertFalse(otpVerified)

        // Test Case 4: Correct OTP but Bootstrap fails
        displayedError = null
        otpVerified = false
        onVerifyClicked(" 123 456 ", "test@example.com", simulateOtpSuccess = true, simulateBootstrapSuccess = false)
        assertTrue(otpVerified)
        org.junit.Assert.assertFalse(bootstrapSucceeded)
        assertEquals("Verification succeeded but account setup failed", displayedError)

        // Test Case 5: Correct OTP and Bootstrap succeeds
        displayedError = null
        otpVerified = false
        bootstrapSucceeded = false
        onVerifyClicked(" 123 456 ", "test@example.com", simulateOtpSuccess = true, simulateBootstrapSuccess = true)
        assertTrue(otpVerified)
        assertTrue(bootstrapSucceeded)
        org.junit.Assert.assertNull(displayedError)
    }
}
