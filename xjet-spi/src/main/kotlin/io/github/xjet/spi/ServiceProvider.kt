package io.github.xjet.spi

/**
 * A lazy factory for a single SPI instance. Providers keep the framework
 * dependency-free: each feature owns its interface while a `@SpiService`
 * implementation (or a config asset) supplies the default wiring.
 */
interface ServiceProvider<out T> {
    fun create(): T
}

/** Convenience factory for already constructed instances. */
class InstanceProvider<T : Any>(private val instance: T) : ServiceProvider<T> {
    override fun create(): T = instance
}

/** Convenience factory for no-arg constructors. */
class NewInstanceProvider<T : Any>(private val clazz: Class<T>) : ServiceProvider<T> {
    override fun create(): T {
        return clazz.getDeclaredConstructor().newInstance()
    }
}

