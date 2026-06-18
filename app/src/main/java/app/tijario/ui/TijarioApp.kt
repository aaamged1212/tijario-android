package app.tijario.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.Modifier
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
import app.tijario.ui.screens.AccountScreen
import app.tijario.ui.screens.AiToolsScreen
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
import app.tijario.ui.screens.ProductsScreen
import app.tijario.ui.screens.ProductFormScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.ShoppingBag

private data class RootTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val rootTabs = listOf(
    RootTab("dashboard", "tab_home", Icons.Filled.Home),
    RootTab("customers", "tab_customers", Icons.Filled.People),
    RootTab("products", "tab_products", Icons.Filled.ShoppingBag),
    RootTab("documents", "tab_documents", Icons.Filled.Description),
    RootTab("ai", "tab_ai", Icons.Filled.AutoAwesome),
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

    val showBottomBar = currentRoute in listOf("dashboard", "customers", "products", "documents", "ai")

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
                        startRoute = "dashboard"
                    } else {
                        startRoute = "onboarding"
                    }
                } else {
                    startRoute = "login"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                startRoute = "login"
            } finally {
                isCheckingSession = false
            }
        }
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    rootTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = t(tab.label)) },
                            label = { Text(t(tab.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            NavHost(navController = navController, startDestination = startRoute ?: "login") {
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
                                            navController.navigate("dashboard") {
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
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                                popUpTo("login") { inclusive = true }
                            }
                        },
                    )
                }
                composable("dashboard") {
                    DashboardScreen(
                        onNewQuote = { navController.navigate("new-quote") },
                        onNewInvoice = { navController.navigate("new-invoice") },
                        onCustomers = {
                            navController.navigate("customers") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onAiTools = {
                            navController.navigate("ai") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onBusinessSettings = { navController.navigate("account") },
                    )
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
                composable("documents") {
                    DocumentsScreen(
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
                    )
                }
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
                composable("ai") { AiToolsScreen() }
                composable("account") {
                    AccountScreen(
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
