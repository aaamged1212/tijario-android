package app.tijario.features.documents.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.template.AndroidAssetDocumentTemplateLoader
import app.tijario.features.documents.template.DocumentHtmlRenderer
import app.tijario.features.documents.template.DocumentRenderTarget
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private const val A4_WIDTH_CSS_PX = 794
private const val A4_HEIGHT_CSS_PX = 1123

class LocalPdfGenerator(
    private val context: Context,
    private val renderer: DocumentHtmlRenderer = DocumentHtmlRenderer(AndroidAssetDocumentTemplateLoader(context)),
    private val cacheManager: PdfCacheManager = PdfCacheManager(context),
) {
    suspend fun ensurePdf(model: DocumentRenderModel): PdfGenerationResult {
        val file = cacheManager.resolve(model)
        if (cacheManager.isValid(file)) {
            return PdfGenerationResult(file, reusedCache = true)
        }
        return PdfGenerationResult(renderPdf(model, file), reusedCache = false)
    }

    suspend fun renderPdf(model: DocumentRenderModel, outputFile: File): File =
        withContext(Dispatchers.Main) {
            val html = renderer.render(model, DocumentRenderTarget.Pdf)
            val webView = WebView(context)
            try {
                configure(webView)
                awaitPageLoad(webView, html)
                prepareA4Viewport(webView)
                awaitVisualState(webView)
                expandToRenderedContent(webView)
                awaitVisualState(webView)
                writeWebViewToPdf(webView, outputFile)
                require(cacheManager.isValid(outputFile)) { "Generated PDF is invalid" }
                cacheManager.cleanup()
                outputFile
            } finally {
                webView.destroy()
            }
        }

    fun printAdapter(pdfFile: File): PrintDocumentAdapter =
        object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?,
            ) {
                callback?.onLayoutFinished(
                    PrintDocumentInfo.Builder(pdfFile.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build(),
                    true,
                )
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?,
            ) {
                try {
                    if (destination == null) {
                        callback?.onWriteFailed("Missing destination")
                        return
                    }
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (_: Exception) {
                    callback?.onWriteFailed("Unable to write PDF")
                }
            }
        }

    private fun configure(webView: WebView) {
        webView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        webView.textDirection = View.TEXT_DIRECTION_LTR
        webView.settings.javaScriptEnabled = false
        webView.settings.domStorageEnabled = false
        webView.settings.allowContentAccess = false
        webView.settings.allowFileAccess = true
        webView.settings.useWideViewPort = false
        webView.settings.loadWithOverviewMode = false
        webView.settings.builtInZoomControls = false
        webView.settings.displayZoomControls = false
        webView.setInitialScale(100)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private suspend fun awaitPageLoad(webView: WebView, html: String) {
        suspendCancellableCoroutine<Unit> { cont ->
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (cont.isActive) cont.resume(Unit)
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?,
                ) {
                    if (cont.isActive) {
                        cont.resumeWithException(IllegalStateException(description ?: "Document PDF rendering failed"))
                    }
                }
            }
            webView.loadDataWithBaseURL(
                "file:///android_asset/documents/",
                html,
                "text/html",
                "utf-8",
                null,
            )
        }
    }

    private fun prepareA4Viewport(webView: WebView) {
        val density = webView.resources.displayMetrics.density
        val widthPx = (A4_WIDTH_CSS_PX * density).roundToInt()
        val heightPx = (A4_HEIGHT_CSS_PX * density).roundToInt()
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
        )
        webView.layout(0, 0, widthPx, heightPx)
        webView.scrollTo(0, 0)
    }

    private fun expandToRenderedContent(webView: WebView) {
        val density = webView.resources.displayMetrics.density
        val widthPx = (A4_WIDTH_CSS_PX * density).roundToInt()
        val minHeightPx = (A4_HEIGHT_CSS_PX * density).roundToInt()
        val contentHeightPx = (webView.contentHeight * density).roundToInt()
        val heightPx = maxOf(webView.measuredHeight, webView.height, contentHeightPx, minHeightPx)
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
        )
        webView.layout(0, 0, widthPx, heightPx)
        webView.scrollTo(0, 0)
    }

    private suspend fun awaitVisualState(webView: WebView) {
        suspendCancellableCoroutine<Unit> { cont ->
            webView.postVisualStateCallback(
                1L,
                object : WebView.VisualStateCallback() {
                    override fun onComplete(requestId: Long) {
                        if (cont.isActive) cont.resume(Unit)
                    }
                },
            )
        }
    }

    private fun writeWebViewToPdf(webView: WebView, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()
        val attrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build()
        val document: PdfDocument = PrintedPdfDocument(context, attrs)
        try {
            val density = attrs.resolution ?: error("Missing PDF resolution")
            val mediaSize = attrs.mediaSize ?: error("Missing PDF media size")
            val pageWidthPx = (mediaSize.widthMils / 1000f * density.horizontalDpi).roundToInt()
            val pageHeightPx = (mediaSize.heightMils / 1000f * density.verticalDpi).roundToInt()
            val contentWidthPx = webView.width.toFloat()
            val contentHeightPx = webView.height.toFloat()
            val fitScale = min(pageWidthPx / contentWidthPx, pageHeightPx / contentHeightPx)
            val dx = (pageWidthPx - contentWidthPx * fitScale) / 2f
            val dy = (pageHeightPx - contentHeightPx * fitScale) / 2f

            webView.scrollTo(0, 0)
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, 1).create())
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)
            canvas.save()
            canvas.translate(dx.coerceAtLeast(0f), dy.coerceAtLeast(0f))
            canvas.scale(fitScale, fitScale)
            webView.draw(canvas)
            canvas.restore()
            document.finishPage(page)
            FileOutputStream(outputFile).use { output -> document.writeTo(output) }
        } finally {
            document.close()
        }
    }
}
