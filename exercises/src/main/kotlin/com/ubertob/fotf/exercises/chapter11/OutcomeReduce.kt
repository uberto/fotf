package com.ubertob.fotf.exercises.chapter11

import com.ubertob.fotf.exercises.chapter7.Failure
import com.ubertob.fotf.exercises.chapter7.Outcome
import com.ubertob.fotf.exercises.chapter7.OutcomeError
import com.ubertob.fotf.exercises.chapter7.Success

fun <E : OutcomeError, T, U> Outcome<E, T>.bind(f: (T) -> Outcome<E, U>): Outcome<E, U> =
    when (this) {
        is Success -> f(value)
        is Failure -> this
    }

fun <E : OutcomeError, S, T : S> List<Outcome<E, T>>.reduceSuccess(f: (S, T) -> T): Outcome<E, S> =
    reduce { acc, r ->
        acc.bind { a: T ->
            r.transform { b ->
                f(a, b)
            }
        }
    }

