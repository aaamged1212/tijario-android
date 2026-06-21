package app.tijario.features.documents.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentPartyInfo
import app.tijario.features.documents.model.DocumentRenderItem
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentRenderStatus
import app.tijario.features.documents.model.DocumentTotals
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.preview.DocumentPreviewWebView
import java.math.BigDecimal

@Composable
fun DocumentTemplatePicker(
    selectedTemplateId: String,
    onTemplateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    renderModel: DocumentRenderModel? = null,
) {
    val templates = remember { DocumentTemplateRegistry.templates.take(6) }
    val activeModel = renderModel ?: remember { createTemplateSampleModel() }
    val pagerState = rememberPagerState(
        initialPage = templates.indexOfFirst { it.id == selectedTemplateId }.coerceAtLeast(0),
        pageCount = { templates.size },
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "القوالب",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 14.dp,
        ) { page ->
            val template = templates[page]
            val selected = template.id == selectedTemplateId
            val templateModel = remember(page, activeModel) { activeModel.copy(templateId = template.id) }

            Card(
                onClick = { onTemplateSelected(template.id) },
                modifier = Modifier
                    .width(280.dp)
                    .height(410.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = if (selected) {
                    BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                },
                elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = template.name.removePrefix("Tijario "),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(348.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        DocumentPreviewWebView(
                            model = templateModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium),
                        )
                    }
                }
            }
        }
    }
}

private fun createTemplateSampleModel(): DocumentRenderModel =
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
                unitPrice = BigDecimal("150.00"),
                lineTotal = BigDecimal("300.00"),
            ),
        ),
        totals = DocumentTotals(
            subtotal = BigDecimal("300.00"),
            discount = BigDecimal.ZERO,
            extraFees = BigDecimal.ZERO,
            total = BigDecimal("300.00"),
            amountPaid = BigDecimal("100.00"),
            amountRemaining = BigDecimal("200.00"),
            currency = "SAR",
        ),
        invoiceNote = "Sample invoice note",
        termsAndConditions = "Sample terms",
    )
