package app.tijario.features.documents.preview

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.github.jan.supabase.auth.auth
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.template.AndroidAssetDocumentTemplateLoader
import app.tijario.features.documents.template.DocumentHtmlRenderer
import app.tijario.features.documents.template.DocumentRenderTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.security.MessageDigest

private const val A4_WIDTH_TO_HEIGHT = 210f / 297f
private val A4_LAYOUT_WIDTH = 794.dp
private val A4_LAYOUT_HEIGHT = 1123.dp

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun DocumentPreviewWebView(
    model: DocumentRenderModel,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
) {
    val context = LocalContext.current
    val logoUrl = model.business.logoUrl
    val cachedLogoBase64 by produceState<String?>(initialValue = null, logoUrl) {
        if (!logoUrl.isNullOrBlank() && logoUrl.startsWith("http")) {
            value = getCachedLogoBase64(context, logoUrl)
        }
    }

    val finalModel = remember(model, cachedLogoBase64) {
        if (cachedLogoBase64 != null) {
            model.copy(
                business = model.business.copy(logoUrl = cachedLogoBase64)
            )
        } else {
            model
        }
    }

    val renderer = remember(context) { DocumentHtmlRenderer(AndroidAssetDocumentTemplateLoader(context)) }
    val html = remember(finalModel) { renderer.render(finalModel, DocumentRenderTarget.Preview) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val boundedHeight = if (maxHeight == Dp.Infinity) maxWidth / A4_WIDTH_TO_HEIGHT else maxHeight
        val pageWidth = minOf(maxWidth, boundedHeight * A4_WIDTH_TO_HEIGHT)
        val pageHeight = pageWidth / A4_WIDTH_TO_HEIGHT
        val pageScale = pageWidth.value / A4_LAYOUT_WIDTH.value

        Box(
            modifier = Modifier
                .width(pageWidth)
                .height(pageHeight)
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutDirection = View.LAYOUT_DIRECTION_LTR
                        textDirection = View.TEXT_DIRECTION_LTR
                        settings.javaScriptEnabled = false
                        settings.domStorageEnabled = false
                        settings.allowContentAccess = false
                        settings.allowFileAccess = true
                        settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        settings.useWideViewPort = false
                        settings.loadWithOverviewMode = false
                        settings.setSupportZoom(interactive)
                        settings.builtInZoomControls = interactive
                        settings.displayZoomControls = false
                        setInitialScale(100)
                        isVerticalScrollBarEnabled = interactive
                        isHorizontalScrollBarEnabled = interactive
                        isNestedScrollingEnabled = interactive
                        overScrollMode = if (interactive) View.OVER_SCROLL_IF_CONTENT_SCROLLS else View.OVER_SCROLL_NEVER
                        isFocusable = interactive
                        setOnTouchListener { _, _ -> !interactive }
                        setBackgroundColor(Color.TRANSPARENT)
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true

                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.scrollTo(0, 0)
                            }
                        }
                    }
                },
                update = { webView ->
                    webView.settings.setSupportZoom(interactive)
                    webView.settings.builtInZoomControls = interactive
                    webView.isVerticalScrollBarEnabled = interactive
                    webView.isHorizontalScrollBarEnabled = interactive
                    webView.isNestedScrollingEnabled = interactive
                    webView.overScrollMode = if (interactive) View.OVER_SCROLL_IF_CONTENT_SCROLLS else View.OVER_SCROLL_NEVER
                    webView.isFocusable = interactive
                    webView.setOnTouchListener { _, _ -> !interactive }
                    webView.loadDataWithBaseURL(
                        "file:///android_asset/documents/",
                        html,
                        "text/html",
                        "utf-8",
                        null,
                    )
                },
                modifier = Modifier
                    .requiredWidth(A4_LAYOUT_WIDTH)
                    .requiredHeight(A4_LAYOUT_HEIGHT)
                    .graphicsLayer {
                        scaleX = pageScale
                        scaleY = pageScale
                        transformOrigin = TransformOrigin.Center
                    },
            )
        }
    }
}

private suspend fun getCachedLogoBase64(context: Context, logoUrl: String): String? =
    withContext(Dispatchers.IO) {
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
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val mimeType = when {
                    logoUrl.endsWith(".png", ignoreCase = true) -> "image/png"
                    logoUrl.endsWith(".webp", ignoreCase = true) -> "image/webp"
                    else -> "image/jpeg"
                }
                "data:$mimeType;base64,$base64"
            } else {
                null
            }
        }.getOrNull()
    }

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }

