package io.github.xjet.annotation

import kotlin.reflect.KClass

/**
 * Marks a class as an SPI implementation of [api] so the XJet KSP processor
 * generates a registration entry at compile time.
 *
 * The framework intentionally stays free of third-party DI so consumers can
 * swap the implementation without touching the framework source.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class SpiService(
    /** The public API interface this class implements. */
    val api: KClass<*>,
    /** When `true`, this implementation replaces an already-registered one. */
    val override: Boolean = false,
    /** Optional name used by config-file/asset discovery and logs. */
    val name: String = ""
)
