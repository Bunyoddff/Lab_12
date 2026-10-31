package task09

data class Book(val title: String, val pages: Int)

class Point(val x: Int, val y: Int) {
    override fun toString() = "Point($x, $y)"
    override fun equals(other: Any?): Boolean =
        other is Point && x == other.x && y == other.y
    override fun hashCode(): Int = 31 * x + y
}

fun describe(value: Any) {
    when (value) {
        is String -> println("String of length ${value.length}")
        is Int -> println("Int, doubled is ${value * 2}")
        else -> println("Something else: $value")
    }
}

fun main() {
    val b1 = Book("Kotlin", 300)
    val b2 = Book("Kotlin", 300)
    println(b1)
    println(b1 == b2)

    val p1 = Point(1, 2)
    val p2 = Point(1, 2)
    println(p1)
    println(p1 == p2)
    println(p1 === p2)

    describe("hello")
    describe(21)
    describe(3.5)

    val anything: Any = "text"
    val s = anything as String
    println(s.length)

    val n = anything as? Int
    println(n)

    val bad: Any? = null
    println("bad is null: ${bad == null}")
}

