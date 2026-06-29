package app.tijario.features.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Close
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

// Premium SaaS Color Palette
private val SaaSBackground = Color(0xFF0F1115)
private val SaaSSurface = Color(0xFF181B20)
private val SaaSElevatedSurface = Color(0xFF20242A)
private val SaaSBorder = Color(0xFF2D323A)
private val SaaSPrimaryTeal = Color(0xFF14B8A6)
private val SaaSPrimaryPressed = Color(0xFF0F9488)
private val SaaSTextPrimary = Color(0xFFF5F7FA)
private val SaaSTextSecondary = Color(0xFFA6ADB7)
private val SaaSSuccess = Color(0xFF22C55E)
private val SaaSError = Color(0xFFEF4444)

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

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

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
        containerColor = SaaSBackground,
        contentColor = SaaSTextPrimary,
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
                containerColor = SaaSSurface,
                contentColor = SaaSTextPrimary,
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
                containerColor = SaaSSurface,
                contentColor = SaaSTextPrimary,
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
                containerColor = SaaSSurface,
                contentColor = SaaSTextPrimary,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = SaaSPrimaryTeal,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = t("ai_title"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaaSTextPrimary
            )
            Text(
                text = t("ai_subtitle"),
                style = MaterialTheme.typography.bodySmall,
                color = SaaSTextSecondary
            )
        }
    }
}

@Composable
private fun UsageBanner() {
    val language = LocalLanguage.current
    Text(
        text = localized(language, "AI يعمل عند الاتصال بالإنترنت فقط، وبقية التطبيق تستمر Offline.", "AI is online-only; the rest of the app can continue offline."),
        color = SaaSTextSecondary,
        fontSize = 11.sp,
    )
}

