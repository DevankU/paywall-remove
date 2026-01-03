package com.freedium.reader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freedium.reader.utils.UrlRouter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent()
    }

    private fun handleShareIntent() {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            val url = extractUrl(sharedText)

            if (url != null) {
                val routedUrl = UrlRouter.routeUrl(url)
                launchReader(routedUrl)
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null

        // Common URL pattern
        val urlRegex = Regex("https?://[\\w\\-./?&#=%]+")
        val match = urlRegex.find(text)
        return match?.value
    }

    private fun launchReader(url: String) {
        val intent = Intent(this, ReaderActivity::class.java).apply {
            putExtra(ReaderActivity.EXTRA_URL, url)
        }
        startActivity(intent)
        finish()
    }
}
