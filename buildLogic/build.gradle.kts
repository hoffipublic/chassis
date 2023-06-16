// forcing the precompiled script plugins here to make it part of the Kotlin source set like any other class.
// Henceforth, the composite module as part of the project build is responsible for the configuration,
// which we usually do in the project-level Gradle build script.
// Therefore, we move the Gradle-specific declarations here.
// Precompiled script plugins are basically XXX.gradle.kts Kotlin scripts that can be placed together with other Kotlin source sets (src/main/kotlin ).
// Precompiled scripts have accessors, and don’t need extensions (Project.xxx) to be added, like Binary plugins.
plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
//    id("com.github.ben-manes.versions") version libs.versions.gradle.versions.plugin
//    id("nl.littlerobots.version-catalog-update") version libs.versions.gradle.versions.catalogUpdate
//    id("se.ascp.gradle.gradle-versions-filter") version libs.versions.gradle.versions.filter
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
//    // val libs and its values defined in ROOT/buildLogic/src/main/kotlin/VersionCatalogsExtensions.kt
//    implementation(libs.kotlin.gradlePlugin)
//    implementation("com.github.ben-manes:gradle-versions-plugin:${libs.versions.gradle.versions.plugin.get()}")
//    implementation("nl.littlerobots.vcu:plugin:${libs.versions.gradle.versions.catalogUpdate.get()}")
//    implementation("se.ascp.gradle:gradle-versions-filter:${libs.versions.gradle.versions.filter.get()}")

    // val libs and its values defined in ROOT/buildLogic/src/main/kotlin/VersionCatalogsExtensions.kt
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")
    implementation("nl.littlerobots.vcu:plugin:0.8.0")
    implementation("se.ascp.gradle:gradle-versions-filter:0.1.16")
}

//versionCatalogUpdate {
//    catalogFile.set(File(rootProject.projectDir, "libs.versions.toml")) // that's where libs.versions.toml is located in the standalone master buildLogic git repo project
//    keep {
//        keepUnusedVersions.set(true)
//        keepUnusedLibraries.set(true)
//        keepUnusedPlugins.set(true)
//    }
//}
//versionsFilter {
//    outPutFormatter.set("json,plain") // you at least need "json"
//    strategy.set(se.ascp.gradle.Strategy.EXCLUSIVE)
//    exclusiveQualifiers.addAll("beta","rc","cr","m","preview","b" )
//    checkForGradleUpdate.set(true)
//    log.set(false)
//}
