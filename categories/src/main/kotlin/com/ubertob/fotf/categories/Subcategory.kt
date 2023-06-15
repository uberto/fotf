package com.ubertob.fotf.categories

fun <T> identity(x: T): T = x

fun intToString(x: Int): String = x.toString()

fun boolToString(x: Boolean): String = x.toString()

fun isEven(x: Int): Boolean = x % 2 == 0

fun isOdd(x: Int): Boolean = !isEven(x)

fun reverse(s: String): String = s.reversed()