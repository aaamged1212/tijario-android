package app.tijario.features.documents

import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.mapper.DraftDocumentRenderMapper
import app.tijario.features.documents.mapper.SavedDocumentRenderMapper
import app.tijario.features.documents.pdf.PdfCacheKeyFactory
import app.tijario.features.documents.pdf.PdfFileNameSanitizer
import app.tijario.features.documents.template.DocumentHtmlRenderer
import app.tijario.features.documents.template.DocumentRenderTarget
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.template.DocumentTemplateValidator
import app.tijario.features.documents.template.FileSystemDocumentTemplateLoader
import app.tijario.features.documents.template.HtmlEscaper
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentEngineTests {
    private val assetsRoot = File("src/main/assets")
    private val renderer = DocumentHtmlRenderer(FileSystemDocumentTemplateLoader(assetsRoot))

    @Test
    fun registryContainsOriginalAndInvoiceMakerStyleTemplates() {
        val templates = DocumentTemplateRegistry.templates
        assertTrue(templates.size > 10)
        assertEquals(templates.size, templates.map { it.id }.toSet().size)
        assertEquals(templates.size, templates.map { it.layoutFamily }.toSet().size)
        assertTrue(templates.any { it.visual.styleFamily == 2 })
        assertTrue(templates.any { it.visual.styleFamily == 5 })
        assertTrue(templates.any { it.visual.styleFamily == 7 })
        assertTrue(DocumentTemplateValidator.validateRegistry().isEmpty())
    }

    @Test
    fun templateAssetsExist() {
        DocumentTemplateRegistry.templates.forEach { template ->
            assertTrue(File(assetsRoot, "${template.assetDir}/template.json").isFile)
            assertTrue(File(assetsRoot, "${template.assetDir}/template.css").isFile)
        }
        assertTrue(File(assetsRoot, "documents/base/document.html").isFile)
        assertTrue(File(assetsRoot, "documents/base/common.css").isFile)
        assertTrue(File(assetsRoot, "documents/base/preview.css").isFile)
        assertTrue(File(assetsRoot, "documents/base/print.css").isFile)
    }

    @Test
    fun htmlEscaperEscapesDangerousCharacters() {
        assertEquals("&lt;tag attr=&quot;1&quot;&gt;&#39;&amp;&lt;/tag&gt;", HtmlEscaper.escape("<tag attr=\"1\">'&</tag>"))
    }

    @Test
    fun draftMappingCalculatesLocalPreviewValues() {
        val model = DraftDocumentRenderMapper.map(
            documentType = DocumentType.Invoice,
            form = DocumentFixtures.draftForm(),
            businessSettings = DocumentFixtures.business,
            customerCity = DocumentFixtures.customer.city,
        )
        assertEquals("INV-DRAFT", model.documentNumber)
        assertEquals("SAR", model.totals.currency)
        assertEquals(2, model.items.size)
        assertEquals("360.50", model.totals.total.toPlainString())
    }

    @Test
    fun savedMappingUsesAuthoritativeValues() {
        val model = SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved(),
            businessSettings = DocumentFixtures.business,
        )
        assertEquals("INV-1130", model.documentNumber)
        assertEquals("360.5", model.totals.total.stripTrailingZeros().toPlainString())
        assertEquals("paid", model.status.paymentStatus)
    }

    @Test(expected = IllegalArgumentException::class)
    fun savedMappingFailsWhenCriticalItemsAreMissing() {
        SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved().copy(items = emptyList()),
            businessSettings = DocumentFixtures.business,
        )
    }

    @Test
    fun generatedArabicDocumentUsesNeutralNumberLabelsAndNoWhatsAppWording() {
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business,
                language = AppLanguage.AR,
            )
        )
        assertTrue(html.contains("الرقم"))
        assertFalse(html.contains("واتساب"))
        assertFalse(html.contains("رقم واتساب"))
        assertFalse(html.contains("WhatsApp"))
    }

    @Test
    fun generatedEnglishDocumentUsesNumberLabel() {
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business,
                language = AppLanguage.EN,
            )
        )
        assertTrue(html.contains("Number"))
        assertFalse(html.contains("WhatsApp"))
    }

    @Test
    fun everyTemplateShowsFullCustomerDetailsWithoutBusinessDetailsCard() {
        val base = SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved(),
            businessSettings = DocumentFixtures.business,
            language = AppLanguage.AR,
        )

        DocumentTemplateRegistry.templates.forEach { template ->
            val html = renderer.render(base.copy(templateId = template.id, templateVersion = template.version))

            assertFalse("Business details card should not be duplicated for ${template.id}", html.contains("<h3 class=\"section-title\">بيانات النشاط</h3>"))
            assertTrue("Customer-only layout missing for ${template.id}", html.contains("parties customer-only"))
            assertTrue("Customer title missing for ${template.id}", html.contains("بيانات العميل"))
            assertTrue("Customer name missing for ${template.id}", html.contains(DocumentFixtures.customer.name))
            assertTrue("Customer number missing for ${template.id}", html.contains(DocumentFixtures.customer.whatsappNumber))
            assertTrue("Customer city missing for ${template.id}", html.contains(DocumentFixtures.customer.city.orEmpty()))
        }
    }

    @Test
    fun generatedDocumentUsesBusinessLogoWhenPublicUrlExists() {
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business.copy(logoUrl = "https://example.com/logo.png"),
                language = AppLanguage.EN,
            )
        )

        assertTrue(html.contains("logo-image"))
        assertTrue(html.contains("https://example.com/logo.png"))
    }

    @Test
    fun generatedDocumentRejectsUnsafeLogoUrls() {
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business.copy(logoUrl = "javascript:alert(1)"),
                language = AppLanguage.EN,
            )
        )

        assertFalse(html.contains("javascript:alert"))
        assertTrue(html.contains("logo-initials"))
    }

    @Test
    fun previewAndPdfUseDifferentRenderTargets() {
        val model = SavedDocumentRenderMapper.map(DocumentFixtures.saved(), DocumentFixtures.business)
        val previewHtml = renderer.render(model, DocumentRenderTarget.Preview)
        val pdfHtml = renderer.render(model, DocumentRenderTarget.Pdf)

        assertTrue(previewHtml.contains("preview-mode"))
        assertTrue(pdfHtml.contains("pdf-mode"))
        assertTrue(previewHtml.contains("height: 297mm"))
        assertTrue(pdfHtml.contains("@page"))
    }

    @Test
    fun quotationOmitsPaymentStatus() {
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(type = DocumentType.Quote, paymentStatus = "paid"),
                businessSettings = DocumentFixtures.business,
            )
        )
        assertTrue(html.contains("عرض سعر"))
        assertFalse(html.contains(">مدفوع<"))
        assertFalse(html.contains("<div class=\"badge payment-paid\">"))
    }

    @Test
    fun zeroDiscountAndExtraFeesAreOmitted() {
        val doc = DocumentFixtures.saved().copy(discount = 0.0, extraFees = 0.0)
        val html = renderer.render(SavedDocumentRenderMapper.map(doc, DocumentFixtures.business))
        assertFalse(html.contains("الخصم</span>"))
        assertFalse(html.contains("الرسوم الإضافية</span>"))
    }

    @Test
    fun cacheKeyChangesWithTemplateLocaleRevisionAndRenderEngine() {
        val base = SavedDocumentRenderMapper.map(DocumentFixtures.saved(), DocumentFixtures.business)
        val templateChanged = base.copy(templateId = "tijario-modern")
        val localeChanged = base.copy(language = AppLanguage.EN)
        val revisionChanged = base.copy(updatedAt = "2026-06-21")
        val key = PdfCacheKeyFactory.key(base)

        assertTrue(key.startsWith("pdfv3-"))
        assertNotEquals(key, PdfCacheKeyFactory.key(templateChanged))
        assertNotEquals(key, PdfCacheKeyFactory.key(localeChanged))
        assertNotEquals(key, PdfCacheKeyFactory.key(revisionChanged))
    }

    @Test
    fun filenameSanitizerPreventsTraversalAndReservedCharacters() {
        val sanitized = PdfFileNameSanitizer.sanitize("../INV/1130:*?<>|")
        assertFalse(sanitized.contains(".."))
        assertFalse(sanitized.contains("/"))
        assertFalse(sanitized.contains("\\"))
        assertTrue(sanitized.contains("INV"))
    }
}
