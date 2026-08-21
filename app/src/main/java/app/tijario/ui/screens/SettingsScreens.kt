package app.tijario.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextFieldDefaults
import app.tijario.ui.components.TijarioTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tijario.config.AppLanguage
import app.tijario.config.AppLanguageMode
import app.tijario.config.Localization
import app.tijario.config.AppRuntimeState
import app.tijario.config.AppThemeMode
import app.tijario.config.Supabase
import app.tijario.config.t
import app.tijario.domain.LocalizedErrorMapper
import app.tijario.data.remote.BillingPlanDto
import app.tijario.features.billing.BillingCatalog
import app.tijario.features.billing.BillingUiState
import app.tijario.features.billing.BillingUiEffect
import app.tijario.features.billing.BillingViewModel
import app.tijario.features.billing.GooglePlayBillingRepository
import app.tijario.features.notifications.NotificationTopicManager
import app.tijario.ui.state.TijarioDataViewModel
import app.tijario.ui.state.PlanUsageState
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import app.tijario.ui.components.LocalAdaptiveLayoutInfo
import app.tijario.ui.components.LogoutConfirmationDialog
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
    onStoreSettings: () -> Unit,
    onPersonalProfile: () -> Unit,
    onPaymentsSubscriptions: () -> Unit,
    onUpgradePlan: () -> Unit,
    onAccountSettings: () -> Unit,
    onAppSettings: () -> Unit,
    onBackupSettings: () -> Unit,
    onRequestReview: () -> Unit,
    onLogout: () -> Unit,
) {
    val adaptive = LocalAdaptiveLayoutInfo.current
    val context = LocalContext.current
    val appLanguage = LocalLanguage.current
    val isAr = appLanguage == AppLanguage.AR
    val planUsageState by dataViewModel.planUsageState.collectAsStateWithLifecycle()
    val profilePicFile = remember { File(context.filesDir, "personal_profile_pic.jpg") }
    var profilePicBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(
            profilePicFile.takeIf(File::exists)?.let { android.graphics.BitmapFactory.decodeFile(it.absolutePath) }
        )
    }
    val userId = remember { Supabase.client.auth.currentUserOrNull()?.id.orEmpty() }
    var profileName by remember {
        mutableStateOf(
            context.getSharedPreferences("tijario_app_preferences", Context.MODE_PRIVATE)
                .getString("profile_fullname_$userId", "") ?: ""
        )
    }
    val profileEmail = Supabase.client.auth.currentUserOrNull()?.email.orEmpty()
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showFeedbackScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val updatedName = dataViewModel.fetchCurrentProfileFullName().orEmpty()
        if (updatedName.isNotEmpty()) {
            profileName = updatedName
        }
        profilePicBitmap = profilePicFile
            .takeIf(File::exists)
            ?.let { android.graphics.BitmapFactory.decodeFile(it.absolutePath) }
        dataViewModel.refreshPlanUsage(force = false)
    }

    if (showFeedbackScreen) {
        FeedbackScreen(
            dataViewModel = dataViewModel,
            onBack = { showFeedbackScreen = false }
        )
    } else {
        Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("menu"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = adaptive.pageHorizontalPadding, vertical = adaptive.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsPlanBanner(
                state = planUsageState,
                onOpenPlan = onPaymentsSubscriptions,
                onUpgrade = onUpgradePlan,
            )

            SettingsProfileCard(
                name = profileName.ifBlank {
                    profileEmail.substringBefore("@").ifBlank { t("personal_account") }
                },
                email = profileEmail,
                profileBitmap = profilePicBitmap,
                onClick = onPersonalProfile,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SettingsOption(Icons.Outlined.BusinessCenter, t("store_settings"), onStoreSettings)
                    SettingsOption(Icons.Outlined.Person, t("account_settings"), onAccountSettings)
                    SettingsOption(Icons.Outlined.Tune, t("app_settings"), onAppSettings)
                    SettingsOption(Icons.Outlined.CreditCard, t("payments_subscriptions"), onPaymentsSubscriptions)
                    SettingsOption(Icons.Outlined.CloudSync, t("backup_restore"), onBackupSettings)
                    SettingsOption(
                        Icons.Outlined.Feedback,
                        if (isAr) "إرسال ملاحظة أو إبلاغ عن مشكلة" else "Send Feedback / Report Problem"
                    ) {
                        showFeedbackScreen = true
                    }
                    SettingsOption(
                        Icons.Filled.Star,
                        t("rate_app")
                    ) {
                        onRequestReview()
                    }
                    SettingsOption(
                        Icons.Outlined.Share,
                        if (isAr) "شارك التطبيق" else "Share App"
                    ) {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                if (isAr) {
                                    """
                                    🚀 تجاريو — كل أدوات تجارتك في مكان واحد
                                    
                                    نظّم أعمالك بسهولة، وأنشئ الفواتير وعروض الأسعار، وأدر العملاء والمنتجات والخدمات، واستفد من تجاريو AI لصياغة الردود والكابشنات بسرعة واحتراف.
                                    
                                    ✅ فواتير وعروض أسعار احترافية
                                    ✅ إدارة العملاء والمنتجات
                                    ✅ متابعة أعمالك ومبيعاتك
                                    ✅ مشاركة المستندات بسهولة
                                    ✅ أدوات AI ذكية تساعدك في عملك اليومي
                                    ✅ واجهة عربية بسيطة وسريعة
                                    
                                    إذا كنت صاحب متجر، مشروع صغير، مقدم خدمة أو تعمل بشكل مستقل، تجاريو يساعدك تنجز أكثر وبوقت أقل.
                                    
                                    📲 حمّل تجاريو من Google Play:
                                    https://play.google.com/store/apps/details?id=app.tijario
                                    """.trimIndent()
                                } else {
                                    """
                                    🚀 Tijario — All your business tools in one place
                                    
                                    Manage your business with ease, create professional invoices and quotations, organize customers, products, and services, and use Tijario AI to generate smart replies and captions faster.
                                    
                                    ✅ Professional invoices and quotations
                                    ✅ Customer and product management
                                    ✅ Track your business and sales
                                    ✅ Easily share your documents
                                    ✅ Smart AI tools for your daily work
                                    ✅ Clean, simple, and fast English interface
                                    
                                    Whether you run a store, small business, provide services, or work independently, Tijario helps you get more done in less time.
                                    
                                    📲 Download Tijario on Google Play:
                                    https://play.google.com/store/apps/details?id=app.tijario
                                    """.trimIndent()
                                }
                            )
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, if (isAr) "مشاركة التطبيق" else "Share App"))
                    }
                }
            }

            Button(
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(t("logout"), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = t("app_version"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = app.tijario.BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

        if (showLogoutConfirmation) {
            LogoutConfirmationDialog(
                visible = true,
                onDismiss = { showLogoutConfirmation = false },
                onConfirm = {
                    showLogoutConfirmation = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun SettingsPlanBanner(
    state: PlanUsageState,
    onOpenPlan: () -> Unit,
    onUpgrade: () -> Unit,
) {
    val planCode = (state as? PlanUsageState.Success)?.value?.planCode?.lowercase()
    val canUpgrade = planCode == "free" || planCode == "starter"
    val planName = when (state) {
        is PlanUsageState.Success -> when (state.value.planCode.lowercase()) {
            "free" -> t("free_plan_short")
            "starter" -> t("starter_plan_short")
            "pro", "business" -> t("pro_plan_short")
            else -> state.value.planName
        }
        else -> t("current_plan")
    }
    val planPitch = if (canUpgrade) t("plan_upgrade_pitch") else t("plan_active_pitch")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPlan),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = t("your_current_plan"),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "|",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.52f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = planName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                    )
                }
                Text(
                    text = planPitch,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                )
            }
            if (canUpgrade) {
                Surface(
                    modifier = Modifier.clickable(onClick = onUpgrade),
                    color = Color(0xFFFFC857).copy(alpha = 0.18f),
                    contentColor = Color(0xFFFFD166),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.65f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(t("upgrade"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsProfileCard(
    name: String,
    email: String,
    profileBitmap: android.graphics.Bitmap?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = name.trim().take(2).ifBlank { "T" },
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                if (email.isNotBlank()) {
                    Text(
                        email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsSubscriptionsScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
) {
    val adaptive = LocalAdaptiveLayoutInfo.current
    val planUsageState by dataViewModel.planUsageState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        dataViewModel.refreshPlanUsage()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("payments_subscriptions"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = adaptive.pageHorizontalPadding,
                    vertical = adaptive.sectionSpacing,
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = t("payments_subscriptions_desc"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            CurrentPlanUsageCard(
                state = planUsageState,
                onRetry = { dataViewModel.refreshPlanUsage() },
            )
            Button(
                onClick = onUpgrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(t("upgrade_plan"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CurrentPlanUsageCard(
    state: PlanUsageState,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF022C30), Color(0xFF0D6E76)),
                ),
            )
            .padding(15.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            when (state) {
                is PlanUsageState.Loading, PlanUsageState.Idle -> PlanUsageSkeleton()
                is PlanUsageState.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(t("current_plan"), color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                        Text(state.message, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onRetry) { Text(t("retry")) }
                    }
                }
                is PlanUsageState.Success -> {
                    val usage = state.value
                    val displayPlanName = when (usage.planCode.lowercase()) {
                        "free" -> t("free_plan")
                        "pro" -> "Pro"
                        "business" -> "Business"
                        else -> usage.planName
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(t("current_plan"), color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                            Text(displayPlanName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(23.dp),
                                )
                            }
                        }
                    }
                    UsageLine(t("documents"), usage.documentsUsed, usage.documentsLimit, Icons.Filled.Description, Color(0xFFCCFBF1))
                    UsageLine(t("ai_uses"), usage.aiUsed, usage.aiLimit, Icons.Filled.AutoAwesome, Color(0xFFBAE6FD))
                    UsageLine(t("tab_customers"), usage.customersUsed, usage.customersLimit, Icons.Filled.Person, Color(0xFFFDE68A))
                    UsageLine(t("tab_products"), usage.productsUsed, usage.productsLimit, Icons.Filled.ShoppingBag, Color(0xFFC4B5FD))
                }
            }
        }
    }
}

@Composable
private fun PlanUsageSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.22f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfileScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
) {
    val adaptive = LocalAdaptiveLayoutInfo.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val profilePicFile = remember { File(context.filesDir, "personal_profile_pic.jpg") }
    var profilePicBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var email by remember { mutableStateOf("") }
    var profileFullName by remember { mutableStateOf("") }
    var editNameValue by remember { mutableStateOf("") }
    var editNameError by remember { mutableStateOf<String?>(null) }
    var isEditingName by remember { mutableStateOf(false) }
    var isSavingName by remember { mutableStateOf(false) }
    val unknownUser = t("unknown_user")
    val invalidName = t("edit_name_invalid")
    val nameUpdated = t("name_updated")
    val nameUpdateFailed = t("name_update_failed")
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    profilePicFile.writeBytes(input.readBytes())
                } ?: error("profile_photo_unavailable")
                android.graphics.BitmapFactory.decodeFile(profilePicFile.absolutePath)
                    ?: error("profile_photo_invalid")
            }.onSuccess { profilePicBitmap = it }
        }
    }

    LaunchedEffect(Unit) {
        email = Supabase.client.auth.currentUserOrNull()?.email.orEmpty()
        profileFullName = dataViewModel.fetchCurrentProfileFullName().orEmpty()
        editNameValue = profileFullName
        profilePicBitmap = profilePicFile
            .takeIf(File::exists)
            ?.let { android.graphics.BitmapFactory.decodeFile(it.absolutePath) }
    }

    fun saveName() {
        if (isSavingName) return
        val normalized = editNameValue.trim()
        if (normalized.length !in 2..80) {
            editNameError = invalidName
            return
        }
        scope.launch {
            isSavingName = true
            val result = dataViewModel.updateCurrentProfileFullName(normalized)
            isSavingName = false
            if (result.isSuccess) {
                profileFullName = normalized
                editNameValue = normalized
                editNameError = null
                isEditingName = false
                snackbarHostState.showSnackbar(nameUpdated)
            } else {
                snackbarHostState.showSnackbar(nameUpdateFailed)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("tab_personal_account"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = adaptive.pageHorizontalPadding, vertical = adaptive.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF081C36), Color(0xFF0F2D54)),
                            ),
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = profileFullName.trim().ifBlank { unknownUser },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            text = t("personal_account_desc"),
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                        )
                    }
                    Box(
                        modifier = Modifier.clickable { photoPicker.launch("image/*") },
                    ) {
                        Surface(
                            color = Color(0xFF0D9488),
                            shape = CircleShape,
                            modifier = Modifier.size(68.dp),
                            border = BorderStroke(2.dp, Color.White),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                profilePicBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = t("edit_profile_photo"),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                } ?: Text(
                                    text = profileFullName.trim().take(2).ifBlank { "T" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 23.sp,
                                )
                            }
                        }
                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = (-2).dp, y = 2.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = t("edit_profile_photo"),
                                    tint = Color(0xFF081C36),
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = editNameValue,
                        onValueChange = {
                            editNameValue = it
                            editNameError = null
                        },
                        label = { Text(t("fullname")) },
                        leadingIcon = { Icon(Icons.Filled.PersonOutline, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (isEditingName) {
                                        saveName()
                                    } else {
                                        editNameValue = profileFullName
                                        editNameError = null
                                        isEditingName = true
                                    }
                                },
                                enabled = !isSavingName,
                            ) {
                                if (isSavingName) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        if (isEditingName) Icons.Filled.Check else Icons.Filled.Edit,
                                        contentDescription = if (isEditingName) t("btn_save") else t("edit_name"),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        },
                        readOnly = !isEditingName,
                        placeholder = { Text(unknownUser) },
                        singleLine = true,
                        isError = editNameError != null,
                        supportingText = editNameError?.let { message -> { Text(message) } },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = email.ifBlank { t("no_email_associated") },
                        onValueChange = {},
                        label = { Text(t("email")) },
                        leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit,
    onDeleteAccount: suspend () -> Result<Unit> = { Result.success(Unit) },
) {
    val adaptive = LocalAdaptiveLayoutInfo.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val language = LocalLanguage.current
    val subscriptionBillingViewModel: BillingViewModel = viewModel(
        key = "account-subscription-sync",
        factory = remember(context) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BillingViewModel(
                        GooglePlayBillingRepository(
                            context = context.applicationContext,
                            backendApiClient = Supabase.apiClient,
                        )
                    ) as T
                }
            }
        }
    )
    val subscriptionBillingState by subscriptionBillingViewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptionBillingViewModel) {
        subscriptionBillingViewModel.effects.collect { effect ->
            if (effect is BillingUiEffect.SubscriptionSynced) {
                val result = dataViewModel.refreshUntilPlanMatches(effect.expectedPlanCode)
                val message = if (result.isSuccess) {
                    if (language == AppLanguage.AR) {
                        "تم تحديث اشتراكك بنجاح."
                    } else {
                        "Your subscription has been updated successfully."
                    }
                } else {
                    if (language == AppLanguage.AR) {
                        "تعذر تحديث الاشتراك الآن. حاول مرة أخرى."
                    } else {
                        "We could not update your subscription. Please try again."
                    }
                }
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(subscriptionBillingState.errorMessage) {
        val errorKey = subscriptionBillingState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(billingMessage(errorKey, language))
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(t("delete_account"), fontWeight = FontWeight.Bold) },
            text = { Text(t("delete_account_desc")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = onDeleteAccount()
                            if (result.isSuccess) {
                                showDeleteConfirm = false
                                onLogout()
                            } else {
                                snackbarHostState.showSnackbar(
                                    LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language)
                                )
                            }
                        }
                    }
                ) { Text(t("delete_account")) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(t("btn_cancel")) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("account_settings"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = adaptive.pageHorizontalPadding, vertical = adaptive.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CompactSettingsRow(
                        icon = Icons.Filled.Lock,
                        title = t("change_password"),
                        onClick = onChangePassword,
                    )
                    CompactSettingsRow(
                        icon = Icons.Filled.WorkspacePremium,
                        title = if (subscriptionBillingState.isRestoring) {
                            if (language == AppLanguage.AR) "جارٍ مزامنة الاشتراك..." else "Syncing subscription..."
                        } else {
                            if (language == AppLanguage.AR) "مزامنة الاشتراك" else "Sync subscription"
                        },
                        subtitle = if (language == AppLanguage.AR) {
                            "استعادة اشتراك Google Play عند الحاجة"
                        } else {
                            "Restore your Google Play subscription when needed"
                        },
                        enabled = !subscriptionBillingState.isRestoring,
                        onClick = subscriptionBillingViewModel::restorePurchases,
                        trailing = if (subscriptionBillingState.isRestoring) {
                            {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        } else {
                            null
                        },
                    )
                    CompactSettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = t("logout"),
                        titleColor = MaterialTheme.colorScheme.error,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showLogoutConfirmation = true },
                    )
                    CompactSettingsRow(
                        icon = Icons.Filled.DeleteForever,
                        title = t("delete_account"),
                        subtitle = t("delete_account_short_desc"),
                        titleColor = MaterialTheme.colorScheme.error,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showDeleteConfirm = true },
                    )
                }
            }

            if (showLogoutConfirmation) {
                LogoutConfirmationDialog(
                    visible = true,
                    onDismiss = { showLogoutConfirmation = false },
                    onConfirm = {
                        showLogoutConfirmation = false
                        onLogout()
                    },
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var languageMode by remember(context) { mutableStateOf(AppPreferences.getLanguageMode(context)) }
    var themeMode by remember(context) { mutableStateOf(AppPreferences.getThemeMode(context)) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    val effectiveLanguageMode = when (languageMode) {
        AppLanguageMode.SYSTEM -> if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
            AppLanguageMode.ARABIC
        } else {
            AppLanguageMode.ENGLISH
        }
        else -> languageMode
    }

    if (showLanguageSheet) {
        SettingsSelectionBottomSheet(
            title = t("settings_lang"),
            selected = effectiveLanguageMode,
            options = listOf(
                SettingsSelectionOption(AppLanguageMode.ARABIC, t("language_arabic"), Icons.Filled.Translate),
                SettingsSelectionOption(AppLanguageMode.ENGLISH, t("language_english"), Icons.Filled.Translate),
            ),
            onDismiss = { showLanguageSheet = false },
            onSelect = { selected ->
                languageMode = selected
                AppPreferences.setLanguageMode(context, selected)
                AppRuntimeState.currentLanguage = AppPreferences.getLanguage(context)
                showLanguageSheet = false
            },
        )
    }

    if (showThemeSheet) {
        SettingsSelectionBottomSheet(
            title = t("settings_theme"),
            selected = themeMode,
            options = listOf(
                SettingsSelectionOption(AppThemeMode.SYSTEM, t("theme_system"), Icons.Filled.SettingsBrightness),
                SettingsSelectionOption(AppThemeMode.LIGHT, t("theme_light"), Icons.Filled.LightMode),
                SettingsSelectionOption(AppThemeMode.DARK, t("theme_dark"), Icons.Filled.DarkMode),
            ),
            onDismiss = { showThemeSheet = false },
            onSelect = { selected ->
                themeMode = selected
                AppPreferences.setThemeMode(context, selected)
                AppRuntimeState.isDarkMode = AppPreferences.getDarkMode(context)
                showThemeSheet = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(t("app_settings"), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CompactSettingsRow(
                        icon = Icons.Filled.Translate,
                        title = t("settings_lang"),
                        subtitle = when (effectiveLanguageMode) {
                            AppLanguageMode.ARABIC -> t("language_arabic")
                            AppLanguageMode.ENGLISH -> t("language_english")
                            AppLanguageMode.SYSTEM -> t("language_arabic")
                        },
                        onClick = { showLanguageSheet = true },
                    )

                    CompactSettingsRow(
                        icon = Icons.Filled.SettingsBrightness,
                        title = t("settings_theme"),
                        subtitle = when (themeMode) {
                            AppThemeMode.SYSTEM -> t("theme_system")
                            AppThemeMode.LIGHT -> t("theme_light")
                            AppThemeMode.DARK -> t("theme_dark")
                        },
                        onClick = { showThemeSheet = true },
                        showIconContainer = false,
                        iconSize = 28.dp,
                    )

                    LocalNotificationSettingsSection()

                    CompactSettingsRow(
                        icon = Icons.Filled.Shield,
                        title = t("privacy_policy"),
                        subtitle = "tijario.site/privacy",
                        onClick = {
                            runCatching {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://tijario.site/privacy"),
                                )
                                context.startActivity(intent)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalNotificationSettingsSection() {
    val context = LocalContext.current
    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    val topicManager = remember(context) { NotificationTopicManager(context) }
    var enabled by remember { mutableStateOf(AppPreferences.isPushEnabled(context)) }

    CompactSettingsRow(
        icon = Icons.Filled.Notifications,
        title = t("notifications"),
        onClick = {
            enabled = !enabled
            AppPreferences.setPushEnabled(context, enabled)
            scope.launch { runCatching { topicManager.syncForLanguage(language) } }
        },
        trailing = {
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    AppPreferences.setPushEnabled(context, it)
                    scope.launch { runCatching { topicManager.syncForLanguage(language) } }
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradePlanScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val planUsageState by dataViewModel.planUsageState.collectAsStateWithLifecycle()
    val currentUserId = Supabase.client.auth.currentUserOrNull()?.id.orEmpty()
    val billingViewModel: BillingViewModel = viewModel(
        factory = remember(context) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BillingViewModel(
                        GooglePlayBillingRepository(
                            context = context.applicationContext,
                            backendApiClient = Supabase.apiClient,
                        )
                    ) as T
                }
            }
        }
    )
    val billingState by billingViewModel.state.collectAsStateWithLifecycle()
    val annualBilling = billingState.selectedInterval == BillingCatalog.INTERVAL_YEARLY
    var upgradedPlanCode by remember { mutableStateOf<String?>(null) }
    var upgradeRefreshFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dataViewModel.refreshPlanUsage()
        billingViewModel.load()
    }

    LaunchedEffect(billingViewModel) {
        billingViewModel.effects.collect { effect ->
            when (effect) {
                is BillingUiEffect.PurchaseVerified -> {
                    val result = dataViewModel.refreshUntilPlanMatches(effect.expectedPlanCode)
                    val usage = result.getOrNull()
                    if (usage != null) {
                        upgradedPlanCode = usage.planCode
                        upgradeRefreshFailed = false
                    } else {
                        upgradeRefreshFailed = true
                    }
                }

                is BillingUiEffect.SubscriptionSynced -> {
                    dataViewModel.refreshUntilPlanMatches(effect.expectedPlanCode)
                }
            }
        }
    }

    val usage = (planUsageState as? PlanUsageState.Success)?.value
    val currentPlanCode = BillingCatalog.publicPlanCode(usage?.planCode.orEmpty()) ?: "free"

    upgradedPlanCode?.let { planCode ->
        val planName = when (planCode.lowercase()) {
            "starter" -> "Starter"
            "pro" -> "Pro"
            else -> planCode
        }

        AlertDialog(
            onDismissRequest = { upgradedPlanCode = null },
            title = {
                Text(
                    text = if (isArabic) "تمت الترقية بنجاح" else "Upgrade successful",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = if (isArabic) {
                        "تمت ترقية خطتك إلى $planName، وأصبحت مزايا وحدود خطتك الجديدة متاحة الآن."
                    } else {
                        "Your plan has been upgraded to $planName. Your new features and usage limits are now available."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { upgradedPlanCode = null }) {
                    Text(
                        if (isArabic) {
                            "ابدأ باستخدام الخطة"
                        } else {
                            "Start using your plan"
                        }
                    )
                }
            }
        )
    }

    if (upgradeRefreshFailed) {
        AlertDialog(
            onDismissRequest = { upgradeRefreshFailed = false },
            title = {
                Text(
                    text = if (isArabic) "تم تأكيد عملية الشراء" else "Purchase confirmed",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = if (isArabic) {
                        "تم تأكيد عملية الشراء، لكن تعذر تحديث الخطة الآن. افتح إعدادات الحساب واستخدم مزامنة الاشتراك."
                    } else {
                        "Your purchase was confirmed, but the plan could not be refreshed. Open account settings and use Sync subscription."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { upgradeRefreshFailed = false }) {
                    Text(if (isArabic) "حسناً" else "OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("upgrade_plan"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val annualDiscountPercent = pricingAnnualDiscountPercent()

            PricingHeroCard(
                isArabic = isArabic,
                annualBilling = annualBilling,
                annualDiscountPercent = annualDiscountPercent,
                onToggleBilling = {
                    billingViewModel.selectInterval(
                        if (it) BillingCatalog.INTERVAL_YEARLY else BillingCatalog.INTERVAL_MONTHLY
                    )
                },
            )

            PricingPlansSection(
                isArabic = isArabic,
                currentPlanCode = currentPlanCode,
                annualBilling = annualBilling,
                billingState = billingState,
                onRetry = { billingViewModel.load() },
                onPurchase = { planCode ->
                    val purchaseActivity = activity ?: return@PricingPlansSection
                    if (currentUserId.isNotBlank()) {
                        billingViewModel.purchase(purchaseActivity, currentUserId, planCode)
                    }
                },
            )

            PricingComparisonSection(isArabic = isArabic)
            PricingFaqSection(isArabic = isArabic)

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when {
                            billingState.isLoading -> t("billing_loading_prices")
                            billingState.errorMessage != null -> billingMessage(billingState.errorMessage, language)
                            billingState.successMessage != null -> billingMessage(billingState.successMessage, language)
                            else -> t("billing_google_play_note")
                        },
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private data class PricingPlanUi(
    val code: String,
    val nameAr: String,
    val nameEn: String,
    val descAr: String,
    val descEn: String,
    val monthlyPriceCents: Int,
    val annualPriceCents: Int,
    val monthlyDocumentLimit: Int,
    val monthlyAiLimit: Int,
    val customerLimit: Int?,
    val productLimit: Int?,
    val templatesAr: String,
    val templatesEn: String,
    val supportAr: String,
    val supportEn: String,
    val featured: Boolean,
)

private fun BillingPlanDto.toPricingPlanUi(): PricingPlanUi =
    PricingPlanUi(
        code = code.lowercase(),
        nameAr = nameAr ?: defaultPlanNameAr(code),
        nameEn = nameEn ?: defaultPlanNameEn(code),
        descAr = defaultPlanDescriptionAr(code),
        descEn = defaultPlanDescriptionEn(code),
        monthlyPriceCents = billingOptions.firstOrNull { it.billingInterval == BillingCatalog.INTERVAL_MONTHLY }?.priceCents ?: 0,
        annualPriceCents = billingOptions.firstOrNull { it.billingInterval == BillingCatalog.INTERVAL_YEARLY }?.priceCents ?: 0,
        monthlyDocumentLimit = monthlyDocumentLimit,
        monthlyAiLimit = monthlyAiLimit,
        customerLimit = customerLimit,
        productLimit = productLimit,
        templatesAr = if (templateAccess == "all") "جميع القوالب الحالية" else "القالب الأساسي فقط",
        templatesEn = if (templateAccess == "all") "All current templates" else "Basic template only",
        supportAr = when (supportLevel) {
            "priority" -> "دعم أولوية"
            "self_service" -> "دعم ذاتي"
            else -> "دعم قياسي"
        },
        supportEn = when (supportLevel) {
            "priority" -> "Priority support"
            "self_service" -> "Self-service support"
            else -> "Standard support"
        },
        featured = code.lowercase() == "pro",
    )

private fun fallbackPricingPlans(): List<PricingPlanUi> = listOf(
    PricingPlanUi(
        code = "free",
        nameAr = "مجاني",
        nameEn = "Free",
        descAr = defaultPlanDescriptionAr("free"),
        descEn = defaultPlanDescriptionEn("free"),
        monthlyPriceCents = 0,
        annualPriceCents = 0,
        monthlyDocumentLimit = 5,
        monthlyAiLimit = 10,
        customerLimit = 5,
        productLimit = 5,
        templatesAr = "القالب الأساسي فقط",
        templatesEn = "Basic template only",
        supportAr = "دعم ذاتي",
        supportEn = "Self-service support",
        featured = false,
    ),
    PricingPlanUi(
        code = "starter",
        nameAr = "Starter",
        nameEn = "Starter",
        descAr = defaultPlanDescriptionAr("starter"),
        descEn = defaultPlanDescriptionEn("starter"),
        monthlyPriceCents = 0,
        annualPriceCents = 0,
        monthlyDocumentLimit = 50,
        monthlyAiLimit = 150,
        customerLimit = 30,
        productLimit = 30,
        templatesAr = "جميع القوالب الحالية",
        templatesEn = "All current templates",
        supportAr = "دعم قياسي",
        supportEn = "Standard support",
        featured = false,
    ),
    PricingPlanUi(
        code = "pro",
        nameAr = "Pro",
        nameEn = "Pro",
        descAr = defaultPlanDescriptionAr("pro"),
        descEn = defaultPlanDescriptionEn("pro"),
        monthlyPriceCents = 0,
        annualPriceCents = 0,
        monthlyDocumentLimit = 200,
        monthlyAiLimit = 500,
        customerLimit = null,
        productLimit = null,
        templatesAr = "جميع القوالب الحالية",
        templatesEn = "All current templates",
        supportAr = "دعم أولوية",
        supportEn = "Priority support",
        featured = true,
    ),
)

private fun resolvedPricingPlans(backendPlans: List<BillingPlanDto>): List<PricingPlanUi> {
    val remotePlans = backendPlans
        .filter { BillingCatalog.publicPlanCode(it.code) == it.code.lowercase() }
        .associate { it.code.lowercase() to it.toPricingPlanUi() }
    return fallbackPricingPlans().map { fallback -> remotePlans[fallback.code] ?: fallback }
}

private fun defaultPlanNameAr(code: String): String =
    when (code.lowercase()) {
        "free" -> "مجاني"
        "starter" -> "Starter"
        "pro" -> "Pro"
        "business" -> "Business"
        else -> code
    }

private fun defaultPlanNameEn(code: String): String =
    when (code.lowercase()) {
        "free" -> "Free"
        "starter" -> "Starter"
        "pro" -> "Pro"
        "business" -> "Business"
        else -> code
    }

private fun defaultPlanDescriptionAr(code: String): String =
    when (code.lowercase()) {
        "free" -> "ابدأ مجاناً مع الأساسيات."
        "starter" -> "للمتاجر التي بدأت تكبر."
        "pro" -> "الخطة الأكثر توازناً للمتاجر النشطة."
        "business" -> "للاستخدام المرتفع والمتاجر النشطة."
        else -> ""
    }

private fun defaultPlanDescriptionEn(code: String): String =
    when (code.lowercase()) {
        "free" -> "Start free with the basics."
        "starter" -> "For stores starting to grow."
        "pro" -> "The balanced plan for active stores."
        "business" -> "For high usage and active stores."
        else -> ""
    }

private fun billingMessage(key: String?, language: AppLanguage): String =
    Localization.getString(key ?: "billing_unavailable", language)

@Composable
private fun PricingHeroCard(
    isArabic: Boolean,
    annualBilling: Boolean,
    annualDiscountPercent: Int,
    onToggleBilling: (Boolean) -> Unit,
) {
    val title = if (isArabic) "خطة تناسب كل مرحلة من نمو متجرك" else "A plan for every stage of your store"
    val subtitle = if (isArabic) {
        "ابدأ مجاناً ثم قم بالترقية عندما يكبر عملك."
    } else {
        "Start free, then upgrade as your documents, customers, products, and AI usage grow."
    }
    val monthly = if (isArabic) "شهري" else "Monthly"
    val annual = if (isArabic) "سنوي" else "Annual"
    val discountBadge = if (isArabic) "$annualDiscountPercent% خصم" else "$annualDiscountPercent% OFF"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(color = Color(0xFFE6FFFA), shape = CircleShape, modifier = Modifier.size(50.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.RocketLaunch,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(25.dp),
                    )
                }
            }
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                PricingToggleChip(
                    label = monthly,
                    selected = !annualBilling,
                    onClick = { onToggleBilling(false) },
                )
                Spacer(modifier = Modifier.size(10.dp))
                Box {
                    PricingToggleChip(
                        label = annual,
                        selected = annualBilling,
                        onClick = { onToggleBilling(true) },
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-8).dp),
                    ) {
                        Text(
                            text = discountBadge,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF0D9488) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun CurrentUsagePricingCard(
    isArabic: Boolean,
    usage: app.tijario.data.model.UserPlanUsage?,
    currentPlanCode: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t("current_plan"), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = when (currentPlanCode) {
                            "free" -> if (isArabic) "الخطة المجانية" else "Free"
                            "pro" -> "Pro"
                            else -> currentPlanCode
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Surface(color = Color(0xFFE6FFFA), shape = CircleShape, modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }

            if (usage != null) {
                UsageLine(t("documents"), usage.documentsUsed, usage.documentsLimit, Icons.Filled.Description, Color(0xFFCCFBF1))
                UsageLine(t("ai_uses"), usage.aiUsed, usage.aiLimit, Icons.Filled.AutoAwesome, Color(0xFFBAE6FD))
                UsageLine(t("tab_customers"), usage.customersUsed, usage.customersLimit, Icons.Filled.Person, Color(0xFFFDE68A))
                UsageLine(t("tab_products"), usage.productsUsed, usage.productsLimit, Icons.Filled.ShoppingBag, Color(0xFFC4B5FD))
            } else {
                Text(
                    text = if (isArabic) "جارٍ تحميل الاستهلاك الحالي..." else "Loading current usage...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PricingPlansSection(
    isArabic: Boolean,
    currentPlanCode: String,
    annualBilling: Boolean,
    billingState: BillingUiState,
    onRetry: () -> Unit,
    onPurchase: (String) -> Unit,
) {
    val plans = remember(billingState.backendPlans) {
        resolvedPricingPlans(billingState.backendPlans)
    }
    val interval = if (annualBilling) BillingCatalog.INTERVAL_YEARLY else BillingCatalog.INTERVAL_MONTHLY
    val initialPage = plans.indexOfFirst { it.code == currentPlanCode }
        .takeIf { it >= 0 }
        ?: plans.indexOfFirst { it.code == "pro" }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { plans.size }

    LaunchedEffect(currentPlanCode, plans.size) {
        plans.indexOfFirst { it.code == currentPlanCode }
            .takeIf { it >= 0 && it != pagerState.currentPage }
            ?.let { pagerState.scrollToPage(it) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (isArabic) "الباقات" else "Plans",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(448.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            pageSpacing = 12.dp,
        ) { page ->
            val plan = plans[page]
            SwipePricingPlanCard(
                modifier = Modifier.fillMaxSize(),
                plan = plan,
                currentPlanCode = currentPlanCode,
                annualBilling = annualBilling,
                isArabic = isArabic,
                googlePlayPrice = billingState.offerFor(plan.code, interval)?.formattedPrice,
                isPurchasing = billingState.isPurchasing,
                onPurchase = { onPurchase(plan.code) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            plans.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == pagerState.currentPage) 18.dp else 7.dp, 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                )
            }
        }
        Text(
            text = t("plans_swipe_hint"),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        if (billingState.errorMessage != null) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(t("billing_retry"))
            }
        }
    }
}

@Composable
private fun SwipePricingPlanCard(
    modifier: Modifier,
    plan: PricingPlanUi,
    currentPlanCode: String,
    annualBilling: Boolean,
    isArabic: Boolean,
    googlePlayPrice: String?,
    isPurchasing: Boolean,
    onPurchase: () -> Unit,
) {
    val isCurrent = plan.code == currentPlanCode
    val isPro = plan.code == "pro"
    val accent = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val priceLabel = when {
        plan.code == "free" -> if (isArabic) "مجاني" else "Free"
        !googlePlayPrice.isNullOrBlank() -> {
            val period = if (annualBilling) {
                if (isArabic) " / سنوياً" else " / year"
            } else {
                if (isArabic) " / شهرياً" else " / month"
            }
            "$googlePlayPrice$period"
        }
        else -> t("plans_price_from_play")
    }
    val canPurchase = plan.code != "free" && !isCurrent && !googlePlayPrice.isNullOrBlank() && !isPurchasing
    val hasPaidBackup = plan.code != "free"
    val features = listOf(
        (if (isArabic) "مستندات شهرية: ${plan.monthlyDocumentLimit}" else "Documents/month: ${plan.monthlyDocumentLimit}") to true,
        (if (isArabic) "عمليات AI شهرية: ${plan.monthlyAiLimit}" else "AI/month: ${plan.monthlyAiLimit}") to true,
        (if (plan.customerLimit == null) {
            if (isArabic) "العملاء: غير محدود" else "Customers: Unlimited"
        } else {
            if (isArabic) "العملاء: ${plan.customerLimit}" else "Customers: ${plan.customerLimit}"
        }) to true,
        (if (plan.productLimit == null) {
            if (isArabic) "المنتجات: غير محدود" else "Products: Unlimited"
        } else {
            if (isArabic) "المنتجات: ${plan.productLimit}" else "Products: ${plan.productLimit}"
        }) to true,
        (if (isArabic) plan.templatesAr else plan.templatesEn) to true,
        (if (isArabic) "نسخ محلي تلقائي يومي أو أسبوعي" else "Daily or weekly automatic local backup") to hasPaidBackup,
        (if (isArabic) "النسخ والاستعادة عبر Google Drive" else "Google Drive backup and restore") to hasPaidBackup,
        (if (isArabic) plan.supportAr else plan.supportEn) to true,
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isPro) 2.dp else 1.dp,
            color = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPro) 3.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (plan.code == "free") Icons.Filled.Check else Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isArabic) plan.nameAr else plan.nameEn,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = if (isArabic) plan.descAr else plan.descEn,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    )
                }
                if (isCurrent) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = t("current_plan"),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            color = Color(0xFF2E7D32),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Text(
                text = priceLabel,
                fontSize = if (googlePlayPrice.isNullOrBlank() && plan.code != "free") 13.sp else 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                features.forEach { (feature, included) ->
                    PricingFeatureRow(text = feature, accent = accent, included = included)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = onPurchase,
                enabled = canPurchase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = when {
                        isCurrent -> if (isArabic) "الخطة الحالية" else "Current plan"
                        plan.code == "free" -> if (isArabic) "الخطة المجانية" else "Free plan"
                        isPurchasing -> if (isArabic) "جارٍ فتح Google Play..." else "Opening Google Play..."
                        googlePlayPrice.isNullOrBlank() -> if (isArabic) "السعر غير متاح الآن" else "Price unavailable"
                        annualBilling -> if (isArabic) "ترقية" else "Upgrade"
                        else -> if (isArabic) "ترقية" else "Upgrade"
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun PricingPlanCard(
    plan: PricingPlanUi,
    currentPlanCode: String,
    annualBilling: Boolean,
    isArabic: Boolean,
    googlePlayPrice: String?,
    isBillingLoading: Boolean,
    isPurchasing: Boolean,
    onPurchase: () -> Unit,
) {
    val isCurrent = plan.code == currentPlanCode
    val isDarkTheme = AppRuntimeState.isDarkMode
    val accent = when (plan.code) {
        "free" -> Color(0xFF0D9488)
        "starter" -> Color(0xFF7C3AED)
        "pro" -> Color(0xFF2563EB)
        else -> MaterialTheme.colorScheme.primary
    }
    val priceLabel = if (plan.code == "free") {
        if (isArabic) "مجاني" else "Free"
    } else if (!googlePlayPrice.isNullOrBlank()) {
        val period = if (annualBilling) {
            if (isArabic) " / سنوياً" else " / year"
        } else {
            if (isArabic) " / شهرياً" else " / month"
        }
        "$googlePlayPrice$period"
    } else if (isBillingLoading) {
        if (isArabic) "جارٍ تحميل السعر..." else "Loading price..."
    } else {
        if (isArabic) "غير متاح في Google Play" else "Unavailable in Google Play"
    }
    val canPurchase = plan.code != "free" && !isCurrent && !googlePlayPrice.isNullOrBlank() && !isPurchasing
    var expanded by remember(plan.code) { mutableStateOf(false) }
    val currentContainer = when {
        isCurrent && isDarkTheme -> Color.White
        isCurrent -> Color(0xFFEFFAF7)
        else -> MaterialTheme.colorScheme.surface
    }
    val currentOnSurface = when {
        isCurrent && isDarkTheme -> Color(0xFF111827)
        isCurrent -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    val currentSurfaceVariant = when {
        isCurrent && isDarkTheme -> Color(0xFFF3F4F6)
        isCurrent -> Color(0xFFE0F2FE)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = currentContainer),
        border = BorderStroke(
            1.dp,
            when {
                isCurrent && isDarkTheme -> Color(0xFFE5E7EB)
                plan.featured -> accent
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (plan.featured) 4.dp else 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isArabic) plan.nameAr else plan.nameEn,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = currentOnSurface,
                        )
                        if (plan.featured) {
                            Surface(color = if (isCurrent && isDarkTheme) Color(0xFFF3F4F6) else Color(0xFFE0F2FE), shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = if (isArabic) "الأكثر اختياراً" else "Most chosen",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = if (isCurrent && isDarkTheme) Color(0xFF111827) else Color(0xFF075985),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (isCurrent) {
                            Surface(color = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFFD1FAE5), shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = t("current_plan"),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = if (isDarkTheme) Color(0xFF111827) else Color(0xFF065F46),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isArabic) plan.descAr else plan.descEn,
                        color = if (isCurrent && isDarkTheme) Color(0xFF374151) else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = currentSurfaceVariant, shape = CircleShape, modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (plan.code == "free") Icons.Filled.Check else Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (expanded) {
                                if (isArabic) "إخفاء المزايا" else "Hide features"
                            } else {
                                if (isArabic) "عرض المزايا" else "Show features"
                            },
                            tint = currentOnSurface,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f),
                        )
                    }
                }
            }

            Text(
                text = priceLabel,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = accent,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PricingCompactChip(
                    text = if (isArabic) "مستندات ${plan.monthlyDocumentLimit}" else "Docs ${plan.monthlyDocumentLimit}",
                    accent = accent,
                )
                PricingCompactChip(
                    text = if (isArabic) "AI ${plan.monthlyAiLimit}" else "AI ${plan.monthlyAiLimit}",
                    accent = accent,
                )
                PricingCompactChip(
                    text = if (plan.customerLimit == null) {
                        if (isArabic) "عملاء غير محدود" else "Customers unlimited"
                    } else {
                        if (isArabic) "عملاء ${plan.customerLimit}" else "Customers ${plan.customerLimit}"
                    },
                    accent = accent,
                )
            }

            Button(
                onClick = onPurchase,
                enabled = canPurchase,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White,
                    disabledContainerColor = if (isCurrent) accent else MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = when {
                        isCurrent -> if (isArabic) "الخطة الحالية" else "Current plan"
                        plan.code == "free" -> if (isArabic) "الخطة المجانية" else "Free plan"
                        isPurchasing -> if (isArabic) "جارٍ فتح Google Play..." else "Opening Google Play..."
                        googlePlayPrice.isNullOrBlank() -> if (isArabic) "غير متاح الآن" else "Unavailable"
                        else -> if (annualBilling) {
                            if (isArabic) "اشترك سنوياً" else "Subscribe yearly"
                        } else {
                            if (isArabic) "اشترك شهرياً" else "Subscribe monthly"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                )
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PricingFeatureRow(
                        text = if (isArabic) "مستندات شهرية: ${plan.monthlyDocumentLimit}" else "Documents/month: ${plan.monthlyDocumentLimit}",
                        accent = accent,
                        textColor = if (isCurrent && isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PricingFeatureRow(
                        text = if (isArabic) "عمليات AI شهرية: ${plan.monthlyAiLimit}" else "AI/month: ${plan.monthlyAiLimit}",
                        accent = accent,
                        textColor = if (isCurrent && isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PricingFeatureRow(
                        text = if (plan.customerLimit == null) {
                            if (isArabic) "العملاء: غير محدود" else "Customers: Unlimited"
                        } else {
                            if (isArabic) "العملاء: ${plan.customerLimit}" else "Customers: ${plan.customerLimit}"
                        },
                        accent = accent,
                        textColor = if (isCurrent && isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PricingFeatureRow(
                        text = if (plan.productLimit == null) {
                            if (isArabic) "المنتجات: غير محدود" else "Products: Unlimited"
                        } else {
                            if (isArabic) "المنتجات: ${plan.productLimit}" else "Products: ${plan.productLimit}"
                        },
                        accent = accent,
                        textColor = if (isCurrent && isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PricingFeatureRow(
                        text = if (isArabic) plan.templatesAr else plan.templatesEn,
                        accent = accent,
                        textColor = if (isCurrent && isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PricingFeatureRow(
                        text = if (isArabic) plan.supportAr else plan.supportEn,
                        accent = accent,
                        textColor = if (isCurrent && isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                }
            }
        }
    }
}

@Composable
private fun PricingCompactChip(text: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PricingFeatureRow(
    text: String,
    accent: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    included: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(color = accent.copy(alpha = 0.12f), shape = CircleShape, modifier = Modifier.size(22.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (included) Icons.Filled.Check else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (included) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (included) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

private fun pricingAnnualDiscountPercent(): Int {
    return 20
}

private fun pricingAnnualDiscountPercent(monthlyPriceCents: Int, annualPriceCents: Int): Int {
    if (monthlyPriceCents <= 0 || annualPriceCents <= 0) return 0
    return (((monthlyPriceCents * 12) - annualPriceCents) * 100f / (monthlyPriceCents * 12)).toInt()
}

@Composable
private fun PricingComparisonSection(isArabic: Boolean) {
    val title = if (isArabic) "مقارنة كاملة" else "Full comparison"
    val groups = listOf(
        ComparisonGroup(
            titleAr = "الاستخدام والحدود",
            titleEn = "Usage and limits",
            rows = listOf(
                ComparisonFeatureRow(
                    featureAr = "المستندات الشهرية",
                    featureEn = "Monthly documents",
                    freeAr = "5",
                    freeEn = "5",
                    starterAr = "50",
                    starterEn = "50",
                    proAr = "200",
                    proEn = "200",
                    businessAr = "300",
                    businessEn = "300",
                ),
                ComparisonFeatureRow(
                    featureAr = "عمليات الذكاء الاصطناعي الشهرية",
                    featureEn = "Monthly AI uses",
                    freeAr = "10",
                    freeEn = "10",
                    starterAr = "150",
                    starterEn = "150",
                    proAr = "500",
                    proEn = "500",
                    businessAr = "800",
                    businessEn = "800",
                ),
            ),
        ),
        ComparisonGroup(
            titleAr = "العملاء والمنتجات",
            titleEn = "Customers and products",
            rows = listOf(
                ComparisonFeatureRow(
                    featureAr = "العملاء",
                    featureEn = "Customers",
                    freeAr = "5",
                    freeEn = "5",
                    starterAr = "30",
                    starterEn = "30",
                    proAr = "غير محدود",
                    proEn = "Unlimited",
                    businessAr = "غير محدود",
                    businessEn = "Unlimited",
                ),
                ComparisonFeatureRow(
                    featureAr = "المنتجات",
                    featureEn = "Products",
                    freeAr = "5",
                    freeEn = "5",
                    starterAr = "30",
                    starterEn = "30",
                    proAr = "غير محدود",
                    proEn = "Unlimited",
                    businessAr = "غير محدود",
                    businessEn = "Unlimited",
                ),
            ),
        ),
        ComparisonGroup(
            titleAr = "المستندات والقوالب",
            titleEn = "Documents and templates",
            rows = listOf(
                ComparisonFeatureRow(
                    featureAr = "عروض الأسعار والفواتير",
                    featureEn = "Quotes and invoices",
                    freeAr = "متاح",
                    freeEn = "Included",
                    starterAr = "متاح",
                    starterEn = "Included",
                    proAr = "متاح",
                    proEn = "Included",
                    businessAr = "متاح",
                    businessEn = "Included",
                ),
                ComparisonFeatureRow(
                    featureAr = "قوالب المستندات",
                    featureEn = "Document templates",
                    freeAr = "قالب واحد",
                    freeEn = "One",
                    starterAr = "الكل",
                    starterEn = "All",
                    proAr = "الكل",
                    proEn = "All",
                    businessAr = "الكل",
                    businessEn = "All",
                ),
            ),
        ),
        ComparisonGroup(
            titleAr = "أدوات الذكاء الاصطناعي",
            titleEn = "AI tools",
            rows = listOf(
                ComparisonFeatureRow(
                    featureAr = "رد ذكي",
                    featureEn = "Smart Reply",
                    freeAr = "متاح",
                    freeEn = "Included",
                    starterAr = "متاح",
                    starterEn = "Included",
                    proAr = "متاح",
                    proEn = "Included",
                    businessAr = "متاح",
                    businessEn = "Included",
                ),
                ComparisonFeatureRow(
                    featureAr = "كابشن ذكي",
                    featureEn = "Smart Caption",
                    freeAr = "متاح",
                    freeEn = "Included",
                    starterAr = "متاح",
                    starterEn = "Included",
                    proAr = "متاح",
                    proEn = "Included",
                    businessAr = "متاح",
                    businessEn = "Included",
                ),
            ),
        ),
        ComparisonGroup(
            titleAr = "النسخ الاحتياطي والاستعادة",
            titleEn = "Backup and restore",
            rows = listOf(
                ComparisonFeatureRow(
                    featureAr = "نسخ محلي تلقائي يومي أو أسبوعي",
                    featureEn = "Daily or weekly automatic local backup",
                    freeAr = "مقفل",
                    freeEn = "Locked",
                    starterAr = "متاح",
                    starterEn = "Included",
                    proAr = "متاح",
                    proEn = "Included",
                    businessAr = "متاح",
                    businessEn = "Included",
                ),
                ComparisonFeatureRow(
                    featureAr = "Google Drive",
                    featureEn = "Google Drive",
                    freeAr = "مقفل",
                    freeEn = "Locked",
                    starterAr = "متاح",
                    starterEn = "Included",
                    proAr = "متاح",
                    proEn = "Included",
                    businessAr = "متاح",
                    businessEn = "Included",
                ),
            ),
        ),
        ComparisonGroup(
            titleAr = "الدعم والحساب",
            titleEn = "Support and account",
            rows = listOf(
                ComparisonFeatureRow(
                    featureAr = "مستوى الدعم",
                    featureEn = "Support level",
                    freeAr = "ذاتي",
                    freeEn = "Self-service",
                    starterAr = "قياسي",
                    starterEn = "Standard",
                    proAr = "قياسي",
                    proEn = "Standard",
                    businessAr = "أولوية",
                    businessEn = "Priority",
                ),
            ),
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ComparisonTableHeader(isArabic)

                groups.forEachIndexed { index, group ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    PricingComparisonGroup(
                        isArabic = isArabic,
                        group = group,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonTableHeader(isArabic: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ComparisonCell(
            text = if (isArabic) "الميزة" else "Feature",
            column = ComparisonColumn.FEATURE,
            isHeader = true,
            modifier = Modifier.weight(1.5f),
        )
        ComparisonCell(
            text = if (isArabic) "مجاني" else "Free",
            column = ComparisonColumn.FREE,
            isHeader = true,
            modifier = Modifier.weight(1f),
        )
        ComparisonCell(
            text = if (isArabic) "مبتدئ" else "Starter",
            column = ComparisonColumn.STARTER,
            isHeader = true,
            modifier = Modifier.weight(1f),
        )
        ComparisonCell(
            text = if (isArabic) "احترافي" else "Pro",
            column = ComparisonColumn.PRO,
            isHeader = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PricingComparisonGroup(
    isArabic: Boolean,
    group: ComparisonGroup,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (isArabic) group.titleAr else group.titleEn,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        group.rows.forEach { row ->
            PricingComparisonFeatureRow(
                isArabic = isArabic,
                row = row,
            )
        }
    }
}

@Composable
private fun PricingComparisonFeatureRow(
    isArabic: Boolean,
    row: ComparisonFeatureRow,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ComparisonCell(
            text = if (isArabic) row.featureAr else row.featureEn,
            column = ComparisonColumn.FEATURE,
            modifier = Modifier.weight(1.5f),
        )
        ComparisonCell(
            text = if (isArabic) row.freeAr else row.freeEn,
            column = ComparisonColumn.FREE,
            modifier = Modifier.weight(1f),
        )
        ComparisonCell(
            text = if (isArabic) row.starterAr else row.starterEn,
            column = ComparisonColumn.STARTER,
            modifier = Modifier.weight(1f),
        )
        ComparisonCell(
            text = if (isArabic) row.proAr else row.proEn,
            column = ComparisonColumn.PRO,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ComparisonCell(
    text: String,
    column: ComparisonColumn,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
) {
    val isDarkTheme = AppRuntimeState.isDarkMode
    val accent = when (column) {
        ComparisonColumn.FEATURE -> Color(0xFF0F172A)
        ComparisonColumn.FREE -> Color(0xFF0D9488)
        ComparisonColumn.STARTER -> Color(0xFF7C3AED)
        ComparisonColumn.PRO -> Color(0xFF2563EB)
        ComparisonColumn.BUSINESS -> Color(0xFFEA580C)
    }
    val containerColor = when {
        isHeader -> accent
        column == ComparisonColumn.FEATURE && isDarkTheme -> Color(0xFF1E293B)
        column == ComparisonColumn.FEATURE -> Color(0xFFF1F5F9)
        isDarkTheme -> accent.copy(alpha = 0.20f)
        else -> accent.copy(alpha = 0.10f)
    }
    val contentColor = when {
        isHeader -> Color.White
        column == ComparisonColumn.FEATURE && isDarkTheme -> Color.White
        column == ComparisonColumn.FEATURE -> Color(0xFF0F172A)
        else -> accent
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isHeader -> accent
                column == ComparisonColumn.FEATURE -> Color(0xFF94A3B8).copy(alpha = 0.45f)
                else -> accent.copy(alpha = if (isDarkTheme) 0.45f else 0.24f)
            },
        ),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = if (isHeader) 10.dp else 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                fontSize = if (isHeader) 10.sp else 11.sp,
                fontWeight = if (isHeader || column == ComparisonColumn.FEATURE) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
                color = contentColor,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
            )
        }
    }
}

private enum class ComparisonColumn {
    FEATURE,
    FREE,
    STARTER,
    PRO,
    BUSINESS,
}

private data class ComparisonGroup(
    val titleAr: String,
    val titleEn: String,
    val rows: List<ComparisonFeatureRow>,
)

private data class ComparisonFeatureRow(
    val featureAr: String,
    val featureEn: String,
    val freeAr: String,
    val freeEn: String,
    val starterAr: String,
    val starterEn: String,
    val proAr: String,
    val proEn: String,
    val businessAr: String,
    val businessEn: String,
)

@Composable
private fun PricingFaqSection(isArabic: Boolean) {
    val title = if (isArabic) "الأسئلة الشائعة" else "FAQ"
    val items = if (isArabic) {
        listOf(
            "متى تتجدد حدود الاستخدام؟" to
                "تتجدد حدود المستندات والذكاء الاصطناعي شهرياً حسب تاريخ بدء اشتراكك. وفي الاشتراك السنوي يتم الدفع سنوياً، بينما تتجدد حدود الاستخدام كل شهر حسب تاريخ بدء الاشتراك.",
            "هل تنتقل الحدود غير المستخدمة؟" to
                "لا. تبدأ كل دورة شهرية بالحد الكامل للخطة، ولا تُضاف إليها الحدود المتبقية من الدورة السابقة.",
            "ماذا يحدث عند ترقية الخطة؟" to
                "تُفعّل الخطة الجديدة بعد تأكيد Google Play، وتزداد حدودك مباشرة دون تصفير استخدامك الحالي.",
            "ماذا يحدث عند الوصول إلى الحد؟" to
                "يتوقف إنشاء العمليات الجديدة التي وصلت إلى حدها، مع بقاء بياناتك ومستنداتك السابقة محفوظة وقابلة للعرض.",
            "ماذا يحدث عند إلغاء أو انتهاء الاشتراك؟" to
                "يستمر اشتراكك حتى نهاية الفترة المدفوعة. بعد انتهائها يعود الحساب إلى الخطة المجانية، وتخضع العمليات الجديدة لحدودها.",
            "دفعت ولم تظهر خطتي، ماذا أفعل؟" to
                "يحدّث التطبيق خطتك تلقائياً بعد الدفع. وإذا لم تظهر، استخدم خيار مزامنة الاشتراك من إعدادات الحساب، دون خصم مبلغ جديد.",
            "هل أفقد بياناتي عند انتهاء الاشتراك؟" to
                "لا. تبقى مستنداتك وعملاؤك ومنتجاتك محفوظة، لكن إنشاء عمليات جديدة يخضع لحدود الخطة الحالية.",
            "كيف أدير أو ألغي اشتراكي؟" to
                "يمكنك إدارة الاشتراك وإلغاء التجديد التلقائي من قسم الاشتراكات داخل Google Play. يستمر اشتراكك حتى نهاية الفترة المدفوعة.",
        )
    } else {
        listOf(
            "When do usage limits reset?" to
                "Document and AI limits reset monthly based on your subscription start date. Annual plans are billed yearly, while usage limits still reset every month based on that start date.",
            "Do unused limits roll over?" to
                "No. Every monthly cycle starts with the plan's full allowance, and unused limits are not added to the next cycle.",
            "What happens when I upgrade?" to
                "The new plan is activated after Google Play confirms the purchase. Your limits increase immediately without resetting your current usage.",
            "What happens when I reach a limit?" to
                "New actions for the exhausted allowance are blocked, while your existing data and documents remain available to view.",
            "What happens when I cancel or my subscription expires?" to
                "Your paid plan remains active until the end of the paid period. After that, the account returns to Free and new actions follow Free limits.",
            "I paid, but my plan did not appear. What should I do?" to
                "The app refreshes your plan automatically after payment. If it still does not appear, use Sync subscription in account settings. This does not charge you again.",
            "Will I lose my data when the subscription ends?" to
                "No. Your documents, customers, and products remain saved. New actions follow the limits of your current plan.",
            "How do I manage or cancel my subscription?" to
                "Manage the subscription or turn off auto-renewal in Google Play subscriptions. Your paid access continues until the end of the paid period.",
        )
    }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        items.forEachIndexed { index, (question, answer) ->
            val expanded = expandedIndex == index
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedIndex = if (expanded) null else index
                    },
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (expanded) {
                        Text(
                            text = answer,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 21.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(34.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

private data class SettingsSelectionOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsSelectionBottomSheet(
    title: String,
    selected: T,
    options: List<SettingsSelectionOption<T>>,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            options.forEach { option ->
                val isSelected = option.value == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                        .clickable { onSelect(option.value) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    option.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = option.label,
                        modifier = Modifier.weight(1f),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CompactSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    showIconContainer: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showIconContainer) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                title,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(icon: ImageVector, title: String, trailing: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Text(title, fontWeight = FontWeight.Bold)
            }
            trailing()
        }
    }
}

@Composable
private fun UsageLine(title: String, used: Int, limit: Int?, icon: ImageVector, color: Color) {
    val language = LocalLanguage.current
    val isUnlimited = limit == null
    val progress = if (limit == null || limit <= 0) 0f else (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(
                text = when {
                    isUnlimited -> if (language == AppLanguage.AR) "$used من غير محدود" else "$used of unlimited"
                    limit <= 0 -> "$used"
                    else -> "$used ${t("of")} $limit"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        LinearProgressIndicator(
            progress = { progress }, 
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)), 
            color = Color(0xFF2DD4BF), 
            trackColor = Color.White.copy(alpha = 0.18f)
        )
    }
}

@Composable
private fun PlanCard(
    title: String, 
    subtitle: String, 
    features: List<String>,
    badgeColor: Color = Color(0xFFE6FFFA),
    badgeTextColor: Color = Color(0xFF0F766E),
    border: BorderStroke? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(24.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Surface(
                        color = badgeColor, 
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = title, 
                            color = badgeTextColor, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = subtitle, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color(0xFF081C36)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            features.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFE6FFFA), 
                        shape = CircleShape, 
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome, 
                                contentDescription = null, 
                                tint = Color(0xFF0D9488), 
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = it, 
                        fontWeight = FontWeight.Medium, 
                        fontSize = 14.sp, 
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    dataViewModel: app.tijario.ui.state.TijarioDataViewModel,
    onBack: () -> Unit
) {
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    val defaultSubject = t("feedback_subject_general")
    val feedbackSuccessMessage = t("feedback_success")
    val feedbackErrorMessage = t("feedback_error")

    var selectedSubject by remember(defaultSubject) { mutableStateOf(defaultSubject) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            selectedImages = (selectedImages + uris).take(3)
        }
    }

    val subjects = listOf(
        t("feedback_subject_general"),
        t("feedback_subject_problem"),
        t("feedback_subject_feature"),
        t("feedback_subject_billing"),
        t("feedback_subject_other"),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("feedback_title"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSubmitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = t("feedback_heading"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = t("feedback_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Subject Selector
            Text(
                text = if (isArabic) "الموضوع" else "Subject",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = showSubjectDropdown,
                    onExpandedChange = { if (!isSubmitting) showSubjectDropdown = !showSubjectDropdown }
                ) {
                    TijarioTextField(
                        label = "",
                        value = selectedSubject,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubjectDropdown)
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false }
                    ) {
                        for (subject in subjects) {
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    selectedSubject = subject
                                    showSubjectDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Message Body
            Text(
                text = if (isArabic) "الرسالة" else "Message",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = messageText,
                onValueChange = { if (it.length <= 4000) messageText = it },
                placeholder = { Text(t("feedback_message_placeholder")) },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                text = "${messageText.length}/4000",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )

            // Image Attachments Selector
            Text(
                text = if (isArabic) "الصور (اختياري)" else "Images (Optional)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedImages.size < 3) {
                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .clickable(enabled = !isSubmitting) { imagePicker.launch("image/*") },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = t("feedback_add_image"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = t("feedback_add_image"),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                selectedImages.forEachIndexed { index, uri ->
                    Box(modifier = Modifier.size(72.dp)) {
                        val bitmapState = produceState<android.graphics.Bitmap?>(initialValue = null, key1 = uri) {
                            value = try {
                                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                                android.graphics.ImageDecoder.decodeBitmap(source)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        bitmapState.value?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(20.dp)
                                .background(Color.Red, CircleShape)
                                .clickable(enabled = !isSubmitting) { selectedImages = selectedImages.toMutableList().apply { removeAt(index) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = if (isArabic) "أضف حتى 3 صور، بحجم أقصى 5 ميجابايت لكل صورة." else "Add up to 3 images, max 5MB per image.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (messageText.isBlank()) return@Button
                    isSubmitting = true
                    coroutineScope.launch {
                        val feedbackImages = withContext(Dispatchers.IO) {
                            selectedImages.mapNotNull { uri ->
                                runCatching {
                                    app.tijario.data.remote.FeedbackImageEncoder.encode(context.contentResolver, uri)
                                }.getOrNull()
                            }
                        }
                        
                        val result = dataViewModel.submitUserFeedback(
                            subject = selectedSubject,
                            message = messageText,
                            images = feedbackImages
                        )
                        
                        isSubmitting = false
                        if (result.isSuccess) {
                            android.widget.Toast.makeText(
                                context,
                                feedbackSuccessMessage,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            onBack()
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                feedbackErrorMessage,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                enabled = !isSubmitting && messageText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0FA36E))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = t("feedback_title"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
