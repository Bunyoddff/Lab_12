package task13

sealed class Shape {
    data class Circle(val radius: Double) : Shape()
    data class Rectangle(val width: Double, val height: Double) : Shape()
    data class Triangle(val base: Double, val height: Double) : Shape()
    data object Empty : Shape()
}

fun area(shape: Shape): Double = when (shape) {
    is Shape.Circle -> Math.PI * shape.radius * shape.radius
    is Shape.Rectangle -> shape.width * shape.height
    is Shape.Triangle -> 0.5 * shape.base * shape.height
    Shape.Empty -> 0.0
}

fun main() {
    val shapes = listOf(
        Shape.Circle(1.0),
        Shape.Rectangle(2.0, 3.0),
        Shape.Triangle(4.0, 3.0),
        Shape.Empty
    )
    for (s in shapes) {
        println("$s -> ${area(s)}")
    }
}

