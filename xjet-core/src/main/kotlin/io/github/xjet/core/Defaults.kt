package io.github.xjet.core

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

/** In-memory default cache. Override via config.cache when persistence is needed. */
class InMemoryCacheProvider : CacheProvider {
    private val store = ConcurrentHashMap<String, Any>()

    override fun get(key: String): String? = store[key] as String?
    override fun put(key: String, value: String) {
        store[key] = value
    }
    override fun getBytes(key: String): ByteArray? = store[key] as ByteArray?
    override fun putBytes(key: String, value: ByteArray) {
        store[key] = value
    }
    override fun remove(key: String) {
        store.remove(key)
    }
    override fun clear() = store.clear()
}

/** Default event bus built on coroutines: SharedFlow once-off, StateFlow sticky. */
class SharedFlowEventBus : EventBusProvider {
    private data class Envelope(val payload: Any, val sticky: Boolean)

    private val bus = MutableSharedFlow<Envelope>(extraBufferCapacity = 64, onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST)
    private val sticky = MutableStateFlow<Map<Class<*>, Any>>(emptyMap())

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> events(clazz: Class<T>): Flow<T> {
        return bus.filter { it.payload::class.java == clazz }.map { it.payload as T }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> stickyEvents(clazz: Class<T>): Flow<T> {
        return sticky.map { it[clazz] as T? }.filterNotNull()
    }

    override suspend fun post(event: Any) {
        bus.emit(Envelope(event, sticky = false))
    }

    override suspend fun postSticky(event: Any) {
        sticky.update { it + (event::class.java to event) }
        bus.emit(Envelope(event, sticky = true))
    }
}

/** Default nav: starts [android.app.Activity] targets; Compose targets are handled upstream. */
class SimpleRouterProvider(
    private val context: Context,
    private val registry: RouteRegistry,
    private val delegate: RouterProvider? = null,
) : RouterProvider {

    override fun navigate(route: String, args: Map<String, Any>) {
        val descriptor = registry.resolve(route) ?: throw IllegalArgumentException("Unknown route: $route")
        val targetClass = run {
            if (descriptor.targetClassName.isBlank()) null else Class.forName(descriptor.targetClassName)
        }
        if (targetClass != null && android.app.Activity::class.java.isAssignableFrom(targetClass)) {
            val intent = Intent(context, targetClass)
            args.entries.forEach { (key, value) ->
                when (value) {
                    is String -> intent.putExtra(key, value)
                    is Int -> intent.putExtra(key, value)
                    is Long -> intent.putExtra(key, value)
                    is Boolean -> intent.putExtra(key, value)
                    is ArrayList<*> -> intent.putStringArrayListExtra(key, ArrayList(value.map { it as String }))
                }
            }
            if (context is android.app.Activity) context.startActivity(intent) else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            return
        }
        delegate?.navigate(route, args) ?: throw UnsupportedOperationException(
            "Route '$route' targets '${descriptor.targetClassName}' which is not an Activity. " +
                "Use the Compose router or provide a custom RouterProvider."
        )
    }

    override fun back() {
        val activity = context as? android.app.Activity
        if (activity != null) activity.finish()
    }

    override fun currentRoute(): String? = null
}

