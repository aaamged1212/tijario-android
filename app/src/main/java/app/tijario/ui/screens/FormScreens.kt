package app.tijario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import java.time.LocalDate
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.ui.components.buildLogoUploadRequest
import app.tijario.ui.components.clearBusinessLogoCache
import app.tijario.ui.components.loadStoreLogoBitmap
import app.tijario.domain.DocumentCalculator
import app.tijario.domain.DocumentNumbering
import app.tijario.domain.CreationTarget
import app.tijario.domain.CountryCatalog
import app.tijario.domain.CurrencyCatalog
import app.tijario.domain.filterCurrencyOptions
import app.tijario.domain.Validation
import app.tijario.domain.creationTargetForErrorCode
import app.tijario.domain.limitErrorCode
import app.tijario.domain.normalizePhoneWithDialCode
import app.tijario.domain.splitPhoneNumber
import app.tijario.data.model.DocumentType
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.BusinessSettings
import app.tijario.data.remote.localizedDisplayMessage
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
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
import app.tijario.features.documents.ui.AmountPreset
import app.tijario.features.documents.ui.DocumentInvoiceOptionPreferences
import app.tijario.ui.components.ModernDocumentPreview
import app.tijario.ui.components.CountryPickerBottomSheet
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.components.TijarioPhoneField
import app.tijario.ui.components.TijarioSearchField
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

private val MoneyKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Decimal,
    imeAction = ImeAction.Next,
)

private fun formatLocalMoney(value: java.math.BigDecimal, currency: String): String =
    "${value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()} $currency"

private fun selectedLinesCount(value: String): Int =
    value.split('\n').map { it.trim() }.filter { it.isNotEmpty() }.size

/** Keeps a save-time quota race from degrading into a generic form error. */
@Composable
private fun CreationLimitDialog(
    target: CreationTarget,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.getString("limit_reached_title", language)) },
        text = {
            Text(
                LocalizedErrorMapper.map(target.limitErrorCode(), null, language),
            )
        },
        confirmButton = {
            Button(onClick = onUpgrade) {
                Text(Localization.getString("upgrade_plan", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.getString("btn_cancel", language))
            }
        },
    )
}

@Composable
private fun DocumentOptionRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val QuantityKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Number,
    imeAction = ImeAction.Next,
)

private fun todayDocumentDate(): String = LocalDate.now().toString()

internal fun documentNumberPrefix(type: DocumentType): String =
    DocumentNumbering.canonicalPrefix(type)

internal fun documentNumberEditablePart(number: String, type: DocumentType): String =
    DocumentNumbering.editableDigits(number, type)

internal fun displayDraftDocumentNumber(number: String, type: DocumentType): String =
    number.ifBlank { documentNumberPrefix(type) + "..." }

internal fun nextLocalDocumentNumber(documents: List<DocumentSummary>, type: DocumentType): String {
    return DocumentNumbering.nextDocumentNumber(
        documents.asSequence()
            .filter { it.type == type }
            .map(DocumentSummary::documentNumber)
            .asIterable(),
        type,
    )
}

internal fun isDocumentIdentityEditable(isEditMode: Boolean): Boolean = !isEditMode

internal fun shouldDeleteStoredProductImage(
    hasSelectedImage: Boolean,
    imageDeleted: Boolean,
): Boolean = !hasSelectedImage && imageDeleted

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

internal fun invoiceStockValidationMessage(
    documentType: DocumentType,
    item: DocumentItemState,
    items: List<DocumentItemState>,
    products: List<Product>,
    language: AppLanguage,
    originalQuantitiesByProductId: Map<String, Int> = emptyMap(),
): String? {
    if (documentType != DocumentType.Invoice) return null
    val productId = item.productId ?: return null
    val product = products.firstOrNull { it.id == productId } ?: return null
    if (product.kind != ProductKind.Product) return null
    val additionalStockRequired = additionalInvoiceStockRequired(
        documentType = documentType,
        item = item,
        items = items,
        products = products,
        originalQuantitiesByProductId = originalQuantitiesByProductId,
    ) ?: return null
    val currentStock = product.stockQuantity ?: return null
    val availableStock = currentStock + (originalQuantitiesByProductId[productId] ?: 0)

    return Localization.getString("invoice_quantity_exceeds_stock", language).format(availableStock)
}

internal fun additionalInvoiceStockRequired(
    documentType: DocumentType,
    item: DocumentItemState,
    items: List<DocumentItemState>,
    products: List<Product>,
    originalQuantitiesByProductId: Map<String, Int> = emptyMap(),
): Int? {
    if (documentType != DocumentType.Invoice) return null
    val productId = item.productId ?: return null
    val product = products.firstOrNull { it.id == productId } ?: return null
    if (product.kind != ProductKind.Product) return null
    val currentStock = product.stockQuantity ?: return null
    val requestedQuantity = quantityByProductId(items)[productId] ?: return null
    val availableStock = currentStock + (originalQuantitiesByProductId[productId] ?: 0)
    return (requestedQuantity - availableStock).takeIf { it > 0 }
}

internal fun canAddProductToInvoice(
    documentType: DocumentType,
    product: Product,
    items: List<DocumentItemState>,
    originalQuantitiesByProductId: Map<String, Int> = emptyMap(),
): Boolean {
    if (documentType != DocumentType.Invoice || product.kind != ProductKind.Product) return true
    val stock = product.stockQuantity ?: return true
    val reserved = quantityByProductId(items)[product.id].orEmptyInt()
    val originalQuantity = originalQuantitiesByProductId[product.id] ?: 0
    return reserved < stock + originalQuantity
}

internal fun remainingProductStockForPicker(
    documentType: DocumentType,
    product: Product,
    items: List<DocumentItemState>,
    originalQuantitiesByProductId: Map<String, Int> = emptyMap(),
): Int? {
    if (product.kind != ProductKind.Product) return null
    val stock = product.stockQuantity ?: return null
    if (documentType != DocumentType.Invoice) return stock

    val reserved = quantityByProductId(items)[product.id].orEmptyInt()
    val originalQuantity = originalQuantitiesByProductId[product.id] ?: 0
    return (stock + originalQuantity - reserved).coerceAtLeast(0)
}

internal fun isProductCurrencyCompatibleWithDocument(
    documentCurrency: String,
    productCurrency: String,
): Boolean {
    val normalizedDocumentCurrency = documentCurrency.trim()
    val normalizedProductCurrency = productCurrency.trim()
    return normalizedDocumentCurrency.isBlank() ||
        normalizedProductCurrency.isBlank() ||
        normalizedDocumentCurrency.equals(normalizedProductCurrency, ignoreCase = true)
}

internal fun firstDocumentCurrencyMismatch(
    items: List<DocumentItemState>,
    products: List<Product>,
    documentCurrency: String,
): Product? = items
    .mapNotNull { item -> item.productId?.let { productId -> products.firstOrNull { it.id == productId } } }
    .firstOrNull { product -> !isProductCurrencyCompatibleWithDocument(documentCurrency, product.currency) }

