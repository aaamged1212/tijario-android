package app.tijario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import android.net.Uri
import app.tijario.config.AppRuntimeState
import app.tijario.config.loadAppConfig
import app.tijario.config.t
import app.tijario.ui.screens.AccountScreen
import app.tijario.features.ai.AiToolsScreen
import app.tijario.ui.screens.BusinessSettingsScreen
import app.tijario.ui.screens.CustomerFormScreen
import app.tijario.ui.screens.ConfigurationRequiredScreen
import app.tijario.ui.screens.CustomersScreen
import app.tijario.ui.screens.DashboardScreen
import app.tijario.ui.screens.DocumentFormScreen
import app.tijario.ui.screens.DocumentsScreen
import app.tijario.ui.screens.ForgotPasswordScreen
import app.tijario.ui.screens.LoginScreen
import app.tijario.ui.screens.OnboardingScreen
import app.tijario.ui.screens.RegisterScreen
import app.tijario.ui.screens.VerifyEmailScreen
import app.tijario.ui.screens.ProductsScreen
import app.tijario.ui.screens.ProductFormScreen
import app.tijario.ui.screens.AccountSettingsScreen
import app.tijario.ui.screens.AppSettingsScreen
import app.tijario.ui.screens.ChangePasswordScreen
import app.tijario.ui.screens.IntroWalkthroughScreen
import app.tijario.ui.screens.DocumentDetailScreen
import app.tijario.ui.screens.SettingsHomeScreen
import app.tijario.ui.screens.BackupSettingsScreen
import app.tijario.ui.screens.UpgradePlanScreen
import app.tijario.ui.state.TijarioDataViewModel
import app.tijario.ui.state.TijarioDataViewModelFactory
import app.tijario.ui.state.AuthViewModel
import app.tijario.ui.state.AuthViewModelFactory
import app.tijario.ui.state.CentralAuthState
import app.tijario.features.notifications.NotificationBellButton
import app.tijario.features.notifications.NotificationDeepLinkState
import app.tijario.features.notifications.NotificationPermissionPrompt
import app.tijario.features.notifications.NotificationsScreen
import app.tijario.features.notifications.NotificationsViewModel
import app.tijario.features.notifications.NotificationsViewModelFactory
import app.tijario.features.notifications.StartupAnnouncementDialog
import app.tijario.config.AppPreferences
import app.tijario.domain.LocalizedErrorMapper
import app.tijario.domain.CreationAllowance
import app.tijario.domain.CreationTarget
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.activity.compose.BackHandler
import app.tijario.ui.components.LocalAdaptiveLayoutInfo
import app.tijario.ui.components.ProvideAdaptiveLayout
import io.github.jan.supabase.auth.auth

private data class RootTab(
    val route: String,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
)

private sealed interface AccountDeletionRecoveryState {
    data object Running : AccountDeletionRecoveryState
    data class Succeeded(val recoveredPendingDeletion: Boolean) : AccountDeletionRecoveryState
    data object Failed : AccountDeletionRecoveryState
}

internal fun shouldFallbackToMainAfterBack(popSucceeded: Boolean): Boolean = !popSucceeded

