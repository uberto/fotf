package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.db.fp.ContextProvider
import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.db.fp.bindOutcome
import com.ubertob.fotf.zettai.domain.InconsistentStateError
import com.ubertob.fotf.zettai.domain.ToDoListCommandError
import com.ubertob.fotf.zettai.domain.ZettaiError
import com.ubertob.fotf.zettai.domain.ZettaiOutcome
import com.ubertob.fotf.zettai.events.*
import com.ubertob.fotf.zettai.fp.asFailure
import com.ubertob.fotf.zettai.fp.asSuccess
import com.ubertob.fotf.zettai.fp.join

typealias CommandOutcomeReader<CTX> = ContextReader<CTX, ToDoListCommandOutcome>

typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler<CTX>(
    private val contextProvider: ContextProvider<CTX>,
    private val eventStore: ToDoListEventStore<CTX>
) : (ToDoListCommand) -> ToDoListCommandOutcome {

    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        contextProvider.tryRun(
            when (command) {
                is CreateToDoList -> command.execute()
                is AddToDoItem -> command.execute()
            }.bindOutcome(eventStore)
        ).join()
            .transform { storedEvents -> storedEvents.map { it.event } }
            .transformFailure { it as? ZettaiError ?: ToDoListCommandError(it.msg) }


    private fun CreateToDoList.execute(): CommandOutcomeReader<CTX> =
        eventStore.retrieveByNaturalKey(UserListName(user, name))
            .transform { listState ->
                when (listState) {
                    null -> ListCreated(id, user, name).asCommandSuccess()
                    else -> InconsistentStateError(this, listState).asFailure()
                }
            }

    private fun AddToDoItem.execute(): CommandOutcomeReader<CTX> =
        eventStore.retrieveByNaturalKey(UserListName(user, name))
            .transform { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            ToDoListCommandError("cannot have 2 items with same name").asFailure()
                        else {
                            ItemAdded(listState.id, item).asCommandSuccess()
                        }
                    }

                    null -> ToDoListCommandError("list $name not found").asFailure()
                    else -> InconsistentStateError(this, listState).asFailure()
                }
            }


    private fun ToDoListEvent.asCommandSuccess(): ZettaiOutcome<List<ToDoListEvent>> = listOf(this).asSuccess()


}

