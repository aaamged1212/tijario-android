package app.tijario.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tijario.R
import app.tijario.config.AppLanguage
import app.tijario.config.LocalLanguage
import app.tijario.config.Supabase
import app.tijario.data.remote.FeedbackImageEncoder
import app.tijario.features.billing.BillingCatalog
import app.tijario.features.billing.BillingUiEffect
import app.tijario.features.billing.BillingViewModel
import app.tijario.features.billing.GooglePlayBillingRepository
import app.tijario.features.notifications.NotificationsViewModel
import app.tijario.ui.state.TijarioDataViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class YemenPaymentMethod(
    val id: String,
    val accountName: String,
    val accountValues: List<Pair<String, String>>,
    val iconRes: Int,
) {
    Kuraimi(
        id = "kuraimi",
        accountName = "أمجد حامد عبده الحمادي",
        accountValues = listOf("yer" to "3001047999", "sar" to "3107287174", "usd" to "3104501043"),
        iconRes = R.drawable.ic_payment_kuraimi,
    ),
    Jeeb(
        id = "jeeb",
        accountName = "أمجد حامد الحمادي",
        accountValues = listOf("wallet" to "86248"),
        iconRes = R.drawable.ic_payment_jeeb,
    ),
    InternalTransfer(
        id = "internal_transfer",
        accountName = "أمجد حامد عبده احمد الحمادي",
        accountValues = listOf("mobile" to "779400097"),
        iconRes = R.drawable.ic_payment_internal,
    ),
}

private fun YemenPaymentMethod.localized(isArabic: Boolean) = when (this) {
    YemenPaymentMethod.Kuraimi -> if (isArabic) "بنك الكريمي" else "Al-Kuraimi Bank"
    YemenPaymentMethod.Jeeb -> if (isArabic) "محفظة جيب" else "Jeeb wallet"
    YemenPaymentMethod.InternalTransfer -> if (isArabic) "حوالة داخلية" else "Internal transfer"
}

private fun YemenPaymentMethod.localizedDescription(isArabic: Boolean) = when (this) {
    YemenPaymentMethod.Kuraimi -> if (isArabic) "حسابات تحويل بالريال اليمني والسعودي والدولار." else "Transfer accounts in YER, SAR, and USD."
    YemenPaymentMethod.Jeeb -> if (isArabic) "محفظة شخصية داخل اليمن." else "Personal wallet in Yemen."
    YemenPaymentMethod.InternalTransfer -> if (isArabic) "تحويل عبر شبكات الصرافة المحلية إلى اسم المستلم." else "Transfer through local exchange networks to the recipient."
}

private fun YemenPaymentMethod.recipientLabel(isArabic: Boolean) =
    if (isArabic) "اسم المستلم" else "Recipient name"

