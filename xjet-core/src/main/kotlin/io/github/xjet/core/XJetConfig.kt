package io.github.xjet.core

import android.content.Context

/** Configuration passed once to [XJet.init]. */
data class XJetConfig(
    val context: Context,
    val debug: Boolean = false,
    val autoDiscoverConfig: Boolean = true,
    val spiAssetPath: String = SpiConfigPaths.ANDROID_ASSET_PATH,
    val database: DatabaseProvider? = null,
    val cache: CacheProvider? = null,
    val eventBus: EventBusProvider? = null,
    val router: RouterProvider? = null,
    val imageLoader: ImageLoaderProvider? = null,
    val logTag: String = "XJet",
    val onInitialized: ((XJet) -> Unit)? = null,
) {
    class Builder(private val context: Context) {
        private var debug = false
        private var autoDiscoverConfig = true
        private var spiAssetPath = SpiConfigPaths.ANDROID_ASSET_PATH
        private var database: DatabaseProvider? = null
        private var cache: CacheProvider? = null
        private var eventBus: EventBusProvider? = null
        private var router: RouterProvider? = null
        private var imageLoader: ImageLoaderProvider? = null
        private var logTag = "XJet"
        private var onInitialized: ((XJet) -> Unit)? = null

        fun debug(value: Boolean) = apply { debug = value }
        fun autoDiscoverConfig(value: Boolean) = apply { autoDiscoverConfig = value }
        fun spiAssetPath(value: String) = apply { spiAssetPath = value }
        fun database(value: DatabaseProvider?) = apply { database = value }
        fun cache(value: CacheProvider?) = apply { cache = value }
        fun eventBus(value: EventBusProvider?) = apply { eventBus = value }
        fun router(value: RouterProvider?) = apply { router = value }
        fun imageLoader(value: ImageLoaderProvider?) = apply { imageLoader = value }
        fun logTag(value: String) = apply { logTag = value }
        fun onInitialized(value: (XJet) -> Unit) = apply { onInitialized = value }
        fun build() = XJetConfig(
            context = context,
            debug = debug,
            autoDiscoverConfig = autoDiscoverConfig,
            spiAssetPath = spiAssetPath,
            database = database,
            cache = cache,
            eventBus = eventBus,
            router = router,
            imageLoader = imageLoader,
            logTag = logTag,
            onInitialized = onInitialized,
        )
    }
}

object SpiConfigPaths {
    const val JVM_CONFIG_PATH = "/META-INF/xjet/spi.properties"
    const val ANDROID_ASSET_PATH = "xjet/spi.properties"
}
