package com.ubertob.fotf.exercises.chapter2


typealias FUN<A, B> = (A) -> B

infix fun <A, B, C> FUN<A, B>.andThen(other: FUN<B, C>): FUN<A, C> = { a: A -> other(this(a)) }
