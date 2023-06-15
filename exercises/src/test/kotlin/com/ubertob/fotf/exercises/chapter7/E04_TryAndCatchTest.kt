package com.ubertob.fotf.exercises.chapter7

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class E04_TryAndCatchTest {

    @Test
    fun `try to convert a correct string into a date`() {
        val res = todayGreetings("2020-03-12")

        expectThat(res).isEqualTo("Today is 2020-03-12".asSuccess())
    }

    @Test
    fun `try to convert a wrong string into a date`() {
        val res = todayGreetings("12/3/2020")

        expectThat(res).isA<Failure<ThrowableError>>()
    }
}