package com.sitexa.sweet

import com.sitexa.sweet.dao.*
import com.sitexa.sweet.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

/**
 * Created by open on 03/04/2017.
 *
 */

fun Route.register(dao: DAOFacade, hashFunction: (String) -> String) {
    post<Register> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(UserPage(user.userId))
        } else {

            println("userId:${it.userId},email:${it.email},displayName:${it.displayName},password:${it.password}")
            if (it.password.length < 6) {
                call.redirect(it.copy(error = "Password should be at least 6 characters long", password = ""))
            } else if (it.userId.length < 4) {
                call.redirect(it.copy(error = "Login should be at least 4 characters long", password = ""))
            } else if (!userNameValid(it.userId)) {
                call.redirect(it.copy(error = "Login should be consists of digits, letters, dots or underscores", password = ""))
            } else if (dao.user(it.userId) != null) {
                call.redirect(it.copy(error = "User with the following login is already registered", password = ""))
            } else {
                val hash = hashFunction(it.password)
                val newUser = User(it.userId, it.mobile, it.email, it.displayName, hash)

                try {
                    dao.createUser(newUser)
                } catch (e: Throwable) {
                    if (dao.user(it.userId) != null) {
                        call.redirect(it.copy(error = "User with the following login is already registered", password = ""))
                    } else if (dao.userByEmail(it.email) != null) {
                        call.redirect(it.copy(error = "User with the following email ${it.email} is already registered", password = ""))
                    } else if (dao.userByMobile(it.mobile) != null) {
                        call.redirect(it.copy(error = "User with the following mobile ${it.mobile} is already registered", password = ""))
                    } else {
                        application.log.error("Failed to register user", e)
                        call.redirect(it.copy(error = "Failed to register", password = ""))
                    }
                }

                call.session(Session(newUser.userId))
                call.redirect(UserPage(newUser.userId))
            }
        }
    }
    get<Register> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(UserPage(user.userId))
        } else {
            call.respond(FreeMarkerContent("register.ftl", mapOf("pageUser" to User(it.userId, it.mobile, it.email, it.displayName, ""), "error" to it.error), ""))
        }
    }
}
