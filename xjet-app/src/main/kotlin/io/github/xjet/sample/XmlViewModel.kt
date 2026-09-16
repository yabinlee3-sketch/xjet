package io.github.xjet.sample

import io.github.xjet.core.XViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** MVVM screen model for the XML/Compose-via-XML page. */
class XmlViewModel(
    private val repository: GreetingRepository = GreetingRepository(),
) : XViewModel() {

    private val _data = MutableStateFlow(HomeGreetingData())
    val data: StateFlow<HomeGreetingData> = _data.asStateFlow()

    init {
        load()
    }

    override fun refresh() = load()

    private fun load() {
        setLoading()
        launchSafe("xml.load") {
            _data.value = repository.load()
            setContent()
        }
    }
}
