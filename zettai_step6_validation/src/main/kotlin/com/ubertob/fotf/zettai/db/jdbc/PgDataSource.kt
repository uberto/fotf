package com.ubertob.fotf.zettai.db.jdbc

import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

class PgDataSource private constructor(
    val name: String,
    private val delegate: PGSimpleDataSource
) : DataSource by delegate {

    companion object {
        fun create(
            host: String,
            port: Int,
            dbUser: String,
            dbPassword: String,
            database: String
        ): PgDataSource = PGSimpleDataSource().apply {
            serverNames = arrayOf(host)
            portNumbers = intArrayOf(port)
            databaseName = database
            user = dbUser
            password = dbPassword
        }.let {
            PgDataSource("$host:$port:$database", it)
        }
    }
}