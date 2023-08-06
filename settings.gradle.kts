import org.gradle.kotlin.dsl.support.serviceOf

rootProject.name = "chassis"

pluginManagement {
    includeBuild("buildLogic")
    // special case of included builds are builds that define Gradle plugins.
    // These builds should be included using the includeBuild statement inside the pluginManagement {} block of the settings file.
    // Using this mechanism, the included build may also contribute a settings plugin that can be applied in the settings file itself.
    includeBuild("buildLogic/binaryPlugins/ProjectInfosBuildLogicPlugin")
    includeBuild("buildLogic/binaryPlugins/ProjectSetupBuildLogicPlugin")
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // FAIL_ON_PROJECT_REPOS or PREFER_PROJECT or PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            if (rootProject.name == "buildLogic") {
                from(files(File(rootProject.projectDir, "libs.versions.toml"))) // that's where libs.versions.toml is located in the standalone master buildLogic git repo project))
            } else {
                from(files(File(rootProject.projectDir, "buildLogic/libs.versions.toml"))) // this is the standard case
            }
        }
    }
}

include(":chassismodel")
include(":codegen")
include(":dsl")
include(":examples")
include(":shared")
include(":dbwrappers:dbwrappers")
include(":dbwrappers:exposed")

class DoSomethingAfterBuild : FlowAction<FlowParameters.None> {
    override fun execute(parameters: FlowParameters.None) {
        println("final message from: 'settings.gradle.kts':")
    }
}
gradle.serviceOf<FlowScope>().always(DoSomethingAfterBuild::class.java) { }

//class DoSomethingWithResult : FlowAction<DoSomethingWithResult.Parameters> {
//    interface Parameters : FlowParameters {
//        @get:Input
//        val failure: Property<Throwable?>
//    }
//    override fun execute(parameters: Parameters) {
//        if (!parameters.failure.isPresent) {
//            println("redundant output from: 'settings.gradle.kts': Build successful")
//        } else {
//            println("redundant output from: 'settings.gradle.kts': Build failed: ${parameters.failure.get()}")
//        }
//    }
//}
//gradle.serviceOf<FlowScope>().always(DoSomethingWithResult::class.java) {
//    val buildResult = gradle.serviceOf<FlowProviders>().buildWorkResult
//    parameters.failure.set(buildResult.map { it.failure.orElse(null) })
//}
