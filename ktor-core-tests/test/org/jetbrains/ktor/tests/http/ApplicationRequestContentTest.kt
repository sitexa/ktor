package org.jetbrains.ktor.tests.http

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.testing.*
import org.jetbrains.ktor.tests.*
import org.jetbrains.ktor.util.*
import org.junit.*
import java.io.*
import kotlin.test.*

class ApplicationRequestContentTest {
    @Test
    fun testSimpleStringContent() {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) { call ->
                assertEquals("bodyContent", call.request.receive<String>())
            }

            handleRequest(HttpMethod.Get, "") {
                body = "bodyContent"
            }
        }
    }

    @Test
    fun testValuesMap() {
        withTestApplication {
            val values = valuesOf("a" to listOf("1"))

            application.intercept(ApplicationCallPipeline.Call) { call ->
                assertEquals(values, call.request.receive<ValuesMap>())
            }

            handleRequest(HttpMethod.Get, "") {
                method = HttpMethod.Post
                addHeader(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
                body = values.formUrlEncode()
            }
        }
    }

    @Test
    fun testInputStreamContent() {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) { call ->
                assertEquals("bodyContent", call.request.receive<InputStream>().reader(Charsets.UTF_8).readText())
            }

            handleRequest(HttpMethod.Get, "") {
                body = "bodyContent"
            }
        }
    }

    @Test
    fun testCustomTransform() {
        withTestApplication {
            val value = IntList(listOf(1, 2, 3, 4))

            application.intercept(ApplicationCallPipeline.Infrastructure) {
                call.request.pipeline.intercept(ApplicationRequestPipeline.Transform) { query ->
                    if (query.type != IntList::class) return@intercept

                    val string = call.request.pipeline.execute(ApplicationReceiveRequest(String::class, query.value)).value as? String
                    if (string != null) {
                        val transformed = IntList.parse(string)
                        proceedWith(ApplicationReceiveRequest(Nothing::class, transformed))
                    }
                }
            }

            application.intercept(ApplicationCallPipeline.Call) { call ->
                assertEquals(value, call.request.receive<IntList>())
            }

            handleRequest(HttpMethod.Get, "") {
                body = value.toString()
            }
        }
    }

}

data class IntList(val values: List<Int>) {
    override fun toString() = "$values"
    companion object {
        fun parse(text: String) = IntList(text.removeSurrounding("[", "]").split(",").map { it.trim().toInt() })
    }
}
