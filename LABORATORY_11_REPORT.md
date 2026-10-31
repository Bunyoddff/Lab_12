# Laboratory 11: Kotlin Special Keywords and Special Classes
## Software Mobile Development — Laboratory Report (Lecture 12)

---

### Executive Overview

This laboratory explores advanced Kotlin compiler mechanics, optimization keywords, and special type systems across 15 structured tasks:
- **Part A (Tasks 1–10): Special Keywords** — `inline`, `noinline`, non-local returns, `crossinline`, `reified` type parameters, inline properties, `@PublishedApi`, `typealias`, `Any`, and `Nothing`.
- **Part B (Tasks 11–15): Special Classes** — `enum class`, `sealed class`, and `sealed interface`, including exhaustive `when` matching and architectural design trade-offs.

All source code is implemented, organized into dedicated packages (`task01` through `task15`, plus `task03b`), and verified with the Kotlin 2.3 compiler.

---

## Part A — Special Keywords (Tasks 1–10)

### Task 1 — `inline` (Experiment)

#### 1. Code Implementation (`task01/Main.kt`)
```kotlin
package task01

inline fun logged(name: String, block: () -> Unit) {
    println("start $name")
    block()
    println("end $name")
}

fun main() {
    var counter = 0
    logged("count") {
        counter++
        println("counter = $counter")
    }
}
```

#### 2. Expected & Actual Output
```
start count
counter = 1
end count
```

#### 3. Bytecode Analysis & Questions Answered
* **Is there a call to `logged(...)` in decompiled `main`?**
  **No.** When decompiling with `javap -c`, the `main()` method contains no `invokestatic` call to `logged()`. The entire body of `logged()` along with the lambda body is copied directly into `main()`.
* **Is there an object for the lambda (`Function0`) or a `Ref.IntRef` holding `counter`?**
  **No.** Because the lambda is inlined, `counter` is maintained purely as a primitive local integer variable in bytecode (`iload_0`, `iadd`, `istore_0`). No heap allocation occurs.
* **What changes when `inline` is removed?**
  1. The compiler generates an anonymous class implementing `kotlin.jvm.functions.Function0` for the lambda.
  2. Because the lambda mutates the local variable `counter` across class boundaries, Kotlin must wrap `counter` inside an object on the heap: `final Ref.IntRef counter = new Ref.IntRef(); counter.element = 0;`.
  3. Every call allocates both the `Function0` instance and the `Ref.IntRef` instance.
* **Why does the lambda need `Ref.IntRef` to change `counter`?**
  In the Java Virtual Machine, anonymous inner classes can only capture effectively final local variables from their enclosing method. To allow the lambda object to mutate `counter`, Kotlin wraps the primitive integer inside a mutable heap-allocated container (`Ref.IntRef`), passing the reference to the lambda.
* **Risk of marking a big function with `inline`, and where it pays off:**
  - **Risk:** Code bloat (bytecode size explosion) because large blocks of bytecode are duplicated at every single call site.
  - **Payoff:** It pays off for higher-order functions taking lambdas that are called frequently in hot code paths, eliminating the object allocation of closures (`FunctionN`) and virtual invocation overhead, as well as enabling non-local returns and reified generics.

---

### Task 2 — `noinline` (Experiment)

#### 1. Code Implementation (`task02/Main.kt`)
```kotlin
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
```

#### 2. Expected & Actual Output
```
first
second
second
```

#### 3. Questions Answered
* **What happens when uncommenting `saved.add(second)` without `noinline`?**
  The compiler reports:
  > `Illegal usage of inline-parameter 'second'. Add 'noinline' modifier to the parameter declaration.`
  Android Studio suggests adding the `noinline` modifier to `second`.
* **Why does this error occur?**
  An inlined lambda does not exist as an object instance at runtime (its instructions are pasted directly into the caller). Therefore, it cannot be passed to a collection, stored in a field, or retained beyond the immediate call.
* **How many times is `second` printed, and by which lines?**
  `second` is printed **2 times**:
  1. Once inside `runAndSave()` when `second()` is called.
  2. Once inside `main()` when `saved.forEach { it() }` executes the stored lambda.
* **What happens if we also add `saved.add(first)`?**
  The same compilation error occurs for `first`. To store `first`, we would have to mark `first` with `noinline` as well.

---

### Task 3 — Non-local Return, Break and Continue (Experiment)

#### 1. Code Implementation (`task03/Main.kt`)
```kotlin
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
```

