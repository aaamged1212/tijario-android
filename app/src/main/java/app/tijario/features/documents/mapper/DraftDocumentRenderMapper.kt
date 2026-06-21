package app.tijario.features.documents.mapper

import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.DocumentType
import app.tijario.domain.DocumentCalculator
import app.tijario.domain.PaymentAmountCalculator
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
    ): DocumentRenderModel {
        val calculation = DocumentCalculator.calculate(
            form.items.map { DocumentCalculator.ItemInput(it.quantity, it.unitPrice) },
            form.discount,
            form.extraFees,
        )
        val items = form.items.map { item ->
            val quantity = Validation.parsePositiveInt(item.quantity) ?: 0
            val unitPrice = BigDecimal.valueOf(Validation.parseNonNegativeMoney(item.unitPrice) ?: 0.0)
            DocumentRenderItem(
                id = item.id,
                name = item.name.ifBlank { if (language == AppLanguage.AR) "بند غير مسمى" else "Unnamed item" },
                description = null,
                quantity = quantity,
                unitPrice = unitPrice,
                lineTotal = unitPrice.multiply(BigDecimal(quantity)),
            )
        }
        val parsedFormTax = BigDecimal.valueOf(Validation.parseNonNegativeMoney(form.finalTaxRate) ?: 0.0)
        val taxAmount = calculation.total.multiply(parsedFormTax.divide(BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP))
        val totalWithTax = calculation.total.add(taxAmount)

        val paymentAmounts = PaymentAmountCalculator.calculate(
            paymentStatus = if (documentType == DocumentType.Invoice) form.paymentStatus else null,
            total = totalWithTax,
            amountPaid = Validation.parseNonNegativeMoney(form.amountPaid)?.let(BigDecimal::valueOf),
        )
        return DocumentRenderModel(
            documentType = documentType,
            documentNumber = if (documentType == DocumentType.Invoice) "INV-DRAFT" else "QT-DRAFT",
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
                total = totalWithTax,
                amountPaid = paymentAmounts.paid,
                amountRemaining = paymentAmounts.remaining,
                currency = businessSettings?.currency ?: "SAR",
                finalTaxName = form.finalTaxName,
                finalTaxRate = parsedFormTax,
                finalTaxAmount = taxAmount
            ),
            invoiceNote = businessSettings?.invoiceNote,
            documentNote = form.notes.ifBlank { null },
            termsAndConditions = form.terms.ifBlank { businessSettings?.termsText },
            language = language,
            templateId = templateId,
            templateVersion = DocumentTemplateRegistry.requireTemplate(templateId).version,
        )
    }
}
