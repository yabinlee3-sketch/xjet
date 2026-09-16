# XJet 2.0 — Deployment (GitHub + JitPack)

This document describes how to publish the framework so other apps can pull
it with one line from JitPack.

## 1. Push the source to GitHub

```bash
# every framework module keeps version 2.0.3 (see root build)
git init
git add .
git commit -m "XJet 2.0 initial release"
git remote add origin https://github.com/<your-org>/xjet.git
git push -u origin main
git tag 2.0.3
git push origin 2.0.3
```

> Publishing to GitHub/JitPack requires YOUR GitHub account and token; this
> step deliberately stays out of the repository (no secrets are checked in).

## 2. JitPack build

Open `https://jitpack.io/#<your-org>/xjet/2.0.3` and click *Get it*.
JitPack builds the tag and exposes the modules below.

## 3. Consumer dependency

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.<your-org>.xjet:xjet-core:2.0.3")
    implementation("com.github.<your-org>.xjet:xjet-room:2.0.3")
    implementation("com.github.<your-org>.xjet:xjet-ui-compose:2.0.3")
    // KSP annotation processor
    ksp("com.github.<your-org>.xjet:xjet-processor:2.0.3")
}
```

## 4. Publication template

A ready template is at `gradle/jitpack-publish.gradle`. To enable it after you
own the repository, add to the root `build.gradle.kts`:

```kotlin
apply(from = file("gradle/jitpack-publish.gradle"))
```

### Android library modules

```groovy
release(MavenPublication) {
    artifactId = project.name
    afterEvaluate {
        artifact "${buildDir}/outputs/aar/${project.name}-release.aar"
    }
}
publishing { publications { release } }
```

### JVM modules (annotation, spi, processor)

```groovy
jitpack(MavenPublication) {
    artifactId = project.name
    afterEvaluate {
        artifact tasks.named('jar').get().archiveFile
    }
}
```

Run `./gradlew assembleRelease :xjet-app:assembleDebug` once before publishing
so the AAR artifacts exist, then publish the tag. No framework versions are
pinned inside the code, so consumers can use `resolutionStrategy` to override
Google/AndroidX dependencies at will (see README).

## 5. Verification checklist

- [ ] `gradle :xjet-spi:test` passes
- [ ] `gradle :xjet-app:assembleDebug` passes
- [ ] `gradle assembleRelease` produces `*/build/outputs/aar/*-release.aar`
- [ ] GitHub tag `2.0.3` pushed
- [ ] JitPack build green for the tag
- [ ] a scratch app compiles with the JitPack coordinates from section 3
