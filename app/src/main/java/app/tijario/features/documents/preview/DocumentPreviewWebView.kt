package app.tijario.features.documents.preview

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.template.AndroidAssetDocumentTemplateLoader
import app.tijario.features.documents.template.DocumentHtmlRenderer

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DocumentPreviewWebView(
    model: DocumentRenderModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val renderer = remember(context) { DocumentHtmlRenderer(AndroidAssetDocumentTemplateLoader(context)) }
    val html = remember(model) { renderer.render(model) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(794f / 1123f),
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false
                    settings.allowContentAccess = false
                    settings.allowFileAccess = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    setBackgroundColor(Color.TRANSPARENT)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true
                    }
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(
                    "file:///android_asset/documents/",
                    html,
                    "text/html",
                    "utf-8",
                    null,
                )
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
