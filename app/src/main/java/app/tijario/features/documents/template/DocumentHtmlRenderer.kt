package app.tijario.features.documents.template

import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentTemplateDefinition
import java.math.BigDecimal

class DocumentHtmlRenderer(
    private val loader: DocumentTemplateLoader,
) {
    fun render(model: DocumentRenderModel): String {
        val template = DocumentTemplateRegistry.requireTemplate(model.templateId)
        val base = loader.loadText("documents/base/document.html")
        val css = buildString {
            append(loader.loadText("documents/base/common.css"))
            append('\n')
            append(loader.loadText("documents/base/print.css"))
            append('\n')
            append(loader.loadText("${template.assetDir}/template.css"))
        }
        val body = renderBody(model, template)
        return base
            .replace("{{LANG}}", if (model.language == AppLanguage.AR) "ar" else "en")
            .replace("{{DIR}}", if (model.isRtl) "rtl" else "ltr")
            .replace("{{TITLE}}", title(model))
            .replace("{{CSS}}", css)
            .replace("{{PAGE_CLASS}}", "document-page template-${template.id}")
            .replace("{{BODY}}", body)
    }

    private fun renderBody(model: DocumentRenderModel, template: DocumentTemplateDefinition): String {
        val labels = labels(model.language)
        val paymentStatus = if (model.documentType == DocumentType.Invoice) {
            DocumentFormatting.status(model.status.paymentStatus, model.language)
        } else {
            null
        }
        return buildString {
            append("<main class=\"document-shell\" data-template=\"${template.id}\">")
            append("<section class=\"header\">")
            append("<div class=\"brand\">")
            append("<div class=\"logo\">${HtmlEscaper.escape(DocumentFormatting.initials(model.business.name))}</div>")
            append("<div><h1 class=\"business-name\">${HtmlEscaper.escape(model.business.name)}</h1>")
            append("<div class=\"business-meta\">${labels.number}: ${HtmlEscaper.escape(model.business.contactNumber)}</div>")
            append(locationLine(model.business.city, model.business.country))
            append("</div></div>")
            append("<div class=\"title-block\"><h2 class=\"document-title\">${title(model)}</h2>")
            append("<div class=\"meta-grid\">")
            append(metaCard(labels.documentNumber, model.documentNumber))
            append(metaCard(labels.issueDate, model.issueDate))
            DocumentFormatting.status(model.status.documentStatus, model.language)?.let {
                append(metaCard(labels.status, it))
            }
            paymentStatus?.let {
                val cssClass = "payment-${model.status.paymentStatus.orEmpty().lowercase()}"
                append("<div class=\"badge $cssClass\">${HtmlEscaper.escape(it)}</div>")
            }
            append("</div></div></section>")
            append("<section class=\"parties\">")
            append(partyCard(labels.businessInfo, model.business, labels))
            append(partyCard(labels.customerInfo, model.customer, labels))
            append("</section>")
            append(itemsTable(model, labels))
            append("<section class=\"summary\">")
            append("<div class=\"note-stack\">")
            note(labels.documentNote, model.documentNote)?.let { append(it) }
            note(labels.invoiceNote, model.invoiceNote)?.let { append(it) }
            note(labels.terms, model.termsAndConditions)?.let { append(it) }
            append("</div>")
            append(totals(model, labels))
            append("</section>")
            append("<footer class=\"footer\">${labels.footer}</footer>")
            append("</main>")
        }
    }

    private fun title(model: DocumentRenderModel): String =
        when (model.documentType) {
            DocumentType.Invoice -> if (model.language == AppLanguage.AR) "فاتورة" else "Invoice"
            DocumentType.Quote -> if (model.language == AppLanguage.AR) "عرض سعر" else "Quotation"
        }

    private fun labels(language: AppLanguage): Labels =
        if (language == AppLanguage.AR) {
            Labels(
                number = "الرقم",
                businessInfo = "بيانات النشاط",
                customerInfo = "بيانات العميل",
                documentNumber = "رقم المستند",
                issueDate = "تاريخ الإصدار",
                status = "الحالة",
                item = "البيان",
                quantity = "الكمية",
                unitPrice = "سعر الوحدة",
                lineTotal = "الإجمالي",
                subtotal = "المجموع الفرعي",
                discount = "الخصم",
                extraFees = "الرسوم الإضافية",
                total = "الإجمالي النهائي",
                invoiceNote = "ملاحظة الفاتورة",
                documentNote = "ملاحظة",
                terms = "الشروط والأحكام",
                footer = "تم إنشاء هذا المستند عبر تجاريو",
            )
        } else {
            Labels(
                number = "Number",
                businessInfo = "Business details",
                customerInfo = "Customer details",
                documentNumber = "Document number",
                issueDate = "Issue date",
                status = "Status",
                item = "Item",
                quantity = "Qty",
                unitPrice = "Unit price",
                lineTotal = "Total",
                subtotal = "Subtotal",
                discount = "Discount",
                extraFees = "Extra fees",
                total = "Final total",
                invoiceNote = "Invoice note",
                documentNote = "Note",
                terms = "Terms and conditions",
                footer = "Created with Tijario",
            )
        }

    private fun metaCard(label: String, value: String): String =
        "<div class=\"meta-card\"><span class=\"label\">${HtmlEscaper.escape(label)}</span><span class=\"value\">${HtmlEscaper.escape(value)}</span></div>"

    private fun partyCard(title: String, party: app.tijario.features.documents.model.DocumentPartyInfo, labels: Labels): String =
        buildString {
            append("<article class=\"party-card\"><h3 class=\"section-title\">${HtmlEscaper.escape(title)}</h3>")
            append("<div class=\"value\">${HtmlEscaper.escape(party.name)}</div>")
            append("<div class=\"party-lines\">${labels.number}: ${HtmlEscaper.escape(party.contactNumber)}</div>")
            append(locationLine(party.city, party.country))
            append("</article>")
        }

    private fun locationLine(city: String?, country: String?): String {
        val value = listOfNotNull(city?.takeIf { it.isNotBlank() }, country?.takeIf { it.isNotBlank() }).joinToString(" - ")
        return if (value.isBlank()) "" else "<div class=\"party-lines\">${HtmlEscaper.escape(value)}</div>"
    }

    private fun itemsTable(model: DocumentRenderModel, labels: Labels): String =
        buildString {
            append("<table><thead><tr>")
            append("<th>${labels.item}</th><th class=\"num\">${labels.quantity}</th><th class=\"num\">${labels.unitPrice}</th><th class=\"num\">${labels.lineTotal}</th>")
            append("</tr></thead><tbody>")
            model.items.forEach { item ->
                append("<tr><td><div class=\"item-name\">${HtmlEscaper.escape(item.name)}</div>")
                if (!item.description.isNullOrBlank()) {
                    append("<div class=\"item-desc\">${HtmlEscaper.escape(item.description)}</div>")
                }
                append("</td><td class=\"num\">${DocumentFormatting.quantity(item.quantity, model.language)}</td>")
                append("<td class=\"num\">${DocumentFormatting.money(item.unitPrice, model.totals.currency, model.language)}</td>")
                append("<td class=\"num\">${DocumentFormatting.money(item.lineTotal, model.totals.currency, model.language)}</td></tr>")
            }
            append("</tbody></table>")
        }

    private fun totals(model: DocumentRenderModel, labels: Labels): String =
        buildString {
            append("<aside class=\"totals\">")
            totalsRow(labels.subtotal, model.totals.subtotal, model).also { append(it) }
            if (model.totals.discount > BigDecimal.ZERO) append(totalsRow(labels.discount, model.totals.discount, model))
            if (model.totals.extraFees > BigDecimal.ZERO) append(totalsRow(labels.extraFees, model.totals.extraFees, model))
            append("<div class=\"totals-row final\"><span>${labels.total}</span><strong>${DocumentFormatting.money(model.totals.total, model.totals.currency, model.language)}</strong></div>")
            append("</aside>")
        }

    private fun totalsRow(label: String, value: BigDecimal, model: DocumentRenderModel): String =
        "<div class=\"totals-row\"><span>${HtmlEscaper.escape(label)}</span><strong>${DocumentFormatting.money(value, model.totals.currency, model.language)}</strong></div>"

    private fun note(label: String, value: String?): String? =
        value?.takeIf { it.isNotBlank() }?.let {
            "<section class=\"notes\"><h3 class=\"section-title\">${HtmlEscaper.escape(label)}</h3><div class=\"note-body\">${HtmlEscaper.escape(it)}</div></section>"
        }

    private data class Labels(
        val number: String,
        val businessInfo: String,
        val customerInfo: String,
        val documentNumber: String,
        val issueDate: String,
        val status: String,
        val item: String,
        val quantity: String,
        val unitPrice: String,
        val lineTotal: String,
        val subtotal: String,
        val discount: String,
        val extraFees: String,
        val total: String,
        val invoiceNote: String,
        val documentNote: String,
        val terms: String,
        val footer: String,
    )
}
