package io.github.xjet.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.xjet.core.OneShotEvent
import io.github.xjet.core.RouteDescriptor
import io.github.xjet.core.RouteRegistry
import io.github.xjet.core.XJet

@Composable
fun XJetTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

/** Collect a [RouteRegistry]-driven set of composable destinations. */
fun NavGraphBuilder.xJetComposeRoutes(
    registry: RouteRegistry = XJet.routes(),
    content: @Composable (RouteDescriptor) -> Unit,
) {
    registry.all().forEach { descriptor ->
        composable(route = descriptor.path) { content(descriptor) }
    }
}

/** Collect one-shot framework events as a composable effect. */
@Composable
fun <T> OneShotEvent<T>.collectAsEffect(onEvent: (T) -> Unit) {
    val flow = asFlow()
    LaunchedEffect(this) {
        flow.collect(onEvent)
    }
}
