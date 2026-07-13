from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
path = ROOT / "app/src/main/java/app/tijario/features/documents/pdf/LocalPdfGenerator.kt"
source = path.read_text(encoding="utf-8")

source = source.replace("import android.graphics.pdf.PdfDocument\n", "")
source = source.replace("import kotlin.math.min\n", "")

old = '''    private suspend fun writeWebViewToPdf(webView: WebView, outputFile: File): Unit = suspendCancellableCoroutine { cont ->
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()
        val adapter = webView.createPrintDocumentAdapter("Document")
        val attrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build()
        try {
            val pfd = ParcelFileDescriptor.open(outputFile, ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE)
            android.print.PrintHelper.runWrite(
                adapter = adapter,
                attributes = attrs,
                pfd = pfd,
                onComplete = {
                    try {
                        pfd.close()
                    } catch (_: Exception) {}
                    if (cont.isActive) cont.resume(Unit)
                },
                onFailed = { err ->
                    try {
                        pfd.close()
                    } catch (_: Exception) {}
                    if (cont.isActive) cont.resumeWithException(IllegalStateException(err ?: "Failed to write PDF"))
                }
            )
        } catch (e: Exception) {
            if (cont.isActive) cont.resumeWithException(e)
        }
    }'''

new = '''    private fun writeWebViewToPdf(webView: WebView, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()

        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build()
        val document = PrintedPdfDocument(context, attributes)

        try {
            val contentRect = document.pageContentRect
            val cssContentHeight = maxOf(webView.contentHeight, A4_HEIGHT_CSS_PX)
            webView.measure(
                View.MeasureSpec.makeMeasureSpec(A4_WIDTH_CSS_PX, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(cssContentHeight, View.MeasureSpec.EXACTLY),
            )
            webView.layout(0, 0, A4_WIDTH_CSS_PX, cssContentHeight)

            val renderScale = contentRect.width().toFloat() / A4_WIDTH_CSS_PX.toFloat()
            val pageHeightCss = (contentRect.height().toFloat() / renderScale)
                .roundToInt()
                .coerceAtLeast(1)
            val pageCount = ((cssContentHeight + pageHeightCss - 1) / pageHeightCss)
                .coerceAtLeast(1)

            repeat(pageCount) { pageIndex ->
                val page = document.startPage(pageIndex)
                val canvas = page.canvas
                val saveCount = canvas.save()
                canvas.translate(contentRect.left.toFloat(), contentRect.top.toFloat())
                canvas.scale(renderScale, renderScale)
                canvas.clipRect(0f, 0f, A4_WIDTH_CSS_PX.toFloat(), pageHeightCss.toFloat())
                canvas.translate(0f, -(pageIndex * pageHeightCss).toFloat())
                webView.draw(canvas)
                canvas.restoreToCount(saveCount)
                document.finishPage(page)
            }

            FileOutputStream(outputFile, false).use(document::writeTo)
        } finally {
            document.close()
        }
    }'''

if new not in source:
    if source.count(old) != 1:
        raise RuntimeError(f"Expected one legacy PrintHelper block, found {source.count(old)}")
    source = source.replace(old, new, 1)

path.write_text(source, encoding="utf-8")
helper = ROOT / "app/src/main/java/android/print/PrintHelper.kt"
if helper.exists():
    helper.unlink()

print("official PrintedPdfDocument rendering applied")
