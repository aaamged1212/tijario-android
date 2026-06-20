package app.tijario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import app.tijario.config.AppLanguage
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.domain.Validation
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.ui.DocumentTemplatePicker
import app.tijario.features.documents.ui.DocumentTemplatePreferences
import app.tijario.ui.components.ModernDocumentPreview
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.CustomerFormState
import app.tijario.ui.state.DocumentFormState
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch
import android.net.Uri
import java.io.File
import java.security.MessageDigest
import java.net.URL
import android.graphics.BitmapFactory
import android.content.Context
import android.util.Base64
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    dataViewModel: TijarioDataViewModel,
    customerId: String? = null,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    var form by remember { mutableStateOf(CustomerFormState()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isEditMode = customerId != null

    LaunchedEffect(customerId) {
        if (customerId != null) {
            val existing = dataViewModel.uiState.value.customers.find { it.id == customerId }
            if (existing != null) {
                form = CustomerFormState(
                    name = existing.name,
                    whatsapp = existing.whatsappNumber,
                    city = existing.city.orEmpty(),
                    notes = existing.notes.orEmpty()
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "تعديل عميل" else t("btn_add_customer"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    TijarioTextField(
                        label = t("fullname"),
                        value = form.name,
                        onValueChange = { form = form.copy(name = it) },
                        error = if (form.name.isNotEmpty()) form.nameError else null,
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    TijarioTextField(
                        label = t("whatsapp_phone"),
                        value = form.whatsapp,
                        onValueChange = { form = form.copy(whatsapp = it) },
                        error = if (form.whatsapp.isNotEmpty()) form.whatsappError else null,
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    TijarioTextField(
                        label = t("city"),
                        value = form.city,
                        onValueChange = { form = form.copy(city = it) },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    TijarioTextField(
                        label = t("notes"),
                        value = form.notes,
                        onValueChange = { form = form.copy(notes = it) },
                        singleLine = false,
                        leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TijarioButton(
                        text = if (isEditMode) t("edit_customer") else t("btn_save_customer"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val customer = app.tijario.data.model.Customer(
                                        id = customerId ?: java.util.UUID.randomUUID().toString(),
                                        name = form.name,
                                        whatsappNumber = form.whatsapp,
                                        city = form.city.ifBlank { null },
                                        notes = form.notes.ifBlank { null }
                                    )
                                    val result = if (isEditMode) {
                                        dataViewModel.updateCustomer(customer)
                                    } else {
                                        dataViewModel.createCustomer(customer)
                                    }
                                    if (result.isSuccess) {
                                        onBack()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message
                                            ?: Localization.getString("save_customer_error", language)
                                    }
                                } catch (e: Exception) {
                                    errorMessage = Localization.getString("save_customer_error", language)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun countryOptions(): List<String> = listOf(
    "السعودية",
    "اليمن",
    "الإمارات",
    "مصر",
    "الكويت",
    "قطر",
    "عمان",
    "البحرين",
    "الأردن",
    "لبنان",
    "المغرب",
    "تونس",
    "الجزائر",
    "ليبيا",
    "السودان",
    "العراق",
    "سوريا",
    "فلسطين",
)

private fun currencyOptions(): List<String> = listOf(
    "SAR",
    "YER",
    "USD",
    "AED",
    "EGP",
    "KWD",
    "BHD",
    "OMR",
    "QAR",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    dataViewModel: TijarioDataViewModel,
    productId: String? = null,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    var form by remember { mutableStateOf(app.tijario.ui.state.ProductFormState()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isEditMode = productId != null

    LaunchedEffect(productId) {
        if (productId != null) {
            val existing = dataViewModel.uiState.value.products.find { it.id == productId }
            if (existing != null) {
                form = app.tijario.ui.state.ProductFormState(
                    name = existing.name,
                    description = existing.description.orEmpty(),
                    price = Validation.normalizedMoneyString(existing.price.toString()),
                    stockQuantity = existing.stockQuantity?.toString().orEmpty(),
                    kind = existing.kind,
                    currency = existing.currency
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "تعديل المنتج" else t("btn_add_product"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    TijarioTextField(
                        label = t("product_name"),
                        value = form.name,
                        onValueChange = { form = form.copy(name = it) },
                        error = if (form.name.isNotEmpty()) form.nameError else null,
                        leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    TijarioTextField(
                        label = t("product_description"),
                        value = form.description,
                        onValueChange = { form = form.copy(description = it) },
                        singleLine = false,
                        leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    TijarioTextField(
                        label = t("product_price"),
                        value = form.price,
                        onValueChange = { form = form.copy(price = it) },
                        error = if (form.price.isNotEmpty()) form.priceError else null,
                        leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    TijarioTextField(
                        label = "الكمية المتوفرة (اختياري)",
                        value = form.stockQuantity,
                        onValueChange = { form = form.copy(stockQuantity = it) },
                        error = if (form.stockQuantity.isNotEmpty()) form.stockQuantityError else null,
                        leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    Text(
                        text = t("product_kind"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = form.kind == app.tijario.data.model.ProductKind.Product,
                                onClick = { form = form.copy(kind = app.tijario.data.model.ProductKind.Product) }
                            )
                            Text(
                                text = t("kind_product"),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = form.kind == app.tijario.data.model.ProductKind.Service,
                                onClick = { form = form.copy(kind = app.tijario.data.model.ProductKind.Service) }
                            )
                            Text(
                                text = t("kind_service"),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TijarioButton(
                        text = if (isEditMode) t("edit_product") else t("btn_save_product"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val product = app.tijario.data.model.Product(
                                        id = productId ?: java.util.UUID.randomUUID().toString(),
                                        kind = form.kind,
                                        name = form.name,
                                        description = form.description.ifBlank { null },
                                        price = Validation.parseNonNegativeMoney(form.price) ?: 0.0,
                                        stockQuantity = Validation.parseNonNegativeInt(form.stockQuantity),
                                        currency = form.currency
                                    )
                                    val result = if (isEditMode) {
                                        dataViewModel.updateProduct(product)
                                    } else {
                                        dataViewModel.createProduct(product)
                                    }
                                    if (result.isSuccess) {
                                        onBack()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message
                                            ?: Localization.getString("save_product_error", language)
                                    }
                                } catch (e: Exception) {
                                    errorMessage = Localization.getString("save_product_error", language)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = form.canSubmit,
                        isLoading = isLoading
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSettingsScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    var form by remember { mutableStateOf(BusinessSettingsFormState()) }
    var existingSettings by remember { mutableStateOf<app.tijario.data.model.BusinessSettings?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()

    // Dialog control states
    var activeDialog by remember { mutableStateOf<String?>(null) } // "name", "category", "phone", "country", "city", "currency", "terms"

    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    LaunchedEffect(uiState.businessSettings) {
        val settings = uiState.businessSettings
        if (settings != null && settings != existingSettings) {
            existingSettings = settings
            form = BusinessSettingsFormState(
                businessName = settings.businessName,
                whatsapp = settings.whatsappNumber,
                country = settings.country,
                city = settings.city ?: "",
                currency = settings.currency,
                terms = settings.termsText ?: ""
            )
        }
    }

    // Edit Dialog Popups
    if (activeDialog != null) {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(t("edit_customer"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (activeDialog) {
                        "name" -> {
                            TijarioTextField(
                                label = t("shop_name"),
                                value = form.businessName,
                                onValueChange = { form = form.copy(businessName = it) },
                                error = if (form.businessName.isNotEmpty()) form.businessNameError else null,
                                leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                        "category" -> {
                            TijarioTextField(
                                label = t("business_category"),
                                value = t("category_default_value"),
                                onValueChange = { /* category is mock/default in setup */ },
                                leadingIcon = { Icon(Icons.Filled.GridView, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                        "phone" -> {
                            TijarioTextField(
                                label = t("whatsapp_phone"),
                                value = form.whatsapp,
                                onValueChange = { form = form.copy(whatsapp = it) },
                                error = if (form.whatsapp.isNotEmpty()) form.whatsappError else null,
                                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                        "country" -> {
                            SettingsDropdownField(
                                label = t("country"),
                                value = form.country,
                                options = countryOptions(),
                                onValueChange = { form = form.copy(country = it) },
                                error = if (form.country.isNotEmpty()) form.countryError else null
                            )
                        }
                        "city" -> {
                            TijarioTextField(
                                label = t("city"),
                                value = form.city,
                                onValueChange = { form = form.copy(city = it) },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                        "currency" -> {
                            SettingsDropdownField(
                                label = t("currency"),
                                value = form.currency,
                                options = currencyOptions(),
                                onValueChange = { form = form.copy(currency = it) },
                                error = if (form.currency.isNotEmpty()) form.currencyError else null
                            )
                        }
                        "terms" -> {
                            TijarioTextField(
                                label = t("terms"),
                                value = form.terms,
                                onValueChange = { form = form.copy(terms = it) },
                                singleLine = false,
                                leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Text(t("btn_ok"))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("tab_store_account"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            // Header store card Teal/Green gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF033E43), Color(0xFF0D9488))
                            )
                        )
                        .padding(24.dp)
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
                            color = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = form.businessName.ifBlank { "تجاريو" },
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(10.dp))
                                        Text(t("verified_store"), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = t("store_slogan"),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Edit Icon
                    Surface(
                        color = Color.Transparent,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { activeDialog = "name" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Options card list matching the screen layout exactly
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Row 1: اسم المتجر / النشاط التجاري
                    SettingsItemRow(
                        icon = Icons.Filled.Storefront,
                        title = t("shop_name"),
                        value = form.businessName.ifBlank { "تجاريو" },
                        onClick = { activeDialog = "name" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 2: تصنيف النشاط التجاري
                    SettingsItemRow(
                        icon = Icons.Filled.GridView,
                        title = t("business_category"),
                        value = t("category_default_value"),
                        onClick = { activeDialog = "category" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 3: رقم التواصل
                    val maskedPhone = remember(form.whatsapp) {
                        if (form.whatsapp.length > 4) "•••• " + form.whatsapp.takeLast(4) else "•••• ••••"
                    }
                    SettingsItemRow(
                        icon = Icons.Filled.Phone,
                        title = t("contact_phone"),
                        value = maskedPhone,
                        onClick = { activeDialog = "phone" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 4: الدولة
                    SettingsItemRow(
                        icon = Icons.Filled.Public,
                        title = t("country"),
                        value = form.country.ifBlank { "اليمن" },
                        onClick = { activeDialog = "country" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 5: المدينة
                    SettingsItemRow(
                        icon = Icons.Filled.Domain,
                        title = t("city"),
                        value = form.city.ifBlank { "صنعاء" },
                        onClick = { activeDialog = "city" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 6: العملة
                    val displayCurrency = when (form.currency.uppercase()) {
                        "YER" -> "ريال يمني - YER"
                        "SAR" -> "الريال السعودي - SAR"
                        "USD" -> "الدولار الأمريكي - USD"
                        else -> form.currency
                    }
                    SettingsItemRow(
                        icon = Icons.Filled.AttachMoney,
                        title = t("currency"),
                        value = displayCurrency,
                        onClick = { activeDialog = "currency" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 7: تفضيلات الفواتير والإشعارات
                    SettingsItemRow(
                        icon = Icons.Filled.Receipt,
                        title = t("invoice_notifications_prefs"),
                        value = t("billing_prefs_value"),
                        onClick = { activeDialog = "terms" }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save store changes button
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null
                            val settings = existingSettings?.copy(
                                businessName = form.businessName,
                                whatsappNumber = form.whatsapp,
                                country = form.country,
                                city = form.city.ifBlank { null },
                                currency = form.currency,
                                termsText = form.terms.ifBlank { null },
                            ) ?: app.tijario.data.model.BusinessSettings(
                                userId = uiState.userId,
                                businessName = form.businessName,
                                whatsappNumber = form.whatsapp,
                                country = form.country,
                                city = form.city.ifBlank { null },
                                currency = form.currency,
                                termsText = form.terms.ifBlank { null },
                            )
                            val result = dataViewModel.saveBusinessSettings(settings)
                            if (result.isSuccess) {
                                onBack()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message
                                    ?: Localization.getString("save_settings_error", language)
                            }
                        } catch (e: Exception) {
                            errorMessage = Localization.getString("save_settings_error", language)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                enabled = form.canSubmit && !isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(t("save_store_changes"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF0F2D54), modifier = Modifier.size(20.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentFormScreen(
    dataViewModel: TijarioDataViewModel,
    type: app.tijario.data.model.DocumentType,
    documentId: String? = null,
    onBack: () -> Unit,
    onDocumentSaved: (String) -> Unit = {},
    onNavigateToSelectCustomer: () -> Unit = {},
    onNavigateToSelectProduct: (Int) -> Unit = {},
    selectedCustomer: app.tijario.data.model.Customer? = null,
    selectedProduct: app.tijario.data.model.Product? = null,
    selectedProductRowIndex: Int? = null,
    onSelectedProductConsumed: () -> Unit = {},
) {
    var form by remember { mutableStateOf(DocumentFormState()) }
    var isLoadingDocument by remember { mutableStateOf(documentId != null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = edit, 1 = preview
    var isLoading by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val templatePreferences = remember(context) { DocumentTemplatePreferences(context) }
    var selectedTemplateId by remember { mutableStateOf(templatePreferences.getDefaultTemplateId()) }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val businessSettings = uiState.businessSettings
    val scope = rememberCoroutineScope()
    val editDocumentId = documentId?.takeIf { it.isNotBlank() }
    val isEditMode = editDocumentId != null

    // Sync selected customer
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let {
            form = form.copy(
                customerId = it.id,
                customerName = it.name,
                customerWhatsapp = it.whatsappNumber,
                customerCity = it.city
            )
        }
    }

    LaunchedEffect(documentId) {
        if (editDocumentId != null) {
            isLoadingDocument = true
            submitError = null
            val result = dataViewModel.fetchCompleteDocument(editDocumentId)
            val existing = result.getOrNull()
            if (existing != null) {
                form = existing.toFormState()
            } else {
                submitError = result.exceptionOrNull()?.message ?: "تعذر تحميل المستند للتعديل."
            }
            isLoadingDocument = false
        }
    }

    // Sync selected product to specific row
    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { prod ->
            val idx = selectedProductRowIndex
            if (idx != null && idx in form.items.indices) {
                form = form.copy(
                    items = form.items.mapIndexed { i, item ->
                        if (i == idx) {
                            item.copy(
                                productId = prod.id,
                                name = prod.name,
                                unitPrice = Validation.normalizedMoneyString(prod.price.toString())
                            )
                        } else item
                    }
                )
                onSelectedProductConsumed()
            }
        }
    }

    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditMode) {
                                if (type == app.tijario.data.model.DocumentType.Invoice) "تعديل الفاتورة" else "تعديل عرض السعر"
                            } else if (type == app.tijario.data.model.DocumentType.Invoice) t("btn_new_invoice") else t("btn_new_quote"),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Tabs: edit and preview
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("التعديل", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("المعاينة", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoadingDocument) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (selectedTab == 0) {
            // Edit Mode Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                            t("form_client_info"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Customer Selector Button
                        Button(
                            onClick = onNavigateToSelectCustomer,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null)
                                    Text(
                                        text = if (form.customerName.isNotEmpty()) form.customerName else "اختر العميل",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                if (form.customerWhatsapp.isNotEmpty()) {
                                    Text(
                                        text = form.customerWhatsapp,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Header with Add Item button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                t("form_items_info"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = {
                                form = form.copy(items = form.items + app.tijario.ui.state.DocumentItemState())
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "إضافة بند", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        form.items.forEachIndexed { index, item ->
                            key(item.id) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "البند ${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (form.items.size > 1) {
                                                IconButton(onClick = {
                                                    form = form.copy(items = form.items.filter { it.id != item.id })
                                                }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "حذف البند", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TijarioTextField(
                                                label = "اسم البند",
                                                value = item.name,
                                                onValueChange = { newName ->
                                                    form = form.copy(
                                                        items = form.items.map {
                                                            if (it.id == item.id) it.copy(name = newName) else it
                                                        }
                                                    )
                                                },
                                                error = if (item.name.isNotEmpty()) item.nameError else null,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    onNavigateToSelectProduct(index)
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Filled.Description, contentDescription = "اختر منتجاً", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            TijarioTextField(
                                                label = t("form_quantity"),
                                                value = item.quantity,
                                                onValueChange = { qty ->
                                                    form = form.copy(
                                                        items = form.items.map {
                                                            if (it.id == item.id) it.copy(quantity = qty) else it
                                                        }
                                                    )
                                                },
                                                error = if (item.quantity.isNotEmpty()) item.quantityError else null,
                                                leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null, tint = Color(0xFF64748B)) },
                                                modifier = Modifier.weight(1f)
                                            )
                                            TijarioTextField(
                                                label = t("form_unit_price"),
                                                value = item.unitPrice,
                                                onValueChange = { price ->
                                                    form = form.copy(
                                                        items = form.items.map {
                                                            if (it.id == item.id) it.copy(unitPrice = price) else it
                                                        }
                                                    )
                                                },
                                                error = if (item.unitPrice.isNotEmpty()) item.unitPriceError else null,
                                                leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) },
                                                modifier = Modifier.weight(1.2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TijarioTextField(
                                label = t("form_discount"),
                                value = form.discount,
                                onValueChange = { form = form.copy(discount = it) },
                                error = if (form.discount.isNotEmpty()) form.discountError else null,
                                leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null, tint = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1f)
                            )
                            TijarioTextField(
                                label = t("form_extra_fees"),
                                value = form.extraFees,
                                onValueChange = { form = form.copy(extraFees = it) },
                                error = if (form.extraFees.isNotEmpty()) form.extraFeesError else null,
                                leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        if (type == app.tijario.data.model.DocumentType.Invoice) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "حالة الدفع",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    val options = listOf(
                                        "unpaid" to "غير مدفوعة",
                                        "paid" to "مدفوعة",
                                        "partial" to "دفع جزئي",
                                    )
                                    options.forEachIndexed { index, option ->
                                        SegmentedButton(
                                            selected = form.paymentStatus == option.first,
                                            onClick = { form = form.copy(paymentStatus = option.first) },
                                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                            label = {
                                                Text(
                                                    option.second,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                )
                                            },
                                        )
                                    }
                                }
                            }

                            if (form.paymentStatus == "partial") {
                                Spacer(modifier = Modifier.height(8.dp))
                                TijarioTextField(
                                    label = "المبلغ المدفوع",
                                    value = form.amountPaid,
                                    onValueChange = { form = form.copy(amountPaid = it) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                                    ),
                                    error = form.amountPaidError,
                                    leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) }
                                )
                            }
                        }

                        TijarioTextField(
                            label = t("notes"),
                            value = form.notes,
                            onValueChange = { form = form.copy(notes = it) },
                            singleLine = false,
                            leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF64748B)) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TijarioButton(
                            text = t("btn_save_doc"),
                            onClick = {
                                scope.launch {
                                    try {
                                        isLoading = true
                                        submitError = null
                                        val req = app.tijario.data.remote.CreateDocumentRequest(
                                            type = type,
                                            paymentStatus = if (type == app.tijario.data.model.DocumentType.Invoice) form.paymentStatus else null,
                                            amountPaid = if (type == app.tijario.data.model.DocumentType.Invoice && form.paymentStatus == "partial") Validation.parseNonNegativeMoney(form.amountPaid) else null,
                                            customer = app.tijario.data.remote.DocumentCustomerInput(
                                                name = form.customerName,
                                                whatsappNumber = form.customerWhatsapp,
                                                city = form.customerCity,
                                            ),
                                            items = form.items.map { itm ->
                                                app.tijario.data.remote.DocumentItemInput(
                                                    name = itm.name,
                                                    productId = itm.productId,
                                                    description = null,
                                                    quantity = Validation.parsePositiveInt(itm.quantity) ?: throw IllegalArgumentException("invalid quantity"),
                                                    unitPrice = Validation.parseNonNegativeMoney(itm.unitPrice) ?: throw IllegalArgumentException("invalid price")
                                                )
                                            },
                                            discount = Validation.parseNonNegativeMoney(form.discount) ?: 0.0,
                                            extraFees = Validation.parseNonNegativeMoney(form.extraFees) ?: 0.0,
                                            notes = form.notes.ifBlank { null },
                                            termsText = form.terms.ifBlank { null }
                                        )
                                        val result = if (editDocumentId != null) {
                                            dataViewModel.updateDocument(editDocumentId, req)
                                        } else {
                                            dataViewModel.createDocument(req)
                                        }
                                        if (result.ok) {
                                            val savedDocumentId = result.data?.documentId
                                            if (savedDocumentId.isNullOrBlank()) {
                                                onBack()
                                            } else {
                                                onDocumentSaved(savedDocumentId)
                                            }
                                        } else {
                                            submitError = result.displayMessage
                                        }
                                    } catch (e: Exception) {
                                        submitError = e.message ?: "تعذر حفظ المستند الآن. تحقق من الاتصال وحاول مرة أخرى."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = form.canSubmit,
                            isLoading = isLoading
                        )

                        submitError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "معاينة أولية",
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = "هذه المعاينة محلية وتستخدم نفس القالب الذي سيُستخدم في ملف PDF.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                DocumentTemplatePicker(
                    selectedTemplateId = selectedTemplateId,
                    onTemplateSelected = {
                        selectedTemplateId = DocumentTemplateRegistry.requireTemplate(it).id
                        templatePreferences.setDefaultTemplateId(selectedTemplateId)
                    },
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ModernDocumentPreview(
                        documentType = type,
                        form = form,
                        businessSettings = businessSettings,
                        customerCity = form.customerCity,
                        templateId = selectedTemplateId,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
