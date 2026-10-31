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
        register("androidFeature") {
            id = "ttpu.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}

