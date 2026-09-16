package io.github.xjet.xml

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import io.github.xjet.core.UiState
import io.github.xjet.core.collectIn
import io.github.xjet.core.XViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * MVVM base Activity for the XML flavor.
 *
 * ```kotlin
 * class ProfileActivity : XJetActivity<ProfileViewModel>() {
 *     override val viewModel: ProfileViewModel by viewModels()
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_profile)
 *     }
 *     override fun onUiState(state: UiState) {
 *         binding.loading.visibility = visibleWhen(state is UiState.Loading)
 *         binding.content.visibility = visibleWhen(state is UiState.Content)
 *     }
 * }
 * ```
 *
 * [uiState] subscribes to the ViewModel's four-state flow while the lifecycle
 * is at least STARTED, so classic screens get the same MVVM contract as Compose.
 */
abstract class XJetActivity<VM : ViewModel> : ComponentActivity() {

    /** The screen's ViewModel (instantiate with `by viewModels()` in [viewModel]). */
    protected abstract val viewModel: VM

    /** Four-state page flow collected from the ViewModel. */
    protected open val uiState: StateFlow<UiState> = MutableStateFlow(UiState.Content)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        if (viewModel is XViewModel) {
            uiState.collectIn(this) { onUiState(it) }
        }
    }

    /** Called on every [uiState] change while the screen is started. */
    protected open fun onUiState(state: UiState) = Unit

    /** Convenience: navigate using the framework router. */
    fun navigate(route: String, args: Map<String, Any> = emptyMap()) {
        io.github.xjet.core.XJet.router().navigate(route, args)
    }
}

