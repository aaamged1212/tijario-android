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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
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
                        label = t("available_stock_optional"),
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
    var isLogoUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Caching business logo
    val logoUrl = uiState.businessSettings?.logoUrl
    val logoBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, logoUrl) {
        value = null
        if (!logoUrl.isNullOrBlank()) {
            value = loadCachedLogoBitmap(context, logoUrl)
        }
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val settings = uiState.businessSettings
                if (settings == null) {
                    errorMessage = Localization.getString("complete_settings_logo", language)
                    return@launch
                }
                try {
                    isLogoUploading = true
                    errorMessage = null
                    val uploadRequest = buildLogoUploadRequest(context, uri, language)
                    val result = app.tijario.config.Supabase.apiClient.uploadBusinessLogo(uploadRequest)
                    val uploadedUrl = result.data?.logoUrl
                    if (result.ok && !uploadedUrl.isNullOrBlank()) {
                        dataViewModel.cacheBusinessSettings(settings.copy(logoUrl = uploadedUrl))
                        dataViewModel.refreshAll()
                    } else {
                        errorMessage = result.displayMessage
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: Localization.getString("logo_upload_error", language)
                } finally {
                    isLogoUploading = false
                }
            }
        }
    }

    // Dialog control states
    var activeDialog by remember { mutableStateOf<String?>(null) } // "name", "phone", "country", "city", "currency", "terms"

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
                            modifier = Modifier
                                .size(60.dp)
                                .clickable(enabled = !isLogoUploading) { logoPicker.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when {
                                    isLogoUploading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0D9488))
                                    logoBitmap != null -> Image(
                                        bitmap = logoBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    else -> Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = null,
                                        tint = Color(0xFF0D9488),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
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
    }
}

private suspend fun loadCachedLogoBitmap(
    context: Context,
    logoUrl: String,
): android.graphics.Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val cacheDir = File(context.filesDir, "business-logo-cache").apply { mkdirs() }
            val cacheFile = File(cacheDir, "${logoUrl.sha256()}.img")

            if (cacheFile.exists() && cacheFile.length() > 0) {
                BitmapFactory.decodeFile(cacheFile.absolutePath)?.let { return@withContext it }
            }

            val bytes = URL(logoUrl).openStream().use { stream ->
                stream.readBytes()
            }

            if (bytes.isNotEmpty()) {
                cacheFile.writeBytes(bytes)
            }

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

private suspend fun buildLogoUploadRequest(
    context: Context,
    uri: Uri,
    language: AppLanguage,
): app.tijario.data.remote.UploadLogoRequest =
    withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

        require(mimeType in allowedMimeTypes) {
            Localization.getString("logo_format_error", language)
        }

        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("تعذر قراءة صورة الشعار.")

        require(bytes.isNotEmpty()) {
            Localization.getString("logo_empty_error", language)
        }
        require(bytes.size <= 2 * 1024 * 1024) {
            val errStr = if (language == AppLanguage.EN) "Logo size must not exceed 2MB." else "حجم الشعار يجب ألا يتجاوز 2MB."
            errStr
        }

        app.tijario.data.remote.UploadLogoRequest(
            fileName = "logo",
            mimeType = mimeType,
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
        )
    }

