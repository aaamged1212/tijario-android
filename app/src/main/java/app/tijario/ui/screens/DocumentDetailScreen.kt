package app.tijario.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.AppLanguage
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.export.DocumentDownloadManager
import app.tijario.features.documents.export.DocumentExportAction
import app.tijario.features.documents.export.DocumentExportManager
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.preview.DocumentPreviewWebView
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.ui.DocumentExportSheet
import app.tijario.features.documents.ui.DocumentInvoiceOptionPreferences
import app.tijario.features.documents.ui.DocumentTemplatePicker
import app.tijario.features.documents.ui.DocumentTemplatePreferences
import app.tijario.domain.LocalizedErrorMapper
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    dataViewModel: TijarioDataViewModel,
    documentId: String,
    onBack: () -> Unit,
    onEditClick: (String, DocumentType) -> Unit,
) {
    val language = LocalLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val businessSettings = uiState.businessSettings
    val planUsage = uiState.planUsage
    val exportManager = remember(context) { DocumentExportManager(context) }
    val invoiceOptionPreferences = remember(context) { DocumentInvoiceOptionPreferences(context) }
    val templatePreferences = remember(context) { DocumentTemplatePreferences(context) }

    var document by remember { mutableStateOf<CompleteDocument?>(null) }
    var documentMetadata by remember { mutableStateOf<app.tijario.data.local.LocalDocumentMetadataEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var selectedTemplateId by remember { mutableStateOf(templatePreferences.getDefaultTemplateId()) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showFullScreenPreview by remember { mutableStateOf(false) }

    fun reloadDocument() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = dataViewModel.fetchCompleteDocument(documentId)
            document = result.getOrNull()
            if (document != null) {
                documentMetadata = dataViewModel.getDocumentMetadata(documentId)
            }
            errorMessage = LocalizedErrorMapper.map(
                null,
                result.exceptionOrNull()?.message,
                language,
            ).takeIf { document == null }
            isLoading = false
        }
    }

    LaunchedEffect(documentId) {
        reloadDocument()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (document?.type == DocumentType.Invoice) t("doc_detail_invoice") else t("doc_detail_quote"),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            errorMessage ?: Localization.getString("error_load_doc_detail", language),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { reloadDocument() }) {
                            Text(t("retry"))
                        }
                    }
                }

                document != null -> document?.let { doc ->
                    val displayDoc = remember(doc) {
                        doc.copy(
                            documentNumber = invoiceOptionPreferences.getDocumentNumberOverride(doc.id)
                                ?: doc.documentNumber,
                            documentTitle = invoiceOptionPreferences.getDocumentTitleOverride(doc.id)
                                ?: doc.documentTitle,
                        )
                    }
                    val documentLanguage = if (doc.documentLanguage.equals("en", true)) "EN" else "AR"
                    val renderModel = remember(displayDoc, businessSettings, selectedTemplateId, documentLanguage, documentMetadata) {
                        val mappedLang = if (documentLanguage == "EN") AppLanguage.EN else AppLanguage.AR
                        TijarioDocumentMapper.fromSaved(
                            document = displayDoc,
                            businessSettings = businessSettings,
                            language = mappedLang,
                            templateId = selectedTemplateId,
                            metadata = documentMetadata,
                            showTijarioBranding = planUsage?.removeTijarioBranding?.not() ?: true,
                        )
                    }
                    fun saveCurrentPdfToDownloads() {
                        scope.launch {
                            try {
                                isBusy = true
                                Toast.makeText(context, Localization.getString("export_download_started", language), Toast.LENGTH_SHORT).show()
                                exportManager.saveToDownloads(renderModel)
                                Toast.makeText(context, Localization.getString("export_saved_downloads", language), Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                android.util.Log.e("TijarioExport", "Error saving document PDF", e)
                                Toast.makeText(context, Localization.getString("export_download_failed", language), Toast.LENGTH_LONG).show()
                            } finally {
                                isBusy = false
                            }
                        }
                    }
                    val downloadPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission(),
                    ) { granted ->
                        if (granted) {
                            saveCurrentPdfToDownloads()
                        } else {
                            Toast.makeText(context, Localization.getString("export_download_failed", language), Toast.LENGTH_LONG).show()
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = if (displayDoc.type == DocumentType.Invoice) t("invoice_num").replace("%s", displayDoc.documentNumber) else t("quote_num").replace("%s", displayDoc.documentNumber),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(t("customer_label").replace("%s", doc.customer?.name ?: t("unknown_customer")), fontSize = 13.sp)
                                Text(t("total_label").replaceFirst("%s", doc.total.toString()).replaceFirst("%s", doc.currency), fontSize = 13.sp)
                                if (doc.type == DocumentType.Invoice) {
                                    Text(t("payment_status_label").replace("%s", app.tijario.domain.PaymentStatusMapper.getStatusText(doc.paymentStatus, language)), fontSize = 13.sp)
                                }
                            }
                        }

                        Text(
                            text = t("document_preview"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )



                        DocumentTemplatePicker(
                            selectedTemplateId = selectedTemplateId,
                            allowedTemplateIds = planUsage?.allowedTemplateIds.orEmpty(),
                            isEntitlementLoaded = planUsage != null,
                            onTemplateSelected = {
                                selectedTemplateId = DocumentTemplateRegistry.requireTemplate(it).id
                                templatePreferences.setDefaultTemplateId(selectedTemplateId)
                            },
                            renderModel = renderModel,
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            DocumentPreviewWebView(
                                model = renderModel,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val compact = maxWidth < 340.dp
                            val editButton: @Composable (Modifier) -> Unit = { modifier ->
                                Button(
                                onClick = { onEditClick(doc.id, doc.type) },
                                modifier = modifier,
                                enabled = !isBusy,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(t("edit"))
                            }
                            }
                            val exportButton: @Composable (Modifier) -> Unit = { modifier ->
                                Button(
                                onClick = { showExportSheet = true },
                                modifier = modifier,
                                enabled = !isBusy,
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isBusy) t("preparing_doc") else t("open_export_options"))
                            }
                            }
                            if (compact) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    editButton(Modifier.fillMaxWidth())
                                    exportButton(Modifier.fillMaxWidth())
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    editButton(Modifier.weight(0.35f))
                                    exportButton(Modifier.weight(0.65f))
                                }
                            }
                        }
                    }

                    if (showExportSheet) {
                        DocumentExportSheet(
                            onDismiss = { showExportSheet = false },
                            onAction = { action ->
                                showExportSheet = false
                                scope.launch {
                                    try {
                                        isBusy = true
                                        when (action) {
                                            DocumentExportAction.ViewPdf -> {
                                                val intent = exportManager.viewIntent(renderModel)
                                                context.startActivity(Intent.createChooser(intent, Localization.getString("export_view_pdf", language)))
                                            }

                                            DocumentExportAction.SaveToDevice -> {
                                                if (DocumentDownloadManager.needsLegacyWritePermission(context)) {
                                                    downloadPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                } else {
                                                    saveCurrentPdfToDownloads()
                                                }
                                            }

                                            DocumentExportAction.Print -> exportManager.printPdf(renderModel)

                                            DocumentExportAction.Email -> {
                                                val intent = exportManager.emailIntent(renderModel)
                                                if (intent == null) {
                                                    Toast.makeText(context, Localization.getString("export_no_email_app", language), Toast.LENGTH_LONG).show()
                                                } else {
                                                    context.startActivity(intent)
                                                }
                                            }

                                            DocumentExportAction.SharePdf -> {
                                                val intent = exportManager.shareIntent(renderModel)
                                                context.startActivity(Intent.createChooser(intent, Localization.getString("export_share_pdf", language)))
                                            }

                                            DocumentExportAction.ShareText -> {
                                                val intent = exportManager.textShareIntent(renderModel)
                                                context.startActivity(Intent.createChooser(intent, Localization.getString("export_share_text", language)))
                                            }
                                        }
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(context, Localization.getString("export_no_app_found", language), Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("TijarioExport", "Error executing export action: ${action.name}", e)
                                        val message = if (action == DocumentExportAction.SaveToDevice) {
                                            Localization.getString("export_download_failed", language)
                                        } else {
                                            Localization.getString("export_error_generic", language)
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    } finally {
                                        isBusy = false
                                    }
                                }
                            },
                        )
                    }

                    if (showFullScreenPreview) {
                        Dialog(
                            onDismissRequest = { showFullScreenPreview = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A))
                                    .padding(12.dp),
                            ) {
                                DocumentPreviewWebView(
                                    model = renderModel,
                                    modifier = Modifier.fillMaxSize(),
                                    interactive = true,
                                )
                                IconButton(
                                    onClick = { showFullScreenPreview = false },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = t("btn_close"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun CompleteDocument.toFormState(lang: AppLanguage): app.tijario.ui.state.DocumentFormState {
    val savedLanguage = if (documentLanguage.equals("en", true)) AppLanguage.EN else AppLanguage.AR
    val isArabic = savedLanguage == AppLanguage.AR
    return app.tijario.ui.state.DocumentFormState(
        customerId = customerId,
        customerName = customer?.name ?: if (isArabic) "عميل غير معروف" else "Unknown customer",
        customerWhatsapp = customer?.whatsappNumber.orEmpty(),
        customerCity = customer?.city,
        items = items.map {
            app.tijario.ui.state.DocumentItemState(
                id = it.id,
                productId = it.productId,
                name = it.name,
                quantity = it.quantity.toString(),
                unitPrice = it.unitPrice.toString(),
            )
        },
        discount = discount.toString(),
        extraFees = extraFees.toString(),
        discountLabel = discountLabel.orEmpty(),
        extraFeesLabel = extraFeesLabel.orEmpty(),
        finalTaxRate = taxRate.toString(),
        finalTaxName = taxName?.takeIf { it.isNotBlank() } ?: (if (isArabic) "الضريبة" else "Tax"),
        paymentStatus = paymentStatus ?: "unpaid",
        amountPaid = amountPaid?.toString().orEmpty(),
        notes = notes.orEmpty(),
        terms = termsText.orEmpty(),
        documentNumber = documentNumber,
        issueDate = issueDate,
        creationDate = issueDate,
        dueTerms = "None",
        dueDate = "",
        poNumber = "",
        documentTitle = documentTitle?.takeIf { it.isNotBlank() } ?: if (type == DocumentType.Invoice) {
            if (isArabic) "فاتورة" else "Invoice"
        } else {
            if (isArabic) "عرض سعر" else "Quote"
        },
        documentLanguage = if (isArabic) "AR" else "EN",
        lang = lang,
    )
}
