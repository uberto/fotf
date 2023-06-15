@file:Suppress("DANGEROUS_CHARACTERS")

package com.ubertob.fotf.zettai.fp

interface Applicative<T>

typealias sequentialApplication<A, B> = (Applicative<(A) -> B>) -> (Applicative<A>) -> Applicative<B>


fun <A, B> List<(A) -> B>.andApply(a: List<A>): List<B> = flatMap { a.map(it) }

infix fun <A, B> List<(A) -> B>.`*`(a: List<A>): List<B> = andApply(a)


fun <A, B, R> ((A, B) -> R).transformAndCurry(other: List<A>): List<(B) -> R> =
    other.map { curry()(it) }

fun <A, B, C, R> ((A, B, C) -> R).transformAndCurry(other: List<A>): List<(B) -> (C) -> R> =
    other.map { curry()(it) }

fun <A, B, C, D, R> ((A, B, C, D) -> R).transformAndCurry(other: List<A>): List<(B) -> (C) -> (D) -> R> =
    other.map { curry()(it) }

infix fun <A, B, R> ((A, B) -> R).`!`(other: List<A>): List<(B) -> R> = transformAndCurry(other)
infix fun <A, B, C, R> ((A, B, C) -> R).`!`(other: List<A>): List<(B) -> (C) -> R> = transformAndCurry(other)
infix fun <A, B, C, D, R> ((A, B, C, D) -> R).`!`(other: List<A>): List<(B) -> (C) -> (D) -> R> =
    transformAndCurry(other)

