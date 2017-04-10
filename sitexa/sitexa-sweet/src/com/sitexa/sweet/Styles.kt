package com.sitexa.sweet

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*

/**
 * Created by open on 03/04/2017.
 *
 */

@location("/styles/main.css")
class MainCss()

fun Route.styles() {
    get<MainCss> {
        call.respond(call.resolveClasspathWithPath("", "blog.css")!!)
    }
}