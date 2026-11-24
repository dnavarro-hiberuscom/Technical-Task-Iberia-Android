# :data · module guide

Read the root guide first (`../AGENTS.md`). Depends only on `:domain`.

## Public surface

Only `di/dataModule`. Everything else (Retrofit service, DTOs, mapping, cache, clock,
repository) is `internal`.

## Implemented (PR 2)

- `network/StarshipService`: Retrofit, `GET starships/?search=…` (first page only, by decision) and
  `GET starships/{id}/`, both `suspend`, so cancelling the coroutine cancels the HTTP call.
  `SwapiBaseUrl = https://swapi.dev/api/`.
- `network/StarshipDto`, `StarshipMapper`: internal DTOs with the used fields (`ignoreUnknownKeys`);
  the id is extracted from `url`; `unknown`, `n/a` or blank → `Quantity.Unknown`; figures are kept
  as published (`1,600`, `30-165`).
- `repository/StarshipRepositoryImpl`: owns the cache and translates errors. `IOException` →
  `Connectivity` (after `ensureActive()`, so a cancelled call is not reported as a failure), HTTP
  404 → `NotFound`, other HTTP errors and `SerializationException` → `Unexpected`.
  `CancellationException` is never caught.
- `cache/SearchCache`: in memory, key = `SearchQuery.value.lowercase()`. `SearchCacheTtl = 3.minutes`
  from the moment a successful response arrives; an entry is valid while `age < SearchCacheTtl`.
  Empty lists are stored; errors and cancellations are not. Time comes from an injected
  monotonic `TimeSource` (`TimeSource.Monotonic` in production, `TestTimeSource` in tests).
- `di/dataModule`: `OkHttpClient`, `StarshipService` and `StarshipRepository` as `single`.

## Tests

Deterministic and offline (`src/test`): `FakeStarshipService` + `TestTimeSource`. Covered: two equivalent
searches → one call; different queries → separate entries; hit just before expiry; new call
exactly at 3 minutes; reuse of the refreshed value; retry after a failure.
