# :data · module guide

Read the root guide first (`../AGENTS.md`). Depends only on `:domain`.

## Public surface

Only `di/dataModule`. Everything else (Retrofit service, DTOs, mapping, cache, clock,
repository) is `internal`.

## Planned (PR 2)

- **Network:** Retrofit + kotlinx.serialization against `https://swapi.dev/api/`.
  `GET starships/?search=…` (first page) and `GET starships/{id}/`. `suspend` functions, so
  cancelling the coroutine cancels the HTTP call.
- **DTOs and mapping:** `internal` DTOs with the fields in use; the `id` is extracted from `url`
  (`…/starships/{id}/`). `unknown`, `n/a` or blank → `Quantity.Unknown`.
- **Errors:** `IOException` → `StarshipException.Connectivity`; HTTP 404 → `NotFound`; other HTTP
  errors or unreadable JSON → `Unexpected`. `CancellationException` is always rethrown.
- **Cache:** in the repository, in memory, per normalised query (`SearchQuery.value` lowercased).
  Lifetime `SearchCacheTtl = 3.minutes` from the moment a successful response arrives; an entry
  is valid while `age < SearchCacheTtl`. Empty lists are stored; errors and cancellations are not.
  Once expired, the network is queried and the entry is replaced.
- **Clock:** a monotonic clock (`TimeSource`) is injected so tests can move time forward.
- **Koin:** network client and repository as `single` (the repository owns the cache).

## Tests

Deterministic and offline: fake service + controlled clock. Minimum cases: two equivalent
searches → one call; different queries → separate entries; hit just before expiry; new call
exactly at 3 minutes; reuse of the refreshed value; retry after a failure.
