// Shared publication config for XJet framework modules (GitHub + JitPack).
import org.gradle.api.publish.maven.MavenPublication

apply(plugin = "maven-publish")

group = "com.github.xjet"
version = "2.0.0"

afterEvaluate {
    publishing {
        publications {
            if (plugins.hasPlugin("com.android.library")) {
                create<MavenPublication>("release") {
                    from(project.components["release"])
                    artifactId = project.name
                    pom { packaging = "aar" }
                }
            } else {
                create<MavenPublication>("jitpack") {
                    from(project.components["java"])
                    artifactId = project.name
                }
            }
        }
    }
}
