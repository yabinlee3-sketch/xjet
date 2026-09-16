package io.github.xjet.core

/**
 * MVVM data layer base. A repository only talks to the framework's injected
 * sources ([XJet] database / cache / http / services); it holds no Activity,
 * no Compose and no ViewModel. Combine it with [XViewModel.refresh].
 */
abstract class XRepository {

    protected val xjet: XJet get() = XJet

    /** Report to the global [XJet.capture] interceptor without rethrowing. */
    protected fun capture(source: String, throwable: Throwable) {
        XJet.capture(source, throwable)
    }

    /** Run a suspend [block] and rethrow; failures are reported first. */
    protected suspend fun <T> safe(source: String, block: suspend () -> T): T {
        return try {
            block()
        } catch (t: Throwable) {
            capture(source, t)
            throw t
        }
    }
}
