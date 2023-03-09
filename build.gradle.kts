plugins {
    kotlin("jvm") version BuildSrcGlobal.VersionKotlin
    kotlin("plugin.serialization") version BuildSrcGlobal.VersionKotlin apply false
    id("com.github.johnrengelman.shadow") version "shadow".pluginVersion()
    application
}

group = "com.hoffi"
version = "1.0-SNAPSHOT"
val artifactName: String by extra { "${rootProject.name.toLowerCase()}-${project.name.replace("[-_]".toRegex(), "").toLowerCase()}" }

val rootPackage: String by extra { "${rootProject.group}.${rootProject.name.toLowerCase()}" }
val projectPackage: String by extra { rootPackage }
val theMainClass: String by extra { "Main" }
application {
    mainClass.set("${rootPackage}.${theMainClass}" + "Kt") // + "Kt" if fun main is outside a class
}

allprojects {
    //println("> root/build.gradle.kts allprojects: $project")
    repositories {
        mavenCentral()
    }
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        println("${project.name}: starting configure for kotlin MPP project ...")
    }
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
        println("${project.name}: starting configure for kotlin JVM project ...")
    }

    afterEvaluate { // needed so that plugins already have been applied to subprojects
        dependencies {
            implementation("org.slf4j:slf4j-api".depAndVersion())
            runtimeOnly("ch.qos.logback:logback-classic".depAndVersion())
            implementation("com.squareup.okio:okio".depAndVersion())
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core".depAndVersion())
            implementation("org.jetbrains.kotlinx:kotlinx-datetime".depAndVersion())
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json".depAndVersion())

            testImplementation("io.kotest:kotest-framework-engine".depButVersionOf("io.kotest:kotest-runner-junit5"))
            testImplementation("io.kotest:kotest-framework-datatest".depButVersionOf("io.kotest:kotest-runner-junit5"))
            testImplementation("io.kotest:kotest-assertions-core".depButVersionOf("io.kotest:kotest-runner-junit5"))
            testImplementation(kotlin("test-common"))
            testImplementation(kotlin("test-annotations-common"))
            testRuntimeOnly("io.kotest:kotest-runner-junit5".depAndVersion()) // depends on jvm { useJUnitPlatform() } // Platform!!! nd not only useJUnit()            testImplementation("io.kotest:kotest-assertions-core".depButVersionOf("io.kotest:kotest-runner-junit5"))
        }
    }

    apply(from ="${rootProject.projectDir}/buildSrc/snippets/printClasspath.gradle.kts")
    /** task build might be finalizedBy(versionsPrint) */
    val versionsPrint = tasks.register("versionsPrint") {
        group = "misc"
        description = "extract spring boot versions from dependency jars"
        doLast {
            val foreground = BuildSrcGlobal.ConsoleColor.YELLOW
            val background = BuildSrcGlobal.ConsoleColor.DEFAULT
            val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class)
            BuildSrcGlobal.printlnColor(foreground, "Project: ${project.name}", background)
            BuildSrcGlobal.printlnColor(foreground, "  fat/uber jar: ${shadowJar.archiveFileName.get()}", background)
            BuildSrcGlobal.printlnColor(foreground, "Gradle version: " + project.gradle.gradleVersion, background)
            BuildSrcGlobal.printColor(foreground, "Kotlin version: " + kotlin.coreLibrariesVersion) ; if (kotlin.coreLibrariesVersion != BuildSrcGlobal.VersionKotlin) BuildSrcGlobal.printColor(BuildSrcGlobal.ConsoleColor.RED, " ( != ${BuildSrcGlobal.VersionKotlin} )")
            println()
            BuildSrcGlobal.printlnColor(foreground, "javac  version: " + org.gradle.internal.jvm.Jvm.current(), background) // + " with compiler args: " + options.compilerArgs, backgroundColor = BuildSrcGlobal.ConsoleColor.DARK_GRAY)
            BuildSrcGlobal.printlnColor(foreground, "       srcComp: " + java.sourceCompatibility, background)
            BuildSrcGlobal.printlnColor(foreground, "       tgtComp: " + java.targetCompatibility, background)
            BuildSrcGlobal.printlnColor(foreground, "versions of core dependencies:", background)
            val regex = Regex(pattern = "^(spring-cloud-starter|spring-boot-starter|micronaut-core|kotlin-stdlib-jdk[0-9-]+|foundation-desktop)-[0-9].*$")
            if (subprojects.size > 0) {
                configurations.compileClasspath.get().map { it.nameWithoutExtension }.filter { it.matches(regex) }
                    .forEach { BuildSrcGlobal.printlnColor(foreground, String.format("%-25s: %s", project.name, it), background) }
            } else {
                configurations.compileClasspath.get().map { it.nameWithoutExtension }.filter { it.matches(regex) }
                    .forEach { BuildSrcGlobal.printlnColor(foreground, "  $it", background) }
            }
        }
    }
}

allprojects {
    //println("> root/build.gradle.kts subprojects for: sub$project")
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        println("root/build.gradle.kts subprojects { configuring sub$project as kotlin(\"jvm\") project }")
        dependencies {
            implementation(kotlin("reflect"))
            implementation("com.github.ajalt.clikt:clikt".depAndVersion())
            implementation("com.squareup:kotlinpoet".depAndVersion())
            runtimeOnly("ch.qos.logback:logback-classic".depAndVersion())
            implementation("org.slf4j:slf4j-api".depAndVersion())
        }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        println("root/build.gradle.kts subprojects { configuring sub$project as kotlin(\"multiplatform\") project }")
    }

}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.github.ajalt.clikt:clikt".depAndVersion())
    implementation("com.squareup:kotlinpoet".depAndVersion())
    runtimeOnly("ch.qos.logback:logback-classic".depAndVersion())
    implementation("org.slf4j:slf4j-api".depAndVersion())
}

