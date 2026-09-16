package io.github.xjet.xml

import android.app.Activity
import android.os.Bundle

/**
 * Classic (XML) base Activity. It stays intentionally thin so the framework
 * doesn't force any UI toolkit; hook lifecycle if you need it, or use the
 * lifecycle-aware Compose module for StateFlow collection.
 */
open class XJetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /** Convenience: navigate using the framework router. */
    fun navigate(route: String, args: Map<String, Any> = emptyMap()) {
        io.github.xjet.core.XJet.router().navigate(route, args)
    }
}
