# Laboratory 11 & Laboratory 12 — Mobile Software Development

This repository contains complete implementations, automated verification scripts, architectural documentation, and experiment reports for **Laboratory 11** and **Laboratory 12** of the **Software Mobile Development** curriculum (Turin Polytechnic University in Tashkent).

---

## Table of Contents
1. [Repository Architecture](#1-repository-architecture)
2. [Laboratory 11 — Kotlin Special Keywords & Special Classes](#2-laboratory-11--kotlin-special-keywords--special-classes)
   - [Part A: Special Keywords (Tasks 1–10)](#part-a-special-keywords-tasks-110)
   - [Part B: Special Classes (Tasks 11–15)](#part-b-special-classes-tasks-1115)
   - [Summary Reference Table](#summary-reference-table-lab-11)
3. [Laboratory 12 — Build Logic & Convention Plugins](#3-laboratory-12--build-logic--convention-plugins)
   - [Architecture & Dependency Flow](#architecture--dependency-flow)
   - [Module Responsibilities](#module-responsibilities)
   - [Convention Plugins (`build-logic`)](#convention-plugins-build-logic)
   - [Gradle Version Catalog](#gradle-version-catalog)
   - [Automated Project Governance (`checkModuleNaming`)](#automated-project-governance-checkmodulenaming)
   - [Performance Optimization Flags](#performance-optimization-flags)
   - [`api` vs `implementation` Dependency Encapsulation](#api-vs-implementation-dependency-encapsulation)
4. [Verification & How to Run](#4-verification--how-to-run)
5. [Git Push & Submission Guide](#5-git-push--submission-guide)

---

## 1. Repository Architecture

```
Laboratory-11-12/
├── README.md                                  # Repository overview, architecture & execution guide
├── LABORATORY_11_REPORT.md                    # Detailed report for Lab 11 (bytecode, decompilation, answers)
├── LABORATORY_12_REPORT.md                    # Detailed report for Lab 12 (architecture, duplication, flags)
│
├── Laboratory-11-Kotlin-Special-Keywords/     # Full standalone Kotlin project for Lab 11
│   ├── build.gradle.kts                       # Kotlin JVM Gradle configuration
│   ├── settings.gradle.kts
│   ├── gradlew / gradlew.bat                  # Gradle wrapper scripts
│   ├── run_all.sh                             # Automated test runner compiling & executing tasks 1–15
│   └── src/main/kotlin/
│       ├── task01/Main.kt                     # Task 1: inline & JVM bytecode decompilation
│       ├── task02/Main.kt                     # Task 2: noinline & closure parameter retention
│       ├── task03/Main.kt                     # Task 3: non-local return semantics
│       ├── task03b/Main.kt                    # Task 3b: Kotlin 2.2+ loop break & continue in lambdas
│       ├── task04/Main.kt                     # Task 4: crossinline & execution context decoupling
│       ├── task05/Main.kt                     # Task 5: reified type parameters & runtime type queries
│       ├── task06/Main.kt                     # Task 6: inline properties & backing field rules
│       ├── task07/Main.kt                     # Task 7: public inline functions & @PublishedApi
│       ├── task08/Main.kt                     # Task 8: typealias vs value class type safety
│       ├── task09/Main.kt                     # Task 9: Any, equals/hashCode contracts, as vs as?
│       ├── task10/Main.kt                     # Task 10: Nothing bottom type, TODO(), covariance
│       ├── task11/Main.kt                     # Task 11: enum class basics, valueOf vs safe search
│       ├── task12/Main.kt                     # Task 12: enum class with properties, functions & fromLabel()
│       ├── task13/Main.kt                     # Task 13: sealed class hierarchy & exhaustive when
│       ├── task14/Main.kt                     # Task 14: sealed interface & multiple hierarchy inheritance
│       └── task15/Main.kt                     # Task 15: architectural union: enum + sealed interface
│
└── Laboratory-12-BuildLogicLab/               # Full multi-module Android project for Lab 12
    ├── build.gradle.kts                       # Root build configuration with plugin definitions
    ├── settings.gradle.kts                    # Root settings declaring modules & includeBuild("build-logic")
    ├── gradle.properties                      # Daemon flags: parallel, caching, configuration-cache
    ├── local.properties                       # Android SDK directory pointer
    ├── gradlew / gradlew.bat                  # Gradle wrapper scripts
    ├── gradle/
    │   └── libs.versions.toml                 # Centralized Gradle Version Catalog
    ├── app/                                   # Application entry point module (:app)
    ├── core/common/                           # Reusable library (:core:common) using ttpu.android.library
    ├── feature/login/                         # Feature module (:feature:login) using ttpu.android.feature
    └── build-logic/                           # Dedicated Gradle included build
        ├── settings.gradle.kts                # Version Catalog binding for build logic
        └── convention/                        # Custom convention plugins module
            ├── build.gradle.kts               # kotlin-dsl Gradle plugin definitions
            └── src/main/kotlin/
                ├── AndroidLibraryConventionPlugin.kt # Reusable Android library convention (compileSdk, minSdk)
                └── AndroidFeatureConventionPlugin.kt # Feature convention + checkModuleNaming rule
```

---

## 2. Laboratory 11 — Kotlin Special Keywords & Special Classes

### Part A: Special Keywords (Tasks 1–10)

1. **`inline` (Task 1):**
   - Eliminates function call overhead and closure allocations (`Function0`) by expanding the function body and lambda directly into the call site.
   - Decompiled bytecode analysis confirms `counter` is maintained as a primitive stack variable (`iload`, `istore`), avoiding the `Ref.IntRef` heap wrapper generated for non-inlined closures.
2. **`noinline` (Task 2):**
   - Inlined lambdas have no object representation at runtime. Marking a parameter with `noinline` instructs the compiler to generate a standard functional object that can be stored in collections or fields.
3. **Non-local Returns & Jump Expressions (Task 3 & 3b):**
   - Inlined lambdas allow unqualified `return` statements that exit the outer enclosing function (**non-local return**).
   - In non-inlined functions, non-local returns are prohibited because execution cannot jump out of unknown calling stacks.
   - Demonstrates Kotlin 2.2+ capability enabling `break` and `continue` inside inlined lambdas within loop structures.
4. **`crossinline` (Task 4):**
   - Used when an inlined lambda is called indirectly from another execution context (e.g. inside a `Runnable`, object literal, or local function).
   - Keeps the performance benefits of inlining while prohibiting non-local returns that would violate JVM stack semantics.
5. **`reified` Type Parameters (Task 5):**
   - Overcomes JVM generic type erasure. Because an inlined function is expanded at compile time where the concrete type argument is known, `item is T` and `T::class` work without reflection.
   - Implemented `countOfType<T>()` to count occurrences of a specific type in heterogeneous collections.
6. **Inline Properties (Task 6):**
   - Inlining getters and setters for properties without backing fields.
   - Implemented `inline val String.wordCount: Int` extension property.
7. **Public Inline & `@PublishedApi` (Task 7):**
   - Public inline functions cannot reference `private` or `internal` declarations because external calling modules cannot access them.
   - Annotating internal helpers with `@PublishedApi` allows public inlined functions to call them while keeping them hidden from general public API discoverability.
8. **`typealias` vs Value Classes (Task 8):**
   - `typealias` provides readability synonyms but **zero type safety** (e.g., swapping `Meters` and `Seconds` compiles without error).
   - Demonstrated how `@JvmInline value class` provides compile-time type safety with zero runtime allocation overhead.
9. **`Any`: `toString`, `equals`, `hashCode` (Task 9):**
   - Examines root class contracts, reference equality (`===`) vs structural equality (`==`), compiler safe casts (`as?`), and why `Any` cannot be assigned `null` without `Any?`.
10. **`Nothing` (Task 10):**
    - The bottom type with zero instances, representing functions that never return normally (always throw or loop indefinitely).
    - Enables idiomatic Elvis branching: `val age = text.toIntOrNull() ?: fail(...)`.
    - Demonstrates type covariance: `List<Nothing>` is a valid subtype of `List<Int>` and `List<String>`.

---

### Part B: Special Classes (Tasks 11–15)

1. **`enum class` Basics (Task 11):**
   - Predefined constant sets with ordinal ordering.
   - Demonstrates `valueOf()` throwing `IllegalArgumentException` on unmatched strings vs safe navigation using `Direction.entries.firstOrNull { ... }`.
2. **`enum class` with Data & Methods (Task 12):**
   - Enums with associated immutable properties (`label`, `level`) and member methods (`isUrgent()`).
   - Implemented companion object lookup `Priority.fromLabel(text)`.
3. **`sealed class` (Task 13):**
   - Closed class hierarchies with exhaustive compile-time `when` verification (no `else` required).
   - Unlike enums, subclasses can hold distinct state and properties (`Circle(radius)` vs `Rectangle(width, height)`).
4. **`sealed interface` (Task 14):**
   - Allows a class to belong to multiple sealed hierarchies simultaneously, solving JVM single-class inheritance limitations.
5. **Architectural Union (Task 15):**
   - Combining sealed interfaces for operation results (`Success`, `Failure`, `Locked`, `Loading`) with enums for discrete error codes (`FailureReason`).

---

### Summary Reference Table (Lab 11)

| Task | Topic | Key Construct | Primary Advantage |
| :--- | :--- | :--- | :--- |
| **01** | `inline` | `inline fun logged(...)` | Eliminates lambda object & `Ref.IntRef` allocations |
| **02** | `noinline` | `noinline second: () -> Unit` | Allows lambda to be stored in collections/fields |
| **03** | Non-local Return | `return n` inside inline lambda | Exits enclosing function immediately |
| **03b**| Jump Expressions | `continue` / `break` in `run { }` | Kotlin 2.2+ loop control inside inline lambdas |
| **04** | `crossinline` | `crossinline block: () -> Unit` | Inlines across contexts (e.g. `Runnable`) without non-local returns |
| **05** | `reified` | `inline fun <reified T> firstOfType()` | Enables `is T` and `T::class` without reflection |
| **06** | Inline Properties | `inline val String.wordCount` | Zero-overhead computed property accessors |
| **07** | `@PublishedApi` | `@PublishedApi internal fun audit()` | Exposes internal helper to public inlined functions |
| **08** | `typealias` | `typealias Meters = Double` | Semantic synonym; contrasted with `@JvmInline value class` |
| **09** | `Any` | `override fun equals() / hashCode()` | Root hierarchy contracts, safe casts (`as?`), non-null guarantees |
| **10** | `Nothing` | `fun fail(...): Nothing` | Bottom type for aborting control flow and covariant empty lists |
| **11** | `enum class` | `enum class Direction` | Fixed constant lists, entries, exhaustive `when` matching |
| **12** | Enum Data | `Priority(label, level)` | Parameterized constants with `fromLabel()` companion function |
| **13** | `sealed class` | `sealed class Shape` | Closed polymorphic hierarchies with variable state |
| **14** | `sealed interface`| `sealed interface Message, Deliverable` | Multiple sealed contract inheritance |
| **15** | Unified Design | `LoginResult` + `FailureReason` | Sealed state modeling combined with discrete enum error codes |

---

## 3. Laboratory 12 — Build Logic & Convention Plugins

### Architecture & Dependency Flow

```
              ┌────────────────────────┐
              │          :app          │  (Application Entry Point)
              └───────────┬────────────┘
                          │ implementation(project(":feature:login"))
                          ▼
              ┌────────────────────────┐
              │     :feature:login     │  (Feature Module)
              └───────────┬────────────┘
                          │ implementation(project(":core:common"))
                          ▼
              ┌────────────────────────┐
              │      :core:common      │  (Reusable Core Library)
              └────────────────────────┘
                          ▲
                          │
  ┌───────────────────────┴────────────────────────┐
  │ Convention Plugins (via included build)        │
  │  - ttpu.android.library                        │
  │  - ttpu.android.feature                        │
  └────────────────────────────────────────────────┘
```

- **Acyclic Dependency Hierarchy:** Features depend on core; core **never** depends on features. Attempting to add `:feature:login` to `:core:common` causes a circular dependency build failure.

---

### Module Responsibilities

| Module | Applied Plugin | Purpose |
| :--- | :--- | :--- |
| `:app` | `com.android.application` | Application packaging, composition root, and launcher activity. |
| `:core:common` | `ttpu.android.library` | Shared foundation, coroutine helpers, and common utilities. |
| `:feature:login` | `ttpu.android.feature` | Isolated login presentation, asynchronous authentication (`FakeLogin.kt`). |
| `build-logic:convention` | `kotlin-dsl` | Encapsulates all common Android and Kotlin configuration logic. |

---

### Convention Plugins (`build-logic`)

Instead of duplicating 25+ lines of Gradle build configuration across every module, convention plugins centralize settings:

1. **`ttpu.android.library` (`AndroidLibraryConventionPlugin.kt`):**
   - Automatically applies `com.android.library` and `org.jetbrains.kotlin.android`.
   - Configures `LibraryExtension` with `compileSdk = 36` and `defaultConfig.minSdk = 24`.
2. **`ttpu.android.feature` (`AndroidFeatureConventionPlugin.kt`):**
   - Automatically applies `ttpu.android.library`.
   - Registers automated quality verification rules (`checkModuleNaming`).

---

### Gradle Version Catalog

Located at `gradle/libs.versions.toml`, managing all dependency coordinates and plugin versions in a single location:

```toml
[versions]
agp = "8.7.0"
kotlin = "2.0.20"
coreKtx = "1.15.0"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"
appcompat = "1.7.0"
material = "1.12.0"
coroutines = "1.10.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

---

### Automated Project Governance (`checkModuleNaming`)

Task 8 implements a custom Gradle verification task registered within `AndroidFeatureConventionPlugin`:

```kotlin
val modulePath = project.path
project.tasks.register("checkModuleNaming") {
    doLast {
        if (!modulePath.startsWith(":feature:")) {
            error("Invalid feature module path: $modulePath")
        }
    }
}
```

- Verifies that any module applying the feature convention plugin resides strictly under the `:feature:` namespace hierarchy.
- Uses `project.path` (e.g. `:feature:login`) rather than `project.name` (`login`) to eliminate false negatives.

---

### Performance Optimization Flags

Configured in `gradle.properties`:

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
```

1. **`org.gradle.parallel=true`:** Executes decoupled tasks across distinct modules concurrently on multi-core processors.
2. **`org.gradle.caching=true`:** Reuses task outputs across subsequent builds (`FROM-CACHE`), even after `./gradlew clean`.
3. **`org.gradle.configuration-cache=true`:** Caches the Gradle configuration phase graph, displaying `Reusing configuration cache.` on subsequent invocations.

---

### `api` vs `implementation` Dependency Encapsulation

Task 10 evaluates dependency visibility using `FakeLogin.kt`:

```kotlin
package uz.ttpu.buildlogic.feature.login

import kotlinx.coroutines.delay

suspend fun fakeLogin(): Boolean {
    delay(500)
    return true
}
```

- **`implementation`:** Keeps dependencies private to the declaring module. If `:core:common` uses `implementation(libs.kotlinx.coroutines.android)`, consumers (`:feature:login`) cannot access `delay`.
- **`api`:** Leaks transitive dependencies into the compile classpath of consuming modules.
- **Architectural Guideline:** Always prefer `implementation`. It prevents API leakage, maintains clean abstraction boundaries, and avoids recompilation cascades when internal module libraries are upgraded.

---

## 4. Verification & How to Run

### Running Laboratory 11 Tasks

Execute all 16 tasks in sequential automated order:

```bash
cd Laboratory-11-Kotlin-Special-Keywords
chmod +x run_all.sh
./run_all.sh
```

Or run any task individually using `kotlinc`:

```bash
kotlinc src/main/kotlin/task01/Main.kt -include-runtime -d task01.jar
java -jar task01.jar
```

### Running Laboratory 12 Build Checks

From the `Laboratory-12-BuildLogicLab` directory:

```bash
cd Laboratory-12-BuildLogicLab

# Verify naming convention rule
./gradlew checkModuleNaming

# Assemble all debug variants
./gradlew assembleDebug
```

Both commands will complete with `BUILD SUCCESSFUL`.

---

## 5. Git Push & Submission Guide

To push this entire repository to your GitHub account:

### Step 1: Create a New GitHub Repository
1. Go to [github.com/new](https://github.com/new).
2. Name the repository (e.g., `Laboratory-11-12`).
3. Leave it empty (do **not** initialize with README or .gitignore).

### Step 2: Push to GitHub
Open your terminal in the `Laboratory-11-12` root directory and run:

```bash
cd /Users/a1234/Downloads/Upwork/Turin/apk/Laboratory-11-12

# Stage all files
git add .

# Create the submission commit
git commit -m "Complete Laboratory 11 & Laboratory 12 implementations"

# Add your GitHub remote repository (replace with your repository URL)
git remote add origin https://github.com/Bunyoddff/Laboratory-11-12.git

# Set default branch to main and push
git branch -M main
git push -u origin main
```

All files, reports, Kotlin packages, and Android Gradle modules will be pushed cleanly and structured for immediate academic review.
