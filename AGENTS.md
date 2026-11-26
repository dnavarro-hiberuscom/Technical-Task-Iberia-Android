# Starships · Agent guide

Native Android app built with **Kotlin, Jetpack Compose and Material 3**. It searches starships in [SWAPI](https://swapi.dev/documentation) (`/api/starships/`) and shows their details. This is a technical assessment: clarity, layer separation, cancellation, caching and tests are what matter.

Before changing a module, also read its guide (`<module>/AGENTS.md`). The reasoning behind each decision lives in [docs/technical-decisions.md](docs/technical-decisions.md); don't repeat it here.

## Layers

| Module          | Role                                         | What's inside                                                                                                         | Depends on                |
|-----------------|----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|---------------------------|
| `:domain`       | The contract: what a starship is and what you can do with it | Models (`Starship`, `Quantity`, `SearchQuery`, `StarshipException`), the `StarshipRepository` interface and use cases | Nothing (pure Kotlin/JVM) |
| `:data`         | Fetches the data                             | Retrofit, DTOs, mapping, in-memory cache and `StarshipRepositoryImpl`, all `internal`. Only `dataModule` is public     | `:domain`                 |
| `:presentation` | Shows the data                               | Compose screens, ViewModels, UI states, `designsystem/` (Material 3 theme) and `presentationModule`                   | `:domain`                 |
| `:app`          | Starts the app and wires everything together | `StarshipsApplication` (starts Koin), `MainActivity` and `navigation/StarshipsNavHost`                                 | `:presentation`, `:data`  |

`ArchitectureRulesTest` enforces these rules. If one fails, fix the code, not the test:

- `:app` imports only `dataModule` from `:data`, and only from `StarshipsApplication`.
- `:app` talks to `:presentation` through composables with callbacks and `Int` IDs. It never imports models or use cases from `:domain`.
- ViewModels receive use cases through their constructor. They never use `StarshipRepository` or Koin (`get()`, `inject()`, `KoinComponent`).
- Module dependencies are declared with `implementation`.

## Flow

- **Search:** text field → `SearchViewModel` (`collectLatest` with a 300 ms wait inside the cancellable block) → `SearchStarshipsUseCase` → repository (3-minute cache per query and page) → `GET /starships/?search=…&page=…`. Editing the query cancels the request in flight.
- **Detail:** row → navigation with the ID → `DetailViewModel` → `GetStarshipUseCase` → `GET /starships/{id}/`.

## Code rules

- **Language:** everything is in English: code, comments, commits, PRs, the README, `docs/` and every `AGENTS.md`. The only Spanish text is the app's own translation in `values-es/`.
- **Style:** official Kotlin code style, packages under `es.technical_task_iberia_android`.
- **UI:** Jetpack Compose with Material 3 only.
  - Colors, typography and dimensions (`dp`, `sp`) live in `presentation/designsystem`.
  - User-facing text lives in resources, in English (`values/`) and Spanish (`values-es/`).
- **Constants:** one per meaningful value, with a name and a unit (`SearchDebounce`, `SearchCacheTtl`).
- **Dependencies:** declared in `gradle/libs.versions.toml`; shared ones go in their bundle (`app`, `data`, `presentation`, their `-test` ones, and `domain-test`). BOMs are added with `platform(...)` in each module.
- **Test data:** only in tests and previews, never in production code.

## Tests

Every behavior change comes with a test. `./gradlew test` passing is the minimum to deliver.

| Layer          | How it's tested                                                                      |
|----------------|--------------------------------------------------------------------------------------|
| Domain         | Unit tests with the repository mocked with MockK                                     |
| Data           | Unit tests with `FakeStarshipService` and `TestTimeSource` (cache and expiry), no network |
| Presentation   | ViewModels with `kotlinx-coroutines-test` and virtual time. Compose in `androidTest` |
| App            | `KoinGraphTest` (graph checked with `verify()`) and `ArchitectureRulesTest`          |

## Documentation

When a change is significant (new screen, new library, architecture change, cache or cancellation change, new commands), update:

- the [README](README.md), briefly, since it's the first thing the reviewer reads;
- [technical-decisions.md](docs/technical-decisions.md), with the details and the reasoning;
- the `AGENTS.md` guide of the affected module.

Minor changes don't need documentation updates.

## Environment and commands

Gradle Wrapper 9.6, JDK 25 for the Gradle daemon (already set in `gradle/gradle-daemon-jvm.properties`), AGP 9.3.0, Kotlin 2.2.10 and Android SDK 37. Minimum version is Android 10 (API 29).

```bash
./gradlew assembleDebug               # Build all modules
./gradlew test                        # Unit tests for all modules
./gradlew lint                        # Android Lint
./gradlew connectedDebugAndroidTest   # Compose tests (needs a device or emulator)
./gradlew installDebug                # Install the app
```

To run a single test class:

```bash
./gradlew :data:testDebugUnitTest --tests '*StarshipRepositoryCacheTest'
./gradlew :presentation:testDebugUnitTest --tests '*SearchViewModelTest'
./gradlew :domain:test
```

## Branches and PRs

- The main branch is `main`. **Never commit, push or merge on `main`.**
- Always work on your own branch: `feature/<topic>`, or `fix/<topic>` and `chore/<topic>` depending on the change.
- If the previous PR is still in review, branch off it and open the new PR against that branch.
- Commit messages are in English and in the imperative mood ("Add search cache").
- The PR description states the goal, how to test it, which checks you ran and any limitations.

## Restrictions

- Don't change the architecture (modules, layers or the dependencies between them) unless explicitly asked.
- Don't skip any step: tests, Lint, documentation and the branch rules always apply, even for small changes.
- Don't upgrade dependency versions or add libraries unless asked. Lint warnings about newer versions are expected.
- Don't force-push or rewrite published history.
- Don't disable tests or Lint rules to make something pass.
- Don't put business, network or cache logic in `:app`.
- Don't add keys or secrets. SWAPI doesn't need any.
