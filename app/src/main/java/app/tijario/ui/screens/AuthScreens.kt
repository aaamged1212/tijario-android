package app.tijario.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.util.Log
import app.tijario.config.AppRuntimeState
import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.domain.CountryCatalog
import app.tijario.domain.CurrencyCatalog
import app.tijario.domain.LocalizedErrorMapper
import app.tijario.domain.normalizePhoneWithDialCode
import app.tijario.domain.splitPhoneNumber
import app.tijario.data.remote.localizedDisplayMessage
import app.tijario.ui.components.GoogleSignInButton
import app.tijario.ui.components.CountryPickerBottomSheet
import app.tijario.ui.components.StoreLogoPicker
import app.tijario.ui.components.buildLogoUploadRequest
import app.tijario.ui.components.TijarioPhoneField
import app.tijario.ui.components.clearBusinessLogoCache
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.components.CurrencyBottomSheet
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.TijarioDataViewModel
import app.tijario.ui.state.AccountInitializationState
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
private fun AuthLanguageToggle(modifier: Modifier = Modifier) {
    val language = LocalLanguage.current
    val context = LocalContext.current
    IconButton(
        onClick = {
            AppRuntimeState.currentLanguage =
                if (language == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
            AppPreferences.setLanguage(context, AppRuntimeState.currentLanguage)
        },
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            imageVector = Icons.Filled.Language,
            contentDescription = if (language == AppLanguage.AR) "تبديل اللغة" else "Switch language",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthThemeToggle(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            AppRuntimeState.isDarkMode = !AppRuntimeState.isDarkMode
            AppPreferences.setDarkMode(context, AppRuntimeState.isDarkMode)
        },
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Icon(
            imageVector = if (AppRuntimeState.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = t("settings_theme"),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthScreenBackground(content: @Composable BoxScope.() -> Unit) {
    val background = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        background,
                    ),
                ),
            ),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .align(Alignment.TopCenter),
        ) {
            val center = Offset(size.width / 2f, -size.height * 0.12f)
            listOf(0.72f, 0.98f, 1.24f).forEachIndexed { index, scale ->
                drawCircle(
                    color = accent.copy(alpha = 0.11f - index * 0.025f),
                    radius = size.width * scale,
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuthThemeToggle()
            AuthLanguageToggle()
        }
        content()
    }
}

@Composable
private fun AuthBrandHeader(compact: Boolean = false) {
    val language = LocalLanguage.current
    Surface(
        modifier = Modifier.size(if (compact) 64.dp else 76.dp),
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = app.tijario.R.drawable.logo_app),
                contentDescription = if (language == AppLanguage.AR) "شعار التطبيق" else "App logo",
                modifier = Modifier
                    .size(if (compact) 54.dp else 64.dp)
                    .clip(RoundedCornerShape(if (compact) 14.dp else 17.dp)),
            )
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        text = t("app_name"),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = if (compact) 24.sp else 28.sp,
        fontWeight = FontWeight.Black,
    )
    Text(
        text = t("app_slogan"),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
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

    AuthScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AuthBrandHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Card Form
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Text(
                        text = t("login_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = t("or"),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
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
                Text(t("no_account"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextButton(onClick = onRegister, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        t("create_account"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline,
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
    val context = LocalContext.current
    val googleSignInEnabled = remember { app.tijario.config.loadAppConfig().isGoogleSignInEnabled }

    AuthScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AuthBrandHeader(compact = true)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = t("register_title"),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = t("register_subtitle"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            put("name", form.fullName)
                                            put("preferred_language", language.name.lowercase())
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

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = t("register_consent_prefix"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = { openExternalPage(context, "https://tijario.site/terms") },
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = t("terms_cond"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                )
                            }
                            Text(
                                text = t("register_consent_and"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(
                                onClick = { openExternalPage(context, "https://tijario.site/privacy") },
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = t("privacy_policy"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                )
                            }
                        }
                    }

                    if (googleSignInEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = t("or"),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
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
                Text(t("already_have_account"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextButton(onClick = onBackToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        t("btn_login"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline,
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
    var awaitingBootstrapRetry by remember { mutableStateOf(false) }
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

    suspend fun waitForAuthContext(timeoutMs: Long = 5_000L): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val session = app.tijario.config.Supabase.client.auth.currentSessionOrNull()
            val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
            if (session != null && currentUser != null) return true
            delay(250)
        }
        return app.tijario.config.Supabase.client.auth.currentSessionOrNull() != null &&
            app.tijario.config.Supabase.client.auth.currentUserOrNull() != null
    }

    suspend fun bootstrapCurrentSession(): Boolean {
        if (!waitForAuthContext()) {
            if (app.tijario.BuildConfig.DEBUG) {
                Log.w("VerifyEmailScreen", "Session unavailable after OTP")
            }
            errorMessage = if (language == AppLanguage.AR) "لم يتم العثور على جلسة صالحة بعد التحقق" else "No valid session found after verification"
            return false
        }

        val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
        if (currentUser == null) {
            if (app.tijario.BuildConfig.DEBUG) {
                Log.w("VerifyEmailScreen", "Current user unavailable after OTP")
            }
            errorMessage = if (language == AppLanguage.AR) "تعذر تحديد المستخدم الحالي بعد التحقق" else "Unable to resolve the current user after verification"
            return false
        }

        val resolvedName = authViewModel.signUpFullName ?: listOfNotNull(
            currentUser.userMetadata?.get("full_name")?.toString(),
            currentUser.userMetadata?.get("name")?.toString(),
            currentUser.userMetadata?.get("preferred_username")?.toString(),
        )
            .map { it.replace("\"", "").trim() }
            .firstOrNull { it.isNotBlank() }

        val bootstrapResult = authViewModel.bootstrapUserAfterVerification(currentUser.id, resolvedName)
        if (bootstrapResult.isFailure) {
            if (app.tijario.BuildConfig.DEBUG) {
                Log.e("VerifyEmailScreen", "operation=verification_bootstrap result=failed error=${bootstrapResult.exceptionOrNull()?.javaClass?.simpleName ?: "unknown"}")
            }
            errorMessage = if (language == AppLanguage.AR) "نجح التحقق ولكن فشل إعداد الحساب، يرجى المحاولة لاحقاً" else "Verification succeeded but account setup failed"
            awaitingBootstrapRetry = true
            return false
        }

        awaitingBootstrapRetry = false
        return true
    }

    AuthScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (language == AppLanguage.AR) "تحقق من البريد" else "Verify your email",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (language == AppLanguage.AR) {
                    "أدخل رمز التأكيد الذي أرسل للبريد: $emailToUse"
                } else {
                    "Enter the verification code sent to: $emailToUse"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

                    Text(
                        text = if (language == AppLanguage.AR) 
                            "* إذا لم يظهر الرمز في البريد الوارد، يرجى التحقق من مجلد الرسائل المهملة (Spam/Junk)." 
                        else 
                            "* If you cannot find the code in your inbox, please check your Spam/Junk folder.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    TijarioButton(
                        text = if (awaitingBootstrapRetry) {
                            if (language == AppLanguage.AR) "إعادة محاولة إعداد الحساب" else "Retry account setup"
                        } else if (language == AppLanguage.AR) "تحقق" else "Verify",
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null

                                    if (!awaitingBootstrapRetry) {
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
                                            app.tijario.config.Supabase.client.auth.verifyEmailOtp(
                                                type = OtpType.Email.EMAIL,
                                                email = emailToUse,
                                                token = normalizedCode,
                                            )
                                        } catch (otpEx: Exception) {
                                            if (app.tijario.BuildConfig.DEBUG) {
                                                Log.e("VerifyEmailScreen", "operation=otp_verify result=failed error=${otpEx.javaClass.simpleName}")
                                            }
                                            errorMessage = app.tijario.domain.ErrorMapper.map(otpEx, language)
                                            return@launch
                                        }
                                    }

                                    if (!bootstrapCurrentSession()) {
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
                        enabled = emailToUse.isNotBlank() && !isLoading && (
                            if (awaitingBootstrapRetry) true else app.tijario.domain.OtpValidator.isValid(token) && secondsLeft > 0
                        ),
                        isLoading = isLoading
                    )

                    if (!awaitingBootstrapRetry) {
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
                                        if (app.tijario.BuildConfig.DEBUG) {
                                            Log.e("VerifyEmailScreen", "operation=otp_resend result=failed error=${e.javaClass.simpleName}")
                                        }
                                        errorMessage = app.tijario.domain.ErrorMapper.map(e, language)
                                    } finally {
                                        isResending = false
                                    }
                                }
                            },
                            enabled = emailToUse.isNotBlank() && !isLoading && !isResending && secondsLeft <= 0,
                            isLoading = isResending
                        )
                    }

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

    AuthScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = t("reset_password_title"),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = t("reset_password_subtitle"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            app.tijario.data.remote.ResetPasswordRequest(email = email, source = "android")
                                        )
                                        if (result.ok) {
                                            isSubmitted = true
                                        } else {
                                            errorMessage = result.localizedDisplayMessage(language)
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
    val initializationState by dataViewModel.accountInitializationState.collectAsState()
    val currencies = CurrencyCatalog.options

    val context = LocalContext.current
    var form by remember(language) {
        val detectedCountry = CountryCatalog.detectCountry(context)
        val detectedCurrency = CountryCatalog.detectCurrency(detectedCountry.countryCode)
        mutableStateOf(
            BusinessSettingsFormState(
                country = detectedCountry.storageName,
                currency = detectedCurrency,
                lang = language
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dataViewModel.initializeAccount()
    }

    var showCountryPicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedLogoUri = uri
        }
    }

    AuthScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = t("onboarding_title"),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = t("onboarding_subtitle"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StoreLogoPicker(
                            logoUrl = null,
                            previewUri = selectedLogoUri,
                            isUploading = isLoading,
                            onClick = { logoPicker.launch("image/*") },
                        )
                        Text(
                            text = t("onboarding_logo_hint"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                    TijarioTextField(
                        label = t("shop_name"),
                        value = form.businessName,
                        onValueChange = { form = form.copy(businessName = it) },
                        error = if (form.businessName.isNotEmpty()) form.businessNameError else null,
                        leadingIcon = {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TijarioTextField(
                            label = t("country"),
                            value = CountryCatalog.display(form.country, language),
                            onValueChange = {},
                            error = if (form.country.isNotEmpty()) form.countryError else null,
                            leadingIcon = {
                                Icon(Icons.Filled.Public, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            },
                            readOnly = true,
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showCountryPicker = true },
                        )
                    }

                    TijarioPhoneField(
                        value = form.whatsapp,
                        onValueChange = { form = form.copy(whatsapp = it) },
                        error = if (form.whatsapp.isNotEmpty()) form.whatsappError else null,
                        defaultDialCode = CountryCatalog.dialCodeFor(form.country),
                        onCountryCodeSelected = { option ->
                            val resolvedCountry = CountryCatalog.find(option.countryCode)
                            val resolvedCurrency = CountryCatalog.detectCurrency(option.countryCode)
                            form = form.copy(
                                country = resolvedCountry?.storageName ?: form.country,
                                currency = resolvedCurrency
                            )
                        },
                    )

                    TijarioTextField(
                        label = t("city"),
                        value = form.city,
                        onValueChange = { form = form.copy(city = it) },
                        leadingIcon = {
                            Icon(Icons.Filled.LocationCity, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("business_address"),
                        value = form.address,
                        onValueChange = { form = form.copy(address = it) },
                        leadingIcon = {
                            Icon(Icons.Filled.LocationCity, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("business_email"),
                        value = form.email,
                        onValueChange = { form = form.copy(email = it) },
                        error = form.emailError,
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    TijarioTextField(
                        label = t("business_website"),
                        value = form.websiteUrl,
                        onValueChange = { form = form.copy(websiteUrl = it) },
                        error = form.websiteError,
                        leadingIcon = {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    )

                    // Currency Dropdown
                    // Currency Picker Bottom Sheet Trigger
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TijarioTextField(
                            label = t("currency"),
                            value = CurrencyCatalog.display(form.currency, language),
                            onValueChange = {},
                            error = if (form.currency.isNotEmpty()) form.currencyError else null,
                            leadingIcon = {
                                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            },
                            readOnly = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showCurrencyPicker = true }
                        )
                    }

                    if (showCurrencyPicker) {
                        CurrencyBottomSheet(
                            onDismiss = { showCurrencyPicker = false },
                            onSelected = { selectedCode ->
                                form = form.copy(currency = selectedCode)
                            },
                            language = language
                        )
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                        )
                    }

                    when (initializationState) {
                        AccountInitializationState.Initializing,
                        AccountInitializationState.Idle -> Text(
                            text = Localization.getString("account_initialization_preparing", language),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AccountInitializationState.RetryableFailure -> {
                            Text(
                                text = Localization.getString("account_initialization_retry", language),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                onClick = dataViewModel::retryAccountInitialization,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) { Text(t("retry")) }
                        }
                        AccountInitializationState.InvalidEntitlement -> Text(
                            text = Localization.getString("account_initialization_invalid", language),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AccountInitializationState.Unauthenticated -> Text(
                            text = Localization.getString("error_session_expired", language),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AccountInitializationState.Ready -> Unit
                    }

                    TijarioButton(
                        text = t("btn_save_continue"),
                        onClick = {
                            scope.launch {
                                if (initializationState != AccountInitializationState.Ready) return@launch
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
                                        val baseSettings = app.tijario.data.model.BusinessSettings(
                                            userId = currentUser.id,
                                            businessName = form.businessName,
                                            whatsappNumber = form.whatsapp,
                                            country = form.country,
                                            city = form.city.ifBlank { null },
                                            address = form.address.ifBlank { null },
                                            email = form.email.ifBlank { null },
                                            websiteUrl = form.websiteUrl.ifBlank { null },
                                            currency = form.currency,
                                            termsText = form.terms.ifBlank { null }
                                        )
                                        val result = dataViewModel.saveBusinessSettings(baseSettings)
                                        if (result.isFailure) {
                                            val error = result.exceptionOrNull()
                                            errorMessage = LocalizedErrorMapper.map(
                                                (error as? app.tijario.data.repository.AccountInitializationException)?.code,
                                                error?.message,
                                                language,
                                            )
                                            return@launch
                                        }

                                        selectedLogoUri?.let { logoUri ->
                                            val uploadRequest = buildLogoUploadRequest(context, logoUri, language)
                                            val uploadResult = app.tijario.config.Supabase.apiClient.uploadBusinessLogo(uploadRequest)
                                            val uploadedUrl = uploadResult.data?.logoUrl
                                            if (!uploadResult.ok || uploadedUrl.isNullOrBlank()) {
                                                errorMessage = uploadResult.localizedDisplayMessage(language).ifBlank { Localization.getString("logo_upload_error", language) }
                                                return@launch
                                            }
                                            clearBusinessLogoCache(context)
                                            val logoSave = dataViewModel.saveBusinessSettings(baseSettings.copy(logoUrl = uploadedUrl))
                                            if (logoSave.isFailure) {
                                                val error = logoSave.exceptionOrNull()
                                                errorMessage = LocalizedErrorMapper.map(
                                                    (error as? app.tijario.data.repository.AccountInitializationException)?.code,
                                                    error?.message,
                                                    language,
                                                )
                                                return@launch
                                            }
                                        }

                                        onDone()
                                    } else {
                                        errorMessage = Localization.getString("save_settings_error", language)
                                    }
                                } catch (error: Exception) {
                                    errorMessage = LocalizedErrorMapper.map(
                                        (error as? app.tijario.data.repository.AccountInitializationException)?.code,
                                        error.message,
                                        language,
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit && initializationState == AccountInitializationState.Ready,
                        isLoading = isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showCountryPicker) {
        CountryPickerBottomSheet(
            currentCountry = form.country,
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                form = form.copy(
                    country = country.storageName,
                    whatsapp = splitPhoneNumber(form.whatsapp).let { phone ->
                        normalizePhoneWithDialCode(country.dialCode ?: "+966", phone.localNumber)
                    },
                )
                showCountryPicker = false
            },
        )
    }
}

private fun openExternalPage(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

@Composable
fun IntroWalkthroughScreen(onFinished: () -> Unit) {
    val systemLocale = java.util.Locale.getDefault()
    val isArabic = systemLocale.language == "ar"

    val backgroundImage = if (isArabic) {
        app.tijario.R.drawable.onboarding_background_ar
    } else {
        app.tijario.R.drawable.onboarding_background_en
    }

    val buttonText = if (isArabic) "ابدأ الآن" else "Start Now"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020E1C))
    ) {
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0FA36E),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
