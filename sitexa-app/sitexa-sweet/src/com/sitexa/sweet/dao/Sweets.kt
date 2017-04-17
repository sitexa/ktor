package com.sitexa.sweet.dao

import org.jetbrains.exposed.sql.Table

/**
 * Created by open on 03/04/2017.
 *
 */


object Sweets : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val user = varchar("user_id", 20).index()
    val date = datetime("date")
    val replyTo = integer("reply_to").index().nullable()
    val directReplyTo = integer("direct_reply_to").index().nullable()
    val text = varchar("text", 1024)
    val mediaFile = varchar("media_file", 1024).nullable()
}
