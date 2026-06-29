package app.tijario.features.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalLayoutApi::class)
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

    LaunchedEffect(state) {
        if (state is AiV3ScreenState.Success) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val isBusy = state is AiV3ScreenState.Loading ||
        state is AiV3ScreenState.Refining ||
        state is AiV3ScreenState.Reporting
    val limitReachedByCache = uiState.planUsage?.let { it.aiLimit > 0 && it.aiUsed >= it.aiLimit } == true

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!hideHeader) {
                HeaderBlock()
            }

            UsageBanner()

            if (limitReachedByCache || state is AiV3ScreenState.LimitReached) {
                LimitBanner((state as? AiV3ScreenState.LimitReached)?.message)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        localError = null
                    },
                    label = { Text(t("tab_ai_reply")) },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        localError = null
                    },
                    label = { Text(t("tab_ai_caption")) },
                    modifier = Modifier.weight(1f),
                )
            }

            if (selectedTab == 0) {
                ReplyForm(
                    customers = uiState.customers,
                    products = uiState.products,
                    customerMessage = replyMessage,
                    onCustomerMessageChange = {
                        replyMessage = it.take(2000)
                        aiViewModel.markEditing()
                    },
                    quickCase = replyQuickCase,
                    onQuickCaseChange = { replyQuickCase = it },
                    selectedCustomerId = replyCustomerId,
                    onCustomerSelected = { replyCustomerId = it },
                    selectedProductId = replyProductId,
                    onProductSelected = { replyProductId = it },
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
                            return@ReplyForm
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
                )
            } else {
                CaptionForm(
                    products = uiState.products,
                    selectedProductId = captionProductId,
                    onProductSelected = { product ->
                        captionProductId = product?.id
                        if (product != null) productOrService = product.name
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
                            return@CaptionForm
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
                )
            }

            localError?.let { ErrorCard(it) }

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
                is AiV3ScreenState.Success -> ResultBlock(current, aiViewModel, language, isBusy = false) {
                    dataViewModel.refreshPlanUsage()
                }
            }
        }
    }
}

