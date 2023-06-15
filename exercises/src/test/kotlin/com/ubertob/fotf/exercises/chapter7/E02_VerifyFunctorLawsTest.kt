package com.ubertob.fotf.exercises.chapter7

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E02_VerifyFunctorLawsTest {

    @Test
    fun `functors must preserve identity morphisms`() {
        repeat(1000) {
            val o = randomOutcome()

            val o1 = o.transform(::identity)
            expectThat(o1).isEqualTo(o)
        }

    }

    @Test
    fun `functors must preserve composition of morphisms`() {

        repeat(1000) {
            val str = randomString()
            val o1 = str.asSuccess().transform { it.length }

            val o2 = str.length.asSuccess()
            expectThat(o1).isEqualTo(o2)
        }
    }
}