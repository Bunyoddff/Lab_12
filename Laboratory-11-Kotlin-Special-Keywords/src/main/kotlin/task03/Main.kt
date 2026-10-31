package task03

fun firstEven(numbers: List<Int>): Int? {
    numbers.forEach { n ->
        if (n % 2 == 0) return n
    }
    return null
}

inline fun myForEach(list: List<Int>, action: (Int) -> Unit) {
    for (item in list) action(item)
}

fun main() {
    println(firstEven(listOf(1, 3, 4, 5, 6)))
    println(firstEven(listOf(1, 3, 5)))
}

