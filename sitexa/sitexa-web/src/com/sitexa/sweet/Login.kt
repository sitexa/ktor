package com.sitexa.sweet

import com.sitexa.sweet.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

/**
 * Created by open on 03/04/2017.
 *
 */

fun Route.login(dao: DAOFacade, hash: (String) -> String) {
    get<Login> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }

        if (user != null) {
            call.redirect(UserPage(user.userId))
        } else {
            call.respond(FreeMarkerContent("login.ftl", mapOf("userId" to it.userId, "error" to it.error), ""))
        }
    }
    post<Login> {
        val login = when {
            it.userId.length < 4 -> null
            it.password.length < 6 -> null
            !userNameValid(it.userId) -> null
            else -> dao.user(it.userId, hash(it.password))
        }

        if (login == null) {
            call.redirect(it.copy(password = "", error = "Invalid username or password"))
        } else {
            call.session(Session(login.userId))
            call.redirect(UserPage(login.userId))
        }
    }
    get<Logout> {
        call.clearSession()
        call.redirect(Index())
    }
}
