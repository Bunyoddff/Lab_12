package task11

enum class Direction {
    NORTH, EAST, SOUTH, WEST
}

fun turnRight(d: Direction): Direction = when (d) {
    Direction.NORTH -> Direction.EAST
    Direction.EAST -> Direction.SOUTH
    Direction.SOUTH -> Direction.WEST
    Direction.WEST -> Direction.NORTH
}

fun turnLeft(d: Direction): Direction = when (d) {
    Direction.NORTH -> Direction.WEST
    Direction.EAST -> Direction.NORTH
    Direction.SOUTH -> Direction.EAST
    Direction.WEST -> Direction.SOUTH
}

fun main() {
    for (d in Direction.entries) {
        println("${d.ordinal}: ${d.name} -> ${turnRight(d)}")
    }
    println(Direction.valueOf("SOUTH"))
    println(Direction.NORTH < Direction.SOUTH)

    println("Safe lookup for UP: ${Direction.entries.firstOrNull { it.name == "UP" }}")

    for (d in Direction.entries) {
        println("turnLeft(turnRight($d)) = ${turnLeft(turnRight(d))}")
    }
}

