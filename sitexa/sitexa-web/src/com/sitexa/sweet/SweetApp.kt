package com.sitexa.sweet


import com.google.gson.GsonBuilder
import com.sitexa.sweet.dao.DAOFacade
import com.sitexa.sweet.dao.DAOFacadeCache
import com.sitexa.sweet.dao.DAOFacadeDatabase
import com.sitexa.sweet.handler.indexHandler
import com.sitexa.sweet.handler.staticHandler
import com.sitexa.sweet.handler.sweetRouting
import com.sitexa.sweet.handler.userHandler
import com.sitexa.sweet.model.User
import com.zaxxer.hikari.HikariDataSource
import freemarker.cache.ClassTemplateLoader
import org.jetbrains.exposed.sql.Database
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.content.TextContent
import org.jetbrains.ktor.features.ConditionalHeaders
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.features.PartialContentSupport
import org.jetbrains.ktor.freemarker.FreeMarker
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.logging.CallLogging
import org.jetbrains.ktor.request.acceptItems
import org.jetbrains.ktor.request.header
import org.jetbrains.ktor.request.host
import org.jetbrains.ktor.request.port
import org.jetbrains.ktor.response.respondRedirect
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.sessions.SessionCookieTransformerMessageAuthentication
import org.jetbrains.ktor.sessions.SessionCookiesSettings
import org.jetbrains.ktor.sessions.withCookieByValue
import org.jetbrains.ktor.sessions.withSessions
import org.jetbrains.ktor.transform.transform
import org.jetbrains.ktor.util.hex
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Created by open on 05/04/2017.
 *
 */


class JsonResponse(val data: Any)

data class Session(val userId: String)

class SweetApp : AutoCloseable {

    val datasource = getDataSource()

    val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")
    val dao: DAOFacade = DAOFacadeCache(DAOFacadeDatabase(Database.connect(datasource)), File(dir.parentFile, "ehcache"))

    fun Application.install() {
        dao.init()
        install(DefaultHeaders)
        install(CallLogging)
        install(ConditionalHeaders)
        install(PartialContentSupport)
        install(Locations)
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(SweetApp::class.java.classLoader, "templates")
        }

        withSessions<Session> {
            withCookieByValue {
                settings = SessionCookiesSettings(transformers = listOf(SessionCookieTransformerMessageAuthentication(hashKey)))
            }
        }

        val hashFunction = { s: String -> hash(s) }

        val gson = GsonBuilder().create()
        intercept(ApplicationCallPipeline.Infrastructure) { call ->
            if (call.request.acceptItems().any { it.value == "application/json" }) {
                call.transform.register<JsonResponse> { value ->
                    TextContent(gson.toJson(value.data), ContentType.Application.Json)
                }
            }
        }

        install(Routing) {
            staticHandler()
            indexHandler(dao)
            userHandler(dao,hashFunction)
            sweetRouting(dao,hashFunction)
        }
    }

    override fun close() {
        datasource.close()
    }

    fun hash(password: String): String {
        val hmac = Mac.getInstance("HmacSHA1")
        hmac.init(hmacKey)
        return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
    }

}

suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host() ?: "localhost"
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.feature(Locations).href(location)}")
}

fun ApplicationCall.securityCode(date: Long, user: User, hashFunction: (String) -> String) =
        hashFunction("$date:${user.userId}:${request.host()}:${refererHost()}")

fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashFunction: (String) -> String) =
        securityCode(date, user, hashFunction) == code
                && (System.currentTimeMillis() - date).let { it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) }

fun ApplicationCall.refererHost() = request.header(HttpHeaders.Referrer)?.let { URI.create(it).host }


fun getDataSource(): HikariDataSource {
    val ds = HikariDataSource()
    ds.maximumPoolSize = dbConfig["pool"].toString().toInt()
    ds.driverClassName = dbConfig["driver"].toString()
    ds.jdbcUrl = dbConfig["url"].toString()
    ds.isAutoCommit = dbConfig["autoCommit"].toString().toBoolean()
    ds.addDataSourceProperty("user", dbConfig["user"].toString())
    ds.addDataSourceProperty("password", dbConfig["password"].toString())
    ds.addDataSourceProperty("dialect", dbConfig["dialect"].toString())
    return ds
}