private fun Int?.orEmptyInt(): Int = this ?: 0

internal fun quantityByProductId(items: List<DocumentItemState>): Map<String, Int> =
    items.fold(mutableMapOf<String, Int>()) { totals, item ->
        val productId = item.productId
        val quantity = Validation.parsePositiveInt(item.quantity)
        if (productId != null && quantity != null) {
            totals[productId] = (totals[productId] ?: 0) + quantity
        }
        totals
    }

internal fun firstInvoiceStockValidationMessage(
    documentType: DocumentType,
    items: List<DocumentItemState>,
    products: List<Product>,
    language: AppLanguage,
    originalQuantitiesByProductId: Map<String, Int> = emptyMap(),
): String? = items.firstNotNullOfOrNull {
    invoiceStockValidationMessage(documentType, it, items, products, language, originalQuantitiesByProductId)
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
    onCustomerSaved: (app.tijario.data.model.Customer) -> Unit = {},
    onUpgrade: () -> Unit = {},
) {
    val language = LocalLanguage.current
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    var form by remember(language) { mutableStateOf(CustomerFormState(lang = language)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteErrorMsg by remember { mutableStateOf<String?>(null) }
    var isDeletingCustomer by remember { mutableStateOf(false) }
    var limitReachedTarget by remember { mutableStateOf<CreationTarget?>(null) }
    val scope = rememberCoroutineScope()
    val isEditMode = customerId != null
    val defaultCustomerDialCode = remember(uiState.businessSettings?.whatsappNumber, uiState.businessSettings?.country, language) {
        uiState.businessSettings?.whatsappNumber
            ?.takeIf { it.isNotBlank() }
            ?.let { splitPhoneNumber(it).dialCode }
            ?: dialCodeForCountrySelection(uiState.businessSettings?.country.orEmpty())
    }

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

                    TijarioPhoneField(
                        value = form.whatsapp,
                        onValueChange = { form = form.copy(whatsapp = it) },
                        error = if (form.whatsapp.isNotEmpty()) form.whatsappError else null,
                        defaultDialCode = defaultCustomerDialCode,
                        showCountryNameInDialCode = false,
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
                                        onCustomerSaved(customer)
                                        onBack()
                                    } else {
                                        val target = creationTargetForErrorCode(result.exceptionOrNull()?.message)
                                        if (!isEditMode && target != null) {
                                            limitReachedTarget = target
                                        } else {
                                            errorMessage = LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language)
                                                ?: Localization.getString("save_customer_error", language)
                                        }
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

                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                showDeleteConfirm = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text("حذف العميل")
                        }
                    }
                }
            }
        }
    }

    limitReachedTarget?.let { target ->
        CreationLimitDialog(
            target = target,
            language = language,
            onDismiss = { limitReachedTarget = null },
            onUpgrade = {
                limitReachedTarget = null
                onUpgrade()
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false; deleteErrorMsg = null },
            title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("هل أنت متأكد من رغبتك في حذف هذا العميل؟")
                    deleteErrorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val isLinked = dataViewModel.uiState.value.documents.any { it.customerId == customerId }
                                if (isLinked) {
                                    deleteErrorMsg = "لا يمكن حذف هذا العميل لأنه مرتبط بفواتير أو عروض أسعار."
                                    return@launch
                                }
                                val resolvedCustomerId = customerId ?: return@launch
                                isDeletingCustomer = true
                                val res = dataViewModel.deleteCustomer(resolvedCustomerId)
                                if (res.isSuccess) {
                                    showDeleteConfirm = false
                                    onBack()
                                } else {
                                    deleteErrorMsg = LocalizedErrorMapper.map(null, res.exceptionOrNull()?.message, language)
                                }
                            } catch (e: Exception) {
                                deleteErrorMsg = LocalizedErrorMapper.map(null, e.message, language)
                            } finally {
                                isDeletingCustomer = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeletingCustomer) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false; deleteErrorMsg = null }) {
                    Text("إلغاء")
                }
            }
        )
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
    optionLabel: (String) -> String = { it },
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = optionLabel(value),
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
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun dialCodeForCountrySelection(country: String): String = CountryCatalog.dialCodeFor(country)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    dataViewModel: TijarioDataViewModel,
    productId: String? = null,
    onBack: () -> Unit,
    onProductSaved: (Product) -> Unit = {},
    onUpgrade: () -> Unit = {},
) {
    val language = LocalLanguage.current
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    var form by remember(language) { mutableStateOf(app.tijario.ui.state.ProductFormState(lang = language)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var limitReachedTarget by remember { mutableStateOf<CreationTarget?>(null) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var currencyEditedByUser by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isEditMode = productId != null

    LaunchedEffect(isEditMode, uiState.businessSettings?.currency, currencyEditedByUser) {
        val businessCurrency = uiState.businessSettings?.currency?.takeIf { it.isNotBlank() }
        if (!isEditMode && !currencyEditedByUser && businessCurrency != null) {
            form = form.copy(currency = businessCurrency)
        }
    }

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var imageDeleted by remember { mutableStateOf(false) }

    val imageFile = remember(productId, imageDeleted) {
        if (productId != null && !imageDeleted) File(context.filesDir, "product_images/$productId.jpg") else null
    }

    val bitmap = remember(selectedImageUri, imageFile) {
        if (selectedImageUri != null) {
            try {
                val imageUri = selectedImageUri ?: return@remember null
                context.contentResolver.openInputStream(imageUri)?.use { input ->
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        TijarioTextField(
                            label = t("product_price"),
                            value = form.price,
                            onValueChange = { form = form.copy(price = it) },
                            error = if (form.price.isNotEmpty()) form.priceError else null,
                            keyboardOptions = MoneyKeyboardOptions,
                            leadingIcon = { Icon(Icons.Filled.PriceChange, contentDescription = null, tint = Color(0xFF64748B)) },
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = { showCurrencyPicker = true },
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(form.currency, fontWeight = FontWeight.Bold)
                                Text(
                                    text = CountryCatalog.find(CurrencyCatalog.find(form.currency)?.countryCode)?.flag.orEmpty(),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }

                    TijarioTextField(
                        label = if (form.kind == ProductKind.Product) {
                            Localization.getString("available_stock_required", language)
                        } else {
                            Localization.getString("available_stock_optional", language)
                        },
                        value = form.stockQuantity,
                        onValueChange = { form = form.copy(stockQuantity = it) },
                        error = if (form.stockQuantity.isNotEmpty() || form.kind == ProductKind.Product) form.stockQuantityError else null,
                        keyboardOptions = QuantityKeyboardOptions,
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
                                selected = form.kind == ProductKind.Product,
                                onClick = { form = form.copy(kind = ProductKind.Product) }
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
                                selected = form.kind == ProductKind.Service,
                                onClick = { form = form.copy(kind = ProductKind.Service) }
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
                                        stockQuantity = if (form.kind == ProductKind.Product) {
                                            Validation.parsePositiveInt(form.stockQuantity)
                                        } else {
                                            Validation.parseNonNegativeInt(form.stockQuantity)
                                        },
                                        currency = form.currency,
                                        category = form.category.ifBlank { null }
                                    )
                                    val result = if (isEditMode) {
                                        dataViewModel.updateProduct(product)
                                    } else {
                                        dataViewModel.createProduct(product)
                                    }
                                    if (result.isSuccess) {
                                        val hasSelectedImage = selectedImageUri != null
                                        selectedImageUri?.let { imageUri ->
                                            val dir = File(context.filesDir, "product_images")
                                            if (!dir.exists()) dir.mkdirs()
                                            val destFile = File(dir, "${product.id}.jpg")
                                            context.contentResolver.openInputStream(imageUri)?.use { input ->
                                                destFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                        }
                                        if (shouldDeleteStoredProductImage(hasSelectedImage, imageDeleted)) {
                                            val destFile = File(context.filesDir, "product_images/${product.id}.jpg")
                                            if (destFile.exists()) {
                                                destFile.delete()
                                            }
                                        }
                                        onProductSaved(product)
                                        onBack()
                                    } else {
                                        val target = creationTargetForErrorCode(result.exceptionOrNull()?.message)
                                        if (!isEditMode && target != null) {
                                            limitReachedTarget = target
                                        } else {
                                            errorMessage = LocalizedErrorMapper.map(null, result.exceptionOrNull()?.message, language)
                                                ?: Localization.getString("save_product_error", language)
                                        }
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

    limitReachedTarget?.let { target ->
        CreationLimitDialog(
            target = target,
            language = language,
            onDismiss = { limitReachedTarget = null },
            onUpgrade = {
                limitReachedTarget = null
                onUpgrade()
            },
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCurrency = form.currency,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { currency ->
                currencyEditedByUser = true
                form = form.copy(currency = currency)
                showCurrencyPicker = false
            },
        )
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
    var activeDialog by remember { mutableStateOf<String?>(null) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

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
            title = { Text(t("tab_store_account"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (activeDialog) {
                        "name" -> {
                            TijarioTextField(
                                label = t("shop_name"),
                                value = form.businessName,
                                onValueChange = { form = form.copy(businessName = it) },
                                error = if (form.businessName.isNotEmpty()) form.businessNameError else null,
                                leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                        }

                        "phone" -> {
                            TijarioPhoneField(
                                value = form.whatsapp,
                                onValueChange = { form = form.copy(whatsapp = it) },
                                error = if (form.whatsapp.isNotEmpty()) form.whatsappError else null,
                                defaultDialCode = form.whatsapp
                                    .takeIf { it.isNotBlank() }
                                    ?.let { splitPhoneNumber(it).dialCode }
                                    ?: dialCodeForCountrySelection(form.country),
                                onCountryCodeSelected = { option ->
                                    form = form.copy(country = CountryCatalog.find(option.countryCode)?.storageName ?: form.country)
                                },
                            )
                        }
                        "city" -> {
                            TijarioTextField(
                                label = t("city"),
                                value = form.city,
                                onValueChange = { form = form.copy(city = it) },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                        }
                        "address" -> {
                            TijarioTextField(
                                label = t("business_address"),
                                value = form.address,
                                onValueChange = { form = form.copy(address = it) },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                        }
                        "email" -> {
                            TijarioTextField(
                                label = t("business_email"),
                                value = form.email,
                                onValueChange = { form = form.copy(email = it) },
                                error = form.emailError,
                                leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                        }
                        "website" -> {
                            TijarioTextField(
                                label = t("business_website"),
                                value = form.websiteUrl,
                                onValueChange = { form = form.copy(websiteUrl = it) },
                                error = form.websiteError,
                                leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                        }
                        "terms" -> {
                            TijarioTextField(
                                label = t("terms"),
                                value = form.terms,
                                onValueChange = { form = form.copy(terms = it) },
                                singleLine = false,
                                leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
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

    if (showCountryPicker) {
        CountryPickerBottomSheet(
            currentCountry = form.country,
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                form = form.copy(
                    country = country.storageName,
                    whatsapp = splitPhoneNumber(form.whatsapp).let { phone ->
                        normalizePhoneWithDialCode(country.dialCode ?: "+966", phone.localNumber)
                    },
                )
                showCountryPicker = false
            },
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCurrency = form.currency,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { currency ->
                form = form.copy(currency = currency)
                showCurrencyPicker = false
            },
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
                                        logoBitmap != null -> logoBitmap?.let { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                            )
                                        }
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
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Row 1: اسم المتجر / النشاط التجاري
                    SettingsItemRow(
                        icon = Icons.Filled.Storefront,
                        title = t("shop_name"),
                        value = form.businessName.ifBlank { t("app_name") },
                        onClick = { activeDialog = "name" }
                    )




                    // Row 3: رقم التواصل
                    SettingsItemRow(
                        icon = Icons.Filled.Phone,
                        title = t("contact_phone"),
                        value = form.whatsapp,
                        onClick = { activeDialog = "phone" }
                    )


                    // Row 4: الدولة
                    SettingsItemRow(
                        icon = Icons.Filled.Public,
                        title = t("country"),
                        value = form.country.ifBlank { if (language == AppLanguage.AR) "اليمن" else "Yemen" },
                        onClick = { showCountryPicker = true }
                    )


                    // Row 5: المدينة
                    SettingsItemRow(
                        icon = Icons.Filled.Domain,
                        title = t("city"),
                        value = form.city.ifBlank { if (language == AppLanguage.AR) "صنعاء" else "Sana'a" },
                        onClick = { activeDialog = "city" }
                    )


                    SettingsItemRow(
                        icon = Icons.Filled.LocationOn,
                        title = t("business_address"),
                        value = form.address,
                        onClick = { activeDialog = "address" }
                    )


                    SettingsItemRow(
                        icon = Icons.Filled.Description,
                        title = t("business_email"),
                        value = form.email,
                        onClick = { activeDialog = "email" }
                    )


                    SettingsItemRow(
                        icon = Icons.Filled.Public,
                        title = t("business_website"),
                        value = form.websiteUrl,
                        onClick = { activeDialog = "website" }
                    )


                    // Row 6: العملة
                    SettingsItemRow(
                        icon = Icons.Filled.AttachMoney,
                        title = t("currency"),
                        value = CurrencyCatalog.display(form.currency, language),
                        onClick = { showCurrencyPicker = true }
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
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .clickable { onClick() }
            .padding(vertical = 9.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (value.isNotBlank()) {
                Text(value, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
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
            state.lang.name,
            state.operationId,
            state.discountType,
            state.shippingAmount,
            state.shippingLabel
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
        val hasLocalOptions = list.size >= 30 && (list.size - 30) % 10 == 0
        val hasLabels = hasLocalOptions || list.size >= 27
        val headerSize = if (hasLocalOptions) 30 else if (hasLabels) 27 else 24
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
        val operationId = if (hasLabels) list[26] as String else java.util.UUID.randomUUID().toString()
        val discountType = if (hasLocalOptions) list[27] as String else "fixed"
        val shippingAmount = if (hasLocalOptions) list[28] as String else ""
        val shippingLabel = if (hasLocalOptions) list[29] as String else ""

        val itemsList = mutableListOf<app.tijario.ui.state.DocumentItemState>()
        val itemsData = list.subList(headerSize, list.size)
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
            discountType = discountType,
            extraFees = extraFees,
            shippingAmount = shippingAmount,
            shippingLabel = shippingLabel,
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
            lang = lang,
            operationId = operationId
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceInfoDialog(
    form: app.tijario.ui.state.DocumentFormState,
    documentType: DocumentType,
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
    val numberPrefix = documentNumberPrefix(documentType)
    var documentNumberDigits by remember(form.documentNumber, documentType) {
        mutableStateOf(documentNumberEditablePart(form.documentNumber, documentType))
    }
    var creationDate by remember(form.creationDate) { mutableStateOf(form.creationDate.ifBlank { todayDocumentDate() }) }
    var dueTerms by remember { mutableStateOf(form.dueTerms) }
    var dueDate by remember { mutableStateOf(form.dueDate) }
    var showCreationDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var poNumber by remember { mutableStateOf(form.poNumber) }
    var invoiceTitle by remember(form.documentTitle) { mutableStateOf(form.documentTitle) }
    val identityLocked = !isDocumentIdentityEditable(isEditMode)

    if (showCreationDatePicker) {
        val initialDateMillis = runCatching { LocalDate.parse(creationDate) }.getOrNull()
            ?.atStartOfDay(java.time.ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
            ?: LocalDate.now().atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        DatePickerDialog(
            onDismissRequest = { showCreationDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            creationDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                        }
                        showCreationDatePicker = false
                    }
                ) { Text(t("btn_ok")) }
            },
            dismissButton = {
                TextButton(onClick = { showCreationDatePicker = false }) { Text(t("btn_cancel")) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDueDatePicker) {
        val initialDateMillis = runCatching { LocalDate.parse(dueDate) }.getOrNull()
            ?.atStartOfDay(java.time.ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
            ?: LocalDate.now().atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            dueDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                        }
                        showDueDatePicker = false
                    }
                ) { Text(t("btn_ok")) }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text(t("btn_cancel")) }
            },
        ) {
            DatePicker(state = pickerState)
        }
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
                        title = { Text(t(if (documentType == DocumentType.Invoice) "invoice_info_title" else "quote_info_title"), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onSave(numberPrefix + documentNumberDigits, creationDate, dueTerms, dueDate, poNumber, invoiceTitle)
                                },
                                enabled = documentNumberDigits.isNotBlank() && creationDate.isNotBlank()
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                ) {
                                    Text(
                                        text = numberPrefix,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                TijarioTextField(
                                    label = t(if (documentType == DocumentType.Invoice) "invoice_number" else "quote_number") + " *",
                                    value = documentNumberDigits,
                                    onValueChange = { if (!identityLocked) documentNumberDigits = it },
                                    readOnly = identityLocked,
                                    keyboardOptions = QuantityKeyboardOptions,
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            TijarioTextField(
                                label = t("creation_date") + " *",
                                value = creationDate,
                                onValueChange = { if (!identityLocked) creationDate = it },
                                readOnly = identityLocked,
                                keyboardOptions = QuantityKeyboardOptions,
                                trailingIcon = {
                                    if (!identityLocked) {
                                        IconButton(onClick = { showCreationDatePicker = true }) {
                                            Icon(Icons.Filled.DateRange, contentDescription = t("creation_date"))
                                        }
                                    }
                                },
                            )

                            var showTermsDropdown by remember { mutableStateOf(false) }

                            TijarioTextField(
                                label = t("due_date"),
                                value = dueDate,
                                onValueChange = { dueDate = it },
                                keyboardOptions = QuantityKeyboardOptions,
                                trailingIcon = {
                                    IconButton(onClick = { showDueDatePicker = true }) {
                                        Icon(Icons.Filled.DateRange, contentDescription = t("due_date"))
                                    }
                                },
                            )

                            TijarioTextField(
                                label = t(if (documentType == DocumentType.Invoice) "invoice_title_name" else "quote_title_name"),
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
    documentType: DocumentType,
    itemsForStockValidation: List<DocumentItemState>,
    products: List<Product>,
    language: AppLanguage,
    originalQuantitiesByProductId: Map<String, Int> = emptyMap(),
    onDismiss: () -> Unit,
    onSave: (app.tijario.ui.state.DocumentItemState) -> Unit,
    onDelete: () -> Unit,
    onChooseProduct: () -> Unit,
    onIncreaseStock: (productId: String, amount: Int) -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.unitPrice) }
    var quantity by remember { mutableStateOf(item.quantity) }
    var unitOfMeasure by remember { mutableStateOf(item.unitOfMeasure) }
    var description by remember { mutableStateOf(item.description) }

    val parsedPrice = Validation.parseNonNegativeMoney(price) ?: 0.0
    val parsedQty = Validation.parsePositiveInt(quantity) ?: 1
    val totalAmount = parsedPrice * parsedQty
    val draftItem = item.copy(quantity = quantity)
    val draftItems = itemsForStockValidation.map { current ->
        if (current.id == item.id) draftItem else current
    }
    val quantityStockError = invoiceStockValidationMessage(
        documentType = documentType,
        item = draftItem,
        items = draftItems,
        products = products,
        language = language,
        originalQuantitiesByProductId = originalQuantitiesByProductId,
    )
    val additionalStockRequired = additionalInvoiceStockRequired(
        documentType = documentType,
        item = draftItem,
        items = draftItems,
        products = products,
        originalQuantitiesByProductId = originalQuantitiesByProductId,
    )
    var stockIncreaseAmount by remember(additionalStockRequired) {
        mutableStateOf(additionalStockRequired?.toString().orEmpty())
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
                                    if (quantityStockError == null) {
                                        onSave(
                                            item.copy(
                                                name = name,
                                                unitPrice = price,
                                                quantity = quantity,
                                                description = description,
                                                unitOfMeasure = unitOfMeasure,
                                            )
                                        )
                                    }
                                },
                                enabled = name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank() && quantityStockError == null
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
                                onValueChange = { price = it },
                                keyboardOptions = MoneyKeyboardOptions,
                            )

                            TijarioTextField(
                                label = t("item_qty_label"),
                                value = quantity,
                                onValueChange = { quantity = it },
                                error = quantityStockError,
                                keyboardOptions = QuantityKeyboardOptions,
                            )

                            if (quantityStockError != null && item.productId != null) {
                                TijarioTextField(
                                    label = Localization.getString("stock_increase_amount", language),
                                    value = stockIncreaseAmount,
                                    onValueChange = { stockIncreaseAmount = it },
                                    keyboardOptions = QuantityKeyboardOptions,
                                )
                                Button(
                                    onClick = {
                                        val amount = Validation.parsePositiveInt(stockIncreaseAmount)
                                        if (amount != null) onIncreaseStock(item.productId, amount)
                                    },
                                    enabled = Validation.parsePositiveInt(stockIncreaseAmount) != null,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(Localization.getString("add_stock_and_continue", language))
                                }
                            }

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
    onNavigateToCreateCustomer: () -> Unit = {},
    onNavigateToCreateProduct: (Int) -> Unit = {},
    selectedCustomer: app.tijario.data.model.Customer? = null,
    selectedProduct: app.tijario.data.model.Product? = null,
    selectedProductRowIndex: Int? = null,
    onSelectedCustomerConsumed: () -> Unit = {},
    onSelectedProductConsumed: () -> Unit = {},
    onNavigateToBusinessSettings: () -> Unit = {},
    onUpgrade: () -> Unit = {},
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
    var documentNumberEditedByUser by rememberSaveable { mutableStateOf(false) }
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
    var originalQuantitiesByProductId by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showInvoiceInfoDialog by remember { mutableStateOf(false) }
    var showTemplatePickerDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var currencyEditedByUser by rememberSaveable { mutableStateOf(false) }
    var showLocalTaxesDialog by remember { mutableStateOf(false) }
    var showLocalPaymentDialog by remember { mutableStateOf(false) }
    var showLocalSignaturesDialog by remember { mutableStateOf(false) }
    var showLocalTermsDialog by remember { mutableStateOf(false) }
    var showDiscountSheet by remember { mutableStateOf(false) }
    var showExtraFeesSheet by remember { mutableStateOf(false) }
    var showShippingSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showCustomerPickerSheet by remember { mutableStateOf(false) }
    var showProductPickerRowIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var limitReachedTarget by remember { mutableStateOf<CreationTarget?>(null) }
    var customerPickerQuery by rememberSaveable { mutableStateOf("") }
    var productPickerQuery by rememberSaveable { mutableStateOf("") }

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

    var suggestedDocumentNumber by rememberSaveable(type) { mutableStateOf("") }
    var isLoadingNextDocumentNumber by rememberSaveable(isEditMode, type) { mutableStateOf(false) }

    LaunchedEffect(isEditMode, type) {
        if (isEditMode) {
            isLoadingNextDocumentNumber = false
            return@LaunchedEffect
        }
        isLoadingNextDocumentNumber = true
        try {
            val nextNumber = runCatching {
                dataViewModel.getNextDocumentNumber(type.name).data?.documentNumber
            }.getOrNull()
                ?: nextLocalDocumentNumber(uiState.documents, type)
            val shouldApplyLocalNumber = !documentNumberEditedByUser &&
                (form.documentNumber.isBlank() || form.documentNumber == suggestedDocumentNumber)
            suggestedDocumentNumber = nextNumber
            if (shouldApplyLocalNumber) {
                form = form.copy(documentNumber = nextNumber)
            }
        } finally {
            isLoadingNextDocumentNumber = false
        }
    }

    LaunchedEffect(isEditMode, language, type) {
        if (!isEditMode && form.documentTitle.isBlank()) {
            form = form.copy(
                documentTitle = defaultDocumentTitle(type, form.documentLanguage),
            )
        }
        if (!isEditMode) {
            originalQuantitiesByProductId = emptyMap()
        }
    }

    LaunchedEffect(language) {
        form = form.copy(
            lang = language,
            documentTitle = documentTitleAfterLanguageChange(
                type = type,
                currentTitle = form.documentTitle,
                nextDocumentLanguage = form.documentLanguage,
                titleEditedByUser = titleEditedByUser,
            ),
        )
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
        firstDocumentCurrencyMismatch(form.items, uiState.products, form.currency)?.let {
            showDocumentError(Localization.getString("cannot_add_product_currency_mismatch", language))
            return
        }
        firstInvoiceStockValidationMessage(
            type,
            form.items,
            uiState.products,
            language,
            originalQuantitiesByProductId,
        )?.let {
            showDocumentError(it)
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
            discountType = form.discountType,
            shippingStr = form.shippingAmount,
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
                    operationId = form.operationId,
                    paymentStatus = if (type == app.tijario.data.model.DocumentType.Invoice) form.paymentStatus else null,
                    amountPaid = if (type == app.tijario.data.model.DocumentType.Invoice && form.paymentStatus == "partial") Validation.parseNonNegativeMoney(form.amountPaid) else null,
                    documentNumber = form.documentNumber.ifBlank { null },
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
                    discount = totals.discount.toDouble(),
                    extraFees = totals.extraFees.toDouble(),
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
                        invoiceOptionPreferences.setDocumentTitleOverride(savedDocumentId, form.documentTitle)
                        invoiceOptionPreferences.setDocumentNumberOverride(savedDocumentId, form.documentNumber)
                        dataViewModel.upsertDocumentMetadata(
                            app.tijario.data.local.LocalDocumentMetadataEntity(
                                documentId = savedDocumentId,
                                currency = form.currency,
                                signatureData = form.signatureData.takeIf { it.isNotEmpty() },
                                paymentMethod = form.paymentMethod.takeIf { it.isNotEmpty() },
                                taxRate = Validation.parseNonNegativeMoney(form.finalTaxRate) ?: 0.0,
                                taxName = form.finalTaxName.ifBlank { "Tax" },
                                discountType = form.discountType,
                                discountValue = form.discount.takeIf { it.isNotBlank() },
                                shippingAmount = Validation.parseNonNegativeMoney(form.shippingAmount) ?: 0.0,
                                shippingLabel = form.shippingLabel.takeIf { it.isNotBlank() },
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
                    val target = creationTargetForErrorCode(result.code)
                    if (!isEditMode && target != null) {
                        limitReachedTarget = target
                    } else {
                        showDocumentError(message)
                    }
                }
            } catch (e: Exception) {
                val target = creationTargetForErrorCode(e.message)
                if (!isEditMode && target != null) {
                    limitReachedTarget = target
                } else {
                    showDocumentError(LocalizedErrorMapper.map(null, e.message, language))
                }
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
                onSelectedCustomerConsumed()
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
                    val loadedForm = existing.toFormState(language).copy(
                        documentNumber = invoiceOptionPreferences.getDocumentNumberOverride(currentEditDocumentId)
                            ?: existing.documentNumber,
                        documentTitle = invoiceOptionPreferences.getDocumentTitleOverride(currentEditDocumentId)
                            ?: existing.documentTitle
                            ?: existing.toFormState(language).documentTitle,
                        currency = metadata?.currency ?: existing.currency.ifBlank { null } ?: form.currency,
                        signatureData = metadata?.signatureData.orEmpty(),
                        paymentMethod = metadata?.paymentMethod.orEmpty(),
                        finalTaxRate = metadata?.taxRate?.toString() ?: form.finalTaxRate,
                        finalTaxName = metadata?.taxName ?: form.finalTaxName,
                        discountType = metadata?.discountType ?: form.discountType,
                        discount = metadata?.discountValue ?: form.discount,
                        shippingAmount = metadata?.shippingAmount?.takeIf { it > 0.0 }?.toString().orEmpty(),
                        shippingLabel = metadata?.shippingLabel.orEmpty(),
                        lang = language
                    )
                    form = loadedForm
                    originalQuantitiesByProductId = quantityByProductId(loadedForm.items)
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
                currency = if (currencyEditedByUser) form.currency else businessSettings?.currency
                    ?.takeIf { it.isNotBlank() }
                    ?: form.currency,
                paymentMethod = form.paymentMethod.ifBlank { defaults.paymentMethod },
                terms = form.terms.ifBlank { defaults.termsContent.ifBlank { businessSettings?.termsText.orEmpty() } },
                signatureData = form.signatureData.ifBlank { defaults.signatureData },
                finalTaxName = if (form.finalTaxRate.isBlank() && defaults.taxName.isNotBlank()) {
                    defaults.taxName
                } else {
                    form.finalTaxName
                },
                finalTaxRate = form.finalTaxRate.ifBlank { defaults.taxRate },
            )
        }
    }

    // Sync selected product to specific row
    LaunchedEffect(selectedProduct, selectedProductRowIndex) {
        selectedProduct?.let { prod ->
            if (isProductCurrencyCompatibleWithDocument(form.currency, prod.currency)) {
                val idx = selectedProductRowIndex ?: pendingProductRowIndex ?: form.items.size
                val (updatedItems, targetIndex) = mergeSelectedProductIntoItems(form.items, prod, idx)
                form = form.copy(items = updatedItems)
                editingItemIndex = targetIndex
            } else {
                showDocumentError(Localization.getString("cannot_add_product_currency_mismatch", language))
            }
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
                // Info Top Card (e.g. INV-1132 / Created on today's date)
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
                                text = displayDraftDocumentNumber(form.documentNumber, type),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(t("created_on"), form.creationDate.ifBlank { todayDocumentDate() }),
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
                                    customerPickerQuery = ""
                                    showCustomerPickerSheet = true
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
                                val stockError = invoiceStockValidationMessage(
                                    documentType = type,
                                    item = item,
                                    items = form.items,
                                    products = uiState.products,
                                    language = language,
                                    originalQuantitiesByProductId = originalQuantitiesByProductId,
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                                        val parsedPrice = Validation.parseNonNegativeMoney(item.unitPrice) ?: 0.0
                                        val parsedQty = Validation.parsePositiveInt(item.quantity) ?: 1
                                        val itemTotal = parsedPrice * parsedQty

                                        Text(
                                            text = String.format("%.2f", itemTotal),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (stockError != null) {
                                        Text(
                                            text = stockError,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                        )
                                    }
                                }
                            }
                        }

                        val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
                        OutlinedButton(
                            onClick = {
                                pendingProductRowIndex = form.items.size
                                productPickerQuery = ""
                                showProductPickerRowIndex = form.items.size
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
                        val liveCalculation = DocumentCalculator.calculate(
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
                            discountType = form.discountType,
                            shippingStr = form.shippingAmount,
                        )
                        val discountTitle = if (form.discountType.equals("percent", ignoreCase = true) && form.discount.isNotBlank()) {
                            "${t("form_discount")} (${form.discount}%)"
                        } else {
                            t("form_discount")
                        }
                        val discountValue = if (liveCalculation.discount > java.math.BigDecimal.ZERO) {
                            "- ${formatLocalMoney(liveCalculation.discount, form.currency)}"
                        } else {
                            t("not_specified")
                        }
                        DocumentOptionRow(
                            icon = Icons.Filled.LocalOffer,
                            title = discountTitle,
                            value = discountValue,
                            onClick = { showDiscountSheet = true },
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        DocumentOptionRow(
                            icon = Icons.Filled.PriceChange,
                            title = t("form_extra_fees"),
                            value = if (liveCalculation.extraFees > java.math.BigDecimal.ZERO) formatLocalMoney(liveCalculation.extraFees, form.currency) else t("not_specified"),
                            onClick = { showExtraFeesSheet = true },
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        val parsedTax = Validation.parseNonNegativeMoney(form.finalTaxRate) ?: 0.0
                        DocumentOptionRow(
                            icon = Icons.Filled.AccountBalance,
                            title = t("tax"),
                            value = if (parsedTax > 0.0) "${form.finalTaxName} ($parsedTax%)" else t("no_tax"),
                            onClick = { showLocalTaxesDialog = true },
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        DocumentOptionRow(
                            icon = Icons.Filled.LocalShipping,
                            title = Localization.getString("shipping", language),
                            value = if (liveCalculation.shipping > java.math.BigDecimal.ZERO) formatLocalMoney(liveCalculation.shipping, form.currency) else t("not_specified"),
                            onClick = { showShippingSheet = true },
                        )

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
                            discountType = form.discountType,
                            shippingStr = form.shippingAmount,
                        )
                        val subtotalVal = calculations.subtotal.toDouble()
                        val discountAmount = calculations.discount.toDouble()
                        val extraFeesAmount = calculations.extraFees.toDouble()
                        val taxAmount = calculations.taxAmount.toDouble()
                        val shippingAmount = calculations.shipping.toDouble()
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
                            if (discountAmount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = form.discountLabel.ifBlank { discountTitle },
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(text = String.format("- %.2f %s", discountAmount, form.currency), fontSize = 14.sp)
                                }
                            }
                            if (extraFeesAmount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = form.extraFeesLabel.ifBlank { t("form_extra_fees") },
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(text = String.format("%.2f %s", extraFeesAmount, form.currency), fontSize = 14.sp)
                                }
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
                            if (shippingAmount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = form.shippingLabel.ifBlank { Localization.getString("shipping", language) },
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(text = String.format("%.2f %s", shippingAmount, form.currency), fontSize = 14.sp)
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
                                    keyboardOptions = MoneyKeyboardOptions,
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
                                val pmCount = selectedLinesCount(form.paymentMethod)
                                val pmText = when {
                                    pmCount == 0 -> t("not_specified")
                                    pmCount == 1 -> form.paymentMethod.lineSequence().first().trim()
                                    else -> if (language == AppLanguage.AR) "$pmCount طرق دفع" else "$pmCount payment methods"
                                }
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
                                val termsCount = form.terms.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }.size
                                val termsText = when {
                                    form.terms.isBlank() -> t("not_specified")
                                    termsCount > 1 -> if (language == AppLanguage.AR) "$termsCount شروط" else "$termsCount terms"
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
                    documentType = type,
                    itemsForStockValidation = form.items,
                    products = uiState.products,
                    language = language,
                    originalQuantitiesByProductId = originalQuantitiesByProductId,
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
                        productPickerQuery = ""
                        showProductPickerRowIndex = index
                    },
                    onIncreaseStock = { productId, amount ->
                        scope.launch {
                            dataViewModel.increaseProductStock(productId, amount)
                                .onFailure { error ->
                                    showDocumentError(LocalizedErrorMapper.map(null, error.message, language))
                                }
                        }
                    },
                )
            }
        }
    }

    if (showInvoiceInfoDialog) {
        InvoiceInfoDialog(
            form = form,
            documentType = type,
            isEditMode = isEditMode,
            onDismiss = { showInvoiceInfoDialog = false },
            onSave = { docNum, date, terms, due, po, title ->
                titleEditedByUser = title.isNotBlank() && !isDefaultDocumentTitle(type, title)
                documentNumberEditedByUser = suggestedDocumentNumber.isBlank() || docNum != suggestedDocumentNumber
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

    if (showDiscountSheet) {
        AmountOptionSheet(
            title = t("form_discount"),
            amountLabel = t("form_discount"),
            reasonLabel = Localization.getString("doc_discount_reason", language),
            amount = form.discount,
            reason = form.discountLabel,
            discountType = form.discountType,
            showDiscountType = true,
            presets = invoiceOptionPreferences.getDiscountPresets(),
            onDismiss = { showDiscountSheet = false },
            onSave = { amount, reason, type ->
                form = form.copy(
                    discount = amount,
                    discountLabel = reason,
                    discountType = type ?: "fixed",
                )
                invoiceOptionPreferences.addDiscountPreset(amount, reason, type ?: "fixed")
                showDiscountSheet = false
            },
        )
    }

    if (showExtraFeesSheet) {
        AmountOptionSheet(
            title = t("form_extra_fees"),
            amountLabel = t("form_extra_fees"),
            reasonLabel = Localization.getString("doc_extra_fee_reason", language),
            amount = form.extraFees,
            reason = form.extraFeesLabel,
            presets = invoiceOptionPreferences.getExtraFeesPresets(),
            onDismiss = { showExtraFeesSheet = false },
            onSave = { amount, reason, _ ->
                form = form.copy(extraFees = amount, extraFeesLabel = reason)
                invoiceOptionPreferences.addExtraFeesPreset(amount, reason)
                showExtraFeesSheet = false
            },
        )
    }

    if (showShippingSheet) {
        AmountOptionSheet(
            title = Localization.getString("shipping", language),
            amountLabel = Localization.getString("shipping_amount", language),
            reasonLabel = Localization.getString("shipping_reason", language),
            amount = form.shippingAmount,
            reason = form.shippingLabel,
            onDismiss = { showShippingSheet = false },
            onSave = { amount, reason, _ ->
                form = form.copy(shippingAmount = amount, shippingLabel = reason)
                showShippingSheet = false
            },
        )
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCurrency = form.currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                currencyEditedByUser = true
                form = form.copy(currency = it)
                showCurrencyDialog = false
            }
        )
    }

    if (showLocalTaxesDialog) {
        LocalTaxesManagerDialog(
            dataViewModel = dataViewModel,
            selectedNames = form.finalTaxName.split('\n').map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            onDismiss = { showLocalTaxesDialog = false },
            onApplyTax = { name, rate ->
                form = form.copy(finalTaxName = name, finalTaxRate = rate)
                invoiceOptionPreferences.setTax(name, rate)
            }
        )
    }

    if (showLocalPaymentDialog) {
        LocalPaymentMethodsManagerDialog(
            dataViewModel = dataViewModel,
            currentValue = form.paymentMethod,
            onDismiss = { showLocalPaymentDialog = false },
            onSelect = { pm ->
                form = form.copy(paymentMethod = pm)
                invoiceOptionPreferences.setPaymentMethod(pm)
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
            currentContent = form.terms,
            onDismiss = { showLocalTermsDialog = false },
            onSelect = { title, content ->
                form = form.copy(terms = content)
                invoiceOptionPreferences.setTerms(title, content)
            }
        )
    }

    if (showCustomerPickerSheet) {
        val filteredCustomers = remember(uiState.customers, customerPickerQuery) {
            uiState.customers.filter {
                it.name.contains(customerPickerQuery, ignoreCase = true) ||
                    it.whatsappNumber.contains(customerPickerQuery)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { showCustomerPickerSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = t("select_customer"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TijarioSearchField(
                        placeholder = t("search_placeholder"),
                        value = customerPickerQuery,
                        onValueChange = { customerPickerQuery = it },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = {
                            showCustomerPickerSheet = false
                            onNavigateToCreateCustomer()
                        },
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(t("picker_new"))
                    }
                }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredCustomers) { customer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    form = form.copy(
                                        customerId = customer.id,
                                        customerName = customer.name,
                                        customerWhatsapp = customer.whatsappNumber,
                                        customerCity = customer.city,
                                    )
                                    showCustomerPickerSheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(customer.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(customer.whatsappNumber, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    showProductPickerRowIndex?.let { rowIndex ->
        val filteredProducts = remember(uiState.products, productPickerQuery) {
            uiState.products.filter {
                it.name.contains(productPickerQuery, ignoreCase = true) ||
                    (it.description ?: "").contains(productPickerQuery, ignoreCase = true)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { showProductPickerRowIndex = null },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(t("select_product"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TijarioSearchField(
                        placeholder = t("search_products"),
                        value = productPickerQuery,
                        onValueChange = { productPickerQuery = it },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = {
                            showProductPickerRowIndex = null
                            onNavigateToCreateProduct(rowIndex)
                        },
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(t("picker_new"))
                    }
                }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredProducts) { product ->
                        val itemsForStockSelection = if (rowIndex in form.items.indices) {
                            form.items.filterIndexed { index, _ -> index != rowIndex }
                        } else {
                            form.items
                        }
                        val hasMatchingCurrency = isProductCurrencyCompatibleWithDocument(
                            documentCurrency = form.currency,
                            productCurrency = product.currency,
                        )
                        val hasAvailableStock = canAddProductToInvoice(
                            documentType = type,
                            product = product,
                            items = itemsForStockSelection,
                            originalQuantitiesByProductId = originalQuantitiesByProductId,
                        )
                        val canAddProduct = hasMatchingCurrency && hasAvailableStock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = canAddProduct) {
                                    val (updatedItems, targetIndex) = mergeSelectedProductIntoItems(form.items, product, rowIndex)
                                    form = form.copy(items = updatedItems)
                                    editingItemIndex = targetIndex
                                    pendingProductRowIndex = null
                                    showProductPickerRowIndex = null
                                }
                                .padding(12.dp)
                                .then(if (canAddProduct) Modifier else Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Filled.BusinessCenter,
                                contentDescription = null,
                                tint = if (canAddProduct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(product.description ?: t("kind_product"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                remainingProductStockForPicker(
                                    documentType = type,
                                    product = product,
                                    items = itemsForStockSelection,
                                    originalQuantitiesByProductId = originalQuantitiesByProductId,
                                )?.let { remainingStock ->
                                    Text(
                                        text = "${Localization.getString("available_stock", language)}$remainingStock",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                    )
                                }
                                if (!hasMatchingCurrency) {
                                    Text(
                                        text = Localization.getString("cannot_add_product_currency_mismatch", language),
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                    )
                                } else if (!hasAvailableStock) {
                                    Text(
                                        text = Localization.getString("cannot_add_product_insufficient_stock", language),
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                            Text("${product.price} ${product.currency}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    // A store setting may arrive after the draft route. It owns the initial currency,
    // until the seller explicitly chooses a different per-document value.
    LaunchedEffect(isEditMode, businessSettings?.currency, currencyEditedByUser) {
        val businessCurrency = businessSettings?.currency?.takeIf { it.isNotBlank() }
        if (!isEditMode && !currencyEditedByUser && businessCurrency != null) {
            form = form.copy(currency = businessCurrency)
        }
    }

    limitReachedTarget?.let { target ->
        CreationLimitDialog(
            target = target,
            language = language,
            onDismiss = { limitReachedTarget = null },
            onUpgrade = {
                limitReachedTarget = null
                onUpgrade()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountOptionSheet(
    title: String,
    amountLabel: String,
    reasonLabel: String,
    amount: String,
    reason: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit,
    discountType: String = "fixed",
    showDiscountType: Boolean = false,
    presets: List<AmountPreset> = emptyList(),
) {
    val language = LocalLanguage.current
    var localAmount by rememberSaveable { mutableStateOf(amount) }
    var localReason by rememberSaveable { mutableStateOf(reason) }
    var localDiscountType by rememberSaveable { mutableStateOf(discountType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (showDiscountType) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "fixed" to Localization.getString("discount_fixed", language),
                        "percent" to Localization.getString("discount_percentage", language),
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = localDiscountType.equals(value, ignoreCase = true),
                            onClick = { localDiscountType = value },
                            label = { Text(label) },
                        )
                    }
                }
            }
            if (presets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        AssistChip(
                            onClick = {
                                localAmount = preset.amount
                                localReason = preset.reason
                                localDiscountType = preset.type
                            },
                            label = {
                                Text(
                                    listOf(preset.reason, preset.amount)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" - ")
                                )
                            },
                        )
                    }
                }
            }
            TijarioTextField(
                label = amountLabel,
                value = localAmount,
                onValueChange = { localAmount = it },
                keyboardOptions = MoneyKeyboardOptions,
                leadingIcon = {
                    Icon(
                        if (showDiscountType && localDiscountType.equals("percent", ignoreCase = true)) Icons.Filled.Percent else Icons.Filled.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            TijarioTextField(
                label = reasonLabel,
                value = localReason,
                onValueChange = { localReason = it },
                leadingIcon = {
                    Icon(Icons.Filled.Note, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(Localization.getString("btn_cancel", language))
                }
                Button(
                    onClick = {
                        onSave(
                            localAmount.trim(),
                            localReason.trim(),
                            if (showDiscountType) localDiscountType else null,
                        )
                    },
                ) {
                    Text(Localization.getString("btn_save", language))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val language = LocalLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    val currencies = remember(query, language) {
        filterCurrencyOptions(query, language)
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
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
                    text = Localization.getString("select_currency", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                TijarioSearchField(
                    placeholder = t("search_currency_placeholder"),
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currencies) { currency ->
                        val isSelected = currency.code == currentCurrency
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelect(currency.code) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = CurrencyCatalog.display(currency.code, language),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.getString("btn_cancel", language))
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
    selectedNames: Set<String>,
    onDismiss: () -> Unit,
    onApplyTax: (String, String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val taxes by dataViewModel.observeLocalTaxes().collectAsState(initial = emptyList())
    var newTaxName by remember { mutableStateOf("") }
    var newTaxRate by remember { mutableStateOf("") }
    var selected by remember(selectedNames) { mutableStateOf(selectedNames) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
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
                        singleLine = true,
                        keyboardOptions = MoneyKeyboardOptions
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
                                .clickable {
                                    val next = if (selected.contains(tax.name)) selected - tax.name else selected + tax.name
                                    selected = next
                                    val selectedTaxes = taxes.filter { next.contains(it.name) }
                                    onApplyTax(
                                        selectedTaxes.joinToString("\n") { it.name },
                                        selectedTaxes.sumOf { it.rate }.toString()
                                    )
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tax.name, fontWeight = FontWeight.Bold)
                                Text("${tax.rate}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selected.contains(tax.name),
                                    onCheckedChange = { checked ->
                                        val next = if (checked) selected + tax.name else selected - tax.name
                                        selected = next
                                        val selectedTaxes = taxes.filter { next.contains(it.name) }
                                        onApplyTax(
                                            selectedTaxes.joinToString("\n") { it.name },
                                            selectedTaxes.sumOf { it.rate }.toString()
                                        )
                                    }
                                )
                                IconButton(onClick = {
                                    scope.launch { dataViewModel.deleteLocalTax(tax.id) }
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
fun LocalPaymentMethodsManagerDialog(
    dataViewModel: TijarioDataViewModel,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val methods by dataViewModel.observeLocalPaymentMethods().collectAsState(initial = emptyList())
    var newMethodName by remember { mutableStateOf("") }
    var selected by remember(currentValue) {
        mutableStateOf(currentValue.split('\n').map { it.trim() }.filter { it.isNotEmpty() }.toSet())
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
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
                                .clickable {
                                    val next = if (selected.contains(method.name)) selected - method.name else selected + method.name
                                    selected = next
                                    onSelect(next.joinToString("\n"))
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selected.contains(method.name),
                                    onCheckedChange = { checked ->
                                        val next = if (checked) selected + method.name else selected - method.name
                                        selected = next
                                        onSelect(next.joinToString("\n"))
                                    }
                                )
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
    currentContent: String,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val isArabic = LocalLanguage.current == app.tijario.config.AppLanguage.AR
    val scope = rememberCoroutineScope()
    val termsList by dataViewModel.observeLocalTerms().collectAsState(initial = emptyList())
    var termTitle by remember { mutableStateOf("") }
    var termContent by remember { mutableStateOf("") }
    var selectedContent by remember(currentContent) {
        mutableStateOf(currentContent.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }.toSet())
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
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
                                .clickable {
                                    val next = if (selectedContent.contains(term.content)) selectedContent - term.content else selectedContent + term.content
                                    selectedContent = next
                                    val selectedTerms = termsList.filter { next.contains(it.content) }
                                    onSelect(
                                        selectedTerms.joinToString(" + ") { it.title },
                                        selectedTerms.joinToString("\n\n") { it.content }
                                    )
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(term.title, fontWeight = FontWeight.Bold)
                                Text(term.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedContent.contains(term.content),
                                    onCheckedChange = { checked ->
                                        val next = if (checked) selectedContent + term.content else selectedContent - term.content
                                        selectedContent = next
                                        val selectedTerms = termsList.filter { next.contains(it.content) }
                                        onSelect(
                                            selectedTerms.joinToString(" + ") { it.title },
                                            selectedTerms.joinToString("\n\n") { it.content }
                                        )
                                    }
                                )
                                IconButton(onClick = {
                                    scope.launch { dataViewModel.deleteLocalTerms(term.id) }
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

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
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
