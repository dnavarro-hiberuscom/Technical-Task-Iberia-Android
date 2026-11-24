# :app · module guide

Read the root guide first (`../AGENTS.md`).

`:app` only assembles the application. It contains:

- `StarshipsApplication`: starts Koin with `appModules` (`dataModule` + `presentationModule`).
  It is the **only** file that imports from `:data`, and only `data.di.dataModule`.
- `MainActivity`: applies `StarshipsTheme` once and renders `StarshipsNavHost`.
- `navigation/StarshipsNavHost`: root graph with type-safe routes (`@Serializable`). Each
  destination calls a public composable from `:presentation` and passes callbacks or simple ids.
  `SearchDestination` is the start; `DetailDestination(starshipId: Int)` carries only the id and
  `entry.toRoute()` reads it. Going back pops the detail, so the search entry keeps its ViewModel,
  query and results.
- `AndroidManifest.xml` declares the `INTERNET` permission the data layer needs.

## Rules

- No search logic, networking, cache or repositories in the Activity or in navigation.
- Do not import `:domain` models or use cases; if a value is needed, `:presentation` exposes a
  callback with a simple type.
- Screen texts and the theme live in `:presentation`; only `app_name` lives here.
- A new destination = a `@Serializable` route + `composable<Route> { XxxRoute(...) }`.

## Tests

- `src/test/KoinGraphTest`: `verify()` of `appModules` (`Int` is an extra type because
  `DetailViewModel` receives the id with `parametersOf`).
- `src/test/ArchitectureRulesTest`: only `StarshipsApplication` imports `dataModule`; `:app` does
  not import `:domain`; `:presentation` and `:domain` never reference `:data`; ViewModels use only
  use cases and models, with no repository and no Koin lookups.
- `src/androidTest/SearchToDetailFlowTest`: real root graph and real Koin modules with only the
  repository replaced (`loadKoinModules`). It needs `androidTestImplementation(project(":domain"))`,
  which is test-only. `build.gradle.kts` has a `concurrent-futures` constraint because AGP aligns
  the test and runtime classpaths.
