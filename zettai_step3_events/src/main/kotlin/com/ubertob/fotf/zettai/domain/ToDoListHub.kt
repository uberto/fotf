package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.commands.ToDoListCommand
import com.ubertob.fotf.zettai.commands.ToDoListCommandHandler
import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.fp.EventPersister

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun getLists(user: User): List<ListName>?
    fun handle(command: ToDoListCommand): ToDoListCommand?
}

interface ToDoListFetcher {

    fun get(user: User, listName: ListName): ToDoList?

    fun getAll(user: User): List<ListName>?

}

class ToDoListHub(
    val fetcher: ToDoListFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {

    override fun handle(command: ToDoListCommand): ToDoListCommand? =
        commandHandler(command)
            ?.let(persistEvents)
            ?.let { command }

    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher.get(user, listName)

    override fun getLists(user: User): List<ListName>? =
        fetcher.getAll(user)

}

