package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.commands.ToDoListCommandHandler
import com.ubertob.fotf.zettai.db.createToDoListEventStreamerOnPg
import com.ubertob.fotf.zettai.db.jdbc.PgDataSource
import com.ubertob.fotf.zettai.db.jdbc.TransactionIsolationLevel
import com.ubertob.fotf.zettai.db.jdbc.TransactionProvider
import com.ubertob.fotf.zettai.events.EventStreamerInMemory
import com.ubertob.fotf.zettai.events.InMemoryEventsProvider
import com.ubertob.fotf.zettai.events.ToDoListEventStore
import com.ubertob.fotf.zettai.eventsourcing.EventSeq
import com.ubertob.fotf.zettai.logger.stdOutLogger
import com.ubertob.fotf.zettai.queries.ToDoItemProjection
import com.ubertob.fotf.zettai.queries.ToDoListProjectionInMemory
import com.ubertob.fotf.zettai.queries.ToDoListProjectionOnPg
import com.ubertob.fotf.zettai.queries.ToDoListQueryRunner
import com.ubertob.fotf.zettai.webserver.Zettai
import org.jetbrains.exposed.sql.StdOutSqlLogger


fun prepareToDoListHubInMemory(): ToDoListHub {
    val streamer = EventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val inMemoryEvents = InMemoryEventsProvider()
    val cmdHandler = ToDoListCommandHandler(inMemoryEvents, eventStore)
    val fetcher = { lastEvent: EventSeq ->
        inMemoryEvents.tryRun(streamer.fetchAfter(lastEvent))
    }
    val queryRunner = ToDoListQueryRunner(
        ToDoListProjectionInMemory(fetcher),
        ToDoItemProjection(fetcher)
    )

    return ToDoListHub(queryRunner, cmdHandler)
}


fun prepareZettaiOnTestDatabase(): Zettai {
    val dataSource = pgDataSourceForTest()

    val streamer = createToDoListEventStreamerOnPg()
    val eventStore = ToDoListEventStore(streamer)
    val txProvider = TransactionProvider(dataSource, StdOutSqlLogger, TransactionIsolationLevel.Serializable)

    val commandHandler = ToDoListCommandHandler(txProvider, eventStore)

    val fetcher = { lastEvent: EventSeq ->
        txProvider.tryRun(streamer.fetchAfter(lastEvent))
    }

    val queryHandler = ToDoListQueryRunner(ToDoListProjectionOnPg(txProvider, fetcher), ToDoItemProjection(fetcher))

    return Zettai(ToDoListHub(queryHandler, commandHandler), stdOutLogger())
}

fun pgDataSourceForTest(): PgDataSource =
    PgDataSource.create(
        host = "localhost",
        port = 6433,
        database = "zettai_db_test",
        dbUser = "zettai_test",
        dbPassword = "test123"
    )

