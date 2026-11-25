# Starships

Starships is an Android app to search Star Wars starships and view their details. It's built with Kotlin, Jetpack Compose and Material 3. For this technical assessment I focused on making search responsive while you type, on separating the code's responsibilities, and on being able to verify cancellation and caching with tests.

## API

| Action                   | Request                                                |
|--------------------------|--------------------------------------------------------|
| Search by name or model  | `GET https://swapi.dev/api/starships/?search=wing`     |
| Load another page        | `GET https://swapi.dev/api/starships/?search=e&page=2` |
| Get details by ID        | `GET https://swapi.dev/api/starships/{id}/`            |

- I use the fields `name`, `model`, `manufacturer`, `length`, `crew` and `url`. The ID comes from the last segment of `url` (`…/starships/12/` → `12`).
- The list first shows up to ten matches. If SWAPI has more, you can tap **Load more** at the bottom. The app keeps the visible rows while it adds the next page, and lets you retry if loading fails.

### Starship images

SWAPI doesn't return images. I use the community CDN **Star Wars Gallery**, which uses the same IDs (`https://starwarsgallery.netlify.app/assets/starships/{id}.jpg`), loaded with Coil. If it fails, a local icon is shown and the data is still available. [Author's documentation](https://dev.to/sergiocoitino/free-images-for-swapi-the-star-wars-api-no-api-key-cors-enabled-3873)

## Requirements and commands

- Android Studio compatible with Android Gradle Plugin 9.3 and Android SDK 37. The app installs on Android 10 (API 29) and later.
- The project uses the Gradle Wrapper and requests JDK 25 for the Gradle daemon. [Daemon configuration](gradle/gradle-daemon-jvm.properties)
- The app needs internet access. Unit tests run with controlled data.

```bash
./gradlew installDebug                # Build and install the app on a device or emulator
./gradlew test                        # Run the unit tests
./gradlew connectedDebugAndroidTest   # Run the Compose tests on a device or emulator
./gradlew lint                        # Run Android Lint
```

## Architecture and decisions

The details are in the [technical decisions](docs/technical-decisions.md): chosen libraries, how search flows through each module, debounce, bundles and tests.

- **Architecture.** Four Gradle modules: `:domain` (pure Kotlin), `:data`, `:presentation` and `:app`, which only wires things together. Dependencies point towards `:domain`.
- **Cache.** In memory for three minutes per normalized query and page. If there's a valid entry, no network call is made; if it has expired, the data is fetched and the cache updated.
- **Cancellation.** `collectLatest` cancels the previous search when the query is edited, including the HTTP call, and stale results are never shown.

## Tests

There are unit tests for Domain with a mocked repository, for Data with a controlled clock (cache hits and expiry at three minutes), and for the ViewModels with virtual time (cancellation and discarding stale results). Compose tests run on a device or emulator. The full breakdown is in the [technical decisions](docs/technical-decisions.md#validation).

```bash
./gradlew :data:testDebugUnitTest --tests '*StarshipRepositoryCacheTest'
./gradlew :presentation:testDebugUnitTest --tests '*SearchViewModelTest'
```