kotlin {
    jvmToolchain(BuildSrcGlobal.jdkVersion)
}

tasks {
    val versionsPrint by existing
    build { finalizedBy(versionsPrint) }
    named<JavaExec>("run") {
        // needed if App wants to read from stdin
        standardInput = System.`in`
    }
    withType<Jar> {
        archiveBaseName.set(artifactName)
    }
    shadowJar {
        manifest { attributes["Main-Class"] = theMainClass }
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


// ==============================================================================
// ======   Helpers and pure informational stuff not necessary for build ========
// ==============================================================================
tasks.register<CheckVersionsTask>("checkVersions") { // implemented in buildSrc/src/main/kotlin/CheckVersionsTask.kt
    //scope = "ALL"
}

tasks.register("setup") {
    dependsOn(createIntellijScopeSentinels, createSrcBasePackages)
}
// from ./buildSrc/snippets/createSrcBasePackages.kts
val createSrcBasePackages = tasks.register("createSrcBasePackages") {
    doLast {
        project.subprojects.forEach { prj ->
            var relProjectDirString = prj.projectDir.toString().removePrefix(rootProject.projectDir.toString())
            if (relProjectDirString.isBlank()) { relProjectDirString = "ROOT" } else { relProjectDirString = relProjectDirString.removePrefix("/") }
            println("  in project: $relProjectDirString ...")
            val projectPackage: String by prj.extra
            val projectPackageDirString = projectPackage.split('.').joinToString("/")
            prj.pluginManager.let() { when {
                it.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                    prj.sourceSets.forEach { sourceSet ->
                        val ssDir = File("${prj.projectDir}/src/${sourceSet.name}/kotlin")
                        if (ssDir.exists()) {
                            mkdir("$ssDir/$projectPackageDirString")
                        }
                    }
                }
                it.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
                    val kotlinMultiplatformExtension = prj.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)
                    val kotlinProjectExtension = kotlinMultiplatformExtension as org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
                    //prj.kotlin.sourceSets.forEach {
                    kotlinProjectExtension.sourceSets.forEach { topKotlinSourceSet ->
                        kotlin.sourceSets.forEach { kotlinSourceSet ->
                            val ssDir = File("${prj.projectDir}/src/${topKotlinSourceSet.name}/kotlin")
                            if (ssDir.exists()) {
                                mkdir("$ssDir/$projectPackageDirString")
                            }
                        }
                    }
                }
            } }
            println("  in project: $relProjectDirString ok.")
        }
    }
}
// from ./buildSrc/snippets/createIntellijScopeSentinels.kts
val createIntellijScopeSentinels = tasks.register("createIntellijScopeSentinels") {
    doLast {
        project.allprojects.forEach { prj ->
            var relProjectDirString = prj.projectDir.toString().removePrefix(rootProject.projectDir.toString())
            if (relProjectDirString.isBlank()) { relProjectDirString = "ROOT" } else { relProjectDirString = relProjectDirString.removePrefix("/") }
            println("  in project: $relProjectDirString ...")
            val suffix = if (prj.name == rootProject.name) {
                "ROOT"
            } else {
                prj.name.toUpperCase()
            }
            prj.pluginManager.let { when {
                it.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                    if (prj.name != rootProject.name) {
                        val dir = mkdir("${prj.projectDir}/01__$suffix")
                        File(dir, ".gitkeep").createNewFile()
                        File(prj.projectDir, "ZZ__$suffix").createNewFile()
                    }
                    prj.sourceSets.forEach { ss: SourceSet ->
                        val ssDir = if (prj.name == rootProject.name) {
                            File("src/${ss.name}")
                        } else {
                            File("${prj.projectDir}/src/${ss.name}")
                        }
                        if (ssDir.exists()) {
                            val mName = ss.name.capitalize()
                            val dir = mkdir("$ssDir/_src${mName}_$suffix")
                            File(dir, ".gitkeep").createNewFile()
                            File(ssDir, "ZZsrc${mName}_$suffix").createNewFile()
                        }
                    }
                }
                it.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
                    if (prj.name != rootProject.name) {
                        val dir = mkdir("${prj.projectDir}/01__$suffix")
                        File(dir, ".gitkeep").createNewFile()
                        File(prj.projectDir, "ZZ__$suffix").createNewFile()
                    }
                    prj.kotlin.sourceSets.forEach { sourceSet ->
                        val ssDir = if (prj.name == rootProject.name) {
                            File("src/${sourceSet.name}")
                        } else {
                            File("${prj.projectDir}/src/${sourceSet.name}")
                        }
                        if (ssDir.exists()) {
                            if (sourceSet.name.endsWith("Main")) {
                                val mName = sourceSet.name.removeSuffix("Main").capitalize()
                                val dir = mkdir("$ssDir/_src${mName}_$suffix")
                                File(dir, ".gitkeep").createNewFile()
                                File(ssDir, "ZZsrc${mName}_$suffix").createNewFile()
                            }
                        }
                    }
                }
            }}
            println("  in project: $relProjectDirString ok.")
        }
    }
}
