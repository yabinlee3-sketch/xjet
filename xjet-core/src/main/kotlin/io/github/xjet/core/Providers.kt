package io.github.xjet.core

import kotlinx.coroutines.flow.Flow

/** Navigation destination metadata collected by @XRoute. */
data class RouteDescriptor(
    val path: String,
    val group: String = "app",
    val title: String = "",
    val targetClassName: String = "",
)

/** Cache abstraction. Default: in-memory map backed implementation. */
interface CacheProvider {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun getBytes(key: String): ByteArray?
    fun putBytes(key: String, value: ByteArray)
    fun remove(key: String)
    fun clear()
}

/** Event bus abstraction. Default: [SharedFlowEventBus]. */
interface EventBusProvider {
    fun <T : Any> events(clazz: Class<T>): Flow<T>
    fun <T : Any> stickyEvents(clazz: Class<T>): Flow<T>
    suspend fun post(event: Any)
    suspend fun postSticky(event: Any)
}

/**
 * Database abstraction. XJet ships [io.github.xjet.room.RoomDatabaseProvider]
 * as the default Room-backed implementation; pass your own implementation to
 * XJetConfig.database to swap the storage engine without touching call sites.
 */
interface DatabaseProvider {
    fun <T : Any> dao(daoClass: Class<T>): T
    suspend fun <T : Any> transaction(block: suspend () -> T): T
    suspend fun clearAllTables()
    fun databaseName(): String
}

/** Navigation abstraction. Default: [SimpleRouterProvider] + RouteRegistry. */
interface RouterProvider {
    fun navigate(route: String, args: Map<String, Any> = emptyMap())
    fun back()
    fun currentRoute(): String?
}

/** Minimal image loading abstraction. */
interface ImageLoaderProvider {
    fun load(url: String, target: ImageTarget)
}

/** A view/object that can receive a loaded bitmap. Custom targets are welcome. */
interface ImageTarget {
    fun onLoadSuccess(bitmap: android.graphics.Bitmap) {}
    fun onLoadFailed(throwable: Throwable) {}
}

/** Generic one-shot UI affordance delegated by base activities/fragments. */
interface UiDelegate {
    fun showToast(message: String)
    fun showShortToast(message: String) = showToast(message)
    fun showLoading()
    fun hideLoading()
    fun onBackPressed(): Boolean = false
}

/** Compose / XML dual-format marker implemented by content hosts. */
interface XJetContent {
    val routePath: String
}

