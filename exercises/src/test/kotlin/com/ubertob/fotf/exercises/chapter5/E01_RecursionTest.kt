package com.ubertob.fotf.exercises.chapter5

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E01_RecursionTest {

    @Test
    fun `expand collatz sequence`() {
        expectThat(13.collatz()).isEqualTo(listOf(13, 40, 20, 10, 5, 16, 8, 4, 2, 1))
        expectThat(8.collatz()).isEqualTo(listOf(8, 4, 2, 1))
    }
}