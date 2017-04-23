package org.jetbrains.ktor.locations

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.util.*

@Deprecated("Seems to be too risky as host/port are not initialized")
fun Application.url(location: Any, block: URLBuilder.() -> Unit = {}): String = url {
    feature(Locations).href(location, this)
    block()
}

fun ApplicationCall.url(location: Any, block: URLBuilder.() -> Unit = {}): String = url {
    parameters.clear()
    application.feature(Locations).href(location, this)
    block()
}

