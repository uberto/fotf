package com.ubertob.fotf.zettai.domain

typealias ToDoListFetcher = (user: User, listName: ListName) -> ToDoList?

interface ToDoListUpdatableFetcher : ToDoListFetcher {

    override fun invoke(user: User, listName: ListName): ToDoList?

    fun assignListToUser(user: User, list: ToDoList): ToDoList?

}