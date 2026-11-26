# Technical decisions

## The API

I chose SWAPI because I like Star Wars and because it had exactly what the assessment asked for: search by name or model, and a detail view by ID. That ID comes from the resource URL itself. [SWAPI starships documentation](https://swapi.dev/documentation#starships)

SWAPI has no images. While searching I found **Star Wars Gallery** on Reddit, a CDN that uses the same IDs, so I load the image separately. If the CDN fails, the detail screen still shows the data with a local radar icon. [Image source](../README.md#starship-images)

Some values come as `unknown` or `n/a`, and some numbers include separators or ranges (`1,600`, `30-165`). The model represents them with `Quantity.Known(value)` and `Quantity.Unknown`, and the screen shows unknown values as text.

## Libraries

### Jetpack Compose and Material 3

The app started from scratch, so I used the toolkit Google recommends today for native UIs. Material 3 gives me common components and a single theme for search and detail. If the UI ever had to be shared with iOS, Compose Multiplatform builds on the same model. [Jetpack Compose](https://developer.android.com/compose) · [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/)

### Koin

For a small app it was enough to declare dependencies in Kotlin, right where the modules are wired together. ViewModels and use cases get everything through their constructor, so in tests I simply pass test doubles. Koin also works with Kotlin Multiplatform if the project grew in that direction. [Koin for KMP](https://insert-koin.io/docs/reference/koin-mp/kmp/)

### Retrofit, OkHttp and Kotlin Serialization

Since the deliverable was Android only, Retrofit with OkHttp was a good fit: search and detail are typed `suspend` functions, and it covers a key requirement of the assessment: cancelling the coroutine cancels the HTTP request. Kotlin Serialization turns the JSON into DTOs.

If the network layer had to be shared with iOS, I would use Ktor Client. [Ktor platforms](https://ktor.io/docs/client-supported-platforms.html)

### Navigation Compose

Two Compose screens with a simple flow. The detail route receives the ID and, when you go back, search keeps its query and results.

### Coil

I already knew it, and `AsyncImage` plugs straight into Compose. Images have their own loading and cache, so the data shows up as soon as it arrives and the image fills in afterwards. [Coil with Compose](https://coil-kt.github.io/coil/compose/)

## Version catalog and bundles

All versions live in `gradle/libs.versions.toml`. To avoid repeating the same list of libraries in each module, the catalog defines seven *bundles*, which are just named groups:

| Bundle         | Used by              | What it groups                                                                  |
|----------------|----------------------|---------------------------------------------------------------------------------|
| `data`         | `:data`              | Koin, coroutines, Kotlin Serialization, Retrofit with its converter, and OkHttp. |
| `presentation` | `:presentation`      | Koin for ViewModels and Compose, lifecycle, Compose UI, Material 3 and Coil.    |
| `app`          | `:app`               | Koin for Android, activity and navigation for Compose, core KTX and Kotlin Serialization. |
| `app-test`     | `:app` unit tests    | JUnit and Koin test.                                                            |
| `data-test`    | `:data` tests        | JUnit and `kotlinx-coroutines-test`.                                            |
| `domain-test`  | `:domain` tests      | JUnit, `kotlinx-coroutines-test` and MockK. `:domain` has no production libraries, so it has no bundle of its own. |
| `presentation-test` | `:presentation` unit tests | JUnit and `kotlinx-coroutines-test`.                                      |

- Each module uses them in one line, for example `implementation(libs.bundles.data)`. Nothing changes compared to declaring them one by one (versions still come from the catalog), but I find it more practical to keep each layer's libraries in a single place.
- BOMs (`koin-bom`, `compose-bom`) go separately with `platform(...)`, in each module.
- Anything only one module uses (Compose instrumented tests, `ui-tooling`) stays on its own in that module's `build.gradle.kts`.
- To add a shared library, declare it in `[libraries]` and add its alias to the right bundle.

## Modules

The idea is simple: **Domain is the contract**. Data implements it, Presentation uses it, and neither knows about the other. App connects them at startup.

| Module          | What it's for                                                                                     | Depends on, and why                                                                                        |
|-----------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `:domain`       | Models, use cases and the `StarshipRepository` interface.                                         | Nothing. It's pure Kotlin, with no Android or Retrofit, so its tests are simple and fast.                  |
| `:data`         | Implements `StarshipRepository` with Retrofit, DTOs, mapping and cache. Everything is `internal` except `dataModule`. | `:domain`, because it has to see the interface to implement it. Domain never points to Data.               |
| `:presentation` | Screens, ViewModels and theme. Calls use cases, never the repository.                             | `:domain`, to use the use cases and the `Starship` model. It doesn't know whether data comes from the network or the cache. |
| `:app`          | Starts the app: initializes Koin, hosts the activity and builds the navigation.                   | `:presentation` to show the screens, and `:data` only to register `dataModule` in Koin.                    |

**How they connect.** The use cases need a `StarshipRepository`, but they only know the interface. At startup, `StarshipsApplication` registers `dataModule` in Koin, which provides the implementation, and `presentationModule`, with the use cases and ViewModels. That's why `:app` depends on `:data`, and it's its only import from that module. The repository and the HTTP client are `single`, so the cache lives as long as the app. Use cases are `factory`, and Domain knows nothing about Koin. `KoinGraphTest` checks the graph with `verify()`.

**What I get out of it.** In the ViewModel tests I can simulate results, failures and cancellations without a network, and I can swap the HTTP client or the cache without touching anything outside Data. `ArchitectureRulesTest` makes sure nobody crosses these boundaries: `:app` only imports `dataModule`, and ViewModels receive use cases through their constructor.

## How search works

![Search flow across Compose, the ViewModel, the domain, the repository and SWAPI](./search-flow.png)

[Open the diagram](./search-flow.png)

1. When you type `wing`, Compose sends each edit to `SearchViewModel`, which publishes the state in a read-only `StateFlow`. Compose observes it in a lifecycle-aware way.
2. `collectLatest` cancels the previous work as soon as a new edit arrives. Inside the new block, the ViewModel waits 300 ms and calls `SearchStarshipsUseCase`. If an HTTP request was in flight, it is cancelled too. An empty query takes the screen back to its initial state.
3. The use case validates the query and calls the repository. If there is a valid response for that query, Data returns it without going to the network. If there isn't one, or it has expired, it requests the page from SWAPI and stores the response. Each page is kept in memory for three minutes, including searches with no results. Tests use a controlled clock to check expiry.
4. Before publishing a result or an error, the ViewModel checks that the search is still active and matches the current query. A network failure becomes a typed error.

If there are more pages, **Load more** appears. The app requests the next one with the same query, adds the new starships without duplicates and keeps the visible ones while loading or if a retry is needed. If you change the query, that request is cancelled and it starts again from the first page.

Tapping a row opens the detail screen by ID: `GetStarshipUseCase` fetches the data and the detail ViewModel publishes the state. Coil loads the image on its own, both in the list and in the detail screen.

### Why it waits 300 ms (debounce)

The wait is there to **save API calls and avoid overloading the server**. Without it, typing `x-wing` would fire one request per keystroke, and almost all of them would be thrown away.

It's not what makes search correct. Cancellation is handled by `collectLatest`, with or without the wait: editing cuts off the request in flight and a stale result is never shown. It's not there to make up for slowness either, because the SWAPI endpoint responds very quickly.

### Why the cache is in memory

The brief says an in-memory cache is enough and disk persistence is optional, so I kept it simple, with a map keyed by query and page, with a three-minute expiry (`SearchCacheTtl`), behind the `StarshipRepository` interface. It does what is asked, with no network call while the entry is valid and a refresh once it expires, and it is easy to test with a controlled clock.

The trade-off is that the cache is lost when the process dies and there is no offline mode. I didn't add Room because it brings new dependencies, entities, a DAO and extra mapping, and none of that improves what the brief values most, which is clarity, separation of layers and cancellation. If offline support were needed, Room would fit as a second level inside `:data`, with no changes to `:domain` or `:presentation`.

## Accessibility

- Text and messages live in resources. The search field has a label, and the clear and back actions have translated descriptions.
- Each result is read and tapped as a single element. Titles are headings, and on the detail screen each label is grouped with its value. Decorative icons are not read out.
- While loading, the screen shows text and progress semantics, and the Rebel emblem spins. Status messages are announced without interrupting.
- On narrow screens or with large text, the cards and detail fields stack vertically.

## Validation

| Area         | What it checks                                                                                                                       |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------|
| Domain       | Query validation, whitespace trimming and use of the repository through its contract.                                               |
| Data         | Mapping, cache still valid at 2:59 and expired at 3:00, refresh and retries, with a fake service and a controlled clock.             |
| Presentation | Cancellation before the wait ends, discarding stale results and errors, and detail loading and retry.                                |
| App          | Koin graph and dependency rules between modules.                                                                                     |
| Compose      | Editing and clearing the search, cancellation notice, search with no results, detail fields and going back to the same query.        |

Compose tests use controlled data and a local image.

### Manual checks

Against the real SWAPI I checked that searching for `e` and tapping **Load more** goes from 10 to 20 starships. I also opened the X-wing, went back to the query, cleared the field and tried a search with no results. I reviewed the layout on a simulated tablet and on a small screen with dark mode and text at 160 %. A manual check with TalkBack is still pending.
