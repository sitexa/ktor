package com.sitexa.sweet

import com.sitexa.sweet.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

/**
 * Created by open on 03/04/2017.
 *
 */



fun Route.delete(dao: DAOFacade, hashFunction: (String) -> String) {
    post<SweetDelete> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val sweet = dao.getSweet(it.id)

        if (user == null || sweet.userId != user.userId || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.redirect(ViewSweet(it.id))
        } else {
            dao.deleteSweet(it.id)
            call.redirect(Index())
        }
    }
}
