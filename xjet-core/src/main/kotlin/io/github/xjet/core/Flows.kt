package io.github.xjet.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/** Injectable dispatcher bag. Replace at init via config for tests or platform runtimes. */
data class XJetDispatchers(
    val main: CoroutineDispatcher = Dispatchers.Main,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val computation: CoroutineDispatcher = Dispatchers.Default,
)

/** Application-scoped supervisor scope created on [XJet.init]. */
fun XJet.scope(): CoroutineScope = CoroutineScope(SupervisorJob() + XJetDispatchers().io)

/** Collect a [Flow] only while [owner] is at least [minState] (default RESUMED). */
fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
) {
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(minState) {
            collect { action(it) }
        }
    }
}

/** One-shot event with SharedFlow (no replay). */
class OneShotEvent<T> {
    private val flow = MutableSharedFlow<T>(extraBufferCapacity = 1)
    suspend fun emit(value: T) {
        flow.emit(value)
    }
    fun asFlow(): Flow<T> = flow
}
