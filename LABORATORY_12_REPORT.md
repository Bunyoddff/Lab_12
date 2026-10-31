# Laboratory 12: Build Logic + Convention Plugins
## Mobile Apps (Native & Web) — Laboratory Report (Lecture 13: Build Logic)

---

### Executive Overview

This laboratory explores modern Android build architecture and Gradle build logic organization:
- Where build logic lives and how modules prevent architectural drift.
- Centralizing dependencies and versions using Gradle's **Version Catalog** (`libs.versions.toml`).
- Eliminating cross-module duplication with **Convention Plugins** in an included `build-logic` project (`ttpu.android.library` and `ttpu.android.feature`).
- Implementing automated architectural governance via custom Gradle verification tasks (`checkModuleNaming`).
- Optimizing build performance with Gradle daemon parallel execution, build cache, and configuration cache.
- Understanding dependency encapsulation: `implementation` vs. `api`.

The complete project **`BuildLogicLab`** (package `uz.ttpu.buildlogic`) has been constructed with all modules, plugins, and configuration files, committed into Git with the message `"Build logic lab"`.

---

## Part A — Modules, Duplication and the Version Catalog (Tasks 1–4)

### Task 1 — Where Does Build Logic Live? (Read)

#### 1. What is Build Logic?
Build logic is the configuration and executable code (written in Gradle Kotlin DSL, plugins, and tasks) that instructs the build system how to compile source code, process resources, resolve dependencies, run tests, and package deployable artifacts (APKs/AABs).

#### 2. Analysis of the Key Build Files
* **`settings.gradle.kts`:**
  - **Which line lists the modules of the project?**
    The `include` statement:
    ```kotlin
    include(":app")
    ```
    (and later `include(":core:common")`, `include(":feature:login")`).
  - **Which block says where plugins and libraries are downloaded from?**
    - Plugins repository block: `pluginManagement { repositories { ... } }`
    - Libraries/dependencies repository block: `dependencyResolutionManagement { repositories { ... } }`
* **`app/build.gradle.kts`:**
  - Line defining compile SDK: `compileSdk = 36`.
  - Line defining minimum SDK: `minSdk = 24`.
  - Line adding dependencies: `implementation(libs.androidx.core.ktx)`.
* **`gradle.properties`:**
  Two representative configuration lines:
  1. `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`
     * **Function:** Configures the Gradle Daemon JVM, allocating up to 2048 MB of heap memory and enforcing UTF-8 character encoding across compiler operations.
  2. `android.useAndroidX=true`
     * **Function:** Instructs the Android Gradle Plugin (AGP) to use AndroidX support libraries instead of legacy Android support libraries.

#### 3. Where to change the SDK version?
To change the SDK version of the `:app` module, you open **`app/build.gradle.kts`** (inside the `android { compileSdk = ... }` and `defaultConfig { targetSdk = ... }` blocks).

---

### Task 2 — Two Modules and a Dependency Direction (Build)

#### 1. Modules Created
Two Android Library modules were created:
1. `:core:common` — package `uz.ttpu.buildlogic.core.common`
2. `:feature:login` — package `uz.ttpu.buildlogic.feature.login`

#### 2. Changes to `settings.gradle.kts`
Android Studio added the following lines to `settings.gradle.kts`:
```kotlin
include(":core:common")
include(":feature:login")
```

#### 3. Architectural Dependency Direction
The dependency chain follows the strict acyclic layered architecture:
$$\text{:app} \longrightarrow \text{:feature:login} \longrightarrow \text{:core:common}$$

* `:app` depends on `:feature:login`:
  ```kotlin
  // app/build.gradle.kts
  dependencies {
      implementation(project(":feature:login"))
  }
  ```
* `:feature:login` depends on `:core:common`:
  ```kotlin
  // feature/login/build.gradle.kts
  dependencies {
      implementation(project(":core:common"))
  }
  ```

#### 4. Breaking the Rule: Reverse / Circular Dependency
When adding `:feature:login` as a dependency inside `core/common/build.gradle.kts` (having core depend on feature):
```kotlin
// core/common/build.gradle.kts (WRONG)
dependencies {
    implementation(project(":feature:login"))
}
```
Running `./gradlew assembleDebug` causes Gradle to fail during dependency resolution / task graph creation with a **Circular Dependency Error**:
> `Circular dependency between the following tasks: ...` or
> `A cycle was detected in the project dependency graph: :core:common -> :feature:login -> :core:common`
Core must never depend on higher-level features; the invalid line was removed.

---

