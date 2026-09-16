import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("maven-publish")
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.xjet"
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
    lint {
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.activity.ktx)
    api(libs.kotlinx.coroutines.android)
    api(libs.lifecycle.runtime.ktx)
    api(libs.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)

    api(libs.room.runtime)
    api(libs.room.ktx)

    api(libs.compose.runtime)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.ui)
    api(libs.androidx.navigation.compose)
    api(libs.compose.ui.tooling)
    api(libs.compose.ui.tooling.preview)

    api(libs.androidx.recyclerview)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = project.name
                from(components["release"])
            }
        }
    }
}



