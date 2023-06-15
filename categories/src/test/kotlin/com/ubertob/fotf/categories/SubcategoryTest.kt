package com.ubertob.fotf.categories

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*

class SubcategoryTest {
    val random = Random()

    @Test
    fun `subcategory respects the associativity law`() {

        val f1 = (::isOdd andThen ::boolToString) andThen ::reverse
        val f2 = ::isOdd andThen (::boolToString andThen ::reverse)

        repeat(100) {
            val number = random.nextInt()
            expectThat(f1(number)).isEqualTo(f2(number))
        }
    }

    @Test
    fun `subcategory respects the identity law`() {
        val idOdd = ::isOdd andThen ::identity

        repeat(100) {
            val number = random.nextInt()
            expectThat(idOdd(number)).isEqualTo(isOdd(number))
        }
    }

    fun randomString(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val stringLength = random.nextInt(20)
        val randomString = (1..stringLength)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
        return randomString
    }
}