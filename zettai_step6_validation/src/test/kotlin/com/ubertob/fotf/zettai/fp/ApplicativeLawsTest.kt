package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.domain.tooling.randomString
import com.ubertob.fotf.zettai.domain.tooling.text
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate

internal class ApplicativeLawsTest {

    val x = randomString(text, 1, 10)
    val f: (String) -> Int = { it.length }
    val id: (String) -> String = { it }

    @Test
    fun `Holder law`() {
        expectThat(listOf(id) `*` listOf(x)).isEqualTo(listOf(x))
    }

    @Test
    fun `Homomorphism law`() {
        expectThat(listOf(f) `*` listOf(x)).isEqualTo(listOf(f(x)))
    }

    @Test
    fun `Interchange law`() {

        expectThat(
            listOf(f) `*` listOf(x)
        ).isEqualTo(
            listOf({ fx: (String) -> Int -> fx(x) }) `*` (listOf(f))
        )
    }


    @Test
    fun `Composition law`() {

        fun <A, B, C> composeFn(f: (B) -> C): ((A) -> B) -> (A) -> C =
            { fab -> { a -> f(fab(a)) } }

        val g: (LocalDate) -> String = { it.toString() }
        val d = LocalDate.now()

        expectThat(
            listOf({ fx: (String) -> Int -> composeFn<LocalDate, String, Int>(fx) })
                    `*` listOf(f) `*` listOf(g) `*` listOf(d)
        ).isEqualTo(
            listOf(f) `*` (listOf(g) `*` listOf(d))
        )
    }


}
