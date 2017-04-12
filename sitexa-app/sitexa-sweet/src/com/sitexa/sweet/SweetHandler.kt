package com.sitexa.sweet

import com.sitexa.sweet.dao.DAOFacade
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.freemarker.FreeMarkerContent
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.locations.location
import org.jetbrains.ktor.locations.post
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.sessions.sessionOrNull

/**
 * Created by open on 10/04/2017.
 *
 */


@location("/sweet/{id}")
data class SweetView(val id: Int)

@location("/sweet-new")
data class SweetNew(val text: String = "", val date: Long = 0L, val code: String = "")

//set default values to the data class!!!
@location("/sweet-del")
data class SweetDel(val id: Int = 0, val date: Long = 0L, val code: String = "")

@location("/sweet-upd")
data class SweetUpd(val id: Int = 0, val text: String = "", val date: Long = 0L, val code: String = "")

@location("/sweet-reply")
data class SweetReply(val replyTo: Int = 0, val text: String = "", val date: Long = 0L, val code: String = "")


fun Route.newSweet(dao: DAOFacade, hashFunction: (String) -> String) {
    get<SweetNew> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }

        if (user == null) {
            call.redirect(Login())
        } else {
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)

            call.respond(FreeMarkerContent("sweet-new.ftl", mapOf("user" to user, "date" to date, "code" to code), user.userId))
        }
    }
    post<SweetNew> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user == null || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.redirect(Login())
        } else {
            val id = dao.createSweet(user.userId, it.text, null)
            call.redirect(SweetView(id))
        }
    }
}

fun Route.delSweet(dao: DAOFacade, hashFunction: (String) -> String) {
    post<SweetDel> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val sweet = dao.getSweet(it.id)

        if (user == null || sweet.userId != user.userId || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.redirect(SweetView(it.id))
        } else {
            dao.deleteSweet(it.id)
            call.redirect(Index())
        }
    }
}

fun Route.viewSweet(dao: DAOFacade, hashFunction: (String) -> String) {
    get<SweetView> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val sweet = dao.getSweet(it.id)
        val replies = dao.getReplies(it.id).map { dao.getSweet(it) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null
        val etagString = date.toString() + "," + user?.userId + "," + sweet.id.toString()
        val etag = etagString.hashCode()

        call.respond(FreeMarkerContent("sweet-view.ftl", mapOf("user" to user, "sweet" to sweet, "replies" to replies, "date" to date, "code" to code), etag.toString()))
    }
}

fun Route.updSweet(dao: DAOFacade, hashFunction: (String) -> String) {
    get<SweetUpd> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val sweet = dao.getSweet(it.id)
        val date = System.currentTimeMillis()
        if (user != null && sweet.userId == user.userId) {
            val code = call.securityCode(date, user, hashFunction)
            val etagString = date.toString() + "," + user.userId + "," + sweet.id.toString()
            val etag = etagString.hashCode()
            call.respond(FreeMarkerContent("sweet-upd.ftl", mapOf("user" to user, "sweet" to sweet, "date" to date, "code" to code), etag.toString()))
        } else {
            call.redirect(SweetView(it.id))
        }
    }
    post<SweetUpd> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user == null || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.redirect(Login())
        } else {
            dao.updateSweet(user.userId, it.id, it.text, null)
            call.redirect(SweetView(it.id))
        }
    }
}

fun Route.replySweet(dao: DAOFacade, hashFunction: (String) -> String) {
    get<SweetReply> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val sweet = dao.getSweet(it.replyTo)
        val date = System.currentTimeMillis()
        if (user != null) {
            val code = call.securityCode(date, user, hashFunction)
            val etagString = date.toString() + "," + user.userId + "," + sweet.id.toString()
            val etag = etagString.hashCode()
            call.respond(FreeMarkerContent("sweet-reply.ftl", mapOf("user" to user, "sweet" to sweet, "date" to date, "code" to code), etag.toString()))
        } else {
            call.redirect(SweetView(it.replyTo))
        }
    }
    post<SweetReply> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }

        println("Route.replySweet.post:user=${user?.userId},code=${it.code},replyTo=${it.replyTo},text=${it.text}")

        if (user == null || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.redirect(Login())
        } else {
            val id = dao.createSweet(user.userId, it.text, it.replyTo)
            call.redirect(SweetView(id))
        }
    }
}