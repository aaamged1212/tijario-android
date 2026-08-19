package app.tijario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import app.tijario.config.AppLanguage
import androidx.compose.material.icons.filled.Star
import app.tijario.config.t
import app.tijario.domain.DialCodeOption
import app.tijario.domain.CountryCatalog
import app.tijario.domain.CurrencyCatalog
import app.tijario.domain.CountryOption
import app.tijario.domain.MvpDialCodeOptions
import app.tijario.domain.filterCountryOptions
import app.tijario.domain.filterDialCodeOptions
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
        modifier = modifier.fillMaxWidth().then(if (error == null) Modifier.height(52.dp) else Modifier),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
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
                        contentDescription = if (passwordVisible) t("hide_password") else t("show_password"),
                        modifier = Modifier.size(20.dp)
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

@Composable
fun TijarioSearchField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(50.dp),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 12.sp,
                maxLines = 1,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodySmall,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TijarioPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
    label: String = t("whatsapp_phone"),
    defaultDialCode: String = "+966",
    onDialCodeChange: ((String) -> Unit)? = null,
    onCountryCodeSelected: ((DialCodeOption) -> Unit)? = null,
    showCountryNameInDialCode: Boolean = true,
) {
    val language = LocalLanguage.current
    val adaptive = LocalAdaptiveLayoutInfo.current
    val safeDefaultDialCode = MvpDialCodeOptions.firstOrNull { it.dialCode == defaultDialCode }?.dialCode
        ?: "+966"
    val parts = if (value.isBlank()) {
        app.tijario.domain.PhoneNumberParts(safeDefaultDialCode, "")
    } else {
        splitPhoneNumber(value)
    }
    var selectedDialCode by rememberSaveable { mutableStateOf(safeDefaultDialCode) }
    LaunchedEffect(value, safeDefaultDialCode) {
        if (value.isNotBlank()) {
            selectedDialCode = parts.dialCode
        } else {
            selectedDialCode = safeDefaultDialCode
        }
    }
    val activeDialCode = if (value.isBlank()) selectedDialCode else parts.dialCode
    val selectedOption = MvpDialCodeOptions.firstOrNull { it.dialCode == activeDialCode }
        ?: MvpDialCodeOptions.first()
    var showDialCodeSheet by rememberSaveable { mutableStateOf(false) }
    var dialCodeQuery by rememberSaveable { mutableStateOf("") }

    val dialCodeField: @Composable (Modifier) -> Unit = { fieldModifier ->
        OutlinedButton(
            onClick = {
                dialCodeQuery = ""
                showDialCodeSheet = true
            },
            modifier = fieldModifier.height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = if (showCountryNameInDialCode) {
                    listOf(selectedOption.flag, selectedOption.dialCode)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                } else {
                    selectedOption.dialCode
                },
                maxLines = 1,
            )
        }
    }
    val numberField: @Composable (Modifier) -> Unit = { fieldModifier ->
        TijarioTextField(
            label = label,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dialCodeField(Modifier.weight(0.75f))
            numberField(Modifier.weight(1.25f))
        }
    }

    if (showDialCodeSheet) {
        val filteredOptions = remember(dialCodeQuery, language) {
            filterDialCodeOptions(dialCodeQuery, language)
        }
        ModalBottomSheet(
            onDismissRequest = { showDialCodeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(t("country_code"), fontWeight = FontWeight.Bold)
                TijarioSearchField(
                    placeholder = t("search_dial_code_placeholder"),
                    value = dialCodeQuery,
                    onValueChange = { dialCodeQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredOptions, key = { it.countryCode }) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedDialCode = option.dialCode
                                    onDialCodeChange?.invoke(option.dialCode)
                                    onCountryCodeSelected?.invoke(option)
                                    onValueChange(normalizePhoneWithDialCode(option.dialCode, parts.localNumber))
                                    showDialCodeSheet = false
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(option.flag)
                            Text(
                                text = if (language == app.tijario.config.AppLanguage.AR) option.nameAr else option.nameEn,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                            Text(option.dialCode, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerBottomSheet(
    currentCountry: String,
    onDismiss: () -> Unit,
    onSelect: (CountryOption) -> Unit,
) {
    val language = LocalLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    val countries = remember(query, language) {
        filterCountryOptions(query, language)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(t("country"), fontWeight = FontWeight.Bold)
            TijarioSearchField(
                placeholder = t("search_country_placeholder"),
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(countries, key = { it.countryCode }) { country ->
                    val selected = CountryCatalog.find(currentCountry)?.countryCode == country.countryCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                            )
                            .clickable { onSelect(country) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(country.flag)
                        Text(
                            text = country.name(language),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        country.dialCode?.let { dialCode ->
                            Text(
                                text = dialCode,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    onDismissRequest: () -> Unit,
    onRateSubmitted: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    val currentLang = LocalLanguage.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (currentLang == AppLanguage.AR) "هل يعجبك تطبيق تجاريو؟" else "Do you like Tijario?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (currentLang == AppLanguage.AR) 
                    "يسعدنا تقييمك للتطبيق بخمس نجوم لدعمنا في الاستمرار وتقديم الأفضل!" 
                else 
                    "We would love it if you could rate us 5 stars to support our continued improvements!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                (1..5).forEach { index ->
                    val isSelected = index <= rating
                    IconButton(
                        onClick = { rating = index },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFFB300) else Color.LightGray.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (rating > 0) {
                        onRateSubmitted(rating)
                    }
                },
                enabled = rating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0FA36E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (currentLang == AppLanguage.AR) "تقديم التقييم" else "Submit Rating",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyBottomSheet(
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
    language: AppLanguage
) {
    val currencies = remember { CurrencyCatalog.options.distinctBy { it.code } }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCurrencies = remember(searchQuery) {
        currencies.filter { currency ->
            currency.code.contains(searchQuery, ignoreCase = true) ||
                    CurrencyCatalog.display(currency.code, language).contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = if (language == AppLanguage.AR) "اختر العملة" else "Select Currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TijarioSearchField(
                placeholder = if (language == AppLanguage.AR) "بحث عن عملة..." else "Search currency...",
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(filteredCurrencies) { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(currency.code)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencyCatalog.display(currency.code, language),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = currency.code,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
