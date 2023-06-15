package com.ubertob.fotf.exercises.chapter5

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E04_MonoidTest {

    @Test
    fun `verify monoid of Int`() {

        with(Monoid(0, Int::plus)) {
            expectThat(listOf(1, 2, 3, 4, 10).fold())
                .isEqualTo(20)
        }
    }

    @Test
    fun `verify monoid of String`() {

        with(Monoid("", String::plus)) {
            expectThat(listOf("My", "Fair", "Lady").fold())
                .isEqualTo("MyFairLady")
        }
    }

    @Test
    fun `verify monoid of Money`() {

        with(Monoid(zeroMoney, Money::sum)) {
            expectThat(
                listOf(
                    Money(2.1),
                    Money(3.9),
                    Money(4.0)
                ).fold()
            )
                .isEqualTo(Money(10.0))
        }
    }
}
