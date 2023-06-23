plugins {
    kotlin("jvm") version libs.versions.kotlin.asProvider().get()
    kotlin("plugin.serialization") version libs.versions.kotlin.asProvider().get() apply false
    application
    id("buildLogic.binaryPlugins.ProjectSetupBuildLogicPlugin")
    id("buildLogic.binaryPlugins.ProjectInfosBuildLogicPlugin")
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


allprojects {
    //println("> root/build.gradle.kts allprojects: $project")
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        println("${project.name}: starting configure for kotlin MPP project ...")
    }
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
        println("${project.name}: starting configure for kotlin JVM project ...")
    }

    afterEvaluate { // needed so that plugins already have been applied to subprojects
        dependencies {
            implementation("org.slf4j:slf4j-api:${libs.versions.slf4j.get()}")
            //runtimeOnly("ch.qos.logback:logback-classic:${libs.versions.logback.v()}")
            implementation("ch.qos.logback:logback-classic:${libs.versions.logback.get()}")
            implementation("com.squareup.okio:okio:${libs.versions.okio.get()}")
            //implementation("org.apache.commons:commons-lang3".depAndVersion())
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinx.coroutines.get()}")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.get()}")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinx.serialization.json.get()}")
            implementation("com.benasher44:uuid:${libs.versions.benasher44.uuid.get()}")

            testImplementation("io.kotest:kotest-framework-engine:${libs.versions.kotest.asProvider().get()}")
            testImplementation("io.kotest:kotest-framework-datatest:${libs.versions.kotest.asProvider().get()}")
            testImplementation("io.kotest:kotest-assertions-core:${libs.versions.kotest.asProvider().get()}")
            testImplementation(kotlin("test-common"))
            testImplementation(kotlin("test-annotations-common"))
            testRuntimeOnly("io.kotest:kotest-runner-junit5:${libs.versions.kotest.asProvider().get()}")
        }
    }
}

allprojects {
    //println("> root/build.gradle.kts subprojects for: sub$project")
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        println("root/build.gradle.kts subprojects { configuring sub$project as kotlin(\"jvm\") project }")
        dependencies {
            implementation(kotlin("reflect"))
//            implementation("com.github.ajalt.clikt:clikt".depAndVersion())
//            implementation("com.squareup:kotlinpoet".depAndVersion())
//            runtimeOnly("ch.qos.logback:logback-classic".depAndVersion())
//            implementation("org.slf4j:slf4j-api".depAndVersion())
        }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        println("root/build.gradle.kts subprojects { configuring sub$project as kotlin(\"multiplatform\") project }")
    }

}

dependencies {
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

