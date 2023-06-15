package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.events.ToDoListId

sealed class ToDoListCommand

data class CreateToDoList(val user: User, val name: ListName) : ToDoListCommand() {
    val id: ToDoListId = ToDoListId.mint()
}

data class RenameToDoList(val user: User, val oldName: ListName, val newName: ListName) : ToDoListCommand()
//data class FreezeToDoList(val user: User, val name: ListName, val reason: String) : ToDoListCommand()
//data class RestoreToDoList(val user: User, val name: ListName) : ToDoListCommand()

data class AddToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()
data class UpdateToDoItem(val user: User, val name: ListName, val oldItemName: String, val newItem: ToDoItem) :
    ToDoListCommand()

data class DeleteToDoItem(val user: User, val name: ListName, val itemName: String) : ToDoListCommand()



