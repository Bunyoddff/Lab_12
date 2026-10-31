package task02

val saved = mutableListOf<() -> Unit>()

inline fun runAndSave(first: () -> Unit, noinline second: () -> Unit) {
    first()
    second()
    saved.add(second)
}

fun main() {
    runAndSave({ println("first") }, { println("second") })
    saved.forEach { it() }
}

