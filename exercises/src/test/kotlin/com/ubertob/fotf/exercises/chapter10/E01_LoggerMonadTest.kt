package com.ubertob.fotf.exercises.chapter10

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E01_LoggerMonadTest {
    @Test
    fun testMonadLaws() {
        val f: (Int) -> Logger<Int> = { value -> Logger(value * 2, listOf("multiplied by 2")) }
        val g: (Int) -> Logger<Int> = { value -> Logger(value + 1, listOf("increased by 1")) }

        val m = Logger(2, emptyList())

        // Left identity
        expectThat(m.bind(f)).isEqualTo(f(2))

        // Right identity
        val pure = { value: Int -> Logger(value, emptyList()) }
        expectThat(m.bind(pure)).isEqualTo(m)

        // Associativity
        expectThat((m.bind(f)).bind(g)).isEqualTo(m.bind { x -> (f(x).bind(g)) })
    }

}