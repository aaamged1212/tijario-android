package app.tijario.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.MainActivity
import app.tijario.config.AppLanguage
import app.tijario.config.t
import app.tijario.ui.components.TijarioButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.tijario.ui.components.TijarioTextField
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import java.net.URL

@Composable
fun ConfigurationRequiredScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = t("config_req"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "يرجى إضافة مفاتيح ورابط Supabase في gradle.properties المحلي الخاص بك لتشغيل التطبيق.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TIJARIO_SUPABASE_URL=...\nTIJARIO_SUPABASE_ANON_KEY=...\nTIJARIO_API_BASE_URL=...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onNewQuote: () -> Unit,
    onNewInvoice: () -> Unit,
    onAddProduct: () -> Unit,
    onCustomers: () -> Unit,
    onAiTools: () -> Unit,
    onBusinessSettings: () -> Unit,
    hideHeader: Boolean = false,
) {
    var totalAmount by remember { mutableDoubleStateOf(0.0) }
    var paidInvoicesCount by remember { mutableIntStateOf(0) }
    var pendingQuotesCount by remember { mutableIntStateOf(0) }
    var businessCurrency by remember { mutableStateOf("ر.س") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val settingsList = app.tijario.config.Supabase.client.from("business_settings")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.BusinessSettings>()
                    val settings = settingsList.firstOrNull()
                    if (settings != null) {
                        businessCurrency = settings.currency
                    }

                    val docs = app.tijario.config.Supabase.client.from("documents")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.DocumentSummary>()

                    var sum = 0.0
                    var paid = 0
                    var pending = 0
                    for (doc in docs) {
                        if (doc.type == app.tijario.data.model.DocumentType.Invoice) {
                            if (doc.status.lowercase() == "paid") {
                                sum += doc.total
                                paid++
                            }
                        } else if (doc.type == app.tijario.data.model.DocumentType.Quote) {
                            if (doc.status.lowercase() == "draft") {
                                pending++
                            }
                        }
                    }
                    totalAmount = sum
                    paidInvoicesCount = paid
                    pendingQuotesCount = pending
                }
            } catch (e: Exception) {
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header
        if (!hideHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = t("welcome"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = t("dash_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onBusinessSettings,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .size(48.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Stats Card Gradient
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0F766E), Color(0xFF0D9488))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("financial_summary"), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                t("this_month"),
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        String.format(java.util.Locale.getDefault(), "%.2f %s", totalAmount, businessCurrency),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatsSmall(label = t("paid_invoices"), value = paidInvoicesCount.toString(), icon = Icons.Filled.Payments)
                        StatsSmall(label = t("pending_quotes"), value = pendingQuotesCount.toString(), icon = Icons.Filled.Description)
                    }
                }
            }
        }

        // Quick Actions Grid
        Text(
            text = t("quick_actions"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = t("btn_new_invoice"),
                icon = Icons.Filled.Receipt,
                backgroundColor = Color(0xFFF0FDF4),
                iconColor = Color(0xFF16A34A),
                onClick = onNewInvoice,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = t("btn_new_quote"),
                icon = Icons.Filled.Description,
                backgroundColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF2563EB),
                onClick = onNewQuote,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = t("btn_add_product"),
                icon = Icons.Filled.BusinessCenter,
                backgroundColor = Color(0xFFECFDF5),
                iconColor = Color(0xFF059669),
                onClick = onAddProduct,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = t("btn_add_customer"),
                icon = Icons.Filled.People,
                backgroundColor = Color(0xFFFDF2F8),
                iconColor = Color(0xFFDB2777),
                onClick = onCustomers,
                modifier = Modifier.weight(1f)
            )
        }
        QuickActionButton(
            title = t("tab_ai"),
            icon = Icons.Filled.AutoAwesome,
            backgroundColor = Color(0xFFF5F3FF),
            iconColor = Color(0xFF7C3AED),
            onClick = onAiTools,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatsSmall(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = Color.White.copy(alpha = 0.15f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Column {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 104.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomersScreen(
    onCreateCustomer: () -> Unit,
    onCustomerSelected: ((app.tijario.data.model.Customer) -> Unit)? = null,
    hideHeader: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var customers by remember { mutableStateOf<List<app.tijario.data.model.Customer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val list = app.tijario.config.Supabase.client.from("customers")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.Customer>()
                    customers = list
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.whatsappNumber.contains(searchQuery)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Icon next to Title
            if (!hideHeader) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = t("customers_title"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (onCustomerSelected != null) "اختر عميلاً لتحديده للفاتورة" else t("customers_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            TijarioTextField(
                label = t("search_placeholder"),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )

            // Loading state or Empty state
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Customers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCustomers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCustomerSelected?.invoke(customer)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            customer.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            customer.whatsappNumber,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                val context = LocalContext.current
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.whatsappNumber}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Phone, contentDescription = "اتصال", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                val formatted = customer.whatsappNumber.replace("[^0-9]".toRegex(), "")
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formatted"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Chat, contentDescription = "واتساب", tint = Color(0xFF25D366))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button - BottomStart mirrors to bottom-right in AR and bottom-left in EN
        FloatingActionButton(
            onClick = onCreateCustomer,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
fun ProductsScreen(
    onCreateProduct: () -> Unit,
    onProductSelected: ((app.tijario.data.model.Product) -> Unit)? = null,
    hideHeader: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<app.tijario.data.model.Product>>(emptyList()) }
    var businessCurrency by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val settingsList = app.tijario.config.Supabase.client.from("business_settings")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.BusinessSettings>()
                    val settings = settingsList.firstOrNull()
                    if (settings != null) {
                        businessCurrency = settings.currency
                    }

                    val list = app.tijario.config.Supabase.client.from("products")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.Product>()
                    products = list
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || (it.description ?: "").contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hideHeader) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BusinessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = t("products_title"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (onProductSelected != null) "اختر منتجاً أو خدمة لتحديد قيمتها للفاتورة" else t("products_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TijarioTextField(
                label = t("search_products"),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onProductSelected?.invoke(item)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (item.kind == app.tijario.data.model.ProductKind.Service) Icons.Filled.Star else Icons.Filled.BusinessCenter,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            item.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            item.description ?: (if (item.kind == app.tijario.data.model.ProductKind.Service) t("kind_service") else t("kind_product")),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Text(
                                    "${item.price} ${businessCurrency.ifBlank { item.currency }}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateProduct,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
fun DocumentsScreen(
    onNewQuote: () -> Unit,
    onNewInvoice: () -> Unit,
    hideHeader: Boolean = false
) {
    var selectedSection by remember { mutableStateOf(0) } // 0 = Invoices, 1 = Quotes
    var menuExpanded by remember { mutableStateOf(false) }

    var documents by remember { mutableStateOf<List<app.tijario.data.model.DocumentSummary>>(emptyList()) }
    var customers by remember { mutableStateOf<List<app.tijario.data.model.Customer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val docs = app.tijario.config.Supabase.client.from("documents")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.DocumentSummary>()
                    val custs = app.tijario.config.Supabase.client.from("customers")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.Customer>()
                    documents = docs
                    customers = custs
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    // Filter documents depending on selection
    val filteredDocs = documents.filter { doc ->
        if (selectedSection == 0) doc.type == app.tijario.data.model.DocumentType.Invoice else doc.type == app.tijario.data.model.DocumentType.Quote
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Icon next to Title
            if (!hideHeader) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = t("documents_title"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = t("documents_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // TabRow for sections
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text(t("section_invoices"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text(t("section_quotes"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }

            // Loading state or List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Document list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredDocs) { doc ->
                        val customerName = customers.find { it.id == doc.customerId }?.name ?: "عميل غير معروف"
                        val statusText = when(doc.status.lowercase()) {
                            "paid" -> t("doc_status_paid")
                            "draft" -> t("doc_status_draft")
                            else -> t("doc_status_expired")
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        doc.documentNumber,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    )
                                    Surface(
                                        color = when(doc.status.lowercase()) {
                                            "paid" -> Color(0xFFDCFCE7)
                                            "draft" -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFF1F5F9)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            statusText,
                                            color = when(doc.status.lowercase()) {
                                                "paid" -> Color(0xFF15803D)
                                                "draft" -> Color(0xFFB45309)
                                                else -> Color(0xFF475569)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(customerName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                    Text("${doc.total} ${doc.currency}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button with DropdownMenu - BottomStart mirrors to bottom-right in AR and bottom-left in EN
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = { menuExpanded = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text(t("btn_create_invoice"), fontWeight = FontWeight.Medium) },
                    onClick = {
                        menuExpanded = false
                        onNewInvoice()
                    }
                )
                DropdownMenuItem(
                    text = { Text(t("btn_create_quote"), fontWeight = FontWeight.Medium) },
                    onClick = {
                        menuExpanded = false
                        onNewQuote()
                    }
                )
            }
        }
    }
}

@Composable
fun AiToolsScreen(hideHeader: Boolean = false) {
    var selectedTab by remember { mutableStateOf(0) }

    var chatMessage by remember { mutableStateOf("") }
    var aiReplyResult by remember { mutableStateOf("") }
    var aiReplyError by remember { mutableStateOf<String?>(null) }
    var isReplyLoading by remember { mutableStateOf(false) }

    var captionMessage by remember { mutableStateOf("") }
    var aiCaptionResult by remember { mutableStateOf("") }
    var aiCaptionError by remember { mutableStateOf<String?>(null) }
    var isCaptionLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hideHeader) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = t("ai_title"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = t("ai_subtitle"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // TabRow for AI sections
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(t("tab_ai_reply"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(t("tab_ai_caption"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        if (selectedTab == 0) {
            // Card AI Assistant - Smart Reply
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        t("ai_card_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TijarioTextField(
                        label = t("ai_input_placeholder"),
                        value = chatMessage,
                        onValueChange = { chatMessage = it },
                        singleLine = false
                    )

                    TijarioButton(
                        text = t("btn_generate_reply"),
                        onClick = {
                            scope.launch {
                                try {
                                    isReplyLoading = true
                                    aiReplyError = null
                                    aiReplyResult = ""
                                    val result = app.tijario.config.Supabase.apiClient.generateAiReply(
                                        app.tijario.data.remote.AiReplyRequest(
                                            caseType = "customer_inquiry",
                                            customerName = null,
                                            dialect = "gulf",
                                            tone = "friendly",
                                            length = "short",
                                            extraNote = chatMessage,
                                        )
                                    )
                                    if (result.ok) {
                                        val replies = result.data?.replies.orEmpty()
                                        aiReplyResult = listOfNotNull(
                                            replies["short"],
                                            replies["sales"],
                                            replies["formal"],
                                        ).joinToString("\n\n").ifBlank {
                                            "تم إنشاء الرد لكن لم تصل صيغة صالحة من الخادم."
                                        }
                                    } else {
                                        aiReplyError = result.displayMessage
                                    }
                                } catch (e: Exception) {
                                    aiReplyError = "تعذر إنشاء الرد الآن. تحقق من الاتصال وحاول مرة أخرى."
                                } finally {
                                    isReplyLoading = false
                                }
                            }
                        },
                        enabled = chatMessage.isNotBlank(),
                        isLoading = isReplyLoading,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    if (aiReplyResult.isNotBlank()) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(t("ai_suggestion"), color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFBBC05), modifier = Modifier.size(16.dp))
                                }
                                Text(aiReplyResult, color = Color(0xFF1E293B), fontSize = 14.sp)
                            }
                        }
                    }

                    aiReplyError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        } else {
            // Card AI Assistant - Smart Caption
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        t("ai_caption_card_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TijarioTextField(
                        label = t("ai_caption_input_placeholder"),
                        value = captionMessage,
                        onValueChange = { captionMessage = it },
                        singleLine = false
                    )

                    TijarioButton(
                        text = t("btn_generate_caption"),
                        onClick = {
                            scope.launch {
                                try {
                                    isCaptionLoading = true
                                    aiCaptionError = null
                                    aiCaptionResult = ""
                                    val result = app.tijario.config.Supabase.apiClient.generateAiCaption(
                                        app.tijario.data.remote.AiCaptionRequest(
                                            captionType = "product_post",
                                            platform = "instagram",
                                            dialect = "gulf",
                                            tone = "sales",
                                            length = "short",
                                            productOrService = captionMessage,
                                        )
                                    )
                                    if (result.ok) {
                                        val captions = result.data?.captions.orEmpty()
                                        aiCaptionResult = listOf("short", "sales", "premium")
                                            .mapNotNull { key -> captions[key] }
                                            .joinToString("\n\n") { variant ->
                                                buildString {
                                                    append(variant.caption)
                                                    append("\n")
                                                    append(variant.cta)
                                                    if (variant.hashtags.isNotEmpty()) {
                                                        append("\n")
                                                        append(variant.hashtags.joinToString(" "))
                                                    }
                                                }
                                            }
                                            .ifBlank { "تم إنشاء الكابشن لكن لم تصل صيغة صالحة من الخادم." }
                                    } else {
                                        aiCaptionError = result.displayMessage
                                    }
                                } catch (e: Exception) {
                                    aiCaptionError = "تعذر إنشاء الكابشن الآن. تحقق من الاتصال وحاول مرة أخرى."
                                } finally {
                                    isCaptionLoading = false
                                }
                            }
                        },
                        enabled = captionMessage.isNotBlank(),
                        isLoading = isCaptionLoading,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    if (aiCaptionResult.isNotBlank()) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(t("ai_caption_suggestion"), color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFBBC05), modifier = Modifier.size(16.dp))
                                }
                                Text(aiCaptionResult, color = Color(0xFF1E293B), fontSize = 14.sp)
                            }
                        }
                    }

                    aiCaptionError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onLogout: () -> Unit, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Store, 1 = Personal
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var businessSettings by remember { mutableStateOf<app.tijario.data.model.BusinessSettings?>(null) }
    var currentUserEmail by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLogoUploading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                val user = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (user != null) {
                    currentUserEmail = user.email ?: ""
                    currentUserName = user.userMetadata?.get("full_name")?.toString() ?: ""

                    val settingsList = app.tijario.config.Supabase.client.from("business_settings")
                        .select {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.BusinessSettings>()
                    businessSettings = settingsList.firstOrNull()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text(t("tab_account"), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("حساب المتجر", fontWeight = FontWeight.Bold) } // Store Account
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("الحساب الشخصي", fontWeight = FontWeight.Bold) } // Personal Account
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (selectedTab == 0) {
                // Store Account Tab
                StoreAccountContent(
                    settings = businessSettings,
                    isLogoUploading = isLogoUploading,
                    onLogoSelected = { logoUri ->
                        scope.launch {
                            val settings = businessSettings
                            if (settings == null) {
                                snackbarHostState.showSnackbar("أكمل إعدادات المتجر أولاً قبل رفع الشعار")
                                return@launch
                            }

                            try {
                                isLogoUploading = true
                                val uploadRequest = buildLogoUploadRequest(context, logoUri)
                                val result = app.tijario.config.Supabase.apiClient.uploadBusinessLogo(uploadRequest)
                                val logoUrl = result.data?.logoUrl
                                if (result.ok && !logoUrl.isNullOrBlank()) {
                                    businessSettings = settings.copy(logoUrl = logoUrl)
                                    snackbarHostState.showSnackbar("تم حفظ شعار المتجر بنجاح")
                                } else {
                                    snackbarHostState.showSnackbar(result.displayMessage)
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(e.message ?: "تعذر رفع الشعار. حاول مرة أخرى.")
                            } finally {
                                isLogoUploading = false
                            }
                        }
                    },
                    onUpdate = { updated ->
                        scope.launch {
                            try {
                                app.tijario.config.Supabase.client.from("business_settings").upsert(updated)
                                businessSettings = updated
                                snackbarHostState.showSnackbar("تم حفظ تغييرات المتجر بنجاح")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("خطأ في الحفظ")
                            }
                        }
                    }
                )
            } else {
                // Personal Account Tab
                PersonalAccountContent(
                    email = currentUserEmail,
                    name = currentUserName,
                    onLogout = onLogout,
                    onChangePassword = {
                        scope.launch {
                            if (currentUserEmail.isBlank()) {
                                snackbarHostState.showSnackbar("لا يوجد بريد إلكتروني مرتبط بالحساب")
                                return@launch
                            }

                            try {
                                val result = app.tijario.config.Supabase.apiClient.requestPasswordReset(
                                    app.tijario.data.remote.ResetPasswordRequest(email = currentUserEmail)
                                )
                                if (result.ok) {
                                    snackbarHostState.showSnackbar("تم إرسال رابط تغيير كلمة المرور إلى بريدك")
                                } else {
                                    snackbarHostState.showSnackbar(result.displayMessage)
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("تعذر إرسال رابط تغيير كلمة المرور")
                            }
                        }
                    },
                )
            }

            // App Settings (Global)
            Text(
                text = "إعدادات التطبيق",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Language
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(t("settings_lang"), fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { MainActivity.currentLanguage = AppLanguage.AR }) {
                                Text("العربية", color = if (MainActivity.currentLanguage == AppLanguage.AR) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                            TextButton(onClick = { MainActivity.currentLanguage = AppLanguage.EN }) {
                                Text("English", color = if (MainActivity.currentLanguage == AppLanguage.EN) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }
                    HorizontalDivider()
                    // Theme
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(t("settings_theme"), fontWeight = FontWeight.Bold)
                        }
                        Switch(checked = MainActivity.isDarkMode, onCheckedChange = { MainActivity.isDarkMode = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StoreAccountContent(
    settings: app.tijario.data.model.BusinessSettings?,
    isLogoUploading: Boolean,
    onLogoSelected: (Uri) -> Unit,
    onUpdate: (app.tijario.data.model.BusinessSettings) -> Unit
) {
    var businessName by remember(settings) { mutableStateOf(settings?.businessName ?: "") }
    var whatsapp by remember(settings) { mutableStateOf(settings?.whatsappNumber ?: "") }
    var country by remember(settings) { mutableStateOf(settings?.country ?: "السعودية") }
    var city by remember(settings) { mutableStateOf(settings?.city ?: "") }
    var currency by remember(settings) { mutableStateOf(settings?.currency ?: "SAR") }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onLogoSelected(uri)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StoreLogoPicker(
                logoUrl = settings?.logoUrl,
                isUploading = isLogoUploading,
                onClick = { logoPicker.launch("image/*") },
            )
        }

        TijarioTextField(
            label = t("shop_name"),
            value = businessName,
            onValueChange = { businessName = it },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) }
        )
        TijarioTextField(
            label = t("whatsapp_phone"),
            value = whatsapp,
            onValueChange = { whatsapp = it },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) }
        )
        TijarioTextField(
            label = t("country"),
            value = country,
            onValueChange = { country = it },
            leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null) }
        )
        TijarioTextField(
            label = "المدينة",
            value = city,
            onValueChange = { city = it },
            leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) }
        )
        TijarioTextField(
            label = t("currency"),
            value = currency,
            onValueChange = { currency = it },
            leadingIcon = { Icon(Icons.Filled.MonetizationOn, contentDescription = null) }
        )

        TijarioButton(
            text = "حفظ تغييرات المتجر",
            onClick = {
                if (settings != null) {
                    onUpdate(settings.copy(
                        businessName = businessName,
                        whatsappNumber = whatsapp,
                        country = country,
                        city = city.ifBlank { null },
                        currency = currency
                    ))
                }
            }
        )
    }
}
@Composable
private fun StoreLogoPicker(
    logoUrl: String?,
    isUploading: Boolean,
    onClick: () -> Unit,
) {
    val logoBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, logoUrl) {
        value = null
        if (!logoUrl.isNullOrBlank()) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    URL(logoUrl).openStream().use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        }
    }

    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(enabled = !isUploading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            isUploading -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
            logoBitmap != null -> Image(
                bitmap = logoBitmap!!.asImageBitmap(),
                contentDescription = "شعار المتجر",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("شعار المتجر", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private suspend fun buildLogoUploadRequest(
    context: Context,
    uri: Uri,
): app.tijario.data.remote.UploadLogoRequest =
    withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

        require(mimeType in allowedMimeTypes) {
            "ارفع صورة بصيغة PNG أو JPG أو WebP."
        }

        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("تعذر قراءة صورة الشعار.")

        require(bytes.isNotEmpty()) {
            "ملف الشعار فارغ."
        }
        require(bytes.size <= 2 * 1024 * 1024) {
            "حجم الشعار يجب ألا يتجاوز 2MB."
        }

        app.tijario.data.remote.UploadLogoRequest(
            fileName = "logo",
            mimeType = mimeType,
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
        )
    }

@Composable
fun PersonalAccountContent(
    email: String,
    name: String,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TijarioTextField(
                    label = "الاسم الكامل",
                    value = name,
                    onValueChange = {},
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                )
                TijarioTextField(
                    label = "البريد الإلكتروني",
                    value = email,
                    onValueChange = {},
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
                )
            }
        }

        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("تغيير كلمة المرور", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UsageIndicator(title: String, used: Int, total: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$used من $total", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
        }
        LinearProgressIndicator(
            progress = { used.toFloat() / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
