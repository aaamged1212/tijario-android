package app.tijario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.config.AppLanguage
import app.tijario.config.LocalLanguage
import app.tijario.config.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
) {
    if (!visible) return

    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val title = if (language == AppLanguage.AR) "تغيير كلمة المرور" else "Change password"
    val currentLabel = if (language == AppLanguage.AR) "كلمة المرور الحالية" else "Current password"
    val newLabel = if (language == AppLanguage.AR) "كلمة المرور الجديدة" else "New password"
    val confirmLabel = if (language == AppLanguage.AR) "تأكيد كلمة المرور الجديدة" else "Confirm new password"
    val cancelLabel = if (language == AppLanguage.AR) "إلغاء" else "Cancel"
    val saveLabel = if (language == AppLanguage.AR) "حفظ" else "Save"
    val currentRequired = if (language == AppLanguage.AR) "أدخل كلمة المرور الحالية." else "Enter your current password."
    val tooShort = if (language == AppLanguage.AR) "يجب أن تتكون كلمة المرور الجديدة من 6 أحرف على الأقل." else "The new password must contain at least 6 characters."
    val mismatch = if (language == AppLanguage.AR) "كلمتا المرور الجديدتان غير متطابقتين." else "The new passwords do not match."
    val unchanged = if (language == AppLanguage.AR) "يجب أن تختلف كلمة المرور الجديدة عن الحالية." else "The new password must be different from the current password."
    val wrongCurrent = if (language == AppLanguage.AR) "كلمة المرور الحالية غير صحيحة." else "The current password is incorrect."
    val genericFailure = if (language == AppLanguage.AR) "تعذر تغيير كلمة المرور الآن. حاول مرة أخرى." else "We could not change your password. Please try again."
    val successMessage = if (language == AppLanguage.AR) "تم تغيير كلمة المرور بنجاح." else "Your password was changed successfully."

    fun close() {
        if (!isSaving) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            errorMessage = null
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; errorMessage = null },
                    label = { Text(currentLabel) },
                    singleLine = true,
                    enabled = !isSaving,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    label = { Text(newLabel) },
                    singleLine = true,
                    enabled = !isSaving,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text(confirmLabel) },
                    singleLine = true,
                    enabled = !isSaving,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    when {
                        currentPassword.isBlank() -> errorMessage = currentRequired
                        newPassword.length < 6 -> errorMessage = tooShort
                        newPassword != confirmPassword -> errorMessage = mismatch
                        newPassword == currentPassword -> errorMessage = unchanged
                        else -> scope.launch {
                            isSaving = true
                            errorMessage = null
                            try {
                                val email = Supabase.client.auth.currentUserOrNull()?.email.orEmpty()
                                if (email.isBlank()) {
                                    errorMessage = genericFailure
                                } else {
                                    Supabase.client.auth.signInWith(Email) {
                                        this.email = email
                                        password = currentPassword
                                    }
                                    Supabase.client.auth.updateUser {
                                        password = newPassword
                                    }
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    onDismiss()
                                    onSuccess(successMessage)
                                }
                            } catch (error: Exception) {
                                val normalized = error.message.orEmpty().lowercase()
                                errorMessage = if (
                                    normalized.contains("invalid login credentials") ||
                                    normalized.contains("invalid credentials")
                                ) {
                                    wrongCurrent
                                } else {
                                    genericFailure
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(saveLabel)
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = { close() }) {
                Text(cancelLabel)
            }
        },
    )
}
