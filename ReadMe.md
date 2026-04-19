# 📅 EventsApp

> A production-grade Android events discovery app built with **Jetpack Compose**, **Clean Architecture**, **MVVM**, **Hilt**, **Room**, and **Retrofit (mocked via local JSON)**.

## 📁 Project Structure

```
com.rogers.eventapp/

│
├── data/
│   │   ├── ImageCacheManager.kt       ← Coil memory + disk cache config
│   │   └── ResponseCache.kt           ← In-memory TTL cache (15 min)
│   ├── local/
│   │   ├── dao/       EventDao, 
│   │   ├── database/  AppDatabase (Room)
│   │   └── entity/    EventEntity, 
│   ├── mapper/
│   │   └── EventMappers.kt            ← DTO ↔ Entity ↔ Domain converters
│   ├── remote/
│   │   ├── api/       EventApiService (Retrofit interface)
│   │   ├── dto/       EventDto, EventsResponseDto
│   │   └── interceptor/      FakeResponse interceptor    ← Reads raw/events.json
│   └── repository/
│       └── EventRepositoryImpl.kt     ← Cache → API → Room fallback chain
│
├── di/
│   ├── AppModule.kt                   ← Repository + UseCases bindings
│   ├── DatabaseModule.kt              ← Room + DAOs
│   ├── ImageModule.kt                 ← Coil ImageLoader with cache
│   └── NetworkModule.kt              ← OkHttp, Retrofit, Gson
│
├── domain/
│   ├── model/
│   │   └── Event.kt                   ← Pure domain model (no Android deps)
│   ├── repository/
│   │   └── EventRepository.kt         ← Repository interface (contract)
│   └── usecase/
│       └── EventUseCase.kt           ← GetEventsUseCase, GetEventByIdUseCase,
│                                         GetBookmarksUseCase, ToggleBookmarkUseCase,
│                                         GetCategoriesUseCase, RefreshEventsUseCase
│
├── presentation/
│   ├── components/
│   │   └── (UI components)         ← EventCard, BookmarkButton,
│   │                                     DistanceBadge, RelativeTimeBadge
│   ├── navigation/
│   │   ├── EventsNavHost.kt            ← NavHost, bottom nav
│   │   └── Route.kt                    ← Route sealed class
│   ├── screen/
│   │   ├── EventListScreen.kt         ← Discovery feed: search + filter + list
│   │   ├── EventDetailScreen.kt       ← Full detail, maps deep link, share
│   │   └── BookmarksScreen.kt         ← Saved events list
│   ├── state/
│   │   ├── EventListUiState.kt
│   │   ├── EventDetailUiState.kt
│   │   └── BookmarksUiState.kt
│   ├── ui.theme/
│   │   └── Theme.kt             ← Color, Typography, Light/Dark scheme
│   ├── viewmodel/
│   │   ├── EventListViewModel.kt
│   │   ├── EventDetailViewModel.kt
│   │   └── BookmarksViewModel.kt
│   └── MainActivity.kt
│
├── service/
│   └── EventRefreshWorker.kt          ← WorkManager periodic refresh (every 6h)
│
├── utils/
│   └── AppUtils.kt                    ← LocationUtils, DistanceUtils (Haversine),
│                                         DeepLinkUtils, DateUtils,
│                                         NetworkUtils, FormatUtils
│
└── MyApplication.kt                   ← @HiltAndroidApp + WorkManager init
```

---

## 🏗 Architecture Diagram

