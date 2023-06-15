package com.ubertob.fotf.exercises.chapter8

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.system.measureTimeMillis

class E01_SequencePerformanceTest {

    @Test
    fun performanceTest() {
        val numbers = (1..1_000_000L).toList()
        val numbersSeq = numbers.asSequence()
        expectThat(sumOfOddSquaresForLoop(numbers)).isEqualTo(166666666666500000)
        expectThat(sumOfOddSquaresList(numbers)).isEqualTo(166666666666500000)
        expectThat(sumOfOddSquaresSequence(numbersSeq)).isEqualTo(166666666666500000)

        repeat(10) {
            measureTimeMillis {
                sumOfOddSquaresForLoop(numbers)
            }.let { println("Time with for loop: $it ms") }

            measureTimeMillis {
                sumOfOddSquaresList(numbers)
            }.let { println("Time with list: $it ms") }

            measureTimeMillis {
                sumOfOddSquaresSequence(numbersSeq)
            }.let { println("Time with sequence: $it ms") }
        }
    }

    /*
    my timings
Time with for loop: 3 ms
Time with list: 6 ms
Time with sequence: 4 ms
     */

}