@Composable
private fun LimitBanner(message: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SaaSError.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message ?: t("ai_limit_reached"),
            modifier = Modifier.padding(12.dp),
            color = SaaSError,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SaaSSurface)
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
                    .background(if (active) SaaSPrimaryTeal else Color.Transparent)
                    .clickable { onOptionSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (active) SaaSTextPrimary else SaaSTextSecondary,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SaaSSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localized(language, "صياغة رد ذكي للعملاء", "Compose Smart Reply"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SaaSTextPrimary
            )

            // Message Input
            OutlinedTextField(
                value = replyMessage,
                onValueChange = onReplyMessageChange,
                placeholder = { Text(localized(language, "الصق رسالة العميل هنا...", "Paste the customer message here..."), color = SaaSTextSecondary) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaaSPrimaryTeal,
                    unfocusedBorderColor = SaaSBorder,
                    focusedContainerColor = SaaSBackground,
                    unfocusedContainerColor = SaaSBackground,
                    focusedTextColor = SaaSTextPrimary,
                    unfocusedTextColor = SaaSTextPrimary
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
                    color = SaaSPrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = SaaSPrimaryTeal
                )
            }

            if (showAdvanced) {
                ChipGroupSlider(localized(language, "اللهجة", "Dialect"), dialectOptions(language), dialect, onDialectChange)
                ChipGroupSlider(localized(language, "الطول", "Length"), lengthOptions(language), length, onLengthChange)
                ChipGroupSlider(localized(language, "الهدف", "Goal"), goalOptions(language), goal, onGoalChange)
                
                OutlinedTextField(
                    value = extra,
                    onValueChange = onExtraChange,
                    placeholder = { Text(t("ai_notes_label"), color = SaaSTextSecondary) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaaSPrimaryTeal,
                        unfocusedBorderColor = SaaSBorder,
                        focusedContainerColor = SaaSBackground,
                        unfocusedContainerColor = SaaSBackground,
                        focusedTextColor = SaaSTextPrimary,
                        unfocusedTextColor = SaaSTextPrimary
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
                    containerColor = SaaSPrimaryTeal,
                    contentColor = SaaSTextPrimary,
                    disabledContainerColor = SaaSBorder,
                    disabledContentColor = SaaSTextSecondary
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SaaSSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localized(language, "صياغة كابشن ذكي للمنتجات", "Generate Product Caption"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SaaSTextPrimary
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
                    placeholder = { Text(t("ai_prod_srv_label"), color = SaaSTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaaSPrimaryTeal,
                        unfocusedBorderColor = SaaSBorder,
                        focusedContainerColor = SaaSBackground,
                        unfocusedContainerColor = SaaSBackground,
                        focusedTextColor = SaaSTextPrimary,
                        unfocusedTextColor = SaaSTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = primaryBenefit,
                onValueChange = onPrimaryBenefitChange,
                placeholder = { Text(localized(language, "ما أهم فائدة تريد إبرازها؟ (اختياري)", "Primary benefit (optional)"), color = SaaSTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaaSPrimaryTeal,
                    unfocusedBorderColor = SaaSBorder,
                    focusedContainerColor = SaaSBackground,
                    unfocusedContainerColor = SaaSBackground,
                    focusedTextColor = SaaSTextPrimary,
                    unfocusedTextColor = SaaSTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = offer,
                onValueChange = onOfferChange,
                placeholder = { Text(t("ai_offer_label"), color = SaaSTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaaSPrimaryTeal,
                    unfocusedBorderColor = SaaSBorder,
                    focusedContainerColor = SaaSBackground,
                    unfocusedContainerColor = SaaSBackground,
                    focusedTextColor = SaaSTextPrimary,
                    unfocusedTextColor = SaaSTextPrimary
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
                    color = SaaSPrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = SaaSPrimaryTeal
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
                    containerColor = SaaSPrimaryTeal,
                    contentColor = SaaSTextPrimary,
                    disabledContainerColor = SaaSBorder,
                    disabledContentColor = SaaSTextSecondary
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
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SaaSPrimaryTeal.copy(alpha = 0.15f) else SaaSBackground)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (isSelected) SaaSPrimaryTeal else SaaSTextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = localized(language, "إلغاء التحديد", "Deselect"),
                tint = SaaSPrimaryTeal,
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
            options = success.data.variants.map { it.label },
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
    Card(
        colors = CardDefaults.cardColors(containerColor = SaaSSurface),
        shape = RoundedCornerShape(12.dp)
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
                    color = SaaSTextPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = SaaSTextSecondary
                )
            }

            val briefMeaning = if (data.analysis.objection != null) {
                localized(language, "العميل مهتم ولكنه يواجه اعتراضًا: ${data.analysis.objection}", "Customer is interested but has an objection: ${data.analysis.objection}")
            } else {
                localized(language, "العميل يبدو في وضع: ${data.analysis.buyingStage}", "Customer seems to be in: ${data.analysis.buyingStage}")
            }

            Text(
                text = briefMeaning,
                fontSize = 12.sp,
                color = SaaSTextSecondary
            )

            if (expanded) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text("نية: ${data.analysis.intent}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SaaSBackground,
                            labelColor = SaaSTextSecondary
                        ),
                        border = null
                    )
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text("مزاج: ${data.analysis.mood}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SaaSBackground,
                            labelColor = SaaSTextSecondary
                        ),
                        border = null
                    )
                }

                if (data.missingInformation.isNotEmpty()) {
                    Text(
                        text = localized(language, "معلومات ناقصة ننصح بسؤال العميل عنها: ", "Recommended missing info to ask: ") + 
                            data.missingInformation.joinToString("، "),
                        fontSize = 11.sp,
                        color = SaaSError
                    )
                }

                Text(
                    text = localized(language, "متبقي من باقتك: ", "Remaining credits: ") + 
                        "${data.usage.limit - data.usage.used} / ${data.usage.limit}",
                    fontSize = 11.sp,
                    color = SaaSTextSecondary
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SaaSSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = variant.text,
                lineHeight = 22.sp,
                fontSize = 14.sp,
                color = SaaSTextPrimary
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
                        contentColor = SaaSPrimaryTeal
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
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
                        containerColor = SaaSPrimaryTeal,
                        contentColor = SaaSTextPrimary
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
            containerColor = SaaSSurface,
            contentColor = SaaSTextPrimary,
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
            color = SaaSTextPrimary,
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
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = SaaSPrimaryTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, color = SaaSTextPrimary, fontSize = 13.sp)
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
            Icon(Icons.Filled.Flag, contentDescription = null, tint = SaaSError, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = t("ai_report"),
                color = SaaSError,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = localized(language, "اختر عميلاً للرد", "Select customer context"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = SaaSTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(localized(language, "بحث باسم العميل...", "Search customer name..."), color = SaaSTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaaSPrimaryTeal,
                unfocusedBorderColor = SaaSBorder,
                focusedContainerColor = SaaSBackground,
                unfocusedContainerColor = SaaSBackground,
                focusedTextColor = SaaSTextPrimary,
                unfocusedTextColor = SaaSTextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                text = t("no_search_results"),
                color = SaaSTextSecondary,
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
                        Text(customer.name, color = SaaSTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = localized(language, "اختر منتجاً لسياق البيانات", "Select product context"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = SaaSTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(localized(language, "بحث باسم المنتج...", "Search product name..."), color = SaaSTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaaSPrimaryTeal,
                unfocusedBorderColor = SaaSBorder,
                focusedContainerColor = SaaSBackground,
                unfocusedContainerColor = SaaSBackground,
                focusedTextColor = SaaSTextPrimary,
                unfocusedTextColor = SaaSTextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                text = t("no_products_available"),
                color = SaaSTextSecondary,
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
                        Text(product.name, color = SaaSTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${product.price} ${product.currency}", color = SaaSPrimaryTeal, fontSize = 12.sp)
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
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = SaaSTextSecondary,
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
                    .background(if (active) SaaSPrimaryTeal else SaaSBackground)
                    .clickable { onSelected(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = if (active) SaaSTextPrimary else SaaSTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SaaSError.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = SaaSError,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InfoCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SaaSSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = SaaSTextPrimary,
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
