rootProject.name = "chassis"

pluginManagement {
    includeBuild("buildLogic")
    // special case of included builds are builds that define Gradle plugins.
    // These builds should be included using the includeBuild statement inside the pluginManagement {} block of the settings file.
    // Using this mechanism, the included build may also contribute a settings plugin that can be applied in the settings file itself.
    includeBuild("buildLogic/binaryPlugins/ProjectInfosBuildLogicPlugin")
    includeBuild("buildLogic/binaryPlugins/ProjectSetupBuildLogicPlugin")
}

dependencyResolutionManagement {
    versionCatalogs {
        println("searching for libs.versions.toml in project '${rootProject.name}'s settings.gradle.kts")
        create("libs") {
            from(files(File(rootProject.projectDir, "libs.versions.toml"))) // in rootProject folder
        }
    }
}

include(":chassismodel")
include(":codegen")
include(":dsl")
include(":examples")
include(":shared")

