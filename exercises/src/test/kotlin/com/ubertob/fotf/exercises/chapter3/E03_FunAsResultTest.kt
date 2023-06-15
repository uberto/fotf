package com.ubertob.fotf.exercises.chapter3

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E03_FunAsResultTest {

    @Test
    fun `char at pos function builder`() {
        val myCharAtPosKotlin = buildCharAtPos("Kotlin")
        expectThat(myCharAtPosKotlin(0)).isEqualTo('K')

        val myCharAtPosPragProg = buildCharAtPos("PragProg")
        expectThat(myCharAtPosPragProg(5)).isEqualTo('r')
    }

    private fun buildCharAtPos(text: String): (Int) -> Char =
        { pos -> text[pos] }
}