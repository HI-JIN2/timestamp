import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKmpTargets(
    kotlin: KotlinMultiplatformExtension,
    frameworkBaseName: String = defaultFrameworkBaseName(),
) {
    kotlin.androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        kotlin.iosArm64(),
        kotlin.iosSimulatorArm64(),
        kotlin.iosX64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkBaseName
            isStatic = true
        }
    }
}

internal fun Project.configureAndroidLibrary(
    extension: LibraryExtension,
    namespace: String = defaultNamespace(),
) {
    extension.apply {
        this.namespace = namespace
        compileSdk = versionInt("android-compileSdk")

        defaultConfig {
            minSdk = versionInt("android-minSdk")
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

internal fun Project.configureAndroidApplication(
    extension: ApplicationExtension,
    namespace: String = defaultNamespace(),
) {
    extension.apply {
        this.namespace = namespace
        compileSdk = versionInt("android-compileSdk")

        defaultConfig {
            applicationId = namespace
            minSdk = versionInt("android-minSdk")
            targetSdk = versionInt("android-targetSdk")
            versionCode = 1
            versionName = "0.1.0"
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

internal fun Project.defaultNamespace(): String {
    return if (path == ":composeApp") {
        "com.yujin.timestamp"
    } else {
        "com.yujin.timestamp.${path.removePrefix(":").replace(":", ".")}"
    }
}

internal fun Project.defaultFrameworkBaseName(): String {
    return path
        .removePrefix(":")
        .split(":")
        .filter { it.isNotBlank() }
        .joinToString(separator = "") { segment ->
            segment.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase()
                } else {
                    char.toString()
                }
            }
        }
}

internal fun Project.versionInt(alias: String): Int = libsCatalog().findVersion(alias).get().requiredVersion.toInt()

internal fun Project.kotlinTestLibrary(): Provider<MinimalExternalModuleDependency> =
    libsCatalog().findLibrary("kotlin-test").get()

private fun Project.libsCatalog(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