#### 2. Kotlin 2.2+ Break & Continue (`task03b/Main.kt`)
```kotlin
package task03b

fun main() {
    for (n in 1..10) {
        val odd = n.takeIf { it % 2 == 1 } ?: run { continue }
        if (odd > 7) break
        println(odd)
    }
}
```

#### 3. Expected & Actual Output
- **`task03`:**
  ```
  4
  null
  ```
- **`task03b`:**
  ```
  1
  3
  5
  7
  ```

#### 4. Questions Answered
* **Why does `return n` inside `forEach` exit `firstEven`, and not just the lambda?**
  `forEach` is declared `inline`. The lambda body is inlined directly into the call site inside `firstEven`. Therefore, an unqualified `return n` is in the lexical scope of `firstEven` and acts as a **non-local return**, returning from `firstEven` immediately.
* **What happens when replacing `numbers.forEach` with `myForEach` (without `inline`)?**
  The compiler throws:
  > `'return' is not allowed here.`
  Because `myForEach` is not inline, the lambda is an object whose execution lifecycle may differ from `firstEven`, so non-local returns are prohibited.
* **What happens if we change `return n` to `return@myForEach`?**
  `firstEven(listOf(1, 3, 4, 5, 6))` returns `null`! `return@myForEach` is a local return that only terminates that specific invocation of the lambda (behaving like `continue` in a loop). The loop completes all numbers and reaches `return null`.
* **Behavior of `task03b` when changing 7 to 5:**
  If `if (odd > 7) break` is changed to `if (odd > 5) break`:
  The loop breaks when `odd` is 7, printing `1`, `3`, `5`.

---

### Task 4 — `crossinline` (Experiment)

#### 1. Code Implementation (`task04/Main.kt`)
```kotlin
package task04

inline fun runLater(crossinline block: () -> Unit) {
    val task = Runnable { block() }
    task.run()
}

fun main() {
    runLater { println("running") }
    println("done")
}
```

#### 2. Expected & Actual Output
```
running
done
```

#### 3. Questions Answered
* **What happens when removing `crossinline`?**
  Compiler error at `Runnable { block() }`:
  > `Can't inline 'block' here: it may contain non-local returns. Add 'crossinline' modifier to parameter declaration 'block'.`
* **What happens if `main` attempts `runLater { println("running"); return }`?**
  Compiler error:
  > `'return' is not allowed here.`
  Because `block` is marked `crossinline`, non-local returns are disallowed. Changing it to `return@runLater` allows local return and prints `running` followed by `done`.
* **Difference between `noinline` and `crossinline`:**
  - `noinline` completely disables inlining for the parameter, creating a real `FunctionN` object at runtime that can be stored or passed anywhere.
  - `crossinline` preserves inlining for performance (no function call overhead), but strictly disallows non-local returns because the lambda is invoked from an execution context (e.g. Runnable, nested lambda, object literal) where returning from the outer enclosing function is impossible.

---

### Task 5 — `reified` Type Parameters (Experiment)

#### 1. Code Implementation (`task05/Main.kt`)
```kotlin
package task05

inline fun <reified T> List<Any>.firstOfType(): T? {
    println("looking for ${T::class.simpleName}")
    for (item in this) {
        if (item is T) return item
    }
    return null
}

inline fun <reified T> List<Any>.countOfType(): Int {
    var count = 0
    for (item in this) {
        if (item is T) count++
    }
    return count
}

fun main() {
    val things = listOf(1, "two", 3.0, "four")
    println(things.firstOfType<String>())
    println(things.firstOfType<Double>())
    println(things.firstOfType<Boolean>())
    println("String count: ${things.countOfType<String>()}")
}
```

#### 2. Expected & Actual Output
```
looking for String
two
looking for Double
3.0
looking for Boolean
null
String count: 2
```

#### 3. Questions Answered
* **What errors occur when removing `reified` or `inline`?**
  - Removing `reified`: `Cannot check for instance of erased type: T`. (Generics are erased at runtime on JVM).
  - Removing `inline` (while keeping `reified`): `Only type parameters of inline functions can be reified`. (Reification works only because the compiler inlines the call site where the concrete type argument is known).
* **Implementation of `countOfType<T>()`:**
  Implemented as above; `things.countOfType<String>()` returns `2` (matching `"two"` and `"four"`).

---

### Task 6 — Inline Properties (Experiment)

