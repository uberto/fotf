sealed class Door {
    data class Open(val angle: Double) : Door() {
        fun close() = Closed
        fun swing(delta: Double) = Open(angle + delta)
    }

    object Closed : Door() {
        fun open(degrees: Double) = Open(degrees)
        fun lock() = Locked(1)
    }

    data class Locked(val turns: Int) : Door() {
        fun unlock() = Closed
        fun turnKey(delta: Int) = Locked(turns + delta)
    }
}


typealias DoorEvent = (Door) -> Door

val unlockDoor: DoorEvent = { aDoor: Door ->
    when (aDoor) {
        is Door.Locked -> aDoor.unlock()
        else -> aDoor
    }
}

val door = Door
    .Closed
    .lock()
    .turnKey(3)
    .unlock()
    .open(12.4)
    .swing(34.5)
    .close()

println(door)