### Task 3 — Find the Duplication (Experiment)

#### 1. File Comparison: `core/common/build.gradle.kts` vs `feature/login/build.gradle.kts`
Before applying convention plugins, comparing both module build files reveals extensive duplication:
* Identical `plugins { }` block applying `com.android.library` and `org.jetbrains.kotlin.android`.
* Identical `compileSdk = 36`.
* Identical `defaultConfig { minSdk = 24; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }`.
* Identical `compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }`.
* Identical `kotlinOptions { jvmTarget = "11" }`.
* Identical build type definitions and testing dependencies (`junit`).

#### 2. Line Count and Duplication Metrics
* **Number of identical lines:** Approximately **24–28 lines** copied verbatim across both files.
* **If `minSdk` must change from 24 to 26:**
  A developer must manually open and edit **3 files**:
  1. `app/build.gradle.kts`
  2. `core/common/build.gradle.kts`
  3. `feature/login/build.gradle.kts`
  In large projects with 50+ modules, this causes severe configuration drift and maintenance friction.

---

### Task 4 — A Version Catalog (Build)

#### 1. Sections of `gradle/libs.versions.toml`
The Gradle Version Catalog defines four canonical sections:
1. `[versions]` — Version variables referenced across libraries and plugins.
2. `[libraries]` — Dependency definitions (`group:name:version` or `module + version.ref`).
3. `[bundles]` — Groups of libraries applied together with a single alias.
4. `[plugins]` — Plugin definitions (`id + version.ref`).

#### 2. Navigation
Holding `Ctrl` (or `Cmd` on macOS) and clicking `libs.androidx.core.ktx` in any module jumps directly to its corresponding declaration entry inside `gradle/libs.versions.toml`.

#### 3. Adding Coroutines to the Catalog
Added to `gradle/libs.versions.toml`:
```toml
[versions]
coroutines = "1.10.2"

[libraries]
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
```
Consumed in `core/common/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
```

#### 4. Upgrading / Changing Versions
When changing `coroutines = "1.10.2"` to `"1.10.1"`:
* **How many files were edited?** Exactly **1 file** (`gradle/libs.versions.toml`).
* **Why is this superior to in-line version strings?**
  It provides a Single Source of Truth (SSOT). All modules are guaranteed to use binary-compatible versions of shared libraries, avoiding runtime `NoSuchMethodError` / `ClassNotFoundException` and simplifying dependency audits.

---

## Part B — Convention Plugins, Project Rules and Build Speed (Tasks 5–10)

### Task 5 — Create the `build-logic` Project (Build)

#### 1. Included Build Architecture
The `build-logic` Gradle project is structured as follows:
```
build-logic/
├── settings.gradle.kts
└── convention/
    ├── build.gradle.kts
    └── src/main/kotlin/
        └── AndroidLibraryConventionPlugin.kt
```

#### 2. `build-logic/settings.gradle.kts`
Allows the convention plugins to resolve dependencies and share the root project's Version Catalog:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
```

#### 3. `build-logic/convention/build.gradle.kts`
```kotlin
plugins {
    `kotlin-dsl`
}

group = "uz.ttpu.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "ttpu.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
```

#### 4. Wiring into Root `settings.gradle.kts`
```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories { ... }
}
```

#### 5. Verification
Running `./gradlew :core:common:tasks --console=plain` outputs:
```
ttpu.android.library applied to :core:common
```
proving the convention plugin is loaded and applied during the Gradle configuration phase.

---

### Task 6 — Move Shared Configuration into the Plugin (Build)

#### 1. `AndroidLibraryConventionPlugin.kt` Implementation
The convention plugin centralizes AGP and Kotlin application, along with compile and minimum SDK settings:
```kotlin
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("ttpu.android.library applied to ${project.path}")
        project.pluginManager.apply("com.android.library")
        project.pluginManager.apply("org.jetbrains.kotlin.android")

        project.extensions.configure<LibraryExtension> {
            compileSdk = 36
            defaultConfig.minSdk = 24
        }
    }
}
```

#### 2. Simplified Module Build Files
In `core/common/build.gradle.kts` and `feature/login/build.gradle.kts`:
```kotlin
plugins {
    id("ttpu.android.library")
}

