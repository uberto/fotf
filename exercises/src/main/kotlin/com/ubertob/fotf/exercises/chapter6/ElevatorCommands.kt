package com.ubertob.fotf.exercises.chapter6

import com.ubertob.fotf.exercises.chapter6.ElevatorState.DoorsOpenAtFloor
import com.ubertob.fotf.exercises.chapter6.ElevatorState.TravelingAtFloor

sealed class ElevatorCommand {
    data class CallElevator(val floor: Int) : ElevatorCommand()
    data class GoToFloor(val floor: Int) : ElevatorCommand()
}

sealed class ElevatorState {
    data class DoorsOpenAtFloor(val floor: Int) : ElevatorState()
    data class TravelingAtFloor(val floor: Int) : ElevatorState()
}

fun handleCommand(state: ElevatorState, command: ElevatorCommand): ElevatorState {
    return when (command) {
        is ElevatorCommand.CallElevator -> {
            when (state) {
                is DoorsOpenAtFloor -> state // if doors are open, no need to do anything
                is TravelingAtFloor -> DoorsOpenAtFloor(command.floor) // if traveling, open doors at the called floor
            }
        }

        is ElevatorCommand.GoToFloor -> {
            when (state) {
                is DoorsOpenAtFloor -> TravelingAtFloor(command.floor) // if doors are open, start traveling
                is TravelingAtFloor -> state // if already traveling, no need to do anything
            }
        }
    }
}