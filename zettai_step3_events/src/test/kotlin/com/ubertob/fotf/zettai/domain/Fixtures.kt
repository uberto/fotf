package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.commands.ToDoListCommandHandler
import com.ubertob.fotf.zettai.events.ToDoListEventStore
import com.ubertob.fotf.zettai.events.ToDoListEventStreamerInMemory
import com.ubertob.fotf.zettai.webserver.Zettai


fun prepareToDoListHubForTests(fetcher: ToDoListFetcherFromMap): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore, fetcher)
    return ToDoListHub(fetcher, cmdHandler, eventStore)
}


fun prepareZettaiForTests(): Zettai {
    return Zettai(
        prepareToDoListHubForTests(
            ToDoListFetcherFromMap(
                mutableMapOf()
            )
        )
    )
}