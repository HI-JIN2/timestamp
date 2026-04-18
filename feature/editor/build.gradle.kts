plugins {
    id("timestamp.compose.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
            implementation(projects.core.model)
            implementation(projects.domain.editor)
        }
    }
}
