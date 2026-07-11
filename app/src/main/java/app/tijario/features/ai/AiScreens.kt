package app.tijario.features.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tijario.config.AppLanguage
import app.tijario.config.LocalLanguage
import app.tijario.config.Supabase
import app.tijario.config.t
import app.tijario.data.model.Customer
import app.tijario.data.model.Product
import app.tijario.data.remote.AiV3CaptionRequest
import app.tijario.data.remote.AiV3ReplyRequest
import app.tijario.data.remote.AiV3ResponseData
import app.tijario.data.remote.AiV3Variant
import app.tijario.ui.state.TijarioDataViewModel
import java.util.UUID

// Dynamic SaaS Color Scheme for Light and Dark mode harmony
data class SaaSColorScheme(
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val border: Color,
    val primaryTeal: Color,
    val primaryPressed: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color,
    val error: Color,
    val isDark: Boolean
)

@Composable
fun getSaaSColors(): SaaSColorScheme {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    return if (isDark) {
        SaaSColorScheme(
            background = Color(0xFF0F1115),
            surface = Color(0xFF181B20),
            elevatedSurface = Color(0xFF20242A),
            border = Color(0xFF2D323A),
            primaryTeal = Color(0xFF14B8A6),
            primaryPressed = Color(0xFF0F9488),
            textPrimary = Color(0xFFF5F7FA),
            textSecondary = Color(0xFFA6ADB7),
            success = Color(0xFF22C55E),
            error = Color(0xFFEF4444),
            isDark = true
        )
    } else {
        SaaSColorScheme(
            background = Color(0xFFF9FAFB),
            surface = Color(0xFFFFFFFF),
            elevatedSurface = Color(0xFFF3F4F6),
            border = Color(0xFFE5E7EB),
            primaryTeal = Color(0xFF0D9488),
            primaryPressed = Color(0xFF0F766E),
            textPrimary = Color(0xFF111827),
            textSecondary = Color(0xFF4B5563),
            success = Color(0xFF16A34A),
            error = Color(0xFFDC2626),
            isDark = false
        )
    }
}

