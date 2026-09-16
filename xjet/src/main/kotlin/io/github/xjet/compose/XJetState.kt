package io.github.xjet.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.xjet.core.UiState

/** Canonical page state: loading / error / empty / content. */

/**
 * Four-state content container mirroring XDroid's contentLayout but built for
 * Compose. Swap any slot for your own composable.
 */
@Composable
fun XJetStateBox(
    state: UiState,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { CircularProgressIndicator() },
    error: @Composable (String?) -> Unit = { message -> Text(message ?: "出错了") },
    empty: @Composable () -> Unit = { Text("暂无数据") },
    content: @Composable () -> Unit,
) {
    when (state) {
        UiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { loading() }
        is UiState.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { error(state.message) }
        UiState.Empty -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { empty() }
        UiState.Content -> content()
    }
}

