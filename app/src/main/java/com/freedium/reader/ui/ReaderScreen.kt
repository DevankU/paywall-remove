package com.freedium.reader.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.freedium.reader.ui.components.AIAction
import com.freedium.reader.ui.components.LiquidAIPopup
import com.freedium.reader.ui.components.LiquidDropdownMenu
import com.freedium.reader.ui.components.LiquidMenuButton
import com.freedium.reader.ui.components.LiquidToggle
import com.freedium.reader.ui.components.MenuItem
import com.freedium.reader.ui.components.WebViewContainer
import com.freedium.reader.ui.components.exportAsPdf
import com.freedium.reader.ui.components.injectDarkMode
import com.freedium.reader.ui.components.injectReadingMode
import com.freedium.reader.ui.components.removeDarkMode
import com.freedium.reader.ui.components.removeReadingMode
import com.freedium.reader.ui.components.takeLongScreenshot
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun ReaderScreen(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backdrop = rememberLayerBackdrop()

    // State
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isMenuVisible by remember { mutableStateOf(false) }
    var isMenuButtonVisible by remember { mutableStateOf(true) }
    var isAIPopupVisible by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    // Feature toggles
    var isDarkMode by remember { mutableStateOf(false) }
    var isReadingMode by remember { mutableStateOf(false) }

    fun captureScreenshot() {
        Toast.makeText(context, "Capturing screenshot...", Toast.LENGTH_SHORT).show()
        webView?.takeLongScreenshot { success, message ->
            val finalMessage = if (success) message else "Error: $message"
            Toast.makeText(context, finalMessage, Toast.LENGTH_LONG).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            captureScreenshot()
        } else {
            Toast.makeText(context, "Storage permission required to save screenshot", Toast.LENGTH_LONG).show()
        }
    }

    // Handle back press
    BackHandler {
        if (isMenuVisible) {
            isMenuVisible = false
        } else if (isAIPopupVisible) {
            isAIPopupVisible = false
        } else if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            // Let the activity handle it (finish)
            (context as? android.app.Activity)?.finish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color(0xFF121212) else Color.White)
    ) {
        WebViewContainer(
            url = url,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = { wv ->
                webView = wv
                // Apply initial settings if needed
            },
            onScrollDirectionChange = { isUp ->
                // Hide menu on scroll down (isUp = false), show on scroll up (isUp = true)
                isMenuButtonVisible = isUp
                if (!isUp) isMenuVisible = false
            },
            onTextSelected = { text ->
                selectedText = text
                isAIPopupVisible = true
            }
        )

        // Menu Button
        AnimatedVisibility(
            visible = isMenuButtonVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            LiquidMenuButton(
                onClick = { isMenuVisible = !isMenuVisible },
                backdrop = backdrop
            )
        }

        // Dropdown Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 24.dp)
        ) {
            LiquidDropdownMenu(
                visible = isMenuVisible,
                backdrop = backdrop,
                onDismiss = { isMenuVisible = false },
                darkModeToggle = {
                    LiquidToggle(
                        checked = isDarkMode,
                        onCheckedChange = { checked ->
                            isDarkMode = checked
                            if (checked) {
                                // Disable reading mode if active
                                if (isReadingMode) {
                                    isReadingMode = false
                                    webView?.removeReadingMode()
                                }
                                webView?.injectDarkMode()
                            } else {
                                webView?.removeDarkMode()
                            }
                        },
                        backdrop = backdrop
                    )
                },
                items = listOf(
                    MenuItem(
                        title = if (isReadingMode) "Exit Reading Mode" else "Reading Mode",
                        onClick = {
                            isReadingMode = !isReadingMode
                            if (isReadingMode) {
                                // Disable dark mode if active
                                if (isDarkMode) {
                                    isDarkMode = false
                                    webView?.removeDarkMode()
                                }
                                webView?.injectReadingMode()
                            } else {
                                webView?.removeReadingMode()
                            }
                        }
                    ),
                    MenuItem(
                        title = "Long Screenshot",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Android 10+ uses MediaStore, no permission needed for downloads
                                captureScreenshot()
                            } else {
                                // Android 9 and below need permission
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) -> {
                                        captureScreenshot()
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    }
                                }
                            }
                        }
                    ),
                    MenuItem(
                        title = "Export as PDF",
                        onClick = {
                            webView?.exportAsPdf(context)
                        }
                    ),
                    MenuItem(
                        title = "Open in Browser",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    MenuItem(
                        title = "Share Link",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, url)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        }
                    )
                )
            )
        }

        // AI Popup
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp)
        ) {
            LiquidAIPopup(
                visible = isAIPopupVisible,
                selectedText = selectedText,
                backdrop = backdrop,
                onDismiss = { isAIPopupVisible = false },
                onAction = { action, prompt ->
                    when (action) {
                        AIAction.AskChatGPT -> {
                            // Copy to clipboard first
                            copyToClipboard(context, selectedText)

                            // Open ChatGPT
                            openChatGPT(context, prompt)
                        }
                        else -> {
                            // For Explain, Summarize, Translate
                            // Copy to clipboard
                            copyToClipboard(context, prompt)

                            // Open ChatGPT
                            openChatGPT(context, prompt)
                        }
                    }
                }
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun openChatGPT(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.openai.chatgpt")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web if app not installed, or just show toast
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.openai.com/"))
            context.startActivity(webIntent)
            Toast.makeText(context, "ChatGPT app not found, opening web", Toast.LENGTH_SHORT).show()
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open ChatGPT", Toast.LENGTH_SHORT).show()
        }
    }
}
