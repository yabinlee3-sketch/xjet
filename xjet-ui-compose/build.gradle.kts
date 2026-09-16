plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.xjet.compose"
    compileSdk = 35
    defaultConfig {
        minSdk = 23
    }
    publishing {
        singleVariant("release")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(project(":xjet-core"))
    api(libs.compose.runtime)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.ui)
    api(libs.compose.ui.tooling)
    api(libs.compose.ui.tooling.preview)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.navigation.compose)
}
