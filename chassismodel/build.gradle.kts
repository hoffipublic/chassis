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
    // versions file: ROOT/buildLogic/libs.versions.toml
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinx.coroutines.v()}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.v()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinx.serialization.json.v()}")
    implementation("com.squareup.okio:okio:${libs.versions.okio.v()}")
    implementation("com.squareup:kotlinpoet:${libs.versions.kotlinpoet.v()}")
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
