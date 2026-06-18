package app.tijario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import app.tijario.ui.components.TijarioCard
import app.tijario.ui.components.TijarioPage
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.LoginFormState
import app.tijario.ui.state.RegisterFormState

@Composable
fun LoginScreen(
    onLoginReady: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var form by remember { mutableStateOf(LoginFormState()) }

    TijarioPage(
        title = "تسجيل الدخول",
        subtitle = "ادخل إلى حساب تجاري لمتابعة إدارة متجرك ومستنداتك.",
    ) {
        TijarioTextField(
            label = "البريد الإلكتروني",
            value = form.email,
            onValueChange = { form = form.copy(email = it) },
            error = form.emailError,
        )
        TijarioTextField(
            label = "كلمة المرور",
            value = form.password,
            onValueChange = { form = form.copy(password = it) },
            error = form.passwordError,
            visualTransformation = PasswordVisualTransformation(),
        )
        Button(onClick = onLoginReady, enabled = form.canSubmit) {
            Text("تسجيل الدخول")
        }
        OutlinedButton(onClick = onRegister) {
            Text("إنشاء حساب")
        }
        OutlinedButton(onClick = onForgotPassword) {
            Text("نسيت كلمة المرور")
        }
        TijarioCard(
            title = "تكامل backend",
            body = "سيستخدم تسجيل الدخول Supabase Auth مع تخزين جلسة آمن عند ربط العميل الفعلي.",
        )
    }
}

@Composable
fun RegisterScreen(onBackToLogin: () -> Unit) {
    var form by remember { mutableStateOf(RegisterFormState()) }

    TijarioPage(
        title = "إنشاء حساب",
        subtitle = "حساب واحد لنفس مشروع Tijario وسجلات Supabase الحالية.",
    ) {
        TijarioTextField("الاسم الكامل", form.fullName, { form = form.copy(fullName = it) }, error = form.fullNameError)
        TijarioTextField("البريد الإلكتروني", form.email, { form = form.copy(email = it) }, error = form.emailError)
        TijarioTextField(
            label = "كلمة المرور",
            value = form.password,
            onValueChange = { form = form.copy(password = it) },
            error = form.passwordError,
            visualTransformation = PasswordVisualTransformation(),
        )
        Button(onClick = {}, enabled = false && form.canSubmit) {
            Text("إنشاء الحساب")
        }
        OutlinedButton(onClick = onBackToLogin) {
            Text("العودة لتسجيل الدخول")
        }
    }
}

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }

    TijarioPage(
        title = "استعادة كلمة المرور",
        subtitle = "إرسال رابط استعادة عبر Supabase Auth وربط deep link بالتطبيق.",
    ) {
        val form = LoginFormState(email = email, password = "placeholder")
        TijarioTextField("البريد الإلكتروني", email, { email = it }, error = form.emailError)
        Button(onClick = {}, enabled = false) {
            Text("إرسال الرابط")
        }
        OutlinedButton(onClick = onBackToLogin) {
            Text("العودة")
        }
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var form by remember { mutableStateOf(BusinessSettingsFormState()) }

    TijarioPage(
        title = "تهيئة المتجر",
        subtitle = "هذه البيانات تظهر في عروض الأسعار والفواتير.",
    ) {
        TijarioTextField("اسم المتجر", form.businessName, { form = form.copy(businessName = it) }, error = form.businessNameError)
        TijarioTextField("رقم واتساب", form.whatsapp, { form = form.copy(whatsapp = it) }, error = form.whatsappError)
        TijarioTextField("الدولة", form.country, { form = form.copy(country = it) }, error = form.countryError)
        TijarioTextField("العملة", form.currency, { form = form.copy(currency = it) }, error = form.currencyError)
        Button(onClick = onDone, enabled = form.canSubmit) {
            Text("حفظ والمتابعة")
        }
    }
}
