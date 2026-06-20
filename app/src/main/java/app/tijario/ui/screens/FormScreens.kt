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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.t
import app.tijario.domain.Validation
import app.tijario.ui.components.ModernDocumentPreview
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.CustomerFormState
import app.tijario.ui.state.DocumentFormState
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    dataViewModel: TijarioDataViewModel,
    customerId: String? = null,
    onBack: () -> Unit,
) {
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
                        text = if (isEditMode) "تعديل وحفظ" else t("btn_save_customer"),
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
                                            ?: "تعذر حفظ العميل. تحقق من البيانات وحاول مرة أخرى."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "تعذر حفظ العميل. تحقق من البيانات وحاول مرة أخرى."
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
fun ProductFormScreen(
    dataViewModel: TijarioDataViewModel,
    productId: String? = null,
    onBack: () -> Unit,
) {
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
                        text = if (isEditMode) "تعديل وحفظ" else t("btn_save_product"),
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
                                            ?: "تعذر حفظ المنتج أو الخدمة. تحقق من البيانات وحاول مرة أخرى."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "تعذر حفظ المنتج أو الخدمة. تحقق من البيانات وحاول مرة أخرى."
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
    var form by remember { mutableStateOf(BusinessSettingsFormState()) }
    var existingSettings by remember { mutableStateOf<app.tijario.data.model.BusinessSettings?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("tab_account"), fontWeight = FontWeight.Bold) },
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
                        label = t("shop_name"),
                        value = form.businessName,
                        onValueChange = { form = form.copy(businessName = it) },
                        error = if (form.businessName.isNotEmpty()) form.businessNameError else null,
                        leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = Color(0xFF64748B)) }
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TijarioTextField(
                            label = t("country"),
                            value = form.country,
                            onValueChange = { form = form.copy(country = it) },
                            error = if (form.country.isNotEmpty()) form.countryError else null,
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF64748B)) },
                            modifier = Modifier.weight(1f)
                        )
                        TijarioTextField(
                            label = t("currency"),
                            value = form.currency,
                            onValueChange = { form = form.copy(currency = it) },
                            error = if (form.currency.isNotEmpty()) form.currencyError else null,
                            leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF64748B)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TijarioTextField(
                        label = t("terms"),
                        value = form.terms,
                        onValueChange = { form = form.copy(terms = it) },
                        singleLine = false,
                        leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF64748B)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TijarioButton(
                        text = t("btn_save_settings"),
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
                                            ?: "تعذر حفظ إعدادات المتجر. حاول مرة أخرى."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "تعذر حفظ إعدادات المتجر. حاول مرة أخرى."
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
fun DocumentFormScreen(
    dataViewModel: TijarioDataViewModel,
    type: app.tijario.data.model.DocumentType,
    onBack: () -> Unit,
    onNavigateToSelectCustomer: () -> Unit = {},
    onNavigateToSelectProduct: () -> Unit = {},
    selectedCustomer: app.tijario.data.model.Customer? = null,
    selectedProduct: app.tijario.data.model.Product? = null
) {
    var form by remember { mutableStateOf(DocumentFormState()) }
    var activeProductSelectRowIndex by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = التعديل (Edit), 1 = المعاينة (Preview)
    var isLoading by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val businessSettings = uiState.businessSettings
    val scope = rememberCoroutineScope()

    // Sync selected customer
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let {
            form = form.copy(
                customerId = it.id,
                customerName = it.name,
                customerWhatsapp = it.whatsappNumber
            )
        }
    }

    // Sync selected product to specific row
    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { prod ->
            val idx = activeProductSelectRowIndex
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
                            if (type == app.tijario.data.model.DocumentType.Invoice) t("btn_new_invoice") else t("btn_new_quote"),
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

                // Tabs: التعديل & المعاينة
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
        if (selectedTab == 0) {
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
                                                    activeProductSelectRowIndex = index
                                                    onNavigateToSelectProduct()
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Filled.Description, contentDescription = "اختر منتجًا", tint = MaterialTheme.colorScheme.primary)
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
                                            customer = app.tijario.data.remote.DocumentCustomerInput(
                                                name = form.customerName,
                                                whatsappNumber = form.customerWhatsapp,
                                                city = selectedCustomer?.city,
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
                                        val result = dataViewModel.createDocument(req)
                                        if (result.ok) {
                                            onBack()
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "معاينة PDF",
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = "هذه المعاينة تتبع قالب Tijario الحديث المستخدم في الويب.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ModernDocumentPreview(
                        documentType = type,
                        form = form,
                        businessSettings = businessSettings,
                        customerCity = selectedCustomer?.city,
                        productDescription = selectedProduct?.description,
                    )
                }
            }
        }
    }
}
