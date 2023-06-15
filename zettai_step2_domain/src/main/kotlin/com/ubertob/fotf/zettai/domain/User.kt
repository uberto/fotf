package com.ubertob.fotf.zettai.domain

data class User internal constructor(val name: String) {
    companion object {
        fun fromTrusted(name: String): User = User(name)
        fun fromUntrusted(name: String): User? =
            if (name.matches(pathElementPattern) && name.length in 1..40) fromTrusted(name) else null
    }
}