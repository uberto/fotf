package com.ubertob.fotf.exercises.chapter6

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E03_ElevatorBrokenTest {

    @Test
    fun `if elevator is broken it won't move`() {
        val events = listOf<ElevatorBroken.ElevatorEvent>(ElevatorBroken.ElevatorEvent.ElevatorBroken)
        val newEvents = events + ElevatorBroken.handleCommand(
            ElevatorBroken.foldEvents(events),
            ElevatorBroken.ElevatorCommand.CallElevator(5)
        )
        expectThat(ElevatorBroken.foldEvents(newEvents)).isEqualTo(ElevatorBroken.ElevatorState.OutOfOrder)
    }

    @Test
    fun `if elevator is fixed change status`() {

        val events = listOf<ElevatorBroken.ElevatorEvent>(ElevatorBroken.ElevatorEvent.ElevatorBroken)
        val newEvents = events + ElevatorBroken.handleCommand(
            ElevatorBroken.foldEvents(events),
            ElevatorBroken.ElevatorCommand.FixElevator
        )
        expectThat(ElevatorBroken.foldEvents(newEvents)).isEqualTo(ElevatorBroken.ElevatorState.DoorsOpenAtFloor(0))
    }
}
