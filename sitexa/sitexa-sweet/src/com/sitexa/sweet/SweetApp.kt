package com.sitexa.sweet


import com.sitexa.sweet.dao.DAOFacade
import com.sitexa.sweet.dao.DAOFacadeCache
import com.sitexa.sweet.dao.DAOFacadeDatabase
import com.sitexa.sweet.dao.getDataSource
import com.sitexa.sweet.model.User
import freemarker.cache.ClassTemplateLoader
import org.jetbrains.exposed.sql.Database
import org.jetbrains.ktor.application.Application
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.feature
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.features.ConditionalHeaders
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.features.PartialContentSupport
import org.jetbrains.ktor.freemarker.FreeMarker
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.locations.location
import org.jetbrains.ktor.logging.CallLogging
import org.jetbrains.ktor.request.header
import org.jetbrains.ktor.request.host
import org.jetbrains.ktor.request.port
import org.jetbrains.ktor.response.respondRedirect
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.sessions.SessionCookieTransformerMessageAuthentication
import org.jetbrains.ktor.sessions.SessionCookiesSettings
import org.jetbrains.ktor.sessions.withCookieByValue
import org.jetbrains.ktor.sessions.withSessions
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


@location("/")
class Index


@location("/sweet/{id}")
data class SweetView(val id: Int)

@location("/sweet-new")
data class SweetNew(val text: String = "", val date: Long = 0L, val code: String = "")

//set default values to the data class!!!
@location("/sweet-del")
data class SweetDel(val id: Int = 0, val date: Long = 0L, val code: String = "")

@location("/sweet-upd")
data class SweetUpd(val id: Int = 0, val text: String = "", val date: Long = 0L, val code: String = "")


@location("/user/{user}")
data class UserPage(val user: String)

@location("/register")
data class Register(val userId: String = "", val mobile: String = "", val displayName: String = "", val email: String = "", val password: String = "", val error: String = "")

@location("/login")
data class Login(val userId: String = "", val password: String = "", val error: String = "")

@location("/logout")
class Logout

data class Session(val userId: String)


class SweetApp : AutoCloseable {

    val hashKey = hex("6819b57a326945c1968f45236589")
    val dir = File("target/db")

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

        install(Routing) {
            styles()
            index(dao)
            userPage(dao)

            newSweet(dao, hashFunction)
            delSweet(dao, hashFunction)
            viewSweet(dao, hashFunction)
            updSweet(dao, hashFunction)

            login(dao, hashFunction)
            register(dao, hashFunction)
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

private val userIdPattern = "[a-zA-Z0-9_\\.]+".toRegex()
internal fun userNameValid(userId: String) = userId.matches(userIdPattern)

private val emailPattern = "[a-zA-Z0-9_]+@[a-zA-Z0-9_]+([-.][a-zA-Z0-9_]+)".toRegex()
internal fun emailValid(email: String) = email.matches(emailPattern)

private val phonePattern = "\\d{11}|\\d{7,8}".toRegex()
internal fun phoneValid(phone: String) = phone.matches(phonePattern)