```
╔══════════════════════════════════════════════════════════════════╗
║                      PRESENTATION LAYER                         ║
║                                                                  ║
║  ┌─────────────────┐ ┌──────────────────┐ ┌──────────────────┐  ║
║  │ EventListScreen │ │EventDetailScreen │ │ BookmarksScreen  │  ║
║  └────────┬────────┘ └────────┬─────────┘ └────────┬─────────┘  ║
║           │ collectAsState    │ collectAsState      │            ║
║  ┌────────▼────────┐ ┌────────▼─────────┐ ┌────────▼─────────┐  ║
║  │ EventListVM     │ │ EventDetailVM    │ │ BookmarksVM      │  ║
║  │ StateFlow<UI>   │ │ StateFlow<UI>    │ │ StateFlow<UI>    │  ║
║  └────────┬────────┘ └────────┬─────────┘ └────────┬─────────┘  ║
╚═══════════╪════════════════════╪════════════════════╪════════════╝
            │                   │                    │
            └───────────────────┼────────────────────┘
                          UseCases ▼
╔══════════════════════════════════════════════════════════════════╗
║                        DOMAIN LAYER                              ║
║                    (Pure Kotlin — no Android)                    ║
║                                                                  ║
║  GetEventsUseCase          GetEventByIdUseCase                   ║
║  GetBookmarksUseCase       ToggleBookmarkUseCase                 ║
║  GetCategoriesUseCase      RefreshEventsUseCase                  ║
║                                                                  ║
║              ┌──────────────────────────┐                        ║
║              │   EventRepository        │  ← interface only      ║
║              └──────────────────────────┘                        ║
╚══════════════════════════╪═══════════════════════════════════════╝
                           │ implements
╔══════════════════════════▼═══════════════════════════════════════╗
║                         DATA LAYER                               ║
║                                                                  ║
║  ┌────────────────────────────────────────────────────────────┐  ║
║  │                 EventRepositoryImpl                        │  ║
║  │                                                            │  ║
║  │  Step 1 ──► ResponseCache  (in-memory, TTL 15 min)         │  ║
║  │                 │ miss / forceRefresh                      │  ║
║  │  Step 2 ──► FakeEventInterceptor   (reads raw/events.json) │  ║
║  │                 │ success → update cache + persist Room    │  ║
║  │                 │ failure → fallback ↓                     │  ║
║  │  Step 3 ──► Room Database  (last known good data)          │  ║
║  └────────────────────────────────────────────────────────────┘  ║
║                                                                  ║
║  ┌─────────────────┐  ┌───────────────┐  ┌───────────────────┐   ║
║  │ ResponseCache   │  │ FakeEventApi  │  │    AppDatabase    │   ║
║  │ TTL: 15 min     │  │ events.json   │  │ EventDao          │   ║
║  └─────────────────┘  └───────────────┘  │ BookmarkDao       │   ║
║                                          └───────────────────┘   ║
╚══════════════════════════════════════════════════════════════════╝

          ┌──────────────────────────────────────┐
          │         BACKGROUND / SERVICES        │
          │  EventRefreshWorker (WorkManager)     │
          │  Interval: 30min | Constraints: network  │
          │  + battery not low | Backoff: exp     │
          └──────────────────────────────────────┘

          ┌──────────────────────────────────────┐
          │         DEPENDENCY INJECTION         │
          │  Hilt — SingletonComponent scope     │
          │  AppModule  DatabaseModule           │
          │  NetworkModule  ImageModule          │
          └──────────────────────────────────────┘
```

---

## 🔄 Sequence Diagrams

### 1 · App Launch → Load Event List

```
MainActivity      EventListScreen    EventListViewModel   GetEventsUseCase   EventRepositoryImpl
     │                  │                   │                    │                   │
     │  setContent()    │                   │                    │                   │
     ├─────────────────►│                   │                    │                   │
     │                  │ collectAsState()  │                    │                   │
     │                  ├──────────────────►│                    │                   │
     │                  │                   │ init { loadEvents }│                   │
     │                  │                   ├───────────────────►│                   │
     │                  │                   │                    │ invoke()           │
     │                  │                   │                    ├──────────────────►│
     │                  │                   │                    │                   │ 1. Check ResponseCache
     │                  │                   │                    │◄── cache miss ────│
     │                  │                   │                    │                   │ 2. FakeEventApi.getEvents()
     │                  │                   │                    │                   │    (reads assets/events.json)
     │                  │                   │                    │◄── Result.success─│
     │                  │                   │                    │                   │ 3. putEvents(cache)
     │                  │                   │                    │                   │ 4. insertEvents(Room)
     │                  │                   │◄── Flow<Result> ───│                   │
     │                  │◄── UiState(events)│                    │                   │
     │                  │  render cards     │                    │                   │
```

---

### 2 · Location Permission → Distance on Cards

```
EventListScreen      AccompanistPerms    EventListViewModel       LocationUtils
     │                     │                    │                       │
     │  LaunchedEffect      │                    │                       │
     ├────────────────────►│                    │                       │
     │                     │ request()          │                       │
     │  onGranted          │                    │                       │
     │◄────────────────────│                    │                       │
     │  onLocationPermissionGranted()           │                       │
     ├─────────────────────────────────────────►│                       │
     │                                          │  fetchLocation()      │
     │                                          ├──────────────────────►│
     │                                          │                       │ FusedLocationProvider
     │                                          │◄── Location ──────────│
     │                                          │  enrichWithDistance() │
     │                                          │  (Haversine per event)│
     │◄── UiState(events with distanceKm) ──────│                       │
     │  DistanceBadge visible on each card      │                       │
```

