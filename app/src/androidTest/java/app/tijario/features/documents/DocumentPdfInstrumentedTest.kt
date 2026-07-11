package app.tijario.features.documents

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentPartyInfo
import app.tijario.features.documents.model.DocumentRenderItem
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.DocumentRenderStatus
import app.tijario.features.documents.model.DocumentTotals
import app.tijario.features.documents.pdf.LocalPdfGenerator
import app.tijario.features.documents.template.DocumentTemplateRegistry
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentPdfInstrumentedTest {
    @Test
    fun localPdfGeneratorCreatesSinglePageVisiblePdf() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.cacheDir, "instrumented-document-render.pdf")
        if (output.exists()) output.delete()

        LocalPdfGenerator(context).renderPdf(model(), output)

        assertTrue(output.exists())
        assertTrue("PDF should contain rendered page data", output.length() > 10_000L)
        assertTrue(output.inputStream().use { input ->
            val signature = ByteArray(4)
            input.read(signature) == 4 && signature.decodeToString() == "%PDF"
        })

        ParcelFileDescriptor.open(output, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertEquals("PDF should fit the document into one A4 page", 1, renderer.pageCount)
                renderer.openPage(0).use { page ->
                    val bitmap = Bitmap.createBitmap(600, 849, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val bounds = renderedInkBounds(bitmap)
                    assertTrue("Rendered content should not be clipped to the far right", bounds.left < bitmap.width * 0.55f)
                    assertTrue("Rendered content should remain visible inside the page", bounds.right > bitmap.width * 0.45f)
                    assertTrue("Rendered content should not be clipped to the bottom", bounds.bottom < bitmap.height * 0.96f)
                }
            }
        }
    }

    private fun renderedInkBounds(bitmap: Bitmap): Rect {
        val bounds = Rect(bitmap.width, bitmap.height, 0, 0)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (!isNearWhite(bitmap.getPixel(x, y))) {
                    if (x < bounds.left) bounds.left = x
                    if (x > bounds.right) bounds.right = x
                    if (y < bounds.top) bounds.top = y
                    if (y > bounds.bottom) bounds.bottom = y
                }
            }
        }
        return bounds
    }

    private fun isNearWhite(color: Int): Boolean =
        Color.red(color) > 245 && Color.green(color) > 245 && Color.blue(color) > 245

    private fun model(): DocumentRenderModel {
        val template = "tijario-blue-band"
        return DocumentRenderModel(
            documentId = "instrumented-doc",
            documentType = DocumentType.Invoice,
            documentNumber = "INV-INSTRUMENTED",
            issueDate = "2026-06-20",
            updatedAt = "instrumented-test",
            status = DocumentRenderStatus(documentStatus = "sent", paymentStatus = "paid"),
            business = DocumentPartyInfo(
                name = "متجر تجاريو",
                contactNumber = "+966500000000",
                country = "السعودية",
                city = "الرياض",
            ),
            customer = DocumentPartyInfo(
                name = "عميل تجريبي",
                contactNumber = "+966511111111",
                city = "جدة",
            ),
            items = listOf(
                DocumentRenderItem(
                    id = "line-1",
                    name = "تصميم فاتورة",
                    description = "وصف تجريبي يظهر داخل PDF",
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
                currency = "SAR",
            ),
            documentNote = "ملاحظة تجريبية",
            termsAndConditions = "الشروط والأحكام التجريبية",
            language = AppLanguage.AR,
            templateId = template,
            templateVersion = DocumentTemplateRegistry.requireTemplate(template).version,
        )
    }
}
