import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("timestamp.compose.app")
}

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework>().configureEach {
            baseName = "ComposeApp"
            isStatic = true
            export(projects.core.model)
            export(projects.feature.editor)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.exifinterface)
            implementation(projects.core.model)
            implementation(projects.feature.editor)
        }
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.feature.editor)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
