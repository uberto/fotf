package com.ubertob.fotf.categories

import java.util.*

typealias SomeContext = Properties

data class ContextReader<CTX, out T>(val runWith: (CTX) -> T) {

    fun <U> transform(f: (T) -> U): ContextReader<CTX, U> = ContextReader { ctx -> f(runWith(ctx)) }

    fun <U> bind(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U> =
        ContextReader { ctx -> f(runWith(ctx)).runWith(ctx) }

}

typealias KArrow<A, B> = (A) -> ContextReader<SomeContext, B>

infix fun <A, B, C> KArrow<A, B>.fish(other: KArrow<B, C>): KArrow<A, C> =
    { a -> this(a).bind { b -> other(b) } }


//example

val myProperties = Properties().apply {
    setProperty("k1", "v1")
    setProperty("environment", "DEV")
    setProperty("DEV.url", "http://dev.example.com")
    setProperty("PROD.url", "http://example.com")
}

typealias ConfReader<T> = ContextReader<Properties, T>

fun readProp(propName: String): ConfReader<String> = ContextReader { it[propName].toString() }

fun readUrl(env: String) = readProp("${env}.url")

fun main() {
    val arrows = ::readProp fish ::readUrl
    val envUrl = arrows("environment").runWith(myProperties)

    println(envUrl) //http://dev.example.com
}
