package com.ubertob.fotf.exercises.chapter4

import java.time.LocalDate

data class ToDoItem(
    val description: String,
    val dueDate: LocalDate? = null,
    val status: ToDoStatus = ToDoStatus.Todo
)

enum class ToDoStatus { Todo, InProgress, Done, Blocked }


fun <T> T.discardUnless(predicate: T.() -> Boolean): T? =
    if (predicate(this)) this else null