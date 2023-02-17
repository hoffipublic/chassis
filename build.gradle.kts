plugins {
    kotlin("jvm") version BuildSrcGlobal.VersionKotlin
    id("com.github.johnrengelman.shadow") version "shadow".pluginVersion()
    application
}

group = "com.hoffi"
version = "1.0-SNAPSHOT"
val artifactName: String by extra { "${rootProject.name.toLowerCase()}-${project.name.toLowerCase()}" }

val rootPackage: String by extra { "${rootProject.group}.${rootProject.name.toLowerCase()}" }
val projectPackage: String by extra { rootPackage }
val theMainClass: String by extra { "Main" }
application {
    mainClass.set(theMainClass)
}

allprojects {
    repositories {
        mavenCentral()
    }
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        println("${project.name}: starting configure for kotlin MPP project ...")
    }
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
        println("${project.name}: starting configure for kotlin JVM project ...")
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

dependencies {
    implementation("com.github.ajalt.clikt:clikt".depAndVersion())
    implementation("com.squareup:kotlinpoet".depAndVersion())
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
        buildSrcTestConfig()
    }
}


// ==============================================================================
// ======   Helpers, but not necessary for build ================================
// ==============================================================================

tasks.register("setup") {
    dependsOn(createIntellijScopeSentinels, createSrcBasePackages)
}
/** create package dirs under each subprojects src/module/kotlin
 * based on subproject's extra property: projectPackage
 *
 * val rootPackage: String by rootProject.extra
 * val projectPackage by extra { "${rootPackage}.${project.name.toLowerCase()}" }
 */
val createSrcBasePackages = tasks.register("createSrcBasePackages") {
    doLast {
        project.subprojects.forEach { sub ->
            val projectPackage: String by sub.extra
            val projectPackageDirString = projectPackage.split('.').joinToString("/")
            sub.pluginManager.let() { when {
                it.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                    sub.sourceSets.forEach { ss: SourceSet ->
                        val ssDir = File("${sub.name}/src/${ss.name}/kotlin")
                        if (ssDir.exists()) {
                            mkdir("$ssDir/$projectPackageDirString")
                        }
                    }
                }
                it.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
                    sub.kotlin.sourceSets.forEach { ss ->
                        val ssDir = File("${sub.name}/src/${ss.name}/kotlin")
                        if (ssDir.exists()) {
                            mkdir("$ssDir/$projectPackageDirString")
                        }
                    }
                }
            }}
        }
    }
}
/** creating `01__PRJNAME/.gitkeep` and `ZZ__PRJNAME` files in each kotlin mpp project
 * as well as `_srcModule_PRJNAME/.gitkeep` and `ZZsrcModule_PRJNAME` files in each main sourceSets of these
 *
 * .gitignore:
 * <block>
 *  .idea/
 *  !.idea/scopes/
 *  !.idea/fileColors.xml
 * </block>
 *
 * if you had .idea/ ignored before, try
 * <block>
 * git rm --cached .idea/filename
 * git add --forced .idea/filename
 * </block>
 *
 * e.g. define scopes (in Settings... `Scopes`):
 * - scope 00__ (scope with all folders where the name starts with: 0[0-3]__, meaning the first folder
 * - scope src with _src.../ or ZZsrc... (scope with all folders where the name starts with _src)
 * - scope buildfiles (e.g. build.gradle.kts)
 *
 * and then in Settings ... `File Colors` add the scope(s) and give them a color .
 *
 * If you _then_ add folders / files matching the above scope names
 * you can see more clearly which "area" of code in the folder structure you are just looking at the moment .
 */
val createIntellijScopeSentinels = tasks.register("createIntellijScopeSentinels") {
    doLast {
        project.allprojects.forEach { prj ->
            var d: File
            val suffix = if (prj.name == rootProject.name) {
                "ROOT"
            } else {
                prj.name.toUpperCase()
            }
            prj.pluginManager.let { when {
                it.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                    if (prj.name != rootProject.name) {
                        d = mkdir("${prj.name}/01__$suffix")
                        File(d, ".gitkeep").createNewFile()
                        File(prj.name, "ZZ__$suffix").createNewFile()
                    }
                    prj.sourceSets.forEach { ss: SourceSet ->
                        val ssDir = if (prj.name == rootProject.name) {
                            File("src/${ss.name}")
                        } else {
                            File("${prj.name}/src/${ss.name}")
                        }
                        if (ssDir.exists()) {
                            val mName = ss.name.capitalize()
                            d = mkdir("$ssDir/_src${mName}_$suffix")
                            File(d, ".gitkeep").createNewFile()
                            File(ssDir, "ZZsrc${mName}_$suffix").createNewFile()
                        }
                    }
                }
                it.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
                    if (prj.name != rootProject.name) {
                        d = mkdir("${prj.name}/01__$suffix")
                        File(d, ".gitkeep").createNewFile()
                        File(prj.name, "ZZ__$suffix").createNewFile()
                    }
                    prj.kotlin.sourceSets.forEach { ss ->
                        val ssDir = if (prj.name == rootProject.name) {
                            File("src/${ss.name}")
                        } else {
                            File("${prj.name}/src/${ss.name}")
                        }
                        if (ssDir.exists()) {
                            if (ss.name.endsWith("Main")) {
                                val mName = ss.name.removeSuffix("Main").capitalize()
                                d = mkdir("$ssDir/_src${mName}_$suffix")
                                File(d, ".gitkeep").createNewFile()
                                File(ssDir, "ZZsrc${mName}_$suffix").createNewFile()
                            }
                        }
                    }
                }
            }}
        }
    }
}

// ################################################################################################
// #####    pure informational stuff on stdout    #################################################
// ################################################################################################
tasks.register<CheckVersionsTask>("checkVersions") { // implemented in buildSrc/src/main/kotlin/Deps.kt
    scope = "USED" // "ALL"
}
