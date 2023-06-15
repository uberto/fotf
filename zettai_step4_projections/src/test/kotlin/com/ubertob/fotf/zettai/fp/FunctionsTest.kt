package com.ubertob.fotf.zettai.fp

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FunctionsTest {

    @Test
    fun liftList() {

        val strFirstLifted = liftList(String::first)
        val words: List<String> = listOf(
            "Cuddly", "Acrobatic", "Tenacious", "Softly-purring"
        )

        val initials: List<Char> = strFirstLifted(words)
        expectThat(initials).isEqualTo("CATS".toList())
    }
}