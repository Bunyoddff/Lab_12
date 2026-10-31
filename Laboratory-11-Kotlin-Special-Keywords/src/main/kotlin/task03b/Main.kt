package task03b

fun main() {
    for (n in 1..10) {
        val odd = n.takeIf { it % 2 == 1 } ?: run { continue }
        if (odd > 7) break
        println(odd)
    }
}

