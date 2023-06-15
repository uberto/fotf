package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.domain.tooling.digits
import com.ubertob.fotf.zettai.domain.tooling.lowercase
import com.ubertob.fotf.zettai.domain.tooling.randomString
import com.ubertob.fotf.zettai.domain.tooling.randomText
import com.ubertob.fotf.zettai.events.*
import java.time.Instant
import kotlin.random.Random.Default.nextInt

fun usersGenerator(): Sequence<User> = generateSequence {
    randomUser()
}

fun randomUser() = User.fromTrusted(randomString(lowercase, 3, 6).capitalize())

fun itemsGenerator(): Sequence<ToDoItem> = generateSequence {
    randomItem()
}

fun randomItem() = ToDoItem(randomString(lowercase + digits, 5, 20), null, ToDoStatus.Todo)


fun toDoListsGenerator(): Sequence<ToDoList> = generateSequence {
    randomToDoList()
}

fun randomToDoList(): ToDoList = ToDoList(
    randomListName(),
    itemsGenerator().take(nextInt(5) + 1).toList()
)


fun randomListName(): ListName = ListName.fromTrusted(randomString(lowercase, 5, 8))

fun randomEvent(): ToDoListEvent =
    when (val kClass = ToDoListEvent::class.sealedSubclasses.random()) {
        ListCreated::class -> ListCreated(ToDoListId.mint(), randomUser(), randomListName())
        ItemAdded::class -> ItemAdded(ToDoListId.mint(), randomItem())
        ItemRemoved::class -> ItemRemoved(ToDoListId.mint(), randomItem())
        ItemModified::class -> ItemModified(ToDoListId.mint(), randomItem(), randomItem())
        ListPutOnHold::class -> ListPutOnHold(ToDoListId.mint(), randomText(20))
        ListReleased::class -> ListReleased(ToDoListId.mint())
        ListClosed::class -> ListClosed(ToDoListId.mint(), Instant.now())
        else -> error("Unexpected class: $kClass")
    }

fun eventsGenerator(): Sequence<ToDoListEvent> = generateSequence {
    randomEvent()
}