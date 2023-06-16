rootProject.name = "buildLogic"

pluginManagement {
    // special case of included builds are builds that define Gradle plugins.
    // These builds should be included using the includeBuild statement inside the pluginManagement {} block of the settings file.
    // Using this mechanism, the included build may also contribute a settings plugin that can be applied in the settings file itself.
    // ===================================================================================
    // THESE INCLUDES will NOT(!!!) propagate the plugin to be defined in the ROOT project
    // BUT ONLY for this buildLogic/ sub-composite-build
    // ===================================================================================
    //// TO USE SOME OF THESE in your Project, you have to import it in settings.gradle.kts of THAT project
    //// via pluginManagement { includeBuild("buildLogic/binaryPlugins/ProjectSetupBuildLogicPlugin") }
    //includeBuild("binaryPlugins/ProjectInfosBuildLogicPlugin")
    //includeBuild("binaryPlugins/ProjectSetupBuildLogicPlugin")
}

dependencyResolutionManagement {
//    versionCatalogs {
//        println("searching for libs.versions.toml in project '${rootProject.name}'s settings.gradle.kts")
//        create("libs") {
//            val libsVersionsRootDir = File(rootProject.projectDir.parent)
//            val libsVersionsTomlLocation = "/gradle/libs.versions.toml"
//            val rootDirGradleLibsVersionsToml = File(libsVersionsRootDir, libsVersionsTomlLocation)
//            if (rootDirGradleLibsVersionsToml.exists()) {
//                logger.lifecycle("-> using version information on dependencies inside '${libsVersionsRootDir.name}${libsVersionsTomlLocation}' for ${rootProject.name} ")
//            } else {
//                throw GradleException(rootProject.name + ": did not find version information on dependencies inside '${rootDirGradleLibsVersionsToml}'")
//            }
//            from(files(rootDirGradleLibsVersionsToml.absolutePath)) // in rootProject folder
//        }
//    }
}
