package app.tijario.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import app.tijario.config.t
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.export.DocumentExportAction
import app.tijario.features.documents.export.DocumentExportManager
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.preview.DocumentPreviewWebView
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.ui.DocumentExportSheet
import app.tijario.features.documents.ui.DocumentTemplatePicker
import app.tijario.features.documents.ui.DocumentTemplatePreferences
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    dataViewModel: TijarioDataViewModel,
    documentId: String,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val businessSettings = uiState.businessSettings
    val exportManager = remember(context) { DocumentExportManager(context) }
    val templatePreferences = remember(context) { DocumentTemplatePreferences(context) }

    var document by remember { mutableStateOf<CompleteDocument?>(null) }
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
            errorMessage = result.exceptionOrNull()?.message ?: if (document == null) {
                "تعذر تحميل تفاصيل المستند."
            } else {
                null
            }
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
                        if (document?.type == DocumentType.Invoice) "تفاصيل الفاتورة" else "تفاصيل عرض السعر",
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
                            errorMessage ?: "تعذر تحميل تفاصيل المستند.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { reloadDocument() }) {
                            Text("إعادة المحاولة")
                        }
                    }
                }

                document != null -> {
                    val doc = document!!
                    var documentLanguage by remember { mutableStateOf("AR") }
                    var showLanguageDropdown by remember { mutableStateOf(false) }
                    val renderModel = remember(doc, businessSettings, selectedTemplateId, documentLanguage) {
                        val mappedLang = if (documentLanguage == "EN") AppLanguage.EN else AppLanguage.AR
                        TijarioDocumentMapper.fromSaved(
                            document = doc,
                            businessSettings = businessSettings,
                            language = mappedLang,
                            templateId = selectedTemplateId,
                        )
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
                                    text = if (doc.type == DocumentType.Invoice) t("invoice_num").replace("%s", doc.documentNumber) else t("quote_num").replace("%s", doc.documentNumber),
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

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        ) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showLanguageDropdown = true }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text(t("invoice_language"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val currentLangStr = if (documentLanguage == "AR") "العربية" else "English"
                                        Text(currentLangStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                DropdownMenu(
                                    expanded = showLanguageDropdown,
                                    onDismissRequest = { showLanguageDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("العربية") },
                                        onClick = {
                                            documentLanguage = "AR"
                                            showLanguageDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("English") },
                                        onClick = {
                                            documentLanguage = "EN"
                                            showLanguageDropdown = false
                                        }
                                    )
                                }
                            }
                        }

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
                                .weight(1f),
                        ) {
                            DocumentPreviewWebView(
                                model = renderModel,
                                modifier = Modifier.fillMaxSize(),
                            )
                            IconButton(
                                onClick = { showFullScreenPreview = true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                            ) {
                                Icon(Icons.Filled.Fullscreen, contentDescription = "تكبير المعاينة")
                            }
                        }

                        Button(
                            onClick = { showExportSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isBusy,
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBusy) "جاري تجهيز المستند..." else "فتح خيارات التصدير")
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
                                                context.startActivity(Intent.createChooser(intent, "عرض PDF"))
                                            }

                                            DocumentExportAction.SaveToDevice -> {
                                                exportManager.saveToDownloads(renderModel)
                                                Toast.makeText(context, "تم حفظ الملف في التنزيلات", Toast.LENGTH_LONG).show()
                                            }

                                            DocumentExportAction.Print -> exportManager.printPdf(renderModel)

                                            DocumentExportAction.Email -> {
                                                val intent = exportManager.emailIntent(renderModel)
                                                context.startActivity(Intent.createChooser(intent, "إرسال بالبريد"))
                                            }

                                            DocumentExportAction.SharePdf -> {
                                                val intent = exportManager.shareIntent(renderModel)
                                                context.startActivity(Intent.createChooser(intent, "مشاركة PDF"))
                                            }

                                            DocumentExportAction.ShareText -> {
                                                val intent = exportManager.textShareIntent(renderModel)
                                                context.startActivity(Intent.createChooser(intent, "مشاركة النص"))
                                            }
                                        }
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(context, "لا يوجد تطبيق مناسب لتنفيذ هذا الإجراء", Toast.LENGTH_LONG).show()
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "تعذر تنفيذ الإجراء الآن", Toast.LENGTH_LONG).show()
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
                                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun CompleteDocument.toFormState(): app.tijario.ui.state.DocumentFormState =
    app.tijario.ui.state.DocumentFormState(
        customerId = customerId,
        customerName = customer?.name ?: "عميل غير معروف",
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
        documentTitle = if (type == app.tijario.data.model.DocumentType.Invoice) "Online Orders" else "عرض سعر",
    )
