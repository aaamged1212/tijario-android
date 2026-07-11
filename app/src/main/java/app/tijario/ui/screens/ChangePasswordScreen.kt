package app.tijario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.tijario.config.Localization
import app.tijario.config.LocalLanguage
import app.tijario.config.Supabase
import app.tijario.config.t
import app.tijario.data.remote.ChangePasswordRequest
import kotlinx.coroutines.launch

internal const val TIJARIO_PASSWORD_MIN_LENGTH = 6

internal fun validateChangePasswordInput(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
): String? = when {
    currentPassword.isBlank() -> "change_password_current_required"
    newPassword.isBlank() -> "change_password_new_required"
    confirmPassword.isBlank() -> "change_password_confirm_required"
    newPassword.length < TIJARIO_PASSWORD_MIN_LENGTH -> "validation_password_min_length"
    newPassword != confirmPassword -> "validation_password_mismatch"
    newPassword == currentPassword -> "change_password_unchanged"
    else -> null
}

internal fun changePasswordErrorKey(code: String?): String = when (code) {
    "invalid_current_password" -> "change_password_invalid_current"
    "password_mismatch" -> "validation_password_mismatch"
    "password_too_short" -> "validation_password_min_length"
    "password_unchanged" -> "change_password_unchanged"
    "unauthorized" -> "change_password_unauthorized"
    "user_not_found" -> "change_password_user_not_found"
    "invalid_fields" -> "change_password_invalid_fields"
    "password_update_failed" -> "change_password_update_failed"
    else -> "change_password_server_error"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var messageKey by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("change_password"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = t("change_password_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = t("change_password_subtitle"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PasswordField(
                label = t("current_password"),
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    messageKey = null
                },
                enabled = !isLoading,
            )
            PasswordField(
                label = t("new_password"),
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    messageKey = null
                },
                enabled = !isLoading,
            )
            PasswordField(
                label = t("confirm_new_password"),
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    messageKey = null
                },
                enabled = !isLoading,
                imeAction = ImeAction.Done,
            )

            messageKey?.let { key ->
                Text(
                    text = Localization.getString(key, language),
                    color = if (messageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    val validationKey = validateChangePasswordInput(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                    )
                    if (validationKey != null) {
                        messageKey = validationKey
                        messageIsError = true
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        val result = runCatching {
                            Supabase.apiClient.changePassword(
                                ChangePasswordRequest(
                                    currentPassword = currentPassword,
                                    newPassword = newPassword,
                                    confirmPassword = confirmPassword,
                                ),
                            )
                        }
                        isLoading = false

                        val response = result.getOrNull()
                        if (response?.ok == true) {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            messageKey = "change_password_success"
                            messageIsError = false
                        } else {
                            messageKey = changePasswordErrorKey(response?.code)
                            messageIsError = true
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Text(if (isLoading) t("saving") else t("save_new_password"))
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = t(if (visible) "hide_password" else "show_password"),
                )
            }
        },
    )
}
