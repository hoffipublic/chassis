plugins {
    id("kotlinJvmBuildLogic") // ROOT/buildLogic/src/kotlin/kotlinJvmBuildLogic.gradle.kts instead of kotlin("jvm")
    //id("com.github.johnrengelman.shadow")
    application
}

group = "${rootProject.group}"
version = "${rootProject.version}"
val artifactName: String by extra { "${rootProject.name.lowercase()}-${project.name.lowercase()}" }

val rootPackage: String by rootProject.extra
val projectPackage: String by extra { "${rootPackage}.${project.name.lowercase()}" }
val theMainClass: String by extra { "scratch.Main" }
application {
    mainClass.set("${projectPackage}.${theMainClass}" + "Kt") // + "Kt" if fun main is outside a class
}


dependencies {
    // versions file: ROOT/buildLogic/libs.versions.toml
    implementation(project(":chassismodel"))
    implementation(project(":shared"))
    implementation(project(":dbwrappers:dbwrappers"))
    implementation(project(":dbwrappers:exposed"))
    implementation(kotlin("reflect"))
    implementation("com.github.ajalt.clikt:clikt:${libs.versions.clikt.v()}")
    implementation("com.squareup.okio:okio:${libs.versions.okio.v()}")
    implementation("com.squareup:kotlinpoet:${libs.versions.kotlinpoet.v()}")
    implementation("org.slf4j:slf4j-api:${libs.versions.slf4j.v()}")
    implementation("ch.qos.logback:logback-classic:${libs.versions.logback.v()}")

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
