plugins {
    kotlin("multiplatform") version BuildSrcGlobal.VersionKotlin
    id("com.github.johnrengelman.shadow") version "shadow".pluginVersion()
    application
}

group = "com.hoffi"
version = "1.0-SNAPSHOT"
val artifactName: String by extra { "${rootProject.name.toLowerCase()}-${project.name.toLowerCase()}" }

val rootPackage: String by extra { "${rootProject.group}.${rootProject.name.toLowerCase()}" }
val projectPackage: String by extra { rootPackage }
val theMainClass: String by extra { "Main" }

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
}

kotlin {
    val nativeTarget = when(BuildSrcGlobal.hostOS) {
        BuildSrcGlobal.HOSTOS.MACOS -> macosX64("native")
        BuildSrcGlobal.HOSTOS.LINUX -> linuxX64("native")
        BuildSrcGlobal.HOSTOS.WINDOWS -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    jvm {
        jvmToolchain(BuildSrcGlobal.jdkVersion)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val nativeMain by getting
        val nativeTest by getting
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
    }
}

// ==============================================================================
// ======   Helpers and pure informational stuff not necessary for build ========
// ==============================================================================

tasks.register("setup") {
    dependsOn(createIntelllijScopeSentinels, createSrcBasePackages)
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
                    sub.kotlin.sourceSets.forEach { ss: org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet ->
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
val createIntelllijScopeSentinels = tasks.register("createIntellijScopeSentinels") {
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
                    prj.kotlin.sourceSets.forEach { ss: org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet ->
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
