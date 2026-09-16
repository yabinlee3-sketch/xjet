package io.github.xjet.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * MVVM conveniences built on top of [XJet].
 *
 * Subclass this instead of the platform [ViewModel] when you want the
 * framework's global [XJet.capture] error funnel and coroutine plumbing for
 * free. Vanilla `StateFlow` + `MutableStateFlow` remain the recommended UI
 * state contract (see the sample app), and Compose helpers collect them with
 * lifecycle awareness.
 */
abstract class XViewModel : ViewModel() {

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
            }
        }
    }
}
