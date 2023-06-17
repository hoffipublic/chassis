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
        println("project '${rootProject.name}'s settings.gradle.kts searching for libs.versions.toml")
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

