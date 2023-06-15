package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoStatus
import com.ubertob.fotf.zettai.domain.ZettaiParsingError
import com.ubertob.fotf.zettai.domain.tooling.expectFailure
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate

class ListApplicativeTest {

    @Test
    fun `andApply allow to compose functions`() {
        val strNumbers = listOf(Int::toString).andApply(listOf(1, 1, 2, 3, 5, 8))

        expectThat(strNumbers).isEqualTo(
            listOf("1", "1", "2", "3", "5", "8")
        )


        val exprMap = listOf("hmm", "ouch", "wow").map { s1 -> { s2: String -> s1 + s2 } }.andApply(listOf("?", "!"))

        expectThat(exprMap.joinToString()).isEqualTo(
            "hmm?, hmm!, ouch?, ouch!, wow?, wow!"
        )


        val exprCurry = listOf(String::plus.curry()).andApply(listOf("hmm", "ouch", "wow")).andApply(listOf("?", "!"))
        expectThat(exprCurry.joinToString()).isEqualTo(
            "hmm?, hmm!, ouch?, ouch!, wow?, wow!"
        )

    }

    @Test
    fun `transformAndCurry and the infix notation`() {
        val applExpressions =
            String::plus.transformAndCurry(listOf("hmm", "ouch", "wow")).andApply(listOf("?", "!"))
        expectThat(applExpressions.joinToString()).isEqualTo(
            "hmm?, hmm!, ouch?, ouch!, wow?, wow!"
        )


        val applExpressionsInfix = String::plus `!` listOf("hmm", "ouch", "wow") `*` listOf("?", "!")
        expectThat(applExpressionsInfix.joinToString()).isEqualTo(
            "hmm?, hmm!, ouch?, ouch!, wow?, wow!"
        )
    }

    @Test
    fun `Infix notation work with ToDoItems as well`() {

        val names = listOf("a", "b", "c")
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val dates = listOf(today, yesterday)
        val statuses = listOf(ToDoStatus.Todo, ToDoStatus.InProgress)


        val items = ::ToDoItem.transformAndCurry(names).andApply(dates).andApply(statuses)
        expectThat(items.toSet()).isEqualTo(
            setOf(
                ToDoItem("a", today, ToDoStatus.Todo),
                ToDoItem("b", today, ToDoStatus.Todo),
                ToDoItem("c", today, ToDoStatus.Todo),
                ToDoItem("a", yesterday, ToDoStatus.Todo),
                ToDoItem("b", yesterday, ToDoStatus.Todo),
                ToDoItem("c", yesterday, ToDoStatus.Todo),
                ToDoItem("a", today, ToDoStatus.InProgress),
                ToDoItem("b", today, ToDoStatus.InProgress),
                ToDoItem("c", today, ToDoStatus.InProgress),
                ToDoItem("a", yesterday, ToDoStatus.InProgress),
                ToDoItem("b", yesterday, ToDoStatus.InProgress),
                ToDoItem("c", yesterday, ToDoStatus.InProgress)
            )
        )

        val items2 = ::ToDoItem `!` names `*` dates `*` statuses
        expectThat(items2).isEqualTo(items)


        val successItem = ::ToDoItem `!` "abc".asSuccess() `*` today
            .asSuccess() `*` ToDoStatus.InProgress.asSuccess()

        expectThat(successItem.expectSuccess()).isEqualTo(
            ToDoItem("abc", today, ToDoStatus.InProgress)
        )


    }

    @Test
    fun `Infix notation failures `() {
        val failedItem1 = ::ToDoItem `!` "abc".asSuccess() `*`
                ZettaiParsingError("No date").asFailure() `*`
                ZettaiParsingError("No status").asFailure()

        expectThat(failedItem1.expectFailure().msg).isEqualTo("No date")

    }
}