val DocumentFormStateSaver = listSaver<DocumentFormState, Any>(
    save = { state ->
        val list = mutableListOf<Any>(
            state.customerId ?: "",
            state.customerName,
            state.customerWhatsapp,
            state.customerCity ?: "",
            state.discount,
            state.extraFees,
            state.paymentStatus,
            state.amountPaid,
            state.notes,
            state.terms,
            state.documentNumber,
            state.issueDate,
            state.creationDate,
            state.dueTerms,
            state.dueDate,
            state.poNumber,
            state.documentTitle
        )
        state.items.forEach { item ->
            list.add(item.id)
            list.add(item.productId ?: "")
            list.add(item.name)
            list.add(item.quantity)
            list.add(item.unitPrice)
            list.add(item.description)
            list.add(item.unitOfMeasure)
            list.add(item.discount)
            list.add(item.discountType)
            list.add(item.taxRate)
        }
        list
    },
    restore = { list ->
        val customerId = list[0] as String
        val customerName = list[1] as String
        val customerWhatsapp = list[2] as String
        val customerCity = list[3] as String
        val discount = list[4] as String
        val extraFees = list[5] as String
        val paymentStatus = list[6] as String
        val amountPaid = list[7] as String
        val notes = list[8] as String
        val terms = list[9] as String
        val documentNumber = list[10] as String
        val issueDate = list[11] as String
        val creationDate = list[12] as String
        val dueTerms = list[13] as String
        val dueDate = list[14] as String
        val poNumber = list[15] as String
        val documentTitle = list[16] as String
        
        val itemsList = mutableListOf<app.tijario.ui.state.DocumentItemState>()
        val itemsData = list.subList(17, list.size)
        for (i in itemsData.indices step 10) {
            if (i + 9 < itemsData.size) {
                itemsList.add(
                    app.tijario.ui.state.DocumentItemState(
                        id = itemsData[i] as String,
                        productId = (itemsData[i+1] as String).takeIf { it.isNotEmpty() },
                        name = itemsData[i+2] as String,
                        quantity = itemsData[i+3] as String,
                        unitPrice = itemsData[i+4] as String,
                        description = itemsData[i+5] as String,
                        unitOfMeasure = itemsData[i+6] as String,
                        discount = itemsData[i+7] as String,
                        discountType = itemsData[i+8] as String,
                        taxRate = itemsData[i+9] as String,
                    )
                )
            }
        }
        
        DocumentFormState(
            customerId = customerId.takeIf { it.isNotEmpty() },
            customerName = customerName,
            customerWhatsapp = customerWhatsapp,
            customerCity = customerCity.takeIf { it.isNotEmpty() },
items = if (itemsList.isEmpty()) listOf(app.tijario.ui.state.DocumentItemState()) else itemsList,
            discount = discount,
            extraFees = extraFees,
            paymentStatus = paymentStatus,
            amountPaid = amountPaid,
            notes = notes,
            terms = terms,
            documentNumber = documentNumber,
            issueDate = issueDate,
            creationDate = creationDate,
            dueTerms = dueTerms,
            dueDate = dueDate,
            poNumber = poNumber,
            documentTitle = documentTitle
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceInfoDialog(
    form: app.tijario.ui.state.DocumentFormState,
    onDismiss: () -> Unit,
    onSave: (
        documentNumber: String,
        creationDate: String,
        dueTerms: String,
        dueDate: String,
        poNumber: String,
        documentTitle: String
    ) -> Unit
) {
    var invoiceNumber by remember { mutableStateOf(form.documentNumber) }
    var creationDate by remember { mutableStateOf(form.creationDate.ifBlank { "21-06-2026" }) }
    var dueTerms by remember { mutableStateOf(form.dueTerms) }
    var dueDate by remember { mutableStateOf(form.dueDate) }
    var poNumber by remember { mutableStateOf(form.poNumber) }
    var invoiceTitle by remember { mutableStateOf(form.documentTitle) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(t("invoice_info_title"), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onSave(invoiceNumber, creationDate, dueTerms, dueDate, poNumber, invoiceTitle)
                                },
                                enabled = invoiceNumber.isNotBlank() && creationDate.isNotBlank()
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Save")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TijarioTextField(
                                label = t("invoice_number") + " *",
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it }
                            )

                            TijarioTextField(
                                label = t("creation_date") + " *",
                                value = creationDate,
                                onValueChange = { creationDate = it }
                            )

                            var showTermsDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                TijarioTextField(
                                    label = t("due_terms"),
                                    value = dueTerms,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showTermsDropdown = true }) {
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = showTermsDropdown,
                                    onDismissRequest = { showTermsDropdown = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val termsOptions = listOf("None", "Net 7", "Net 15", "Net 30")
                                    termsOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                dueTerms = option
                                                showTermsDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            TijarioTextField(
                                label = t("due_date"),
                                value = dueDate,
                                onValueChange = { dueDate = it }
                            )

                            TijarioTextField(
                                label = t("po_number"),
                                value = poNumber,
                                onValueChange = { poNumber = it }
                            )

                            TijarioTextField(
                                label = t("invoice_title_name"),
                                value = invoiceTitle,
                                onValueChange = { invoiceTitle = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTemplateDialog(
    selectedTemplateId: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedId by remember { mutableStateOf(selectedTemplateId) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(t("select_template"), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { onSave(selectedId) }
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Save")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DocumentTemplateRegistry.templates) { template ->
                        val selected = template.id == selectedId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedId = template.id
                                    onSave(template.id)
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(12.dp)
                                                .background(Color(android.graphics.Color.parseColor(template.accentColor)))
                                        )
                                        Box(modifier = Modifier.width(50.dp).height(6.dp).background(Color.LightGray))
                                        Box(modifier = Modifier.width(30.dp).height(4.dp).background(Color.LightGray))
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                                        Box(modifier = Modifier.fillMaxWidth().height(20.dp).background(Color(0xFFE2E8F0)))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = template.name.removePrefix("Tijario "),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    if (template.id.contains("elegant") || template.id.contains("bold") || template.id.contains("premium")) {
                                        Text(
                                            text = "VIP",
                                            color = Color(0xFFD97706),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            modifier = Modifier
                                                .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
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
fun EditItemDialog(
    item: app.tijario.ui.state.DocumentItemState,
    onDismiss: () -> Unit,
    onSave: (app.tijario.ui.state.DocumentItemState) -> Unit,
    onDelete: () -> Unit,
    onChooseProduct: () -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.unitPrice) }
    var quantity by remember { mutableStateOf(item.quantity) }
    var unitOfMeasure by remember { mutableStateOf(item.unitOfMeasure) }
    var discount by remember { mutableStateOf(item.discount) }
    var discountType by remember { mutableStateOf(item.discountType) }
    var taxRate by remember { mutableStateOf(item.taxRate) }
    var description by remember { mutableStateOf(item.description) }

    val parsedPrice = Validation.parseNonNegativeMoney(price) ?: 0.0
    val parsedQty = Validation.parsePositiveInt(quantity) ?: 1
    val parsedDiscount = Validation.parseNonNegativeMoney(discount) ?: 0.0
    val parsedTax = Validation.parseNonNegativeMoney(taxRate) ?: 0.0

    val subtotal = parsedPrice * parsedQty
    val discountAmt = if (discountType == "Percentage") {
        subtotal * (parsedDiscount / 100.0)
    } else {
        parsedDiscount
    }
    val taxAmt = (subtotal - discountAmt) * (parsedTax / 100.0)
    val totalAmount = subtotal - discountAmt + taxAmt

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(t("edit_item_title"), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                            }
                        },
                        actions = {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                            IconButton(
                                onClick = {
                                    onSave(
                                        item.copy(
                                            name = name,
                                            unitPrice = price,
                                            quantity = quantity,
                                            description = description,
                                            unitOfMeasure = unitOfMeasure,
                                            discount = discount,
                                            discountType = discountType,
                                            taxRate = taxRate
                                        )
                                    )
                                },
                                enabled = name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Save")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TijarioTextField(
                                    label = t("item_name_label") + " *",
                                    value = name,
                                    onValueChange = { name = it },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onChooseProduct,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            TijarioTextField(
                                label = t("item_price_label"),
                                value = price,
                                onValueChange = { price = it }
                            )

                            TijarioTextField(
                                label = t("item_qty_label"),
                                value = quantity,
                                onValueChange = { quantity = it }
                            )

                            TijarioTextField(
                                label = t("unit_measure_label"),
                                value = unitOfMeasure,
                                onValueChange = { unitOfMeasure = it }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TijarioTextField(
                                    label = t("item_discount"),
                                    value = discount,
                                    onValueChange = { discount = it },
                                    modifier = Modifier.weight(1f)
                                )
                                var showMenu by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(onClick = { showMenu = true }) {
                                        Text(if (discountType == "Percentage") "%" else "$")
                                    }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Percentage") },
                                            onClick = {
                                                discountType = "Percentage"
                                                showMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Fixed") },
                                            onClick = {
                                                discountType = "Fixed"
                                                showMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            TijarioTextField(
                                label = t("item_tax_rate"),
                                value = taxRate,
                                onValueChange = { taxRate = it }
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = t("amount_label"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = String.format("$%.2f", totalAmount),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TijarioTextField(
                                label = t("item_desc_label"),
                                value = description,
                                onValueChange = { description = it },
                                singleLine = false
                            )
                        }
                    }
                }
            }
        }
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
    onNavigateToBusinessSettings: () -> Unit = {},
) {
    var form by rememberSaveable(stateSaver = DocumentFormStateSaver) { mutableStateOf(DocumentFormState()) }
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

    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var showInvoiceInfoDialog by remember { mutableStateOf(false) }
    var showTemplatePickerDialog by remember { mutableStateOf(false) }

    val nextDocNumber = remember(uiState.documents, type) {
        val prefix = if (type == app.tijario.data.model.DocumentType.Invoice) "INV-" else "QT-"
        val typedDocs = uiState.documents.filter { it.type == type }
        val maxNum = typedDocs.mapNotNull { doc ->
            val clean = doc.documentNumber.uppercase()
                .removePrefix("INV-")
                .removePrefix("QT-")
                .removePrefix("INV_")
                .removePrefix("QT_")
                .trim()
            clean.toIntOrNull()
        }.maxOrNull() ?: 284 // Default sequence helper starting after last invoice like INV-285 if no matches
        "$prefix${maxNum + 1}"
    }

    LaunchedEffect(nextDocNumber, isEditMode) {
        if (!isEditMode && form.documentNumber.isBlank()) {
            form = form.copy(documentNumber = nextDocNumber)
        }
    }

    fun moveItemUp(index: Int) {
        if (index > 0) {
            val list = form.items.toMutableList()
            val temp = list[index]
            list[index] = list[index - 1]
            list[index - 1] = temp
            form = form.copy(items = list)
        }
    }

    fun moveItemDown(index: Int) {
        if (index < form.items.size - 1) {
            val list = form.items.toMutableList()
            val temp = list[index]
            list[index] = list[index + 1]
            list[index + 1] = temp
            form = form.copy(items = list)
        }
    }

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
        },
        bottomBar = {
            if (!isLoadingDocument) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(t("btn_preview"), fontWeight = FontWeight.Bold)
                        }

                         Button(
                            onClick = {
                                if (isLoading) return@Button
                                if (form.customerId == null) {
                                    android.widget.Toast.makeText(context, "يرجى اختيار العميل أولاً", android.widget.Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (form.items.isEmpty()) {
                                    android.widget.Toast.makeText(context, "يرجى إضافة بند واحد على الأقل", android.widget.Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (!form.items.all { it.isValid }) {
                                    android.widget.Toast.makeText(context, "يرجى إدخال اسم البند والكمية والسعر بشكل صحيح", android.widget.Toast.LENGTH_LONG).show()
                                    return@Button
                                }

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
                                                    description = itm.description.ifBlank { null },
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
                            enabled = !isLoading,
                            modifier = Modifier
                                .weight(2.5f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(t("btn_save"), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Top Card (e.g. INV-1132 / Created on 21-06-2026)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInvoiceInfoDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = form.documentNumber.ifBlank {
                                    if (type == app.tijario.data.model.DocumentType.Invoice) "INV-XXXX" else "QT-XXXX"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(t("created_on"), form.creationDate.ifBlank { "21-06-2026" }),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Language selection & Templates row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* No action needed */ }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(t("invoice_language"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("English", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTemplatePickerDialog = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(t("templates"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Store From and Client Bill To Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToBusinessSettings() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t("from"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val storeName = businessSettings?.businessName?.takeIf { it.isNotBlank() } ?: "TAWFIR - توفير"
                                val storeCity = businessSettings?.city?.takeIf { it.isNotBlank() } ?: "Sana'a"
                                val storeNotes = businessSettings?.invoiceNote?.takeIf { it.isNotBlank() } ?: "Hada city / Zero st"
                                Text(storeName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(storeCity, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(storeNotes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSelectCustomer() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t("bill_to"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (form.customerName.isNotEmpty()) form.customerName else t("add_client"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Items list section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = String.format(t("items_count"), form.items.size),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        form.items.forEachIndexed { index, item ->
                            key(item.id) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .clickable { editingItemIndex = index }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Reordering controls
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.DragHandle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            IconButton(
                                                onClick = { moveItemUp(index) },
                                                modifier = Modifier.size(24.dp),
                                                enabled = index > 0
                                            ) {
                                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { moveItemDown(index) },
                                                modifier = Modifier.size(24.dp),
                                                enabled = index < form.items.size - 1
                                            ) {
                                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Item details (compact layout)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name.ifBlank { "اسم البند" },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${item.quantity.ifBlank { "0" }} x $${item.unitPrice.ifBlank { "0.00" }}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Total amount
                                    val parsedPrice = Validation.parseNonNegativeMoney(item.unitPrice) ?: 0.0
                                    val parsedQty = Validation.parsePositiveInt(item.quantity) ?: 1
                                    val parsedDiscount = Validation.parseNonNegativeMoney(item.discount) ?: 0.0
                                    val parsedTax = Validation.parseNonNegativeMoney(item.taxRate) ?: 0.0
                                    val sub = parsedPrice * parsedQty
                                    val disc = if (item.discountType == "Percentage") sub * (parsedDiscount / 100.0) else parsedDiscount
                                    val tax = (sub - disc) * (parsedTax / 100.0)
                                    val itemTotal = sub - disc + tax

                                    Text(
                                        text = String.format("$%.2f", itemTotal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
                        OutlinedButton(
                            onClick = {
                                form = form.copy(items = form.items + app.tijario.ui.state.DocumentItemState())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "إضافة بند" else "Add Item", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Subtotal summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t("subtotal"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val subtotalVal = form.items.sumOf { item ->
                                val parsedPrice = Validation.parseNonNegativeMoney(item.unitPrice) ?: 0.0
                                val parsedQty = Validation.parsePositiveInt(item.quantity) ?: 1
                                parsedPrice * parsedQty
                            }
                            Text(text = String.format("$%.2f", subtotalVal), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }

                // Discount, tax, payments, notes section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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

    // Launch Edit Item Dialog when requested
    editingItemIndex?.let { index ->
        if (index in form.items.indices) {
            EditItemDialog(
                item = form.items[index],
                onDismiss = { editingItemIndex = null },
                onSave = { updated ->
                    form = form.copy(
                        items = form.items.mapIndexed { idx, itm ->
                            if (idx == index) updated else itm
                        }
                    )
                    editingItemIndex = null
                },
                onDelete = {
                    form = form.copy(
                        items = form.items.filterIndexed { idx, _ -> idx != index }
                    )
                    editingItemIndex = null
                },
                onChooseProduct = {
                    editingItemIndex = null
                    onNavigateToSelectProduct(index)
                }
            )
        }
    }

    if (showInvoiceInfoDialog) {
        InvoiceInfoDialog(
            form = form,
            onDismiss = { showInvoiceInfoDialog = false },
            onSave = { docNum, date, terms, due, po, title ->
                form = form.copy(
                    documentNumber = docNum,
                    creationDate = date,
                    dueTerms = terms,
                    dueDate = due,
                    poNumber = po,
                    documentTitle = title
                )
                showInvoiceInfoDialog = false
            }
        )
    }

    if (showTemplatePickerDialog) {
        SelectTemplateDialog(
            selectedTemplateId = selectedTemplateId,
            onDismiss = { showTemplatePickerDialog = false },
            onSave = { templateId ->
                selectedTemplateId = DocumentTemplateRegistry.requireTemplate(templateId).id
                templatePreferences.setDefaultTemplateId(selectedTemplateId)
                showTemplatePickerDialog = false
            }
        )
    }
}
