package com.ubertob.fotf.categories

data class User(val name: String, val id: Int) {
    constructor(pair: Pair<String, Int>) :
            this(pair.first, pair.second)

    fun toPair(): Pair<String, Int> = name to id
}

val userPairIso: (Pair<String, Int>) -> Pair<String, Int> = isomorphism(::User, User::toPair)
