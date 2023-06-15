package com.ubertob.fotf.bowlingkata

import com.ubertob.fotf.bowlingkata.BowlingGameFP.Companion.newBowlingGame
import com.ubertob.fotf.bowlingkata.Pins.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class BowlingBowlingGameFPTest {

    private fun BowlingGameFP.rollMany(n: Int, pins: Pins): BowlingGameFP =
        generateSequence(1) { it + 1 }
            .take(n)
            .fold(this) { g, _ -> g.roll(pins) }

    fun BowlingGameFP.rollSpare(): BowlingGameFP = roll(five).roll(five)

    fun BowlingGameFP.rollStrike(): BowlingGameFP = roll(ten)

    @Test
    fun testGutterGame() {
        val g = newBowlingGame()
            .rollMany(20, zero)
        assertEquals(0, g.score)
    }

    @Test
    fun testAllOnes() {
        val g = newBowlingGame()
            .rollMany(20, one)

        assertEquals(20, g.score)
    }

    @Test
    fun testOneSpare() {
        val g = newBowlingGame()
            .rollSpare()
            .roll(three)

        assertEquals(16, g.score)
    }

    @Test
    fun testOneStrike() {
        val g = newBowlingGame()
            .rollStrike()
            .roll(three)
            .roll(four)
        assertEquals(24, g.score)
    }

    @Test
    fun testPerfectGame() {
        val g = newBowlingGame()
            .rollMany(12, ten)
        assertEquals(300, g.score)
    }

    @Test
    fun testAlmostPerfectGame() {
        val g = newBowlingGame()
            .rollMany(10, ten)
            .roll(nine)
            .roll(one)
        assertEquals(289, g.score)
    }

    @Test
    fun finalStrike() {
        val g = newBowlingGame()
            .rollMany(16, one)
            .roll(ten)
            .rollMany(2, one)
        assertEquals(30, g.score)
    }

    @Test
    fun testAll5() {
        val g = newBowlingGame()
            .rollMany(21, five)
        assertEquals(150, g.score)
    }

    @Test
    fun purity() {
        val nullScore: (List<Pins>) -> Int = { 0 }

        val g1 = BowlingGameFP(emptyList(), nullScore).roll(five).roll(four)

        val g2 = BowlingGameFP(listOf(five), nullScore).roll(four)

        val g3 = BowlingGameFP(listOf(five, four), nullScore)

        assertEquals(g1, g2)
        assertEquals(g1, g3)
    }

    @Test
    fun immutability() {
        val nullScore: (List<Pins>) -> Int = { 0 }

        val g1 = BowlingGameFP(emptyList(), nullScore).roll(five)

        val expected = BowlingGameFP(emptyList(), nullScore).roll(five).roll(four)

        assertEquals(expected, g1.roll(four))

        assertEquals(expected, g1.roll(four))
    }

}