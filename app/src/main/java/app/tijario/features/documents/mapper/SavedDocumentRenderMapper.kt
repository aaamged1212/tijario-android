package app.tijario.features.documents.mapper

import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import app.tijario.domain.DocumentCalculator
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
        language: AppLanguage =
            if (document.documentLanguage.equals("en", ignoreCase = true)) AppLanguage.EN else AppLanguage.AR,
        templateId: String = DocumentTemplateRegistry.defaultTemplateId,
        metadata: app.tijario.data.local.LocalDocumentMetadataEntity? = null,
        showTijarioBranding: Boolean = true,
    ): DocumentRenderModel {
        require(document.id.isNotBlank()) { "Missing document ID" }
        require(document.documentNumber.isNotBlank()) { "Missing document number" }
        require(document.issueDate.isNotBlank()) { "Missing issue date" }

        val items = document.items.mapNotNull { item ->
            if (item.id.isBlank() || item.name.isBlank()) return@mapNotNull null
            val quantity = item.quantity
            val unitPrice = BigDecimal.valueOf(item.unitPrice)
            DocumentRenderItem(
                id = item.id,
                name = item.name,
                description = item.description,
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
        val fallbackCalculation = DocumentCalculator.calculate(
            document.items.map {
                DocumentCalculator.ItemInput(
                    quantity = it.quantity.toString(),
                    unitPrice = it.unitPrice.toString(),
                )
            },
            discountStr = document.discount.toString(),
            extraFeesStr = document.extraFees.toString(),
            taxRateStr = document.taxRate.toString(),
            amountPaidStr = document.amountPaid?.toString() ?: "0",
        )
        val resolvedTaxRate = when {
            document.taxRate > 0.0 -> BigDecimal.valueOf(document.taxRate)
            metadata?.taxRate != null -> BigDecimal.valueOf(metadata.taxRate)
            else -> BigDecimal.ZERO
        }
        val resolvedTaxAmount = when {
            document.taxAmount > 0.0 -> BigDecimal.valueOf(document.taxAmount)
            document.taxRate > 0.0 -> fallbackCalculation.taxAmount
            metadata?.taxRate != null -> fallbackCalculation.taxAmount
            else -> BigDecimal.ZERO
        }
        val resolvedTaxName = document.taxName?.takeIf { it.isNotBlank() }
            ?: metadata?.taxName?.takeIf { it.isNotBlank() }
            ?: if (language == AppLanguage.AR) "الضريبة" else "Tax"
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
                address = businessSettings?.address,
                email = businessSettings?.email,
                websiteUrl = businessSettings?.websiteUrl,
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
                currency = metadata?.currency ?: document.currency.ifBlank { null } ?: businessSettings?.currency ?: "SAR",
                finalTaxName = resolvedTaxName,
                finalTaxRate = resolvedTaxRate,
                finalTaxAmount = resolvedTaxAmount
            ),
            invoiceNote = businessSettings?.invoiceNote,
            documentNote = document.notes,
            termsAndConditions = document.termsText ?: businessSettings?.termsText,
            language = language,
            templateId = templateId,
            templateVersion = DocumentTemplateRegistry.requireTemplate(templateId).version,
            signatureData = metadata?.signatureData,
            paymentMethod = metadata?.paymentMethod,
            documentTitle = document.documentTitle?.takeIf { it.isNotBlank() } ?: when (document.type) {
                DocumentType.Invoice -> if (language == AppLanguage.AR) "فاتورة" else "Invoice"
                DocumentType.Quote -> if (language == AppLanguage.AR) "عرض سعر" else "Quote"
            },
            discountLabel = document.discountLabel?.takeIf { it.isNotBlank() },
            extraFeesLabel = document.extraFeesLabel?.takeIf { it.isNotBlank() },
            showTijarioBranding = showTijarioBranding,
        )
    }
}