@Composable
private fun HeaderBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(t("ai_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(t("ai_subtitle"), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun UsageBanner() {
    val language = LocalLanguage.current
    Text(
        text = localized(language, "AI يعمل عند الاتصال بالإنترنت فقط، وبقية التطبيق تستمر Offline.", "AI is online-only; the rest of the app can continue offline."),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
    )
}

@Composable
private fun LimitBanner(message: String?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(
            text = message ?: t("ai_limit_reached"),
            modifier = Modifier.padding(14.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReplyForm(
    customers: List<Customer>,
    products: List<Product>,
    customerMessage: String,
    onCustomerMessageChange: (String) -> Unit,
    quickCase: String?,
    onQuickCaseChange: (String?) -> Unit,
    selectedCustomerId: String?,
    onCustomerSelected: (String?) -> Unit,
    selectedProductId: String?,
    onProductSelected: (String?) -> Unit,
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
) {
    val language = LocalLanguage.current
    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(t("ai_card_title"), fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = customerMessage,
                onValueChange = onCustomerMessageChange,
                label = { Text(localized(language, "الصق رسالة العميل هنا", "Paste the customer message here")) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            ChipGroup(
                title = localized(language, "حالات سريعة اختيارية", "Optional quick cases"),
                options = replyCaseOptions(language),
                selected = quickCase,
                onSelected = { onQuickCaseChange(if (quickCase == it) null else it) },
            )
            EntityChips(
                title = localized(language, "عميل محفوظ اختياري", "Optional saved customer"),
                items = customers.mapNotNull { customer -> customer.id?.let { it to customer.name } },
                selected = selectedCustomerId,
                onSelected = { onCustomerSelected(if (selectedCustomerId == it) null else it) },
            )
            EntityChips(
                title = localized(language, "منتج محفوظ اختياري", "Optional saved product"),
                items = products.mapNotNull { product -> product.id?.let { it to product.name } },
                selected = selectedProductId,
                onSelected = { onProductSelected(if (selectedProductId == it) null else it) },
            )
            OutlinedButton(onClick = onToggleAdvanced, modifier = Modifier.fillMaxWidth()) {
                Text(if (showAdvanced) localized(language, "إخفاء الإعدادات المتقدمة", "Hide advanced settings") else localized(language, "إعدادات متقدمة", "Advanced settings"))
            }
            if (showAdvanced) {
                ChipGroup(localized(language, "اللهجة", "Dialect"), dialectOptions(language), dialect, onDialectChange)
                ChipGroup(localized(language, "النبرة", "Tone"), replyToneOptions(language), tone, onToneChange)
                ChipGroup(localized(language, "الطول", "Length"), lengthOptions(language), length, onLengthChange)
                ChipGroup(localized(language, "الهدف", "Goal"), goalOptions(language), goal, onGoalChange)
                OutlinedTextField(
                    value = extra,
                    onValueChange = onExtraChange,
                    label = { Text(t("ai_notes_label")) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(onClick = onSubmit, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text(t("btn_generate_reply"), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun CaptionForm(
    products: List<Product>,
    selectedProductId: String?,
    onProductSelected: (Product?) -> Unit,
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
) {
    val language = LocalLanguage.current
    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(t("ai_caption_card_title"), fontWeight = FontWeight.Bold)
            ProductEntityChips(
                title = localized(language, "اختر منتجًا محفوظًا اختياريًا", "Optional saved product"),
                products = products,
                selected = selectedProductId,
                onSelected = { product -> onProductSelected(if (selectedProductId == product?.id) null else product) },
            )
            OutlinedTextField(
                value = productOrService,
                onValueChange = onProductOrServiceChange,
                label = { Text(t("ai_prod_srv_label")) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = primaryBenefit,
                onValueChange = onPrimaryBenefitChange,
                label = { Text(localized(language, "الميزة الأساسية (اختياري)", "Primary benefit (optional)")) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = offer,
                onValueChange = onOfferChange,
                label = { Text(t("ai_offer_label")) },
                modifier = Modifier.fillMaxWidth(),
            )
            ChipGroup(localized(language, "المنصة", "Platform"), platformOptions(language), platform, onPlatformChange)
            OutlinedButton(onClick = onToggleAdvanced, modifier = Modifier.fillMaxWidth()) {
                Text(if (showAdvanced) localized(language, "إخفاء الإعدادات المتقدمة", "Hide advanced settings") else localized(language, "إعدادات متقدمة", "Advanced settings"))
            }
            if (showAdvanced) {
                ChipGroup(localized(language, "نوع الكابشن", "Caption type"), captionTypeOptions(language), captionType, onCaptionTypeChange)
                ChipGroup(localized(language, "اللهجة", "Dialect"), dialectOptions(language), dialect, onDialectChange)
                ChipGroup(localized(language, "الأسلوب", "Style"), captionStyleOptions(language), style, onStyleChange)
                ChipGroup(localized(language, "الطول", "Length"), lengthOptions(language), length, onLengthChange)
            }
            Button(onClick = onSubmit, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text(t("ai_btn_gen_caption"), modifier = Modifier.padding(start = 8.dp))
            }
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
    success.notice?.let { InfoCard(it) }
    AnalysisCard(success.data, language)
    Text(t("generated_suggestions"), fontWeight = FontWeight.Bold)
    success.data.variants.forEach { variant ->
        VariantCard(
            generation = success,
            variant = variant,
            aiViewModel = aiViewModel,
            language = language,
            isBusy = isBusy,
            onUsageChanged = onUsageChanged,
        )
    }
}

@Composable
private fun AnalysisCard(data: AiV3ResponseData, language: AppLanguage) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(localized(language, "تحليل سريع", "Quick analysis"), fontWeight = FontWeight.Bold)
            Text(localized(language, "النية: ${data.analysis.intent}", "Intent: ${data.analysis.intent}"), fontSize = 12.sp)
            Text(localized(language, "المزاج: ${data.analysis.mood}", "Mood: ${data.analysis.mood}"), fontSize = 12.sp)
            Text(localized(language, "النبرة المقترحة: ${data.analysis.recommendedTone}", "Recommended tone: ${data.analysis.recommendedTone}"), fontSize = 12.sp)
            if (data.missingInformation.isNotEmpty()) {
                Text(
                    localized(language, "معلومات ناقصة: ${data.missingInformation.joinToString("، ")}", "Missing information: ${data.missingInformation.joinToString(", ")}"),
                    fontSize = 12.sp,
                )
            }
            Text(
                localized(language, "استخدام AI: ${data.usage.used}/${data.usage.limit}", "AI usage: ${data.usage.used}/${data.usage.limit}"),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VariantCard(
    generation: AiV3ScreenState.Success,
    variant: AiV3Variant,
    aiViewModel: AiViewModel,
    language: AppLanguage,
    isBusy: Boolean,
    onUsageChanged: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val copyLabel = t("copy")
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(variant.label, fontWeight = FontWeight.Bold)
                Text(variant.id, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(variant.text, lineHeight = 22.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(variant.text))
                        Toast.makeText(context, copyLabel, Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Text(t("copy"), modifier = Modifier.padding(start = 6.dp))
                }
                refineOptions(language).forEach { (preset, label) ->
                    AssistChip(
                        enabled = !isBusy,
                        onClick = {
                            aiViewModel.refine(
                                previous = generation,
                                variantId = variant.id,
                                preset = preset,
                                dialect = null,
                                language = language.code,
                                onSuccess = onUsageChanged,
                            )
                        },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                    )
                }
                AssistChip(
                    enabled = !isBusy,
                    onClick = {
                        aiViewModel.report(
                            previous = generation,
                            variantId = variant.id,
                            issueType = "wrong_answer",
                            note = null,
                            onDone = {},
                        )
                    },
                    label = { Text(t("ai_report")) },
                    leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun ChipGroup(
    title: String,
    options: List<Pair<String, String>>,
    selected: String?,
    onSelected: (String) -> Unit,
) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun EntityChips(
    title: String,
    items: List<Pair<String, String>>,
    selected: String?,
    onSelected: (String) -> Unit,
) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (items.isEmpty()) {
        Text(t("no_search_results"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.take(12).forEach { (id, name) ->
            FilterChip(
                selected = selected == id,
                onClick = { onSelected(id) },
                label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}

@Composable
private fun ProductEntityChips(
    title: String,
    products: List<Product>,
    selected: String?,
    onSelected: (Product?) -> Unit,
) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (products.isEmpty()) {
        Text(t("no_products_available"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        products.take(12).forEach { product ->
            FilterChip(
                selected = selected == product.id,
                onClick = { onSelected(product) },
                label = { Text("${product.name} • ${product.price} ${product.currency}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(message, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun InfoCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
        Text(message, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurface)
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
    "change_dialect" to localized(language, "تغيير اللهجة", "Change dialect"),
    "rewrite" to localized(language, "إعادة الصياغة", "Rewrite"),
)
