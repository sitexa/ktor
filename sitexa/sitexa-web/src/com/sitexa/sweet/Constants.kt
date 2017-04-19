package com.sitexa.sweet

import org.jetbrains.ktor.util.hex
import java.io.File

/**
 * Created by open on 18/04/2017.
 *
 */

val uploadDir = "/Users/open/IdeaProjects/sweet/sitexa/sitexa-web/uploads"

val dbConfig = mapOf("driver" to "org.mariadb.jdbc.Driver",
        "url" to "jdbc:mysql://localhost:3306/sitexa",
        "user" to "root",
        "password" to "pop007",
        "pool" to 20,
        "autoCommit" to false,
        "dialect" to "MysqlDialect")

val hashKey = hex("6819b57a326945c1968f45236589")
val dir = File("target/db")



