import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "xjet"

include(":xjet-annotation")
include(":xjet-spi")
include(":xjet-processor")
include(":xjet-core")
include(":xjet-room")
include(":xjet-ui-xml")
include(":xjet-ui-compose")
include(":xjet-app")

