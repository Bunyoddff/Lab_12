package task14

sealed interface Message {
    val text: String
}

sealed interface Deliverable {
    fun deliver(): String
}

data class Email(override val text: String, val address: String) : Message, Deliverable {
    override fun deliver() = "email to $address: $text"
}

data class Sms(override val text: String, val phone: String) : Message, Deliverable {
    override fun deliver() = "sms to $phone: $text"
}

data class Note(override val text: String) : Message

data class Fax(override val text: String) : Message

fun describe(message: Message): String = when (message) {
    is Email -> "E-mail"
    is Sms -> "SMS"
    is Note -> "Note"
    is Fax -> "Fax"
}

fun main() {
    val messages = listOf(
        Email("Hi", "a@b.uz"),
        Sms("Yo", "+998901234567"),
        Note("Buy milk"),
        Fax("Important announcement")
    )
    for (m in messages) {
        println("${describe(m)}: ${m.text}")
        if (m is Deliverable) println(m.deliver())
    }
}

