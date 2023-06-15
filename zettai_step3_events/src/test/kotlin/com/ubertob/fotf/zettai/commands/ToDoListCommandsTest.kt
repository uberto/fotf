package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.events.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class ToDoListCommandsTest {

    val noopFetcher = object : ToDoListUpdatableFetcher {
        override fun assignListToUser(user: User, list: ToDoList): ToDoList? = null //do nothing
        override fun get(user: User, listName: ListName): ToDoList? = TODO("not implemented")
        override fun getAll(user: User): List<ListName>? = TODO("not implemented")
    }

    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val handler = ToDoListCommandHandler(eventStore, noopFetcher)

    fun handle(cmd: ToDoListCommand): List<ToDoListEvent>? =
        handler(cmd)?.let(eventStore)


    val name = randomListName()
    val user = randomUser()

    @Test
    fun `CreateToDoList generate the correct event`() {

        val cmd = CreateToDoList(randomUser(), randomListName())
        val entityRetriever: ToDoListRetriever = object : ToDoListRetriever {
            override fun retrieveByName(user: User, listName: ListName) = InitialState
        }

        val handler = ToDoListCommandHandler(entityRetriever, noopFetcher)
        val res = handler(cmd)?.single()

        expectThat(res).isEqualTo(ListCreated(cmd.id, cmd.user, cmd.name))
    }

    @Test
    fun `Add list fails if the user has already a list with same name`() {

        val cmd = CreateToDoList(user, name)
        val res = handle(cmd)?.single()

        expectThat(res).isA<ListCreated>()

        val duplicatedRes = handle(cmd)
        expectThat(duplicatedRes).isNull()

    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())
        val res = handle(cmd)

        expectThat(res).isNull()

    }

}