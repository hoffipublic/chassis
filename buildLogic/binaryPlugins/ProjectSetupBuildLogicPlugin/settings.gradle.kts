rootProject.name = "ProjectSetupBuildLogicPlugin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
        create("libs") {
            //println("buildLogic/binaryPlugins/${rootProject.name}/settings.gradle.kts searching for libs.versions.toml")
            // special code for binaryPlugins to determine if they are in the standalone reference buildLogic project
            // or a composite buildLogic subproject of a normal (multi)project layout
            val tomlFile = if ( (rootProject.projectDir.parentFile.name == "binaryPlugins") && (File(rootProject.projectDir.parentFile.parentFile.parentFile, "settings.gradle.kts").exists()) ) {
                // "normal" buildLogic/ subfolder of a gradle multiproject (as parent also has a settings.gradle.kts file)
                File(rootProject.projectDir.parentFile.parentFile, "../libs.versions.toml").canonicalFile // using file of rootProject this buildLogic composite build is in
            } else {
                // standalone/reference buildLogic/ project
                println("-> ${rootProject.name} of standalone project buildLogic")
                File(rootProject.projectDir.parentFile.parentFile, "libs.versions.toml")
            }

            if (tomlFile.exists()) {
                logger.lifecycle("-> ${rootProject.name}: buildLogic/binaryPlugins/${rootProject.name}/settings.gradle.kts using versions of \n\t\t'$tomlFile'")
            } else {
                throw GradleException("${rootProject.name}: buildLogic/binaryPlugins/${rootProject.name}/settings.gradle.kts did not find version information file '$tomlFile'")
            }
            // use libs.versions.toml from root project instead of this composite build
            from(files(tomlFile.absolutePath)) // in rootProject folder
        }
    }
}
