package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.domain.ToDoListCommandError
import com.ubertob.fotf.zettai.domain.randomItem
import com.ubertob.fotf.zettai.domain.randomListName
import com.ubertob.fotf.zettai.domain.randomUser
import com.ubertob.fotf.zettai.domain.tooling.expectFailure
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.fotf.zettai.domain.tooling.randomText
import com.ubertob.fotf.zettai.events.EventStreamerInMemory
import com.ubertob.fotf.zettai.events.InMemoryEventsProvider
import com.ubertob.fotf.zettai.events.ListCreated
import com.ubertob.fotf.zettai.events.ToDoListEventStore
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.single

internal class ToDoListCommandsTest {
    val streamer = EventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val inMemoryEvents = InMemoryEventsProvider()

    val handler = ToDoListCommandHandler(inMemoryEvents, eventStore)

    val name = randomListName()
    val user = randomUser()

    @Test
    fun `Add list fails if the user has already a list with same name`() {

        val cmd = CreateToDoList(user, name)
        val res = handler(cmd).expectSuccess()

        expectThat(res).single().isA<ListCreated>()
        eventStore(res)

        val duplicatedRes = handler(cmd).expectFailure()
        expectThat(duplicatedRes).isA<ToDoListCommandError>()
    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())
        val res = handler(cmd).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }


    @Test
    fun `Rename list fails if a list with same name already exists`() {

        handler(CreateToDoList(user, name)).expectSuccess()

        val newName = randomListName()
        handler(CreateToDoList(user, newName)).expectSuccess()
        val res = handler(RenameToDoList(user, name, newName)).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }


    @Test
    fun `RetrieveIdFromNaturalKey considers only the most recent name of a list`() {
        handler(CreateToDoList(user, name)).expectSuccess()
        val newName = randomListName()
        handler(RenameToDoList(user, name, newName)).expectSuccess()
        val newNewName = randomListName()
        handler(RenameToDoList(user, newName, newNewName)).expectSuccess()
        handler(AddToDoItem(user, name, randomItem())).expectFailure()
        handler(AddToDoItem(user, newName, randomItem())).expectFailure()
        handler(AddToDoItem(user, newNewName, randomItem())).expectSuccess()
    }

    @Test
    fun `Edit item fails if the item doesn't exists`() {
        val cmd = UpdateToDoItem(user, name, randomText(10), randomItem())
        val res = handler(cmd).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }

    @Test
    fun `Delete item fails if the item doesn't exists`() {
        val cmd = DeleteToDoItem(user, name, randomText(10))
        val res = handler(cmd).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }
}