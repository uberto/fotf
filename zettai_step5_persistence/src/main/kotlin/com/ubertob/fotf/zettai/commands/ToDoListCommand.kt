package com.ubertob.fotf.zettai.commands

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.events.ToDoListId

sealed class ToDoListCommand

data class CreateToDoList(val user: User, val name: ListName) : ToDoListCommand() {
    val id: ToDoListId = ToDoListId.mint()
}

data class AddToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()
