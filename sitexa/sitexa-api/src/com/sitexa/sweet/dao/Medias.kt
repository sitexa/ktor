package com.sitexa.sweet.dao

import org.jetbrains.exposed.sql.Table

/**
 * Created by open on 17/04/2017.
 *
 */

object Medias : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val refId = integer("ref_id")
    val fileName = varchar("file_name", 50)
    val fileType = varchar("file_type", 20)
    val title = varchar("title", 100)
    val sortOrder = integer("sort_order").index()
}