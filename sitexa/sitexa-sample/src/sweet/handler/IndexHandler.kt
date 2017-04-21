package com.sitexa.sweet.handler

import com.sitexa.sweet.Session
import com.sitexa.sweet.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*
import org.joda.time.DateTime

/**
 * Created by open on 03/04/2017.
 *
 */


@location("/")
class Index

fun Route.indexHandler(dao: DAOFacade) {
    get<Index> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val top = dao.top(10).map { dao.getSweet(it) }
        val latest = dao.latest(10).map { dao.getSweet(it) }
        val date = System.currentTimeMillis()
        val etagString = date.toString() + "," + user?.userId + "," + top.joinToString { it.id.toString() } + latest.joinToString { it.id.toString() }
        val etag = etagString.hashCode()

        call.respond(FreeMarkerContent("index.ftl", mapOf("top" to top, "latest" to latest, "user" to user), etag.toString()))
    }
}
