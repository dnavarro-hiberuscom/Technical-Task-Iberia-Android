# :domain · module guide

Read the root guide first (`../AGENTS.md`).

Pure Kotlin/JVM: it does not depend on Android, Retrofit, Koin or any other project module.
If a change needs one of those libraries, it does not belong here.

## Contents

- `model/Starship`: `id` (taken from the SWAPI URL), `name`, `model`, `manufacturer`,
  `length` and `crew`.
- `model/Quantity`: `Known(value)` or `Unknown`, so an unknown value is never mistaken for a real
  figure. `value` is kept as the source publishes it (`12.5`, `1,600`, `30-165`).
- `model/SearchQuery`: the single query rule. `SearchQuery.from(raw)` trims outer whitespace and
  returns `null` when nothing is left.
- `model/StarshipException`: `Connectivity`, `NotFound`, `Unexpected`. Cancellation is never wrapped.
- `repository/StarshipRepository`: contract with `suspend` functions, implemented in `:data`.

## Planned

- PR 2: `SearchStarships` (blank text → empty list without calling the repository).
- PR 3: `GetStarship`.

## Rules

- Stateless use cases with a single `operator fun invoke` and constructor dependencies; they are
  registered in Koin from `:presentation`, not here.
- Unit tests with a mocked repository: blank query without calls, normalisation and returned results.
