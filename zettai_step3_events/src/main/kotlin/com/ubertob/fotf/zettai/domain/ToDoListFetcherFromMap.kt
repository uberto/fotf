package com.ubertob.fotf.zettai.domain


typealias ToDoListStore = MutableMap<User, MutableMap<ListName, ToDoList>>

interface ToDoListUpdatableFetcher : ToDoListFetcher {

    fun assignListToUser(user: User, list: ToDoList): ToDoList?

    fun addItemToList(user: User, listName: ListName, item: ToDoItem) {
        get(user, listName)?.run {
            val newList = copy(items = items.filterNot { it.description == item.description } + item)
            assignListToUser(user, newList)
        }
    }

}


data class ToDoListFetcherFromMap(
    private val store: ToDoListStore
) : ToDoListUpdatableFetcher {

    override fun get(user: User, listName: ListName): ToDoList? =
        store[user]?.get(listName)

    override fun getAll(user: User): List<ListName> =
        store[user]?.keys?.toList() ?: emptyList()

    override fun assignListToUser(user: User, list: ToDoList): ToDoList? =
        store.compute(user) { _, value ->
            val listMap = value ?: mutableMapOf()
            listMap.apply { put(list.listName, list) }
        }?.let { list }

}
