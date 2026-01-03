package com.freedium.reader.ui.components

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WebViewState(
    val webView: WebView? = null,
    var isDarkMode: Boolean = false,
    var isReadingMode: Boolean = false
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    url: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {},
    onProgressChanged: (Int) -> Unit = {},
    onScrollDirectionChange: (Boolean) -> Unit = {}, // true = up (show), false = down (hide)
    onTextSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var lastScrollY by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.apply {
                stopLoading()
                clearHistory()
                clearCache(true)
                loadUrl("about:blank")
                onPause()
                removeAllViews()
                destroy()
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }

                // Scroll listener for auto-hiding menu
                viewTreeObserver.addOnScrollChangedListener {
                    val currentScrollY = scrollY
                    if (currentScrollY > lastScrollY + 10) {
                        // Scrolling down - Hide
                        onScrollDirectionChange(false)
                    } else if (currentScrollY < lastScrollY - 10) {
                        // Scrolling up - Show
                        onScrollDirectionChange(true)
                    }
                    lastScrollY = currentScrollY
                }

                addJavascriptInterface(object {
                    private var lastSelectedText = ""

                    @JavascriptInterface
                    fun onTextSelected(text: String) {
                        if (text.isNotBlank() && text != lastSelectedText) {
                            lastSelectedText = text
                            post { onTextSelected(text) }
                        }
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        injectTextSelectionHandler(this@apply)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                    }
                }

                loadUrl(url)
                webViewInstance = this
                onWebViewCreated(this)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

private fun injectTextSelectionHandler(webView: WebView) {
    val js = """
        (function() {
            document.addEventListener('selectionchange', function() {
                var selection = window.getSelection();
                var text = selection.toString().trim();
                if (text.length > 0) {
                    Android.onTextSelected(text);
                }
            });
        })();
    """.trimIndent()
    webView.evaluateJavascript(js, null)
}

fun WebView.injectDarkMode() {
    val js = """
        (function() {
            var style = document.getElementById('freedium-dark-mode');
            if (!style) {
                style = document.createElement('style');
                style.id = 'freedium-dark-mode';
                style.textContent = `
                    html, body { background-color: #1a1a1a !important; color: #e0e0e0 !important; }
                    * { background-color: inherit !important; color: inherit !important; border-color: #444 !important; }
                    img, video, picture, svg, canvas, iframe { filter: none !important; background-color: transparent !important; }
                    a { color: #6db3f2 !important; }
                    pre, code { background-color: #2d2d2d !important; }
                `;
                document.head.appendChild(style);
            }
        })();
    """.trimIndent()
    evaluateJavascript(js, null)
}

fun WebView.removeDarkMode() {
    val js = """
        (function() {
            var style = document.getElementById('freedium-dark-mode');
            if (style) style.remove();
        })();
    """.trimIndent()
    evaluateJavascript(js, null)
}

fun WebView.injectReadingMode() {
    val js = """
        (function() {
            var style = document.getElementById('freedium-reading-mode');
            if (!style) {
                style = document.createElement('style');
                style.id = 'freedium-reading-mode';
                style.textContent = `
                    html, body {
                        background-color: #FDF6E3 !important;
                        color: #5C4B37 !important;
                    }
                    * {
                        background-color: inherit !important;
                        color: inherit !important;
                    }
                    img, video, picture, svg, canvas, iframe {
                        filter: none !important;
                        background-color: transparent !important;
                    }
                    a { color: #268BD2 !important; }
                    body {
                        max-width: 700px !important;
                        margin: 0 auto !important;
                        padding: 20px !important;
                        font-size: 18px !important;
                        line-height: 1.8 !important;
                    }
                    header, footer, nav, aside, .sidebar, .ads, .advertisement, .social-share, .comments {
                        display: none !important;
                    }
                `;
                document.head.appendChild(style);
            }
        })();
    """.trimIndent()
    evaluateJavascript(js, null)
}

fun WebView.removeReadingMode() {
    val js = """
        (function() {
            var style = document.getElementById('freedium-reading-mode');
            if (style) style.remove();
        })();
    """.trimIndent()
    evaluateJavascript(js, null)
}

fun WebView.takeLongScreenshot(onComplete: (Boolean, String) -> Unit) {
    try {
        // Enable slow whole document drawing for API 21+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw()
        }

        measure(
            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )

        // Calculate total height
        val totalHeight = (contentHeight * scale).toInt()
        val totalWidth = width

        if (totalHeight <= 0 || totalWidth <= 0) {
            onComplete(false, "Cannot capture screenshot: invalid dimensions")
            return
        }

        // Create bitmap with full dimensions
        // Limit height to avoid OutOfMemoryError (approx 20000px is a safe limit on most devices)
        val safeHeight = totalHeight.coerceAtMost(20000)

        val bitmap = Bitmap.createBitmap(totalWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Save current state
        val originalLeft = scrollX
        val originalTop = scrollY

        // Draw the entire content
        // We need to layout the WebView to the full height temporarily
        layout(0, 0, totalWidth, safeHeight)
        draw(canvas)

        // Restore original state
        layout(0, 0, totalWidth, height)
        scrollTo(originalLeft, originalTop)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "FreediumReader_$timestamp.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Failed to create MediaStore entry")

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            bitmap.recycle()
            onComplete(true, "Saved to Downloads: $fileName")
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Create directory if it doesn't exist
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            onComplete(true, "Saved to Downloads: $fileName")
        }
    } catch (e: OutOfMemoryError) {
        onComplete(false, "Page too long to capture (OOM)")
    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(false, "Failed to save screenshot: ${e.message}")
    }
}

fun WebView.exportAsPdf(context: android.content.Context) {
    val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val jobName = "FreediumReader_$timestamp"

    val printAdapter = createPrintDocumentAdapter(jobName)
    val attributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()

    printManager.print(jobName, printAdapter, attributes)
}
