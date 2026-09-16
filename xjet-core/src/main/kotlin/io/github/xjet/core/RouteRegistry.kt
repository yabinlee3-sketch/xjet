package io.github.xjet.core

import java.util.concurrent.ConcurrentHashMap

/**
 * Compile-time/first-class route table. Entries come from `@XRoute` via the
 * generated `XJetGeneratedSpi` and can be extended at runtime.
 */
class RouteRegistry {
    private val routes = ConcurrentHashMap<String, RouteDescriptor>()

    fun register(descriptor: RouteDescriptor, override: Boolean = false) {
        val previous = routes.putIfAbsent(descriptor.path, descriptor)
        if (previous != null) {
            if (!override) {
                throw IllegalStateException("Route '${descriptor.path}' already registered as ${previous.targetClassName}")
            }
            routes[descriptor.path] = descriptor
        }
    }

    fun resolve(route: String): RouteDescriptor? = routes[route]

    fun all(): List<RouteDescriptor> = routes.values.sortedBy { it.path }

    fun byGroup(group: String): List<RouteDescriptor> = all().filter { it.group == group }

    fun has(route: String): Boolean = routes.containsKey(route)

    fun clear() = routes.clear()
}
