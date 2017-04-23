package org.jetbrains.ktor.tests.http

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.cio.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.testing.*
import org.jetbrains.ktor.transform.*
import org.jetbrains.ktor.util.*
import org.junit.*
import kotlin.test.*

class StatusPageTest {
    @Test
    fun testStatusMapping() {
        withTestApplication {
            application.install(StatusPages) {
                statusFile(HttpStatusCode.NotFound, filePattern = "error#.html")
            }
            application.intercept(ApplicationCallPipeline.Call) { call ->
                call.respond(HttpStatusCode.NotFound)
            }
            handleRequest(HttpMethod.Get, "/foo").let { call ->
                assertEquals("<html><body>error 404</body></html>", call.response.content)
            }
        }
    }

    @Test
    fun testStatus404() {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Fallback) { call ->
                call.respond(HttpStatusCode.NotFound)
            }

            application.install(StatusPages) {
                status(HttpStatusCode.NotFound) {
                    call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
                }
            }

            application.routing {
                get("/") {
                    call.respond("ok")
                }
                get("/notFound") {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertEquals("ok", call.response.content)
            }

            handleRequest(HttpMethod.Get, "/missing").let { call ->
                assertEquals("404 ${HttpStatusCode.NotFound.description}", call.response.content)
            }

            handleRequest(HttpMethod.Get, "/notFound").let { call ->
                assertEquals("404 ${HttpStatusCode.NotFound.description}", call.response.content)
            }
        }
    }

    @Test
    fun testStatus404CustomObject() {
        withTestApplication {
            application.install(StatusPages) {
                status(HttpStatusCode.NotFound) {
                    call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
                }
            }

            application.intercept(ApplicationCallPipeline.Call) {
                call.respond(object : FinalContent.ReadChannelContent() {
                    override val status = HttpStatusCode.NotFound

                    override val headers: ValuesMap
                        get() = ValuesMap.Empty

                    override fun readFrom(): ReadChannel = fail("Should never reach here")
                })
            }

            handleRequest(HttpMethod.Get, "/missing").let { call ->
                assertEquals("404 ${HttpStatusCode.NotFound.description}", call.response.content)
            }
        }
    }

    @Test
    fun testStatus404WithTransform() {
        class O

        withTestApplication {
            application.intercept(ApplicationCallPipeline.Fallback) { call ->
                call.respond(HttpStatusCode.NotFound)
            }

            application.install(StatusPages) {
                status(HttpStatusCode.NotFound) {
                    call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
                }
            }

            application.transform.register<O> { HttpStatusCode.NotFound }
            application.routing {
                get("/") {
                    call.respond(O())
                }
            }

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertEquals("404 ${HttpStatusCode.NotFound.description}", call.response.content)
            }
        }
    }

    @Test
    fun testFailPage() {
        withTestApplication {
            application.install(StatusPages) {
                exception<Throwable> { cause ->
                    call.respond(TextContent(cause::class.java.simpleName, ContentType.Text.Plain.withCharset(Charsets.UTF_8), HttpStatusCode.InternalServerError))
                }
            }

            application.routing {
                get("/iae") {
                    throw IllegalArgumentException()
                }
                get("/npe") {
                    throw NullPointerException()
                }
            }

            handleRequest(HttpMethod.Get, "/iae").let { call ->
                assertEquals("IllegalArgumentException", call.response.content)
            }

            handleRequest(HttpMethod.Get, "/npe").let { call ->
                assertEquals("NullPointerException", call.response.content)
            }
        }
    }

    @Test
    fun testFailPageDuringTransform() {
        class O

        withTestApplication {
            application.transform.register<O> {
                throw IllegalStateException()
            }

            application.install(StatusPages) {
                exception<IllegalStateException> { cause ->
                    call.respond(TextContent(cause::class.java.simpleName, ContentType.Text.Plain.withCharset(Charsets.UTF_8), HttpStatusCode.InternalServerError))
                }
            }

            application.routing {
                get("/") {
                    call.respond(O())
                }
            }

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertEquals("IllegalStateException", call.response.content)
            }
        }
    }

    @Test
    fun testErrorDuringStatus() {
        withTestApplication {
            application.install(StatusPages) {
                status(HttpStatusCode.NotFound) {
                    throw IllegalStateException("")
                }
                exception<Throwable> { cause ->
                    call.respond(TextContent(cause::class.java.simpleName, ContentType.Text.Plain.withCharset(Charsets.UTF_8), HttpStatusCode.InternalServerError))
                }
            }

            application.intercept(ApplicationCallPipeline.Fallback) { call ->
                call.respond(HttpStatusCode.NotFound)
            }

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertEquals("IllegalStateException", call.response.content)
            }
        }
    }

    @Test
    fun testErrorShouldNotRecurse() {
        withTestApplication {
            application.install(StatusPages) {
                exception<IllegalStateException> {
                    throw IllegalStateException()
                }
            }

            application.intercept(ApplicationCallPipeline.Fallback) {
                throw NullPointerException()
            }

            assertFails {
                handleRequest(HttpMethod.Get, "/")
            }
        }
    }
}