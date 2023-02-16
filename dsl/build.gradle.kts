plugins {
    kotlin("multiplatform")
    id("com.github.johnrengelman.shadow")
    application
}

val jdkVersion: Int by rootProject.extra

group = "${rootProject.group}"
version = "${rootProject.version}"
val artifactName: String by extra { "${rootProject.name.toLowerCase()}-${project.name.toLowerCase()}" }

val rootPackage: String by rootProject.extra
val projectPackage: String by extra { "${rootPackage}.${project.name.toLowerCase()}" }
val theMainClass: String by extra { "Main" }
application {
    mainClass.set("${projectPackage}.${theMainClass}" + "Kt") // + "Kt" if fun main is outside a class
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
