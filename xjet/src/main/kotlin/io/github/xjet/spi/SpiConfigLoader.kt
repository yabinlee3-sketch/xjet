package io.github.xjet.spi

import java.io.InputStream
import java.util.Properties

/**
 * Config-file based discovery ("配置" registration mode).
 *
 * The file is a standard `.properties` file whose keys are the fully
 * qualified public API and whose values are the fully qualified
 * implementation class. Implementations must expose a public no-arg
 * constructor.
 *
 *   io.github.xjet.core.DatabaseProvider = io.github.xjet.room.RoomDatabaseProvider
 */
object SpiConfigLoader {

    const val JVM_CONFIG_PATH = "/META-INF/xjet/spi.properties"
    const val ANDROID_ASSET_PATH = "xjet/spi.properties"

    fun load(registry: SpiRegistry, input: InputStream, override: Boolean = true): List<Class<*>> {
        val properties = Properties()
        properties.load(input)
        return register(registry, properties, override)
    }

    fun register(registry: SpiRegistry, properties: Properties, override: Boolean = true): List<Class<*>> {
        val registered = mutableListOf<Class<*>>()
        properties.stringPropertyNames().sorted().forEach { apiName ->
            val implName = properties.getProperty(apiName).trim()
            if (implName.isBlank()) return@forEach
            val api = Class.forName(apiName.trim())
            val clazz = Class.forName(implName)
            @Suppress("UNCHECKED_CAST")
            registry.registerClass(api as Class<Any>, clazz as Class<out Any>, override)
            registered += api
        }
        return registered
    }

    fun loadFromClasspath(registry: SpiRegistry, path: String = JVM_CONFIG_PATH): List<Class<*>> {
        val stream = SpiConfigLoader::class.java.getResourceAsStream(path) ?: return emptyList()
        return stream.use { load(registry, it) }
    }
}
