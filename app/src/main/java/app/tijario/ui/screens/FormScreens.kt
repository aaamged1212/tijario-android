package app.tijario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import app.tijario.config.t
import app.tijario.domain.Validation
import app.tijario.ui.components.ModernDocumentPreview
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.CustomerFormState
import app.tijario.ui.state.DocumentFormState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(onBack: () -> Unit) {
    var form by remember { mutableStateOf(CustomerFormState()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("btn_add_customer"), fontWeight = FontWeight.Bold) },
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
                        text = t("btn_save_customer"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
                                        val customer = app.tijario.data.model.Customer(
                                            userId = currentUser.id,
                                            name = form.name,
                                            whatsappNumber = form.whatsapp,
                                            city = form.city.ifBlank { null },
                                            notes = form.notes.ifBlank { null }
                                        )
                                        app.tijario.config.Supabase.client.from("customers")
                                            .insert(customer)
                                        onBack()
                                    } else {
                                        errorMessage = "يجب تسجيل الدخول قبل حفظ العميل."
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
fun ProductFormScreen(onBack: () -> Unit) {
    var form by remember { mutableStateOf(app.tijario.ui.state.ProductFormState()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("btn_add_product"), fontWeight = FontWeight.Bold) },
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
                        text = t("btn_save_product"),
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
                                        val product = app.tijario.data.model.Product(
                                            userId = currentUser.id,
                                            kind = form.kind,
                                            name = form.name,
                                            description = form.description.ifBlank { null },
                                            price = Validation.parseNonNegativeMoney(form.price) ?: 0.0,
                                            currency = form.currency
                                        )
                                        app.tijario.config.Supabase.client.from("products")
                                            .insert(product)
                                        onBack()
                                    } else {
                                        errorMessage = "يجب تسجيل الدخول قبل حفظ المنتج."
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
fun BusinessSettingsScreen(onBack: () -> Unit) {
    var form by remember { mutableStateOf(BusinessSettingsFormState()) }
    var existingSettings by remember { mutableStateOf<app.tijario.data.model.BusinessSettings?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val list = app.tijario.config.Supabase.client.from("business_settings")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.BusinessSettings>()
                    val settings = list.firstOrNull()
                    if (settings != null) {
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
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
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
                                    val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
                                        val settings = existingSettings?.copy(
                                            businessName = form.businessName,
                                            whatsappNumber = form.whatsapp,
                                            country = form.country,
                                            city = form.city.ifBlank { null },
                                            currency = form.currency,
                                            termsText = form.terms.ifBlank { null },
                                        ) ?: app.tijario.data.model.BusinessSettings(
                                            userId = currentUser.id,
                                            businessName = form.businessName,
                                            whatsappNumber = form.whatsapp,
                                            country = form.country,
                                            city = form.city.ifBlank { null },
                                            currency = form.currency,
                                            termsText = form.terms.ifBlank { null },
                                        )
                                        app.tijario.config.Supabase.client.from("business_settings")
                                            .upsert(settings)
                                        onBack()
                                    } else {
                                        errorMessage = "يجب تسجيل الدخول قبل حفظ الإعدادات."
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
    type: app.tijario.data.model.DocumentType,
    onBack: () -> Unit,
    onNavigateToSelectCustomer: () -> Unit = {},
    onNavigateToSelectProduct: () -> Unit = {},
    selectedCustomer: app.tijario.data.model.Customer? = null,
    selectedProduct: app.tijario.data.model.Product? = null
) {
    var form by remember { mutableStateOf(DocumentFormState()) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = التعديل (Edit), 1 = المعاينة (Preview)
    var isLoading by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var businessSettings by remember { mutableStateOf<app.tijario.data.model.BusinessSettings?>(null) }
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

    // Sync selected product
    LaunchedEffect(selectedProduct) {
        selectedProduct?.let {
            form = form.copy(
                productId = it.id,
                itemName = it.name,
                unitPrice = Validation.normalizedMoneyString(it.price.toString())
            )
        }
    }

    // Load business settings for preview
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val currentUser = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val list = app.tijario.config.Supabase.client.from("business_settings")
                        .select {
                            filter {
                                eq("user_id", currentUser.id)
                            }
                        }
                        .decodeList<app.tijario.data.model.BusinessSettings>()
                    businessSettings = list.firstOrNull()
                }
            } catch (e: Exception) {
            }
        }
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

                        Text(
                            t("form_items_info"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Product/Service Selector Button
                        Button(
                            onClick = onNavigateToSelectProduct,
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
                                    Icon(Icons.Filled.Description, contentDescription = null)
                                    Text(
                                        text = if (form.itemName.isNotEmpty()) form.itemName else "اختر المنتج أو الخدمة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                if (form.unitPrice.isNotEmpty() && form.unitPrice != "0") {
                                    Text(
                                        text = "${form.unitPrice} ${businessSettings?.currency ?: "SAR"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TijarioTextField(
                                label = t("form_quantity"),
                                value = form.quantity,
                                onValueChange = { form = form.copy(quantity = it) },
                                error = if (form.quantity.isNotEmpty()) form.quantityError else null,
                                leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null, tint = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1f)
                            )
                            TijarioTextField(
                                label = t("form_unit_price"),
                                value = form.unitPrice,
                                onValueChange = { form = form.copy(unitPrice = it) },
                                error = if (form.unitPrice.isNotEmpty()) form.unitPriceError else null,
                                leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1.2f)
                            )
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
                                            items = listOf(
                                                app.tijario.data.remote.DocumentItemInput(
                                                    name = form.itemName,
                                                    productId = form.productId,
                                                    description = selectedProduct?.description,
                                                    quantity = Validation.parsePositiveInt(form.quantity) ?: 1,
                                                    unitPrice = Validation.parseNonNegativeMoney(form.unitPrice) ?: 0.0
                                                )
                                            ),
                                            discount = Validation.parseNonNegativeMoney(form.discount) ?: 0.0,
                                            extraFees = Validation.parseNonNegativeMoney(form.extraFees) ?: 0.0,
                                            notes = form.notes.ifBlank { null },
                                            termsText = form.terms.ifBlank { null }
                                        )
                                        val result = app.tijario.config.Supabase.apiClient.createDocument(req)
                                        if (result.ok) {
                                            onBack()
                                        } else {
                                            submitError = result.displayMessage
                                        }
                                    } catch (e: Exception) {
                                        submitError = "تعذر حفظ المستند الآن. تحقق من الاتصال وحاول مرة أخرى."
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
