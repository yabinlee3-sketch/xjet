# XJet 2.2.0 — Deployment (GitHub + JitPack)

Single-module release. Consumers depend on **one** artifact:
`com.github.yabinlee3-sketch:xjet:xjet:2.2.0`.

## 1. Push the source to GitHub

```bash
git add .
git commit -m "Merge XJet into a single module (v2.2.0)"
git push -u origin main
git tag 2.2.0
git push origin 2.2.0
```

## 2. JitPack build

Open `https://jitpack.io/#com.github.yabinlee3-sketch/xjet/2.2.0` and click
*Get it*. Because only one module remains, the build exposes a single
`xjet` artifact.

## 3. Consumer dependency

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.yabinlee3-sketch:xjet:xjet:2.2.0")
}
```

## 4. Publication

`xjet/build.gradle.kts` uses `maven-publish` + `singleVariant("release")` so
JitPack publishes the `release` AAR directly. Run
`./gradlew :xjet:test :xjet:assembleRelease :xjet-app:assembleDebug` before
tagging to verify locally.

## 5. Verification checklist

- [ ] `:xjet:test` passes
- [ ] `:xjet:assembleRelease` produces `xjet/build/outputs/aar/xjet-release.aar`
- [ ] `:xjet-app:assembleDebug` passes
- [ ] lint: `:xjet:lintVitalAnalyzeRelease` + `:xjet-app:lintVitalAnalyzeRelease`
- [ ] GitHub tag `2.2.0` pushed
- [ ] JitPack build green for `2.2.0`
- [ ] a scratch app compiles with the single JitPack coordinate from section 3
