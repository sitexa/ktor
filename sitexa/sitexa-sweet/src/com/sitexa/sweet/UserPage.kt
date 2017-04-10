package com.sitexa.sweet


import com.sitexa.sweet.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

/**
 * Created by open on 03/04/2017.
 *
 */

fun Route.userPage(dao: DAOFacade) {
    get<UserPage> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val pageUser = dao.user(it.user)

        if (pageUser == null) {
            call.respond(HttpStatusCode.NotFound.description("User ${it.user} doesn't exist"))
        } else {
            val sweets = dao.userSweets(it.user).map { dao.getSweet(it) }
            val etag = (user?.userId ?: "") + "_" + sweets.map { it.text.hashCode() }.hashCode().toString()

            call.respond(FreeMarkerContent("user.ftl", mapOf("user" to user, "pageUser" to pageUser, "sweets" to sweets), etag))
        }
    }
}
