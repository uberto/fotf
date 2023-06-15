package com.ubertob.fotf.exercises.chapter8

import com.ubertob.fotf.exercises.chapter8.ElevatorEvent.*
import com.ubertob.fotf.exercises.chapter8.ElevatorState.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E03_ElevatorProjectionTest {

    @Test
    fun `generate projection from elevator state`() {

        val events = listOf(
            ElevatorMoved(0, 0, 0),
            ElevatorMoved(1, 0, 0),
            ElevatorMoved(2, 0, 0),
            ButtonPressed(1, 5),
            ElevatorMoved(1, 0, 5),
            ButtonPressed(0, 6),
            ElevatorBroken(2)
        )

        val projection = ElevatorProjectionInMemory(events)

        expectThat(projection.getRow(0))
            .isEqualTo(ElevatorProjectionRow(0, 6, TravelingAtFloor(6)))
        expectThat(projection.getRow(1))
            .isEqualTo(ElevatorProjectionRow(1, 5, DoorsOpenAtFloor(5)))
        expectThat(projection.getRow(2))
            .isEqualTo(ElevatorProjectionRow(2, 0, OutOfOrder))

    }
}