package app.tijario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.config.t
import app.tijario.ui.components.GoogleSignInButton
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.LoginFormState
import app.tijario.ui.state.RegisterFormState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun LoginScreen(
    onLoginReady: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var form by remember { mutableStateOf(LoginFormState()) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F766E),
                        Color(0xFF0F766E),
                        Color(0xFF064E3B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo & Title
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = app.tijario.R.drawable.logo_app),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = t("app_name"),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = t("app_slogan"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = t("login_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TijarioTextField(
                        label = t("email"),
                        value = form.email,
                        onValueChange = { form = form.copy(email = it) },
                        error = if (form.email.isNotEmpty()) form.emailError else null,
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("password"),
                        value = form.password,
                        onValueChange = { form = form.copy(password = it) },
                        error = if (form.password.isNotEmpty()) form.passwordError else null,
                        isPassword = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onForgotPassword,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                t("forgot_password"),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    TijarioButton(
                        text = t("btn_login"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    app.tijario.config.Supabase.client.auth.signInWith(
                                        io.github.jan.supabase.auth.providers.builtin.Email
                                    ) {
                                        email = form.email
                                        password = form.password
                                    }
                                    onLoginReady()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "فشل تسجيل الدخول: البريد الإلكتروني أو كلمة المرور غير صحيحة"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        Text(
                            text = t("or"),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    }

                    val loginGoogleAction = app.tijario.config.Supabase.client.composeAuth.rememberSignInWithGoogle(
                        onResult = { result ->
                            when (result) {
                                is NativeSignInResult.Success -> {
                                    onLoginReady()
                                }
                                is NativeSignInResult.Error -> {
                                    errorMessage = result.message
                                }
                                else -> {}
                            }
                        }
                    )

                    GoogleSignInButton(
                        onClick = {
                            loginGoogleAction.startFlow()
                        },
                        enabled = !isLoading && !isGoogleLoading,
                        text = t("google_login")
                    )

                    if (isGoogleLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(t("no_account"), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                TextButton(onClick = onRegister, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        t("create_account"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RegisterScreen(onBackToLogin: () -> Unit) {
    var form by remember { mutableStateOf(RegisterFormState()) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F766E),
                        Color(0xFF0F766E),
                        Color(0xFF064E3B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = t("register_title"),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = t("register_subtitle"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TijarioTextField(
                        label = t("fullname"),
                        value = form.fullName,
                        onValueChange = { form = form.copy(fullName = it) },
                        error = if (form.fullName.isNotEmpty()) form.fullNameError else null,
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("email"),
                        value = form.email,
                        onValueChange = { form = form.copy(email = it) },
                        error = if (form.email.isNotEmpty()) form.emailError else null,
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("password"),
                        value = form.password,
                        onValueChange = { form = form.copy(password = it) },
                        error = if (form.password.isNotEmpty()) form.passwordError else null,
                        isPassword = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    TijarioButton(
                        text = t("register_title"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    app.tijario.config.Supabase.client.auth.signUpWith(
                                        io.github.jan.supabase.auth.providers.builtin.Email
                                    ) {
                                        email = form.email
                                        password = form.password
                                        data = buildJsonObject {
                                            put("full_name", form.fullName)
                                        }
                                    }
                                    onBackToLogin()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = e.localizedMessage ?: "فشل إنشاء الحساب"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        Text(
                            text = t("or"),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    }

                    val registerGoogleAction = app.tijario.config.Supabase.client.composeAuth.rememberSignInWithGoogle(
                        onResult = { result ->
                            when (result) {
                                is NativeSignInResult.Success -> {
                                    onBackToLogin()
                                }
                                is NativeSignInResult.Error -> {
                                    errorMessage = result.message
                                }
                                else -> {}
                            }
                        }
                    )

                    GoogleSignInButton(
                        onClick = {
                            registerGoogleAction.startFlow()
                        },
                        enabled = !isLoading && !isGoogleLoading,
                        text = t("google_register")
                    )

                    if (isGoogleLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(t("already_have_account"), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                TextButton(onClick = onBackToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        t("btn_login"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val form = LoginFormState(email = email, password = "placeholder")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F766E),
                        Color(0xFF0F766E),
                        Color(0xFF064E3B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = t("reset_password_title"),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = t("reset_password_subtitle"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isSubmitted) {
                        TijarioTextField(
                            label = t("email"),
                            value = email,
                            onValueChange = { email = it },
                            error = if (email.isNotEmpty()) form.emailError else null,
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF64748B))
                            }
                        )

                        TijarioButton(
                            text = t("btn_send_reset"),
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    delay(1200)
                                    isLoading = false
                                    isSubmitted = true
                                }
                            },
                            enabled = email.isNotBlank() && form.emailError == null,
                            isLoading = isLoading
                        )
                    } else {
                        Text(
                            text = t("reset_success"),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedButton(
                        onClick = onBackToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(t("back_to_login"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val countries = listOf("السعودية", "اليمن", "مصر", "الإمارات", "الكويت", "قطر", "عمان", "البحرين", "الأردن", "لبنان", "المغرب", "تونس", "الجزائر", "ليبيا", "السودان", "العراق", "سوريا", "فلسطين")
    val currencies = listOf("SAR", "YER", "EGP", "AED", "KWD", "QAR", "OMR", "BHD", "JOD", "LBP", "MAD", "TND", "DZD", "LYD", "SDG", "IQD", "SYP", "USD", "EUR")

    var form by remember {
        mutableStateOf(
            BusinessSettingsFormState(
                country = try {
                    val currentCountry = java.util.Locale.getDefault().displayCountry
                    if (countries.any { it in currentCountry }) countries.first { it in currentCountry } else "السعودية"
                } catch (e: Exception) { "السعودية" }
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var countryMenuExpanded by remember { mutableStateOf(false) }
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F766E),
                        Color(0xFF0F766E),
                        Color(0xFF064E3B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = t("onboarding_title"),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = t("onboarding_subtitle"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TijarioTextField(
                        label = t("shop_name"),
                        value = form.businessName,
                        onValueChange = { form = form.copy(businessName = it) },
                        error = if (form.businessName.isNotEmpty()) form.businessNameError else null,
                        leadingIcon = {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("whatsapp_phone"),
                        value = form.whatsapp,
                        onValueChange = { form = form.copy(whatsapp = it) },
                        error = if (form.whatsapp.isNotEmpty()) form.whatsappError else null,
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    // Country Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = countryMenuExpanded,
                            onExpandedChange = { countryMenuExpanded = !countryMenuExpanded }
                        ) {
                            TijarioTextField(
                                label = t("country"),
                                value = form.country,
                                onValueChange = {},
                                error = if (form.country.isNotEmpty()) form.countryError else null,
                                leadingIcon = {
                                    Icon(Icons.Filled.Public, contentDescription = null, tint = Color(0xFF64748B))
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded)
                                },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                readOnly = true
                            )
                            ExposedDropdownMenu(
                                expanded = countryMenuExpanded,
                                onDismissRequest = { countryMenuExpanded = false }
                            ) {
                                countries.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) },
                                        onClick = {
                                            form = form.copy(country = selection)
                                            countryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    TijarioTextField(
                        label = "المدينة", // City
                        value = form.city,
                        onValueChange = { form = form.copy(city = it) },
                        leadingIcon = {
                            Icon(Icons.Filled.LocationCity, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    // Currency Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = currencyMenuExpanded,
                            onExpandedChange = { currencyMenuExpanded = !currencyMenuExpanded }
                        ) {
                            TijarioTextField(
                                label = t("currency"),
                                value = form.currency,
                                onValueChange = {},
                                error = if (form.currency.isNotEmpty()) form.currencyError else null,
                                leadingIcon = {
                                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Color(0xFF64748B))
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded)
                                },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                readOnly = true
                            )
                            ExposedDropdownMenu(
                                expanded = currencyMenuExpanded,
                                onDismissRequest = { currencyMenuExpanded = false }
                            ) {
                                currencies.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) },
                                        onClick = {
                                            form = form.copy(currency = selection)
                                            currencyMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    TijarioButton(
                        text = t("btn_save_continue"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
                                        val settings = app.tijario.data.model.BusinessSettings(
                                            userId = currentUser.id,
                                            businessName = form.businessName,
                                            whatsappNumber = form.whatsapp,
                                            country = form.country,
                                            city = form.city.ifBlank { null },
                                            currency = form.currency,
                                            termsText = form.terms.ifBlank { null }
                                        )
                                        app.tijario.config.Supabase.client.from("business_settings")
                                            .upsert(settings)
                                        onDone()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

data class IntroSlide(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color
)

@Composable
fun IntroWalkthroughScreen(onFinished: () -> Unit) {
    val slides = listOf(
        IntroSlide(
            title = "أدر تجارتك بسهولة",
            description = "تتبع فواتيرك، عملائك، ومنتجاتك في تطبيق واحد متكامل ومصمم بذكاء.",
            icon = Icons.Filled.Business,
            iconColor = Color(0xFF0F766E)
        ),
        IntroSlide(
            title = "فواتير وعروض أسعار سريعة",
            description = "أصدر مستنداتك وشاركها مباشرة مع عملائك عبر الواتساب في ثوانٍ معدودة.",
            icon = Icons.Filled.Phone,
            iconColor = Color(0xFF2563EB)
        ),
        IntroSlide(
            title = "تجاريو AI ومساعدك الذكي",
            description = "صياغة ردود ذكية لعملائك وكتابة كابشن لمنتجاتك بلمح البصر لمبيعات أكثر.",
            icon = Icons.Filled.Person,
            iconColor = Color(0xFF7C3AED)
        )
    )

    var currentSlide by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F766E),
                        Color(0xFF0F766E),
                        Color(0xFF064E3B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: App Name & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تجاريو",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onFinished) {
                    Text(text = "تجاوز", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }

            // Slide Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                val slide = slides[currentSlide]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = slide.iconColor.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.iconColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = slide.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = slide.description,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom controls: Indicators and Next button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators (dots)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    slides.forEachIndexed { index, _ ->
                        val active = index == currentSlide
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 18.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (currentSlide < slides.size - 1) {
                            currentSlide++
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentSlide == slides.size - 1) "ابدأ الآن" else "التالي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

