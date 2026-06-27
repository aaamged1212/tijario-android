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
import android.util.Base64
import java.net.URL
import java.security.MessageDigest
import io.github.jan.supabase.auth.auth
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
        val resolvedModel = resolveModelLogo(model)
        val file = cacheManager.resolve(resolvedModel)
        if (cacheManager.isValid(file)) {
            return PdfGenerationResult(file, reusedCache = true)
        }
        val docId = model.documentId ?: ""
        val quotaResult = app.tijario.data.AppContainer.repository(context).finalizeOrVerifyQuota(docId)
        if (quotaResult.isFailure) {
            val exception = quotaResult.exceptionOrNull()
            if (exception?.message == "QUOTA_LIMIT_EXCEEDED") {
                throw IllegalStateException("QUOTA_LIMIT_EXCEEDED")
            }
        }
        return PdfGenerationResult(renderPdf(resolvedModel, file), reusedCache = false)
    }

    private suspend fun resolveModelLogo(model: DocumentRenderModel): DocumentRenderModel {
        val logoUrl = model.business.logoUrl ?: return model
        if (!logoUrl.startsWith("http")) return model

        val base64 = getCachedLogoBase64(logoUrl) ?: return model
        return model.copy(
            business = model.business.copy(logoUrl = base64)
        )
    }

    private suspend fun getCachedLogoBase64(logoUrl: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val userId = app.tijario.config.Supabase.client.auth.currentUserOrNull()?.id
            var logoFile: File? = null
            if (userId != null) {
                logoFile = app.tijario.features.business.logo.LogoAssetManager(context).getLocalLogoFile(userId)
            }

            if (logoFile == null || !logoFile.exists()) {
                val cacheDir = File(context.filesDir, "business-logo-cache").apply { mkdirs() }
                val cacheFile = File(cacheDir, "${logoUrl.sha256()}.img")
                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    val bytes = URL(logoUrl).openStream().use { it.readBytes() }
                    if (bytes.isNotEmpty()) {
                        cacheFile.writeBytes(bytes)
                    }
                }
                if (cacheFile.exists() && cacheFile.length() > 0L) {
                    logoFile = cacheFile
                }
            }

            if (logoFile != null && logoFile.exists() && logoFile.length() > 0L) {
                val bytes = logoFile.readBytes()
                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val mimeType = when {
                    logoUrl.endsWith(".png", ignoreCase = true) -> "image/png"
                    logoUrl.endsWith(".webp", ignoreCase = true) -> "image/webp"
                    else -> "image/jpeg"
                }
                "data:$mimeType;base64,$base64String"
            } else {
                null
            }
        }.getOrNull()
    }

    private fun String.sha256(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { "%02x".format(it) }

    suspend fun renderPdf(model: DocumentRenderModel, outputFile: File): File =
        withContext(Dispatchers.Main) {
            val html = renderer.render(model, DocumentRenderTarget.Pdf)
            val webView = WebView(context)
            try {
                configure(webView)
                awaitPageLoad(webView, html)
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

    private suspend fun writeWebViewToPdf(webView: WebView, outputFile: File): Unit = suspendCancellableCoroutine { cont ->
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
    }
}
