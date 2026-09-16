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


/**
 * SharedPreferences-backed [CacheProvider]. Handy for tiny persistent key/value
 * settings. Optional — swap in at init via XJetConfig.cache(...).
 */
class SharedPrefsCacheProvider(
    context: Context,
    private val spName: String = "xjet_cache",
) : CacheProvider {
    private val sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE)

    override fun get(key: String): String? = sp.getString(key, null)
    override fun put(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }
    override fun getBytes(key: String): ByteArray? {
        val encoded = sp.getString(key, null) ?: return null
        return android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP)
    }
    override fun putBytes(key: String, value: ByteArray) {
        sp.edit().putString(key, android.util.Base64.encodeToString(value, android.util.Base64.NO_WRAP)).apply()
    }
    override fun remove(key: String) {
        sp.edit().remove(key).apply()
    }
    override fun clear() {
        sp.edit().clear().apply()
    }
}

/**
 * Simple file-backed [CacheProvider] (application cache dir). Optional — swap in
 * at init via XJetConfig.cache(...) when persistence is needed.
 */
class FileCacheProvider(
    context: Context,
    private val dirName: String = "xjet_cache",
    maxBytes: Long = 16L * 1024 * 1024,
) : CacheProvider {
    private val dir = java.io.File(context.cacheDir, dirName).apply { mkdirs() }

    override fun get(key: String): String? = read(file(key))
    override fun put(key: String, value: String) = write(file(key), value.toByteArray(Charsets.UTF_8))
    override fun getBytes(key: String): ByteArray? = readBytes(file(key))
    override fun putBytes(key: String, value: ByteArray) = write(file(key), value)
    override fun remove(key: String) { file(key).delete() }
    override fun clear() { dir.listFiles()?.forEach { it.delete() } }

    private fun file(key: String) = java.io.File(dir, safeName(key))
    private fun safeName(key: String) = key.hashCode().toString(16) + "_" + key.replace(Regex("[^a-zA-Z0-9._-]"), "_")

    private fun write(file: java.io.File, bytes: ByteArray) {
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
    }
    private fun read(file: java.io.File): String? = try { file.readText(Charsets.UTF_8) } catch (_: Exception) { null }
    private fun readBytes(file: java.io.File): ByteArray? = try { file.readBytes() } catch (_: Exception) { null }
}

