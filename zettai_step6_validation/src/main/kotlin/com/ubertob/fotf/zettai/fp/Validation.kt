package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.fp.Outcome.Companion.transform2Failures


fun <E : OutcomeError, T> List<Outcome<E, T>>.reduceFailures(f: (E, E) -> E): Outcome<E, T> =
    reduce { acc, r -> transform2Failures(acc, r, f) }


//alternative:
//fun <T, E : OutcomeError> T.validateWith(
//        validations: List<(T) -> Outcome<E, T>>,
//        combineErrors: (E, E) -> E
//): Outcome<E, T> =
//        ( validations `*` listOf(this) ).reduceFailures(combineErrors)

fun <T, E : OutcomeError> T.validateWith(
    validations: List<(T) -> Outcome<E, T>>,
    combineErrors: (E, E) -> E
): Outcome<E, T> =
    validations
        .map { it(this) }
        .reduceFailures(combineErrors)

