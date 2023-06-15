package com.ubertob.fotf.exercises.chapter4

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E03_CurryingTest {

    fun sum(num1: Int, num2: Int) = num1 + num2
    fun strConcat(s1: String, s2: String) = "$s1 $s2"

    @Test
    fun `simple currying`() {
        val plus3Fn = ::sum.curry()(3)
        expectThat(plus3Fn(4)).isEqualTo(7)

        val starPrefixFn = ::strConcat.curry()("*")
        expectThat(starPrefixFn("abc")).isEqualTo("* abc")
    }

    @Test
    fun `infix partial application`() {
        val curriedConcat = ::strConcat.curry()
        expectThat(curriedConcat `+++` "head" `+++` "tail")
            .isEqualTo("head tail")

        val curriedSum = ::sum.curry()
        expectThat(curriedSum `+++` 4 `+++` 5).isEqualTo(9)

    }


}