package com.ubertob.fotf.zettai.domain

data class ListName(val name: String)

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)


data class ToDoItem(
    val description: String
)
