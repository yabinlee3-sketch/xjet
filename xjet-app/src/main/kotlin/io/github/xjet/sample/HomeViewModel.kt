package io.github.xjet.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.xjet.core.OneShotEvent
import io.github.xjet.core.XJet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val greeting: String = "",
    val config: String = "",
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    val toasts = OneShotEvent<String>()

    init {
        viewModelScope.launch {
            val greeting = XJet.get(GreetingService::class.java).greet()
            val config = XJet.getOrNull(ConfigGreetingService::class.java)?.greet() ?: "config-not-found"
            _state.update { it.copy(greeting = greeting, config = config) }
        }
    }

    fun onToast() {
        viewModelScope.launch {
            toasts.emit("one-shot event @ ${System.currentTimeMillis()}")
        }
    }

    fun onOpenXml() {
        XJet.router().navigate("xmlScreen")
    }

    suspend fun insertDraft(text: String) {
        XJet.database().dao(MessageDao::class.java).insert(MessageEntity(text = text))
    }
}
