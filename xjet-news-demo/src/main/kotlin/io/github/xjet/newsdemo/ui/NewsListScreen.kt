package io.github.xjet.newsdemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.xjet.compose.XJetStateBox
import io.github.xjet.core.XJet
import io.github.xjet.core.open
import io.github.xjet.newsdemo.adapter.NewsCard
import io.github.xjet.newsdemo.model.NewsItem
import io.github.xjet.newsdemo.present.NewsViewModel

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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item: NewsItem ->
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

