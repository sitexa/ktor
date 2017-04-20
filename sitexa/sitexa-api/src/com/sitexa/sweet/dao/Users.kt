package com.sitexa.sweet.dao

import org.jetbrains.exposed.sql.Table

/**
 * Created by open on 03/04/2017.
 *
 */

object Users : Table() {
    val id = varchar("id", 20).primaryKey()
    val mobile = varchar("mobile",15).uniqueIndex()
    val email = varchar("email", 128).uniqueIndex()
    val displayName = varchar("display_name", 256)
    val passwordHash = varchar("password_hash", 64)
}
