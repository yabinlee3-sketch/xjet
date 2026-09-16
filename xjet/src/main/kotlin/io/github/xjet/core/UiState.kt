package io.github.xjet.core

/** Canonical four-state page state shared by Compose and XML UIs. */
sealed interface UiState {
    data object Loading : UiState
    data class Error(val message: String? = null) : UiState
    data object Empty : UiState
    /** Content is ready; render the page body (data lives in the state object). */
    data object Content : UiState
}
