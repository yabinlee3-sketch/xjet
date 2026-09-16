package io.github.xjet.core

import android.content.Context
import android.util.Log
import io.github.xjet.spi.ErrorSeverity
import io.github.xjet.spi.ExceptionInterceptor
import io.github.xjet.spi.LogExceptionInterceptor
import io.github.xjet.spi.SpiConfigLoader
import io.github.xjet.spi.SpiRegistry
import io.github.xjet.spi.tryCatch
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
            XJetLog.init(this.debug, this.logTag)

            val reg = SpiRegistry()
            val routes = RouteRegistry()
            routeRegistry = routes

            // 1) Explicit programmatic choices (via XJetConfig) always win.
            config.database?.let { reg.register(DatabaseProvider::class.java, it, override = true) }
            config.cache?.let { reg.register(CacheProvider::class.java, it, override = true) }
            config.eventBus?.let { reg.register(EventBusProvider::class.java, it, override = true) }
            config.router?.let { reg.register(RouterProvider::class.java, it, override = true) }
            config.imageLoader?.let { reg.register(ImageLoaderProvider::class.java, it, override = true) }
            config.errorInterceptor?.let { reg.register(ExceptionInterceptor::class.java, it, override = true) }

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
            if (!reg.has(HttpProvider::class.java)) {
                reg.register(HttpProvider::class.java, JdkHttpProvider(), override = false)
            }

            if (!reg.has(ImageLoaderProvider::class.java)) {
                reg.register(ImageLoaderProvider::class.java, AndroidImageLoaderProvider(), override = false)
            }

            if (!reg.has(ExceptionInterceptor::class.java)) {
                reg.register(ExceptionInterceptor::class.java, LogExceptionInterceptor(), override = false)
            }

            // 3) Config-asset automatic discovery (can replace built-in defaults).
            if (config.autoDiscoverConfig) {
                discoverAssetConfig(context.applicationContext, config.spiAssetPath, reg)
            }

            // 4) Compile-time @SpiService / @XRoute via KSP.
            loadGenerated(reg, routes)

            registry = reg

            if (config.installUncaughtErrorHandler) {
                installUncaughtErrorHandler(reg.get(ExceptionInterceptor::class.java))
            }

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

    private fun installUncaughtErrorHandler(interceptor: ExceptionInterceptor) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            interceptor.onError("uncaught:${thread.name}", throwable)
            previous?.uncaughtException(thread, throwable)
        }
        if (debug) Log.d(logTag, "Global uncaught error handler installed")
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
    fun http(): HttpProvider = get(HttpProvider::class.java)

    fun errorInterceptor(): ExceptionInterceptor = get(ExceptionInterceptor::class.java)

    /** Report an exception to the global interceptor without rethrowing. */
    fun capture(source: String, throwable: Throwable, severity: ErrorSeverity = ErrorSeverity.ERROR) {
        errorInterceptor().onError(source, throwable, severity)
    }

    /** Run [block] and funnel any exception to the global interceptor, returning null on failure. */
    fun <T> tryCatch(source: String, severity: ErrorSeverity = ErrorSeverity.ERROR, block: () -> T): T? {
        return errorInterceptor().tryCatch(source, severity, block)
    }

    /** Route table collected from @XRoute + runtime registration. */
    fun routes(): RouteRegistry = routeRegistry

    private fun requireRegistry(): SpiRegistry =
        registry ?: throw IllegalStateException("XJet.init(...) must be called before accessing any capability.")
}
