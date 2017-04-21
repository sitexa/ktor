package com.sitexa.sweet.handler


import com.sitexa.sweet.*
import com.sitexa.sweet.dao.*
import com.sitexa.sweet.model.User
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


@location("/user/{user}")
data class UserPage(val user: String)

@location("/register")
data class Register(val userId: String = "", val mobile: String = "", val displayName: String = "", val email: String = "", val password: String = "", val error: String = "")

@location("/login")
data class Login(val userId: String = "", val password: String = "", val error: String = "")

@location("/logout")
class Logout

fun Route.userHandler(dao: DAOFacade, hashFunction: (String) -> String) {
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
            else -> dao.user(it.userId, hashFunction(it.password))
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


private val userIdPattern = "[a-zA-Z0-9_\\.]+".toRegex()
internal fun userNameValid(userId: String) = userId.matches(userIdPattern)

private val emailPattern = "[a-zA-Z0-9_]+@[a-zA-Z0-9_]+([-.][a-zA-Z0-9_]+)".toRegex()
internal fun emailValid(email: String) = email.matches(emailPattern)

private val phonePattern = "\\d{11}|\\d{7,8}".toRegex()
internal fun phoneValid(phone: String) = phone.matches(phonePattern)
