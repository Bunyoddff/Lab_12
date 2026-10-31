package task06

inline val List<Int>.secondOrZero: Int
    get() = if (size >= 2) this[1] else 0

class Temperature(var celsius: Double) {
    inline var fahrenheit: Double
        get() = celsius * 9 / 5 + 32
        set(value) {
            celsius = (value - 32) * 5 / 9
        }
}

inline val String.wordCount: Int
    get() = if (this.isBlank()) 0 else this.trim().split(Regex("\\s+")).size

fun main() {
    println(listOf(5, 6, 7).secondOrZero)
    println(emptyList<Int>().secondOrZero)

    val t = Temperature(100.0)
    println(t.fahrenheit)
    t.fahrenheit = 32.0
    println(t.celsius)

    println("hello big world".wordCount)
}

