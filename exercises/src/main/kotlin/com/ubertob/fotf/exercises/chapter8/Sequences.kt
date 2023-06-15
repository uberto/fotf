package com.ubertob.fotf.exercises.chapter8

fun Long.isOdd(): Boolean =
    this % 2 != 0L

fun sumOfOddSquaresList(numbers: List<Long>): Long =
    numbers.filter { it.isOdd() }.map { it * it }.sum()

fun sumOfOddSquaresSequence(numbers: Sequence<Long>): Long =
    numbers.filter { it.isOdd() }.map { it * it }.sum()

fun sumOfOddSquaresForLoop(numbers: List<Long>): Long {
    var tot = 0L
    for (i in numbers) {
        if (i.isOdd()) tot += i * i
    }
    return tot
}