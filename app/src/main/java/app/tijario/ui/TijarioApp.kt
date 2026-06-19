package app.tijario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tijario.config.loadAppConfig
import app.tijario.config.t
import app.tijario.ui.screens.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

private data class RootTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val rootTabs = listOf(
    RootTab("dashboard", "tab_home", Icons.Filled.Home),
    RootTab("documents", "tab_documents", Icons.Filled.Description),
    RootTab("ai", "tab_ai", Icons.Filled.AutoAwesome),
    RootTab("products", "tab_products", Icons.Filled.ShoppingBag),
    RootTab("customers", "tab_customers", Icons.Filled.People),
)

@Composable
fun TijarioApp() {
    val config = loadAppConfig()
    if (!config.isComplete) {
        ConfigurationRequiredScreen()
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val scope = rememberCoroutineScope()

    // Shared states for selection
    var activeSelectedCustomer by remember { mutableStateOf<app.tijario.data.model.Customer?>(null) }
    var activeSelectedProduct by remember { mutableStateOf<app.tijario.data.model.Product?>(null) }

    // Auto login & session check
    var startRoute by remember { mutableStateOf<String?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }

    val showSettingsShortcut = currentRoute != null &&
        currentRoute !in listOf("login", "register", "forgot-password", "onboarding", "account", "main", "intro")

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                app.tijario.config.Supabase.client.auth.awaitInitialization()
                val session = app.tijario.config.Supabase.client.auth.currentSessionOrNull()
                if (session != null) {
                    // Check if business settings onboarding is completed
                    val settingsList = app.tijario.config.Supabase.client.from("business_settings")
                        .select {
                            filter {
                                eq("user_id", session.user?.id ?: "")
                            }
                        }
                        .decodeList<app.tijario.data.model.BusinessSettings>()
                    if (settingsList.isNotEmpty()) {
                        startRoute = "main"
                    } else {
                        startRoute = "onboarding"
                    }
                } else {
                    startRoute = "intro"
                }
            } catch (e: Exception) {
                startRoute = "intro"
            } finally {
                isCheckingSession = false
            }
        }
    }

    if (isCheckingSession) {
        SplashScreen()
        return
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            NavHost(navController = navController, startDestination = startRoute ?: "intro") {
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
                        onLoginReady = {
                            scope.launch {
                                try {
                                    val session = app.tijario.config.Supabase.client.auth.currentSessionOrNull()
                                    if (session != null) {
                                        val settingsList = app.tijario.config.Supabase.client.from("business_settings")
                                            .select {
                                                filter {
                                                    eq("user_id", session.user?.id ?: "")
                                                }
                                            }
                                            .decodeList<app.tijario.data.model.BusinessSettings>()
                                        if (settingsList.isNotEmpty()) {
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("onboarding") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate("onboarding")
                                    }
                                } catch (e: Exception) {
                                    navController.navigate("onboarding")
                                }
                            }
                        },
                        onRegister = { navController.navigate("register") },
                        onForgotPassword = { navController.navigate("forgot-password") },
                    )
                }
                composable("register") { RegisterScreen(onBackToLogin = { navController.popBackStack() }) }
                composable("forgot-password") { ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() }) }
                composable("onboarding") {
                    OnboardingScreen(
                        onDone = {
                            navController.navigate("main") {
                                popUpTo("onboarding") { inclusive = true }
                                popUpTo("login") { inclusive = true }
                            }
                        },
                    )
                }
                composable("main") {
                    val pagerState = rememberPagerState(pageCount = { 5 })
                    val pagerScope = rememberCoroutineScope()

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
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                shadowElevation = 4.dp
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
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    IconButton(
                                        onClick = { navController.navigate("account") },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "الإعدادات",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.background,
                                tonalElevation = 0.dp
                            ) {
                                rootTabs.forEachIndexed { index, tab ->
                                    val selected = pagerState.currentPage == index
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            pagerScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = t(tab.label), modifier = Modifier.size(26.dp)) },
                                        label = {
                                            Text(
                                                text = t(tab.label),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    ) { pagerPadding ->
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(pagerPadding)
                        ) { page ->
                            when (page) {
                                0 -> DashboardScreen(
                                    onNewQuote = { navController.navigate("new-quote") },
                                    onNewInvoice = { navController.navigate("new-invoice") },
                                    onAddProduct = { navController.navigate("product-form") },
                                    onCustomers = {
                                        pagerScope.launch {
                                            pagerState.animateScrollToPage(4)
                                        }
                                    },
                                    onAiTools = {
                                        pagerScope.launch {
                                            pagerState.animateScrollToPage(2)
                                        }
                                    },
                                    onBusinessSettings = { navController.navigate("account") },
                                    hideHeader = true
                                )
                                1 -> DocumentsScreen(
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
                                    hideHeader = true
                                )
                                2 -> AiToolsScreen(hideHeader = true)
                                3 -> ProductsScreen(
                                    onCreateProduct = { navController.navigate("product-form") },
                                    hideHeader = true
                                )
                                4 -> CustomersScreen(
                                    onCreateCustomer = { navController.navigate("customer-form") },
                                    hideHeader = true
                                )
                            }
                        }
                    }
                }

                composable("customers") {
                    CustomersScreen(
                        onCreateCustomer = { navController.navigate("customer-form") },
                        onCustomerSelected = { customer ->
                            activeSelectedCustomer = customer
                            navController.popBackStack()
                        }
                    )
                }
                composable("customer-form") {
                    CustomerFormScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("products") {
                    ProductsScreen(
                        onCreateProduct = { navController.navigate("product-form") },
                        onProductSelected = { product ->
                            activeSelectedProduct = product
                            navController.popBackStack()
                        }
                    )
                }
                composable("product-form") {
                    ProductFormScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("business-settings") { BusinessSettingsScreen(onBack = { navController.popBackStack() }) }
                composable("new-quote") {
                    DocumentFormScreen(
                        type = app.tijario.data.model.DocumentType.Quote,
                        onBack = { navController.popBackStack() },
                        onNavigateToSelectCustomer = { navController.navigate("customers") },
                        onNavigateToSelectProduct = { navController.navigate("products") },
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct
                    )
                }
                composable("new-invoice") {
                    DocumentFormScreen(
                        type = app.tijario.data.model.DocumentType.Invoice,
                        onBack = { navController.popBackStack() },
                        onNavigateToSelectCustomer = { navController.navigate("customers") },
                        onNavigateToSelectProduct = { navController.navigate("products") },
                        selectedCustomer = activeSelectedCustomer,
                        selectedProduct = activeSelectedProduct
                    )
                }
                composable("account") {
                    AccountScreen(
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            if (showSettingsShortcut) {
                IconButton(
                    onClick = {
                        navController.navigate("account") {
                            launchSingleTop = true
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "الإعدادات")
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
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }
            Text(
                text = "تجاريو",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "مستنداتك وفواتيرك بلمح البصر",
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
