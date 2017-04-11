package com.sitexa.sweet

import com.sitexa.sweet.dao.DAOFacade
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.freemarker.FreeMarkerContent
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.locations.post
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.sessions.sessionOrNull

/**
 * Created by open on 10/04/2017.
 *
 */

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
        println("Route.delSweet.post:${it.id}")
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
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null

        println("viewSweet-code:$code")

        call.respond(FreeMarkerContent("sweet-view.ftl", mapOf("user" to user, "sweet" to dao.getSweet(it.id), "date" to date, "code" to code), user?.userId ?: ""))
    }
}

fun Route.updSweet(dao: DAOFacade, hashFunction: (String) -> String) {
    get<SweetUpd> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null

        call.respond(FreeMarkerContent("sweet-upd.ftl", mapOf("user" to user, "sweet" to dao.getSweet(it.id), "date" to date, "code" to code), user?.userId ?: ""))
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