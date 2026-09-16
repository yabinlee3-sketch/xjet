# XJet 2.2.0

A modern Android framework written in Kotlin that is **one library, one
dependency** — just like XDroid. It keeps the design idea of
[XDroid](https://github.com/limedroid/XDroid) (everything behind an interface,
nothing hard-wired to a specific cloud/UI/database vendor) and rebuilds it on
the modern Android stack: Kotlin, coroutines, Flow, Room, RecyclerView and
Compose.

> Golden rule: **the framework owns the contract, the ecosystem owns the implementation.**
> You can swap EventBus, database, image loader, router or UI toolkit without
> ever editing the framework source.

Unlike XDroid's split of MVP/JVM/UI modules, XJet ships as a **single `xjet`
AAR** and it is MVVM-oriented: `ViewModel` + `StateFlow` + lifecycle-aware
UI collection (XML `XJetActivity` or Compose).

## Single dependency

```kotlin
repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.yabinlee3-sketch:xjet:2.2.0")
}
```

That is the whole integration. No annotation processor, no extra module.

## Everything you get, in one artifact

| Area | API |
|------|-----|
| Single entry point | `XJet.init(context, XJetConfig)` then `XJet.xxx()` |
| DI / SPI registry | `XJet.register(api, impl)` / `XJet.override` / config asset discovery |
| MVVM base | `XViewModel` + `UiState` loading/error/empty/content + Compose helpers |
| Event bus | `SharedFlowEventBus` (once-off + sticky), `OneShotEvent` |
| Router | Activity routes via `XJet.registerRoute(...)`, chain builder `XJet.open(Activity){ putString(..)}` |
| Database | `DatabaseProvider` + Room-backed default |
| Cache | in-memory, SharedPreferences, file — swap via `XJetConfig.cache` |
| Global errors | `ExceptionInterceptor` + `XJet.capture/tryCatch` + uncaught handler |
| Structured log | `XJetLog` |
| Network | `XJet.http()/getText/postJson` (dependency-free default) |
| Image loading | default `AndroidImageLoaderProvider` + `ImageViewTarget` |
| Permissions | `PermissionKit` (check / request / rationale) |
| Kits & crypto | `Kits` (date/file/random/package) + `Codec` (MD5/SHA-1/SHA-256) |
| Lists | `SimpleRecyclerAdapter<T,VH>` (XML flavor) |
| XML + Compose | both UI toolkits, can be embedded in each other |

## Quick start (MVVM)

```kotlin
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // explicit runtime wiring — no annotation processor needed
        XJet.register(GreetingService::class.java, DemoGreetingService())
        XJet.registerRoute(path = "main", target = MainActivity::class.java)

        val db = RoomDatabaseProvider.create(this, AppDatabase::class.java, "demo.db")
        XJet.init(
            this,
            XJetConfig.Builder(this)
                .debug(BuildConfig.DEBUG)
                .database(db)
                .installUncaughtErrorHandler(true)
                .build()
        )
    }
}
```

### ViewModel side

```kotlin
class HomeViewModel : XViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        launchSafe("home.load") {
            val greeting = XJet.get(GreetingService::class.java).greet()
            _state.update { it.copy(greeting = greeting) }
        }
    }
}
```

### Compose side

```kotlin
@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    XJetStateBox(state = vm.uiState) {
        Text(state.greeting)
    }
}
```

### XML side

Extend `XJetActivity` and reuse the same ViewModel with
`by viewModels()` — same MVVM shape, just the classic toolkit.

## Runtime SPI registration

Three modes, none require KSP:

1. **Code**: `XJet.register(api, impl)` / `XJet.registerProvider(api, factory)`
2. **Config asset**: put `assets/xjet/spi.properties`
   ```
   io.github.xjet.core.DatabaseProvider=com.example.MyDatabaseProvider
   ```
3. **Route table**: `XJet.registerRoute(path, TargetActivity::class.java)`

There is intentionally **no annotation processor** — it was the main reason
the old version had to be split into multiple modules, and XDroid proved
that a single dependency with runtime registration is much easier to consume.

## Global exception interceptor

```kotlin
class MyErrorReporter : ExceptionInterceptor {
    override fun onError(source: String, t: Throwable, severity: ErrorSeverity) {
        Analytics.record(source, t)
    }
}

XJet.init(this, XJetConfig.Builder(this)
    .errorInterceptor(MyErrorReporter())
    .installUncaughtErrorHandler(true)
    .build())
```

Then use the framework-wide helpers: `XJet.capture(source, t)`,
`XJet.tryCatch { ... }`, `Flow.catchAndReport(source)`.

## Override defaults, don't edit the framework

```kotlin
class CacheWithAudit(default: CacheProvider) : CacheProvider by default {
    override fun put(key: String, value: String) {
        println("cache write $key")
        default.put(key, value)
    }
}
XJet.override(CacheProvider::class.java, CacheWithAudit(XJet.cache()))
```

## Gradle override of any transitive version

Nothing is hard-pinned inside the framework, so the consuming app stays in
control:

```kotlin
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.room") useVersion("2.7.0")
    }
}
```

## What changed from 2.1.x

- **Single module** — `xjet` replaces the old 7-module split; one line to use.
- **No KSP annotation processor** — registration is runtime (code/config/routes).
- **MVVM first** — `XViewModel` + `UiState` + lifecycle-aware Compose collection,
  XML still supported through `XJetActivity`.
- All 2.1 features kept: global exception interceptor, persistent cache,
  structured log, network, image loader, permissions, kits/codec, chain router,
  recycle adapter, Room default, XML/Compose dual UI.

