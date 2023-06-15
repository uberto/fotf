package com.ubertob.fotf.exercises.chapter5


data class Monoid<T : Any>(val zero: T, val combination: (T, T) -> T) {
    fun List<T>.fold(): T = fold(zero, combination)
}

val zeroMoney = Money(0.0)

data class Money(val amount: Double) {
    fun sum(other: Money) = Money(this.amount + other.amount)
}