---

### 3 · Bookmark Toggle

```
EventCard        EventListViewModel    ToggleBookmarkUseCase   EventRepositoryImpl     BookmarkDao
    │                   │                       │                      │                   │
    │ onBookmarkClick() │                       │                      │                   │
    ├──────────────────►│                       │                      │                   │
    │                   │  optimistic UI update │                      │                   │
    │◄── re-render ─────│                       │                      │                   │
    │                   │  invoke(event)        │                      │                   │
    │                   ├──────────────────────►│                      │                   │
    │                   │                       │ toggleBookmark(event)│                   │
    │                   │                       ├─────────────────────►│                   │
    │                   │                       │                      │ getBookmark(id)   │
    │                   │                       │                      ├──────────────────►│
    │                   │                       │                      │◄── null (new) ────│
    │                   │                       │                      │ insertBookmark()  │
    │                   │                       │                      ├──────────────────►│
    │                   │  snackbar("Added")    │                      │◄── done ──────────│
    │◄── snackbar ──────│                       │                      │                   │
```

---

### 4 · Get Directions Deep Link

```
EventDetailScreen         DeepLinkUtils           Google Maps / Browser
       │                        │                          │
       │  "Get Directions" tap  │                          │
       ├───────────────────────►│                          │
       │                        │ build geo: URI           │
       │                        │ Intent(ACTION_VIEW)      │
       │                        │ setPackage(maps)         │
       │                        ├─────────────────────────►│
       │                        │                          │ Opens at
       │                        │                          │ lat/lng + label
       │                        │                          │
       │                        │ [no maps app installed?] │
       │                        │ open maps.google.com URL │
       │                        ├─────────────────────────►│ (browser)
```

---

### 5 · Background Refresh (WorkManager)

```
MyApplication       WorkManager         EventRefreshWorker    RefreshEventsUseCase    FakeEventApi
     │                   │                      │                     │                    │
     │ onCreate()        │                      │                     │                    │
     ├──────────────────►│                      │                     │                    │
     │ schedulePeriodicRefresh()                │                     │                    │
     │ (6h, network+battery constraints)        │                     │                    │
     │                   │                      │                     │                    │
     │                   │ [6h elapsed, ok]     │                     │                    │
     │                   ├─────────────────────►│                     │                    │
     │                   │                      │ doWork()            │                    │
     │                   │                      ├────────────────────►│                    │
     │                   │                      │                     │ invoke()           │
     │                   │                      │                     ├───────────────────►│
     │                   │                      │                     │◄── events ─────────│
     │                   │                      │                     │ update cache+Room  │
     │                   │                      │◄── Result.success() │                    │
     │                   │◄── schedule next ────│                     │                    │
```

---

## 🛠 Tech Stack

| Category          | Library                        |
| ----------------- | ------------------------------ |
| **UI**            | Jetpack Compose BOM            |
| **UI**            | Material 3                     |
| **UI**            | Compose Navigation             |
| **DI**            | Hilt (Dagger)                  |
| **DI**            | Hilt Navigation Compose        |
| **Local DB**      | Room Runtime + KTX             |
| **Networking**    | Retrofit 2                     |
| **Serialization** | Gson                           |
| **Image loading** | Coil Compose                   |
| **Background**    | WorkManager KTX                |
| **Permissions**   | Accompanist Permissions        |
| **Async**         | Kotlin Coroutines Android      |
| **Splash**        | Core SplashScreen              |
| **Prefs**         | DataStore Preferences          |
| **Testing**       | JUnit 4                        |
| **Testing**       | MockK                          |
| **Testing**       | Coroutines Test                |


---

## ⚙️ Build Configuration

```kotlin
// app/build.gradle.kts (key settings)
android {
    namespace      = "com.rogers.eventapp"
    compileSdk     = 35
    minSdk         = 26        // Android 8.0 Oreo+
    targetSdk      = 35
    versionCode    = 1
    versionName    = "1.0"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }

    lint {
        abortOnError       = false
        warningsAsErrors   = false
        checkReleaseBuilds = true
        xmlReport          = true   // app/build/reports/lint-results.xml
        htmlReport         = true   // app/build/reports/lint-results.html
    }
}

plugins used:
  com.google.dagger.hilt.android   → 2.50
  com.google.devtools.ksp          → 1.9.22-1.0.17
  kotlin("kapt")                   → for Hilt + Hilt-Work annotation processing
```

