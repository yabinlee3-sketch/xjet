package io.github.xjet.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * MVVM base for the whole framework. Unlike a bare platform [ViewModel],
 * [XViewModel] owns the canonical four-state [uiState] flow (loading / error /
 * empty / content) that the Compose and XML layers both collect, plus the
 * framework-wide error funnel.
 *
 * Subclass it, expose business state as an immutable `StateFlow`, publish page
 * phase via [setLoading] / [setContent] / [setError] / [setEmpty], and let the
 * view call [retry] to re-run [refresh].
 */
abstract class XViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    /** Canonical four-state page phase, collected by Compose and XML views. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** The UI requested retry after an error or an empty state. */
    protected open fun refresh() {
        setLoading()
    }

    /** Called by the view (button / error slot) to reload. */
    fun retry() {
        refresh()
        if (uiState.value is UiState.Error) {
            // keep loading visible even if refresh() forgets
            setLoading()
        }
    }

    /** Marks the page as loading. */
    protected fun setLoading() {
        _uiState.value = UiState.Loading
    }

    /** Marks the page as errored and reports [t] to the global interceptor. */
    protected fun setError(t: Throwable? = null, message: String? = t?.message) {
        _uiState.value = UiState.Error(message)
        t?.let { capture("vm:${javaClass.simpleName}", it) }
    }

    /** Marks the page as empty. */
    protected fun setEmpty() {
        _uiState.value = UiState.Empty
    }

    /** Marks the page as ready for content. */
    protected fun setContent() {
        _uiState.value = UiState.Content
    }

    /** Report to the global [XJet.capture] interceptor without rethrowing. */
    protected fun capture(source: String, throwable: Throwable) {
        XJet.capture(source, throwable)
    }

    /** Launch in [viewModelScope] and funnel any failure to the interceptor. */
    protected fun launchSafe(source: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (t: Throwable) {
                capture(source, t)
                setError(t)
            }
        }
    }

    /** Collect [flow] in [viewModelScope]; failures go to the interceptor. */
    protected fun <T> collect(flow: Flow<T>, onEach: (T) -> Unit) {
        viewModelScope.launch {
            try {
                flow.collect { value ->
                    try {
                        onEach(value)
                    } catch (t: Throwable) {
                        capture("collect:$flow", t)
                    }
                }
            } catch (t: Throwable) {
                capture("collect:$flow", t)
                setError(t)
            }
        }
    }
}
