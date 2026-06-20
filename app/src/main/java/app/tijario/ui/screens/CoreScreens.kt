package app.tijario.ui.screens

import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.MainActivity
import app.tijario.config.AppLanguage
import app.tijario.config.t
import app.tijario.features.documents.export.DocumentExportManager
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.ui.DocumentTemplatePreferences
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.tijario.ui.components.TijarioTextField
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
    dataViewModel: TijarioDataViewModel,
    onNewQuote: () -> Unit,
    onNewInvoice: () -> Unit,
    onAddProduct: () -> Unit,
    onCustomers: () -> Unit,
    onAiTools: () -> Unit,
    onBusinessSettings: () -> Unit,
    hideHeader: Boolean = false,
) {
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val businessCurrency = uiState.businessSettings?.currency ?: "SAR"
    val referenceDate = remember { java.time.LocalDate.now() }
    val totalAmount = app.tijario.domain.DashboardStatsCalculator.calculateCurrentMonthEarnings(uiState.documents, referenceDate)
    val paidInvoicesCount = app.tijario.domain.DashboardStatsCalculator.countPaidInvoices(uiState.documents, referenceDate)
    val pendingQuotesCount = app.tijario.domain.DashboardStatsCalculator.countPendingQuotes(uiState.documents)

    val planUsage = uiState.planUsage
    val isDocLimitReached = planUsage != null && planUsage.documentsLimit > 0 && planUsage.documentsUsed >= planUsage.documentsLimit
    var showLimitAlert by remember { mutableStateOf(false) }

    if (showLimitAlert) {
        AlertDialog(
            onDismissRequest = { showLimitAlert = false },
            title = { Text("تم الوصول للحد الأقصى", fontWeight = FontWeight.Bold) },
            text = { Text("لقد استهلكت كامل الحد المتاح لك من الفواتير وعروض الأسعار لشهرك الحالي حسب باقتك (${planUsage?.planName}). يرجى الترقية للاستمرار.") },
            confirmButton = {
                Button(onClick = { showLimitAlert = false }) {
                    Text("حسناً")
                }
            }
        )
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
                backgroundColor = if (isDocLimitReached) Color.LightGray.copy(alpha = 0.2f) else Color(0xFFF0FDF4),
                iconColor = if (isDocLimitReached) Color.Gray else Color(0xFF16A34A),
                onClick = { if (isDocLimitReached) showLimitAlert = true else onNewInvoice() },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = t("btn_new_quote"),
                icon = Icons.Filled.Description,
                backgroundColor = if (isDocLimitReached) Color.LightGray.copy(alpha = 0.2f) else Color(0xFFEFF6FF),
                iconColor = if (isDocLimitReached) Color.Gray else Color(0xFF2563EB),
                onClick = { if (isDocLimitReached) showLimitAlert = true else onNewQuote() },
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

        if (planUsage != null) {
            Text(
                text = "تفاصيل باقتك الحالية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
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
                    Text("الباقة: ${planUsage.planName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    UsageIndicator(
                        title = "المستندات المستخدمة",
                        used = planUsage.documentsUsed,
                        total = planUsage.documentsLimit,
                        color = MaterialTheme.colorScheme.primary
                    )
                    UsageIndicator(
                        title = "استعلامات الذكاء الاصطناعي",
                        used = planUsage.aiUsed,
                        total = planUsage.aiLimit,
                        color = Color(0xFF7C3AED)
                    )
                }
            }
        }

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
    dataViewModel: TijarioDataViewModel,
    onCreateCustomer: () -> Unit,
    onCustomerSelected: ((app.tijario.data.model.Customer) -> Unit)? = null,
    onEditCustomer: ((String) -> Unit)? = null,
    hideHeader: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val customers = uiState.customers
    val isLoading = uiState.isInitialLoading && customers.isEmpty()
    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.whatsappNumber.contains(searchQuery)
    }

    var customerToDelete by remember { mutableStateOf<app.tijario.data.model.Customer?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null; deleteErrorMessage = null },
            title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("هل أنت متأكد من رغبتك في حذف العميل ${customerToDelete?.name}؟")
                    deleteErrorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isDeleting = true
                                deleteErrorMessage = null
                                val res = dataViewModel.deleteCustomer(customerToDelete!!.id!!)
                                if (res.isSuccess) {
                                    customerToDelete = null
                                } else {
                                    deleteErrorMessage = res.exceptionOrNull()?.message ?: "فشل حذف العميل."
                                }
                            } catch (e: Exception) {
                                deleteErrorMessage = e.message ?: "حدث خطأ غير متوقع."
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null; deleteErrorMessage = null }) {
                    Text("إلغاء")
                }
            }
        )
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
            } else if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("لا يوجد عملاء مضافين", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onCreateCustomer) {
                            Text("إضافة عميل جديد")
                        }
                    }
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
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (onCustomerSelected == null) {
                                        IconButton(
                                            onClick = {
                                                onEditCustomer?.invoke(customer.id!!)
                                            }
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = {
                                                customerToDelete = customer
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.whatsappNumber}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "تعذر تشغيل تطبيق الاتصال", android.widget.Toast.LENGTH_SHORT).show()
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
                                                android.widget.Toast.makeText(context, "تعذر فتح تطبيق واتساب", android.widget.Toast.LENGTH_SHORT).show()
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
    dataViewModel: TijarioDataViewModel,
    onCreateProduct: () -> Unit,
    onProductSelected: ((app.tijario.data.model.Product) -> Unit)? = null,
    onEditProduct: ((String) -> Unit)? = null,
    hideHeader: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val products = uiState.products
    val businessCurrency = uiState.businessSettings?.currency ?: "SAR"
    val isLoading = uiState.isInitialLoading && products.isEmpty()
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || (it.description ?: "").contains(searchQuery, ignoreCase = true)
    }

    var productToDelete by remember { mutableStateOf<app.tijario.data.model.Product?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null; deleteErrorMessage = null },
            title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("هل أنت متأكد من رغبتك في حذف المنتج ${productToDelete?.name}؟")
                    deleteErrorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isDeleting = true
                                deleteErrorMessage = null
                                val res = dataViewModel.deleteProduct(productToDelete!!.id!!)
                                if (res.isSuccess) {
                                    productToDelete = null
                                } else {
                                    deleteErrorMessage = res.exceptionOrNull()?.message ?: "فشل حذف المنتج."
                                }
                            } catch (e: Exception) {
                                deleteErrorMessage = e.message ?: "حدث خطأ غير متوقع."
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null; deleteErrorMessage = null }) {
                    Text("إلغاء")
                }
            }
        )
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
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("لا يوجد منتجات أو خدمات مضافة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onCreateProduct) {
                            Text("إضافة منتج جديد")
                        }
                    }
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "${item.price} ${businessCurrency.ifBlank { item.currency }}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (onProductSelected == null) {
                                        IconButton(
                                            onClick = {
                                                onEditProduct?.invoke(item.id!!)
                                            }
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = {
                                                productToDelete = item
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
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
    dataViewModel: TijarioDataViewModel,
    onNewQuote: () -> Unit,
    onNewInvoice: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onEditDocument: (String, app.tijario.data.model.DocumentType) -> Unit = { _, _ -> },
    hideHeader: Boolean = false
) {
    var selectedSection by remember { mutableStateOf(0) } // 0 = Invoices, 1 = Quotes
    var menuExpanded by remember { mutableStateOf(false) }
    var documentPendingDelete by remember { mutableStateOf<app.tijario.data.model.DocumentSummary?>(null) }
    var busyDocumentId by remember { mutableStateOf<String?>(null) }

    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportManager = remember(context) { DocumentExportManager(context) }
    val templatePreferences = remember(context) { DocumentTemplatePreferences(context) }
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val documents = uiState.documents
    val customers = uiState.customers
    val isLoading = uiState.isInitialLoading && documents.isEmpty()
    // Filter documents depending on selection
    val filteredDocs = documents.filter { doc ->
        if (selectedSection == 0) doc.type == app.tijario.data.model.DocumentType.Invoice else doc.type == app.tijario.data.model.DocumentType.Quote
    }

    fun shareDocument(documentId: String) {
        scope.launch {
            try {
                busyDocumentId = documentId
                val completeDocument = dataViewModel.fetchCompleteDocument(documentId).getOrThrow()
                val renderModel = TijarioDocumentMapper.fromSaved(
                    document = completeDocument,
                    businessSettings = uiState.businessSettings,
                    language = AppLanguage.AR,
                    templateId = templatePreferences.getDefaultTemplateId(),
                )
                val intent = exportManager.shareIntent(renderModel)
                context.startActivity(Intent.createChooser(intent, "مشاركة PDF"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "لا يوجد تطبيق مناسب للمشاركة", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(context, "تعذر تجهيز المستند للمشاركة الآن", Toast.LENGTH_LONG).show()
            } finally {
                busyDocumentId = null
            }
        }
    }

    fun deleteDocument(documentId: String) {
        scope.launch {
            try {
                busyDocumentId = documentId
                val result = dataViewModel.deleteDocument(documentId)
                if (!result.ok) {
                    Toast.makeText(context, result.displayMessage, Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "تعذر حذف المستند الآن", Toast.LENGTH_LONG).show()
            } finally {
                busyDocumentId = null
            }
        }
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDocumentClick(doc.id) },
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
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (busyDocumentId == doc.id) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        } else if (doc.type == app.tijario.data.model.DocumentType.Invoice) {
                                            val payStatusText = app.tijario.domain.PaymentStatusMapper.getStatusText(doc.paymentStatus)
                                            val payColors = app.tijario.domain.PaymentStatusMapper.getStatusColors(doc.paymentStatus)
                                            Surface(
                                                color = Color(payColors.first),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    payStatusText,
                                                    color = Color(payColors.second),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = { shareDocument(doc.id) },
                                        enabled = busyDocumentId == null,
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { onEditDocument(doc.id, doc.type) },
                                        enabled = busyDocumentId == null,
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = Color(0xFF64748B))
                                    }
                                    IconButton(
                                        onClick = { documentPendingDelete = doc },
                                        enabled = busyDocumentId == null,
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                    }
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

        documentPendingDelete?.let { doc ->
            AlertDialog(
                onDismissRequest = { documentPendingDelete = null },
                title = { Text("حذف المستند") },
                text = { Text("هل تريد حذف ${doc.documentNumber}؟ لا يمكن التراجع عن هذا الإجراء.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            documentPendingDelete = null
                            deleteDocument(doc.id)
                        },
                    ) {
                        Text("حذف", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { documentPendingDelete = null }) {
                        Text("إلغاء")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiToolsScreen(
    dataViewModel: TijarioDataViewModel,
    hideHeader: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val planUsage = uiState.planUsage
    val isAiLimitReached = planUsage != null && planUsage.aiLimit > 0 && planUsage.aiUsed >= planUsage.aiLimit

    var selectedTab by remember { mutableStateOf(0) }

    // Smart Reply form
    var caseType by remember { mutableStateOf("customer_inquiry") }
    var customerName by remember { mutableStateOf("") }
    var replyDialect by remember { mutableStateOf("gulf") }
    var replyTone by remember { mutableStateOf("friendly") }
    var replyLength by remember { mutableStateOf("short") }
    var replyExtraNote by remember { mutableStateOf("") }
    var repliesList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var replyError by remember { mutableStateOf<String?>(null) }
    var isReplyLoading by remember { mutableStateOf(false) }

    // Smart Caption form
    var captionType by remember { mutableStateOf("product_post") }
    var platform by remember { mutableStateOf("instagram") }
    var captionDialect by remember { mutableStateOf("gulf") }
    var captionTone by remember { mutableStateOf("sales") }
    var captionLength by remember { mutableStateOf("short") }
    var productOrService by remember { mutableStateOf("") }
    var offer by remember { mutableStateOf("") }
    var captionExtraNote by remember { mutableStateOf("") }
    var captionsList by remember { mutableStateOf<List<Pair<String, app.tijario.data.remote.AiCaptionVariant>>>(emptyList()) }
    var captionError by remember { mutableStateOf<String?>(null) }
    var isCaptionLoading by remember { mutableStateOf(false) }

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

        // Limit Banner
        if (isAiLimitReached) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "لقد استهلكت كامل الحد المتاح لك من استعلامات الذكاء الاصطناعي لشهرك الحالي. يرجى الترقية للاستمرار.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

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
            // Smart Reply Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("إعدادات الرد الذكي", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Case type
                    Text("نوع الحالة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("customer_inquiry" to "استفسار", "objection" to "شكوى", "greeting" to "ترحيب").forEach { (code, label) ->
                            FilterChip(
                                selected = caseType == code,
                                onClick = { caseType = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Dialect
                    Text("اللهجة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("gulf" to "خليجي", "egyptian" to "مصري", "standard" to "فصحى").forEach { (code, label) ->
                            FilterChip(
                                selected = replyDialect == code,
                                onClick = { replyDialect = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Tone
                    Text("الأسلوب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("friendly" to "ودي", "formal" to "رسمي", "sales" to "تسويقي").forEach { (code, label) ->
                            FilterChip(
                                selected = replyTone == code,
                                onClick = { replyTone = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    TijarioTextField(
                        label = "اسم العميل (اختياري)",
                        value = customerName,
                        onValueChange = { customerName = it }
                    )

                    TijarioTextField(
                        label = "ملاحظات إضافية أو تفاصيل الاستفسار",
                        value = replyExtraNote,
                        onValueChange = { replyExtraNote = it },
                        singleLine = false
                    )

                    TijarioButton(
                        text = "توليد الرد الذكي",
                        onClick = {
                            scope.launch {
                                try {
                                    isReplyLoading = true
                                    replyError = null
                                    repliesList = emptyList()
                                    val req = app.tijario.data.remote.AiReplyRequest(
                                        caseType = caseType,
                                        customerName = customerName.ifBlank { null },
                                        dialect = replyDialect,
                                        tone = replyTone,
                                        length = replyLength,
                                        extraNote = replyExtraNote.ifBlank { null }
                                    )
                                    val res = dataViewModel.generateAiReply(req)
                                    if (res.ok) {
                                        repliesList = res.data?.replies?.toList() ?: emptyList()
                                    } else {
                                        replyError = res.displayMessage
                                    }
                                } catch (e: Exception) {
                                    replyError = e.message ?: "حدث خطأ غير متوقع"
                                } finally {
                                    isReplyLoading = false
                                }
                            }
                        },
                        enabled = !isAiLimitReached && !isReplyLoading && replyExtraNote.isNotBlank(),
                        isLoading = isReplyLoading,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    if (replyError != null) {
                        Text(replyError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            // Suggestions List
            if (repliesList.isNotEmpty()) {
                Text("الاقتراحات المولدة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                repliesList.forEach { (variant, text) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("صيغة: $variant", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 12.sp)
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Reply", text)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "تم نسخ الرد بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Share, contentDescription = "نسخ", tint = Color(0xFF15803D))
                                }
                            }
                            Text(text, color = Color(0xFF1E293B), fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            // Smart Caption Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("إعدادات كابشن منصات التواصل", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Platform
                    Text("المنصة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("instagram" to "انستغرام", "twitter" to "تويتر", "facebook" to "فيسبوك").forEach { (code, label) ->
                            FilterChip(
                                selected = platform == code,
                                onClick = { platform = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Dialect
                    Text("اللهجة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("gulf" to "خليجي", "egyptian" to "مصري", "standard" to "فصحى").forEach { (code, label) ->
                            FilterChip(
                                selected = captionDialect == code,
                                onClick = { captionDialect = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Tone
                    Text("الأسلوب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("sales" to "تسويقي", "friendly" to "ودي", "funny" to "مرح").forEach { (code, label) ->
                            FilterChip(
                                selected = captionTone == code,
                                onClick = { captionTone = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    TijarioTextField(
                        label = "المنتج أو الخدمة",
                        value = productOrService,
                        onValueChange = { productOrService = it }
                    )

                    TijarioTextField(
                        label = "تفاصيل العرض (اختياري)",
                        value = offer,
                        onValueChange = { offer = it }
                    )

                    TijarioTextField(
                        label = "ملاحظات إضافية",
                        value = captionExtraNote,
                        onValueChange = { captionExtraNote = it },
                        singleLine = false
                    )

                    TijarioButton(
                        text = "توليد الكابشن",
                        onClick = {
                            scope.launch {
                                try {
                                    isCaptionLoading = true
                                    captionError = null
                                    captionsList = emptyList()
                                    val req = app.tijario.data.remote.AiCaptionRequest(
                                        captionType = captionType,
                                        platform = platform,
                                        dialect = captionDialect,
                                        tone = captionTone,
                                        length = captionLength,
                                        productOrService = productOrService,
                                        offer = offer.ifBlank { null },
                                        extraNote = captionExtraNote.ifBlank { null }
                                    )
                                    val res = dataViewModel.generateAiCaption(req)
                                    if (res.ok) {
                                        captionsList = res.data?.captions?.toList() ?: emptyList()
                                    } else {
                                        captionError = res.displayMessage
                                    }
                                } catch (e: Exception) {
                                    captionError = e.message ?: "حدث خطأ غير متوقع"
                                } finally {
                                    isCaptionLoading = false
                                }
                            }
                        },
                        enabled = !isAiLimitReached && !isCaptionLoading && productOrService.isNotBlank(),
                        isLoading = isCaptionLoading,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    if (captionError != null) {
                        Text(captionError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            // Captions List
            if (captionsList.isNotEmpty()) {
                Text("الاقتراحات المولدة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                captionsList.forEach { (variant, item) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("صيغة: $variant", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 12.sp)
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val textToCopy = "${item.caption}\n\n${item.cta}\n\n${item.hashtags.joinToString(" ")}"
                                    val clip = android.content.ClipData.newPlainText("Caption", textToCopy)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "تم نسخ الكابشن بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Share, contentDescription = "نسخ", tint = Color(0xFF15803D))
                                }
                            }
                            Text(item.caption, color = Color(0xFF1E293B), fontSize = 14.sp)
                            Text(item.cta, color = Color(0xFF16A34A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (item.hashtags.isNotEmpty()) {
                                Text(item.hashtags.joinToString(" "), color = Color(0xFF2563EB), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    dataViewModel: TijarioDataViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Store, 1 = Personal
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()

    var currentUserEmail by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var isLogoUploading by remember { mutableStateOf(false) }
    val businessSettings = uiState.businessSettings

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                dataViewModel.refreshAll()
                val user = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (user != null) {
                    currentUserEmail = user.email ?: ""
                    currentUserName = user.userMetadata?.get("full_name")?.toString() ?: ""
                }
            } catch (e: Exception) {
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
                                    dataViewModel.cacheBusinessSettings(settings.copy(logoUrl = logoUrl))
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
                                val result = dataViewModel.saveBusinessSettings(updated)
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar("تم حفظ تغييرات المتجر بنجاح")
                                } else {
                                    snackbarHostState.showSnackbar("خطأ في الحفظ")
                                }
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
