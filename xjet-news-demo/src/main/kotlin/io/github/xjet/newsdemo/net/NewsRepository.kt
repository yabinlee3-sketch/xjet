package io.github.xjet.newsdemo.net

import io.github.xjet.core.HttpResponse
import io.github.xjet.core.XJet
import io.github.xjet.core.XRepository
import io.github.xjet.core.getText
import io.github.xjet.newsdemo.kit.NewsKit
import io.github.xjet.newsdemo.model.NewsItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject

/**
 * Real network source (Hacker News) plus an offline fallback.
 * Swap in any other source by editing [fetchTop] — the ViewModel never sees
 * whether the data came from the network or the sample.
 */
class NewsRepository : XRepository() {

    suspend fun fetchTop(limit: Int = 20): List<NewsItem> = safe("news.top") {
        val ids = idsFrom(XJet.getText(NewsKit.HN_TOP_URL))
        coroutineScope {
            ids.take(limit).map { id -> async { itemFrom(id) } }.awaitAll()
        }.filterNotNull()
    }

    fun sample(): List<NewsItem> = SampleNews.items

    private fun idsFrom(response: HttpResponse): List<Long> {
        if (!response.isSuccess) error("Hacker News responded ${response.status}")
        val array = JSONArray(response.bodyText)
        return buildList {
            for (i in 0 until array.length()) add(array.getLong(i))
        }
    }

    private suspend fun itemFrom(id: Long): NewsItem? {
        val response = XJet.getText("${NewsKit.HN_ITEM_URL}/$id.json")
        if (!response.isSuccess) return null
        val json = JSONObject(response.bodyText)
        if (!json.has("title") || json.isNull("title")) return null
        return NewsItem(
            id = id,
            title = json.getString("title"),
            by = json.optString("by"),
            score = json.optInt("score"),
            comments = json.optInt("descendants"),
            url = json.optString("url").ifBlank { "https://news.ycombinator.com/item?id=$id" },
        )
    }
}
