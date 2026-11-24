# Starships · repository guide

Native Android app (Kotlin + Jetpack Compose) that searches starships on
[SWAPI](https://swapi.dev/documentation) (`/api/starships/`) and shows their detail.
Technical assessment: clarity, layer separation, correct cancellation and caching come first.

Before touching a module, read this guide and that module's guide (`<module>/AGENTS.md`).

## Status

| Block | Status |
|---|---|
| Four modules, shared theme, Koin, root navigation, entry screen | Done (PR 1) |
| `Starship` model, `SearchQuery`, `StarshipException` and the `StarshipRepository` contract | Done (PR 1) |
| Search: `SearchStarships`, Retrofit, DTOs, 3-minute cache, `SearchViewModel`, list | Done (PR 2) |
| Detail: `GetStarship`, `DetailViewModel`, screen, back-and-forth navigation | Done (PR 3) |
| Koin graph `verify()`, architecture test, README | Done (PR 3) |

## Module map

```
:app          → :presentation, :data   (assembly only)
:presentation → :domain
:data         → :domain
:domain       → (no project module)
```

| Module | Type | Contains |
|---|---|---|
| `:app` | Android app | `StarshipsApplication` (Koin), `MainActivity`, `navigation/StarshipsNavHost` |
| `:presentation` | Android library + Compose | Screens, ViewModels, UI state, `designsystem/`, `di/presentationModule` |
| `:domain` | Pure Kotlin/JVM | Models, `StarshipRepository` contract, use cases |
| `:data` | Android library | Retrofit, DTOs, mapping, cache, repository (all `internal`), `di/dataModule` |

Rules Gradle does not enforce, checked by `app/src/test/.../ArchitectureRulesTest`:

- Only the `:app` composition root (`StarshipsApplication`) imports anything from `:data`, and only `dataModule`.
- ViewModels reach data only through use cases received in the constructor; they never use
  `StarshipRepository` nor resolve Koin objects (`get()`, `inject()`, `KoinComponent`).
- `:app` does not import `:domain` models or use cases: `:presentation` exposes composables
  that take callbacks and simple identifiers (`Int`).
- Module dependencies are declared with `implementation`.

## Flow

- **Search (done, PR 2):** text field → `SearchViewModel` (query Flow + `collectLatest`, 300 ms wait
  inside the cancellable block) → `SearchStarships` → `StarshipRepository` (3-minute in-memory
  cache per normalised query) → `GET /api/starships/?search=…` → list.
  Only the first page of results is used.
- **Detail (done, PR 3):** row → `navController` with the `id` → `DetailViewModel` → `GetStarship`
  → `GET /api/starships/{id}/`. The `id` comes from the resource URL.

## Common conventions

- Everything in the codebase is in English: code, comments, guides, commits and PRs.
  User-facing text lives in string resources (see `presentation/AGENTS.md`).
- Official Kotlin style (`kotlin.code.style=official`), packages under `es.technical_task_iberia_android`.
- `README.md` is the entry point for reviewers; keep its commands and decisions in sync with these guides.
- Versions only in `gradle/libs.versions.toml`. The project versions are kept (AGP 9.4.1,
  Kotlin 2.2.10, Compose BOM 2026.02.01, Gradle 9.6); Lint warnings about newer versions are expected.
- Every text shown on screen or announced by TalkBack lives in resources (`strings.xml`, `plurals`).
- Colours, `dp` and `sp` only in `presentation/designsystem`. Every meaningful number has a single
  named constant with its unit (e.g. `SearchDebounce`, `SearchCacheTtl`).
- No test code or test data in production: fixtures live in tests and previews.

## Branches and PRs

- Base branch: `main`. One branch per PR (`feature/<topic>`); if the previous one is still under
  review, the next one branches from it and its PR targets it.
- No direct pushes to `main`, no force-push. Commit messages in English, imperative mood.
- Every PR states its goal, how to test it, the checks that were run and any limitation, and
  updates these guides when they change.

## Validation

Requirements: JDK 17+ for Gradle (Android Studio bundles one), Android SDK 37.

```bash
./gradlew assembleDebug               # builds every module
./gradlew test                        # unit tests (JVM): domain, data, ViewModels, Koin graph, architecture rules
./gradlew lint                        # Android Lint
./gradlew connectedDebugAndroidTest   # Compose tests in :presentation and :app (emulator or device)
./gradlew installDebug                # installs the app
```
