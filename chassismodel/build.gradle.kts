plugins {
    kotlin("jvm")
    //id("com.github.johnrengelman.shadow")
}

group = "${rootProject.group}"
version = "${rootProject.version}"
val artifactName: String by extra { "${rootProject.name.lowercase()}-${project.name.lowercase()}" }

val rootPackage: String by rootProject.extra
val projectPackage: String by extra { "${rootPackage}.${project.name.lowercase()}" }


dependencies {
}

tasks {
    // named<JavaExec>("run") {
    //     // needed if App wants to read from stdin
    //     standardInput = System.`in`
    // }
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
