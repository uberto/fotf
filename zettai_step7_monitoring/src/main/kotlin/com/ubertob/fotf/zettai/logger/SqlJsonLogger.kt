package com.ubertob.fotf.zettai.logger

import com.ubertob.fotf.zettai.fp.asSuccess
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs

data class SqlJsonLogger(val logger: ZettaiLogger) : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        logger(
            context.expandArgs(transaction).asSuccess(),
            LogContext(
                "txId: ${transaction.id} duration: ${transaction.duration}",
                OperationKind.SqlStatement,
                null,
                null
            )
        )
    }

}


