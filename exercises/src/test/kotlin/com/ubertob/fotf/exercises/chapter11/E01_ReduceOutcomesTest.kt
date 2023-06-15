package com.ubertob.fotf.exercises.chapter11

import com.ubertob.fotf.exercises.chapter7.Outcome
import com.ubertob.fotf.exercises.chapter7.OutcomeError
import com.ubertob.fotf.exercises.chapter7.asFailure
import com.ubertob.fotf.exercises.chapter7.asSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E01_ReduceOutcomesTest {

    data class BaseError(override val msg: String) : OutcomeError

    @Test
    fun `reduceSuccess should reduce all success outcomes into one`() {
        val list = listOf(
            1.asSuccess(),
            2.asSuccess(),
            3.asSuccess()
        )

        val result = list.reduceSuccess { acc: Int, next: Int ->
            acc + next
        }

        expectThat(result).isEqualTo((1 + 2 + 3).asSuccess())

    }

    @Test
    fun `reduceSuccess should return first error if any`() {
        val list = listOf(
            1.asSuccess(),
            BaseError("An error occurred").asFailure(),
            3.asSuccess()
        )

        val result: Outcome<OutcomeError, Int> = list.reduceSuccess { acc: Int, next: Int ->
            acc + next
        }

        expectThat(result).isEqualTo(BaseError("An error occurred").asFailure())

    }
}