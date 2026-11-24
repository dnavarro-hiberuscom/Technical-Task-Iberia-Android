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
  texts from resources. Run with `./gradlew :presentation:connectedDebugAndroidTest`.

## Search screen (done, PR 2)

- `search/SearchViewModel`: typed text → `MutableStateFlow` → `collectLatest`; inside the block
  `delay(SearchDebounce)` and then `SearchStarships`. `onQueryChange` shows the text and `Loading`
  at once (old results disappear) and `onClearQuery` goes back to `Initial`. After the use case
  returns, `ensureActive()` prevents a cancelled search from writing state. `CancellationException`
  is rethrown.
- `search/SearchUiState`: `query` + `SearchContent` (`Initial`, `Loading`, `Results`, `Empty`,
  `Error(SearchError)`). `SearchError` is a typed enum mapped to string resources in the screen.
- `search/SearchDebounce`: the single 300 ms constant.
- `search/SearchScreen`: public `SearchRoute(onStarshipClick)` (gets the ViewModel with
  `koinViewModel()`), internal `SearchRoute(viewModel, …)` for tests and stateless `SearchScreen`.
- Tests: `src/test/.../SearchViewModelTest` (virtual time; includes the guard that a new text
  cancels the in-flight search before its own debounce elapses) and
  `src/androidTest/.../SearchScreenTest` (field found by semantics and cleared, row selected).
  Compose tests use a stub repository and read texts from resources.
- The clear icon is a vector in `res/drawable` to avoid the material-icons dependency.

## Detail screen (done, PR 3)

- `detail/DetailViewModel(starshipId, GetStarship)`: loads on creation, exposes
  `StateFlow<DetailUiState>` (`Loading`, `Content`, `Error(DetailError)`) and `onRetry()`.
  The id arrives with `parametersOf(id)` (`koinViewModel(parameters = …)`).
- `detail/DetailScreen`: public `DetailRoute(starshipId, onBack)`; shows name, model,
  manufacturer, length and crew. Each label/value pair is one merged semantics node. Unknown values
  use `detail_value_unknown`, length uses `detail_length_value` (`%1$s m`). The back action has a
  localised name; `NotFound` has no retry button.
- Tests: `DetailViewModelTest` (unit) and `DetailScreenTest` (Compose, unknown values, back, retry).
