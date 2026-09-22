package io.github.xjet.newsdemo.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.xjet.newsdemo.model.NewsItem

@Composable
fun NewsDetailScreen(item: NewsItem?) {
    if (item == null) {
        Text("没有这条新闻", modifier = Modifier.padding(24.dp))
        return
    }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(item.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("@${item.by} · ▲ ${item.score} · ${item.comments} 评论", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Text(item.url, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("用浏览器打开原文")
        }
    }
}
