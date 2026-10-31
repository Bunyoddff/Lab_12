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

