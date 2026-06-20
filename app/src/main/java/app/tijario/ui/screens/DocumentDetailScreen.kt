package app.tijario.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.t
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import app.tijario.ui.components.ModernDocumentPreview
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    dataViewModel: TijarioDataViewModel,
    documentId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val businessSettings = uiState.businessSettings

    var document by remember { mutableStateOf<CompleteDocument?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPdfProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        isLoading = true
        errorMessage = null
        val result = dataViewModel.fetchCompleteDocument(documentId)
        if (result.isSuccess) {
            document = result.getOrNull()
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "تعذر تحميل تفاصيل المستند."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (document?.type == DocumentType.Invoice) "تفاصيل الفاتورة" else "تفاصيل عرض السعر",
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = dataViewModel.fetchCompleteDocument(documentId)
                            if (result.isSuccess) {
                                document = result.getOrNull()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "تعذر تحميل تفاصيل المستند."
                            }
                            isLoading = false
                        }
                    }) {
                        Text("إعادة المحاولة")
                    }
                }
            } else if (document != null) {
                val doc = document!!
                val formState = remember(doc) { doc.toFormState() }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Document Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernDocumentPreview(
                            documentType = doc.type,
                            form = formState,
                            businessSettings = businessSettings,
                            customerCity = doc.customer?.city,
                            productDescription = null
                        )
                    }

                    // Action buttons
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("خيارات المشاركة والتحميل", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                isPdfProcessing = true
                                                val bytes = dataViewModel.fetchDocumentPdf(doc.id)
                                                val dir = File(context.cacheDir, "documents")
                                                if (!dir.exists()) dir.mkdirs()
                                                val file = File(dir, "${doc.documentNumber}.pdf")
                                                file.writeBytes(bytes)

                                                val uri = FileProvider.getUriForFile(context, "app.tijario.fileprovider", file)
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    android.widget.Toast.makeText(context, "لم يتم العثور على تطبيق لعرض ملفات PDF", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "فشل تحميل ملف PDF: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                            } finally {
                                                isPdfProcessing = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = !isPdfProcessing
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("عرض PDF", fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                isPdfProcessing = true
                                                val bytes = dataViewModel.fetchDocumentPdf(doc.id)
                                                val dir = File(context.cacheDir, "documents")
                                                if (!dir.exists()) dir.mkdirs()
                                                val file = File(dir, "${doc.documentNumber}.pdf")
                                                file.writeBytes(bytes)

                                                val uri = FileProvider.getUriForFile(context, "app.tijario.fileprovider", file)
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "مشاركة ملف PDF"))
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "فشل تحميل ملف PDF: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                            } finally {
                                                isPdfProcessing = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    enabled = !isPdfProcessing
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مشاركة PDF", fontSize = 13.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    val typeLabel = if (doc.type == DocumentType.Invoice) "فاتورة" else "عرض سعر"
                                    val builder = StringBuilder()
                                    builder.append("*$typeLabel رقم ${doc.documentNumber}*\n\n")
                                    builder.append("العميل: ${doc.customer?.name ?: "غير محدد"}\n")
                                    builder.append("التاريخ: ${doc.issueDate}\n\n")
                                    builder.append("البنود:\n")
                                    doc.items.forEachIndexed { index, item ->
                                        builder.append("${index + 1}. ${item.name} (${item.quantity} x ${item.unitPrice} ${doc.currency})\n")
                                    }
                                    builder.append("\nالمجموع الفرعي: ${doc.subtotal} ${doc.currency}")
                                    builder.append("\nالرسوم الإضافية: ${doc.extraFees} ${doc.currency}")
                                    builder.append("\nالخصم: ${doc.discount} ${doc.currency}")
                                    builder.append("\n*الإجمالي: ${doc.total} ${doc.currency}*")

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, builder.toString())
                                    }
                                    context.startActivity(Intent.createChooser(intent, "مشاركة تفاصيل المستند"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("مشاركة النص للتواصل", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun CompleteDocument.toFormState(): app.tijario.ui.state.DocumentFormState {
    return app.tijario.ui.state.DocumentFormState(
        customerId = this.customerId,
        customerName = this.customer?.name ?: "عميل غير معروف",
        customerWhatsapp = this.customer?.whatsappNumber.orEmpty(),
        items = this.items.map {
            app.tijario.ui.state.DocumentItemState(
                id = it.id,
                productId = it.productId,
                name = it.name,
                quantity = it.quantity.toString(),
                unitPrice = it.unitPrice.toString()
            )
        },
        discount = this.discount.toString(),
        extraFees = this.extraFees.toString(),
        notes = this.notes.orEmpty(),
        terms = this.termsText.orEmpty()
    )
}
