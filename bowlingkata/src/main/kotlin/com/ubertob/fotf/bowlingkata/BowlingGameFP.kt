package com.ubertob.fotf.bowlingkata

enum class Pins(val number: Int) {
    zero(0),
    one(1),
    two(2),
    three(3),
    four(4),
    five(5),
    six(6),
    seven(7),
    eight(8),
    nine(9),
    ten(10)
}

data class BowlingGameFP(val rolls: List<Pins>, val scoreFn: (List<Pins>) -> Int) {

    val score by lazy { scoreFn(rolls) }

    fun roll(pins: Pins): BowlingGameFP =
        copy(rolls = rolls + pins)

    companion object {
        fun newBowlingGame() = BowlingGameFP(emptyList(), Companion::calcBowlingScoreRec)


        fun calcBowlingScore(rolls: List<Pins>): Int {
            fun getRoll(roll: Int): Int = rolls.getOrElse(roll) { Pins.zero }.number
            fun isStrike(frameIndex: Int): Boolean = getRoll(frameIndex) == 10
            fun sumOfBallsInFrame(frameIndex: Int): Int = getRoll(frameIndex) + getRoll(frameIndex + 1)
            fun spareBonus(frameIndex: Int): Int = getRoll(frameIndex + 2)
            fun strikeBonus(frameIndex: Int): Int = getRoll(frameIndex + 1) + getRoll(frameIndex + 2)
            fun isSpare(frameIndex: Int): Boolean = getRoll(frameIndex) + getRoll(frameIndex + 1) == 10


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


        fun calcBowlingScoreRec(rolls: List<Pins>): Int {
            val lastFrame = 10
            val noOfPins = 10
            fun List<Int>.isStrike(): Boolean = first() == noOfPins
            fun List<Int>.isSpare(): Boolean = take(2).sum() == noOfPins

            fun calcFrameScore(frame: Int, rolls: List<Int>): Int =
                when {
                    frame == lastFrame || rolls.size < 3 ->
                        rolls.sum()

                    rolls.isStrike() ->
                        rolls.take(3).sum() + calcFrameScore(frame + 1, rolls.drop(1))

                    rolls.isSpare() ->
                        rolls.take(3).sum() + calcFrameScore(frame + 1, rolls.drop(2))

                    else ->
                        rolls.take(2).sum() + calcFrameScore(frame + 1, rolls.drop(2))
                }
            return calcFrameScore(1, rolls.map(Pins::number))
        }
    }
}

fun scoreAndLog(
    fnLog: (Int) -> Unit,
    fnScore: (List<Int>) -> Int
): (List<Int>) -> Int = { rolls ->
    fnScore(rolls).also(fnLog)
}