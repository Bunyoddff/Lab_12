package task01

inline fun logged(name: String, block: () -> Unit) {
    println("start $name")
    block()
    println("end $name")
}

fun main() {
    var counter = 0
    logged("count") {
        counter++
        println("counter = $counter")
    }
}

