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

fun Route.viewSweet(dao: DAOFacade, hashFunction: (String) -> String) {
    get<ViewSweet> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null

        call.respond(FreeMarkerContent("view-sweet.ftl", mapOf("user" to user, "sweet" to dao.getSweet(it.id), "date" to date, "code" to code), user?.userId ?: ""))
    }
}
