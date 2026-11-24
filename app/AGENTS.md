# :app · Guía del módulo

Lee primero [la guía del repositorio](../AGENTS.md).

`:app` ensambla la aplicación. Contiene los siguientes puntos de entrada.

- `StarshipsApplication` inicia Koin con `appModules` (`dataModule` y `presentationModule`). Es el único archivo que importa desde `:data` y usa únicamente `data.di.dataModule`.
- `MainActivity` aplica `StarshipsTheme` una vez y muestra `StarshipsNavHost`.
- `navigation/StarshipsNavHost` define el grafo raíz con rutas seguras en tipos (`@Serializable`). Cada destino llama a un composable público de `:presentation` y le pasa callbacks o IDs simples. `SearchDestination` es el inicio. `DetailDestination(starshipId: Int)` lleva el ID y `entry.toRoute()` lo lee. Al volver se retira el detalle de la pila, de modo que la búsqueda conserva su ViewModel, consulta y resultados.
- `AndroidManifest.xml` declara el permiso `INTERNET` que necesita Data.

## Reglas

- Mantén la lógica de búsqueda, red, caché y repositorios fuera de la actividad y de la navegación.
- Usa callbacks con tipos simples de `:presentation` cuando necesites un valor. Mantén los modelos y casos de uso de `:domain` fuera de `:app`.
- Los textos de pantalla y el tema viven en `:presentation`. Aquí se define `app_name`.
- Para añadir un destino, declara una ruta `@Serializable` y regístrala con `composable<Route> { XxxRoute(...) }`.

## Pruebas

- `src/test/KoinGraphTest` ejecuta `verify()` sobre `appModules`. Declara `Int` como tipo adicional porque `DetailViewModel` recibe el ID con `parametersOf`.
- `src/test/ArchitectureRulesTest` comprueba el acceso a `dataModule` desde `StarshipsApplication`, la separación de `:app` y `:domain`, y que `:presentation` y `:domain` carezcan de referencias a `:data`. Los ViewModels usan casos de uso y modelos, sin acceder al repositorio ni resolver dependencias de Koin.
- `src/androidTest/SearchToDetailFlowTest` usa el grafo raíz y los módulos reales de Koin. Sustituye el repositorio mediante `loadKoinModules`. Usa `androidTestImplementation(project(":domain"))`, una dependencia de pruebas. `build.gradle.kts` incluye una restricción de `concurrent-futures` porque AGP alinea los classpaths de pruebas y ejecución.
