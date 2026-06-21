package app.tijario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.preview.DocumentPreviewWebView
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.ui.state.DocumentFormState

@Composable
fun ModernDocumentPreview(
    documentType: DocumentType,
    form: DocumentFormState,
    businessSettings: BusinessSettings?,
    customerCity: String?,
    templateId: String = DocumentTemplateRegistry.defaultTemplateId,
    modifier: Modifier = Modifier,
) {
    val model = remember(documentType, form, businessSettings, customerCity, templateId) {
        val mappedLang = if (form.documentLanguage == "EN") AppLanguage.EN else AppLanguage.AR
        TijarioDocumentMapper.fromDraft(
            documentType = documentType,
            form = form,
            businessSettings = businessSettings,
            customerCity = customerCity,
            language = mappedLang,
            templateId = templateId,
        )
    }
    var showFullScreenPreview by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        DocumentPreviewWebView(model = model, modifier = Modifier.fillMaxSize())
        IconButton(
            onClick = { showFullScreenPreview = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        ) {
            Icon(Icons.Filled.Fullscreen, contentDescription = "تكبير المعاينة")
        }
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
                    model = model,
                    modifier = Modifier.fillMaxSize(),
                    interactive = true,
                )
                IconButton(
                    onClick = { showFullScreenPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }
        }
    }
}
