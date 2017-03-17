package org.jetbrains.ktor.host

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.cio.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.request.*
import org.jetbrains.ktor.util.*
import java.io.*
import kotlin.reflect.*

abstract class BaseApplicationRequest() : ApplicationRequest {
    override val pipeline = ApplicationRequestPipeline()

    init {
        // Transform request itself into one of three base types
        pipeline.intercept(ApplicationRequestPipeline.Transform) { query ->
            val value = query.value as? ApplicationRequest ?: return@intercept
            val transformed: Any = when (query.type) {
                ReadChannel::class -> getReadChannel()
                InputStream::class -> getInputStream()
                MultiPartData::class -> getMultiPartData()
                else -> return@intercept
            }
            proceedWith(ApplicationReceiveRequest(Nothing::class, transformed))
        }

        // Get InputStream from the same pipeline and transform it into a String
        pipeline.intercept(ApplicationRequestPipeline.Transform) { query ->
            if (query.type != String::class) return@intercept

            val stream = pipeline.execute(ApplicationReceiveRequest(InputStream::class, query.value)).value as? InputStream
            if (stream != null) {
                val transformed = stream.reader(contentCharset() ?: Charsets.ISO_8859_1).readText()
                proceedWith(ApplicationReceiveRequest(Nothing::class, transformed))
            }
        }

        // If FormUrlEncoded, get String from the same pipeline and transform it into a ValuesMap
        pipeline.intercept(ApplicationRequestPipeline.Transform) { query ->
            if (query.type != ValuesMap::class) return@intercept

            if (contentType().match(ContentType.Application.FormUrlEncoded)) {
                val string = pipeline.execute(ApplicationReceiveRequest(String::class, query.value)).value as? String
                if (string != null) {
                    val transformed = parseQueryString(string)
                    proceedWith(ApplicationReceiveRequest(Nothing::class, transformed))
                }
            }
        }

        // If FormData, get String from the same pipeline and transform it into a ValuesMap
        pipeline.intercept(ApplicationRequestPipeline.Transform) { query ->
            if (query.type != ValuesMap::class) return@intercept

            if (contentType().match(ContentType.MultiPart.FormData)) {
                val multipart = pipeline.execute(ApplicationReceiveRequest(MultiPartData::class, query.value)).value as? MultiPartData
                if (multipart != null) {
                    val transformed = ValuesMap.build {
                        multipart.parts.filterIsInstance<PartData.FormItem>().forEach { part ->
                            part.partName?.let { name ->
                                append(name, part.value)
                            }
                        }
                    }
                    proceedWith(ApplicationReceiveRequest(Nothing::class, transformed))
                }
            }
        }

        // If we were asked for ValuesMap and none of prior transformer could find it, use empty
        // TODO: remove this, because it is weird
        pipeline.intercept(ApplicationRequestPipeline.Transform) { query ->
            if (query.type != ValuesMap::class) return@intercept
            proceedWith(ApplicationReceiveRequest(Nothing::class, ValuesMap.Empty))
        }
    }

    protected abstract fun getReadChannel(): ReadChannel
    protected abstract fun getMultiPartData(): MultiPartData
    protected open fun getInputStream(): InputStream = getReadChannel().toInputStream()

    suspend override fun <T : Any> receive(type: KClass<T>): T {
        val transformed = pipeline.execute(ApplicationReceiveRequest(type, this)).value
        if (transformed is ApplicationRequest)
            throw Exception("Cannot transform this request's content into $type")

        @Suppress("UNCHECKED_CAST")
        return transformed as? T ?: throw Exception("Cannot transform this request's content into $type")
    }
}