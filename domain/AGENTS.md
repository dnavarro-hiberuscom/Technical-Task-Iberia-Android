# :domain · Module guide

General rules are in the [root guide](../AGENTS.md). This module depends on nothing else in the project.

It's pure Kotlin/JVM. Keep Android, Retrofit, Koin and other project modules out of the domain.

## Contents

- `model/Starship` holds `id` (extracted from the SWAPI URL), `name`, `model`, `manufacturer`, `length` and `crew`.
- `model/Quantity` distinguishes `Known(value)` from `Unknown`. `value` keeps the original format (`12.5`, `1,600`, `30-165`).
- `model/SearchQuery` holds the query validation. `SearchQuery.from(raw)` trims leading and trailing whitespace and returns `null` if the result is empty.
- `model/StarshipException` defines `Connectivity`, `NotFound` and `Unexpected`. Cancellation is propagated without wrapping it.
- `repository/StarshipRepository` defines the contract with `suspend` functions that `:data` implements.
- `usecase/SearchStarshipsUseCase` builds a `SearchQuery` in `invoke(rawQuery)`. If it's empty, it returns an empty page without calling the repository. For valid queries it calls `repository.searchStarships(query, page)` and returns a `StarshipPage` with the results and the next page, if any.
- `usecase/GetStarshipUseCase` returns `repository.getStarship(id)` from `invoke(id)`.

## Rules and tests

- Use cases are stateless, have a single `operator fun invoke` and receive their dependencies through the constructor. They are registered in Koin from `:presentation`.
- The unit tests in `src/test` use MockK to mock the repository and check that empty queries make no calls, whitespace trimming, results, empty lists and failure propagation.
