package io.github.xjet.newsdemo.adapter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import io.github.xjet.newsdemo.model.NewsItem

/** List item for the headline feed. Mirrors XDroid demo's adapter/HomeAdapter. */
@Composable
fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("@${item.by}", style = MaterialTheme.typography.bodySmall)
                Text("▲ ${item.score}", style = MaterialTheme.typography.bodySmall)
                Text("${item.comments} 评论", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