// Localized strings helper for variant labels
private fun getLocalizedVariantLabel(id: String, language: AppLanguage): String {
    return when (id) {
        "quick" -> if (language == AppLanguage.AR) "مختصر" else "Quick"
        "professional" -> if (language == AppLanguage.AR) "احترافي" else "Professional"
        "conversion" -> if (language == AppLanguage.AR) "تحويلي" else "Conversion"
        "compact" -> if (language == AppLanguage.AR) "مختصر" else "Compact"
        "direct_sales" -> if (language == AppLanguage.AR) "بيعي مباشر" else "Direct Sales"
        "story" -> if (language == AppLanguage.AR) "ستوري" else "Story"
        else -> id.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

private fun translateAiTerm(term: String, language: AppLanguage): String {
    val clean = term.trim().lowercase()
    if (clean == "unknown") return if (language == AppLanguage.AR) "غير معروف" else "Unknown"
    
    return when (clean) {
        // Intents
        "price_request" -> if (language == AppLanguage.AR) "طلب السعر" else "Price request"
        "price_objection" -> if (language == AppLanguage.AR) "اعتراض على السعر" else "Price objection"
        "discount_request" -> if (language == AppLanguage.AR) "طلب خصم" else "Discount request"
        "delivery_question" -> if (language == AppLanguage.AR) "سؤال عن التوصيل" else "Delivery question"
        "availability_question" -> if (language == AppLanguage.AR) "سؤال عن التوفر" else "Availability question"
        "customer_hesitant" -> if (language == AppLanguage.AR) "عميل متردد" else "Hesitant customer"
        "customer_interested" -> if (language == AppLanguage.AR) "عميل مهتم" else "Interested customer"
        "follow_up" -> if (language == AppLanguage.AR) "متابعة" else "Follow up"
        "payment_reminder" -> if (language == AppLanguage.AR) "تذكير بالدفع" else "Payment reminder"
        "delayed_response_apology" -> if (language == AppLanguage.AR) "اعتذار عن التأخير" else "Delay apology"
        "review_request" -> if (language == AppLanguage.AR) "طلب تقييم" else "Review request"
        "general_inquiry" -> if (language == AppLanguage.AR) "استفسار عام" else "General inquiry"
        "product_promotion" -> if (language == AppLanguage.AR) "ترويج للمنتج" else "Product promotion"
        "product_highlighting" -> if (language == AppLanguage.AR) "إبراز المنتج" else "Product highlighting"
        
        // Moods
        "neutral" -> if (language == AppLanguage.AR) "محايد" else "Neutral"
        "friendly" -> if (language == AppLanguage.AR) "ودي" else "Friendly"
        "happy" -> if (language == AppLanguage.AR) "سعيد" else "Happy"
        "angry" -> if (language == AppLanguage.AR) "غاضب" else "Angry"
        "hesitant" -> if (language == AppLanguage.AR) "متردد" else "Hesitant"
        "interested" -> if (language == AppLanguage.AR) "مهتم" else "Interested"
        
        // Buying Stages
        "awareness" -> if (language == AppLanguage.AR) "مرحلة الوعي" else "Awareness stage"
        "consideration" -> if (language == AppLanguage.AR) "مرحلة الاهتمام" else "Consideration stage"
        "decision" -> if (language == AppLanguage.AR) "مرحلة القرار" else "Decision stage"
        "retention" -> if (language == AppLanguage.AR) "مرحلة الولاء" else "Retention stage"
        
        // Missing Information fields
        "primary_benefit" -> if (language == AppLanguage.AR) "الميزة الأساسية" else "Primary benefit"
        "offer" -> if (language == AppLanguage.AR) "العرض" else "Offer"
        "price" -> if (language == AppLanguage.AR) "السعر" else "Price"
        "delivery_details" -> if (language == AppLanguage.AR) "تفاصيل التوصيل" else "Delivery details"
        "customer_name" -> if (language == AppLanguage.AR) "اسم العميل" else "Customer name"
        "product_details" -> if (language == AppLanguage.AR) "تفاصيل المنتج" else "Product details"
        
        else -> term.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiToolsScreen(
    dataViewModel: TijarioDataViewModel,
    hideHeader: Boolean = false,
) {
    val language = LocalLanguage.current
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val aiViewModel: AiViewModel = viewModel(
        factory = AiViewModelFactory(remember { AiRepositoryV3(Supabase.apiClient) }),
    )
    val state by aiViewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var localError by remember { mutableStateOf<String?>(null) }
    
    // Reply Form State
    var replyMessage by remember { mutableStateOf("") }
    var replyQuickCase by remember { mutableStateOf<String?>(null) }
    var replyCustomerId by remember { mutableStateOf<String?>(null) }
    var replyProductId by remember { mutableStateOf<String?>(null) }
    var replyDialect by remember { mutableStateOf("auto") }
    var replyTone by remember { mutableStateOf("auto") }
    var replyLength by remember { mutableStateOf("short") }
    var replyGoal by remember { mutableStateOf("auto") }
    var replyExtra by remember { mutableStateOf("") }
    var showReplyAdvanced by remember { mutableStateOf(false) }

    // Caption Form State
    var captionProductId by remember { mutableStateOf<String?>(null) }
    var productOrService by remember { mutableStateOf("") }
    var primaryBenefit by remember { mutableStateOf("") }
    var offer by remember { mutableStateOf("") }
    var captionType by remember { mutableStateOf("product_post") }
    var platform by remember { mutableStateOf("instagram") }
    var captionDialect by remember { mutableStateOf("auto") }
    var captionStyle by remember { mutableStateOf("sales") }
    var captionLength by remember { mutableStateOf("short") }
    var showCaptionAdvanced by remember { mutableStateOf(false) }

    // Modal Sheet Control
    var showCustomerSheet by remember { mutableStateOf(false) }
    var showProductSheetForReply by remember { mutableStateOf(false) }
    var showProductSheetForCaption by remember { mutableStateOf(false) }

    val saaSColors = getSaaSColors()

    LaunchedEffect(state) {
        if (state is AiV3ScreenState.Success) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val isBusy = state is AiV3ScreenState.Loading ||
        state is AiV3ScreenState.Refining ||
        state is AiV3ScreenState.Reporting
    val limitReachedByCache = uiState.planUsage?.let { it.aiLimit > 0 && it.aiUsed >= it.aiLimit } == true

    Scaffold(
        containerColor = saaSColors.background,
        contentColor = saaSColors.textPrimary,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!hideHeader) {
                HeaderBlock()
            }

            UsageBanner()

            if (limitReachedByCache || state is AiV3ScreenState.LimitReached) {
                LimitBanner((state as? AiV3ScreenState.LimitReached)?.message)
            }

            // Tab Segmented Control
            SegmentedControl(
                options = listOf(t("tab_ai_reply"), t("tab_ai_caption")),
                selectedIndex = selectedTab,
                onOptionSelected = {
                    selectedTab = it
                    localError = null
                }
            )

            if (selectedTab == 0) {
                // Reply Form Block
                ReplyFormBlock(
                    replyMessage = replyMessage,
                    onReplyMessageChange = {
                        replyMessage = it.take(2000)
                        aiViewModel.markEditing()
                    },
                    replyQuickCase = replyQuickCase,
                    onQuickCaseChange = { replyQuickCase = it },
                    selectedCustomerId = replyCustomerId,
                    customers = uiState.customers,
                    onCustomerClick = { showCustomerSheet = true },
                    onCustomerClear = { replyCustomerId = null },
                    selectedProductId = replyProductId,
                    products = uiState.products,
                    onProductClick = { showProductSheetForReply = true },
                    onProductClear = { replyProductId = null },
                    dialect = replyDialect,
                    onDialectChange = { replyDialect = it },
                    tone = replyTone,
                    onToneChange = { replyTone = it },
                    length = replyLength,
                    onLengthChange = { replyLength = it },
                    goal = replyGoal,
                    onGoalChange = { replyGoal = it },
                    extra = replyExtra,
                    onExtraChange = { replyExtra = it.take(600) },
                    showAdvanced = showReplyAdvanced,
                    onToggleAdvanced = { showReplyAdvanced = !showReplyAdvanced },
                    enabled = !isBusy && !limitReachedByCache,
                    onSubmit = {
                        if (replyMessage.isBlank() && replyQuickCase == null) {
                            localError = localized(language, "أدخل رسالة العميل أو اختر حالة سريعة.", "Paste a customer message or choose a quick case.")
                            return@ReplyFormBlock
                        }
                        localError = null
                        aiViewModel.generateReply(
                            AiV3ReplyRequest(
                                clientRequestId = UUID.randomUUID().toString(),
                                customerMessage = replyMessage.ifBlank { null },
                                quickCase = replyQuickCase,
                                customerId = replyCustomerId,
                                productId = replyProductId,
                                goal = replyGoal,
                                dialect = replyDialect,
                                tone = replyTone,
                                length = replyLength,
                                extraContext = replyExtra.ifBlank { null },
                                language = language.code,
                            ),
                            onSuccess = { dataViewModel.refreshPlanUsage() },
                        )
                    },
                    language = language
                )
            } else {
                // Caption Form Block
                CaptionFormBlock(
                    products = uiState.products,
                    selectedProductId = captionProductId,
                    onProductClick = { showProductSheetForCaption = true },
                    onProductClear = {
                        captionProductId = null
                        productOrService = ""
                    },
                    productOrService = productOrService,
                    onProductOrServiceChange = {
                        captionProductId = null
                        productOrService = it.take(160)
                        aiViewModel.markEditing()
                    },
                    primaryBenefit = primaryBenefit,
                    onPrimaryBenefitChange = { primaryBenefit = it.take(300) },
                    offer = offer,
                    onOfferChange = { offer = it.take(180) },
                    captionType = captionType,
                    onCaptionTypeChange = { captionType = it },
                    platform = platform,
                    onPlatformChange = { platform = it },
                    dialect = captionDialect,
                    onDialectChange = { captionDialect = it },
                    style = captionStyle,
                    onStyleChange = { captionStyle = it },
                    length = captionLength,
                    onLengthChange = { captionLength = it },
                    showAdvanced = showCaptionAdvanced,
                    onToggleAdvanced = { showCaptionAdvanced = !showCaptionAdvanced },
                    enabled = !isBusy && !limitReachedByCache,
                    onSubmit = {
                        if (captionProductId == null && productOrService.isBlank()) {
                            localError = localized(language, "اختر منتجًا محفوظًا أو اكتب اسم المنتج/الخدمة.", "Choose a saved product or type the product/service name.")
                            return@CaptionFormBlock
                        }
                        localError = null
                        aiViewModel.generateCaption(
                            AiV3CaptionRequest(
                                clientRequestId = UUID.randomUUID().toString(),
                                productId = captionProductId,
                                productOrService = if (captionProductId == null) productOrService.ifBlank { null } else null,
                                primaryBenefit = primaryBenefit.ifBlank { null },
                                offer = offer.ifBlank { null },
                                platform = platform,
                                captionType = captionType,
                                dialect = captionDialect,
                                length = captionLength,
                                style = captionStyle,
                                language = language.code,
                            ),
                            onSuccess = { dataViewModel.refreshPlanUsage() },
                        )
                    },
                    language = language
                )
            }

            localError?.let { ErrorCard(it) }

            // Dynamic Result and States Area
            when (val current = state) {
                AiV3ScreenState.Idle, AiV3ScreenState.Editing -> Unit
                AiV3ScreenState.Loading -> InfoCard(localized(language, "جاري توليد النتيجة...", "Generating..."))
                is AiV3ScreenState.Error -> ErrorCard(current.message)
                is AiV3ScreenState.Offline -> ErrorCard(current.message)
                is AiV3ScreenState.LimitReached -> Unit
                is AiV3ScreenState.Refining -> {
                    InfoCard(localized(language, "جاري تحسين النص...", "Refining..."))
                    ResultBlock(current.previous, aiViewModel, language, isBusy = true) { dataViewModel.refreshPlanUsage() }
                }
                is AiV3ScreenState.Reporting -> {
                    InfoCard(localized(language, "جاري إرسال البلاغ...", "Reporting..."))
                    ResultBlock(current.previous, aiViewModel, language, isBusy = true) { dataViewModel.refreshPlanUsage() }
                }
                is AiV3ScreenState.Success -> {
                    ResultBlock(current, aiViewModel, language, isBusy = false) { dataViewModel.refreshPlanUsage() }
                }
            }
        }

        // Modal Sheets Declarations
        if (showCustomerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCustomerSheet = false },
                containerColor = saaSColors.surface,
                contentColor = saaSColors.textPrimary,
            ) {
                CustomerSelectionContent(
                    customers = uiState.customers,
                    onCustomerSelected = { id ->
                        replyCustomerId = id
                        showCustomerSheet = false
                    },
                    language = language
                )
            }
        }

        if (showProductSheetForReply) {
            ModalBottomSheet(
                onDismissRequest = { showProductSheetForReply = false },
                containerColor = saaSColors.surface,
                contentColor = saaSColors.textPrimary,
            ) {
                ProductSelectionContent(
                    products = uiState.products,
                    onProductSelected = { product ->
                        replyProductId = product?.id
                        showProductSheetForReply = false
                    },
                    language = language
                )
            }
        }

        if (showProductSheetForCaption) {
            ModalBottomSheet(
                onDismissRequest = { showProductSheetForCaption = false },
                containerColor = saaSColors.surface,
                contentColor = saaSColors.textPrimary,
            ) {
                ProductSelectionContent(
                    products = uiState.products,
                    onProductSelected = { product ->
                        captionProductId = product?.id
                        productOrService = product?.name ?: ""
                        showProductSheetForCaption = false
                    },
                    language = language
                )
            }
        }
    }
}

@Composable
private fun HeaderBlock() {
    val saaSColors = getSaaSColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = saaSColors.primaryTeal,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = t("ai_title"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = saaSColors.textPrimary
            )
            Text(
                text = t("ai_subtitle"),
                style = MaterialTheme.typography.bodySmall,
                color = saaSColors.textSecondary
            )
        }
    }
}

