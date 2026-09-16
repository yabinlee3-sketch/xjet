package io.github.xjet.xml

import android.view.View
import io.github.xjet.core.UiState

/**
 * Classic XML helper: toggle four views (loading / error / empty / content)
 * according to a [UiState]. This gives XML screens the same four-state MVVM
 * behavior as [io.github.xjet.compose.XJetStateBox].
 */
fun setUiState(
    state: UiState,
    loading: View? = null,
    error: View? = null,
    empty: View? = null,
    content: View? = null,
    onError: (message: String?) -> Unit = {},
) {
    val show = { v: View? -> v?.visibility = View.VISIBLE }
    val hide = { v: View? -> v?.visibility = View.GONE }
    when (state) {
        UiState.Loading -> {
            show(loading)
            hide(error); hide(empty); hide(content)
        }
        is UiState.Error -> {
            hide(loading)
            show(error)
            hide(empty); hide(content)
            onError(state.message)
        }
        UiState.Empty -> {
            hide(loading); hide(error)
            show(empty)
            hide(content)
        }
        UiState.Content -> {
            hide(loading); hide(error); hide(empty)
            show(content)
        }
    }
}
