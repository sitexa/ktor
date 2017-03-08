package org.jetbrains.ktor.websocket

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.jetty.*

class JettyWebSocketTest : WebSocketHostSuite<JettyApplicationHost>() {
    override fun createServer(envInit: ApplicationEnvironmentBuilder.() -> Unit, application: Application.() -> Unit): JettyApplicationHost {
        val _port = this.port

        val hostConfig = applicationHostConfig {
            connector {
                port = _port
            }
        }
        val environmentConfig = applicationEnvironment(envInit)

        return embeddedJettyServer(hostConfig, environmentConfig, application)
    }
}