@Composable
private fun UsageBanner() {
    val saaSColors = getSaaSColors()
    val language = LocalLanguage.current
    Text(
        text = localized(language, "AI يعمل عند الاتصال بالإنترنت فقط، وبقية التطبيق تستمر Offline.", "AI is online-only; the rest of the app can continue offline."),
        color = saaSColors.textSecondary,
        fontSize = 11.sp,
    )
}

@Composable
private fun LimitBanner(message: String?) {
    val saaSColors = getSaaSColors()
    Card(
        colors = CardDefaults.cardColors(containerColor = saaSColors.error.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message ?: t("ai_limit_reached"),
            modifier = Modifier.padding(12.dp),
            color = saaSColors.error,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
) {
    val saaSColors = getSaaSColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(saaSColors.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val active = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) saaSColors.primaryTeal else Color.Transparent)
                    .clickable { onOptionSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (active) Color.White else saaSColors.textSecondary, // Always light text inside the active teal tab for contrast
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReplyFormBlock(
    replyMessage: String,
    onReplyMessageChange: (String) -> Unit,
    replyQuickCase: String?,
    onQuickCaseChange: (String?) -> Unit,
    selectedCustomerId: String?,
    customers: List<Customer>,
    onCustomerClick: () -> Unit,
    onCustomerClear: () -> Unit,
    selectedProductId: String?,
    products: List<Product>,
    onProductClick: () -> Unit,
    onProductClear: () -> Unit,
    dialect: String,
    onDialectChange: (String) -> Unit,
    tone: String,
    onToneChange: (String) -> Unit,
    length: String,
    onLengthChange: (String) -> Unit,
    goal: String,
    onGoalChange: (String) -> Unit,
    extra: String,
    onExtraChange: (String) -> Unit,
    showAdvanced: Boolean,
    onToggleAdvanced: () -> Unit,
    enabled: Boolean,
    onSubmit: () -> Unit,
    language: AppLanguage
) {
    val saaSColors = getSaaSColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = saaSColors.surface),
        border = if (!saaSColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, saaSColors.border) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localized(language, "صياغة رد ذكي للعملاء", "Compose Smart Reply"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = saaSColors.textPrimary
            )

            // Message Input
            OutlinedTextField(
                value = replyMessage,
                onValueChange = onReplyMessageChange,
                placeholder = { Text(localized(language, "الصق رسالة العميل هنا...", "Paste the customer message here..."), color = saaSColors.textSecondary) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = saaSColors.primaryTeal,
                    unfocusedBorderColor = saaSColors.border,
                    focusedContainerColor = saaSColors.background,
                    unfocusedContainerColor = saaSColors.background,
                    focusedTextColor = saaSColors.textPrimary,
                    unfocusedTextColor = saaSColors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Cases Slider
            ChipGroupSlider(
                title = localized(language, "حالات سريعة اختيارية", "Optional quick cases"),
                options = replyCaseOptions(language),
                selected = replyQuickCase,
                onSelected = { onQuickCaseChange(if (replyQuickCase == it) null else it) }
            )

            // Context Pickers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customer Button
                ContextSelectorButton(
                    label = customers.find { it.id == selectedCustomerId }?.name 
                        ?: localized(language, "اختر عميلاً", "Select Customer"),
                    isSelected = selectedCustomerId != null,
                    onClick = onCustomerClick,
                    onClear = onCustomerClear,
                    modifier = Modifier.weight(1f),
                    language = language
                )

                // Product Button
                ContextSelectorButton(
                    label = products.find { it.id == selectedProductId }?.name 
                        ?: localized(language, "اختر منتجاً", "Select Product"),
                    isSelected = selectedProductId != null,
                    onClick = onProductClick,
                    onClear = onProductClear,
                    modifier = Modifier.weight(1f),
                    language = language
                )
            }

            // Advanced settings toggle
            TextButton(
                onClick = onToggleAdvanced,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (showAdvanced) localized(language, "إخفاء الإعدادات المتقدمة", "Hide advanced settings") else localized(language, "إعدادات متقدمة", "Advanced settings"),
                    color = saaSColors.primaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = saaSColors.primaryTeal
                )
            }

            if (showAdvanced) {
                ChipGroupSlider(localized(language, "اللهجة", "Dialect"), dialectOptions(language), dialect, onDialectChange)
                ChipGroupSlider(localized(language, "الطول", "Length"), lengthOptions(language), length, onLengthChange)
                ChipGroupSlider(localized(language, "الهدف", "Goal"), goalOptions(language), goal, onGoalChange)
                
                OutlinedTextField(
                    value = extra,
                    onValueChange = onExtraChange,
                    placeholder = { Text(t("ai_notes_label"), color = saaSColors.textSecondary) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = saaSColors.primaryTeal,
                        unfocusedBorderColor = saaSColors.border,
                        focusedContainerColor = saaSColors.background,
                        unfocusedContainerColor = saaSColors.background,
                        focusedTextColor = saaSColors.textPrimary,
                        unfocusedTextColor = saaSColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Button(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = saaSColors.primaryTeal,
                    contentColor = Color.White, // Keep text light inside button
                    disabledContainerColor = saaSColors.border,
                    disabledContentColor = saaSColors.textSecondary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("btn_generate_reply"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CaptionFormBlock(
    products: List<Product>,
    selectedProductId: String?,
    onProductClick: () -> Unit,
    onProductClear: () -> Unit,
    productOrService: String,
    onProductOrServiceChange: (String) -> Unit,
    primaryBenefit: String,
    onPrimaryBenefitChange: (String) -> Unit,
    offer: String,
    onOfferChange: (String) -> Unit,
    captionType: String,
    onCaptionTypeChange: (String) -> Unit,
    platform: String,
    onPlatformChange: (String) -> Unit,
    dialect: String,
    onDialectChange: (String) -> Unit,
    style: String,
    onStyleChange: (String) -> Unit,
    length: String,
    onLengthChange: (String) -> Unit,
    showAdvanced: Boolean,
    onToggleAdvanced: () -> Unit,
    enabled: Boolean,
    onSubmit: () -> Unit,
    language: AppLanguage
) {
    val saaSColors = getSaaSColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = saaSColors.surface),
        border = if (!saaSColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, saaSColors.border) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localized(language, "صياغة كابشن ذكي للمنتجات", "Generate Product Caption"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = saaSColors.textPrimary
            )

            // Product selector
            ContextSelectorButton(
                label = products.find { it.id == selectedProductId }?.name 
                    ?: localized(language, "اختر منتجاً محفوظاً", "Select Saved Product"),
                isSelected = selectedProductId != null,
                onClick = onProductClick,
                onClear = onProductClear,
                modifier = Modifier.fillMaxWidth(),
                language = language
            )

            if (selectedProductId == null) {
                OutlinedTextField(
                    value = productOrService,
                    onValueChange = onProductOrServiceChange,
                    placeholder = { Text(t("ai_prod_srv_label"), color = saaSColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = saaSColors.primaryTeal,
                        unfocusedBorderColor = saaSColors.border,
                        focusedContainerColor = saaSColors.background,
                        unfocusedContainerColor = saaSColors.background,
                        focusedTextColor = saaSColors.textPrimary,
                        unfocusedTextColor = saaSColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = primaryBenefit,
                onValueChange = onPrimaryBenefitChange,
                placeholder = { Text(localized(language, "ما أهم فائدة تريد إبرازها؟ (اختياري)", "Primary benefit (optional)"), color = saaSColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = saaSColors.primaryTeal,
                    unfocusedBorderColor = saaSColors.border,
                    focusedContainerColor = saaSColors.background,
                    unfocusedContainerColor = saaSColors.background,
                    focusedTextColor = saaSColors.textPrimary,
                    unfocusedTextColor = saaSColors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = offer,
                onValueChange = onOfferChange,
                placeholder = { Text(t("ai_offer_label"), color = saaSColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = saaSColors.primaryTeal,
                    unfocusedBorderColor = saaSColors.border,
                    focusedContainerColor = saaSColors.background,
                    unfocusedContainerColor = saaSColors.background,
                    focusedTextColor = saaSColors.textPrimary,
                    unfocusedTextColor = saaSColors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Platform Switcher (Teal Chips)
            ChipGroupSlider(
                title = localized(language, "المنصة", "Platform"),
                options = platformOptions(language),
                selected = platform,
                onSelected = onPlatformChange
            )

            // Advanced settings toggle
            TextButton(
                onClick = onToggleAdvanced,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (showAdvanced) localized(language, "إخفاء الإعدادات المتقدمة", "Hide advanced settings") else localized(language, "إعدادات متقدمة", "Advanced settings"),
                    color = saaSColors.primaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = saaSColors.primaryTeal
                )
            }

            if (showAdvanced) {
                ChipGroupSlider(localized(language, "نوع الكابشن", "Caption type"), captionTypeOptions(language), captionType, onCaptionTypeChange)
                ChipGroupSlider(localized(language, "اللهجة", "Dialect"), dialectOptions(language), dialect, onDialectChange)
                ChipGroupSlider(localized(language, "الأسلوب", "Style"), captionStyleOptions(language), style, onStyleChange)
                ChipGroupSlider(localized(language, "الطول", "Length"), lengthOptions(language), length, onLengthChange)
            }

            Button(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = saaSColors.primaryTeal,
                    contentColor = Color.White, // Keep text light inside button
                    disabledContainerColor = saaSColors.border,
                    disabledContentColor = saaSColors.textSecondary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("ai_btn_gen_caption"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ContextSelectorButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    language: AppLanguage
) {
    val saaSColors = getSaaSColors()
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) saaSColors.primaryTeal.copy(alpha = 0.15f) else saaSColors.background)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (isSelected) saaSColors.primaryTeal else saaSColors.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = localized(language, "إلغاء التحديد", "Deselect"),
                tint = saaSColors.primaryTeal,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onClear() }
            )
        }
    }
}

@Composable
private fun ResultBlock(
    success: AiV3ScreenState.Success,
    aiViewModel: AiViewModel,
    language: AppLanguage,
    isBusy: Boolean,
    onUsageChanged: () -> Unit,
) {
    var selectedVariantId by remember(success.data.generationId) {
        mutableStateOf(success.data.variants.firstOrNull()?.id ?: "")
    }
    
    success.notice?.let { InfoCard(it) }

    // Human-friendly collapsible analysis summary
    AnalysisSummaryCollapsible(success.data, language)

    // Variant selector segmented switch
    if (success.data.variants.isNotEmpty()) {
        SegmentedControl(
            options = success.data.variants.map { getLocalizedVariantLabel(it.id, language) },
            selectedIndex = success.data.variants.indexOfFirst { it.id == selectedVariantId }.coerceAtLeast(0),
            onOptionSelected = { index ->
                selectedVariantId = success.data.variants[index].id
            }
        )

        val activeVariant = success.data.variants.find { it.id == selectedVariantId }
        if (activeVariant != null) {
            SingleActiveResultCard(
                generation = success,
                variant = activeVariant,
                aiViewModel = aiViewModel,
                language = language,
                isBusy = isBusy,
                onUsageChanged = onUsageChanged
            )
        }
    }
}

@Composable
private fun AnalysisSummaryCollapsible(data: AiV3ResponseData, language: AppLanguage) {
    var expanded by remember { mutableStateOf(false) }
    val saaSColors = getSaaSColors()
    Card(
        colors = CardDefaults.cardColors(containerColor = saaSColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = if (!saaSColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, saaSColors.border) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = localized(language, "تحليل وفهم الرسالة", "Message understanding"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = saaSColors.textPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = saaSColors.textSecondary
                )
            }

            val localizedObjection = data.analysis.objection?.let { translateAiTerm(it, language) }
            val localizedStage = translateAiTerm(data.analysis.buyingStage, language)
            val briefMeaning = if (localizedObjection != null) {
                localized(language, "العميل مهتم ولكنه يواجه اعتراضًا: $localizedObjection", "Customer is interested but has an objection: $localizedObjection")
            } else {
                localized(language, "العميل يبدو في وضع: $localizedStage", "Customer seems to be in: $localizedStage")
            }

            Text(
                text = briefMeaning,
                fontSize = 12.sp,
                color = saaSColors.textSecondary
            )

            if (expanded) {
                val localizedIntent = translateAiTerm(data.analysis.intent, language)
                val localizedMood = translateAiTerm(data.analysis.mood, language)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(localized(language, "نية: $localizedIntent", "Intent: $localizedIntent"), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = saaSColors.background,
                            labelColor = saaSColors.textSecondary
                        ),
                        border = null
                    )
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(localized(language, "مزاج: $localizedMood", "Mood: $localizedMood"), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = saaSColors.background,
                            labelColor = saaSColors.textSecondary
                        ),
                        border = null
                    )
                }

                if (data.missingInformation.isNotEmpty()) {
                    val localizedMissing = data.missingInformation.map { translateAiTerm(it, language) }.joinToString("، ")
                    Text(
                        text = localized(language, "معلومات ناقصة ننصح بسؤال العميل عنها: ", "Recommended missing info to ask: ") + 
                            localizedMissing,
                        fontSize = 11.sp,
                        color = saaSColors.error
                    )
                }

                Text(
                    text = localized(language, "متبقي من باقتك: ", "Remaining credits: ") + 
                        "${data.usage.limit - data.usage.used} / ${data.usage.limit}",
                    fontSize = 11.sp,
                    color = saaSColors.textSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleActiveResultCard(
    generation: AiV3ScreenState.Success,
    variant: AiV3Variant,
    aiViewModel: AiViewModel,
    language: AppLanguage,
    isBusy: Boolean,
    onUsageChanged: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showImproveSheet by remember { mutableStateOf(false) }
    val copyText = t("copy")
    val saaSColors = getSaaSColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = saaSColors.surface),
        border = if (!saaSColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, saaSColors.border) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = variant.text,
                lineHeight = 22.sp,
                fontSize = 14.sp,
                color = saaSColors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy button
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(variant.text))
                        Toast.makeText(context, copyText, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = saaSColors.primaryTeal
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, saaSColors.primaryTeal)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(copyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Improve (Refine) button
                Button(
                    onClick = { showImproveSheet = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = saaSColors.primaryTeal,
                        contentColor = Color.White // Always white text
                    )
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(localized(language, "تحسين", "Improve"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Bottom Sheet for text refinement and reporting
    if (showImproveSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImproveSheet = false },
            containerColor = saaSColors.surface,
            contentColor = saaSColors.textPrimary,
        ) {
            RefineOptionsContent(
                onRefineSelected = { preset ->
                    showImproveSheet = false
                    aiViewModel.refine(
                        previous = generation,
                        variantId = variant.id,
                        preset = preset,
                        dialect = null,
                        language = language.code,
                        onSuccess = onUsageChanged
                    )
                },
                onReportSelected = {
                    showImproveSheet = false
                    aiViewModel.report(
                        previous = generation,
                        variantId = variant.id,
                        issueType = "wrong_answer",
                        note = null,
                        onDone = {}
                    )
                },
                language = language
            )
        }
    }
}

@Composable
private fun RefineOptionsContent(
    onRefineSelected: (String) -> Unit,
    onReportSelected: () -> Unit,
    language: AppLanguage
) {
    val saaSColors = getSaaSColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = localized(language, "تحسين النص الناتج", "Refining output text"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = saaSColors.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val options = refineOptions(language)
        options.forEach { (preset, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onRefineSelected(preset) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = saaSColors.primaryTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, color = saaSColors.textPrimary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Report option at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onReportSelected() }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Flag, contentDescription = null, tint = saaSColors.error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = t("ai_report"),
                color = saaSColors.error,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CustomerSelectionContent(
    customers: List<Customer>,
    onCustomerSelected: (String) -> Unit,
    language: AppLanguage
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, customers) {
        customers.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val saaSColors = getSaaSColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = localized(language, "اختر عميلاً للرد", "Select customer context"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = saaSColors.textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(localized(language, "بحث باسم العميل...", "Search customer name..."), color = saaSColors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = saaSColors.primaryTeal,
                unfocusedBorderColor = saaSColors.border,
                focusedContainerColor = saaSColors.background,
                unfocusedContainerColor = saaSColors.background,
                focusedTextColor = saaSColors.textPrimary,
                unfocusedTextColor = saaSColors.textPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                text = t("no_search_results"),
                color = saaSColors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(260.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filtered) { customer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { customer.id?.let { onCustomerSelected(it) } }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Text(customer.name, color = saaSColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductSelectionContent(
    products: List<Product>,
    onProductSelected: (Product?) -> Unit,
    language: AppLanguage
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, products) {
        products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val saaSColors = getSaaSColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = localized(language, "اختر منتجاً لسياق البيانات", "Select product context"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = saaSColors.textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(localized(language, "بحث باسم المنتج...", "Search product name..."), color = saaSColors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = saaSColors.primaryTeal,
                unfocusedBorderColor = saaSColors.border,
                focusedContainerColor = saaSColors.background,
                unfocusedContainerColor = saaSColors.background,
                focusedTextColor = saaSColors.textPrimary,
                unfocusedTextColor = saaSColors.textPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                text = t("no_products_available"),
                color = saaSColors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(260.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filtered) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onProductSelected(product) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, color = saaSColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${product.price} ${product.currency}", color = saaSColors.primaryTeal, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipGroupSlider(
    title: String,
    options: List<Pair<String, String>>,
    selected: String?,
    onSelected: (String) -> Unit,
) {
    val saaSColors = getSaaSColors()
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = saaSColors.textSecondary,
        modifier = Modifier.padding(top = 4.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (value, label) ->
            val active = selected == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) saaSColors.primaryTeal else saaSColors.background)
                    .clickable { onSelected(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = if (active) Color.White else saaSColors.textSecondary, // Light text inside selected tab, secondary inside unselected
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    val saaSColors = getSaaSColors()
    Card(
        colors = CardDefaults.cardColors(containerColor = saaSColors.error.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = saaSColors.error,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InfoCard(message: String) {
    val saaSColors = getSaaSColors()
    Card(
        colors = CardDefaults.cardColors(containerColor = saaSColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = if (!saaSColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, saaSColors.border) else null
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = saaSColors.textPrimary,
            fontSize = 12.sp
        )
    }
}

private val AppLanguage.code: String
    get() = if (this == AppLanguage.AR) "ar" else "en"

private fun localized(language: AppLanguage, ar: String, en: String): String =
    if (language == AppLanguage.AR) ar else en

private fun replyCaseOptions(language: AppLanguage) = listOf(
    "price_request" to localized(language, "طلب سعر", "Price request"),
    "price_objection" to localized(language, "اعتراض على السعر", "Price objection"),
    "discount_request" to localized(language, "طلب خصم", "Discount request"),
    "delivery_question" to localized(language, "سؤال عن التوصيل", "Delivery question"),
    "availability_question" to localized(language, "سؤال عن التوفر", "Availability question"),
    "customer_hesitant" to localized(language, "عميل متردد", "Hesitant customer"),
    "customer_interested" to localized(language, "عميل مهتم", "Interested customer"),
    "follow_up" to localized(language, "متابعة", "Follow up"),
    "payment_reminder" to localized(language, "تذكير دفع", "Payment reminder"),
    "delayed_response_apology" to localized(language, "اعتذار عن تأخير", "Delay apology"),
    "review_request" to localized(language, "طلب تقييم", "Review request"),
    "general_inquiry" to localized(language, "استفسار عام", "General inquiry"),
)

private fun dialectOptions(language: AppLanguage) = listOf(
    "auto" to localized(language, "تلقائي", "Auto"),
    "fusha_simple" to localized(language, "فصحى بسيطة", "Simple MSA"),
    "gulf" to localized(language, "خليجي", "Gulf"),
    "yemeni" to localized(language, "يمني", "Yemeni"),
)

private fun replyToneOptions(language: AppLanguage) = listOf(
    "auto" to localized(language, "تلقائي", "Auto"),
    "friendly" to localized(language, "ودي", "Friendly"),
    "formal" to localized(language, "رسمي", "Formal"),
    "sales" to localized(language, "بيعي", "Sales"),
)

private fun captionStyleOptions(language: AppLanguage) = listOf(
    "auto" to localized(language, "تلقائي", "Auto"),
    "friendly" to localized(language, "ودي", "Friendly"),
    "sales" to localized(language, "بيعي", "Sales"),
    "premium" to localized(language, "فاخر", "Premium"),
)

private fun lengthOptions(language: AppLanguage) = listOf(
    "auto" to localized(language, "تلقائي", "Auto"),
    "short" to localized(language, "قصير", "Short"),
    "medium" to localized(language, "متوسط", "Medium"),
)

private fun goalOptions(language: AppLanguage) = listOf(
    "auto" to localized(language, "تلقائي", "Auto"),
    "answer" to localized(language, "إجابة", "Answer"),
    "persuade" to localized(language, "إقناع", "Persuade"),
    "close_sale" to localized(language, "إغلاق بيع", "Close sale"),
    "follow_up" to localized(language, "متابعة", "Follow up"),
    "collect_payment" to localized(language, "تحصيل دفع", "Collect payment"),
    "restore_trust" to localized(language, "استعادة ثقة", "Restore trust"),
)

private fun platformOptions(language: AppLanguage) = listOf(
    "instagram" to localized(language, "إنستغرام", "Instagram"),
    "whatsapp" to localized(language, "واتساب", "WhatsApp"),
    "tiktok" to localized(language, "تيك توك", "TikTok"),
)

private fun captionTypeOptions(language: AppLanguage) = listOf(
    "product_post" to localized(language, "منشور منتج", "Product post"),
    "offer_post" to localized(language, "منشور عرض", "Offer post"),
    "new_arrival" to localized(language, "وصل حديثًا", "New arrival"),
    "service_promo" to localized(language, "ترويج خدمة", "Service promo"),
    "story_caption" to localized(language, "ستوري", "Story"),
)

private fun refineOptions(language: AppLanguage) = listOf(
    "shorter" to localized(language, "أقصر", "Shorter"),
    "more_professional" to localized(language, "أكثر احترافية", "More professional"),
    "more_persuasive" to localized(language, "أكثر إقناعًا", "More persuasive"),
    "less_salesy" to localized(language, "أقل بيعية", "Less salesy"),
    "add_cta" to localized(language, "إضافة CTA", "Add CTA"),
    "remove_cta" to localized(language, "إزالة CTA", "Remove CTA"),
    "rewrite" to localized(language, "إعادة الصياغة", "Rewrite"),
)
