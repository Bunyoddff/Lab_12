import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("ttpu.android.library applied to ${project.path}")
        project.pluginManager.apply("com.android.library")
        project.pluginManager.apply("org.jetbrains.kotlin.android") // AGP 8 only

        project.extensions.configure<LibraryExtension> {
            compileSdk = 36
            defaultConfig.minSdk = 24
        }
    }
}

