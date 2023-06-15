package com.ubertob.fotf.zettai.db.jdbc

import java.sql.ResultSet

enum class TransactionIsolationLevel(val jdbcLevel: Int) {
    None(0),
    ReadUncommitted(1),
    ReadCommitted(2),
    RepeatableRead(4),
    Serializable(8),
}

fun <T> ResultSet.asSequence(block: ResultSet.() -> T): Sequence<T> =
    generateSequence {
        takeIf { next() }?.let(block)
    }

