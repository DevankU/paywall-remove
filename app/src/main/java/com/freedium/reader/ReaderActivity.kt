package com.freedium.reader

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.SpringForce
import com.freedium.reader.ui.GlassEffect
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReaderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var webView: WebView
    private lateinit var glassContainer: FrameLayout
    private lateinit var menuButton: ImageButton
    private lateinit var progressBar: ProgressBar

    private var isDarkMode = false
    private var isReadingMode = false
    private var isToolbarVisible = true

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        webView = findViewById(R.id.webView)
        glassContainer = findViewById(R.id.glassContainer)
        menuButton = findViewById(R.id.menuButton)
        progressBar = findViewById(R.id.progressBar)

        setupWebView()
        setupToolbar()

        val url = intent.getStringExtra(EXTRA_URL)
        if (url != null) {
            webView.loadUrl(url)
        } else {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
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

        webView.addJavascriptInterface(JsInterface(), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectTextSelectionHandler()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }

        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val diff = scrollY - oldScrollY
            if (diff > 15 && isToolbarVisible) {
                hideToolbar()
            } else if (diff < -15 && !isToolbarVisible) {
                showToolbar()
            }
        }
    }

    private fun setupToolbar() {
        menuButton.setOnClickListener { showMenu() }
    }

    private fun showMenu() {
        val popup = PopupMenu(this, menuButton)
        popup.menuInflater.inflate(R.menu.reader_menu, popup.menu)

        popup.menu.findItem(R.id.menu_dark_mode)?.title = if (isDarkMode) "Light Mode" else "Dark Mode"
        popup.menu.findItem(R.id.menu_reading_mode)?.title = if (isReadingMode) "Exit Reading Mode" else "Reading Mode"

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_dark_mode -> {
                    toggleDarkMode()
                    true
                }
                R.id.menu_reading_mode -> {
                    toggleReadingMode()
                    true
                }
                R.id.menu_screenshot -> {
                    takeLongScreenshot()
                    true
                }
                R.id.menu_export_pdf -> {
                    exportAsPdf()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        if (isDarkMode) {
            injectDarkMode()
        } else {
            removeDarkMode()
        }
    }

    private fun injectDarkMode() {
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
        webView.evaluateJavascript(js, null)
    }

    private fun removeDarkMode() {
        val js = """
            (function() {
                var style = document.getElementById('freedium-dark-mode');
                if (style) style.remove();
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun toggleReadingMode() {
        isReadingMode = !isReadingMode
        if (isReadingMode) {
            injectReadingMode()
        } else {
            removeReadingMode()
        }
    }

    private fun injectReadingMode() {
        val js = """
            (function() {
                var style = document.getElementById('freedium-reading-mode');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'freedium-reading-mode';
                    style.textContent = `
                        body { max-width: 700px !important; margin: 0 auto !important; padding: 20px !important; font-size: 18px !important; line-height: 1.8 !important; }
                        header, footer, nav, aside, .sidebar, .ads, .advertisement, .social-share, .comments { display: none !important; }
                        img { max-width: 100% !important; height: auto !important; }
                        p { margin-bottom: 1.5em !important; }
                    `;
                    document.head.appendChild(style);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun removeReadingMode() {
        val js = """
            (function() {
                var style = document.getElementById('freedium-reading-mode');
                if (style) style.remove();
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun takeLongScreenshot() {
        Toast.makeText(this, "Capturing screenshot...", Toast.LENGTH_SHORT).show()

        webView.measure(
            View.MeasureSpec.makeMeasureSpec(webView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val contentHeight = webView.contentHeight
        val scale = webView.scale
        val bitmapHeight = (contentHeight * scale).toInt()

        if (bitmapHeight <= 0 || webView.width <= 0) {
            Toast.makeText(this, "Cannot capture screenshot", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = Bitmap.createBitmap(webView.width, bitmapHeight.coerceAtMost(30000), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            webView.draw(canvas)

            saveScreenshot(bitmap)
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, "Page too long to capture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveScreenshot(bitmap: Bitmap) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "FreediumReader_$timestamp.png"

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            Toast.makeText(this, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportAsPdf() {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val jobName = "FreediumReader_$timestamp"

        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, printAdapter, attributes)
    }

    private fun injectTextSelectionHandler() {
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

    inner class JsInterface {
        private var lastSelectedText = ""

        @JavascriptInterface
        fun onTextSelected(text: String) {
            if (text.isNotBlank() && text != lastSelectedText) {
                lastSelectedText = text
                runOnUiThread {
                    showAiPopup(text)
                }
            }
        }
    }

    private fun showAiPopup(selectedText: String) {
        val popup = PopupMenu(this, menuButton)
        popup.menu.add(0, 1, 0, "Explain this")
        popup.menu.add(0, 2, 1, "Summarize")
        popup.menu.add(0, 3, 2, "Translate")
        popup.menu.add(0, 4, 3, "Ask ChatGPT")

        popup.setOnMenuItemClickListener { item ->
            val prompt = when (item.itemId) {
                1 -> "Explain this: $selectedText"
                2 -> "Summarize this: $selectedText"
                3 -> "Translate this to English: $selectedText"
                4 -> selectedText
                else -> selectedText
            }
            openChatGptWithText(prompt)
            true
        }

        popup.show()
    }

    /**
     * Opens ChatGPT app with the selected text using ACTION_SEND intent.
     * Only opens the app - does not fallback to web to avoid opening both.
     */
    private fun openChatGptWithText(prompt: String) {
        // First copy to clipboard
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Prompt", prompt)
        clipboard.setPrimaryClip(clip)

        // Try to open ChatGPT app with the text
        val chatGptIntent = Intent(Intent.ACTION_SEND).apply {
            setPackage("com.openai.chatgpt")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, prompt)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(chatGptIntent)
            Toast.makeText(this, "Opening ChatGPT with your text", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            // ChatGPT app not installed - just notify user, don't open web
            Toast.makeText(this, "ChatGPT app not installed. Text copied to clipboard.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Hide toolbar with iOS-style spring animation
     */
    private fun hideToolbar() {
        if (!isToolbarVisible) return
        isToolbarVisible = false

        // Spring animation for translation
        GlassEffect.createSpringAnimationY(
            glassContainer,
            -glassContainer.height.toFloat() - 50f,
            SpringForce.STIFFNESS_MEDIUM,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).start()

        // Fade out
        GlassEffect.createSpringAnimationAlpha(
            glassContainer,
            0f,
            SpringForce.STIFFNESS_MEDIUM,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).start()
    }

    /**
     * Show toolbar with iOS-style bouncy spring animation
     */
    private fun showToolbar() {
        if (isToolbarVisible) return
        isToolbarVisible = true

        // Spring animation for translation (bouncy)
        GlassEffect.createSpringAnimationY(
            glassContainer,
            0f,
            SpringForce.STIFFNESS_MEDIUM,
            SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        ).start()

        // Fade in
        GlassEffect.createSpringAnimationAlpha(
            glassContainer,
            1f,
            SpringForce.STIFFNESS_LOW,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).start()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        // Clean up WebView to free resources
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.loadUrl("about:blank")
        webView.onPause()
        webView.removeAllViews()
        webView.destroy()
        super.onDestroy()
    }
}
