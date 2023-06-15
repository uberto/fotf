package com.ubertob.fotf.exercises.chapter5

tailrec fun collatzR(acc: List<Int>, x: Int): List<Int> = when {
    x == 1 -> acc + 1
    x % 2 == 0 -> collatzR(acc + x, x / 2)
    else -> collatzR(acc + x, x * 3 + 1)
}

fun Int.collatz() = collatzR(listOf(), this)
