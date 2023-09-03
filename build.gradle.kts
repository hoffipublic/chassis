plugins {
    kotlin("jvm") version libs.versions.kotlin.asProvider().get()
    kotlin("plugin.serialization") version libs.versions.kotlin.asProvider().get() apply false
    application
    id("buildLogic.binaryPlugins.ProjectSetupBuildLogicPlugin")
    id("buildLogic.binaryPlugins.ProjectInfosBuildLogicPlugin")
    id("VersionsUpgradeBuildLogic")
    //id("io.kotest") version libs.versions.kotest.plugin.get() apply false
}

group = "com.hoffi"
version = "1.0-SNAPSHOT"
val artifactName: String by extra { "${rootProject.name.lowercase()}-${project.name.replace("[-_]".toRegex(), "").lowercase()}" }

val rootPackage: String by extra { "${rootProject.group}.${rootProject.name.lowercase()}" }
val projectPackage: String by extra { rootPackage }
val theMainClass: String by extra { "Main" }
application {
    mainClass.set("${rootPackage}.${theMainClass}" + "Kt") // + "Kt" if fun main is outside a class
}

// Helper tasks to speed up things and don't waste time
//=====================================================
// 'c'ompile
val c by tasks.registering {
    dependsOn(subprojects.filter { it.name !in listOf("generated") }.flatMap {
        listOf(
            it.tasks.findByName("compileKotlin"),
            it.tasks.findByName("compileTestKotlin")
        )
    }.mapNotNull{it})
}
// 'c'ompile without compileTestKotlin
val cc by tasks.registering {
    dependsOn(subprojects.filter { it.name !in listOf("generated") }.flatMap {
        listOf(
            it.tasks.findByName("compileKotlin"),
        )
    }.mapNotNull{it})
}
// 'c'ompile 'g'enerated
val cg by tasks.registering {
    dependsOn(
        ":generated:compileKotlin",
        ":generated:compileTestKotlin")
}
val versionsPrint by tasks.existing
val build by tasks.existing {
    finalizedBy(versionsPrint)
}

//allprojects {
//    //println("> root/build.gradle.kts allprojects: $project")
//    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
//        println("${project.name}: starting configure for kotlin MPP project ...")
//    }
//    project.plugins.withId("org.jetbrains.kotlin.jvm") {
//        println("${project.name}: starting configure for kotlin JVM project ...")
//    }
//}

dependencies {
    // versions file: ROOT/buildLogic/libs.versions.toml
    implementation(kotlin("reflect"))
    implementation(project("chassismodel"))
//    implementation("com.github.ajalt.clikt:clikt".depAndVersion())
//    implementation("com.squareup:kotlinpoet".depAndVersion())
//    runtimeOnly("ch.qos.logback:logback-classic".depAndVersion())
//    implementation("org.slf4j:slf4j-api".depAndVersion())
}

//tasks {
//    val versionsPrint by existing
//    build { finalizedBy(versionsPrint) }
//    named<JavaExec>("run") {
//        // needed if App wants to read from stdin
//        standardInput = System.`in`
//    }
//    withType<Jar> {
//        archiveBaseName.set(artifactName)
//    }
//}

