package io.github.xjet.newsdemo

import io.github.xjet.core.XViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** MVVM screen model for the news list. */
class NewsViewModel(
    private val repository: NewsRepository = NewsRepository(),
) : XViewModel() {

    private val _items = MutableStateFlow<List<NewsItem>>(emptyList())
    val items: StateFlow<List<NewsItem>> = _items.asStateFlow()

    private var usingSample = false

    init {
        loadRemote()
    }

    override fun refresh() = if (usingSample) loadSample() else loadRemote()

    fun loadRemote() {
        setLoading()
        launchSafe("news.remote") {
            _items.value = repository.fetchTop()
            usingSample = false
            setContent()
        }
    }

    fun loadSample() {
        setLoading()
        launchSafe("news.sample") {
            _items.value = repository.sample()
            usingSample = true
            setContent()
        }
    }
}
