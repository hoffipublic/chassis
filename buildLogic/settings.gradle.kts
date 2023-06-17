rootProject.name = "buildLogic"

pluginManagement {
    //includeBuild("buildLogic") // TODO uncomment in your toplevel rootProject/settings.gradle.kts (not here(!) as that would be a recursion)
    // special case of included builds are builds that define Gradle plugins.
    // These builds should be included using the includeBuild statement inside the pluginManagement {} block of the settings file.
    // Using this mechanism, the included build may also contribute a settings plugin that can be applied in the settings file itself.
    // ===================================================================================
    // THESE INCLUDES will NOT(!!!) propagate the plugin to be defined in the ROOT project
    // BUT ONLY for this buildLogic/ sub-composite-build
    // ===================================================================================
    //// TO USE SOME OF THESE in your Project, you have to import it in settings.gradle.kts of THAT project
    //// via pluginManagement { includeBuild("buildLogic/binaryPlugins/ProjectSetupBuildLogicPlugin") }
    includeBuild("binaryPlugins/ProjectInfosBuildLogicPlugin")
    includeBuild("binaryPlugins/ProjectSetupBuildLogicPlugin")
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // FAIL_ON_PROJECT_REPOS or PREFER_PROJECT or PREFER_SETTINGS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    versionCatalogs {
        //println("buildLogic/settings.gradle.kts searching for libs.versions.toml")
        create("libs") {
            // This code is a copy of buildLogic/src/main/kotlin/GlobalFunctions.kt fun libsVersionsTomlFile(...)
            var standaloneBuildLogicProject = false
            val tomlFile = if ( (rootProject.name == "buildLogic") && (File(rootProject.projectDir.parentFile, "settings.gradle.kts").exists()) ) {
                // "normal" buildLogic/ subfolder of a gradle multiproject (as parent also has a settings.gradle.kts file)
                File(rootProject.projectDir, "../libs.versions.toml").canonicalFile // using file of rootProject this buildLogic composite build is in
            } else {
                // standalone/reference buildLogic/ project
                standaloneBuildLogicProject = true
                println("-> ${rootProject.name} of standalone project buildLogic")
                File(rootProject.projectDir, "libs.versions.toml")
            }
            if (tomlFile.exists()) {
                if ( rootProject.projectDir.parentFile.name == "binaryPlugins" ) {
                    println("-> ${rootProject.name}: buildLogic/binaryPlugins/${rootProject.name}/settings.gradle.kts using versions of \n\t\t'$tomlFile'")
                } else if (standaloneBuildLogicProject) {
                    println("-> standalone ${rootProject.name}: ${rootProject.name}/settings.gradle.kts using versions of \n\t\t'$tomlFile'")
                } else {
                    println("-> ${rootProject.name}: ${rootProject.name}/settings.gradle.kts using versions of \n\t\t'$tomlFile'")
                }
            } else {
                if ( rootProject.projectDir.parentFile.name == "binaryPlugins" ) {
                    throw GradleException("${rootProject.name}: buildLogic/binaryPlugins/${rootProject.name}/settings.gradle.kts did not find version information file '$tomlFile'")
                } else {
                    throw GradleException("${rootProject.name}: ${rootProject.name}/settings.gradle.kts did not find version information file '$tomlFile'")
                }
            }
            from(files(tomlFile.absolutePath)) // in "parent"'s rootProject folder
        }
    }
}
