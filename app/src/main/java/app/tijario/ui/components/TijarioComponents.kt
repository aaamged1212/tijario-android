package app.tijario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.config.LocalLanguage
import app.tijario.config.t
import app.tijario.domain.MvpDialCodeOptions
import app.tijario.domain.normalizePhoneWithDialCode
import app.tijario.domain.splitPhoneNumber

@Composable
fun TijarioPage(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val adaptive = LocalAdaptiveLayoutInfo.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = adaptive.pageHorizontalPadding, vertical = adaptive.sectionSpacing),
        verticalArrangement = Arrangement.spacedBy(adaptive.cardSpacing),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

@Composable
fun TijarioCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun TijarioTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) t("hide_password") else t("show_password")
                    )
                }
            }
        } else trailingIcon,
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TijarioPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
    defaultDialCode: String = MvpDialCodeOptions.first().dialCode,
    onDialCodeChange: ((String) -> Unit)? = null,
    showCountryNameInDialCode: Boolean = true,
) {
    val language = LocalLanguage.current
    val adaptive = LocalAdaptiveLayoutInfo.current
    val safeDefaultDialCode = MvpDialCodeOptions.firstOrNull { it.dialCode == defaultDialCode }?.dialCode
        ?: MvpDialCodeOptions.first().dialCode
    val parts = if (value.isBlank()) {
        app.tijario.domain.PhoneNumberParts(safeDefaultDialCode, "")
    } else {
        splitPhoneNumber(value)
    }
    var selectedDialCode by rememberSaveable { mutableStateOf(safeDefaultDialCode) }
    LaunchedEffect(value) {
        if (value.isNotBlank()) {
            selectedDialCode = parts.dialCode
        }
    }
    val activeDialCode = if (value.isBlank()) selectedDialCode else parts.dialCode
    val selectedOption = MvpDialCodeOptions.firstOrNull { it.dialCode == activeDialCode }
        ?: MvpDialCodeOptions.first()
    var menuExpanded by remember { mutableStateOf(false) }

    val dialCodeField: @Composable (Modifier) -> Unit = { fieldModifier ->
        ExposedDropdownMenuBox(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = !menuExpanded },
            modifier = fieldModifier,
        ) {
            TijarioTextField(
                label = t("country_code"),
                value = if (showCountryNameInDialCode) selectedOption.label(language) else selectedOption.dialCode,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true,
            )
            ExposedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                MvpDialCodeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label(language), maxLines = 1) },
                        onClick = {
                            selectedDialCode = option.dialCode
                            onDialCodeChange?.invoke(option.dialCode)
                            onValueChange(normalizePhoneWithDialCode(option.dialCode, parts.localNumber))
                            menuExpanded = false
                        },
                    )
                }
            }
        }
    }
    val numberField: @Composable (Modifier) -> Unit = { fieldModifier ->
        TijarioTextField(
            label = t("whatsapp_phone"),
            value = parts.localNumber,
            onValueChange = { onValueChange(normalizePhoneWithDialCode(activeDialCode, it)) },
            error = error,
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            modifier = fieldModifier,
        )
    }

    if (adaptive.isExtraCompact) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dialCodeField(Modifier.fillMaxWidth())
            numberField(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            dialCodeField(Modifier.weight(0.75f))
            numberField(Modifier.weight(1.25f))
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean = false,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(t("logout_confirm_title"), fontWeight = FontWeight.Bold) },
        text = { Text(t("logout_confirm_message")) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(t("logout"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(t("btn_cancel"))
            }
        },
    )
}

@Composable
fun TijarioButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
