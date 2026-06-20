package app.tijario.features.documents.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.ceil
import kotlin.math.roundToInt

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
            val html = renderer.render(model)
            val webView = WebView(context)
            try {
                configure(webView)
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
                            if (cont.isActive) cont.resumeWithException(IllegalStateException(description ?: "Document preview failed"))
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
                extras: android.os.Bundle?,
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
        webView.settings.javaScriptEnabled = false
        webView.settings.domStorageEnabled = false
        webView.settings.allowContentAccess = false
        webView.settings.allowFileAccess = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
    }

    private fun writeWebViewToPdf(webView: WebView, outputFile: File) {
        val attrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .build()
        val document: PdfDocument = PrintedPdfDocument(context, attrs)
        try {
            val density = attrs.resolution ?: error("Missing PDF resolution")
            val mediaSize = attrs.mediaSize ?: error("Missing PDF media size")
            val pageWidthPx = (mediaSize.widthMils / 1000f * density.horizontalDpi).roundToInt()
            val pageHeightPx = (mediaSize.heightMils / 1000f * density.verticalDpi).roundToInt()

            webView.measure(
                View.MeasureSpec.makeMeasureSpec(pageWidthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            val contentHeightPx = maxOf(webView.measuredHeight, webView.contentHeight)
            webView.layout(0, 0, pageWidthPx, contentHeightPx)

            val pageCount = ceil(contentHeightPx / pageHeightPx.toDouble()).toInt().coerceAtLeast(1)
            repeat(pageCount) { index ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, index + 1).create())
                val canvas = page.canvas
                canvas.save()
                canvas.translate(0f, -(index * pageHeightPx).toFloat())
                webView.draw(canvas)
                canvas.restore()
                document.finishPage(page)
            }
            FileOutputStream(outputFile).use { output -> document.writeTo(output) }
        } finally {
            document.close()
        }
    }
}
