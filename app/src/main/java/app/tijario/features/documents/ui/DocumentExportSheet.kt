package app.tijario.features.documents.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tijario.config.AppLanguage
import app.tijario.config.LocalLanguage
import app.tijario.config.t
import app.tijario.features.documents.export.DocumentExportAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentExportSheet(
    onDismiss: () -> Unit,
    onAction: (DocumentExportAction) -> Unit,
) {
    val isArabic = LocalLanguage.current == AppLanguage.AR

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = if (isArabic) "خيارات التصدير" else "Export Options",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            ExportRow(t("export_view_pdf"), DocumentExportAction.ViewPdf, onAction) { Icon(Icons.Filled.Visibility, contentDescription = null) }
            ExportRow(if (isArabic) "حفظ في الجهاز" else "Save to Device", DocumentExportAction.SaveToDevice, onAction) { Icon(Icons.Filled.Download, contentDescription = null) }
            ExportRow(if (isArabic) "طباعة" else "Print", DocumentExportAction.Print, onAction) { Icon(Icons.Filled.Print, contentDescription = null) }
            ExportRow(t("export_send_email"), DocumentExportAction.Email, onAction) { Icon(Icons.Filled.Email, contentDescription = null) }
            ExportRow(t("export_share_pdf"), DocumentExportAction.SharePdf, onAction) { Icon(Icons.Filled.Share, contentDescription = null) }
            ExportRow(t("export_share_text"), DocumentExportAction.ShareText, onAction) { Icon(Icons.Filled.Share, contentDescription = null) }
        }
    }
}

@Composable
private fun ExportRow(
    title: String,
    action: DocumentExportAction,
    onAction: (DocumentExportAction) -> Unit,
    icon: @Composable () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = icon,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(action) },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    )
}
