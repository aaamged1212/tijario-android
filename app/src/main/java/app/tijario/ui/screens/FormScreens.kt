package app.tijario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import java.io.ByteArrayOutputStream
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.ui.components.buildLogoUploadRequest
import app.tijario.ui.components.clearBusinessLogoCache
import app.tijario.ui.components.loadStoreLogoBitmap
import app.tijario.domain.DocumentNumbering
import app.tijario.domain.DocumentCalculator
import app.tijario.domain.Validation
import app.tijario.data.model.DocumentType
import app.tijario.data.model.BusinessSettings
import app.tijario.data.remote.localizedDisplayMessage
import app.tijario.data.model.Product
import app.tijario.domain.LocalizedErrorMapper
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.model.DocumentPartyInfo
import app.tijario.features.documents.model.DocumentRenderItem
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentRenderStatus
import app.tijario.features.documents.model.DocumentTotals
import app.tijario.features.documents.preview.DocumentPreviewWebView
import app.tijario.features.documents.ui.DocumentTemplatePicker
import app.tijario.features.documents.ui.isTemplateAvailableForSelection
import app.tijario.features.documents.ui.DocumentTemplatePreferences
import app.tijario.features.documents.ui.DocumentInvoiceOptionPreferences
import app.tijario.ui.components.ModernDocumentPreview
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.CustomerFormState
import app.tijario.ui.state.DocumentFormState
import app.tijario.ui.state.DocumentItemState
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

internal fun isDocumentIdentityEditable(isEditMode: Boolean): Boolean = !isEditMode

internal fun documentIdentityLockedHint(language: AppLanguage): String =
    Localization.getString("locked_after_save", language)

internal fun documentIdentityLockedDescription(language: AppLanguage): String =
    Localization.getString("document_identity_locked", language)

internal fun shouldLoadEditDocument(loadedDocumentId: String?, editDocumentId: String?): Boolean =
    editDocumentId != null && loadedDocumentId != editDocumentId

internal fun mergeSelectedProductIntoItems(
    items: List<DocumentItemState>,
    product: Product,
    rowIndex: Int?,
): Pair<List<DocumentItemState>, Int> {
    val normalizedPrice = Validation.normalizedMoneyString(product.price.toString())
    val requestedIndex = rowIndex ?: items.size
    return if (requestedIndex in items.indices) {
        val updated = items.mapIndexed { index, item ->
            if (index == requestedIndex) {
                item.copy(
                    productId = product.id,
                    name = product.name,
                    unitPrice = normalizedPrice
                )
            } else {
                item
            }
        }
        updated to requestedIndex
    } else {
        val appended = items + DocumentItemState(
            productId = product.id,
            name = product.name,
            unitPrice = normalizedPrice,
            quantity = "1"
        )
        appended to appended.lastIndex
    }
}

internal fun defaultDocumentTitle(
    type: DocumentType,
    documentLanguage: String,
): String {
    val normalizedLanguage = documentLanguage.uppercase()
    return when (type) {
        DocumentType.Invoice -> if (normalizedLanguage == "EN") "Invoice" else "فاتورة"
        DocumentType.Quote -> if (normalizedLanguage == "EN") "Quote" else "عرض سعر"
    }
}

internal fun isDefaultDocumentTitle(
    type: DocumentType,
    title: String,
): Boolean {
    val normalizedTitle = title.trim()
    return normalizedTitle == defaultDocumentTitle(type, "AR") ||
        normalizedTitle == defaultDocumentTitle(type, "EN")
}

internal fun documentTitleAfterLanguageChange(
    type: DocumentType,
    currentTitle: String,
    nextDocumentLanguage: String,
    titleEditedByUser: Boolean,
): String =
    if (!titleEditedByUser && (currentTitle.isBlank() || isDefaultDocumentTitle(type, currentTitle))) {
        defaultDocumentTitle(type, nextDocumentLanguage)
    } else {
        currentTitle
    }

internal fun buildDocumentCustomerInput(form: DocumentFormState): app.tijario.data.remote.DocumentCustomerInput =
    app.tijario.data.remote.DocumentCustomerInput(
        name = form.customerName,
        whatsappNumber = form.customerWhatsapp,
        city = form.customerCity,
        id = form.customerId,
    )

