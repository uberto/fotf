package com.ubertob.fotf.exercises.chapter9

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random


class E01_MonadLawsTest {

    fun sum(x: Int): ContextReader<Int, Int> = ContextReader { it + x }

    @Test
    fun `left identity law`() {

        val value = Random.nextInt()

        val left = ContextReader { _: Int -> value }.bind(::sum)
        val right = sum(value)

        expectThat(left.runWith(0)).isEqualTo(right.runWith(0))

    }

    @Test
    fun `right identity law`() {
        val contextReader = ContextReader { _: Int -> "Hello" }

        val left = contextReader.bind { value -> ContextReader { _: Int -> value } }

        assert(left.runWith(0) == contextReader.runWith(0))
    }

    @Test
    fun `associativity law`() {
        val value = Random.nextInt()

        val double: (Int) -> ContextReader<Int, Int> = { ContextReader { it * 2 } }

        val left = ContextReader { _: Int -> value }.bind(::sum).bind(double)
        val right = ContextReader { _: Int -> value }.bind { x -> sum(x).bind(double) }

        val ctx = Random.nextInt()

        assert(left.runWith(ctx) == right.runWith(ctx))

    }
}