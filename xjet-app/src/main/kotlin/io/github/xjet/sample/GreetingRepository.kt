package io.github.xjet.sample

import io.github.xjet.core.XJet
import io.github.xjet.core.XRepository

data class HomeGreetingData(
    val greeting: String = "",
    val config: String = "",
)

/** Repository layer: talks to the framework's injected services, never an Activity. */
class GreetingRepository : XRepository() {

    suspend fun load(): HomeGreetingData = safe("greeting.load") {
        HomeGreetingData(
            greeting = XJet.get(GreetingService::class.java).greet(),
            config = XJet.getOrNull(ConfigGreetingService::class.java)?.greet() ?: "config-not-found",
        )
    }
}
