package com.freedium.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.freedium.reader.ui.ReaderScreen
import com.freedium.reader.ui.theme.FreediumReaderTheme

class ReaderActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL)
        if (url == null) {
            finish()
            return
        }

        setContent {
            FreediumReaderTheme {
                ReaderScreen(url = url)
            }
        }
    }
}
