package io.github.xjet.spi

import java.util.logging.Level
import java.util.logging.Logger

/** Severity bucket used when an exception crosses a framework boundary. */
enum class ErrorSeverity { INFO, WARNING, ERROR }

/**
 * Global exception interceptor (SPI contract).
 *
 * Implementing apps can route every captured exception to a crash reporter
 * (Crashlytics, Sentry, custom analytics...) by replacing this via
 * `XJetConfig.errorInterceptor`, `XJet.override(...)`, a config asset, or a
 * compile-time `@SpiService`. The framework only declares the contract and
 * never assumes a concrete reporting backend.
 */
interface ExceptionInterceptor {
    fun onError(source: String, throwable: Throwable, severity: ErrorSeverity)

    /** Convenience overload that defaults to [ErrorSeverity.ERROR]. */
    fun onError(source: String, throwable: Throwable) =
        onError(source, throwable, ErrorSeverity.ERROR)
}

/** Dependency-free default that logs to JUL. Replace it with a reporter. */
class LogExceptionInterceptor : ExceptionInterceptor {
    private val logger = Logger.getLogger("XJet.ExceptionInterceptor")

    override fun onError(source: String, throwable: Throwable, severity: ErrorSeverity) {
        val level = when (severity) {
            ErrorSeverity.INFO -> Level.INFO
            ErrorSeverity.WARNING -> Level.WARNING
            ErrorSeverity.ERROR -> Level.SEVERE
        }
        logger.log(level, "[$source] ${throwable.message}", throwable)
    }
}

/** Runs [block] and funnels any exception to [this] interceptor, returning null on failure. */
inline fun <T> ExceptionInterceptor.tryCatch(
    source: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    block: () -> T,
): T? {
    return try {
        block()
    } catch (t: Throwable) {
        onError(source, t, severity)
        null
    }
}


