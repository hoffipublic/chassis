plugins {
    //id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.asProvider().get()
    kotlin("jvm") version libs.versions.kotlin.asProvider().get()
    `java-gradle-plugin`
}

dependencies {
    // val libs and its values defined in ROOT/buildLogic/src/main/kotlin/VersionCatalogsExtensions.kt
    implementation(libs.kotlin.gradlePlugin)

    //implementation(kotlin("stdlib"))
    //implementation(gradleApi())
    //implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.0.15")
}

gradlePlugin {
    plugins {
        //register("ProjectSetupBuildLogicPlugin") {
        create("ProjectInfosBuildLogicPlugin") {
            id = "buildLogic.binaryPlugins.ProjectInfosBuildLogicPlugin"
            implementationClass = "buildLogic.plugins.projectInfosBuildLogicPlugin.ProjectInfosBuildLogicPlugin"
        }
        create("ProjectSetupBuildLogicPlugin") {
            id = "buildLogic.binaryPlugins.ProjectSetupBuildLogicPlugin"
            implementationClass = "buildLogic.plugins.projectSetupBuildLogicPlugin.ProjectSetupBuildLogicPlugin"
        }
    }
}

dependencies {
//    implementation(kotlin("stdlib"))
//    implementation(gradleApi())
//    implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.0.15")
}

//tasks.test {
//    useJUnitPlatform()
//}
