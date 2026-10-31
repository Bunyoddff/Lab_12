plugins {
    id("ttpu.android.library")
}

android {
    namespace = "uz.ttpu.buildlogic.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}

