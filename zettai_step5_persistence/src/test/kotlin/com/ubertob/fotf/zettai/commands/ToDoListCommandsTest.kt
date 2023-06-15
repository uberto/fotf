package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.domain.tooling.expectFailure
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
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
        expectThat(duplicatedRes).isA<InconsistentStateError>()
    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())
        val res = handler(cmd).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }

}