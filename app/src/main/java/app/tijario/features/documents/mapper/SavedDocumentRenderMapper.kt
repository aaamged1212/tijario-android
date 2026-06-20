package app.tijario.features.documents.mapper

import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import app.tijario.domain.PaymentAmountCalculator
import app.tijario.features.documents.model.DocumentPartyInfo
import app.tijario.features.documents.model.DocumentRenderItem
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentRenderStatus
import app.tijario.features.documents.model.DocumentTotals
import app.tijario.features.documents.template.DocumentTemplateRegistry
import java.math.BigDecimal

object SavedDocumentRenderMapper {
    fun map(
        document: CompleteDocument,
        businessSettings: BusinessSettings?,
        language: AppLanguage = AppLanguage.AR,
        templateId: String = DocumentTemplateRegistry.defaultTemplateId,
    ): DocumentRenderModel {
        require(document.id.isNotBlank()) { "Missing document ID" }
        require(document.documentNumber.isNotBlank()) { "Missing document number" }
        require(document.issueDate.isNotBlank()) { "Missing issue date" }
        require(document.items.isNotEmpty()) { "Missing document items" }

        val items = document.items.map {
            val quantity = it.quantity
            val unitPrice = BigDecimal.valueOf(it.unitPrice)
            DocumentRenderItem(
                id = it.id,
                name = it.name,
                description = it.description,
                quantity = quantity,
                unitPrice = unitPrice,
                lineTotal = unitPrice.multiply(BigDecimal(quantity)),
            )
        }
        val paymentAmounts = PaymentAmountCalculator.calculate(
            paymentStatus = if (document.type == DocumentType.Invoice) document.paymentStatus else null,
            total = BigDecimal.valueOf(document.total),
            amountPaid = document.amountPaid?.let(BigDecimal::valueOf),
        )
        return DocumentRenderModel(
            documentId = document.id,
            documentType = document.type,
            documentNumber = document.documentNumber,
            issueDate = document.issueDate,
            updatedAt = document.issueDate,
            status = DocumentRenderStatus(
                documentStatus = null,
                paymentStatus = if (document.type == DocumentType.Invoice) document.paymentStatus else null,
            ),
            business = DocumentPartyInfo(
                name = businessSettings?.businessName?.ifBlank { null } ?: if (language == AppLanguage.AR) "اسم النشاط" else "Business name",
                contactNumber = businessSettings?.whatsappNumber.orEmpty(),
                country = businessSettings?.country,
                city = businessSettings?.city,
                logoUrl = businessSettings?.logoUrl,
            ),
            customer = DocumentPartyInfo(
                name = document.customer?.name ?: if (language == AppLanguage.AR) "عميل غير معروف" else "Unknown customer",
                contactNumber = document.customer?.whatsappNumber.orEmpty(),
                city = document.customer?.city,
            ),
            items = items,
            totals = DocumentTotals(
                subtotal = BigDecimal.valueOf(document.subtotal),
                discount = BigDecimal.valueOf(document.discount),
                extraFees = BigDecimal.valueOf(document.extraFees),
                total = BigDecimal.valueOf(document.total),
                amountPaid = paymentAmounts.paid,
                amountRemaining = paymentAmounts.remaining,
                currency = document.currency,
            ),
            invoiceNote = businessSettings?.invoiceNote,
            documentNote = document.notes,
            termsAndConditions = document.termsText ?: businessSettings?.termsText,
            language = language,
            templateId = templateId,
            templateVersion = DocumentTemplateRegistry.requireTemplate(templateId).version,
        )
    }
}