@Composable
private fun DocumentFormBottomActions(
    isPreview: Boolean,
    isLoading: Boolean,
    onTogglePreview: () -> Unit,
    onSave: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.padding(10.dp)) {
        val compact = maxWidth < 340.dp
        val secondaryButton: @Composable (Modifier) -> Unit = { modifier ->
            OutlinedButton(
                onClick = onTogglePreview,
                modifier = modifier.height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(if (isPreview) t("edit") else t("btn_preview"), fontWeight = FontWeight.Bold)
            }
        }
        val saveButton: @Composable (Modifier) -> Unit = { modifier ->
            Button(
                onClick = onSave,
                enabled = !isLoading,
                modifier = modifier.height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(t("btn_save"), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                secondaryButton(Modifier.fillMaxWidth())
                saveButton(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                secondaryButton(Modifier.weight(1f))
                saveButton(Modifier.weight(1.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    dataViewModel: TijarioDataViewModel,
    customerId: String? = null,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    var form by remember(language) { mutableStateOf(CustomerFormState(lang = language)) }
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
                    notes = existing.notes.orEmpty(),
                    lang = language
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) t("edit_customer") else t("btn_add_customer"), fontWeight = FontWeight.Bold) },
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
                                        errorMessage = LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language)
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

private fun countryOptions(language: AppLanguage): List<String> = if (language == AppLanguage.AR) {
    listOf(
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
} else {
    listOf(
        "Saudi Arabia",
        "Yemen",
        "United Arab Emirates",
        "Egypt",
        "Kuwait",
        "Qatar",
        "Oman",
        "Bahrain",
        "Jordan",
        "Lebanon",
        "Morocco",
        "Tunisia",
        "Algeria",
        "Libya",
        "Sudan",
        "Iraq",
        "Syria",
        "Palestine",
    )
}

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
    var form by remember(language) { mutableStateOf(app.tijario.ui.state.ProductFormState(lang = language)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isEditMode = productId != null

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var imageDeleted by remember { mutableStateOf(false) }

    val imageFile = remember(productId, imageDeleted) {
        if (productId != null && !imageDeleted) File(context.filesDir, "product_images/$productId.jpg") else null
    }

    val bitmap = remember(selectedImageUri, imageFile) {
        if (selectedImageUri != null) {
            try {
                context.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        } else if (imageFile != null && imageFile.exists()) {
            BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap()
        } else {
            null
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imageDeleted = false
        }
    }

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
                    currency = existing.currency,
                    category = existing.category.orEmpty(),
                    lang = language
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) t("edit_product") else t("btn_add_product"), fontWeight = FontWeight.Bold) },
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

                    TijarioTextField(
                        label = t("category_optional"),
                        value = form.category,
                        onValueChange = { form = form.copy(category = it) },
                        leadingIcon = { Icon(Icons.Filled.GridView, contentDescription = null, tint = Color(0xFF64748B)) }
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

                    Text(
                        text = if (language == AppLanguage.AR) "صورة المنتج (اختياري)" else "Product Image (Optional)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (bitmap != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            OutlinedButton(
                                onClick = {
                                    selectedImageUri = null
                                    imageDeleted = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.AR) "حذف الصورة" else "Remove Image")
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { pickerLauncher.launch("image/*") }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = if (language == AppLanguage.AR) "اضغط لاختيار صورة للمنتج" else "Tap to choose product image",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
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
                                        currency = form.currency,
                                        category = form.category.ifBlank { null }
                                    )
                                    val result = if (isEditMode) {
                                        dataViewModel.updateProduct(product)
                                    } else {
                                        dataViewModel.createProduct(product)
                                    }
                                    if (result.isSuccess) {
                                        if (selectedImageUri != null) {
                                            val dir = File(context.filesDir, "product_images")
                                            if (!dir.exists()) dir.mkdirs()
                                            val destFile = File(dir, "${product.id}.jpg")
                                            context.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                                                destFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                        } else if (imageDeleted) {
                                            val destFile = File(context.filesDir, "product_images/${product.id}.jpg")
                                            if (destFile.exists()) {
                                                destFile.delete()
                                            }
                                        }
                                        onBack()
                                    } else {
                                        errorMessage = LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language)
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
    var form by remember(language) { mutableStateOf(BusinessSettingsFormState(lang = language)) }
    var existingSettings by remember { mutableStateOf<app.tijario.data.model.BusinessSettings?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isLogoUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }

    // Caching business logo
    val logoUrl = uiState.businessSettings?.logoUrl
    val logoBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, selectedLogoUri, logoUrl) {
        value = loadStoreLogoBitmap(context, selectedLogoUri, logoUrl)
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedLogoUri = uri
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
                        clearBusinessLogoCache(context)
                        dataViewModel.saveBusinessSettings(settings.copy(logoUrl = uploadedUrl))
                        dataViewModel.refreshAll()
                    } else {
                        errorMessage = result.localizedDisplayMessage(language)
                    }
                } catch (e: Exception) {
                    errorMessage = LocalizedErrorMapper.map(null, e.message, language)
                } finally {
                    isLogoUploading = false
                }
            }
        }
    }
    LaunchedEffect(isLogoUploading, uiState.businessSettings?.logoUrl) {
        if (!isLogoUploading && !uiState.businessSettings?.logoUrl.isNullOrBlank()) {
            selectedLogoUri = null
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
                address = settings.address ?: "",
                email = settings.email ?: "",
                websiteUrl = settings.websiteUrl ?: "",
                currency = settings.currency,
                terms = settings.termsText ?: "",
                lang = language
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
                                options = countryOptions(language),
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
                        "address" -> {
                            TijarioTextField(
                                label = t("business_address"),
                                value = form.address,
                                onValueChange = { form = form.copy(address = it) },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                        "email" -> {
                            TijarioTextField(
                                label = t("business_email"),
                                value = form.email,
                                onValueChange = { form = form.copy(email = it) },
                                error = form.emailError,
                                leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null, tint = Color(0xFF0D9488)) }
                            )
                        }
                        "website" -> {
                            TijarioTextField(
                                label = t("business_website"),
                                value = form.websiteUrl,
                                onValueChange = { form = form.copy(websiteUrl = it) },
                                error = form.websiteError,
                                leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null, tint = Color(0xFF0D9488)) }
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
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable(enabled = !isLogoUploading) { logoPicker.launch("image/*") }
                        ) {
                            Surface(
                                color = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.fillMaxSize()
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
                            // Edit Badge overlay at bottom right
                            Surface(
                                color = Color(0xFF0D9488),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
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
                                    text = form.businessName.ifBlank { t("app_name") },
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
                            Text(
                                text = if (language == AppLanguage.AR) "ارفع شعار متجرك أو نشاطك" else "Upload store or business logo",
                                color = Color(0xFF2DD4BF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable(enabled = !isLogoUploading) { logoPicker.launch("image/*") }
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
                        value = form.businessName.ifBlank { t("app_name") },
                        onClick = { activeDialog = "name" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))



                    // Row 3: رقم التواصل
                    SettingsItemRow(
                        icon = Icons.Filled.Phone,
                        title = t("contact_phone"),
                        value = form.whatsapp,
                        onClick = { activeDialog = "phone" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 4: الدولة
                    SettingsItemRow(
                        icon = Icons.Filled.Public,
                        title = t("country"),
                        value = form.country.ifBlank { if (language == AppLanguage.AR) "اليمن" else "Yemen" },
                        onClick = { activeDialog = "country" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 5: المدينة
                    SettingsItemRow(
                        icon = Icons.Filled.Domain,
                        title = t("city"),
                        value = form.city.ifBlank { if (language == AppLanguage.AR) "صنعاء" else "Sana'a" },
                        onClick = { activeDialog = "city" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    SettingsItemRow(
                        icon = Icons.Filled.LocationOn,
                        title = t("business_address"),
                        value = form.address,
                        onClick = { activeDialog = "address" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    SettingsItemRow(
                        icon = Icons.Filled.Description,
                        title = t("business_email"),
                        value = form.email,
                        onClick = { activeDialog = "email" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    SettingsItemRow(
                        icon = Icons.Filled.Public,
                        title = t("business_website"),
                        value = form.websiteUrl,
                        onClick = { activeDialog = "website" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Row 6: العملة
                    val displayCurrency = when (form.currency.uppercase()) {
                        "YER" -> if (language == AppLanguage.AR) "ريال يمني - YER" else "Yemeni Rial - YER"
                        "SAR" -> if (language == AppLanguage.AR) "الريال السعودي - SAR" else "Saudi Riyal - SAR"
                        "USD" -> if (language == AppLanguage.AR) "الدولار الأمريكي - USD" else "US Dollar - USD"
                        else -> form.currency
                    }
                    SettingsItemRow(
                        icon = Icons.Filled.AttachMoney,
                        title = t("currency"),
                        value = displayCurrency,
                        onClick = { activeDialog = "currency" }
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
                                address = form.address.ifBlank { null },
                                email = form.email.ifBlank { null },
                                websiteUrl = form.websiteUrl.ifBlank { null },
                                currency = form.currency,
                                termsText = form.terms.ifBlank { null },
                            ) ?: app.tijario.data.model.BusinessSettings(
                                userId = uiState.userId,
                                businessName = form.businessName,
                                whatsappNumber = form.whatsapp,
                                country = form.country,
                                city = form.city.ifBlank { null },
                                address = form.address.ifBlank { null },
                                email = form.email.ifBlank { null },
                                websiteUrl = form.websiteUrl.ifBlank { null },
                                currency = form.currency,
                                termsText = form.terms.ifBlank { null },
                            )
                            val result = dataViewModel.saveBusinessSettings(settings)
                            if (result.isSuccess) {
                                onBack()
                            } else {
                                errorMessage = LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language)
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
            state.documentTitle,
            state.discountLabel,
            state.extraFeesLabel,
            state.finalTaxRate,
            state.finalTaxName,
            state.documentLanguage,
            state.currency,
            state.signatureData,
            state.paymentMethod,
            state.lang.name
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
        val hasLabels = list.size >= 26
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
        val discountLabel = if (hasLabels) list[17] as String else ""
        val extraFeesLabel = if (hasLabels) list[18] as String else ""
        val finalTaxRate = if (hasLabels) list[19] as String else list[17] as String
        val finalTaxName = if (hasLabels) list[20] as String else list[18] as String
        val documentLanguage = if (hasLabels) list[21] as String else list[19] as String
        val currency = if (hasLabels) list[22] as String else list[20] as String
        val signatureData = if (hasLabels) list[23] as String else list[21] as String
        val paymentMethod = if (hasLabels) list[24] as String else list[22] as String
        val lang = app.tijario.config.AppLanguage.valueOf(if (hasLabels) list[25] as String else list[23] as String)

        val itemsList = mutableListOf<app.tijario.ui.state.DocumentItemState>()
        val itemsData = list.subList(if (hasLabels) 26 else 24, list.size)
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
                        lang = lang
                    )
                )
            }
        }
        
        DocumentFormState(
            customerId = customerId.takeIf { it.isNotEmpty() },
            customerName = customerName,
            customerWhatsapp = customerWhatsapp,
            customerCity = customerCity.takeIf { it.isNotEmpty() },
            items = itemsList,
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
            documentTitle = documentTitle,
            discountLabel = discountLabel,
            extraFeesLabel = extraFeesLabel,
            finalTaxRate = finalTaxRate,
            finalTaxName = finalTaxName,
            documentLanguage = documentLanguage,
            currency = currency,
            signatureData = signatureData,
            paymentMethod = paymentMethod,
            lang = lang
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceInfoDialog(
    form: app.tijario.ui.state.DocumentFormState,
    isEditMode: Boolean,
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
    val identityLocked = !isDocumentIdentityEditable(isEditMode)

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
                                onValueChange = { if (!identityLocked) invoiceNumber = it },
                                readOnly = identityLocked
                            )

                            TijarioTextField(
                                label = t("creation_date") + " *",
                                value = creationDate,
                                onValueChange = { if (!identityLocked) creationDate = it },
                                readOnly = identityLocked
                            )

                            var showTermsDropdown by remember { mutableStateOf(false) }

                            TijarioTextField(
                                label = t("due_date"),
                                value = dueDate,
                                onValueChange = { dueDate = it }
                            )

                            TijarioTextField(
                                label = t("invoice_title_name"),
                                value = invoiceTitle,
                                onValueChange = { if (!identityLocked) invoiceTitle = it },
                                readOnly = identityLocked
                            )

                            if (identityLocked) {
                                Text(
                                    text = documentIdentityLockedDescription(form.lang),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
fun SelectTemplateDialog(
    selectedTemplateId: String,
    allowedTemplateIds: List<String> = emptyList(),
    isEntitlementLoaded: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val templates = remember { DocumentTemplateRegistry.templates }
    var selectedId by remember { mutableStateOf(selectedTemplateId) }
    val context = LocalContext.current
    val language = LocalLanguage.current
    fun isTemplateAllowed(templateId: String): Boolean =
        isTemplateAvailableForSelection(isEntitlementLoaded, allowedTemplateIds, templateId)
    val sampleModel = remember {
        DocumentRenderModel(
            documentType = DocumentType.Invoice,
            documentNumber = "INV-0007",
            issueDate = "2026-06-21",
            status = DocumentRenderStatus(paymentStatus = "partial"),
            business = DocumentPartyInfo(
                name = "Tijario Store",
                contactNumber = "77440099",
                country = "Yemen",
                city = "Sana'a",
            ),
            customer = DocumentPartyInfo(
                name = "Ahmad Ali",
                contactNumber = "777000111",
                city = "Aden",
            ),
            items = listOf(
                DocumentRenderItem(
                    id = "1",
                    name = "Sample Item",
                    description = "Default preview content",
                    quantity = 2,
                    unitPrice = java.math.BigDecimal("150.00"),
                    lineTotal = java.math.BigDecimal("300.00"),
                ),
            ),
            totals = DocumentTotals(
                subtotal = java.math.BigDecimal("300.00"),
                discount = java.math.BigDecimal.ZERO,
                extraFees = java.math.BigDecimal.ZERO,
                total = java.math.BigDecimal("300.00"),
                amountPaid = java.math.BigDecimal("100.00"),
                amountRemaining = java.math.BigDecimal("200.00"),
                currency = "SAR",
            ),
            invoiceNote = "Sample invoice note",
            termsAndConditions = "Sample terms",
        )
    }

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(templates) { template ->
                            val templateModel = remember(template.id) { sampleModel.copy(templateId = template.id) }
                            val isSelected = template.id == selectedId
                            val isAllowed = isTemplateAllowed(template.id)
                            Card(
                                onClick = {
                                    if (isAllowed) {
                                        selectedId = template.id
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (language == AppLanguage.AR) "هذا القالب يحتاج ترقية الخطة." else "This template requires a plan upgrade.",
                                            android.widget.Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = template.name.removePrefix("Tijario "),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        DocumentPreviewWebView(
                                            model = templateModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    if (isAllowed) {
                                                        selectedId = template.id
                                                    }
                                                }
                                        ) {
                                            if (!isAllowed) {
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.45f),
                                                    modifier = Modifier.fillMaxSize(),
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = onDismiss) { Text(t("btn_cancel")) }
                        Button(onClick = {
                            if (isTemplateAllowed(selectedId)) {
                                onSave(selectedId)
                            }
                        }) { Text(t("btn_save")) }
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
    var description by remember { mutableStateOf(item.description) }

    val parsedPrice = Validation.parseNonNegativeMoney(price) ?: 0.0
    val parsedQty = Validation.parsePositiveInt(quantity) ?: 1
    val totalAmount = parsedPrice * parsedQty

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
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TijarioTextField(
                                    label = t("item_name_label") + " *",
                                    value = name,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = t("select_product"),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { onChooseProduct() }
                                )
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
                                text = String.format("%.2f", totalAmount),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val language = LocalLanguage.current
    var form by rememberSaveable(stateSaver = DocumentFormStateSaver) {
        mutableStateOf(
            DocumentFormState(
                lang = language,
                documentLanguage = if (language == AppLanguage.EN) "EN" else "AR",
            )
        )
    }
    var isLoadingDocument by rememberSaveable { mutableStateOf(documentId != null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = edit, 1 = preview
    var isLoading by remember { mutableStateOf(false) }
    var titleEditedByUser by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val templatePreferences = remember(context) { DocumentTemplatePreferences(context) }
    val invoiceOptionPreferences = remember(context) { DocumentInvoiceOptionPreferences(context) }
    var selectedTemplateId by remember { mutableStateOf(templatePreferences.getDefaultTemplateId()) }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val isTemplateEntitlementLoaded = uiState.planUsage != null
    val allowedTemplateIds = uiState.planUsage?.allowedTemplateIds.orEmpty()
    val showTijarioBranding = uiState.planUsage?.removeTijarioBranding?.not() ?: true
    fun isTemplateAllowed(templateId: String): Boolean =
        isTemplateAvailableForSelection(isTemplateEntitlementLoaded, allowedTemplateIds, templateId)
    val businessSettings = uiState.businessSettings
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val notesBringIntoViewRequester = remember { BringIntoViewRequester() }
    val editDocumentId = documentId?.takeIf { it.isNotBlank() }
    val isEditMode = editDocumentId != null

    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var pendingProductRowIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var loadedDocumentId by rememberSaveable { mutableStateOf<String?>(null) }
    var showInvoiceInfoDialog by remember { mutableStateOf(false) }
    var showTemplatePickerDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLocalTaxesDialog by remember { mutableStateOf(false) }
    var showLocalPaymentDialog by remember { mutableStateOf(false) }
    var showLocalSignaturesDialog by remember { mutableStateOf(false) }
    var showLocalTermsDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    fun showDocumentError(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
        }
    }

    fun selectDocumentLanguage(nextDocumentLanguage: String) {
        val normalizedLanguage = if (nextDocumentLanguage.equals("EN", ignoreCase = true)) "EN" else "AR"
        form = form.copy(
            documentLanguage = normalizedLanguage,
            documentTitle = documentTitleAfterLanguageChange(
                type = type,
                currentTitle = form.documentTitle,
                nextDocumentLanguage = normalizedLanguage,
                titleEditedByUser = titleEditedByUser,
            ),
        )
    }

    val nextDocNumber = remember(uiState.documents, type) {
        val typedDocs = uiState.documents.filter { it.type == type }
        DocumentNumbering.nextDocumentNumber(typedDocs.map { it.documentNumber }, type)
    }

    LaunchedEffect(nextDocNumber, isEditMode) {
        if (!isEditMode && form.documentNumber.isBlank()) {
            form = form.copy(documentNumber = nextDocNumber)
        }
    }

    LaunchedEffect(isEditMode, language, type) {
        if (!isEditMode && form.documentTitle.isBlank()) {
            form = form.copy(
                documentTitle = defaultDocumentTitle(type, form.documentLanguage),
            )
        }
    }

    LaunchedEffect(language) {
        form = form.copy(lang = language)
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

    fun submitDocument() {
        if (isLoading) return
        if (form.customerId == null) {
            showDocumentError(Localization.getString("select_customer_first", language))
            return
        }
        if (form.items.isEmpty()) {
            showDocumentError(Localization.getString("add_one_item_min", language))
            return
        }
        if (!form.items.all { it.isValid }) {
            showDocumentError(Localization.getString("enter_item_details_correctly", language))
            return
        }
        val totals = DocumentCalculator.calculate(
            form.items.map { item ->
                DocumentCalculator.ItemInput(
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                )
            },
            discountStr = form.discount,
            extraFeesStr = form.extraFees,
            taxRateStr = form.finalTaxRate,
            amountPaidStr = form.amountPaid,
        )
        if (!totals.isValid) {
            showDocumentError(Localization.getString("invalid_document_total", language))
            return
        }

        scope.launch {
            try {
                isLoading = true
                val req = app.tijario.data.remote.CreateDocumentRequest(
                    type = type,
                    paymentStatus = if (type == app.tijario.data.model.DocumentType.Invoice) form.paymentStatus else null,
                    amountPaid = if (type == app.tijario.data.model.DocumentType.Invoice && form.paymentStatus == "partial") Validation.parseNonNegativeMoney(form.amountPaid) else null,
                    customer = buildDocumentCustomerInput(form),
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
                    termsText = form.terms.ifBlank { null },
                    currency = form.currency,
                    templateId = selectedTemplateId,
                    documentTitle = form.documentTitle.ifBlank { null },
                    discountLabel = form.discountLabel.ifBlank { null },
                    extraFeesLabel = form.extraFeesLabel.ifBlank { null },
                    taxName = form.finalTaxName.ifBlank { null },
                    taxRate = Validation.parseNonNegativeMoney(form.finalTaxRate) ?: 0.0,
                    documentLanguage = form.documentLanguage.lowercase(),
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
                        app.tijario.features.documents.pdf.PdfCacheManager(context).invalidate(savedDocumentId)
                        dataViewModel.upsertDocumentMetadata(
                            app.tijario.data.local.LocalDocumentMetadataEntity(
                                documentId = savedDocumentId,
                                currency = form.currency,
                                signatureData = form.signatureData.takeIf { it.isNotEmpty() },
                                paymentMethod = form.paymentMethod.takeIf { it.isNotEmpty() },
                                taxRate = Validation.parseNonNegativeMoney(form.finalTaxRate) ?: 0.0,
                                taxName = form.finalTaxName.ifBlank { "Tax" }
                            )
                        )
                        onDocumentSaved(savedDocumentId)
                    }
                } else {
                    val message = if (
                        result.code.equals("insufficient_stock", ignoreCase = true) &&
                        result.availableStock != null
                    ) {
                        Localization.getString("insufficient_stock_available", language)
                            .format(result.availableStock)
                    } else {
                        result.localizedDisplayMessage(language)
                    }
                    showDocumentError(message)
                }
            } catch (e: Exception) {
                showDocumentError(LocalizedErrorMapper.map(null, e.message, language))
            } finally {
                isLoading = false
            }
        }
    }

    // Sync selected customer
    LaunchedEffect(selectedCustomer, isEditMode) {
        if (!isEditMode) {
            selectedCustomer?.let {
                form = form.copy(
                    customerId = it.id,
                    customerName = it.name,
                    customerWhatsapp = it.whatsappNumber,
                    customerCity = it.city
                )
            }
        }
    }

    LaunchedEffect(editDocumentId, loadedDocumentId) {
        if (shouldLoadEditDocument(loadedDocumentId, editDocumentId)) {
            val currentEditDocumentId = editDocumentId ?: return@LaunchedEffect
            isLoadingDocument = true
            try {
                val result = dataViewModel.fetchCompleteDocument(currentEditDocumentId)
                val existing = result.getOrNull()
                if (existing != null) {
                    val metadata = dataViewModel.getDocumentMetadata(currentEditDocumentId)
                    existing.templateId?.takeIf { it.isNotBlank() }?.let { savedTemplateId ->
                        selectedTemplateId = DocumentTemplateRegistry.normalizeId(savedTemplateId)
                    }
                    form = existing.toFormState(language).copy(
                        currency = metadata?.currency ?: existing.currency.ifBlank { null } ?: form.currency,
                        signatureData = metadata?.signatureData.orEmpty(),
                        paymentMethod = metadata?.paymentMethod.orEmpty(),
                        finalTaxRate = metadata?.taxRate?.toString() ?: form.finalTaxRate,
                        finalTaxName = metadata?.taxName ?: form.finalTaxName,
                        lang = language
                    )
                    titleEditedByUser = form.documentTitle.isNotBlank() &&
                        !isDefaultDocumentTitle(type, form.documentTitle)
                    loadedDocumentId = currentEditDocumentId
                } else {
                    showDocumentError(LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language))
                }
            } finally {
                isLoadingDocument = false
            }
        }
        else {
            isLoadingDocument = false
        }
    }

    LaunchedEffect(isEditMode, businessSettings) {
        if (!isEditMode) {
            val defaults = invoiceOptionPreferences.getDefaults()
            form = form.copy(
                paymentMethod = form.paymentMethod.ifBlank { defaults.paymentMethod },
                terms = form.terms.ifBlank { defaults.termsContent.ifBlank { businessSettings?.termsText.orEmpty() } },
                signatureData = form.signatureData.ifBlank { defaults.signatureData }
            )
        }
    }

    // Sync selected product to specific row
    LaunchedEffect(selectedProduct, selectedProductRowIndex) {
        selectedProduct?.let { prod ->
            val idx = selectedProductRowIndex ?: pendingProductRowIndex ?: form.items.size
            val (updatedItems, targetIndex) = mergeSelectedProductIntoItems(form.items, prod, idx)
            form = form.copy(items = updatedItems)
            editingItemIndex = targetIndex
            pendingProductRowIndex = null
            onSelectedProductConsumed()
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
                            if (type == app.tijario.data.model.DocumentType.Invoice) t("edit_invoice_title") else t("edit_quote_title")
                        } else if (type == app.tijario.data.model.DocumentType.Invoice) t("btn_new_invoice") else t("btn_new_quote"),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTab == 1) {
                            selectedTab = 0
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
            )
        },
        bottomBar = {
            if (!isLoadingDocument) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                    ) {
                        DocumentFormBottomActions(
                            isPreview = selectedTab == 1,
                            isLoading = isLoading,
                            onTogglePreview = { selectedTab = if (selectedTab == 0) 1 else 0 },
                            onSave = { submitDocument() },
                        )
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
                    .background(MaterialTheme.colorScheme.background)
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
                                    DocumentNumbering.firstDocumentNumber(type)
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
                            if (isEditMode) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = documentIdentityLockedHint(language),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
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
                                .clickable { showLanguageSheet = true }
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
                                val currentLangStr = if (form.documentLanguage == "AR") t("language_arabic") else t("language_english")
                                Text(currentLangStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
                                val location = listOfNotNull(
                                    businessSettings?.country?.takeIf { it.isNotBlank() },
                                    businessSettings?.city?.takeIf { it.isNotBlank() }
                                ).joinToString(" - ")
                                val storeContact = businessSettings?.whatsappNumber?.takeIf { it.isNotBlank() }
                                Text(storeName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (location.isNotBlank()) {
                                    Text(location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                storeContact?.let {
                                    Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isDocumentIdentityEditable(isEditMode)) {
                                    onNavigateToSelectCustomer()
                                }
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
                                if (isEditMode) {
                                    Text(
                                        text = documentIdentityLockedHint(language),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
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
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
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
                                            text = item.name.ifBlank { t("item_name_label") },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${item.quantity.ifBlank { "0" }} x ${item.unitPrice.ifBlank { "0.00" }}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Total amount
                                    val parsedPrice = Validation.parseNonNegativeMoney(item.unitPrice) ?: 0.0
                                    val parsedQty = Validation.parsePositiveInt(item.quantity) ?: 1
                                    val sub = parsedPrice * parsedQty
                                    val itemTotal = sub

                                    Text(
                                        text = String.format("%.2f", itemTotal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
                        OutlinedButton(
                            onClick = {
                                pendingProductRowIndex = form.items.size
                                onNavigateToSelectProduct(form.items.size)
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
                            Text(t("add_item"), fontWeight = FontWeight.Bold)
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
                            Text(text = String.format("%.2f %s", subtotalVal, form.currency), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }

                // Discount, tax, and final total summary card
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
                                leadingIcon = { Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color(0xFF64748B)) },
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TijarioTextField(
                                label = Localization.getString("doc_discount_reason", language),
                                value = form.discountLabel,
                                onValueChange = { form = form.copy(discountLabel = it) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color(0xFF64748B)) }
                            )
                            TijarioTextField(
                                label = Localization.getString("doc_extra_fee_reason", language),
                                value = form.extraFeesLabel,
                                onValueChange = { form = form.copy(extraFeesLabel = it) },
                                modifier = Modifier.weight(1.2f),
                                leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) }
                            )
                        }
                        
                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Tax Selection Row (linked to LocalTaxesManagerDialog)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLocalTaxesDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                 Text(t("tax"), fontSize = 14.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val parsedTax = Validation.parseNonNegativeMoney(form.finalTaxRate) ?: 0.0
                                val taxText = if (parsedTax > 0.0) "${form.finalTaxName} ($parsedTax%)" else t("no_tax")
                                Text(taxText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Subtotal and Final Total Calculations
                        val calculations = DocumentCalculator.calculate(
                            form.items.map { item ->
                                DocumentCalculator.ItemInput(
                                    quantity = item.quantity,
                                    unitPrice = item.unitPrice,
                                )
                            },
                            discountStr = form.discount,
                            extraFeesStr = form.extraFees,
                            taxRateStr = form.finalTaxRate,
                            amountPaidStr = form.amountPaid,
                        )
                        val subtotalVal = calculations.subtotal.toDouble()
                        val taxAmount = calculations.taxAmount.toDouble()
                        val finalTotalVal = calculations.total.toDouble()
                        val hasInvalidTotals = !calculations.isValid

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = t("subtotal"),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = String.format("%.2f %s", subtotalVal, form.currency), fontSize = 14.sp)
                            }
                            if (taxAmount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${form.finalTaxName} (${form.finalTaxRate}%)",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(text = String.format("%.2f %s", taxAmount, form.currency), fontSize = 14.sp)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = t("final_total"),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format("%.2f %s", finalTotalVal, form.currency),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF0D9488) // Accent Green
                                )
                            }
                            if (hasInvalidTotals) {
                                Text(
                                    text = Localization.getString("invalid_document_total", language),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Payment Status and Notes Card
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
                        if (type == app.tijario.data.model.DocumentType.Invoice) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = t("payment_status"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    val options = listOf(
                                        "unpaid" to t("filter_unpaid"),
                                        "paid" to t("filter_paid"),
                                        "partial" to t("filter_partial"),
                                    )
                                    options.forEachIndexed { index, option ->
                                        val isSelected = form.paymentStatus == option.first
                                        SegmentedButton(
                                            selected = isSelected,
                                            onClick = { form = form.copy(paymentStatus = option.first) },
                                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                            colors = SegmentedButtonDefaults.colors(
                                                activeContainerColor = Color(0xFF0D9488),
                                                activeContentColor = Color.White,
                                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                                inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            label = {
                                                Text(
                                                    option.second,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                        )
                                    }
                                }
                            }

                            if (form.paymentStatus == "partial") {
                                TijarioTextField(
                                    label = t("amount_paid"),
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
                            modifier = Modifier
                                .bringIntoViewRequester(notesBringIntoViewRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        scope.launch {
                                            delay(150)
                                            notesBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                },
                            singleLine = false,
                            leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Color(0xFF64748B)) }
                        )

                    }
                }

                // Additional settings card (Currency, Payment Method, Signature, Terms)
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
                            text = t("additional_invoice_options"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Currency Selector Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isDocumentIdentityEditable(isEditMode)) {
                                    showCurrencyDialog = true
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(t("currency"), fontSize = 14.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(form.currency, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (isEditMode) {
                            Text(
                                text = documentIdentityLockedHint(language),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Payment Method Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLocalPaymentDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.PriceChange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(t("payment_method"), fontSize = 14.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val pmText = form.paymentMethod.ifBlank { t("not_specified") }
                                Text(pmText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Terms and Conditions Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLocalTermsDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(t("terms_cond"), fontSize = 14.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val defaults = invoiceOptionPreferences.getDefaults()
                                val termsText = when {
                                    form.terms.isBlank() -> t("not_specified")
                                    defaults.termsContent.isNotBlank() && form.terms == defaults.termsContent -> defaults.termsTitle.ifBlank { t("terms_cond") }
                                    else -> t("terms_cond")
                                }
                                Text(termsText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Signature Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLocalSignaturesDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(t("invoice_signature"), fontSize = 14.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val defaults = invoiceOptionPreferences.getDefaults()
                                val sigText = when {
                                    form.signatureData.isBlank() -> t("not_signed")
                                    defaults.signatureData.isNotBlank() && form.signatureData == defaults.signatureData -> defaults.signatureName.ifBlank { t("invoice_signature") }
                                    else -> t("invoice_signature")
                                }
                                Text(sigText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        } else if (!isTemplateEntitlementLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val templates = remember { DocumentTemplateRegistry.templates }
                val pagerState = rememberPagerState(
                    initialPage = templates.indexOfFirst { it.id == selectedTemplateId }.coerceAtLeast(0),
                    pageCount = { templates.size }
                )

                // Sync pager state changes back to selectedTemplateId
                LaunchedEffect(pagerState.currentPage) {
                    val templateId = templates[pagerState.currentPage].id
                    if (templateId != selectedTemplateId && isTemplateAllowed(templateId)) {
                        selectedTemplateId = templateId
                        templatePreferences.setDefaultTemplateId(templateId)
                    }
                }

                LaunchedEffect(allowedTemplateIds) {
                    if (!isTemplateAllowed(selectedTemplateId)) {
                        val fallback = templates.firstOrNull { isTemplateAllowed(it.id) }?.id
                            ?: DocumentTemplateRegistry.defaultTemplateId
                        selectedTemplateId = fallback
                        templatePreferences.setDefaultTemplateId(fallback)
                    }
                }

                // Sync selectedTemplateId changes (from select dialog/grid) back to pager state
                LaunchedEffect(selectedTemplateId) {
                    val targetPage = templates.indexOfFirst { it.id == selectedTemplateId }.coerceAtLeast(0)
                    if (pagerState.currentPage != targetPage) {
                        pagerState.animateScrollToPage(targetPage)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeTemplateName = templates.find { it.id == selectedTemplateId }?.name?.removePrefix("Tijario ") ?: ""
                    Text(
                        text = t("template_label").replace("%s", activeTemplateName),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { showTemplatePickerDialog = true }
                    ) {
                        Text(t("change_template"), fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = t("swipe_change_template"),
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 16.dp
                    ) { page ->
                        val template = templates[page]
                        ModernDocumentPreview(
                            documentType = type,
                            form = form,
                            businessSettings = businessSettings,
                            customerCity = form.customerCity,
                            templateId = template.id,
                            showTijarioBranding = showTijarioBranding,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Launch Edit Item Dialog when requested
    editingItemIndex?.let { index ->
        if (index in form.items.indices) {
            key(form.items[index].id) {
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
                        pendingProductRowIndex = index
                        onNavigateToSelectProduct(index)
                    }
                )
            }
        }
    }

    if (showInvoiceInfoDialog) {
        InvoiceInfoDialog(
            form = form,
            isEditMode = isEditMode,
            onDismiss = { showInvoiceInfoDialog = false },
            onSave = { docNum, date, terms, due, po, title ->
                titleEditedByUser = title.isNotBlank() && !isDefaultDocumentTitle(type, title)
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

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = t("invoice_language"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                listOf(
                    "AR" to t("language_arabic"),
                    "EN" to t("language_english"),
                ).forEach { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectDocumentLanguage(code)
                                showLanguageSheet = false
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        RadioButton(
                            selected = form.documentLanguage == code,
                            onClick = {
                                selectDocumentLanguage(code)
                                showLanguageSheet = false
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showTemplatePickerDialog) {
        SelectTemplateDialog(
            selectedTemplateId = selectedTemplateId,
            allowedTemplateIds = allowedTemplateIds,
            isEntitlementLoaded = isTemplateEntitlementLoaded,
            onDismiss = { showTemplatePickerDialog = false },
            onSave = { templateId ->
                selectedTemplateId = DocumentTemplateRegistry.requireTemplate(templateId).id
                templatePreferences.setDefaultTemplateId(selectedTemplateId)
                showTemplatePickerDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCurrency = form.currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                form = form.copy(currency = it)
                showCurrencyDialog = false
            }
        )
    }

    if (showLocalTaxesDialog) {
        LocalTaxesManagerDialog(
            dataViewModel = dataViewModel,
            onDismiss = { showLocalTaxesDialog = false },
            onApplyTax = { name, rate ->
                form = form.copy(finalTaxName = name, finalTaxRate = rate)
                showLocalTaxesDialog = false
            }
        )
    }

    if (showLocalPaymentDialog) {
        LocalPaymentMethodsManagerDialog(
            dataViewModel = dataViewModel,
            onDismiss = { showLocalPaymentDialog = false },
            onSelect = { pm ->
                form = form.copy(paymentMethod = pm)
                invoiceOptionPreferences.setPaymentMethod(pm)
                showLocalPaymentDialog = false
            }
        )
    }

    if (showLocalSignaturesDialog) {
        LocalSignaturesManagerDialog(
            dataViewModel = dataViewModel,
            onDismiss = { showLocalSignaturesDialog = false },
            onSelect = { name, base64 ->
                form = form.copy(signatureData = base64)
                invoiceOptionPreferences.setSignature(name, base64)
                showLocalSignaturesDialog = false
            }
        )
    }

    if (showLocalTermsDialog) {
        LocalTermsManagerDialog(
            dataViewModel = dataViewModel,
            onDismiss = { showLocalTermsDialog = false },
            onSelect = { title, content ->
                form = form.copy(terms = content)
                invoiceOptionPreferences.setTerms(title, content)
                showLocalTermsDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf("SAR", "AED", "USD", "QAR", "KWD", "BHD", "OMR", "YER", "EGP")
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isArabic) "اختر العملة" else "Select Currency",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currencies) { curr ->
                        val isSelected = curr == currentCurrency
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelect(curr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = curr,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTaxesManagerDialog(
    dataViewModel: TijarioDataViewModel,
    onDismiss: () -> Unit,
    onApplyTax: (String, String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val taxes by dataViewModel.observeLocalTaxes().collectAsState(initial = emptyList())
    var newTaxName by remember { mutableStateOf("") }
    var newTaxRate by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isArabic) "الضرائب المخزنة محلياً" else "Local Stored Taxes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaxName,
                        onValueChange = { newTaxName = it },
                        label = { Text(if (isArabic) "اسم الضريبة" else "Tax Name") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newTaxRate,
                        onValueChange = { newTaxRate = it },
                        label = { Text(if (isArabic) "النسبة %" else "Rate %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Button(
                    onClick = {
                        val rate = newTaxRate.toDoubleOrNull()
                        if (newTaxName.isNotBlank() && rate != null) {
                            scope.launch {
                                dataViewModel.upsertLocalTax(
                                    app.tijario.data.local.LocalTaxEntity(
                                        id = java.util.UUID.randomUUID().toString(),
                                        name = newTaxName,
                                        rate = rate
                                    )
                                )
                                newTaxName = ""
                                newTaxRate = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "حفظ كجديد" else "Save New")
                }
                HorizontalDivider()
                Column(
                    modifier = Modifier.height(180.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (taxes.isEmpty()) {
                        Text(
                            text = if (isArabic) "لا توجد ضرائب مخزنة" else "No taxes stored",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    taxes.forEach { tax ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onApplyTax(tax.name, tax.rate.toString()) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tax.name, fontWeight = FontWeight.Bold)
                                Text("${tax.rate}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                scope.launch { dataViewModel.deleteLocalTax(tax.id) }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPaymentMethodsManagerDialog(
    dataViewModel: TijarioDataViewModel,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val methods by dataViewModel.observeLocalPaymentMethods().collectAsState(initial = emptyList())
    var newMethodName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isArabic) "طرق الدفع المخزنة" else "Stored Payment Methods",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMethodName,
                        onValueChange = { newMethodName = it },
                        label = { Text(if (isArabic) "طريقة دفع جديدة" else "New Method Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newMethodName.isNotBlank()) {
                                scope.launch {
                                    dataViewModel.upsertLocalPaymentMethod(
                                        app.tijario.data.local.LocalPaymentMethodEntity(
                                            id = java.util.UUID.randomUUID().toString(),
                                            name = newMethodName
                                        )
                                    )
                                    newMethodName = ""
                                }
                            }
                        }
                    ) {
                        Text(if (isArabic) "حفظ" else "Save")
                    }
                }
                HorizontalDivider()
                Column(
                    modifier = Modifier.height(180.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val defaultMethods = if (isArabic) listOf("نقدي", "مدى", "تحويل بنكي") else listOf("Cash", "Mada", "Bank Transfer")
                    val activeList = if (methods.isEmpty()) {
                        defaultMethods.map { app.tijario.data.local.LocalPaymentMethodEntity(it, it) }
                    } else methods

                    activeList.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelect(method.name) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method.name, fontWeight = FontWeight.Bold)
                            if (methods.isNotEmpty()) {
                                IconButton(onClick = {
                                    scope.launch { dataViewModel.deleteLocalPaymentMethod(method.id) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTermsManagerDialog(
    dataViewModel: TijarioDataViewModel,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val termsList by dataViewModel.observeLocalTerms().collectAsState(initial = emptyList())
    var termTitle by remember { mutableStateOf("") }
    var termContent by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isArabic) "الشروط والأحكام المخزنة" else "Stored Terms & Conditions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                OutlinedTextField(
                    value = termTitle,
                    onValueChange = { termTitle = it },
                    label = { Text(if (isArabic) "العنوان" else "Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = termContent,
                    onValueChange = { termContent = it },
                    label = { Text(if (isArabic) "شروط العقد" else "Terms Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Button(
                    onClick = {
                        if (termTitle.isNotBlank() && termContent.isNotBlank()) {
                            scope.launch {
                                dataViewModel.upsertLocalTerms(
                                    app.tijario.data.local.LocalTermsEntity(
                                        id = java.util.UUID.randomUUID().toString(),
                                        title = termTitle,
                                        content = termContent
                                    )
                                )
                                termTitle = ""
                                termContent = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "إضافة شرط جديد" else "Save New Terms")
                }
                HorizontalDivider()
                Column(
                    modifier = Modifier.height(160.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (termsList.isEmpty()) {
                        Text(
                            text = if (isArabic) "لا توجد بنود شروط مخزنة" else "No terms templates stored",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    termsList.forEach { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelect(term.title, term.content) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(term.title, fontWeight = FontWeight.Bold)
                                Text(term.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            IconButton(onClick = {
                                scope.launch { dataViewModel.deleteLocalTerms(term.id) }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalSignaturesManagerDialog(
    dataViewModel: TijarioDataViewModel,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val signatures by dataViewModel.observeLocalSignatures().collectAsState(initial = emptyList())
    var sigName by remember { mutableStateOf("") }
    var drawMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isArabic) "التوقيعات" else "Signatures",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (drawMode) {
                    OutlinedTextField(
                        value = sigName,
                        onValueChange = { sigName = it },
                        label = { Text(if (isArabic) "اسم صاحب التوقيع" else "Signature Owner Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Text(
                        text = if (isArabic) "ارسم توقيعك هنا" else "Draw your signature here",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val lines = remember { mutableStateListOf<List<Offset>>() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        lines.add(listOf(offset))
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val line = lines.lastOrNull() ?: emptyList()
                                        if (lines.isNotEmpty()) {
                                            lines[lines.size - 1] = line + change.position
                                        }
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            lines.forEach { path ->
                                if (path.size > 1) {
                                    for (i in 0 until path.size - 1) {
                                        drawLine(
                                            color = Color.Black,
                                            start = path[i],
                                            end = path[i + 1],
                                            strokeWidth = 6f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (sigName.isNotBlank() && lines.isNotEmpty()) {
                                    val bitmap = Bitmap.createBitmap(800, 520, Bitmap.Config.ARGB_8888)
                                    val canvas = AndroidCanvas(bitmap)
                                    canvas.drawColor(android.graphics.Color.WHITE)
                                    val paint = AndroidPaint().apply {
                                        color = android.graphics.Color.BLACK
                                        strokeWidth = 6f
                                        style = AndroidPaint.Style.STROKE
                                        strokeJoin = AndroidPaint.Join.ROUND
                                        strokeCap = AndroidPaint.Cap.ROUND
                                    }
                                    lines.forEach { path ->
                                        if (path.size > 1) {
                                            val p = AndroidPath()
                                            p.moveTo(path[0].x, path[0].y)
                                            for (i in 1 until path.size) {
                                                p.lineTo(path[i].x, path[i].y)
                                            }
                                            canvas.drawPath(p, paint)
                                        }
                                    }
                                    val stream = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                                    scope.launch {
                                        dataViewModel.upsertLocalSignature(
                                            app.tijario.data.local.LocalSignatureEntity(
                                                id = java.util.UUID.randomUUID().toString(),
                                                name = sigName,
                                                signatureData = base64
                                            )
                                        )
                                        drawMode = false
                                        sigName = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isArabic) "حفظ التوقيع" else "Save Signature")
                        }
                        OutlinedButton(
                            onClick = { drawMode = false },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(if (isArabic) "إلغاء" else "Cancel")
                        }
                    }
                } else {
                    Button(
                        onClick = { drawMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isArabic) "إضافة توقيع جديد ورسمه" else "Draw New Signature")
                    }
                    HorizontalDivider()
                    Column(
                        modifier = Modifier.height(160.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (signatures.isEmpty()) {
                            Text(
                                text = if (isArabic) "لا توجد توقيعات مخزنة" else "No signatures stored",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        signatures.forEach { sig ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelect(sig.name, sig.signatureData) }
                                .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sig.name, fontWeight = FontWeight.Bold)
                                IconButton(onClick = {
                                    scope.launch { dataViewModel.deleteLocalSignature(sig.id) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}
