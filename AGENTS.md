# Starships · Guía del repositorio

App nativa de Android con Kotlin y Jetpack Compose. Busca naves en [SWAPI](https://swapi.dev/documentation) (`/api/starships/`) y muestra su ficha. Para la prueba técnica se priorizan la claridad, la separación de responsabilidades, la cancelación y la caché.

Antes de modificar un módulo, lee esta guía y la del módulo (`<módulo>/AGENTS.md`).

## Mapa de módulos

```text
:app          → :presentation, :data   (solo ensamblaje)
:presentation → :domain
:data         → :domain
:domain       → ningún otro módulo del proyecto
```

| Módulo          | Tipo                           | Contenido                                                                       |
|-----------------|--------------------------------|---------------------------------------------------------------------------------|
| `:app`          | App Android                    | `StarshipsApplication` (Koin), `MainActivity`, `navigation/StarshipsNavHost`    |
| `:presentation` | Biblioteca Android con Compose | Pantallas, ViewModels, estados, `designsystem/` y `di/presentationModule`       |
| `:domain`       | Kotlin/JVM puro                | Modelos, contrato `StarshipRepository` y casos de uso                           |
| `:data`         | Biblioteca Android             | Retrofit, DTO, mapeo, caché y repositorio (todos `internal`), y `di/dataModule` |

`app/src/test/.../ArchitectureRulesTest` comprueba los límites que Gradle no puede aplicar por sí solo.

- En `:app`, solo `StarshipsApplication` accede a `:data` y su única importación es `dataModule`.
- Los ViewModels llegan a los datos mediante casos de uso recibidos por constructor. No usan `StarshipRepository` ni resuelven objetos de Koin con `get()`, `inject()` o `KoinComponent`.
- `:app` se comunica con `:presentation` mediante composables con callbacks e identificadores de tipo `Int`. Evita importar modelos o casos de uso de `:domain` desde `:app`.
- Declara las dependencias entre módulos con `implementation`.

## Flujo

- **Búsqueda.** Campo de texto → `SearchViewModel` (flujo de consultas con `collectLatest` y espera de 300 ms dentro del bloque cancelable) → `SearchStarshipsUseCase` → `StarshipRepository` (caché en memoria de tres minutos por consulta normalizada y página) → `GET /api/starships/?search=…&page=…` cuando hace falta red → lista. **Cargar más** añade otra página. Editar la consulta cancela la carga anterior.
- **Detalle.** Fila seleccionada → `navController` con el ID → `DetailViewModel` → `GetStarshipUseCase` → `GET /api/starships/{id}/`. El ID procede de la URL del recurso.

## Convenciones

- Mantén el código, los comentarios, los commits y las PRs en inglés. El README, la nota técnica y las guías del repositorio están en español. Los textos de la interfaz viven en recursos (consulta [la guía de Presentation](presentation/AGENTS.md)).
- Usa el estilo oficial de Kotlin (`kotlin.code.style=official`) y paquetes bajo `es.technical_task_iberia_android`.
- El [README](README.md) es el punto de entrada para la revisión. Mantén sus comandos y las [decisiones técnicas](docs/technical-decisions.md) al día junto con estas guías.
- Centraliza las versiones de las dependencias en `gradle/libs.versions.toml`. Se conservan las versiones del proyecto (AGP 9.3.0, Kotlin 2.2.10, Compose BOM 2026.02.01 y Gradle 9.6). Son esperables los avisos de Lint sobre versiones más nuevas.
- Los textos que se muestran en pantalla o anuncia TalkBack viven en recursos (`strings.xml`, `plurals`).
- Define los colores y las medidas en `dp` y `sp` en `presentation/designsystem`. Usa una sola constante con nombre y unidad por cada valor significativo, como `SearchDebounce` o `SearchCacheTtl`.
- Guarda el código y los datos de prueba en tests y vistas previas, fuera del código de producción.

## Ramas y PRs

- La rama base es `main`. Usa una rama por PR (`feature/<topic>`). Si la anterior sigue en revisión, crea la siguiente a partir de ella y abre la PR contra esa rama.
- Evita los pushes directos a `main` y los force-push. Escribe los mensajes de commit en inglés y en imperativo.
- En cada PR indica el objetivo, cómo probar el cambio, qué comprobaciones ejecutaste y cualquier limitación. Actualiza estas guías cuando corresponda.

## Validación

Usa el Gradle Wrapper, JDK 25 para el daemon de Gradle y Android SDK 37.

```bash
./gradlew assembleDebug               # Compila todos los módulos
./gradlew test                        # Pruebas unitarias de dominio, datos, ViewModels, Koin y arquitectura
./gradlew lint                        # Android Lint
./gradlew connectedDebugAndroidTest   # Pruebas de Compose en :presentation y :app (dispositivo o emulador)
./gradlew installDebug                # Instala la app
```
