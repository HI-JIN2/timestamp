plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

project.configureKmpTargets(kotlin)
project.configureAndroidApplication(android)

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(project.kotlinTestLibrary())
        }
    }
}
