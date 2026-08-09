package app.tijario.features.documents.mapper

import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.DocumentType
import app.tijario.domain.DocumentCalculator
import app.tijario.domain.Validation
import app.tijario.features.documents.model.DocumentPartyInfo
import app.tijario.features.documents.model.DocumentRenderItem
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentRenderStatus
import app.tijario.features.documents.model.DocumentTotals
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.ui.state.DocumentFormState
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DraftDocumentRenderMapper {
    fun map(
        documentType: DocumentType,
        form: DocumentFormState,
        businessSettings: BusinessSettings?,
        customerCity: String?,
        language: AppLanguage = AppLanguage.AR,
        templateId: String = DocumentTemplateRegistry.defaultTemplateId,
        showTijarioBranding: Boolean = true,
    ): DocumentRenderModel {
        val calculation = DocumentCalculator.calculate(
            form.items.map { DocumentCalculator.ItemInput(it.quantity, it.unitPrice) },
            form.discount,
            form.extraFees,
            form.finalTaxRate,
            form.amountPaid,
            discountType = form.discountType,
            shippingStr = form.shippingAmount,
        )
        val items = form.items.map { item ->
            val quantity = Validation.parsePositiveInt(item.quantity) ?: 0
            val unitPrice = Validation.parseNonNegativeMoneyDecimal(item.unitPrice) ?: BigDecimal.ZERO
            DocumentRenderItem(
                id = item.id,
                name = item.name.ifBlank { if (language == AppLanguage.AR) "بند غير مسمى" else "Unnamed item" },
                description = null,
                quantity = quantity,
                unitPrice = unitPrice,
                lineTotal = unitPrice.multiply(BigDecimal(quantity)),
            )
        }
        return DocumentRenderModel(
            documentType = documentType,
            documentNumber = form.documentNumber.takeIf { it.isNotBlank() }
                ?: if (documentType == DocumentType.Invoice) "INV-..." else "Q-...",
            issueDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
            updatedAt = "draft",
            status = DocumentRenderStatus(
                documentStatus = null,
                paymentStatus = if (documentType == DocumentType.Invoice) form.paymentStatus else null,
            ),
            business = DocumentPartyInfo(
                name = businessSettings?.businessName?.ifBlank { null } ?: if (language == AppLanguage.AR) "اسم النشاط" else "Business name",
                contactNumber = businessSettings?.whatsappNumber.orEmpty(),
                country = businessSettings?.country,
                city = businessSettings?.city,
                address = businessSettings?.address,
                email = businessSettings?.email,
                websiteUrl = businessSettings?.websiteUrl,
                logoUrl = businessSettings?.logoUrl,
            ),
            customer = DocumentPartyInfo(
                name = form.customerName.ifBlank { if (language == AppLanguage.AR) "عميل غير معروف" else "Unknown customer" },
                contactNumber = form.customerWhatsapp,
                city = customerCity,
            ),
            items = items,
            totals = DocumentTotals(
                subtotal = calculation.subtotal,
                discount = calculation.discount,
                extraFees = calculation.extraFees,
                total = calculation.total,
                amountPaid = Validation.parseNonNegativeMoneyDecimal(form.amountPaid) ?: BigDecimal.ZERO,
                amountRemaining = calculation.amountRemaining,
                currency = form.currency,
                finalTaxName = form.finalTaxName,
                finalTaxRate = Validation.parseNonNegativeMoneyDecimal(form.finalTaxRate) ?: BigDecimal.ZERO,
                finalTaxAmount = calculation.taxAmount,
                shipping = calculation.shipping
            ),
            invoiceNote = businessSettings?.invoiceNote,
            documentNote = form.notes.ifBlank { null },
            termsAndConditions = form.terms.ifBlank { businessSettings?.termsText },
            language = language,
            templateId = templateId,
            templateVersion = DocumentTemplateRegistry.requireTemplate(templateId).version,
            signatureData = form.signatureData.takeIf { it.isNotBlank() },
            paymentMethod = form.paymentMethod.takeIf { it.isNotBlank() },
            documentTitle = form.documentTitle.takeIf { it.isNotBlank() },
            discountLabel = form.discountLabel.takeIf { it.isNotBlank() }
                ?: if (form.discountType.equals("percent", ignoreCase = true)) {
                    if (language == AppLanguage.AR) "خصم %" else "Discount %"
                } else null,
            extraFeesLabel = form.extraFeesLabel.takeIf { it.isNotBlank() },
            shippingLabel = form.shippingLabel.takeIf { it.isNotBlank() },
            showTijarioBranding = showTijarioBranding,
        )
    }
}
