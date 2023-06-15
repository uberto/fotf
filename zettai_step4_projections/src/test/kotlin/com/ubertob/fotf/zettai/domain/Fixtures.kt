package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.commands.ToDoListCommandHandler
import com.ubertob.fotf.zettai.events.ToDoListEventStore
import com.ubertob.fotf.zettai.events.ToDoListEventStreamerInMemory
import com.ubertob.fotf.zettai.queries.ToDoListQueryRunner
import com.ubertob.fotf.zettai.webserver.Zettai


fun prepareToDoListHubForTests(): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    return ToDoListHub(queryRunner, cmdHandler, eventStore)
}


fun prepareZettaiForTests(): Zettai {
    return Zettai(
        prepareToDoListHubForTests()
    )
}