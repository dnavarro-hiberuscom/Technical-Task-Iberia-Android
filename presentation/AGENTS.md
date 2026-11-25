# :presentation · Module guide

General rules are in the [root guide](../AGENTS.md). This module depends only on `:domain`.

## Structure

- `designsystem/` holds the Material 3 theme (`StarshipsTheme`), the colors and their contrast pairs (`Color.kt`), typography, shapes and the dimensions in `Dimens.kt`. Access spacing and sizes through `StarshipsTheme.spacing` and `StarshipsTheme.sizes`.
- `di/presentationModule` registers use cases as `factory` and ViewModels with `viewModelOf`.
- Each screen has its own folder (`search/` and `detail/`) with a public `XxxRoute` for `:app`, an internal `XxxScreen` that can be previewed and tested without a ViewModel, and its ViewModel and state.

## Compose and MVVM

- Data flows one way. The screen emits events such as `onQueryChange` and `onClearQuery`. The ViewModel exposes a single read-only `StateFlow<XxxUiState>`, and the Route observes it with `collectAsStateWithLifecycle()`.
- The ViewModel receives use cases through its constructor. Keep `StarshipRepository` access and Koin dependency resolution out of the class.
- Errors arrive as `StarshipException` and are turned into a typed state. The screen picks the message with `stringResource`. Exception messages never reach the UI.
- Use `:domain` models to show content where it makes sense. The API for `:app` consists of composables with callbacks and `Int` IDs.

## Theme, dimensions and text

- Define colors and dimensions in `dp` or `sp` in `designsystem/`. Add any missing dimensions there.
- Keep every piece of app text in resources, including content descriptions and units. Use arguments and `plurals` where needed. Add each key to the base English file (`res/values/strings.xml`) and to the Spanish translation (`res/values-es/strings.xml`). SWAPI data is shown as received.

## Accessibility

- Keep a persistent label on the search field, localized names for the clear and back actions, and a touch target of at least `StarshipsTheme.sizes.minTouchTarget`.
- Each result row is a single focusable, clickable element. For decorative images, use `contentDescription = null`.
- Communicate loading, empty and error states with text, and mark screen titles with `heading()`. Check that content stays visible with a large font size.

## Search pagination

The ViewModel keeps the results while loading the next page, and tracks the loading and failure states of the **Load more** button separately. A new edit cancels the pending load and resets the list.

## Starship images

`designsystem/StarshipImage` loads the image with Coil from the SWAPI ID. Keep both the CDN loading and the local radar icon in Presentation. Compose tests use the local resource and controlled repository responses.

## Tests

- **Unit (`src/test`).** ViewModels with `kotlinx-coroutines-test` and virtual time. Use cases with mocked repositories.
- **Compose (`src/androidTest`).** Search field semantics, selecting a row and the detail screen, with text read from resources. Run `./gradlew :presentation:connectedDebugAndroidTest`.
