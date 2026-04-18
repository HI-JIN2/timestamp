plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

project.configureKmpTargets(kotlin)
project.configureAndroidLibrary(android)

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(project.kotlinTestLibrary())
        }
    }
}
