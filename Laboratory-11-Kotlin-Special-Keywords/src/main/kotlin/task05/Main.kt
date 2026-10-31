package task05

inline fun <reified T> List<Any>.firstOfType(): T? {
    println("looking for ${T::class.simpleName}")
    for (item in this) {
        if (item is T) return item
    }
    return null
}

inline fun <reified T> List<Any>.countOfType(): Int {
    var count = 0
    for (item in this) {
        if (item is T) count++
    }
    return count
}

fun main() {
    val things = listOf(1, "two", 3.0, "four")
    println(things.firstOfType<String>())
    println(things.firstOfType<Double>())
    println(things.firstOfType<Boolean>())
    println("String count: ${things.countOfType<String>()}")
}

