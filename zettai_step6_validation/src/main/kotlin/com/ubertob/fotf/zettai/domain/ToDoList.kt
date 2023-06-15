package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.fp.*
import java.time.LocalDate
import java.util.*

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.getDefault()
    ) else it.toString()
}

val pathElementPattern = Regex(pattern = "[A-Za-z0-9-]+")

data class ListName internal constructor(val name: String) {
    companion object {
        fun fromTrusted(name: String): ListName = ListName(name)
        fun fromUntrustedOrThrow(name: String): ListName =
            fromUntrusted(name).onFailure { error(it.msg) }

        fun fromUntrusted(name: String): ListNameOutcome =
            name.validateListName(::nameTooShort, ::nameTooLong, ::nameWithInvalidChars)
    }
}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)


data class ToDoItem(
    val description: String,
    val dueDate: LocalDate? = null,
    val status: ToDoStatus = ToDoStatus.Todo
) {
    fun markAsDone(): ToDoItem = copy(status = ToDoStatus.Done)
}

enum class ToDoStatus { Todo, InProgress, Done, Blocked }


typealias ListNameValidation = (String) -> Outcome<ValidationError, String>

typealias ListNameOutcome = Outcome<ValidationError, ListName>


private fun String.validateListName(vararg validations: ListNameValidation): ListNameOutcome =
    validateWith(validations.toList(), ValidationError::combine)
        .transform { ListName.fromTrusted(it) }


fun nameTooShort(name: String) =
    name.discardUnless { length >= 3 }
        .failIfNull(ValidationError("Name is too short!"))


fun nameTooLong(name: String) =
    name.discardUnless { length <= 40 }
        .failIfNull(ValidationError("Name ${name} is too long"))


fun nameWithInvalidChars(name: String) =
    name.discardUnless { matches(pathElementPattern) }
        .failIfNull(
            ValidationError(
                "Name ${name} contains illegal characters:" +
                        " only letters, digits, and hyphen are allowed"
            )
        )
