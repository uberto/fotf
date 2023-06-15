package com.ubertob.fotf.bowlingkata

class BowlingGame {
    private val rolls = IntArray(21)
    private var currentRoll = 0

    fun roll(pins: Int) {
        rolls[currentRoll++] = pins
    }

    fun score(): Int {
        var score = 0
        var frameIndex = 0
        for (frame in 0..9) {
            if (isStrike(frameIndex)) {
                score += 10 + strikeBonus(frameIndex)
                frameIndex++
            } else if (isSpare(frameIndex)) {
                score += 10 + spareBonus(frameIndex)
                frameIndex += 2
            } else {
                score += sumOfBallsInFrame(frameIndex)
                frameIndex += 2
            }
        }
        return score
    }

    private fun isStrike(frameIndex: Int): Boolean {
        return rolls[frameIndex] == 10
    }

    private fun sumOfBallsInFrame(frameIndex: Int): Int {
        return rolls[frameIndex] + rolls[frameIndex + 1]
    }

    private fun spareBonus(frameIndex: Int): Int {
        return rolls[frameIndex + 2]
    }

    private fun strikeBonus(frameIndex: Int): Int {
        return rolls[frameIndex + 1] + rolls[frameIndex + 2]
    }

    private fun isSpare(frameIndex: Int): Boolean {
        return rolls[frameIndex] + rolls[frameIndex + 1] == 10
    }
}