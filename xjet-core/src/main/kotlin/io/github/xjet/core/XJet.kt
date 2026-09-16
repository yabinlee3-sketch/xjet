package io.github.xjet.core

import android.content.Context
import android.util.Log
import io.github.xjet.spi.SpiConfigLoader
import io.github.xjet.spi.SpiRegistry
import java.io.IOException

/**
 * Single public entry point of XJet.
 *
 * ```
 * XJet.init(this, XJetConfig(this))
 * ```
 *
 * All capabilities are reachable through this object. Layering rule:
 * higher layers depend on [XJet] and never reach into Android internals.
 */
object XJet {

    @Volatile private var registry: SpiRegistry? = null
    @Volatile private var debug = false
    @Volatile private var routeRegistry: RouteRegistry = RouteRegistry()
    @Volatile internal var logTag = "XJet"

    /** Application context captured during [init]. */
    @Volatile
    var appContext: Context? = null
        private set

    val isInitialized: Boolean get() = registry != null

    fun init(context: Context, config: XJetConfig = XJetConfig(context)) {
        synchronized(this) {
            if (isInitialized) return
            this.appContext = context.applicationContext
            this.debug = config.debug
            this.logTag = config.logTag

            val reg = SpiRegistry()
            val routes = RouteRegistry()
            routeRegistry = routes

            // 1) Explicit programmatic choices (via XJetConfig) always win.
            config.database?.let { reg.register(DatabaseProvider::class.java, it, override = true) }
            config.cache?.let { reg.register(CacheProvider::class.java, it, override = true) }
            config.eventBus?.let { reg.register(EventBusProvider::class.java, it, override = true) }
            config.router?.let { reg.register(RouterProvider::class.java, it, override = true) }
            config.imageLoader?.let { reg.register(ImageLoaderProvider::class.java, it, override = true) }

            // 2) Built-in defaults act as fallbacks and may be replaced later by the
            //    config asset or compile-time @SpiService registrations.
            if (!reg.has(CacheProvider::class.java)) {
                reg.register(CacheProvider::class.java, InMemoryCacheProvider(), override = false)
            }
            if (!reg.has(EventBusProvider::class.java)) {
                reg.register(EventBusProvider::class.java, SharedFlowEventBus(), override = false)
            }
            if (!reg.has(RouterProvider::class.java)) {
                reg.register(RouterProvider::class.java, SimpleRouterProvider(context.applicationContext, routes), override = false)
            }

            // 3) Config-asset automatic discovery (can replace built-in defaults).
            if (config.autoDiscoverConfig) {
                discoverAssetConfig(context.applicationContext, config.spiAssetPath, reg)
            }

            // 4) Compile-time @SpiService / @XRoute via KSP.
            loadGenerated(reg, routes)

            registry = reg
            config.onInitialized?.invoke(this)
        }
    }

    private fun discoverAssetConfig(context: Context, assetsPath: String, reg: SpiRegistry) {
        try {
            context.assets.open(assetsPath).use { stream ->
                SpiConfigLoader.load(reg, stream, override = true)
                if (debug) Log.d(logTag, "SPI config loaded from assets/$assetsPath")
            }
        } catch (ioe: IOException) {
            if (debug) Log.d(logTag, "No SPI config asset at $assetsPath (normal when unused)")
        } catch (t: Throwable) {
            if (debug) Log.w(logTag, "Failed to load SPI config $assetsPath", t)
        }
    }

    private fun loadGenerated(reg: SpiRegistry, routes: RouteRegistry) {
        try {
            val clazz = Class.forName("io.github.xjet.generated.XJetGeneratedSpi")
            clazz.getMethod("registerAll", SpiRegistry::class.java).invoke(null, reg)
            val routeList = clazz.getMethod("routes").invoke(null) as? java.util.List<*> ?: emptyList<Any>()
            routeList.forEach { item ->
                if (item is RouteDescriptor) routes.register(item, override = true)
            }
            if (debug) Log.d(logTag, "XJet KSP-generated SPI + routes installed")
        } catch (_: ClassNotFoundException) {
            // Optional; means no annotations were compiled into this app
        } catch (t: Throwable) {
            if (debug) Log.w(logTag, "Failed to load XJetGeneratedSpi", t)
        }
    }

    // ---- registration ----------------------------------------------------

    @JvmStatic
    fun <T : Any> register(api: Class<T>, implementation: T, override: Boolean = false) {
        requireRegistry().register(api, implementation, override)
    }

    @JvmStatic
    fun <T : Any> registerProvider(api: Class<T>, provider: io.github.xjet.spi.ServiceProvider<T>, override: Boolean = false) {
        requireRegistry().registerProvider(api, provider, override)
    }

    /** Replaces a default implementation with a subclass that overrides part of it. */
    @JvmStatic
    fun <T : Any> override(api: Class<T>, implementation: T) = register(api, implementation, override = true)

    // ---- lookup ----------------------------------------------------------

    @JvmStatic
    fun <T : Any> getOrNull(api: Class<T>): T? = requireRegistry().getOrNull(api)

    @JvmStatic
    fun <T : Any> get(api: Class<T>): T = requireRegistry().get(api)

    fun database(): DatabaseProvider = get(DatabaseProvider::class.java)
    fun cache(): CacheProvider = get(CacheProvider::class.java)
    fun eventBus(): EventBusProvider = get(EventBusProvider::class.java)
    fun router(): RouterProvider = get(RouterProvider::class.java)
    fun imageLoader(): ImageLoaderProvider? = getOrNull(ImageLoaderProvider::class.java)

    /** Route table collected from @XRoute + runtime registration. */
    fun routes(): RouteRegistry = routeRegistry

    private fun requireRegistry(): SpiRegistry =
        registry ?: throw IllegalStateException("XJet.init(...) must be called before accessing any capability.")
}

