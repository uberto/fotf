package com.ubertob.fotf.zettai.fp

fun <A, B, R> ((A, B) -> R).curry(): (A) -> (B) -> R = { a -> { b -> invoke(a, b) } }

fun <A, B, C, R> ((A, B, C) -> R).curry(): (A) -> (B) -> (C) -> R = { a -> { b -> { c -> invoke(a, b, c) } } }

fun <A, B, C, D, R> ((A, B, C, D) -> R).curry(): (A) -> (B) -> (C) -> (D) -> R =
    { a -> { b -> { c -> { d -> invoke(a, b, c, d) } } } }
