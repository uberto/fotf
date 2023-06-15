package com.ubertob.fotf.exercises.chapter11

import com.ubertob.fotf.exercises.chapter4.curry
import com.ubertob.fotf.exercises.chapter9.ContextReader


@Suppress("DANGEROUS_CHARACTERS")
infix fun <CTX, A, B> ContextReader<CTX, (A) -> B>.`*`(a: ContextReader<CTX, A>): ContextReader<CTX, B> =
    bind { a.transform(it) }


fun <CTX, A, B, C> ((A, B) -> C).transformAndCurry(other: ContextReader<CTX, A>): ContextReader<CTX, (B) -> C> =
    other.transform { curry()(it) }

infix fun <CTX, A, B, C> ((A, B) -> C).`!`(other: ContextReader<CTX, A>): ContextReader<CTX, (B) -> C> =
    transformAndCurry(other)


infix fun <CTX, A, B, C, D> ((A, B, C) -> D).transformAndCurry(other: ContextReader<CTX, A>): ContextReader<CTX, (B) -> (C) -> D> =
    other.transform { curry()(it) }


infix fun <CTX, A, B, C, D> ((A, B, C) -> D).`!`(other: ContextReader<CTX, A>): ContextReader<CTX, (B) -> (C) -> D> =
    transformAndCurry(other)
