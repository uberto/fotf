package com.ubertob.fotf.zettai.db.jdbc

import com.ubertob.fotf.zettai.db.fp.ContextError
import com.ubertob.fotf.zettai.db.fp.ContextProvider
import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.asFailure
import com.ubertob.fotf.zettai.fp.asSuccess
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.statements.jdbc.iterate
import org.jetbrains.exposed.sql.transactions.inTopLevelTransaction
import java.sql.ResultSet
import javax.sql.DataSource

typealias TxReader<T> = ContextReader<Transaction, T>

data class TransactionError(override val msg: String, override val exception: Throwable?) : ContextError

data class TransactionProvider(
    private val dataSource: DataSource,
    val isolationLevel: TransactionIsolationLevel,
    val maxAttempts: Int = 10
) : ContextProvider<Transaction> {

    override fun <T> tryRun(reader: TxReader<T>): Outcome<ContextError, T> =
        inTopLevelTransaction(
            db = Database.connect(dataSource),
            transactionIsolation = isolationLevel.jdbcLevel,
            repetitionAttempts = maxAttempts
        ) {
            addLogger(StdOutSqlLogger)

            try {
                reader.runWith(this).asSuccess()
            } catch (t: Throwable) {
                rollback()
                TransactionError("Transaction rolled back because ${t.message}", t).asFailure()
            }
        }
}


fun Table.selectWhere(
    tx: Transaction,
    condition: Op<Boolean>?,
    orderByCond: Column<*>? = null
): List<ResultRow> =
    tx.exec(
        Query(this, condition).apply {
            orderByCond?.let { orderBy(it) }
        }
    )?.iterate { toResultRow(this@selectWhere.realFields) }
        ?: emptyList()

fun ResultSet.toResultRow(fields: List<Expression<*>>): ResultRow {
    val fieldsIndex = fields.distinct().mapIndexed { i, field ->
        val value = (field as? Column<*>)?.columnType?.readObject(this, i + 1) ?: getObject(i + 1)
        field to value
    }.toMap()
    return ResultRow.createAndFillValues(fieldsIndex)
}

fun queryBySql(tx: Transaction, fields: List<Expression<*>>, sql: String): List<ResultRow> =
    tx.exec(SqlQuery(sql))
        ?.iterate { toResultRow(fields) }
        ?: emptyList()


fun <T, Self : Table> Self.insertIntoWithReturn(
    tx: Transaction,
    postExecution: InsertStatement<Number>.() -> T,
    block: Self.(InsertStatement<Number>) -> Unit
): T =
    InsertStatement<Number>(this).apply {
        block(this)
        execute(tx)
    }.let { postExecution(it) }


fun <Self : Table> Self.insertInto(
    tx: Transaction,
    block: Self.(InsertStatement<Number>) -> Unit
) {
    insertIntoWithReturn(tx, {}, block)
}

fun Table.updateWhere(
    tx: Transaction,
    where: Op<Boolean>? = null,
    block: Table.(UpdateStatement) -> Unit
) {
    UpdateStatement(targetsSet = this, limit = null, where = where).apply {
        block(this)
        execute(tx)
    }
}


data class SqlQuery(val sql: String) :
    Statement<ResultSet>(StatementType.SELECT, emptyList()) {
    override fun prepareSQL(transaction: Transaction): String = sql

    override fun PreparedStatementApi.executeInternal(transaction: Transaction): ResultSet {
        val fetchSize = transaction.db.defaultFetchSize
        if (fetchSize != null) {
            this.fetchSize = fetchSize
        }
        return executeQuery()
    }

    override fun arguments() = emptyList<List<Pair<IColumnType, Any?>>>()
}