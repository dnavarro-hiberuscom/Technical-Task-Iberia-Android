# :data · Guía del módulo

Lee primero [la guía del repositorio](../AGENTS.md). Entre los módulos del proyecto, `:data` depende únicamente de `:domain`.

## API pública

`di/dataModule` es el punto de entrada público. El servicio Retrofit, los DTO, el mapeo, la caché, el reloj y el repositorio son `internal`.

## Red y caché

- `network/StarshipService` declara las peticiones Retrofit `GET starships/?search=…&page=…` y `GET starships/{id}/`. Las dos son `suspend`, así que cancelar la corrutina cancela la petición HTTP. La URL base es `SwapiBaseUrl = https://swapi.dev/api/`.
- `network/StarshipDto` y `StarshipMapper` trabajan con los campos que usa la app (`ignoreUnknownKeys`). El ID se extrae de `url`. Los valores `unknown`, `n/a` o vacíos se convierten en `Quantity.Unknown`. Las cifras conservan el formato de origen (`1,600`, `30-165`).
- `repository/StarshipRepositoryImpl` gestiona la caché y traduce los errores. `IOException` se convierte en `Connectivity` tras comprobar `ensureActive()`, para excluir llamadas canceladas. HTTP 404 se convierte en `NotFound`. Otros errores HTTP y `SerializationException` se convierten en `Unexpected`. `CancellationException` se propaga.
- `cache/SearchCache` vive en memoria y usa el texto de `SearchQuery.value.lowercase()` y el número de página como clave. La respuesta conserva si SWAPI indica otra página en `next`. `SearchCacheTtl = 3.minutes` empieza cuando llega una respuesta correcta y la entrada es válida mientras `age < SearchCacheTtl`. Se guardan las listas vacías. Los errores y las cancelaciones se descartan. El reloj se inyecta mediante `TimeSource`, con `TimeSource.Monotonic` en producción y `TestTimeSource` en pruebas.
- `di/dataModule` registra `OkHttpClient`, `StarshipService` y `StarshipRepository` como `single`.

## Pruebas

Las pruebas de `src/test` se ejecutan sin red con `FakeStarshipService` y `TestTimeSource`. Comprueban que dos consultas equivalentes hacen una sola llamada, que distintas consultas tienen entradas independientes, que se reutiliza la caché justo antes de caducar, que a los tres minutos se hace una nueva llamada, que se reutiliza el valor actualizado y que se puede reintentar después de un fallo.
