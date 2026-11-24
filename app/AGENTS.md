# :app · module guide

Read the root guide first (`../AGENTS.md`).

`:app` only assembles the application. It contains:

- `StarshipsApplication`: starts Koin with `appModules` (`dataModule` + `presentationModule`).
  It is the **only** file that imports from `:data`, and only `data.di.dataModule`.
- `MainActivity`: applies `StarshipsTheme` once and renders `StarshipsNavHost`.
- `navigation/StarshipsNavHost`: root graph with type-safe routes (`@Serializable`). Each
  destination calls a public composable from `:presentation` and passes callbacks or simple ids.

## Rules

- No search logic, networking, cache or repositories in the Activity or in navigation.
- Do not import `:domain` models or use cases; if a value is needed, `:presentation` exposes a
  callback with a simple type.
- Screen texts and the theme live in `:presentation`; only `app_name` lives here.
- A new destination = a `@Serializable` route + `composable<Route> { XxxRoute(...) }`.

## Planned

- PR 3: detail destination (`DetailDestination(starshipId: Int)`), going back while keeping the
  search, and a test that verifies `appModules` with Koin.
