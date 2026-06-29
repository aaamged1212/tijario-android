package app.tijario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri
import app.tijario.MainActivity
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
import app.tijario.ui.screens.IntroWalkthroughScreen
import app.tijario.ui.screens.DocumentDetailScreen
import app.tijario.ui.screens.SettingsHomeScreen
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

private data class RootTab(
    val route: String,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
)

private val rootTabs = listOf(
    RootTab("dashboard", "tab_home", Icons.Filled.Home, Icons.Outlined.Home),
    RootTab("documents", "tab_documents", Icons.Filled.Description, Icons.Outlined.Description),
    RootTab("ai", "tab_ai", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    RootTab("products", "tab_products", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    RootTab("customers", "tab_customers", Icons.Filled.People, Icons.Outlined.People),
)

@Composable
fun TijarioApp() {
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
    val dataUiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val notificationsState by notificationsViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val showStartupSplash =
        authState is CentralAuthState.Initializing ||
            (authState is CentralAuthState.AuthenticatedReady &&
                dataUiState.isInitialLoading &&
                !dataUiState.hasCachedData)

    // Shared states for selection
    var activeSelectedCustomer by remember { mutableStateOf<app.tijario.data.model.Customer?>(null) }
    var activeSelectedProduct by remember { mutableStateOf<app.tijario.data.model.Product?>(null) }
    var activeSelectedProductRowIndex by remember { mutableStateOf<Int?>(null) }

    // Start data sync when authenticated
    LaunchedEffect(authState) {
        if (authState is CentralAuthState.AuthenticatedReady || authState is CentralAuthState.AuthenticatedNeedsOnboarding) {
            dataViewModel.startForCurrentUser()
        } else if (authState is CentralAuthState.Unauthenticated) {
            notificationsViewModel.logout()
        }
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
            NavHost(
                navController = navController,
                startDestination = if (state is CentralAuthState.AwaitingEmailVerification) "verify-email" else "intro"
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
            var showNotificationPrompt by remember {
                mutableStateOf(!AppPreferences.wasNotificationExplained(context))
            }

            LaunchedEffect(dataUiState.userId) {
                dataUiState.userId?.let { notificationsViewModel.start(it) }
            }

            LaunchedEffect(dataUiState.userId, MainActivity.currentLanguage) {
                notificationsViewModel.syncTopic(MainActivity.currentLanguage)
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
                    language = MainActivity.currentLanguage,
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
                        showNotificationPrompt = false
                        notificationsViewModel.syncTopic(MainActivity.currentLanguage)
                    }
                )
            }

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    Scaffold(
                        topBar = {
                            val currentPage = pagerState.currentPage
                            val titleText = when (currentPage) {
                                0 -> t("welcome")
                                1 -> t("documents_title")
                                2 -> t("ai_title")
                                3 -> t("products_title")
                                else -> t("customers_title")
                            }
                            val subtitleText = when (currentPage) {
                                0 -> t("dash_subtitle")
                                1 -> t("documents_subtitle")
                                2 -> t("ai_subtitle")
                                3 -> t("products_subtitle")
                                else -> t("customers_subtitle")
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
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = pageIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = subtitleText,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        NotificationBellButton(
                                            unreadCount = notificationsState.unreadCount,
                                            onClick = { navController.navigate("notifications") },
                                        )
                                        IconButton(
                                            onClick = { navController.navigate("settings") },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = t("settings"),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
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
                                                            .background(MaterialTheme.colorScheme.primary)
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
                                        label = {
                                            Text(
                                                text = t(tab.label),
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                .padding(paddingValues)
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
                                            navController.navigate("new-quote")
                                        },
                                        onNewInvoice = {
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            navController.navigate("new-invoice")
                                        },
                                        onAddProduct = { pagerScope.launch { pagerState.scrollToPage(3) } },
                                        onCustomers = { pagerScope.launch { pagerState.scrollToPage(4) } },
                                        onAiTools = { pagerScope.launch { pagerState.scrollToPage(2) } },
                                        onBusinessSettings = { navController.navigate("business-settings") },
                                        onViewAllDocuments = { pagerScope.launch { pagerState.scrollToPage(1) } },
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
                                            navController.navigate("new-quote")
                                        },
                                        onNewInvoice = {
                                            activeSelectedCustomer = null
                                            activeSelectedProduct = null
                                            navController.navigate("new-invoice")
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
                                        hideHeader = true
                                    )
                                    2 -> AiToolsScreen(dataViewModel = dataViewModel, hideHeader = true)
                                    3 -> ProductsScreen(
                                        dataViewModel = dataViewModel,
                                        onCreateProduct = { navController.navigate("product-form") },
                                        onEditProduct = { id -> navController.navigate("product-form?productId=$id") },
                                        hideHeader = true
                                    )
                                    4 -> CustomersScreen(
                                        dataViewModel = dataViewModel,
                                        onCreateCustomer = { navController.navigate("customer-form") },
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
                        onCreateCustomer = { navController.navigate("customer-form") },
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
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("customer-form") {
                    CustomerFormScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("products") {
                    ProductsScreen(
                        dataViewModel = dataViewModel,
                        onCreateProduct = { navController.navigate("product-form") },
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
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("product-form") {
                    ProductFormScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("business-settings") {
                    BusinessSettingsScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("new-quote") {
                    DocumentFormScreen(
                        dataViewModel = dataViewModel,
                        type = app.tijario.data.model.DocumentType.Quote,
                        onBack = { navController.popBackStack() },
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
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") }
                    )
                }
                composable("new-invoice") {
                    DocumentFormScreen(
                        dataViewModel = dataViewModel,
                        type = app.tijario.data.model.DocumentType.Invoice,
                        onBack = { navController.popBackStack() },
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
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") }
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
                        onBack = { navController.popBackStack() },
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
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") }
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
                        onBack = { navController.popBackStack() },
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
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct,
                        selectedProductRowIndex = activeSelectedProductRowIndex,
                        onSelectedProductConsumed = {
                            activeSelectedProduct = null
                            activeSelectedProductRowIndex = null
                        },
                        onNavigateToBusinessSettings = { navController.navigate("business-settings") }
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
                        onBack = { navController.popBackStack() },
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
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("settings") {
                    SettingsHomeScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.popBackStack() },
                        onStoreSettings = { navController.navigate("business-settings") },
                        onAccountSettings = { navController.navigate("account-settings") },
                        onAppSettings = { navController.navigate("app-settings") },
                        onUpgrade = { navController.navigate("upgrade-plan") },
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
                        onBack = { navController.popBackStack() },
                    )
                }
                composable("account-settings") {
                    AccountSettingsScreen(
                        dataViewModel = dataViewModel,
                        onBack = { navController.popBackStack() },
                        onLogout = { authViewModel.logout() },
                        onDeleteAccount = {
                            val userId = dataViewModel.currentUserId() ?: ""
                            val res = app.tijario.config.Supabase.apiClient.deleteAccount()
                            if (res.ok) {
                                if (userId.isNotBlank()) {
                                    dataViewModel.deleteAccountLocal(userId)
                                }
                                authViewModel.logout()
                                Result.success(Unit)
                            } else {
                                Result.failure(Exception(res.message ?: "Failed to delete account"))
                            }
                        }
                    )
                }
                composable("app-settings") {
                    AppSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable("upgrade-plan") {
                    UpgradePlanScreen(onBack = { navController.popBackStack() })
                }
            }
        }
        is CentralAuthState.Error -> {
            val errorMessage = when (state.message) {
                "فشل فحص حالة الجلسة" -> t("error_session_check_failed")
                "حدث خطأ أثناء فحص البيانات بعد التحقق." -> t("error_after_verification_check")
                else -> state.message
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
