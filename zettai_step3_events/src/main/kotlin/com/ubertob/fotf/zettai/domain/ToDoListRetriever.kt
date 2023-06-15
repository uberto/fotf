package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.events.ToDoListState

interface ToDoListRetriever {

    fun retrieveByName(user: User, listName: ListName): ToDoListState?

}