import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("ttpu.android.library")

        val modulePath = project.path
        project.tasks.register("checkModuleNaming") {
            doLast {
                if (!modulePath.startsWith(":feature:")) {
                    error("Invalid feature module path: $modulePath")
                }
            }
        }
    }
}

