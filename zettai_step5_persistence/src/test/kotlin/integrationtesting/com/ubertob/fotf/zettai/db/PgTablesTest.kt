package integrationtesting.com.ubertob.fotf.zettai.db

import com.ubertob.fotf.zettai.db.jdbc.insertIntoWithReturn
import com.ubertob.fotf.zettai.db.jdbc.selectWhere
import com.ubertob.fotf.zettai.db.toDoListEventsTable
import com.ubertob.fotf.zettai.db.toDoListProjectionTable
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.domain.pgDataSourceForTest
import com.ubertob.fotf.zettai.domain.randomItem
import com.ubertob.fotf.zettai.domain.randomToDoList
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.fotf.zettai.events.ItemAdded
import com.ubertob.fotf.zettai.events.ListCreated
import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.events.ToDoListId
import com.ubertob.fotf.zettai.eventsourcing.EventSeq
import com.ubertob.fotf.zettai.eventsourcing.StoredEvent
import com.ubertob.fotf.zettai.json.toPgEvent
import com.ubertob.fotf.zettai.json.toToDoListEvent
import com.ubertob.fotf.zettai.queries.ToDoListProjectionRow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan

class PgTablesTest {

    val dataSource = pgDataSourceForTest()

    @Test
    fun `can use SQL with PG connection`() {

        val sql = "SELECT * FROM pg_tables"

        val db = Database.connect(dataSource)

        val tables = mutableListOf<String>()
        transaction(db) {
            exec(sql) { rs ->
                while (rs.next()) {
                    tables += rs.getString("tablename")
                }
            }
        }

        expectThat(tables.size).isGreaterThan(0)
        expectThat(tables).contains("pg_statistic")

    }

    val list = randomToDoList()
    val user = User("uberto")

    @Test
    fun `can read and write events from db`() {

        val db = Database.connect(dataSource)

        transaction(db) {

            val listId = ToDoListId.mint()
            val event = ListCreated(listId, user, list.listName)
            val pgEvent = toPgEvent(event)

            val eventId = toDoListEventsTable.insertIntoWithReturn(this, stored(event))
            { newRow ->
                newRow[entity_id] = pgEvent.entityId.raw
                newRow[event_source] = pgEvent.source
                newRow[event_type] = pgEvent.eventType
                newRow[json_data] = pgEvent.jsonString
                newRow[event_version] = pgEvent.version
            }.eventSeq

            expectThat(eventId.progressive).isGreaterThan(0)

            val row = toDoListEventsTable.selectWhere(
                this,
                toDoListEventsTable.id eq eventId.progressive
            )
                .single()

            expectThat(row.get(toDoListEventsTable.entity_id)).isEqualTo(listId.raw)

        }
    }

    @Test
    fun `can read all events for an entity from db`() {

        val db = Database.connect(dataSource)

        transaction(db) {

            val listId = ToDoListId.mint()
            val event = ListCreated(listId, user, list.listName)

            val eventId = storeEvent(event)
            val eventId2 = storeEvent(ItemAdded(listId, randomItem()))
            val eventId3 = storeEvent(ItemAdded(listId, randomItem()))
            val eventId4 = storeEvent(ItemAdded(listId, randomItem()))

            expectThat(eventId2.progressive).isGreaterThan(eventId.progressive)
            expectThat(eventId3.progressive).isGreaterThan(eventId2.progressive)
            expectThat(eventId4.progressive).isGreaterThan(eventId3.progressive)

            val pgEvents = toDoListEventsTable.select { toDoListEventsTable.entity_id eq listId.raw }
                .map(toDoListEventsTable::rowToPgEvent)

            expectThat(pgEvents.count()).isEqualTo(4)

            val actualEvent = toToDoListEvent(pgEvents.first().event).expectSuccess()
            expectThat(actualEvent).isEqualTo(event)

        }
    }

    private fun Transaction.storeEvent(event: ToDoListEvent): EventSeq =
        toPgEvent(event).let { pgEvent ->
            toDoListEventsTable.insertIntoWithReturn(this, stored(event))
            { newRow ->
                newRow[entity_id] = pgEvent.entityId.raw
                newRow[event_source] = pgEvent.source
                newRow[event_type] = pgEvent.eventType
                newRow[json_data] = pgEvent.jsonString
                newRow[event_version] = pgEvent.version
            }.eventSeq
        }

    private fun stored(event: ToDoListEvent): InsertStatement<Number>.() -> StoredEvent<ToDoListEvent> =
        {
            StoredEvent(
                EventSeq(get(toDoListEventsTable.id)), get(
                    toDoListEventsTable.recorded_at
                ), event
            )
        }


    @Test
    fun `can read and write projection from db`() {

        val db = Database.connect(dataSource)

        val listId = ToDoListId.mint()
        val rowKey = listId.raw.toString()
        transaction(db) {

            val row = ToDoListProjectionRow(listId, user, true, list)
            toDoListProjectionTable.insert {
                it[id] = rowKey
                it[row_data] = row
            }

            val actual = with(toDoListProjectionTable) {
                select { id eq rowKey }
                    .map { it[row_data] }
                    .single()
            }
            expectThat(actual).isEqualTo(row)
        }

    }
}


