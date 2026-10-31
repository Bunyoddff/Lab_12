package task08

typealias Predicate<T> = (T) -> Boolean
typealias Scores = Map<String, List<Int>>
typealias Meters = Double
typealias Seconds = Double
typealias Callback = (String) -> Unit

fun filterAll(numbers: List<Int>, rule: Predicate<Int>): List<Int> =
    numbers.filter(rule)

fun speed(distance: Meters, time: Seconds): Double = distance / time

fun download(url: String, onDone: Callback) {
    onDone("done: $url")
}

fun main() {
    val isEven: Predicate<Int> = { it % 2 == 0 }
    val bigger: (Int) -> Boolean = { it > 2 }
    println(filterAll(listOf(1, 2, 3, 4), isEven))
    println(filterAll(listOf(1, 2, 3, 4), bigger))

    val scores: Scores = mapOf("Ali" to listOf(5, 4), "Vali" to listOf(3))
    println(scores["Ali"])

    val distance: Meters = 100.0
    val time: Seconds = 9.58
    println(speed(distance, time))

    download("https://example.com") { println(it) }
}

