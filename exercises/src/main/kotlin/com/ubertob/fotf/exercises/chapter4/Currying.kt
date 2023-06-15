package com.ubertob.fotf.exercises.chapter4

fun <A, B, C> ((A, B) -> C).curry(): (A) -> (B) -> C = { a: A -> { b: B -> this(a, b) } }

fun <A, B, C, D> ((A, B, C) -> D).curry(): (A) -> (B) -> (C) -> D =
    { a: A -> { b: B -> { c: C -> this(a, b, c) } } }

fun <A, B, C, D, E> ((A, B, C, D) -> E).curry(): (A) -> (B) -> (C) -> (D) -> E =
    { a: A -> { b: B -> { c: C -> { d: D -> this(a, b, c, d) } } } }

fun <A, B, C, D, E, F> ((A, B, C, D, E) -> F).curry(): (A) -> (B) -> (C) -> (D) -> (E) -> F =
    { a: A -> { b: B -> { c: C -> { d: D -> { e: E -> this(a, b, c, d, e) } } } } }

infix fun <A, B> ((A) -> B).`+++`(a: A): B = this(a)
