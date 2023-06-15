package com.ubertob.fotf.zettai.domain.tooling

import com.ubertob.fotf.zettai.fp.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T =
    onFailure { error -> fail { "$this expected success but was $error" } }


fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }


class OutcomeTests {
    data class DivisionError(override val msg: String) : OutcomeError

    val divFail = DivisionError("You cannot divide by zero").asFailure()

    private fun divide100by(x: Int): Outcome<DivisionError, Int> =
        if (x == 0) divFail else (100 / x).asSuccess()

    @Test
    fun `binding two outcome together`() {
        val valid = 5.asSuccess()
        expectThat(valid.bind(::divide100by)).isEqualTo(20.asSuccess())

        val invalid = DivisionError("generic error").asFailure()
        expectThat(invalid.bind(::divide100by)).isEqualTo(invalid)

        val zero = 0.asSuccess()
        expectThat(zero.bind(::divide100by)).isEqualTo(divFail)
    }

    @Test
    fun `joining two outomes together`() {
        val valid = 10.asSuccess().asSuccess()
        expectThat(valid.join()).isEqualTo(10.asSuccess())


        val error = DivisionError("generic error")
        val invalid = error.asFailure().asSuccess()
        expectThat(invalid.join()).isEqualTo(error.asFailure())

    }

}