---

## 🚀 How to Run

### Prerequisites

| Tool | Minimum Version | Check command |
|---|---|---|
| Android Studio | Hedgehog 2023.1.1+ | — |
| JDK | 17 | `java -version` |
| Android SDK | API 35 (compileSdk) | SDK Manager |
| Gradle wrapper | 8.2+ | `./gradlew --version` |
| Device / Emulator | API 26+ (minSdk) | — |

---

### Step-by-Step

**1. Clone the repository**
```bash
git clone https://github.com/your-org/EventApp.git
cd EventApp
```

**2. Open in Android Studio**
```
File → Open → select the EventApp/ root folder
Wait for indexing to complete
```

**3. Sync Gradle**
```
Android Studio: File → Sync Project with Gradle Files
```
Or from terminal:
```bash
./gradlew build
```

**4. Confirm mock data asset is present**

The app reads events from a local JSON file — no real network needed:
```
app/src/res/raw/events.json
```
This file contains 10 sample Pune events. Do not delete it.

**5a. Run on Emulator (recommended for location testing)**
```
Android Studio → Device Manager → Create Device
  → Pixel 6, API 34 → Next → Finish
Click ▶ Run (Shift + F10)
```

**5b. Run on Physical Device**
```bash
# Enable USB Debugging on device, then:
adb devices                    # confirm device listed
./gradlew installDebug         # build + install
adb shell am start \
  -n com.rogers.eventapp/.MainActivity
```

**5c. Run release build**
```bash
./gradlew assembleRelease
# APK at: app/build/outputs/apk/release/app-release-unsigned.apk
```

---

### Running Tests

```bash
# All unit tests
./gradlew test

# Single test class
./gradlew test --tests "com.rogers.eventapp.GetEventsUseCaseTest"

# With verbose output
./gradlew test --info

# HTML report (open in browser after run)
open app/build/reports/tests/testDebugUnitTest/index.html
```

## 📐 Engineering Standards

### Architecture Principles

| Principle | How it's applied |
|---|---|
| **Separation of Concerns** | Strict 3-layer split. `domain` has zero Android imports. |
| **Dependency Rule** | Dependencies point inward only: `presentation → domain ← data` |
| **Single Source of Truth** | Room DB is persistence SSOT. UI reads domain Flows only. |
| **Unidirectional Data Flow (UDF)** | ViewModel holds `StateFlow<UiState>`. UI collects, emits events upward via lambdas. |
| **Repository Pattern** | `EventRepository` interface lives in domain. `EventRepositoryImpl` lives in data. ViewModels never touch DAOs or DTOs. |
| **Fail gracefully** | API failure → Room fallback → error state. Never crash the UI. |

---

### MVVM Contract

```
Composable Screen
  ├── Reads:   viewModel.uiState (StateFlow) via collectAsStateWithLifecycle()
  ├── Reads:   viewModel.snackbarMessage (SharedFlow) via LaunchedEffect
  ├── Calls:   viewModel.onXxx() functions (events)
  └── Never:   contains business logic, data access, or coroutine scopes

ViewModel
  ├── Exposes: UiState data class via StateFlow (never nullable)
  ├── Exposes: one-shot events via SharedFlow (snackbars, navigation)
  ├── Calls:   UseCases via viewModelScope
  └── Never:   imports Compose, View, or Activity

UseCase
  ├── Responsibility: exactly one business operation
  ├── Input:   plain Kotlin types / domain models
  ├── Output:  Flow<Result<T>> or suspend T
  └── Never:   imports Android framework classes

Repository Interface (domain)
  └── Defines the data contract in domain terms (Event, not EventDto/EventEntity)
```

---

### Naming Conventions

| Type | Pattern | Example |
|---|---|---|
| Screens | `{Feature}Screen` | `EventListScreen` |
| ViewModels | `{Feature}ViewModel` | `EventListViewModel` |
| UI State | `{Feature}UiState` | `EventListUiState` |
| Use Cases | `{Verb}{Noun}UseCase` | `GetEventsUseCase` |
| Repository interface | `{Noun}Repository` | `EventRepository` |
| Repository impl | `{Noun}RepositoryImpl` | `EventRepositoryImpl` |
| Room entities | `{Noun}Entity` | `EventEntity` |
| Remote DTOs | `{Noun}Dto` | `EventDto` |
| DI Modules | `{Scope}Module` | `DatabaseModule` |
| Workers | `{Noun}{Action}Worker` | `EventRefreshWorker` |
| Utils (objects) | `{Domain}Utils` | `DistanceUtils` |

