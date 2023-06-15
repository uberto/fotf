package com.ubertob.fotf.exercises.chapter8

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E02_StatefulFunctionTest {

    @Test
    fun `next function keeps the status`() {
        val names = listOf("Ann", "Bob", "Charlie", "Dorothy")

        val nextName = NextFunction(names)

        expectThat(nextName()).isEqualTo("Ann")
        expectThat(nextName()).isEqualTo("Bob")
        expectThat(nextName()).isEqualTo("Charlie")
        expectThat(nextName()).isEqualTo("Dorothy")
        expectThat(nextName()).isEqualTo(null)
    }
}