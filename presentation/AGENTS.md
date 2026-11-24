# :presentation · module guide

Read the root guide first (`../AGENTS.md`). Depends only on `:domain`.

## Structure

- `designsystem/`: the single Material 3 theme (`StarshipsTheme`), colours with their contrast
  pairs (`Color.kt`), typography, shapes, and spacing and size tokens (`Dimens.kt`, read through
  `StarshipsTheme.spacing` / `StarshipsTheme.sizes`).
- `di/presentationModule`: use cases as `factory`, ViewModels with `viewModelOf`.
- One folder per screen (`search/`, and `detail/` in PR 3) with `XxxRoute` (public, used by
  `:app`), `XxxScreen` (internal, no ViewModel, previewable and testable) and its ViewModel/state.

## Compose and MVVM

- Unidirectional flow: the screen emits events (`onQueryChange`, `onClearQuery`, …); the
  ViewModel exposes a single read-only `StateFlow<XxxUiState>` that the Route observes with
  `collectAsStateWithLifecycle()`.
- The ViewModel receives its use cases in the constructor. It never uses `StarshipRepository`
  nor resolves Koin dependencies inside the class.
- Errors arrive as `StarshipException` and are translated into a typed state; the screen picks
  the `stringResource`. No exception message reaches the UI.
- `:presentation` may use `:domain` models to render content, but what it exposes to `:app` is
  only composables with callbacks and `Int` ids.

## Theme, tokens and texts

- No colour, `dp` or `sp` literals outside `designsystem/`. If a token is missing, add it there.
- Every app-owned text lives in resources (with arguments and `plurals` where needed), including
  content descriptions and units. `res/values/strings.xml` is the English default and
  `res/values-es/strings.xml` the Spanish translation; add every key to both. SWAPI data is
  rendered as received.

## Accessibility

- Search field with a persistent label, actions with a localised name (clear, back) and a touch
  target of at least `StarshipsTheme.sizes.minTouchTarget`.
- Each result row is a single focusable, clickable element; decorative images use
  `contentDescription = null`.
- States (loading, empty, error) are communicated with text; screen titles are `heading()`.
  Check with a large font scale that nothing gets clipped.

## Tests

- Unit (`src/test`): ViewModels with `kotlinx-coroutines-test` and virtual time, use cases with
  fake repositories.
- Compose (`src/androidTest`): semantics of the search field, row selection and detail, reading
  texts from resources.

## Planned

- PR 2: `SearchViewModel`, initial/loading/data/empty/error states, field with clear action and list.
- PR 3: `DetailViewModel` and the detail screen (name, model, manufacturer, length, crew).
