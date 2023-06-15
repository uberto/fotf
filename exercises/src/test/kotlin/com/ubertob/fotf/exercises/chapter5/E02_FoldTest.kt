package com.ubertob.fotf.exercises.chapter5

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E02_FoldTest {

    @Test
    fun `fold elevator events`() {

        val values = listOf(Up, Up, Down, Up, Down, Down, Up, Up, Up, Down)

        val tot = values.fold(Elevator(0)) { elevator, direction ->
            when (direction) {
                is Up -> Elevator(elevator.floor + 1)
                is Down -> Elevator(elevator.floor - 1)
            }
        }

        expectThat(tot).isEqualTo(Elevator(2))

    }
}