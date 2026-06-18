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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tijario.config.loadAppConfig
import app.tijario.ui.screens.AccountScreen
import app.tijario.ui.screens.AiToolsScreen
import app.tijario.ui.screens.ConfigurationRequiredScreen
import app.tijario.ui.screens.CustomersScreen
import app.tijario.ui.screens.DashboardScreen
import app.tijario.ui.screens.DocumentsScreen

private data class RootTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val rootTabs = listOf(
    RootTab("dashboard", "الرئيسية", Icons.Filled.Home),
    RootTab("customers", "العملاء", Icons.Filled.People),
    RootTab("documents", "المستندات", Icons.Filled.Description),
    RootTab("ai", "AI", Icons.Filled.AutoAwesome),
    RootTab("account", "الحساب", Icons.Filled.AccountCircle),
)

@Composable
fun TijarioApp() {
    val config = loadAppConfig()
    if (!config.isComplete) {
        ConfigurationRequiredScreen()
        return
    }

    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                rootTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") { DashboardScreen() }
                composable("customers") { CustomersScreen() }
                composable("documents") { DocumentsScreen() }
                composable("ai") { AiToolsScreen() }
                composable("account") { AccountScreen() }
            }
        }
    }
}
