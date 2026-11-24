# :domain · Guía del módulo

Lee primero [la guía del repositorio](../AGENTS.md).

Este módulo es Kotlin/JVM puro. Mantén las dependencias de Android, Retrofit, Koin y otros módulos del proyecto fuera del dominio.

## Contenido

- `model/Starship` contiene `id` (extraído de la URL de SWAPI), `name`, `model`, `manufacturer`, `length` y `crew`.
- `model/Quantity` distingue `Known(value)` y `Unknown`. `value` conserva el formato de origen (`12.5`, `1,600`, `30-165`).
- `model/SearchQuery` reúne la validación de la consulta. `SearchQuery.from(raw)` quita los espacios de los extremos y devuelve `null` si el resultado queda vacío.
- `model/StarshipException` define `Connectivity`, `NotFound` y `Unexpected`. La cancelación se propaga sin envolverla.
- `repository/StarshipRepository` define el contrato con funciones `suspend` que implementa `:data`.
- `usecase/SearchStarships` construye una `SearchQuery` en `invoke(rawQuery)`. Si queda vacía, devuelve una página vacía sin llamar al repositorio. Para las consultas válidas llama a `repository.searchStarships(query, page)` y devuelve una `StarshipPage` con los resultados y la siguiente página, si existe.
- `usecase/GetStarship` devuelve `repository.getStarship(id)` desde `invoke(id)`.

## Reglas y pruebas

- Los casos de uso carecen de estado, tienen un único `operator fun invoke` y reciben sus dependencias por constructor. Se registran en Koin desde `:presentation`.
- Las pruebas unitarias de `src/test` usan MockK para simular el repositorio y comprobar las consultas vacías sin llamadas, el recorte de espacios, los resultados, las listas vacías y la propagación de fallos.
