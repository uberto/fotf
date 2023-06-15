package com.ubertob.fotf.exercises.chapter8

import com.ubertob.fotf.exercises.chapter8.ElevatorEvent.*
import com.ubertob.fotf.exercises.chapter8.ElevatorState.*


sealed class ElevatorEvent {

    abstract val elevatorId: Int

    data class ButtonPressed(override val elevatorId: Int, val floor: Int) : ElevatorEvent()
    data class ElevatorMoved(override val elevatorId: Int, val fromFloor: Int, val toFloor: Int) : ElevatorEvent()
    data class ElevatorBroken(override val elevatorId: Int) : ElevatorEvent()
    data class ElevatorFixed(override val elevatorId: Int) : ElevatorEvent()
}


sealed class ElevatorState {

    abstract val floor: Int

    data class DoorsOpenAtFloor(override val floor: Int) : ElevatorState()
    data class TravelingAtFloor(override val floor: Int) : ElevatorState()
    object OutOfOrder : ElevatorState() {
        override val floor: Int = 0
    }
}

data class ElevatorProjectionRow(val elevatorId: Int, val floor: Int, val state: ElevatorState)

fun foldEvents(events: List<ElevatorEvent>): ElevatorState =
    events.fold(DoorsOpenAtFloor(0) as ElevatorState) { state, event ->
        when (event) {
            is ButtonPressed ->
                if (state != DoorsOpenAtFloor(event.floor))
                    TravelingAtFloor(event.floor)
                else
                    state

            is ElevatorMoved -> DoorsOpenAtFloor(
                event.toFloor
            )

            is ElevatorBroken -> OutOfOrder
            is ElevatorFixed -> DoorsOpenAtFloor(0) // assume elevator goes back to ground floor when fixed

        }
    }


interface ElevatorProjection {
    fun allRows(): List<ElevatorProjectionRow>
    fun getRow(elevatorId: Int): ElevatorProjectionRow?
}

class ElevatorProjectionInMemory(events: List<ElevatorEvent>) : ElevatorProjection {
    //process all events in a map of elevatorstate
    val stateMap: Map<Int, ElevatorProjectionRow> = events.groupBy { it.elevatorId }
        .mapValues {
            println(it)

            foldEvents(it.value).toProjectionRow(it.key)
        }

    override fun allRows(): List<ElevatorProjectionRow> =
        stateMap.values.toList()


    override fun getRow(elevatorId: Int): ElevatorProjectionRow? =
        stateMap[elevatorId]

}

private fun ElevatorState.toProjectionRow(elevatorId: Int) =
    ElevatorProjectionRow(elevatorId, this.floor, this)
