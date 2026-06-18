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
import androidx.compose.ui.unit.dp
import app.tijario.domain.Validation
import app.tijario.ui.components.TijarioCard
import app.tijario.ui.components.TijarioPage
import app.tijario.ui.components.TijarioTextField

@Composable
fun LoginScreen(
    onLoginReady: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val emailError = Validation.email(email)
    val passwordError = Validation.password(password)
    val canSubmit = emailError == null && passwordError == null

    TijarioPage(
        title = "تسجيل الدخول",
        subtitle = "ادخل إلى حساب تجاري لمتابعة إدارة متجرك ومستنداتك.",
    ) {
        TijarioTextField("البريد الإلكتروني", email, { email = it }, error = emailError)
        TijarioTextField(
            label = "كلمة المرور",
            value = password,
            onValueChange = { password = it },
            error = passwordError,
            visualTransformation = PasswordVisualTransformation(),
        )
        Button(onClick = onLoginReady, enabled = canSubmit) {
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
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    TijarioPage(
        title = "إنشاء حساب",
        subtitle = "حساب واحد لنفس مشروع Tijario وسجلات Supabase الحالية.",
    ) {
        TijarioTextField("الاسم الكامل", fullName, { fullName = it }, error = Validation.required(fullName, "الاسم"))
        TijarioTextField("البريد الإلكتروني", email, { email = it }, error = Validation.email(email))
        TijarioTextField(
            label = "كلمة المرور",
            value = password,
            onValueChange = { password = it },
            error = Validation.password(password),
            visualTransformation = PasswordVisualTransformation(),
        )
        Button(onClick = {}, enabled = false) {
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
        TijarioTextField("البريد الإلكتروني", email, { email = it }, error = Validation.email(email))
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
    var businessName by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("السعودية") }
    var currency by remember { mutableStateOf("SAR") }

    TijarioPage(
        title = "تهيئة المتجر",
        subtitle = "هذه البيانات تظهر في عروض الأسعار والفواتير.",
    ) {
        TijarioTextField("اسم المتجر", businessName, { businessName = it }, error = Validation.required(businessName, "اسم المتجر"))
        TijarioTextField("رقم واتساب", whatsapp, { whatsapp = it }, error = Validation.whatsapp(whatsapp))
        TijarioTextField("الدولة", country, { country = it }, error = Validation.required(country, "الدولة"))
        TijarioTextField("العملة", currency, { currency = it }, error = Validation.required(currency, "العملة"))
        Button(onClick = onDone) {
            Text("حفظ والمتابعة")
        }
    }
}