#### 1. Code Implementation (`task06/Main.kt`)
```kotlin
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
```

#### 2. Expected & Actual Output
```
6
0
212.0
0.0
3
```
- `212.0` is Fahrenheit ($100 \times 9/5 + 32$).
- `0.0` is Celsius ($(32 - 32) \times 5/9$).

#### 3. Questions Answered
* **What happens when replacing `celsius * 9 / 5 + 32` with `field`?**
  Compiler error:
  > `Inline property cannot have backing field.`
* **Moving `inline` from accessors to property (`inline var fahrenheit: Double`):**
  Output does not change. Both getter and setter are inlined.
* **`wordCount` property:**
  `"hello big world".wordCount` outputs `3`. Blank strings output `0`.

---

### Task 7 — Public Inline Functions and `@PublishedApi` (Experiment)

#### 1. Code Implementation (`task07/Main.kt`)
```kotlin
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
```

#### 2. Expected & Actual Output
```
AUDIT: transfer 50
moved 50
```

#### 3. Questions Answered
* **Error when calling `internal fun audit` without `@PublishedApi`:**
  > `Public-API inline function cannot access non-public-API 'internal fun audit(message: String): Unit'.`
* **When to choose Fix 1 (`@PublishedApi`) vs Fix 2 (`internal inline`):**
  - **Choose Fix 1 (`@PublishedApi`):** When `transfer` must remain part of the public API for external modules to call, but `audit` should not be exposed in public API code completion.
  - **Choose Fix 2 (`internal inline`):** When `transfer` is only meant for internal use within the module.

---

### Task 8 — `typealias` (Experiment)

#### 1. Code Implementation (`task08/Main.kt`)
```kotlin
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
```

#### 2. Expected & Actual Output
```
[2, 4]
[3, 4]
[5, 4]
10.438413361169102
done: https://example.com
```

#### 3. Questions Answered
* **Why can `bigger` (type `(Int) -> Boolean`) be passed to `filterAll`?**
  `typealias` does not introduce a new type; it is purely a synonym.
* **Swapping arguments: `speed(time, distance)`:**
  Compiles and runs without error. `typealias` provides **zero type safety**.
* **Guaranteeing type safety:**
  Use `@JvmInline value class Meters(val value: Double)` and `@JvmInline value class Seconds(val value: Double)`.

---

### Task 9 — `Any`: `toString`, `equals` and `hashCode` (Experiment)

#### 1. Code Implementation (`task09/Main.kt`)
```kotlin
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
```

#### 2. Expected & Actual Output
```
Book(title=Kotlin, pages=300)
true
Point(1, 2)
true
false
String of length 5
Int, doubled is 42
Something else: 3.5
4
null
bad is null: true
```

#### 3. Questions Answered
* **Changes when adding `data` to `Book`:**
  - Before `data`: `println(b1)` prints `task09.Book@<hex_hash>`, and `b1 == b2` prints `false`.
  - After `data`: `println(b1)` prints `Book(title=Kotlin, pages=300)`, and `b1 == b2` prints `true`.
* **`p1 == p2` vs `p1 === p2`:**
  `p1 == p2` is `true` (values are equal), while `p1 === p2` is `false` (different heap instances).
* **`anything as Int` vs `anything as? Int`:**
  `anything as Int` throws `ClassCastException`, while `as?` safely evaluates to `null`.
* **`val bad: Any = null`:**
  Does not compile because `Any` is non-nullable. Changing to `Any?` compiles.

---

### Task 10 — `Nothing` (Experiment)

#### 1. Code Implementation (`task10/Main.kt`)
```kotlin
package task10

fun fail(message: String): Nothing {
    throw IllegalStateException(message)
}

fun readAge(text: String): Int {
    val age = text.toIntOrNull() ?: fail("'$text' is not a number")
    if (age < 0) fail("age cannot be negative")
    return age
}

fun later(): Int = TODO("later")

fun main() {
    println(readAge("21"))

    val none: List<Nothing> = emptyList()
    val a: List<Int> = none
    val b: List<String> = none
    println("none lists: a=$a, b=$b")

    try {
        readAge("abc")
    } catch (e: IllegalStateException) {
        println("Caught expected exception: ${e.message}")
    }
}
```

#### 2. Expected & Actual Output
```
21
none lists: a=[], b=[]
Caught expected exception: 'abc' is not a number
```

#### 3. Questions Answered
* **What happens at `readAge("abc")`?**
  Throws `java.lang.IllegalStateException: 'abc' is not a number`.
