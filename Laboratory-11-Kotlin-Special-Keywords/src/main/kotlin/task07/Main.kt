package task07

@PublishedApi
internal fun audit(message: String) {
    println("AUDIT: $message")
}

inline fun transfer(amount: Int, action: (Int) -> Unit) {
    audit("transfer $amount")
    action(amount)
}

fun main() {
    transfer(50) { println("moved $it") }
}

