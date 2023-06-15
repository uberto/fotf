package com.ubertob.fotf.exercises.chapter5

data class Elevator(val floor: Int)

sealed class Direction

object Up : Direction()

object Down : Direction()