* **Why changing `fail` to `Unit` breaks `readAge`?**
  The common supertype of `Int` and `Unit` is `Any`. Thus `age` becomes `Any`, breaking `<` comparison and `Int` return.
* **Why assigning `none` to `List<Int>` and `List<String>` compiles:**
  `Nothing` is a subtype of all types, and `List<out E>` is covariant in `E`.
* **`later(): Int = TODO("later")`:**
  Compiles; throws `NotImplementedError` when called.

---

## Part B — Special Classes (Tasks 11–15)

### Task 11 — `enum class`: Basics (Experiment)

```kotlin
package task11

enum class Direction {
    NORTH, EAST, SOUTH, WEST
}

fun turnRight(d: Direction): Direction = when (d) {
    Direction.NORTH -> Direction.EAST
    Direction.EAST -> Direction.SOUTH
    Direction.SOUTH -> Direction.WEST
    Direction.WEST -> Direction.NORTH
}

fun turnLeft(d: Direction): Direction = when (d) {
    Direction.NORTH -> Direction.WEST
    Direction.EAST -> Direction.NORTH
    Direction.SOUTH -> Direction.EAST
    Direction.WEST -> Direction.SOUTH
}

fun main() {
    for (d in Direction.entries) {
        println("${d.ordinal}: ${d.name} -> ${turnRight(d)}")
    }
    println(Direction.valueOf("SOUTH"))
    println(Direction.NORTH < Direction.SOUTH)

    println("Safe lookup for UP: ${Direction.entries.firstOrNull { it.name == "UP" }}")

    for (d in Direction.entries) {
        println("turnLeft(turnRight($d)) = ${turnLeft(turnRight(d))}")
    }
}
```
* **Ordinal of `WEST`:** `3`.
* **Removing `WEST` branch:** `when expression must be exhaustive`.
* **`valueOf("UP")` vs `entries.firstOrNull`:** `valueOf` throws `IllegalArgumentException`; `firstOrNull` safely returns `null`.

---

### Task 12 — `enum class` with Properties & Functions (Write)

```kotlin
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
```

---

### Task 13 — `sealed class` (Experiment)

```kotlin
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
```

---

### Task 14 — `sealed interface` (Experiment)

```kotlin
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
```
* **Why `Note` is not delivered:** Does not implement `Deliverable`.
* **When sealed interface is better:** When a class belongs to multiple sealed hierarchies simultaneously.

---

### Task 15 — `enum` and `sealed interface` Together (Write)

```kotlin
package task15

enum class FailureReason {
    WRONG_PASSWORD, USER_NOT_FOUND, NO_NETWORK, TOO_MANY_ATTEMPTS
}

sealed interface LoginResult {
    data class Success(val userName: String) : LoginResult
    data class Failure(val reason: FailureReason) : LoginResult
    data class Locked(val minutes: Int) : LoginResult
    data object Loading : LoginResult
}

fun login(user: String, password: String): LoginResult = when {
    user.isBlank() -> LoginResult.Failure(FailureReason.USER_NOT_FOUND)
    password != "1234" -> LoginResult.Failure(FailureReason.WRONG_PASSWORD)
    else -> LoginResult.Success(user)
}

fun message(result: LoginResult): String = when (result) {
    is LoginResult.Success -> "Welcome, ${result.userName}!"
    is LoginResult.Failure -> when (result.reason) {
        FailureReason.WRONG_PASSWORD -> "Wrong password"
        FailureReason.USER_NOT_FOUND -> "No such user"
        FailureReason.NO_NETWORK -> "Check your connection"
        FailureReason.TOO_MANY_ATTEMPTS -> "Too many attempts. Please try again later."
    }
    is LoginResult.Locked -> "Try again in ${result.minutes} minutes"
    LoginResult.Loading -> "Please wait..."
}

fun main() {
    println(message(login("ali", "1234")))
    println(message(login("ali", "0000")))
    println(message(login("", "1234")))
    println(message(LoginResult.Loading))
    println(message(LoginResult.Locked(5)))
}
```

#### Four Scenario Decisions:
1. **Days of the week:** `enum class` (fixed constants).
2. **List loading result:** `sealed interface` / `sealed class` (states with different payloads).
3. **Button sizes (small, medium, large):** `enum class` (predefined options).
4. **Payment method (card with number, cash with nothing):** `sealed class` (heterogeneous data payloads).
