plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "${rootProject.group}"
version = "${rootProject.version}"
val artifactName: String by extra { "${rootProject.name.toLowerCase()}-${project.name.toLowerCase()}" }

val rootPackage: String by rootProject.extra
val projectPackage: String by extra { "${rootPackage}.${project.name.toLowerCase()}" }


dependencies {
    implementation(project(":chassismodel"))
    implementation("com.github.ajalt.clikt:clikt".depAndVersion())
    implementation("com.squareup:kotlinpoet".depAndVersion())
}

kotlin {
    jvmToolchain(BuildSrcGlobal.jdkVersion)
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
    shadowJar {
        // manifest { attributes["Main-Class"] = theMainClass }
        mergeServiceFiles()
        minimize()
        doLast {
            delete(project.tasks.jar.get().outputs)
        }
    }
    withType<Test> {
        buildSrcJvmTestConfig()
    }
}
