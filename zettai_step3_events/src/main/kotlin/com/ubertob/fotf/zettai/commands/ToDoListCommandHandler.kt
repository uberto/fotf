package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.ToDoListRetriever
import com.ubertob.fotf.zettai.domain.ToDoListUpdatableFetcher
import com.ubertob.fotf.zettai.events.*

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever,
    private val readModel: ToDoListUpdatableFetcher //temporary needed to update the read model
) : (ToDoListCommand) -> List<ToDoListEvent>? {

    override fun invoke(command: ToDoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    InitialState -> {
                        readModel.assignListToUser(
                            user,
                            ToDoList(name, emptyList())
                        )
                        ListCreated(id, user, name).toList()
                    }

                    else -> null //command fail
                }
            }

    private fun AddToDoItem.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            null //cannot have 2 items with same name
                        else {
                            readModel.addItemToList(user, listState.name, item)
                            ItemAdded(listState.id, item).toList()
                        }
                    }

                    InitialState,
                    is OnHoldToDoList,
                    is ClosedToDoList -> null //command fail
                }
            }

    private fun ToDoListEvent.toList(): List<ToDoListEvent> = listOf(this)

}
