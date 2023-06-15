package com.ubertob.fotf.exercises.chapter7

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E01_HolderCombineTest {


    @Test
    fun `combine two holders`() {
        val h = Holder("hello")
        val w = Holder("world")
        val hw = h.combine(w) { a, b -> "$a $b" }

        expectThat(hw).isEqualTo(Holder("hello world"))
    }
}