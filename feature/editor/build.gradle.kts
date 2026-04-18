plugins {
    id("timestamp.compose.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.domain.editor)
        }
    }
}