private fun NavHostController.safePopBackToMain() {
    if (shouldFallbackToMainAfterBack(popBackStack())) {
        navigate("main") { launchSingleTop = true }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun creationAllowanceMessage(
    allowance: CreationAllowance,
    language: app.tijario.config.AppLanguage,
): String = when (allowance) {
    is CreationAllowance.LimitReached -> when (allowance.target) {
        CreationTarget.Customer -> if (language == app.tijario.config.AppLanguage.AR) {
            "استخدمت ${allowance.used} من ${allowance.limit} عملاء في خطتك الحالية. رقِّ خطتك لإضافة المزيد من العملاء."
        } else {
            "You have used ${allowance.used} of ${allowance.limit} customers on your current plan. Upgrade to add more customers."
        }
        CreationTarget.Product -> if (language == app.tijario.config.AppLanguage.AR) {
            "استخدمت ${allowance.used} من ${allowance.limit} منتجات في خطتك الحالية. رقِّ خطتك لإضافة المزيد من المنتجات."
        } else {
            "You have used ${allowance.used} of ${allowance.limit} products on your current plan. Upgrade to add more products."
        }
        CreationTarget.Invoice,
        CreationTarget.Quote,
        -> if (language == app.tijario.config.AppLanguage.AR) {
            "وصلت إلى حد المستندات في خطتك الحالية. رقِّ خطتك لإنشاء المزيد من الفواتير وعروض الأسعار."
        } else {
            "You have reached the document limit on your current plan. Upgrade to create more invoices and quotes."
        }
    }
    CreationAllowance.PlanUnavailable -> if (language == app.tijario.config.AppLanguage.AR) {
        "تعذر تحميل بيانات خطتك الآن. تحقق من الاتصال ثم حاول مجددًا."
    } else {
        "Your plan details are not available yet. Check your connection and try again."
    }
    CreationAllowance.Retryable -> if (language == app.tijario.config.AppLanguage.AR) {
        "تعذر التحقق من الحد الآن. حاول مرة أخرى."
    } else {
        "We could not check this limit right now. Please try again."
    }
    CreationAllowance.Allowed -> ""
}

private val rootTabs = listOf(
    RootTab("dashboard", "tab_home", Icons.Filled.Home, Icons.Outlined.Home),
    RootTab("documents", "tab_documents", Icons.Filled.Description, Icons.Outlined.Description),
    RootTab("ai", "tab_ai", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    RootTab("products", "tab_products", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    RootTab("customers", "tab_customers", Icons.Filled.People, Icons.Outlined.People),
)

@Composable
fun TijarioApp() {
    ProvideAdaptiveLayout {
        TijarioAppContent()
    }
}

@Composable
private fun TijarioAppContent() {
    val config = loadAppConfig()
    if (!config.isComplete) {
        ConfigurationRequiredScreen()
        return
    }

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext)
    )
    val dataViewModel: TijarioDataViewModel = viewModel(
        factory = TijarioDataViewModelFactory(context.applicationContext)
    )
    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(context.applicationContext)
    )

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val appShellState by dataViewModel.appShellState.collectAsStateWithLifecycle()
    val notificationsState by notificationsViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var accountDeletionRecovery by remember {
        mutableStateOf<AccountDeletionRecoveryState>(AccountDeletionRecoveryState.Running)
    }
    val showStartupSplash =
        authState is CentralAuthState.Initializing ||
            (authState is CentralAuthState.AuthenticatedReady &&
                appShellState.isInitialLoading &&
                !appShellState.hasCachedData)

    // Shared states for selection
    var activeSelectedCustomer by remember { mutableStateOf<app.tijario.data.model.Customer?>(null) }
    var activeSelectedProduct by remember { mutableStateOf<app.tijario.data.model.Product?>(null) }
    var requestedDocumentsType by remember { mutableStateOf<app.tijario.data.model.DocumentType?>(null) }
    var activeSelectedProductRowIndex by remember { mutableStateOf<Int?>(null) }

    suspend fun recoverPendingAccountDeletionCleanup() {
        val pendingUserId = AppPreferences.pendingAccountDeletionCleanupUserId(context)
        if (pendingUserId.isNullOrBlank()) {
            accountDeletionRecovery = AccountDeletionRecoveryState.Succeeded(recoveredPendingDeletion = false)
            return
        }

        if (dataViewModel.deleteAccountLocal(pendingUserId).isFailure) {
            accountDeletionRecovery = AccountDeletionRecoveryState.Failed
            return
        }

        try {
            authViewModel.clearLocalSession(pendingUserId)
            AppPreferences.clearPendingAccountDeletionCleanup(context, pendingUserId)
            accountDeletionRecovery = AccountDeletionRecoveryState.Succeeded(recoveredPendingDeletion = true)
        } catch (_: Exception) {
            // Keep the marker and block authenticated routing until local cleanup can be retried.
            accountDeletionRecovery = AccountDeletionRecoveryState.Failed
        }
    }

    LaunchedEffect(Unit) {
        recoverPendingAccountDeletionCleanup()
    }

    // Start data sync only after a pending account-deletion cleanup has recovered.
    val accountDeletionRecoverySucceeded = accountDeletionRecovery is AccountDeletionRecoveryState.Succeeded
    val recoveredPendingAccountDeletion =
        (accountDeletionRecovery as? AccountDeletionRecoveryState.Succeeded)?.recoveredPendingDeletion == true
    LaunchedEffect(authState, accountDeletionRecoverySucceeded, recoveredPendingAccountDeletion) {
        if (!accountDeletionRecoverySucceeded) return@LaunchedEffect
        if (authState is CentralAuthState.AuthenticatedReady || authState is CentralAuthState.AuthenticatedNeedsOnboarding) {
            dataViewModel.startForCurrentUser()
        } else if (authState is CentralAuthState.Unauthenticated && !recoveredPendingAccountDeletion) {
            notificationsViewModel.logout()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authState, accountDeletionRecoverySucceeded, recoveredPendingAccountDeletion) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_RESUME &&
                accountDeletionRecoverySucceeded &&
                !recoveredPendingAccountDeletion &&
                (authState is CentralAuthState.AuthenticatedReady ||
                    authState is CentralAuthState.AuthenticatedNeedsOnboarding)
            ) {
                dataViewModel.refreshPlanUsage(force = false)
                notificationsViewModel.syncTopic(AppRuntimeState.currentLanguage)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (accountDeletionRecovery) {
        AccountDeletionRecoveryState.Running -> {
            SplashScreen()
            return
        }
        AccountDeletionRecoveryState.Failed -> {
            AccountDeletionRecoveryFailedScreen(
                onRetry = {
                    scope.launch {
                        accountDeletionRecovery = AccountDeletionRecoveryState.Running
                        recoverPendingAccountDeletionCleanup()
                    }
                },
            )
            return
        }
        is AccountDeletionRecoveryState.Succeeded -> Unit
    }

    if (showStartupSplash) {
        SplashScreen()
        return
    }

    when (val state = authState) {
        is CentralAuthState.Initializing -> Unit
        is CentralAuthState.Unauthenticated, is CentralAuthState.AwaitingEmailVerification -> {
            // Unauthenticated Graph
            val navController = rememberNavController()
            val authDeepLinkTarget = AppRuntimeState.authDeepLinkTarget
            val initialAuthRoute = when {
                state is CentralAuthState.AwaitingEmailVerification -> "verify-email"
                authDeepLinkTarget == "/login" -> "login"
                else -> "intro"
            }

            LaunchedEffect(authDeepLinkTarget) {
                if (authDeepLinkTarget == "/login") {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                    AppRuntimeState.consumeAuthDeepLinkTarget()
                }
            }

            NavHost(
                navController = navController,
                startDestination = initialAuthRoute,
            ) {
                composable("intro") {
                    IntroWalkthroughScreen(
                        onFinished = {
                            navController.navigate("login") {
                                popUpTo("intro") { inclusive = true }
                            }
                        }
                    )
                }
                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onRegister = { navController.navigate("register") },
                        onForgotPassword = { navController.navigate("forgot-password") }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        onBackToLogin = { navController.popBackStack() },
                        onVerifyEmail = { email ->
                            authViewModel.setAwaitingVerification()
                            navController.navigate("verify-email?email=${Uri.encode(email)}")
                        }
                    )
                }
                composable(
                    route = "verify-email?email={email}",
                    arguments = listOf(
                        navArgument("email") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email").orEmpty()
                    VerifyEmailScreen(
                        email = email,
                        authViewModel = authViewModel,
                        onBackToLogin = {
                            authViewModel.logout()
                        },
                        onVerified = {
                            authViewModel.handleVerificationSuccess()
                        }
                    )
                }
                composable("verify-email") {
                    VerifyEmailScreen(
                        email = "",
                        authViewModel = authViewModel,
                        onBackToLogin = {
                            authViewModel.logout()
                        },
                        onVerified = {
                            authViewModel.handleVerificationSuccess()
                        }
                    )
                }
                composable("forgot-password") {
                    ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
                }
            }
        }
        is CentralAuthState.AuthenticatedNeedsOnboarding -> {
            OnboardingScreen(
                dataViewModel = dataViewModel,
                onDone = {
                    scope.launch {
                        authViewModel.checkCurrentSession()
                    }
                }
            )
        }
        is CentralAuthState.AuthenticatedReady -> {
            // Authenticated Graph
            val navController = rememberNavController()
            val pagerState = rememberPagerState(pageCount = { 5 })
            val pagerScope = rememberCoroutineScope()
            var creationAllowance by remember { mutableStateOf<CreationAllowance?>(null) }
            var showNotificationPrompt by remember {
                mutableStateOf(!AppPreferences.wasNotificationExplained(context))
            }

            fun requestCreation(target: CreationTarget, route: String) {
                scope.launch {
                    when (val allowance = dataViewModel.creationAllowance(target)) {
                        CreationAllowance.Allowed -> navController.navigateSingleTop(route)
                        else -> creationAllowance = allowance
                    }
                }
            }

            creationAllowance?.let { allowance ->
                AlertDialog(
                    onDismissRequest = { creationAllowance = null },
                    title = {
                        Text(
                            if (allowance is CreationAllowance.LimitReached) t("limit_reached_title")
                            else t("billing_plan_refresh_failed"),
                        )
                    },
                    text = { Text(creationAllowanceMessage(allowance, AppRuntimeState.currentLanguage)) },
                    confirmButton = {
                        if (allowance is CreationAllowance.LimitReached) {
                            Button(
                                onClick = {
                                    creationAllowance = null
                                    navController.navigateSingleTop("upgrade-plan")
                                },
                            ) { Text(t("upgrade_plan")) }
                        } else {
                            Button(onClick = { creationAllowance = null }) { Text(t("btn_ok")) }
                        }
                    },
                    dismissButton = {
                        if (allowance is CreationAllowance.LimitReached) {
                            TextButton(onClick = { creationAllowance = null }) { Text(t("btn_cancel")) }
                        }
                    },
                )
            }

            LaunchedEffect(appShellState.userId) {
                appShellState.userId?.let { notificationsViewModel.start(it) }
            }

            LaunchedEffect(appShellState.userId, AppRuntimeState.currentLanguage) {
                notificationsViewModel.syncTopic(AppRuntimeState.currentLanguage)
            }

            val pendingAnnouncementId = NotificationDeepLinkState.pendingAnnouncementId
            LaunchedEffect(pendingAnnouncementId) {
                if (!pendingAnnouncementId.isNullOrBlank()) {
                    navController.navigate("notifications?announcementId=$pendingAnnouncementId")
                    NotificationDeepLinkState.consumeAnnouncementId()
                }
            }

            notificationsState.startupAnnouncement?.let { startup ->
                StartupAnnouncementDialog(
                    announcement = startup,
                    language = AppRuntimeState.currentLanguage,
                    onViewDetails = {
                        notificationsViewModel.markRead(startup.id, "startup")
                        notificationsViewModel.clearStartup()
                        navController.navigate("notifications?announcementId=${startup.id}")
                    },
                    onDismiss = {
                        notificationsViewModel.dismissStartup(startup.id)
                    }
                )
            }

            if (showNotificationPrompt) {
                NotificationPermissionPrompt(
                    onFinished = {
                        AppPreferences.setPushEnabled(context, true)
                        showNotificationPrompt = false
                        notificationsViewModel.syncTopic(AppRuntimeState.currentLanguage)
                    }
                )
            }

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    val adaptive = LocalAdaptiveLayoutInfo.current
                    val layoutDirection = LocalLayoutDirection.current
                    BackHandler(enabled = pagerState.currentPage != 0) {
                        pagerScope.launch { pagerState.animateScrollToPage(0) }
                    }
                    Scaffold(
                        topBar = {
                            val currentPage = pagerState.currentPage
                            val titleText = when (currentPage) {
                                0 -> t("tab_home")
                                1 -> t("documents_title")
                                2 -> t("tab_ai")
                                3 -> t("tab_products")
                                else -> t("customers_title")
                            }
                            val pageIcon = when (currentPage) {
                                0 -> Icons.Filled.Home
                                1 -> Icons.Filled.Description
                                2 -> Icons.Filled.AutoAwesome
                                3 -> Icons.Filled.ShoppingBag
                                else -> Icons.Filled.People
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.background,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = adaptive.pageHorizontalPadding,
                                            vertical = if (adaptive.isExtraCompact) 8.dp else 12.dp,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(if (adaptive.isExtraCompact) 6.dp else 10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = pageIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(adaptive.iconSize)
                                        )
                                        Text(
                                            text = titleText,
                                            fontSize = adaptive.titleFontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        NotificationBellButton(
                                            unreadCount = notificationsState.unreadCount,
                                            onClick = { navController.navigate("notifications") },
                                        )
                                        IconButton(
                                            onClick = { navController.navigateSingleTop("settings") },
                                            modifier = Modifier
                                                .size(if (adaptive.isExtraCompact) 40.dp else 44.dp)
                                                .clip(CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = t("settings"),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            val selectedNavAccent = if (AppRuntimeState.isDarkMode) Color(0xFF14B8A6) else Color(0xFF0D9488)
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (adaptive.isExtraCompact) 64.dp else 72.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                rootTabs.forEachIndexed { index, tab ->
                                    val selected = pagerState.currentPage == index
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            pagerScope.launch {
                                                pagerState.scrollToPage(index)
                                            }
                                        },
                                        icon = {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                if (selected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(20.dp)
                                                            .height(3.dp)
                                                            .clip(RoundedCornerShape(1.5.dp))
                                                            .background(selectedNavAccent)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                } else {
                                                    Spacer(modifier = Modifier.height(7.dp))
                                                }
                                                Icon(
                                                    imageVector = if (selected) tab.iconFilled else tab.iconOutlined,
                                                    contentDescription = t(tab.label),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        },
                                        label = if (adaptive.showInactiveBottomLabels || selected) {
                                            {
                                            Text(
                                                text = if (adaptive.isCompact && tab.label == "tab_documents") {
                                                    t("tab_documents_short")
                                                } else {
                                                    t(tab.label)
                                                },
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = when {
                                                    adaptive.isExtraCompact -> 9.sp
                                                    adaptive.isCompact -> 10.sp
                                                    else -> 11.sp
                                                },
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            }
                                        } else null,
                                        alwaysShowLabel = adaptive.showInactiveBottomLabels,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = selectedNavAccent,
                                            selectedTextColor = selectedNavAccent,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(
                                    start = paddingValues.calculateStartPadding(layoutDirection),
                                    top = paddingValues.calculateTopPadding(),
                                    end = paddingValues.calculateEndPadding(layoutDirection),
                                    bottom = 0.dp,
                                )
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true
                            ) { page ->
                                when (page) {
                                    0 -> DashboardScreen(
                                        dataViewModel = dataViewModel,
                                        onNewQuote = {
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            requestCreation(CreationTarget.Quote, "new-quote")
                                        },
                                        onNewInvoice = {
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            requestCreation(CreationTarget.Invoice, "new-invoice")
                                        },
                                        onAddProduct = { requestCreation(CreationTarget.Product, "product-form") },
                                        onCustomers = { pagerScope.launch { pagerState.scrollToPage(4) } },
                                        onAiTools = { pagerScope.launch { pagerState.scrollToPage(2) } },
                                        onBusinessSettings = { navController.navigate("business-settings") },
                                        onViewAllDocuments = { type ->
                                            requestedDocumentsType = type
                                            pagerScope.launch { pagerState.scrollToPage(1) }
                                        },
                                        onDocumentClick = { documentId ->
                                            navController.navigate("document-detail?documentId=$documentId")
                                        },
                                        hideHeader = true
                                    )
                                    1 -> DocumentsScreen(
                                        dataViewModel = dataViewModel,
                                        onNewQuote = {
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            requestCreation(CreationTarget.Quote, "new-quote")
                                        },
                                        onNewInvoice = {
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            requestCreation(CreationTarget.Invoice, "new-invoice")
                                        },
                                        onDocumentClick = { documentId ->
                                            navController.navigate("document-detail?documentId=$documentId")
                                        },
                                        onEditDocument = { documentId, type ->
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            val route = if (type == app.tijario.data.model.DocumentType.Invoice) "edit-invoice" else "edit-quote"
                                            navController.navigate("$route?documentId=$documentId")
                                        },
                                        requestedDocumentType = requestedDocumentsType,
                                        hideHeader = true
                                    )
                                    2 -> AiToolsScreen(dataViewModel = dataViewModel, hideHeader = true)
                                    3 -> ProductsScreen(
                                        dataViewModel = dataViewModel,
                                        onCreateProduct = { requestCreation(CreationTarget.Product, "product-form") },
                                        onEditProduct = { id -> navController.navigate("product-form?productId=$id") },
                                        hideHeader = true
                                    )
                                    4 -> CustomersScreen(
                                        dataViewModel = dataViewModel,
                                        onCreateCustomer = { requestCreation(CreationTarget.Customer, "customer-form") },
                                        onEditCustomer = { id -> navController.navigate("customer-form?customerId=$id") },
                                        hideHeader = true
                                    )
                                }
                            }
                        }
                    }
                }

                composable("customers") {
                    CustomersScreen(
                        dataViewModel = dataViewModel,
                        onCreateCustomer = { requestCreation(CreationTarget.Customer, "customer-form") },
                        onCustomerSelected = { customer ->
                            activeSelectedCustomer = customer
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "customer-form?customerId={customerId}",
                    arguments = listOf(
                        navArgument("customerId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getString("customerId")
                    CustomerFormScreen(
                        dataViewModel = dataViewModel,
                        customerId = customerId,
                        onBack = { navController.safePopBackToMain() },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable("customer-form") {
                    CustomerFormScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.safePopBackToMain() },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable("products") {
                    ProductsScreen(
                        dataViewModel = dataViewModel,
                        onCreateProduct = { requestCreation(CreationTarget.Product, "product-form") },
                        onProductSelected = { product ->
                            activeSelectedProduct = product
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "product-form?productId={productId}",
                    arguments = listOf(
                        navArgument("productId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")
                    ProductFormScreen(
                        dataViewModel = dataViewModel,
                        productId = productId,
                        onBack = { navController.safePopBackToMain() },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable("product-form") {
                    ProductFormScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.safePopBackToMain() },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable("business-settings") {
                    BusinessSettingsScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.safePopBackToMain() }
                    )
                }
                composable("new-quote") {
                    DocumentFormScreen(
                        dataViewModel = dataViewModel,
                        type = app.tijario.data.model.DocumentType.Quote,
                        onBack = { navController.safePopBackToMain() },
                        onDocumentSaved = { documentId ->
                            navController.navigate("document-detail?documentId=$documentId") {
                                popUpTo("new-quote") { inclusive = true }
                            }
                        },
                        onNavigateToSelectCustomer = { navController.navigate("customers") },
                        onNavigateToSelectProduct = { rowIndex ->
                            activeSelectedProductRowIndex = rowIndex
                            navController.navigate("products")
                        },
                        onNavigateToCreateCustomer = { requestCreation(CreationTarget.Customer, "customer-form") },
                        onNavigateToCreateProduct = { requestCreation(CreationTarget.Product, "product-form") },
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable("new-invoice") {
                    DocumentFormScreen(
                        dataViewModel = dataViewModel,
                        type = app.tijario.data.model.DocumentType.Invoice,
                        onBack = { navController.safePopBackToMain() },
                        onDocumentSaved = { documentId ->
                            navController.navigate("document-detail?documentId=$documentId") {
                                popUpTo("new-invoice") { inclusive = true }
                            }
                        },
                        onNavigateToSelectCustomer = { navController.navigate("customers") },
                        onNavigateToSelectProduct = { rowIndex ->
                            activeSelectedProductRowIndex = rowIndex
                            navController.navigate("products")
                        },
                        onNavigateToCreateCustomer = { requestCreation(CreationTarget.Customer, "customer-form") },
                        onNavigateToCreateProduct = { requestCreation(CreationTarget.Product, "product-form") },
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable(
                    route = "edit-quote?documentId={documentId}",
                    arguments = listOf(
                        navArgument("documentId") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
                    DocumentFormScreen(
                        dataViewModel = dataViewModel,
                        type = app.tijario.data.model.DocumentType.Quote,
                        documentId = documentId,
                        onBack = { navController.safePopBackToMain() },
                        onDocumentSaved = { savedDocumentId ->
                            navController.navigate("document-detail?documentId=$savedDocumentId") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSelectCustomer = { navController.navigate("customers") },
                        onNavigateToSelectProduct = { rowIndex ->
                            activeSelectedProductRowIndex = rowIndex
                            navController.navigate("products")
                        },
                        onNavigateToCreateCustomer = { requestCreation(CreationTarget.Customer, "customer-form") },
                        onNavigateToCreateProduct = { requestCreation(CreationTarget.Product, "product-form") },
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable(
                    route = "edit-invoice?documentId={documentId}",
                    arguments = listOf(
                        navArgument("documentId") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
                    DocumentFormScreen(
                        dataViewModel = dataViewModel,
                        type = app.tijario.data.model.DocumentType.Invoice,
                        documentId = documentId,
                        onBack = { navController.safePopBackToMain() },
                        onDocumentSaved = { savedDocumentId ->
                            navController.navigate("document-detail?documentId=$savedDocumentId") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSelectCustomer = { navController.navigate("customers") },
                        onNavigateToSelectProduct = { rowIndex ->
                            activeSelectedProductRowIndex = rowIndex
                            navController.navigate("products")
                        },
                        onNavigateToCreateCustomer = { requestCreation(CreationTarget.Customer, "customer-form") },
                        onNavigateToCreateProduct = { requestCreation(CreationTarget.Product, "product-form") },
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                    )
                }
                composable(
                    route = "document-detail?documentId={documentId}",
                    arguments = listOf(
                        navArgument("documentId") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
                    DocumentDetailScreen(
                        dataViewModel = dataViewModel,
                        documentId = documentId,
                        onBack = { navController.safePopBackToMain() },
                        onEditClick = { id, type ->
                            val route = if (type == app.tijario.data.model.DocumentType.Invoice) "edit-invoice?documentId=$id" else "edit-quote?documentId=$id"
                            navController.navigate(route)
                        }
                    )
                }
                composable("account") {
                    AccountScreen(
                        dataViewModel = dataViewModel,
                        onLogout = {
                            notificationsViewModel.logout()
                            authViewModel.logout()
                        },
                        onBack = { navController.safePopBackToMain() },
                        onChangePassword = { navController.navigate("change-password") },
                    )
                }
                composable("settings") {
                    SettingsHomeScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.safePopBackToMain() },
                        onStoreSettings = { navController.navigateSingleTop("business-settings") },
                        onAccountSettings = { navController.navigateSingleTop("account-settings") },
                        onAppSettings = { navController.navigateSingleTop("app-settings") },
                        onBackupSettings = { navController.navigateSingleTop("backup-settings") },
                        onUpgrade = { navController.navigateSingleTop("upgrade-plan") },
                        onLogout = {
                            notificationsViewModel.logout()
                            authViewModel.logout()
                        },
                    )
                }
                composable(
                    route = "notifications?announcementId={announcementId}",
                    arguments = listOf(
                        navArgument("announcementId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    NotificationsScreen(
                        viewModel = notificationsViewModel,
                        initialAnnouncementId = backStackEntry.arguments?.getString("announcementId"),
                        onBack = { navController.safePopBackToMain() },
                    )
                }
                composable("account-settings") {
                    AccountSettingsScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.safePopBackToMain() },
                        onChangePassword = { navController.navigate("change-password") },
                        onLogout = { authViewModel.logout() },
                        onDeleteAccount = {
                            val userId = dataViewModel.currentUserId() ?: ""
                            if (userId.isBlank()) {
                                Result.failure(IllegalStateException("account_delete_failed"))
                            } else {
                                val cleanupPending = AppPreferences.pendingAccountDeletionCleanupUserId(context) == userId
                                val deleteResponse = if (cleanupPending) null else {
                                    app.tijario.config.Supabase.apiClient.deleteAccount()
                                }
                                if (deleteResponse != null && !deleteResponse.ok) {
                                    Result.failure(IllegalStateException(deleteResponse.code ?: "account_delete_failed"))
                                } else {
                                    if (deleteResponse != null) {
                                        AppPreferences.markAccountDeletionCleanupPending(context, userId)
                                    }
                                    dataViewModel.deleteAccountLocal(userId).fold(
                                        onSuccess = {
                                            AppPreferences.clearPendingAccountDeletionCleanup(context, userId)
                                            Result.success(Unit)
                                        },
                                        onFailure = { error -> Result.failure(error) },
                                    )
                                }
                            }
                        }
                    )
                }
                composable("change-password") {
                    ChangePasswordScreen(
                        onBack = { navController.safePopBackToMain() },
                    )
                }
                composable("app-settings") {
                    AppSettingsScreen(onBack = { navController.safePopBackToMain() })
                }
                composable("backup-settings") {
                    BackupSettingsScreen(
                        userId = app.tijario.config.Supabase.client.auth.currentUserOrNull()?.id.orEmpty(),
                        onBack = { navController.safePopBackToMain() },
                    )
                }
                composable("upgrade-plan") {
                    UpgradePlanScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.safePopBackToMain() }
                    )
                }
            }
        }
        is CentralAuthState.Error -> {
            val errorMessage = when (state.message) {
                "فشل فحص حالة الجلسة" -> t("error_session_check_failed")
                "حدث خطأ أثناء فحص البيانات بعد التحقق." -> t("error_after_verification_check")
                else -> LocalizedErrorMapper.map(null, state.message, AppRuntimeState.currentLanguage)
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { authViewModel.checkCurrentSession() }) {
                        Text(t("retry"))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountDeletionRecoveryFailedScreen(onRetry: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = t("account_delete_failed"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = onRetry) {
                    Text(t("retry"))
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F766E),
                        Color(0xFF064E3B)
                    )
                )
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Image(
                        painter = painterResource(id = app.tijario.R.drawable.logo_app),
                        contentDescription = t("app_logo_desc"),
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }
            Text(
                text = t("app_name"),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = t("app_slogan"),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
