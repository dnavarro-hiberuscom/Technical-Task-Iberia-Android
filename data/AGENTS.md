# :data · Module guide

General rules are in the [root guide](../AGENTS.md). This module depends only on `:domain`.

## Public API

`di/dataModule` is the only public entry point. The Retrofit service, DTOs, mapping, cache, clock and repository are all `internal`.

## Network and cache

- `network/StarshipService` declares the Retrofit requests `GET starships/?search=…&page=…` and `GET starships/{id}/`. Both are `suspend`, so cancelling the coroutine cancels the HTTP request. The base URL is `SwapiBaseUrl = https://swapi.dev/api/`.
- `network/StarshipDto` and `StarshipMapper` only handle the fields the app uses (`ignoreUnknownKeys`). The ID is extracted from `url`. The values `unknown`, `n/a` or empty become `Quantity.Unknown`. Numbers keep their original format (`1,600`, `30-165`).
- `repository/StarshipRepositoryImpl` manages the cache and translates errors. `IOException` becomes `Connectivity` after checking `ensureActive()`, so cancelled calls are excluded. HTTP 404 becomes `NotFound`. Other HTTP errors and `SerializationException` become `Unexpected`. `CancellationException` is propagated.
- `cache/SearchCache` lives in memory and uses `SearchQuery.value.lowercase()` plus the page number as the key. The stored response remembers whether SWAPI reports another page in `next`. `SearchCacheTtl = 3.minutes` starts when a successful response arrives, and the entry is valid while `age < SearchCacheTtl`. Empty lists are cached. Errors and cancellations are discarded. The clock is injected through `TimeSource`: `TimeSource.Monotonic` in production and `TestTimeSource` in tests.
- `di/dataModule` registers `OkHttpClient`, `StarshipService` and `StarshipRepository` as `single`.

## Tests

The tests in `src/test` run without a network, using `FakeStarshipService` and `TestTimeSource`. They check that two equivalent queries make a single call, that different queries have independent entries, that the cache is reused just before it expires, that a new call is made at three minutes, that the refreshed value is reused, and that a failed request can be retried.
