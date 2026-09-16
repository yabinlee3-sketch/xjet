package io.github.xjet.spi

import java.util.concurrent.ConcurrentHashMap

/**
 * Dependency-free service registry used by the whole framework.
 *
 * Registration order:
 *  1. code  : XJet.register / SpiRegistry.register
 *  2. assets: config-file discovery (META-INF/xjet/spi.properties on JVM,
 *             assets/xjet/spi.properties on Android)
 *  3. KSP   : generated XJetGeneratedSpi.registerAll()
 *
 * An implementation may be replaced via [register] with [override] = true,
 * which is the framework's extend-by-subclass escape hatch: consumers subclass
 * a default implementation and re-register it against the same API.
 */
class SpiRegistry {

    private val providers = ConcurrentHashMap<Class<*>, ServiceProvider<Any>>()

    /** Registers a concrete instance. */
    fun <T : Any> register(api: Class<T>, implementation: T, override: Boolean = false) {
        put(api, InstanceProvider(implementation), override)
    }

    /** Registers a lazy provider. */
    fun <T : Any> registerProvider(api: Class<T>, provider: ServiceProvider<T>, override: Boolean = false) {
        put(api, provider as ServiceProvider<Any>, override)
    }

    /** Registers a provider loaded reflectively from [implementationClass]. */
    fun <T : Any> registerClass(api: Class<T>, implementationClass: Class<out T>, override: Boolean = true) {
        put(api, NewInstanceProvider(implementationClass as Class<T>) as ServiceProvider<Any>, override)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(api: Class<T>): T? {
        val provider = providers[api] ?: return null
        return provider.create() as T
    }

    fun <T : Any> get(api: Class<T>): T {
        return getOrNull(api) ?: throw IllegalStateException(
            "No SPI implementation registered for ${api.canonicalName}. " +
                "Register one via XJet.register(...), a spi config asset, or add the KSP annotation @SpiService."
        )
    }

    fun <T : Any> has(api: Class<T>): Boolean = providers.containsKey(api)

    fun unregister(api: Class<*>) {
        providers.remove(api)
    }

    fun keys(): Set<Class<*>> = providers.keys.toSet()

    fun size(): Int = providers.size

    private fun put(api: Class<*>, provider: ServiceProvider<Any>, override: Boolean) {
        val previous = providers.putIfAbsent(api, provider)
        if (previous != null) {
            if (!override) {
                throw IllegalStateException(
                    "SPI ${api.canonicalName} is already registered. Pass override = true to replace the default implementation."
                )
            }
            providers[api] = provider
        }
    }
}
