package org.jetbrains.ktor.tomcat

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.host.*

fun embeddedTomcatServer(port: Int = 80, host: String = "0.0.0.0", application: Application.() -> Unit): TomcatApplicationHost {
    val hostConfig = applicationHostConfig {
        connector {
            this.port = port
            this.host = host
        }
    }
    val applicationConfig = applicationEnvironment {}
    return embeddedTomcatServer(hostConfig, applicationConfig, application)
}

fun embeddedTomcatServer(hostConfig: ApplicationHostConfig, environment: ApplicationEnvironment, application: Application.() -> Unit): TomcatApplicationHost {
    val applicationObject = Application(environment, Unit).apply {
        application()
    }
    return TomcatApplicationHost(hostConfig, environment, object : ApplicationLifecycle {
        override val application: Application = applicationObject
        override fun onBeforeInitializeApplication(initializer: Application.() -> Unit) {
            applicationObject.initializer()
        }
        override fun dispose() {}
    })
}


