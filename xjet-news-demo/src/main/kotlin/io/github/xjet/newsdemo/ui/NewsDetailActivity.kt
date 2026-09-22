package io.github.xjet.newsdemo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.xjet.compose.XJetTheme
import io.github.xjet.newsdemo.model.NewsItem

class NewsDetailActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val item = intent?.getSerializableExtra("news") as? NewsItem
        setContent {
            XJetTheme {
                NewsDetailScreen(item)
            }
        }
    }
}
