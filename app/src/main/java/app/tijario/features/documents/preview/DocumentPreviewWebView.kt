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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.github.jan.supabase.auth.auth
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.model.resolveDocumentLogoForRender
import app.tijario.features.documents.template.AndroidAssetDocumentTemplateLoader
import app.tijario.features.documents.template.DocumentHtmlRenderer
import app.tijario.features.documents.template.DocumentRenderTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val A4_WIDTH_TO_HEIGHT = 210f / 297f
// Render at the actual CSS A4 size, not a density-expanded dp size.
private const val A4_WIDTH_CSS_PX = 794
private const val A4_HEIGHT_CSS_PX = 1123

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
        val resolvedLogoUrl = resolveDocumentLogoForRender(logoUrl, cachedLogoBase64)
        if (resolvedLogoUrl == logoUrl) {
            model
        } else {
            model.copy(business = model.business.copy(logoUrl = resolvedLogoUrl))
        }
    }

    val renderer = remember(context) { DocumentHtmlRenderer(AndroidAssetDocumentTemplateLoader(context)) }
    val html = remember(finalModel) { renderer.render(finalModel, DocumentRenderTarget.Preview) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val a4LayoutWidth = with(density) { A4_WIDTH_CSS_PX.toDp() }
        val a4LayoutHeight = with(density) { A4_HEIGHT_CSS_PX.toDp() }
        val boundedHeight = if (maxHeight == Dp.Infinity) maxWidth / A4_WIDTH_TO_HEIGHT else maxHeight
        val pageWidth = minOf(maxWidth, boundedHeight * A4_WIDTH_TO_HEIGHT)
        val pageScale = pageWidth.value / a4LayoutWidth.value
        Box(
            modifier = Modifier
                .requiredWidth(a4LayoutWidth)
                .requiredHeight(a4LayoutHeight)
                .graphicsLayer {
                    scaleX = pageScale
                    scaleY = pageScale
                    transformOrigin = TransformOrigin.Center
                },
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
                    if (webView.tag != html) {
                        webView.tag = html
                        webView.loadDataWithBaseURL(
                            "file:///android_asset/documents/",
                            html,
                            "text/html",
                            "utf-8",
                            null,
                        )
                    }
                },
                modifier = Modifier
                    .requiredWidth(a4LayoutWidth)
                    .requiredHeight(a4LayoutHeight),
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

            if (logoFile != null && logoFile.exists() && logoFile.length() in 1..MAX_LOGO_BYTES) {
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

private const val MAX_LOGO_BYTES = 5L * 1024L * 1024L

