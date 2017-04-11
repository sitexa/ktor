package com.sitexa.sweet.dao

import com.sitexa.sweet.model.*
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.joda.time.*
import java.io.*


fun getDataSource(): HikariDataSource {
    val ds = HikariDataSource()
    ds.maximumPoolSize = 20
    ds.driverClassName = "org.mariadb.jdbc.Driver"
    ds.jdbcUrl = "jdbc:mysql://localhost:3306/sitexa"
    ds.isAutoCommit = false
    ds.addDataSourceProperty("user", "root")
    ds.addDataSourceProperty("password", "pop007")
    ds.addDataSourceProperty("dialect", "MysqlDialect")
    return ds
}

interface DAOFacade : Closeable {
    fun init()
    fun countReplies(id: Int): Int
    fun createSweet(user: String, text: String, replyTo: Int? = null, date: DateTime = DateTime.now()): Int
    fun deleteSweet(id: Int)
    fun updateSweet(user: String, id: Int, text: String, replyTo: Int? = null, date: DateTime = DateTime.now())
    fun getSweet(id: Int): Sweet
    fun userSweets(userId: String): List<Int>
    fun user(userId: String, hash: String? = null): User?
    fun userByEmail(email: String): User?
    fun userByMobile(mobile: String): User?
    fun createUser(user: User)
    fun top(count: Int = 10): List<Int>
    fun latest(count: Int = 10): List<Int>
}

class DAOFacadeDatabase(val db: Database) : DAOFacade {

    override fun init() {
        db.transaction {
            create(Users, Sweets)
        }
    }

    override fun countReplies(id: Int): Int {
        return db.transaction {
            Sweets.slice(Sweets.id.count()).select {
                Sweets.replyTo.eq(id)
            }.single()[Sweets.id.count()]
        }
    }

    override fun createSweet(user: String, text: String, replyTo: Int?, date: DateTime): Int {
        return db.transaction {
            Sweets.insert {
                it[Sweets.user] = user
                it[Sweets.date] = date
                it[Sweets.replyTo] = replyTo
                it[Sweets.text] = text
            }.generatedKey ?: throw IllegalStateException("No generated key returned")
        }
    }

    override fun deleteSweet(id: Int) {
        db.transaction {
            Sweets.deleteWhere { Sweets.id.eq(id) }
        }
    }

    override fun updateSweet(user: String, id: Int, text: String, replyTo: Int?, date: DateTime) {
        db.transaction {
            Sweets.update({Sweets.id eq id}){
                it[Sweets.user] = user
                it[Sweets.date] = date
                it[Sweets.text] = text
                it[Sweets.replyTo] = replyTo
            }
        }
    }

    override fun getSweet(id: Int) = db.transaction {
        val row = Sweets.select { Sweets.id.eq(id) }.single()
        Sweet(id, row[Sweets.user], row[Sweets.text], row[Sweets.date], row[Sweets.replyTo])
    }

    override fun userSweets(userId: String) = db.transaction {
        Sweets.slice(Sweets.id).select { Sweets.user.eq(userId) }.orderBy(Sweets.date, false).limit(100).map { it[Sweets.id] }
    }

    override fun user(userId: String, hash: String?) = db.transaction {
        Users.select { Users.id.eq(userId) }
                .mapNotNull {
                    if (hash == null || it[Users.passwordHash] == hash) {
                        User(userId, it[Users.mobile], it[Users.email], it[Users.displayName], it[Users.passwordHash])
                    } else {
                        null
                    }
                }
                .singleOrNull()
    }

    override fun userByMobile(mobile: String) = db.transaction {
        Users.select { Users.mobile.eq(mobile) }
                .map { User(it[Users.id], mobile, it[Users.email], it[Users.displayName], it[Users.passwordHash]) }.singleOrNull()
    }

    override fun userByEmail(email: String) = db.transaction {
        Users.select { Users.email.eq(email) }
                .map { User(it[Users.id], it[Users.mobile], email, it[Users.displayName], it[Users.passwordHash]) }.singleOrNull()
    }

    override fun createUser(user: User) = db.transaction {
        Users.insert {
            it[Users.id] = user.userId
            it[Users.displayName] = user.displayName
            it[Users.email] = user.email
            it[Users.mobile] = user.mobile
            it[Users.passwordHash] = user.passwordHash
        }
        Unit
    }

    override fun top(count: Int): List<Int> = db.transaction {
        // note: in a real application you shouldn't do it like this
        //   as it may cause database outages on big data
        //   so this implementation is just for demo purposes

        val k2 = Sweets.alias("k2")
        Sweets.join(k2, JoinType.LEFT, Sweets.id, k2[Sweets.replyTo])
                .slice(Sweets.id, k2[Sweets.id].count())
                .selectAll()
                .groupBy(Sweets.id)
                .orderBy(k2[Sweets.id].count(), isAsc = false)
//                .having { k2[Sweets.id].count().greater(0) }
                .limit(count)
                .map { it[Sweets.id] }
    }

    override fun latest(count: Int): List<Int> = db.transaction {
        var attempt = 0
        var allCount: Int? = null

        for (minutes in generateSequence(2) { it * it }) {
            attempt++

            val dt = DateTime.now().minusMinutes(minutes)

            val all = Sweets.slice(Sweets.id)
                    .select { Sweets.date.greater(dt) }
                    .orderBy(Sweets.date, false)
                    .limit(count)
                    .map { it[Sweets.id] }

            if (all.size >= count) {
                return@transaction all
            }
            if (attempt > 10 && allCount == null) {
                allCount = Sweets.slice(Sweets.id.count()).selectAll().count()
                if (allCount <= count) {
                    return@transaction Sweets.slice(Sweets.id).selectAll().map { it[Sweets.id] }
                }
            }
        }

        emptyList()
    }

    override fun close() {
    }
}
