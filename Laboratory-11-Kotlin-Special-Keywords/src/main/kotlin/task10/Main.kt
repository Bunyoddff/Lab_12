package task10

fun fail(message: String): Nothing {
    throw IllegalStateException(message)
}

fun readAge(text: String): Int {
    val age = text.toIntOrNull() ?: fail("'$text' is not a number")
    if (age < 0) fail("age cannot be negative")
    return age
}

fun later(): Int = TODO("later")

fun main() {
    println(readAge("21"))

    val none: List<Nothing> = emptyList()
    val a: List<Int> = none
    val b: List<String> = none
    println("none lists: a=$a, b=$b")

    try {
        readAge("abc")
    } catch (e: IllegalStateException) {
        println("Caught expected exception: ${e.message}")
    }
}