android {
    namespace = "uz.ttpu.buildlogic.core.common"
}
```
* **Duplication removed:** Over 20 duplicate lines per module eliminated.
* **Remaining lines:** Only the module's unique `namespace` and its specific dependencies remain.

---

### Task 7 — A Feature Convention Plugin (Build)

#### 1. Implementation (`AndroidFeatureConventionPlugin.kt`)
A feature plugin composes on top of the library plugin:
```kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("ttpu.android.library")
    }
}
```

#### 2. Registered in `build-logic/convention/build.gradle.kts`
```kotlin
register("androidFeature") {
    id = "ttpu.android.feature"
    implementationClass = "AndroidFeatureConventionPlugin"
}
```

#### 3. Clean `feature/login/build.gradle.kts`
```kotlin
plugins {
    id("ttpu.android.feature")
}

android {
    namespace = "uz.ttpu.buildlogic.feature.login"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}
```
* **Line count:** The file is reduced to under 12 clean lines.
* **Why remaining lines belong to the module:**
  `namespace` must be distinct for every Android module (for R class generation), and `dependencies` declare the specific components this feature depends on.

---

### Task 8 — A Rule that Gradle Checks (Experiment)

#### 1. Why `project.name` Failed
Initial snippet:
```kotlin
val moduleName = project.name
if (!moduleName.startsWith("feature")) {
    error("Invalid feature module name: $moduleName")
}
```
* **Why did it fail?**
  For the Gradle project `:feature:login`, `project.name` evaluates to the folder name `"login"`, which does not start with `"feature"`.

#### 2. The Solution: Checking `project.path`
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
Running `./gradlew :feature:login:checkModuleNaming` passes with `BUILD SUCCESSFUL`.

#### 3. Task Scope Across Modules
* When running `./gradlew checkModuleNaming`:
  The task executes in all modules applying `ttpu.android.feature` (e.g. `:feature:login`).
* **Why does it not exist in `:core:common`?**
  Because `:core:common` applies `ttpu.android.library`, which does not register the `checkModuleNaming` verification task.

---

### Task 9 — Gradle Performance Flags (Experiment)

#### 1. Properties Added to `gradle.properties`
```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
```

#### 2. Proof in Build Output
1. **`org.gradle.configuration-cache=true`:**
   On the second build invocation, Gradle skips the configuration phase and outputs:
   ```
   Reusing configuration cache.
   ```
2. **`org.gradle.caching=true`:**
   After `./gradlew clean` followed by assemble, task outputs are fetched from local cache, marked:
   ```
   > Task :core:common:compileDebugKotlin FROM-CACHE
   ```
3. **`org.gradle.parallel=true`:**
   Independent tasks in `:core:common` and `:feature:login` are executed concurrently across available CPU threads.
* **Which flag is hardest to observe in small projects?**
  `parallel` execution, because a project with only two small modules has few independent execution paths and finishes in milliseconds.
* **When do these flags pay off?**
  In enterprise-scale codebases with hundreds of modules and large CI/CD pipelines, configuration caching and remote build caching reduce build times from 10+ minutes down to seconds.

---

### Task 10 — `api` or `implementation`? (Experiment)

#### 1. Code Added (`feature/login/.../FakeLogin.kt`)
```kotlin
package uz.ttpu.buildlogic.feature.login

import kotlinx.coroutines.delay

suspend fun fakeLogin(): Boolean {
    delay(500)
    return true
}
```

#### 2. Observation with `implementation` in `:core:common`
* If `core/common/build.gradle.kts` declares `implementation(libs.kotlinx.coroutines.android)` and `feature/login` does not declare it:
  `delay` in `FakeLogin.kt` is red (unresolved reference).
* **Why?**
  `implementation` dependencies are private implementation details. They are excluded from the compile classpath of consuming modules.

#### 3. Observation with `api` in `:core:common`
* Changing `implementation` to `api(libs.kotlinx.coroutines.android)` in `core/common`:
  `delay` becomes resolved in `feature/login` without `feature/login` explicitly declaring the dependency.

#### 4. Why `implementation` is Preferred over `api`
* **Encapsulation & ABI Safety:** `api` leaks internal dependencies into the public interface. If `core:common` updates or replaces its internal libraries, every consumer breaks.
* **Compilation Performance:** With `implementation`, changes to internal dependencies of `core:common` do NOT trigger recompilation of consuming modules (`:feature:login`, `:app`). With `api`, any change to a dependency forces Gradle to recompile every module that transitively depends on it, causing massive rebuild cascades.
* **Best Practice:** Keep dependencies as `implementation` in both `core:common` and `feature/login`.

---

### Final Verification and Git Commit
1. Final verification commands:
   ```bash
   ./gradlew assembleDebug
   ./gradlew checkModuleNaming
   ```
2. Git commit executed:
   ```bash
   git commit -m "Build logic lab"
   ```
