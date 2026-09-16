package io.github.xjet.spi

import java.io.ByteArrayInputStream
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

interface GreeterApi {
    fun greet(name: String): String
}

class DefaultGreeter : GreeterApi {
    override fun greet(name: String): String = "Hello, $name"
}

class LoudGreeter : GreeterApi {
    override fun greet(name: String): String = "HELLO $name!"
}

class NoArgCounter {
    companion object { @Volatile var created = 0 }
    init { NoArgCounter.created++ }
}

interface CounterApi

class CounterImpl : CounterApi {
    constructor() { }
}

class SpiRegistryTest {

    @Test
    fun registerAndGetReturnsSameInstance() {
        val registry = SpiRegistry()
        val instance = DefaultGreeter()
        registry.register(GreeterApi::class.java, instance)
        assertTrue(registry.getOrNull(GreeterApi::class.java) === instance)
        assertEquals("Hello, XJet", registry.get(GreeterApi::class.java).greet("XJet"))
    }

    @Test
    fun duplicateRegistrationRequiresOverride() {
        val registry = SpiRegistry()
        registry.register(GreeterApi::class.java, DefaultGreeter())
        assertFailsWith<IllegalStateException> {
            registry.register(GreeterApi::class.java, LoudGreeter())
        }
        registry.register(GreeterApi::class.java, LoudGreeter(), override = true)
        assertEquals("HELLO World!", registry.get(GreeterApi::class.java).greet("World"))
    }

    @Test
    fun configLoaderInstantiatesNoArgImpls() {
        val registry = SpiRegistry()
        val data = "io.github.xjet.spi.CounterApi=" +
            "io.github.xjet.spi.NoArgCounter\n"
        val bytes = data.toByteArray(Charsets.UTF_8)
        val registered = SpiConfigLoader.load(registry, ByteArrayInputStream(bytes))
        assertEquals(listOf<Class<*>>(CounterApi::class.java), registered)
        assertTrue(registry.has(CounterApi::class.java))
        registry.get(CounterApi::class.java)
        assertTrue(NoArgCounter.created >= 1)
    }

    @Test
    fun unregisterRemovesProvider() {
        val registry = SpiRegistry()
        registry.register(GreeterApi::class.java, DefaultGreeter())
        registry.unregister(GreeterApi::class.java)
        assertFalse(registry.has(GreeterApi::class.java))
    }

    @Test
    fun overrideOnlyReplacesForSameApi() {
        val registry = SpiRegistry()
        registry.register(GreeterApi::class.java, DefaultGreeter(), override = true)
        registry.register(GreeterApi::class.java, LoudGreeter(), override = true)
        assertEquals("HELLO hi!", registry.get(GreeterApi::class.java).greet("hi"))
    }
}

