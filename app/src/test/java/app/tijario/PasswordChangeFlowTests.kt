package app.tijario

import app.tijario.ui.screens.changePasswordErrorKey
import app.tijario.ui.screens.validateChangePasswordInput
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordChangeFlowTests {
    private fun readSource(path: String): String {
        val candidates = listOf(File(path), File("app/$path"))
        return candidates.first { it.exists() }.readText()
    }

    @Test
    fun validation_requiresAllFields() {
        assertEquals("change_password_current_required", validateChangePasswordInput("", "secret1", "secret1"))
        assertEquals("change_password_new_required", validateChangePasswordInput("oldpass", "", "secret1"))
        assertEquals("change_password_confirm_required", validateChangePasswordInput("oldpass", "secret1", ""))
    }

    @Test
    fun validation_rejectsWeakMismatchAndUnchangedPasswords() {
        assertEquals("validation_password_min_length", validateChangePasswordInput("oldpass", "12345", "12345"))
        assertEquals("validation_password_mismatch", validateChangePasswordInput("oldpass", "secret1", "secret2"))
        assertEquals("change_password_unchanged", validateChangePasswordInput("secret1", "secret1", "secret1"))
        assertEquals(null, validateChangePasswordInput("oldpass", "secret1", "secret1"))
    }

    @Test
    fun backendErrorCodesMapToLocalizedKeys() {
        assertEquals("change_password_invalid_current", changePasswordErrorKey("invalid_current_password"))
        assertEquals("validation_password_mismatch", changePasswordErrorKey("password_mismatch"))
        assertEquals("validation_password_min_length", changePasswordErrorKey("password_too_short"))
        assertEquals("change_password_unchanged", changePasswordErrorKey("password_unchanged"))
        assertEquals("change_password_unauthorized", changePasswordErrorKey("unauthorized"))
        assertEquals("change_password_user_not_found", changePasswordErrorKey("user_not_found"))
        assertEquals("change_password_update_failed", changePasswordErrorKey("password_update_failed"))
        assertEquals("change_password_server_error", changePasswordErrorKey("unknown"))
    }

    @Test
    fun androidUsesAuthenticatedChangePasswordEndpoint() {
        val clientSource = readSource("src/main/java/app/tijario/data/remote/BackendApiClient.kt")
        assertTrue(clientSource.contains("authorizedPost(\"api/mobile/account/change-password\", request)"))
    }

    @Test
    fun accountSettingsNavigateInsteadOfSendingRecoveryEmail() {
        val appSource = readSource("src/main/java/app/tijario/ui/TijarioApp.kt")
        val settingsSource = readSource("src/main/java/app/tijario/ui/screens/SettingsScreens.kt")
        val coreSource = readSource("src/main/java/app/tijario/ui/screens/CoreScreens.kt")

        assertTrue(appSource.contains("navController.navigate(\"change-password\")"))
        assertTrue(settingsSource.contains(".clickable { onChangePassword() }"))
        assertTrue(coreSource.contains("onChangePassword = onChangePassword"))
        assertTrue(!settingsSource.contains("requestPasswordReset"))
        assertTrue(!coreSource.contains("requestPasswordReset"))
    }
}
