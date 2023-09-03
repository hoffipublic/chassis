plugins {
    kotlin("jvm")
    //id("com.github.johnrengelman.shadow")
    application
}

group = "${rootProject.group}"
version = "${rootProject.version}"
val artifactName: String by extra { "${rootProject.name.lowercase()}-${project.name.lowercase()}" }

val rootPackage: String by rootProject.extra
val projectPackage: String by extra { "${rootPackage}.dbwrappers.${project.name.lowercase()}" }
val theMainClass: String by extra { "scratch.Main" }
application {
    mainClass.set("${projectPackage}.${theMainClass}" + "Kt") // + "Kt" if fun main is outside a class
}


dependencies {
    implementation(project(":chassismodel"))
    implementation(project(":dbwrappers:dbwrappers"))
    implementation(kotlin("reflect"))
    implementation("com.github.ajalt.clikt:clikt:${libs.versions.clikt.v()}")
    implementation("com.squareup:kotlinpoet:${libs.versions.kotlinpoet.v()}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.v()}")
    implementation("org.jetbrains.exposed:exposed-core:${libs.versions.exposed.v()}")
}

tasks {
    // named<JavaExec>("run") {
    //     // needed if App wants to read from stdin
    //     standardInput = System.`in`
    // }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions{
            //Will retain parameter names for Java reflection
            javaParameters = true
            kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }
    withType<Jar> {
        archiveBaseName.set(artifactName)
    }
//    shadowJar {
//        // manifest { attributes["Main-Class"] = theMainClass }
//        mergeServiceFiles()
//        minimize()
//        doLast {
//            delete(project.tasks.jar.get().outputs)
//        }
//    }
}
