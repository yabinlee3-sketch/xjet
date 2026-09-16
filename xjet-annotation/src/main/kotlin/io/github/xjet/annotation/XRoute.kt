package io.github.xjet.annotation

/**
 * Declares a navigation destination. The KSP processor collects these into a
 * generated route table that [io.github.xjet.core.XJet.router] can resolve.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class XRoute(
    /** Stable route path, for example `"home"` or `"order/:id"`. */
    val path: String,
    /** Route group used to namespace tables: `"comp"`, `"xml"`, `"demo"`. */
    val group: String = "app",
    /** Optional human readable title used by nav graphs / logging. */
    val title: String = ""
)
