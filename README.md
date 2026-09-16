# XJet 2.0

A modern, decoupled Android framework written in Kotlin. **XJet** keeps the
design idea of [XDroid](https://github.com/limedroid/XDroid) — every module is
an interface, nothing is hard-wired to any third-party library — and rebuilds
it on the official modern Android stack: coroutines, Flow, Compose, KSP,
Room and Navigation.

> Golden rule: **the framework owns the contract, the ecosystem owns the implementation.**
> You can swap EventBus, database, image loader, router or UI toolkit without
> ever editing the framework source.

---

## Requirements coverage

| # | Requirement | Where |
|---|-------------|-------|
| 4.1 | Interface decoupling | `xjet-core` `Providers.kt`, `xjet-spi` registry |
| 4.2 | SPI + 3 registration modes | code `XJet.register`, config asset discovery, KSP generation |
| 4.3 | Override by subclass | `XJet.override(...)` + `SpiRegistry` `override=true` |
| 4.4 | Gradle resolutionStrategy overriding | see README section below; no framework-enforced versions |
| 4.5 | XML + Compose dual format | `xjet-ui-xml`, `xjet-ui-compose`, interop sample |
| 4.6 | Database abstraction + Room default | `DatabaseProvider` + `xjet-room` |
| 4.7 | Coroutines + Flow architecture | `XJetDispatchers`, `SharedFlowEventBus`, `OneShotEvent`, `Flow.collectIn` |
| 4.8 | KSP annotation processing | `xjet-annotation`, `xjet-processor` (routes + SPI registration) |
| 4.9 | Strict top-down layering | modules only depend downward; app is the only leaf |
| 4.10 | Single entry point | `XJet.init(context, config)`, access via `XJet.xxx()` |

## Modules

| Module | Artifact (JitPack) | Responsibility |
|--------|--------------------|----------------|
| `xjet-annotation` | `xjet-annotation` | `@SpiService`, `@XRoute` |
| `xjet-spi` | `xjet-spi` | dependency-free registry, config discovery |
| `xjet-processor` | `xjet-processor` | KSP processor (routes + SPI code generation) |
| `xjet-core` | `xjet-core` | XJet entry point, default EventBus/Cache/Router, Flow utils |
| `xjet-room` | `xjet-room` | Room-backed `DatabaseProvider` default |
| `xjet-ui-xml` | `xjet-ui-xml` | XML base activity + UiDelegate |
| `xjet-ui-compose` | `xjet-ui-compose` | Compose helpers + NavGraph route builder |
| `xjet-app` | — | sample app (`assembleDebug` verifies the whole stack) |

## Quick start

```kotlin
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val db = RoomDatabaseProvider.create(
            this, AppDatabase::class.java, "demo.db"
        )

        XJet.init(
            this,
            XJetConfig.Builder(this)
                .debug(BuildConfig.DEBUG)
                .database(db)
                .build()
        )
    }
}
```

After init, every capability is reachable through the static entry:

```kotlin
val db = XJet.database()
val bus = XJet.eventBus()
val router = XJet.router()

viewModelScope.launch {
    XJet.database().dao(MessageDao::class.java).insert(Message(text = "hi"))
}

XJet.eventBus().post(UserLoggedInEvent(id = 1uL))
```

### SPI — three registration modes

**1. Code**

```kotlin
// inside Application before/after XJet.init
XJet.register(EventBusProvider::class.java, MyBus(), override = true)
```

**2. Config file discovery**

Add `assets/xjet/spi.properties` to your app:

```properties
io.github.xjet.core.DatabaseProvider=com.example.MyDatabaseProvider
```

Factory-owning no-arg constructors keep the framework dependency-free.

**3. KSP / annotation**

```kotlin
@SpiService(api = GreetingService::class)
class RealGreetingService : GreetingService { ... }
```

Apply the processor once:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}
dependencies {
    implementation(project(":xjet-annotation"))
    ksp(project(":xjet-processor"))
}
```

The KSP task generates `io.github.xjet.generated.XJetGeneratedSpi` with both
SPI registration and `@XRoute` route table.

### Override a default instead of replacing it (4.3)

```kotlin
class LoggingCache(default: CacheProvider) : CacheProvider by default {
    override fun put(key: String, value: String) {
        println("cache write $key")
        default.put(key, value)
    }
}
```

```kotlin
XJet.override(CacheProvider::class.java, LoggingCache(XJet.cache()))
```

### Dynamic dependency overrides via Gradle (4.4)

Because no version is hard-pinned inside the framework, treat Google/AndroidX
versions as normal dependencies and use `resolutionStrategy` when you need a
family-wide bump:

```kotlin
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.room") {
            useVersion("2.7.0")   // example
        }
    }
}
```

### Live data with StateFlow + one-shot SharedFlow (4.7)

```kotlin
class HomeViewModel : ViewModel() {
    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()
    val toasts = OneShotEvent<String>()
}

@Composable
fun Home(vm: HomeViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    vm.toasts.collectAsEffect { msg -> Toast.makeText(...) }
}
```

### XML ⇄ Compose interop (4.5)

- Compose in XML: add a `ComposeView` to your XML layout; see `XmlActivity`.
- XML in Compose: use `AndroidView`; see `HomeScreen` in the sample.

## Build & verify

```bash
# JVM core unit tests
gradle :xjet-spi:test

# full Android build including sample + KSP + Room
gradle :xjet-app:assembleDebug
```

## Publishing (GitHub + JitPack)

See [DEPLOY.md](DEPLOY.md) for the exact steps. The framework modules declare
`com.github.xjet` as group and `2.0.3` as version, and a ready-to-use
publication template lives in `gradle/jitpack-publish.gradle`.

## License

MIT — see [LICENSE](LICENSE).
