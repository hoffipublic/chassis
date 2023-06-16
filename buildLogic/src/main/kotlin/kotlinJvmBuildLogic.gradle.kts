plugins {
    id("kotlinCommonBuildLogic") // local in ROOT/buildLogic/src/main/kotlin/kotlinCommonBuildLogic.gradle.kts
    kotlin("jvm")
    // add gradle plugins here that "automagically should be applied
    // and then in YOUR build.gradle.kts reference:  plugins { id(<thisFileName>) }
}

//apply(from = "${project.projectDir}/${"buildLogic"}/src/main/kotlin/BuildLogicGlobalCommon.gradle.kts")

// you may configure stuff of plugins that you imported above

kotlin {
    jvmToolchain(BuildLogicGlobal.jdkVersion)
    tasks.withType<Test>().configureEach {
        buildLogicJvmTestConfig()
    }
}
afterEvaluate {
    tasks {
        named<JavaExec>("run") {
            // needed if App wants to read from stdin
            standardInput = System.`in`
        }
    }
}

//task("testUnitTest") {
//    dependsOn("test")
//}

dependencies {

}
