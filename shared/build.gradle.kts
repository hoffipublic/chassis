plugins {
    id("kotlinJvmBuildLogic") // ROOT/buildLogic/src/kotlin/kotlinJvmBuildLogic.gradle.kts instead of kotlin("jvm")
    //id("com.github.johnrengelman.shadow")
}

group = "${rootProject.group}"
version = "${rootProject.version}"
val artifactName: String by extra { "${rootProject.name.lowercase()}-${project.name.lowercase()}" }

val rootPackage: String by rootProject.extra
val projectPackage: String by extra { "${rootPackage}.${project.name.lowercase()}" }


dependencies {
    implementation(project(":chassismodel"))
    implementation(project(":dbwrappers:dbwrappers"))
    implementation(project(":dbwrappers:exposed"))
    implementation(kotlin("reflect"))
    implementation("io.arrow-kt:arrow-core:${libs.versions.arrow.v()}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.v()}")
    implementation("com.squareup.okio:okio:${libs.versions.okio.v()}")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.squareup:kotlinpoet:${libs.versions.kotlinpoet.v()}")

    implementation("io.insert-koin:koin-core:${libs.versions.koin.v()}")
    // test dependencies (bundle from ROOT/buildLogic/libs.versions.toml)
    testImplementation(libs.bundles.testJunitKotestKoin)
    // for running junit tests
    testImplementation(kotlin("test"))
    // for running kotest tests
    testImplementation("io.kotest:kotest-runner-junit5:${libs.versions.kotest.v()}")
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
