package com.ubertob.fotf.exercises.chapter8

data class NextFunction<T>(private val list: List<T>) {

    private var index = 0

    operator fun invoke(): T? =
        if (index >= list.size)
            null
        else
            list[index++]

}