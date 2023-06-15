package com.ubertob.fotf.categories

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*

class MorphismsTest {

    @Test
    fun `verify isomorphism`() {
        val iso: (Byte) -> Byte = isomorphism(Byte::toChar, Char::toByte)

        repeat(100) {
            val x = Random().nextInt(256).toByte()

            expectThat(iso(x)).isEqualTo(x)

            println("$x  ${x.toChar()}")
        }
    }

}