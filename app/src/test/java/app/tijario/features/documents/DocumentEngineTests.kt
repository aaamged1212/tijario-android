package app.tijario.features.documents

import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.mapper.DraftDocumentRenderMapper
import app.tijario.features.documents.mapper.SavedDocumentRenderMapper
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.model.resolveDocumentLogoForRender
import app.tijario.features.documents.pdf.PdfCacheKeyFactory
import app.tijario.features.documents.pdf.PdfFileNameSanitizer
import app.tijario.features.documents.template.DocumentHtmlRenderer
import app.tijario.features.documents.template.DocumentRenderTarget
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.template.DocumentTemplateValidator
import app.tijario.features.documents.template.FileSystemDocumentTemplateLoader
import app.tijario.features.documents.template.HtmlEscaper
import app.tijario.domain.DocumentCalculator
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentEngineTests {
    private val assetsRoot = listOf(
        File("src/main/assets"),
        File("app/src/main/assets"),
    ).first { it.isDirectory }
    private val renderer = DocumentHtmlRenderer(FileSystemDocumentTemplateLoader(assetsRoot))

    @Test
    fun registryContainsOriginalAndInvoiceMakerStyleTemplates() {
        val templates = DocumentTemplateRegistry.templates
        assertTrue(templates.size >= 20)
        assertEquals(templates.size, templates.map { it.id }.toSet().size)
        assertTrue(templates.map { it.family }.toSet().size >= 5)
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
    fun commonLogoCssKeepsCircularClipping() {
        val css = File(assetsRoot, "documents/base/common.css").readText()
        assertTrue(css.contains(".logo {"))
        assertTrue(css.contains("border-radius: 50%;"))
        assertTrue(css.contains(".logo-image {"))
        assertTrue(css.contains("overflow: hidden;"))
        assertTrue(css.contains("object-fit: cover;"))
    }

    @Test
    fun htmlEscaperEscapesDangerousCharacters() {
        assertEquals("&lt;tag attr=&quot;1&quot;&gt;&#39;&amp;&lt;/tag&gt;", HtmlEscaper.escape("<tag attr=\"1\">'&</tag>"))
    }

    @Test
    fun generatedDocumentEscapesAllUserControlledTextFields() {
        val payload = "<script>alert(1)</script><img src=\"https://example.invalid/test\"><style>body{display:none}</style><>&\"'"
        val base = SavedDocumentRenderMapper.map(DocumentFixtures.saved(), DocumentFixtures.business)
        val model = base.copy(
            documentTitle = payload,
            business = base.business.copy(name = payload, address = payload, email = payload, websiteUrl = payload),
            customer = base.customer.copy(name = payload, contactNumber = payload, city = payload),
            items = base.items.map { it.copy(name = payload, description = payload) },
            documentNote = payload,
            termsAndConditions = payload,
            paymentMethod = payload,
            discountLabel = payload,
            extraFeesLabel = payload,
            signatureData = payload,
        )

        val html = renderer.render(model)

        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"))
        assertFalse(html.contains("<script>alert(1)</script>"))
        assertFalse(html.contains("<style>body{display:none}</style>"))
        assertFalse(html.contains("<img src=\"https://example.invalid/test\">"))
    }

    @Test
    fun draftMappingCalculatesLocalPreviewValues() {
        val model = DraftDocumentRenderMapper.map(
            documentType = DocumentType.Invoice,
            form = DocumentFixtures.draftForm(),
            businessSettings = DocumentFixtures.business,
            customerCity = DocumentFixtures.customer.city,
        )
        assertEquals("INV-...", model.documentNumber)
        assertEquals("SAR", model.totals.currency)
        assertEquals(2, model.items.size)
        assertEquals("360.5", model.totals.total.stripTrailingZeros().toPlainString())
    }

    @Test
    fun calculatorAppliesPercentageDiscountBeforeTaxAndShippingAfterTax() {
        val result = DocumentCalculator.calculate(
            items = listOf(DocumentCalculator.ItemInput(quantity = "1", unitPrice = "100")),
            discountStr = "10",
            extraFeesStr = "20",
            taxRateStr = "10",
            discountType = "percent",
            shippingStr = "15",
        )

        assertEquals("100", result.subtotal.stripTrailingZeros().toPlainString())
        assertEquals("10", result.discount.stripTrailingZeros().toPlainString())
        assertEquals("110", result.taxBase.stripTrailingZeros().toPlainString())
        assertEquals("11", result.taxAmount.stripTrailingZeros().toPlainString())
        assertEquals("15", result.shipping.stripTrailingZeros().toPlainString())
        assertEquals("136", result.total.stripTrailingZeros().toPlainString())
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

    @Test
    fun missingCachedRemoteLogoFallsBackLocallyWithoutDroppingDocumentContent() {
        assertEquals(
            null,
            resolveDocumentLogoForRender("https://example.com/logo.png", null),
        )
        assertEquals(
            "data:image/png;base64,abc",
            resolveDocumentLogoForRender(
                "https://example.com/logo.png",
                "data:image/png;base64,abc",
            ),
        )

        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business.copy(logoUrl = null),
            ),
        )
        assertTrue(html.contains("logo-initials"))
        DocumentFixtures.saved().items.forEach { item -> assertTrue(html.contains(item.name)) }
    }

    @Test
    fun savedDocumentKeepsItsItemsAndTemplateForPreviewAndPdf() {
        val document = DocumentFixtures.saved().copy(templateId = "tijario-modern")
        val model = TijarioDocumentMapper.fromSaved(
            document = document,
            businessSettings = DocumentFixtures.business,
        )

        val previewHtml = renderer.render(model, DocumentRenderTarget.Preview)
        val pdfHtml = renderer.render(model, DocumentRenderTarget.Pdf)

        assertEquals("tijario-modern", model.templateId)
        assertEquals(document.items.size, model.items.size)
        document.items.forEach { item ->
            assertTrue(previewHtml.contains(item.name))
            assertTrue(pdfHtml.contains(item.name))
        }
    }

    @Test
    fun savedMappingUsesPersistedTaxValues() {
        val document = DocumentFixtures.saved().copy(
            taxName = "VAT",
            taxRate = 10.0,
            taxAmount = 36.05,
            total = 396.55,
        )
        val model = SavedDocumentRenderMapper.map(
            document = document,
            businessSettings = DocumentFixtures.business,
        )

        assertEquals("VAT", model.totals.finalTaxName)
        assertEquals("10", model.totals.finalTaxRate.stripTrailingZeros().toPlainString())
        assertEquals("36.05", model.totals.finalTaxAmount.stripTrailingZeros().toPlainString())
    }

    @Test
    fun savedMappingHandlesMissingItemsWithoutCrashing() {
        val model = SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved().copy(items = emptyList()),
            businessSettings = DocumentFixtures.business,
        )

        assertTrue(model.items.isEmpty())
        assertEquals("فاتورة", model.documentTitle)
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
    fun generatedDocumentIncludesBusinessContactFieldsAndOpposingHeaderLayout() {
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved().copy(documentLanguage = "en"),
                businessSettings = DocumentFixtures.business,
                language = AppLanguage.EN,
            )
        )

        assertTrue(html.contains("Address: ${DocumentFixtures.business.address}"))
        assertTrue(html.contains("Email: ${DocumentFixtures.business.email}"))
        assertTrue(html.contains("Website: ${DocumentFixtures.business.websiteUrl}"))
        assertTrue(html.contains("grid-template-columns:minmax(0,1fr) minmax(160px,.75fr)"))
        assertTrue(html.contains(".title-block{justify-items:end;text-align:end;}"))
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
    fun brandingFlagControlsFooterVisibility() {
        val branded = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business,
                language = AppLanguage.EN,
                showTijarioBranding = true,
            )
        )
        val unbranded = renderer.render(
            SavedDocumentRenderMapper.map(
                document = DocumentFixtures.saved(),
                businessSettings = DocumentFixtures.business,
                language = AppLanguage.EN,
                showTijarioBranding = false,
            )
        )

        assertTrue(branded.contains("Created with Tijario"))
        assertFalse(unbranded.contains("Created with Tijario"))
        assertFalse(unbranded.contains("تم إنشاء هذا المستند عبر تجاريو"))
        assertTrue(unbranded.contains("<main"))
    }

    @Test
    fun brandingFlagDefaultsToVisibleForDraftAndCanBeDisabledForSavedDocuments() {
        val draftModel = DraftDocumentRenderMapper.map(
            documentType = DocumentType.Invoice,
            form = DocumentFixtures.draftForm(),
            businessSettings = DocumentFixtures.business,
            customerCity = DocumentFixtures.customer.city,
        )
        val savedModel = SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved(),
            businessSettings = DocumentFixtures.business,
            showTijarioBranding = false,
        )

        assertTrue(draftModel.showTijarioBranding)
        assertFalse(savedModel.showTijarioBranding)
    }

    @Test
    fun savedPdfRendersTaxRowOnlyWhenTaxExists() {
        val taxDocument = DocumentFixtures.saved().copy(
            taxName = "VAT",
            taxRate = 10.0,
            taxAmount = 36.05,
            total = 396.55,
        )
        val html = renderer.render(
            SavedDocumentRenderMapper.map(
                document = taxDocument,
                businessSettings = DocumentFixtures.business,
                language = AppLanguage.EN,
            )
        )

        assertTrue(html.contains("VAT (10.0%)"))
        assertTrue(html.contains("36.05"))
    }

    @Test
    fun generatedDocumentRendersShippingAndNegativeDiscount() {
        val model = SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved().copy(discount = 25.0),
            businessSettings = DocumentFixtures.business,
            language = AppLanguage.EN,
            metadata = app.tijario.data.local.LocalDocumentMetadataEntity(
                documentId = DocumentFixtures.saved().id,
                currency = "SAR",
                signatureData = null,
                paymentMethod = null,
                shippingAmount = 18.0,
                shippingLabel = "Delivery",
            ),
        )
        val html = renderer.render(model)

        assertTrue(html.contains("-SAR 25.00") || html.contains("-25.00"))
        assertTrue(html.contains("Delivery"))
        assertTrue(html.contains("18.00"))
    }

    @Test
    fun savedMappingUsesCustomSummaryLabelsWhenProvided() {
        val model = SavedDocumentRenderMapper.map(
            document = DocumentFixtures.saved().copy(
                discountLabel = "Promo",
                extraFeesLabel = "Service fee",
            ),
            businessSettings = DocumentFixtures.business,
        )

        assertEquals("Promo", model.discountLabel)
        assertEquals("Service fee", model.extraFeesLabel)
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
    fun localPdfUsesTheWebViewPrintPipelineAfterLayoutAndInvalidatesRasterCache() {
        val source = File(
            "src/main/java/app/tijario/features/documents/pdf/LocalPdfGenerator.kt",
        ).readText()
        val printHelper = File("src/main/java/android/print/PrintHelper.kt").readText()

        assertTrue(source.contains("layoutWebView(webView, A4_HEIGHT_CSS_PX)"))
        assertTrue(source.contains("awaitPageLoad(webView, html)"))
        assertTrue(source.contains("layoutWebView(webView, cssContentHeight)"))
        assertTrue(source.contains("awaitVisualState(webView)"))
        assertTrue(source.contains("webView.createPrintDocumentAdapter(\"Document\")"))
        assertTrue(source.contains("android.print.PrintHelper.runWrite"))
        assertFalse(source.contains("PrintedPdfDocument"))
        assertFalse(source.contains("webView.draw(canvas)"))
        assertTrue(printHelper.contains("adapter.onLayout("))
        assertTrue(printHelper.contains("adapter.onWrite("))
    }

    @Test
    fun localPdfNetworkLogoFetchIsBoundedAndPreviewStaysLocalOnly() {
        val pdfSource = File(
            "src/main/java/app/tijario/features/documents/pdf/LocalPdfGenerator.kt",
        ).readText()
        val previewSource = File(
            "src/main/java/app/tijario/features/documents/preview/DocumentPreviewWebView.kt",
        ).readText()
        val storeLogoSource = File(
            "src/main/java/app/tijario/ui/components/StoreLogoSupport.kt",
        ).readText()

        assertTrue(pdfSource.contains("startsWith(\"https://\""))
        assertTrue(pdfSource.contains("MAX_LOGO_BYTES"))
        assertTrue(pdfSource.contains("LOGO_CONNECT_TIMEOUT_MS"))
        assertTrue(pdfSource.contains("LOGO_READ_TIMEOUT_MS"))
        assertTrue(pdfSource.contains("contentType?.lowercase()?.startsWith(\"image/\")"))
        assertFalse(pdfSource.contains("openStream()"))
        assertFalse(previewSource.contains("openStream()"))
        assertTrue(storeLogoSource.contains("MAX_LOGO_DOWNLOAD_BYTES"))
        assertFalse(storeLogoSource.contains("openStream()"))
    }

    @Test
    fun previewUsesACssPixelA4SurfaceInsteadOfDensityExpandedDpDimensions() {
        val source = File(
            "src/main/java/app/tijario/features/documents/preview/DocumentPreviewWebView.kt",
        ).readText()

        assertTrue(source.contains("A4_WIDTH_CSS_PX.toDp()"))
        assertTrue(source.contains("A4_HEIGHT_CSS_PX.toDp()"))
        assertTrue(source.contains("graphicsLayer"))
        assertFalse(source.contains("A4_LAYOUT_WIDTH = 794.dp"))
    }

    @Test
    fun savedDocumentPreviewKeepsOneWebViewAndDoesNotReloadUnchangedHtml() {
        val previewSource = File(
            "src/main/java/app/tijario/features/documents/preview/DocumentPreviewWebView.kt",
        ).readText()
        val detailSource = File(
            "src/main/java/app/tijario/ui/screens/DocumentDetailScreen.kt",
        ).readText()

        assertTrue(previewSource.contains("if (webView.tag != html)"))
        assertTrue(previewSource.contains("webView.tag = html"))
        assertFalse(detailSource.contains("DocumentTemplatePicker("))
        assertTrue(detailSource.contains("DocumentTemplateRegistry.normalizeId(savedTemplateId)"))
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

        assertTrue(key.startsWith("pdfv5-"))
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
