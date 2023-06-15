package com.ubertob.fotf.zettai.webserver

import com.ubertob.fotf.zettai.commands.ToDoListCommandHandler
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.domain.ToDoStatus.Done
import com.ubertob.fotf.zettai.domain.ToDoStatus.InProgress
import com.ubertob.fotf.zettai.events.ToDoListEventStore
import com.ubertob.fotf.zettai.events.ToDoListEventStreamerInMemory
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.time.LocalDate

fun main() {
    val fetcher = ToDoListFetcherFromMap(storeWithExampleData())
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val commandHandler =
        ToDoListCommandHandler(eventStore, fetcher)
    val hub = ToDoListHub(fetcher, commandHandler, eventStore)

    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto/book")

}

fun storeWithExampleData(): ToDoListStore = mutableMapOf(
    User("uberto") to
            mutableMapOf(exampleToDoList().listName to exampleToDoList())
)

private fun exampleToDoList(): ToDoList {
    return ToDoList(
        listName = ListName.fromTrusted("book"),
        items = listOf(
            ToDoItem("prepare the diagram", LocalDate.now().plusDays(1), Done),
            ToDoItem("rewrite explanations", LocalDate.now().plusDays(2), InProgress),
            ToDoItem("finish the chapter"),
            ToDoItem("draft next chapter")
        )
    )
}

