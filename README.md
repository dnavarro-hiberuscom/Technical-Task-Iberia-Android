# Starships

Small Android app (Kotlin, Jetpack Compose, Material 3) that searches starships on
[SWAPI](https://swapi.dev/documentation) and shows their detail. It is a technical assessment, so
it favours clarity, layer separation, correct cancellation and a testable cache over features.

## API

| Use | Request |
|---|---|
| Search | `GET https://swapi.dev/api/starships/?search=wing` |
| Detail | `GET https://swapi.dev/api/starships/{id}/` |

- Fields used: `name`, `model`, `manufacturer`, `length`, `crew` and `url`.
- The id is the last segment of the resource `url` (`…/starships/12/` → `12`).
- Only the **first page** of matches is used (10 results at most). The exercise is about search and
  detail, so pagination is intentionally left out.
- SWAPI publishes unknown values as `unknown` or `n/a`, and some figures with separators or ranges
  (`1,600`, `30-165`). They are modelled as `Quantity.Known(value)` / `Quantity.Unknown`, and the
  UI shows unknown values as text instead of as numbers.

## Requirements and commands

- Android Studio with JDK 17+ and Android SDK 37; `minSdk` is 29. Use the Gradle wrapper.
- Internet access to run the app; unit tests do not need it.

```bash
./gradlew installDebug                # build and install on a device or emulator
./gradlew test                        # unit tests: domain, data (cache, mapping), ViewModels, Koin graph, architecture rules
./gradlew connectedDebugAndroidTest   # Compose tests (needs an emulator or device)
./gradlew lint                        # Android Lint
```

## Decisions

**Modules** (arrows are compile-time dependencies, all `implementation`):

```
:app → :presentation, :data      :presentation → :domain      :data → :domain      :domain → –
```

- `:domain` is pure Kotlin: `Starship`, `StarshipRepository`, `SearchStarships`, `GetStarship` and the
  query rules (`SearchQuery` trims the text; blank queries never reach the repository).
- `:data` keeps Retrofit, DTOs, mapping and cache `internal` and exposes only `dataModule`.
- `:presentation` has the screens, ViewModels, UI state and the single Material 3 theme.
- `:app` only starts Koin, hosts the Activity and wires the navigation graph. It exposes no domain
  types to the screens: they receive callbacks and a plain `Int` id.
- Gradle enforces the module arrows. Two rules it cannot enforce are checked by
  `ArchitectureRulesTest`: only `StarshipsApplication` imports `dataModule` from `:data`, and
  ViewModels reach data through use cases (no repository, no Koin lookups).

**Koin.** `dataModule` registers the network client and the repository (which owns the cache) as
`single`. `presentationModule` registers use cases as `factory` and ViewModels with `viewModelOf`,
so `:domain` stays free of Koin. `KoinGraphTest` verifies the whole graph with `verify()`.

**Cache.** In memory, per query, keyed by the lowercased and trimmed text. An entry lives
`SearchCacheTtl` (3 minutes) from the moment a successful response arrives: it is valid while its
age is lower than 3 minutes and refreshed at exactly 3 minutes. Empty results are cached; errors and
cancellations are not. Time comes from an injected monotonic `TimeSource`, so tests advance it
without waiting.

**Cancellation.** Typed texts form a Flow collected with `collectLatest`; the 300 ms wait
(`SearchDebounce`) is inside the cancellable block, so a new text cancels the in-flight request
immediately (not after the debounce). Retrofit `suspend` calls cancel the HTTP request, the repository
never swallows `CancellationException`, and `ensureActive()` stops an old answer from writing state.
Old results are removed as soon as the text changes, and clearing the field resets the screen.
`SearchViewModelTest` fails if `debounce` is placed before `collectLatest`.

**UI and accessibility.** All text lives in string resources (English default, Spanish
translation). Colours, type, shapes, spacing and sizes are defined once in `presentation/designsystem`.
The search field has a persistent label, actions have localised names and at least a 48 dp touch
target, and each result row is a single focusable element.

## Tests

| Kind | Where | What |
|---|---|---|
| Unit, mocked repository | `domain` | blank query without calls, trimming, results, failures |
| Unit, fake service + controllable clock | `data` | one call for equivalent queries, per-query entries, just before and exactly at 3 min, reuse of the refreshed value, retry after failure, mapping |
| Unit, virtual time | `presentation` | cancellation before the next debounce, no stale results or errors, clear, typed errors, detail loading/retry |
| Unit | `app` | Koin graph `verify()`, architecture rules |
| Compose (device) | `presentation`, `app` | search field semantics and clearing, row selection, detail fields and unknown values, full flow search → detail → back keeping the query |

The Compose tests use controlled data, not the network; test data is never used in production code.

## Visual phase

Illustrations, animations and custom visual design are out of scope for this delivery. The screens use
standard Material 3 components on top of the shared theme.
