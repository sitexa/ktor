package com.sitexa.app.dao

import com.zaxxer.hikari.HikariDataSource

/**
 * Created by open on 03/04/2017.

 */
class Conn {

    fun Test() {
        val ds = HikariDataSource()
        ds.maximumPoolSize = 20
        ds.driverClassName = "org.mariadb.jdbc.Driver"
        ds.jdbcUrl = "jdbc:mariadb://localhost:3306/sitexa"
        ds.addDataSourceProperty("user", "root")
        ds.addDataSourceProperty("password", "pop007")
        ds.isAutoCommit = false
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {

        }
    }
}
