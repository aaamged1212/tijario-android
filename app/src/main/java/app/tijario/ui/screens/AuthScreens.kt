package app.tijario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.tijario.MainActivity
import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.ui.components.GoogleSignInButton
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.TijarioDataViewModel
import app.tijario.ui.state.LoginFormState
import app.tijario.ui.state.RegisterFormState
import app.tijario.ui.state.AuthViewModel
import app.tijario.ui.state.CentralAuthState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
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
private fun AuthLanguageToggle(modifier: Modifier = Modifier) {
    val language = LocalLanguage.current
    val context = LocalContext.current
    IconButton(
        onClick = {
            MainActivity.currentLanguage =
                if (language == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
            AppPreferences.setLanguage(context, MainActivity.currentLanguage)
        },
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
    ) {
        Icon(
            imageVector = Icons.Filled.Language,
            contentDescription = if (language == AppLanguage.AR) "تبديل اللغة" else "Switch language",
            tint = Color.White
        )
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val language = LocalLanguage.current
    var form by remember(language) { mutableStateOf(LoginFormState(lang = language)) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleAttemptId by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val googleSignInEnabled = remember { app.tijario.config.loadAppConfig().isGoogleSignInEnabled }

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
        AuthLanguageToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
                .zIndex(1f)
        )
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
                        contentDescription = if (language == AppLanguage.AR) "شعار التطبيق" else "App logo",
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
                                    authViewModel.handleLoginSuccess()
                                } catch (e: Exception) {
                                    errorMessage = app.tijario.domain.ErrorMapper.map(e, language)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )

                    if (googleSignInEnabled) {
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
                                isGoogleLoading = false
                                authViewModel.handleGoogleSignInResult(result)
                            },
                            fallback = {
                                isGoogleLoading = false
                                errorMessage = Localization.getString("google_login_error", language)
                            }
                        )

                        GoogleSignInButton(
                            onClick = {
                                errorMessage = null
                                isGoogleLoading = true
                                val attemptId = ++googleAttemptId
                                scope.launch {
                                    delay(30_000)
                                    if (isGoogleLoading && googleAttemptId == attemptId) {
                                        isGoogleLoading = false
                                        errorMessage = Localization.getString("google_login_timeout", language)
                                    }
                                }
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
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onVerifyEmail: (String) -> Unit,
) {
    val language = LocalLanguage.current
    var form by remember(language) { mutableStateOf(RegisterFormState(lang = language)) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleAttemptId by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val googleSignInEnabled = remember { app.tijario.config.loadAppConfig().isGoogleSignInEnabled }

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
        AuthLanguageToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
                .zIndex(1f)
        )
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

                    TijarioTextField(
                        label = t("confirm_password"),
                        value = form.confirmPassword,
                        onValueChange = { form = form.copy(confirmPassword = it) },
                        error = if (form.confirmPassword.isNotEmpty()) form.confirmPasswordError else null,
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
                                    authViewModel.signUpEmail = form.email
                                    authViewModel.signUpFullName = form.fullName
                                    onVerifyEmail(form.email)
                                } catch (e: Exception) {
                                    errorMessage = app.tijario.domain.ErrorMapper.map(e, language)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )

                    if (googleSignInEnabled) {
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
                                isGoogleLoading = false
                                authViewModel.handleGoogleSignInResult(result)
                            },
                            fallback = {
                                isGoogleLoading = false
                                errorMessage = Localization.getString("google_login_error", language)
                            }
                        )

                        GoogleSignInButton(
                            onClick = {
                                errorMessage = null
                                isGoogleLoading = true
                                val attemptId = ++googleAttemptId
                                scope.launch {
                                    delay(30_000)
                                    if (isGoogleLoading && googleAttemptId == attemptId) {
                                        isGoogleLoading = false
                                        errorMessage = Localization.getString("google_login_timeout", language)
                                    }
                                }
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
fun VerifyEmailScreen(
    email: String,
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onVerified: () -> Unit,
) {
    val language = LocalLanguage.current
    val emailToUse = remember(email) {
        if (email.isNotEmpty()) email
        else (authViewModel.signUpEmail ?: app.tijario.config.Supabase.client.auth.currentSessionOrNull()?.user?.email ?: "")
    }
    var token by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendAttemptId by remember { mutableIntStateOf(0) }
    var secondsLeft by remember(emailToUse) { mutableIntStateOf(60) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(emailToUse, resendAttemptId) {
        if (emailToUse.isBlank()) {
            secondsLeft = 0
            return@LaunchedEffect
        }

        secondsLeft = 60
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft -= 1
        }
    }

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
        AuthLanguageToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
                .zIndex(1f)
        )
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
                text = if (language == AppLanguage.AR) "تحقق من البريد" else "Verify your email",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (language == AppLanguage.AR) {
                    "أدخل رمز التأكيد الذي أرسل للبريد: $emailToUse"
                } else {
                    "Enter the verification code sent to: $emailToUse"
                },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (secondsLeft > 0) {
                    Localization.getString("verification_code_expires_in", language).format(secondsLeft)
                } else {
                    Localization.getString("verification_code_expired", language)
                },
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
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
                        label = if (language == AppLanguage.AR) "رمز التأكيد" else "Verification code",
                        value = token,
                        onValueChange = { token = app.tijario.domain.OtpValidator.sanitize(it) },
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    TijarioButton(
                        text = if (language == AppLanguage.AR) "تحقق" else "Verify",
                        onClick = {
                            scope.launch {
                                val normalizedCode = app.tijario.domain.OtpValidator.sanitize(token)
                                if (!app.tijario.domain.OtpValidator.isValid(normalizedCode)) {
                                    errorMessage = if (language == AppLanguage.AR) "رمز التحقق يجب أن يكون 8 أرقام" else "Verification code must be 8 digits"
                                    return@launch
                                }
                                if (secondsLeft <= 0) {
                                    errorMessage = Localization.getString("verification_code_expired", language)
                                    return@launch
                                }

                                try {
                                    isLoading = true
                                    errorMessage = null

                                    // 1. Verify OTP
                                    try {
                                        app.tijario.config.Supabase.client.auth.verifyEmailOtp(
                                            type = OtpType.Email.EMAIL,
                                            email = emailToUse,
                                            token = normalizedCode,
                                        )
                                    } catch (otpEx: Exception) {
                                        if (app.tijario.BuildConfig.DEBUG) {
                                            android.util.Log.e("VerifyEmailScreen", "OTP Verification failed: ${otpEx.javaClass.simpleName}", otpEx)
                                        }
                                        val rawMsg = otpEx.message.orEmpty()
                                        errorMessage = when {
                                            rawMsg.contains("network", ignoreCase = true) || otpEx is java.io.IOException -> {
                                                if (language == AppLanguage.AR) "خطأ في الاتصال بالشبكة، يرجى المحاولة مرة أخرى" else "Network error, please try again"
                                            }
                                            rawMsg.contains("invalid flow state", ignoreCase = true) ||
                                            rawMsg.contains("otp expired", ignoreCase = true) ||
                                            rawMsg.contains("code expired", ignoreCase = true) ||
                                            rawMsg.contains("token expired", ignoreCase = true) ||
                                            rawMsg.contains("invalid grant", ignoreCase = true) -> {
                                                if (language == AppLanguage.AR) "رمز التحقق غير صحيح أو منتهي الصلاحية" else "Invalid or expired code"
                                            }
                                            else -> {
                                                if (language == AppLanguage.AR) "حدث خطأ غير متوقع، يرجى المحاولة لاحقًا" else "An unexpected error occurred, please try again later"
                                            }
                                        }
                                        return@launch
                                    }

                                    // 2. Wait briefly for the session to be available after verification
                                    var session = app.tijario.config.Supabase.client.auth.currentSessionOrNull()
                                    var attempts = 0
                                    while (session == null && attempts < 20) {
                                        delay(250)
                                        session = app.tijario.config.Supabase.client.auth.currentSessionOrNull()
                                        attempts += 1
                                    }
                                    if (session == null) {
                                        errorMessage = if (language == AppLanguage.AR) "لم يتم العثور على جلسة صالحة بعد التحقق" else "No valid session found after verification"
                                        return@launch
                                    }

                                    // 3. User Bootstrap
                                    try {
                                        val userId = session.user?.id ?: error("User ID not found in session")
                                        val resolvedName = authViewModel.signUpFullName ?: runCatching {
                                            session.user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
                                        }.getOrNull()

                                        val bootstrapResult = authViewModel.bootstrapUserAfterVerification(userId, resolvedName)
                                        if (bootstrapResult.isFailure) {
                                            throw bootstrapResult.exceptionOrNull() ?: Exception("Bootstrap failed")
                                        }
                                    } catch (bootEx: Exception) {
                                        if (app.tijario.BuildConfig.DEBUG) {
                                            android.util.Log.e("VerifyEmailScreen", "User bootstrap failed: ${bootEx.javaClass.simpleName}", bootEx)
                                        }
                                        errorMessage = if (language == AppLanguage.AR) "نجح التحقق ولكن فشل إعداد الحساب، يرجى المحاولة لاحقًا" else "Verification succeeded but account setup failed"
                                        return@launch
                                    }

                                    onVerified()
                                } catch (e: Exception) {
                                    errorMessage = app.tijario.domain.ErrorMapper.map(e, language)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = app.tijario.domain.OtpValidator.isValid(token) && emailToUse.isNotBlank() && !isLoading && secondsLeft > 0,
                        isLoading = isLoading
                    )

                    TijarioButton(
                        text = if (isResending) {
                            if (language == AppLanguage.AR) "جارٍ الإرسال..." else "Sending..."
                        } else if (secondsLeft > 0) {
                            Localization.getString("resend_code_wait", language).format(secondsLeft)
                        } else {
                            Localization.getString("resend_code", language)
                        },
                        onClick = {
                            scope.launch {
                                try {
                                    isResending = true
                                    errorMessage = null
                                    app.tijario.config.Supabase.client.auth.resendEmail(
                                        OtpType.Email.SIGNUP,
                                        emailToUse
                                    )
                                    token = ""
                                    resendAttemptId += 1
                                    errorMessage = Localization.getString("verification_code_resent", language)
                                } catch (e: Exception) {
                                    errorMessage = app.tijario.domain.ErrorMapper.map(e, language)
                                } finally {
                                    isResending = false
                                }
                            }
                        },
                        enabled = emailToUse.isNotBlank() && !isLoading && !isResending && secondsLeft <= 0,
                        isLoading = isResending
                    )

                    OutlinedButton(
                        onClick = onBackToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (language == AppLanguage.AR) "العودة إلى تسجيل الدخول" else "Back to login",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    val language = LocalLanguage.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
        AuthLanguageToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
                .zIndex(1f)
        )
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
                                    try {
                                        isLoading = true
                                        errorMessage = null
                                        val result = app.tijario.config.Supabase.apiClient.requestPasswordReset(
                                            app.tijario.data.remote.ResetPasswordRequest(email = email)
                                        )
                                        if (result.ok) {
                                            isSubmitted = true
                                        } else {
                                            errorMessage = result.displayMessage
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = if (language == AppLanguage.AR) "تعذر إرسال رابط إعادة التعيين. حاول مرة أخرى." else "Unable to send reset link. Try again."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = email.isNotBlank() && form.emailError == null,
                            isLoading = isLoading
                        )

                        errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
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
fun OnboardingScreen(
    dataViewModel: TijarioDataViewModel,
    onDone: () -> Unit,
) {
    val language = LocalLanguage.current
    val countries = if (language == AppLanguage.AR) listOf("السعودية", "اليمن", "مصر", "الإمارات", "الكويت", "قطر", "عمان", "البحرين", "الأردن", "لبنان", "المغرب", "تونس", "الجزائر", "ليبيا", "السودان", "العراق", "سوريا", "فلسطين") else listOf("Saudi Arabia", "Yemen", "Egypt", "United Arab Emirates", "Kuwait", "Qatar", "Oman", "Bahrain", "Jordan", "Lebanon", "Morocco", "Tunisia", "Algeria", "Libya", "Sudan", "Iraq", "Syria", "Palestine")
    val currencies = listOf("SAR", "YER", "EGP", "AED", "KWD", "QAR", "OMR", "BHD", "JOD", "LBP", "MAD", "TND", "DZD", "LYD", "SDG", "IQD", "SYP", "USD", "EUR")

    var form by remember(language) {
        mutableStateOf(
            BusinessSettingsFormState(
                country = try {
                    val currentCountry = java.util.Locale.getDefault().displayCountry
                    if (countries.any { it in currentCountry }) countries.first { it in currentCountry } else if (language == AppLanguage.AR) "السعودية" else "Saudi Arabia"
                } catch (e: Exception) { if (language == AppLanguage.AR) "السعودية" else "Saudi Arabia" },
                lang = language
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
        AuthLanguageToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
                .zIndex(1f)
        )
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
                        label = if (language == AppLanguage.AR) "المدينة" else "City",
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
                                        val result = dataViewModel.saveBusinessSettings(settings)
                                        if (result.isSuccess) {
                                            onDone()
                                        }
                                    }
                                } catch (e: Exception) {
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
    val language = LocalLanguage.current
    val slides = listOf(
        IntroSlide(
            title = if (language == AppLanguage.AR) "أدر تجارتك بسهولة" else "Run your business easily",
            description = if (language == AppLanguage.AR) "تتبع فواتيرك، عملائك، ومنتجاتك في تطبيق واحد متكامل ومصمم بذكاء." else "Track your invoices, customers, and products in one integrated app built for speed.",
            icon = Icons.Filled.Business,
            iconColor = Color(0xFF0F766E)
        ),
        IntroSlide(
            title = if (language == AppLanguage.AR) "فواتير وعروض أسعار سريعة" else "Fast quotes and invoices",
            description = if (language == AppLanguage.AR) "أصدر مستنداتك وشاركها مباشرة مع عملائك عبر واتساب في ثوانٍ معدودة." else "Create documents and share them with customers over WhatsApp in seconds.",
            icon = Icons.Filled.Phone,
            iconColor = Color(0xFF2563EB)
        ),
        IntroSlide(
            title = if (language == AppLanguage.AR) "تجاريو AI ومساعدك الذكي" else "Tijario AI and your smart assistant",
            description = if (language == AppLanguage.AR) "صياغة ردود ذكية لعملائك وكتابة كابشن لمنتجاتك بلمح البصر لمبيعات أكثر." else "Generate smart replies and product captions instantly to drive more sales.",
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
        AuthLanguageToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
                .zIndex(1f)
        )
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
                    text = if (language == AppLanguage.AR) "تجاريو" else "Tijario",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onFinished) {
                        Text(text = if (language == AppLanguage.AR) "تجاوز" else "Skip", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
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
                        text = if (currentSlide == slides.size - 1) { if (language == AppLanguage.AR) "ابدأ الآن" else "Get Started" } else { if (language == AppLanguage.AR) "التالي" else "Next" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
