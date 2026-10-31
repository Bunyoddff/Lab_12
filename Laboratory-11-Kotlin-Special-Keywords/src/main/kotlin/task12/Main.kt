package task12

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4);

    fun isUrgent(): Boolean = level >= 3

    companion object {
        fun fromLabel(text: String): Priority? =
            entries.firstOrNull { it.label.equals(text, ignoreCase = true) }
    }
}

data class Task(val title: String, val priority: Priority)

fun main() {
    val tasks = listOf(
        Task("Write report", Priority.MEDIUM),
        Task("Fix crash", Priority.HIGH),
        Task("Water plants", Priority.LOW),
        Task("Server down", Priority.CRITICAL)
    )
    tasks.sortedByDescending { it.priority.level }
        .forEach { println("${it.priority.label}: ${it.title}") }
    println("Urgent tasks: ${tasks.filter { it.priority.isUrgent() }}")

    println("Priority.HIGH.ordinal = ${Priority.HIGH.ordinal}")
    println("Priority.valueOf(\"LOW\").label = ${Priority.valueOf("LOW").label}")

    println("fromLabel(\"high\") = ${Priority.fromLabel("high")}")
    println("fromLabel(\"urgent\") = ${Priority.fromLabel("urgent")}")
}

