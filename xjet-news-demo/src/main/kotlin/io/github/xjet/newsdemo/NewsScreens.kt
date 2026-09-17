package io.github.xjet.newsdemo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.xjet.compose.XJetStateBox
import io.github.xjet.core.XJet
import io.github.xjet.core.open

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(vm: NewsViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("XJet 头条") }) }
    ) { innerPadding ->
        XJetStateBox(
            state = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            error = { message ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(message ?: "网络不可用", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.loadRemote() }, modifier = Modifier.fillMaxWidth()) { Text("重试联网") }
                    OutlinedButton(onClick = { vm.loadSample() }, modifier = Modifier.fillMaxWidth()) { Text("离线示例数据") }
                }
            },
            empty = { Text("暂时没有新闻") },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        NewsCard(item, onClick = {
                            XJet.open(NewsDetailActivity::class.java) {
                                putSerializable("news", item)
                            }
                        })
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
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

@Composable
fun NewsDetailScreen(item: NewsItem?) {
    if (item == null) {
        Text("没有这条新闻", modifier = Modifier.padding(24.dp))
        return
    }
    val context = androidx.compose.ui.platform.LocalContext.current
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

