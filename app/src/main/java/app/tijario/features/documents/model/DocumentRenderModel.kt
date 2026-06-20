package app.tijario.features.documents.model

import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import java.math.BigDecimal

data class DocumentPartyInfo(
    val name: String,
    val contactNumber: String,
    val country: String? = null,
    val city: String? = null,
    val logoUrl: String? = null,
)

data class DocumentRenderItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal,
)

data class DocumentTotals(
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val extraFees: BigDecimal,
    val total: BigDecimal,
    val currency: String,
)

data class DocumentRenderStatus(
    val documentStatus: String? = null,
    val paymentStatus: String? = null,
)

data class DocumentTemplateDefinition(
    val id: String,
    val name: String,
    val version: Int,
    val assetDir: String,
    val layoutFamily: String,
    val accentColor: String,
    val description: String,
    val visual: DocumentTemplateVisual = DocumentTemplateVisual(),
)

data class DocumentTemplateVisual(
    val styleFamily: Int = 1,
    val headStyle: Int = 0,
    val deepColor: String = "#0B5F59",
    val topTextColor: String = "#2C2D2F",
    val titleTextColor: String? = null,
    val logoBackgroundColor: String? = null,
    val logoTextColor: String = "#FFFFFF",
    val dividerColor: String = "#D9E2EC",
    val tableShadeColor: String = "#F1F5F9",
    val tableHeaderTextColor: String = "#FFFFFF",
    val itemTopOuterLine: Boolean = false,
    val itemBottomBlock: Boolean = true,
    val itemBottomLine: Boolean = true,
    val itemTextBold: Boolean = false,
    val pageAccentColor: String? = null,
)

data class DocumentRenderModel(
    val documentId: String? = null,
    val documentType: DocumentType,
    val documentNumber: String,
    val issueDate: String,
    val updatedAt: String? = null,
    val status: DocumentRenderStatus = DocumentRenderStatus(),
    val business: DocumentPartyInfo,
    val customer: DocumentPartyInfo,
    val items: List<DocumentRenderItem>,
    val totals: DocumentTotals,
    val invoiceNote: String? = null,
    val documentNote: String? = null,
    val termsAndConditions: String? = null,
    val language: AppLanguage = AppLanguage.AR,
    val templateId: String = "tijario-classic",
    val templateVersion: Int = 1,
    val formattingVersion: Int = 1,
) {
    val isRtl: Boolean get() = language == AppLanguage.AR
}
