package com.ubertob.fotf.exercises.chapter4

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E01_DiscardUnlessTest {

    @Test
    fun `discard unless`() {
        val itemInProgress = ToDoItem(
            "doing something",
            status = ToDoStatus.InProgress
        )
        val itemBlocked = ToDoItem(
            "must do something",
            status = ToDoStatus.Blocked
        )
        expectThat(
            itemInProgress.discardUnless { status == ToDoStatus.InProgress }
        ).isEqualTo(itemInProgress)

        expectThat(
            itemBlocked.discardUnless { status == ToDoStatus.InProgress }
        ).isEqualTo(null)
    }
}