---

### Code Style Rules

- Use `data class` for all state, models, and DTOs.
- Use `sealed class` for navigation routes.
- Use `object` for stateless utility classes.
- No magic numbers — all constants in `companion object` or top-level `const val`.
- `StateFlow` and `SharedFlow` everywhere — no `LiveData`.
- Immutable UI state — all mutations via `_uiState.update { copy(...) }`.
- Wrap legacy callbacks with `suspendCancellableCoroutine`.
- Coroutines only in `viewModelScope` or `CoroutineWorker.doWork()`.
- `@ApplicationContext` for any injected context — never `ActivityContext` in singletons.

---

### Caching Strategy

```
┌───────────────────────────────────────────────────────────┐
│ Layer 1 — ResponseCache (in-memory)                       │
│   TTL:     15 minutes                                     │
│   Bypass:  forceRefresh = true (pull-to-refresh, worker)  │
├───────────────────────────────────────────────────────────┤
│ Layer 2 — FakeEventApi (raw/events.json)               │
│   Delay:   800ms simulated latency                        │
│   Success: populate ResponseCache + persist to Room DB    │
├───────────────────────────────────────────────────────────┤
│ Layer 3 — Room Database (disk)                            │
│   Used:    on API failure as fallback                     │
│   Scope:   events table (auto-replaced on each refresh)   │
│            bookmarks table (never evicted)                │
└───────────────────────────────────────────────────────────┘

Image cache (Coil):
  Memory cache : 25% of available JVM heap
  Disk cache   : 100 MB  →  app_cache/image_cache/
  Policy       : ENABLED for memory + disk + network layers
```

---

### Error Handling Matrix

| Scenario | UI Behaviour |
|---|---|
| API failure + no DB data | `ErrorContent` with Retry button |
| API failure + DB has data | Silently uses stale Room data |
| Image load failure | Coil placeholder shown |
| Bookmark DB error | Snackbar error message, no crash |
| Location permission denied | Distance badges hidden; all other features work |
| No maps app installed | Falls back to `maps.google.com` in browser |
| Background refresh fails | `Result.retry()` — WorkManager exponential backoff |
| Deep link to unknown event ID | `ErrorContent` with "Event not found" message |

---

### Background Refresh Policy

```
WorkManager PeriodicWorkRequest:
  Repeat interval  : 6 hours
  Flex interval    : 30 minutes
  Network type     : CONNECTED
  Battery          : Not low
  Backoff policy   : EXPONENTIAL
  Uniqueness policy: KEEP  (won't queue duplicate work)
  Tag              : "event_refresh_work"
```

---

### Testing Standards

| Area | Tool | Notes |
|---|---|---|
| Mocking | MockK | `mockk()`, `coEvery`, `coVerify` |
| Flow assertion | Turbine | `flow.test { awaitItem() }` |
| Assertions | Google Truth | `assertThat(x).isEqualTo(y)` |
| Coroutines | `runTest` | `StandardTestDispatcher` |
| Coverage target | UseCases 100% | Repository cache + Utils 100% |

32 unit tests covering: `GetEventsUseCase`, `GetEventByIdUseCase`, `ToggleBookmarkUseCase`, `GetBookmarksUseCase`, `ResponseCache`, `DistanceUtils`, `FormatUtils`, `DateUtils`.

---

## ✅ Feature Checklist

- [x] Event list from local JSON (mock API with 800ms simulated latency)
- [x] Search by title
- [x] Pull-to-refresh (bypasses in-memory cache)
- [x] Event detail screen with full information
- [x] Bookmark events (persisted in Room SQLite)
- [x] Bookmarks tab with real-time Room observation
- [x] Location permission request (Accompanist)
- [x] Distance to each event (Haversine formula)
- [x] Deep link to Google Maps for navigation
- [x] Share event via system share sheet
- [x] In-app deep link support (`eventsapp://event/{id}`)
- [x] Image loading with Coil (memory + 100 MB disk cache)
- [x] In-memory response cache with 15-minute TTL
- [x] Room persistence of last-fetched events
- [x] WorkManager background refresh (every 6h)
- [x] Graceful network/API failure with Room fallback
- [x] Animated screen transitions (slide + fade)
- [x] Bottom navigation bar (Events / Bookmarks)
- [x] Splash screen (core-splashscreen)
- [x] Hilt dependency injection throughout all layers

---

## 📄 License

```
MIT License — free to use, modify, and distribute.
```
