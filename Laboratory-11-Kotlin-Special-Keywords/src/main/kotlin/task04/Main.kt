package task04

inline fun runLater(crossinline block: () -> Unit) {
    val task = Runnable { block() }
    task.run()
}

fun main() {
    runLater { println("running") }
    println("done")
}

