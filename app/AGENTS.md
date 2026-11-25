# :app · Module guide

General rules are in the [root guide](../AGENTS.md). This module depends on `:presentation` and `:data`, and only uses `dataModule` from `:data`.

`:app` wires the application together. It contains these entry points:

- `StarshipsApplication` starts Koin with `appModules` (`dataModule` and `presentationModule`). It's the only file that imports from `:data`, and it only uses `data.di.dataModule`.
- `MainActivity` applies `StarshipsTheme` once and shows `StarshipsNavHost`.
- `navigation/StarshipsNavHost` defines the root graph with type-safe routes (`@Serializable`). Each destination calls a public composable from `:presentation` and passes it callbacks or simple IDs. `SearchDestination` is the start destination. `DetailDestination(starshipId: Int)` carries the ID, and `entry.toRoute()` reads it. Going back pops the detail screen off the back stack, so search keeps its ViewModel, query and results.
- `AndroidManifest.xml` declares the `INTERNET` permission that Data needs.

## Rules

- Keep search, network, cache and repository logic out of the activity and the navigation.
- Use callbacks with simple types from `:presentation` when you need a value. Keep `:domain` models and use cases out of `:app`.
- Screen text and the theme live in `:presentation`. Only `app_name` is defined here.
- To add a destination, declare a `@Serializable` route and register it with `composable<Route> { XxxRoute(...) }`.

## Tests

- `src/test/KoinGraphTest` runs `verify()` on `appModules`. It declares `Int` as an extra type because `DetailViewModel` receives the ID through `parametersOf`.
- `src/test/ArchitectureRulesTest` checks that only `StarshipsApplication` accesses `dataModule`, that `:app` stays separate from `:domain`, and that `:presentation` and `:domain` have no references to `:data`. ViewModels use use cases and models, without accessing the repository or resolving Koin dependencies.
- `src/androidTest/SearchToDetailFlowTest` uses the root graph and the real Koin modules, and replaces the repository with `loadKoinModules`. It relies on `androidTestImplementation(project(":domain"))`, a test-only dependency. `build.gradle.kts` includes a `concurrent-futures` constraint because AGP aligns the test and runtime classpaths.
