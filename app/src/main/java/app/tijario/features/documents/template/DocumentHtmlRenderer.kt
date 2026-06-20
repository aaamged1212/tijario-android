package app.tijario.features.documents.template

import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentPartyInfo
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentTemplateDefinition
import java.math.BigDecimal

class DocumentHtmlRenderer(
    private val loader: DocumentTemplateLoader,
) {
    fun render(model: DocumentRenderModel, target: DocumentRenderTarget = DocumentRenderTarget.Pdf): String {
        val template = DocumentTemplateRegistry.requireTemplate(model.templateId)
        val base = loader.loadText("documents/base/document.html")
        val css = buildString {
            append(loader.loadText("documents/base/common.css"))
            append('\n')
            append(loader.loadText("${template.assetDir}/template.css"))
            append('\n')
            append(runtimeTemplateCss(template))
            append('\n')
            append(
                loader.loadText(
                    when (target) {
                        DocumentRenderTarget.Preview -> "documents/base/preview.css"
                        DocumentRenderTarget.Pdf -> "documents/base/print.css"
                    }
                )
            )
        }
        val body = renderBody(model, template)
        return base
            .replace("{{LANG}}", if (model.language == AppLanguage.AR) "ar" else "en")
            .replace("{{DIR}}", if (model.isRtl) "rtl" else "ltr")
            .replace("{{TITLE}}", title(model))
            .replace("{{CSS}}", css)
            .replace("{{PAGE_CLASS}}", pageClass(template, target))
            .replace("{{PAGE_STYLE}}", templateStyleVars(template))
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
            append("<main class=\"document-shell\" data-template=\"${template.id}\" style=\"${templateStyleVars(template)}\">")
            append("<section class=\"header\">")
            append("<div class=\"brand\">")
            append(logoMarkup(model.business))
            append("<div><h1 class=\"business-name\">${HtmlEscaper.escape(model.business.name)}</h1>")
            append("<div class=\"business-meta\">${labels.number}: ${HtmlEscaper.escape(model.business.contactNumber)}</div>")
            append(locationLine(model.business.city, model.business.country))
            append("</div></div>")
            append("<div class=\"title-block\"><h2 class=\"document-title\">${title(model)}</h2>")
            append("<div class=\"meta-grid\">")
            append(metaCard(labels.documentNumber, model.documentNumber))
            append(metaCard(labels.issueDate, model.issueDate))
            paymentStatus?.let {
                val cssClass = "payment-${model.status.paymentStatus.orEmpty().lowercase()}"
                append("<div class=\"badge $cssClass\">${HtmlEscaper.escape(it)}</div>")
            }
            append("</div></div></section>")
            append("<section class=\"parties customer-only\">")
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

    private fun pageClass(template: DocumentTemplateDefinition, target: DocumentRenderTarget): String =
        buildString {
            append("document-page template-${template.id}")
            append(" im-style-${template.visual.styleFamily}")
            append(" im-head-${template.visual.headStyle}")
            append(' ')
            append(target.cssClass)
        }

    private fun templateStyleVars(template: DocumentTemplateDefinition): String {
        val visual = template.visual
        val logoBackground = visual.logoBackgroundColor ?: template.accentColor
        val pageAccent = visual.pageAccentColor ?: "transparent"
        return listOf(
            "--accent:${template.accentColor}",
            "--deep:${visual.deepColor}",
            "--top-text:${visual.topTextColor}",
            "--title-text:${visual.titleTextColor ?: template.accentColor}",
            "--logo-bg:$logoBackground",
            "--logo-text:${visual.logoTextColor}",
            "--line:${visual.dividerColor}",
            "--table-shade:${visual.tableShadeColor}",
            "--table-head-text:${visual.tableHeaderTextColor}",
            "--page-accent:$pageAccent",
        ).joinToString(";")
    }

    private fun runtimeTemplateCss(template: DocumentTemplateDefinition): String {
        val idClass = ".template-${template.id}"
        val visual = template.visual
        return buildString {
            if (visual.itemTextBold) {
                append("$idClass .item-name{font-weight:950;}\n")
            }
            if (visual.itemTopOuterLine) {
                append("$idClass table{border-top:2px solid var(--accent);}\n")
            }
            if (!visual.itemBottomLine) {
                append("$idClass th,$idClass td{border-bottom:none;}\n")
            }
            if (!visual.itemBottomBlock) {
                append("$idClass th{background:#FFFFFF;color:var(--table-head-text);border-top:2px solid var(--accent);border-bottom:2px solid var(--accent);}\n")
                append("$idClass tbody tr{border-bottom:1px solid var(--line);}\n")
            }
            if (visual.styleFamily == 2) {
                append("$idClass .header{background:var(--deep);color:var(--top-text);border-radius:12px;padding:16px;}\n")
                append("$idClass .header .business-meta,$idClass .header .party-lines{color:rgba(255,255,255,.78);}\n")
                append("$idClass .document-title{color:var(--title-text);}\n")
                append("$idClass .meta-card{background:rgba(255,255,255,.1);border-color:rgba(255,255,255,.22);}\n")
                append("$idClass .meta-card .label,$idClass .meta-card .value{color:#FFFFFF;}\n")
            }
            if (visual.styleFamily == 3) {
                append("$idClass .header{background:linear-gradient(135deg,var(--deep),var(--accent));color:var(--top-text);border-radius:0;padding:18px;}\n")
                append("$idClass .document-title{color:var(--title-text);font-size:34px;}\n")
                append("$idClass .header .business-meta,$idClass .header .party-lines{color:rgba(255,255,255,.82);}\n")
                append("$idClass .meta-card{background:rgba(255,255,255,.12);border-color:rgba(255,255,255,.2);}\n")
                append("$idClass .meta-card .label,$idClass .meta-card .value{color:#FFFFFF;}\n")
            }
            if (visual.styleFamily == 5) {
                append("$idClass .logo{border-radius:50%;}\n")
                append("$idClass .document-title{color:var(--title-text);}\n")
                append("$idClass .meta-card,$idClass .party-card,$idClass .totals,$idClass .notes{border-radius:0;background:#FFFFFF;}\n")
            }
            if (visual.styleFamily == 6) {
                append("$idClass{background:linear-gradient(180deg,var(--page-accent) 0 36mm,#FFFFFF 36mm 100%);}\n")
                append("$idClass .header{background:var(--deep);color:var(--top-text);border-radius:14px;padding:18px;}\n")
                append("$idClass .document-title{color:var(--title-text);}\n")
                append("$idClass .header .business-meta,$idClass .header .party-lines{color:rgba(255,255,255,.82);}\n")
                append("$idClass .meta-card{background:rgba(255,255,255,.12);border-color:rgba(255,255,255,.22);}\n")
                append("$idClass .meta-card .label,$idClass .meta-card .value{color:#FFFFFF;}\n")
            }
            if (visual.styleFamily == 7) {
                append("$idClass{background:linear-gradient(90deg,var(--accent) 0 10mm,#FFFFFF 10mm 100%);}\n")
                append("$idClass .document-shell{padding-inline-start:5mm;}\n")
                append("$idClass .header{border-bottom:3px solid var(--accent);padding-bottom:12px;}\n")
                append("$idClass .document-title{background:var(--accent);color:var(--title-text);display:inline-block;padding:8px 18px;border-radius:0;}\n")
            }
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
                customerInfo = "بيانات العميل",
                city = "المدينة",
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
                amountPaid = "المبلغ المدفوع",
                amountRemaining = "المبلغ المتبقي",
                invoiceNote = "ملاحظة الفاتورة",
                documentNote = "ملاحظة",
                terms = "الشروط والأحكام",
                footer = "تم إنشاء هذا المستند عبر تجاريو",
            )
        } else {
            Labels(
                number = "Number",
                customerInfo = "Customer details",
                city = "City",
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
                amountPaid = "Amount paid",
                amountRemaining = "Amount remaining",
                invoiceNote = "Invoice note",
                documentNote = "Note",
                terms = "Terms and conditions",
                footer = "Created with Tijario",
            )
        }

    private fun metaCard(label: String, value: String): String =
        "<div class=\"meta-card\"><span class=\"label\">${HtmlEscaper.escape(label)}</span><span class=\"value\">${HtmlEscaper.escape(value)}</span></div>"

    private fun logoMarkup(party: DocumentPartyInfo): String {
        val logoUrl = safeLogoUrl(party.logoUrl)
        return if (logoUrl == null) {
            "<div class=\"logo logo-initials\">${HtmlEscaper.escape(DocumentFormatting.initials(party.name))}</div>"
        } else {
            "<div class=\"logo logo-image\"><img src=\"${HtmlEscaper.escape(logoUrl)}\" alt=\"\" loading=\"eager\" decoding=\"sync\"></div>"
        }
    }

    private fun safeLogoUrl(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        return when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> null
        }
    }

    private fun partyCard(title: String, party: DocumentPartyInfo, labels: Labels): String =
        buildString {
            append("<article class=\"party-card customer-card\"><h3 class=\"section-title\">${HtmlEscaper.escape(title)}</h3>")
            append("<div class=\"party-primary\">${HtmlEscaper.escape(party.name)}</div>")
            append("<div class=\"party-details\">")
            append(partyDetail(labels.number, party.contactNumber))
            party.city?.takeIf { it.isNotBlank() }?.let {
                append(partyDetail(labels.city, it))
            }
            append("</div>")
            append("</article>")
        }

    private fun partyDetail(label: String, value: String): String =
        "<div class=\"party-detail\"><span>${HtmlEscaper.escape(label)}</span><strong>${HtmlEscaper.escape(value)}</strong></div>"

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
            append(totalsRow(labels.subtotal, model.totals.subtotal, model))
            if (model.totals.discount > BigDecimal.ZERO) append(totalsRow(labels.discount, model.totals.discount, model))
            if (model.totals.extraFees > BigDecimal.ZERO) append(totalsRow(labels.extraFees, model.totals.extraFees, model))
            append("<div class=\"totals-row final\"><span>${labels.total}</span><strong>${DocumentFormatting.money(model.totals.total, model.totals.currency, model.language)}</strong></div>")
            if (model.documentType == DocumentType.Invoice) {
                if (model.totals.amountPaid > BigDecimal.ZERO) {
                    append(totalsRow(labels.amountPaid, model.totals.amountPaid, model))
                }
                if (model.totals.amountRemaining > BigDecimal.ZERO) {
                    append(totalsRow(labels.amountRemaining, model.totals.amountRemaining, model))
                }
            }
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
        val customerInfo: String,
        val city: String,
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
        val amountPaid: String,
        val amountRemaining: String,
        val invoiceNote: String,
        val documentNote: String,
        val terms: String,
        val footer: String,
    )
}

enum class DocumentRenderTarget(val cssClass: String) {
    Preview("preview-mode"),
    Pdf("pdf-mode"),
}