private fun localizedPaymentValueLabel(label: String, isArabic: Boolean) = when (label) {
    "yer" -> if (isArabic) "ريال يمني" else "Yemeni rial"
    "sar" -> if (isArabic) "ريال سعودي" else "Saudi riyal"
    "usd" -> if (isArabic) "دولار أمريكي" else "US dollar"
    "wallet" -> if (isArabic) "رقم المحفظة" else "Wallet number"
    "mobile" -> if (isArabic) "رقم الجوال" else "Mobile number"
    else -> label
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YemenPaymentMethodsScreen(
    planCode: String,
    interval: String,
    dataViewModel: TijarioDataViewModel,
    onOpenProof: (YemenPaymentMethod) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    val context = LocalContext.current
    val activity = remember(context) { context.findActivityForPayments() }
    val currentUserId = Supabase.client.auth.currentUserOrNull()?.id.orEmpty()
    val billingViewModel: BillingViewModel = viewModel(
        factory = remember(context) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BillingViewModel(GooglePlayBillingRepository(context.applicationContext, Supabase.apiClient)) as T
            }
        },
    )
    val billingState by billingViewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val price = planPrice(planCode, interval, isArabic)

    LaunchedEffect(interval) {
        billingViewModel.selectInterval(interval.takeIf { it in BillingCatalog.supportedIntervals } ?: BillingCatalog.INTERVAL_MONTHLY)
        billingViewModel.load()
    }
    LaunchedEffect(billingViewModel) {
        billingViewModel.effects.collect { effect ->
            if (effect is BillingUiEffect.PurchaseVerified) dataViewModel.refreshUntilPlanMatches(effect.expectedPlanCode)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isArabic) "اختيار الخطة" else "Choose plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isArabic) "رجوع" else "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlanPriceCard(planCode, price, interval, isArabic)
            TabRow(selectedTabIndex = selectedTab) {
                PaymentRegionTab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = if (isArabic) "عالمي" else "Global",
                    icon = Icons.Filled.Public,
                )
                PaymentRegionTab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = if (isArabic) "اليمن" else "Yemen",
                    icon = Icons.Filled.Language,
                )
            }
            if (selectedTab == 0) {
                PaymentMethodCard(
                    title = "Google Play",
                    subtitle = if (isArabic) "أكمل الاشتراك بشكل آمن عبر Google Play." else "Complete your subscription securely through Google Play.",
                    iconRes = R.drawable.ic_payment_google_play,
                    onClick = {
                        if (activity != null && currentUserId.isNotBlank()) billingViewModel.purchase(activity, currentUserId, planCode)
                    },
                    footer = {
                        Text(if (billingState.isPurchasing) if (isArabic) "جارٍ فتح Google Play..." else "Opening Google Play..." else if (isArabic) "المتابعة عبر Google Play" else "Continue with Google Play")
                    },
                    enabled = !billingState.isPurchasing,
                )
            } else {
                Text(if (isArabic) "طرق الدفع المتوفرة" else "Available payment methods", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(if (isArabic) "اختر الطريقة المناسبة ثم ارفع إثبات الدفع في الصفحة التالية." else "Choose a method, then upload payment proof on the next screen.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                YemenPaymentMethod.entries.forEach { method ->
                    PaymentMethodCard(
                        title = method.localized(isArabic),
                        subtitle = method.localizedDescription(isArabic),
                        iconRes = method.iconRes,
                        onClick = { onOpenProof(method) },
                    footer = { Text(if (isArabic) "اضغط للمتابعة" else "Tap to continue") },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YemenPaymentProofScreen(
    planCode: String,
    interval: String,
    methodId: String,
    dataViewModel: TijarioDataViewModel,
    notificationsViewModel: NotificationsViewModel,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
) {
    val method = YemenPaymentMethod.entries.firstOrNull { it.id == methodId } ?: YemenPaymentMethod.Kuraimi
    val context = LocalContext.current
    val isArabic = LocalLanguage.current == AppLanguage.AR
    val scope = rememberCoroutineScope()
    val currentUserId = Supabase.client.auth.currentUserOrNull()?.id.orEmpty()
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var receiptPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { receiptUri = it }

    LaunchedEffect(receiptUri) {
        receiptPreview = withContext(Dispatchers.IO) {
            receiptUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isArabic) "التحويل ورفع الإيصال" else "Transfer and upload receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, if (isArabic) "رجوع" else "Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlanPriceCard(planCode, planPrice(planCode, interval, isArabic), interval, isArabic)
            Text(if (isArabic) "طريقة الدفع المختارة" else "Selected payment method", color = MaterialTheme.colorScheme.onSurfaceVariant)
            PaymentMethodCard(method.localized(isArabic), method.localizedDescription(isArabic), method.iconRes, onClick = {}, footer = {})
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CopyableValue(method.recipientLabel(isArabic), method.accountName, isArabic)
                    method.accountValues.forEach { (label, value) ->
                        CopyableValue(localizedPaymentValueLabel(label, isArabic), value, isArabic)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isArabic) "صورة إثبات الدفع" else "Payment receipt image", fontWeight = FontWeight.Bold)
                Text(" *", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            receiptPreview?.let { preview ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Image(
                        bitmap = preview,
                        contentDescription = if (isArabic) "معاينة الإيصال" else "Receipt preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Image, null)
                Text(if (receiptUri == null) if (isArabic) "رفع صورة الإيصال" else "Upload receipt image" else if (isArabic) "تم اختيار الإيصال" else "Receipt selected")
            }
            Text(
                if (isArabic) "تستغرق المراجعة والتفعيل عادةً من ساعة إلى ساعتين خلال أوقات الدوام. سيصلك إشعار التفعيل داخل التطبيق وعبر البريد الإلكتروني." else "Verification and activation usually take one to two hours during business hours. You will be notified in the app and by email.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                enabled = receiptUri != null && !isSubmitting && currentUserId.isNotBlank(),
                onClick = {
                    val uri = receiptUri ?: return@Button
                    isSubmitting = true
                    errorMessage = null
                    scope.launch {
                        val receipt = withContext(Dispatchers.IO) {
                            runCatching { FeedbackImageEncoder.encode(context.contentResolver, uri) }.getOrNull()
                        }
                        if (receipt == null) {
                            errorMessage = if (isArabic) "تعذر قراءة صورة الإيصال. اختر صورة صالحة." else "The receipt image could not be read. Choose a valid image."
                            isSubmitting = false
                            return@launch
                        }
                        val result = dataViewModel.submitManualPayment(
                            planCode = planCode.lowercase(),
                            paymentMethod = method.id,
                            billingInterval = interval.lowercase(),
                            locale = if (isArabic) "ar" else "en",
                            receipt = receipt,
                        )
                        isSubmitting = false
                        if (result.isSuccess) {
                            notificationsViewModel.recordManualPaymentRequest(
                                userId = currentUserId,
                                planName = if (planCode.equals("starter", true)) if (isArabic) "المبتدئ" else "Starter" else "Pro",
                                paymentMethod = method.localized(isArabic),
                                interval = interval,
                            )
                            submitted = true
                        } else {
                            errorMessage = if (isArabic) "تعذر إرسال طلب الدفع الآن. تحقق من الاتصال وحاول مرة أخرى." else "The payment request could not be sent. Check your connection and try again."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (isArabic) "تأكيد الدفع" else "Confirm payment")
            }
        }
    }

    if (submitted) {
        AlertDialog(
            onDismissRequest = onCompleted,
            title = { Text(if (isArabic) "تم رفع الطلب" else "Request submitted") },
            text = { Text(if (isArabic) "تم رفع إثبات الدفع. تستغرق المراجعة والتفعيل عادةً من ساعة إلى ساعتين خلال أوقات الدوام، وسيصلك إشعار التفعيل داخل التطبيق وعبر البريد الإلكتروني." else "Your payment proof was submitted. Verification and activation usually take one to two hours during business hours, and you will be notified in the app and by email.") },
            confirmButton = { Button(onClick = onCompleted) { Text(if (isArabic) "عرض الإشعارات" else "View notifications") } },
        )
    }
}

@Composable
private fun PlanPriceCard(planCode: String, price: String, interval: String, isArabic: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(if (isArabic) "الخطة المختارة" else "Selected plan", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
            Text(if (planCode.equals(BillingCatalog.PLAN_STARTER, true)) if (isArabic) "خطة المبتدئ" else "Starter plan" else if (isArabic) "خطة برو" else "Pro plan", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(
                if (interval.equals(BillingCatalog.INTERVAL_YEARLY, true)) {
                    if (isArabic) "اشتراك سنوي - خصم 20%" else "Yearly subscription - 20% off"
                } else {
                    if (isArabic) "اشتراك شهري" else "Monthly subscription"
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(price, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit,
    footer: @Composable () -> Unit,
    enabled: Boolean = true,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(58.dp)) {
                Image(
                    painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Box(Modifier.padding(top = 4.dp)) { footer() }
            }
        }
    }
}

@Composable
private fun CopyableValue(label: String, value: String, isArabic: Boolean) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = {
            context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText(label, value))
            Toast.makeText(context, if (isArabic) "تم النسخ" else "Copied", Toast.LENGTH_SHORT).show()
        }) { Icon(Icons.Filled.ContentCopy, contentDescription = if (isArabic) "نسخ" else "Copy") }
    }
}

internal fun planPrice(planCode: String, interval: String, isArabic: Boolean): String {
    val monthly = if (planCode.equals(BillingCatalog.PLAN_STARTER, true)) {
        Triple(2_900.0, 19.0, 4.99)
    } else {
        Triple(4_900.0, 35.0, 9.99)
    }
    val factor = if (interval.equals(BillingCatalog.INTERVAL_YEARLY, true)) 9.6 else 1.0
    val (yer, sar, usd) = Triple(monthly.first * factor, monthly.second * factor, monthly.third * factor)
    val yerValue = String.format(Locale.US, "%,.0f", yer)
    val sarValue = String.format(Locale.US, "%,.2f", sar).removeSuffix(".00")
    val usdValue = String.format(Locale.US, "%.2f", usd)
    return if (isArabic) {
        "$yerValue ريال يمني - $sarValue ريال سعودي - $usdValue دولار أمريكي"
    } else {
        "YER $yerValue - SAR $sarValue - USD $usdValue"
    }
}

@Composable
private fun PaymentRegionTab(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Text(label, color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

private tailrec fun Context.findActivityForPayments(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivityForPayments()
    else -> null
}
