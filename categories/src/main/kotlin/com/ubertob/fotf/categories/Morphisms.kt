package com.ubertob.fotf.categories

typealias FUN<A, B> = (A) -> B

infix fun <A, B, C> FUN<A, B>.andThen(other: FUN<B, C>): FUN<A, C> = { a: A -> other(this(a)) }

typealias AsciiChar = Char

fun <T, U> isomorphism(f1: (T) -> U, f2: (U) -> T): (T) -> T = { x: T -> f2(f1(x)) }

fun <T> terminal(x: T): Unit = Unit

fun <T> initial(x: Nothing): T = TODO("this will never be called")

fun <T> reverseList(l: List<T>): List<T> = l.reversed()
