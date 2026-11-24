# :presentation · Guía del módulo

Lee primero [la guía del repositorio](../AGENTS.md). Entre los módulos del proyecto, `:presentation` depende únicamente de `:domain`.

## Estructura

- `designsystem/` reúne el tema Material 3 (`StarshipsTheme`), los colores y sus pares de contraste (`Color.kt`), la tipografía, las formas y las medidas de `Dimens.kt`. Accede al espaciado y a los tamaños con `StarshipsTheme.spacing` y `StarshipsTheme.sizes`.
- `di/presentationModule` registra los casos de uso como `factory` y los ViewModels con `viewModelOf`.
- Cada pantalla tiene su carpeta (`search/` y `detail/`), un `XxxRoute` público para `:app`, un `XxxScreen` interno que se puede previsualizar y probar sin ViewModel, y su ViewModel y estado.

## Compose y MVVM

- El flujo es unidireccional. La pantalla emite eventos como `onQueryChange` y `onClearQuery`. El ViewModel expone un único `StateFlow<XxxUiState>` de solo lectura y el Route lo observa con `collectAsStateWithLifecycle()`.
- El ViewModel recibe los casos de uso por constructor. Mantén el acceso a `StarshipRepository` y la resolución de dependencias de Koin fuera de la clase.
- Los errores llegan como `StarshipException` y se convierten en un estado tipado. La pantalla elige el mensaje con `stringResource`. Los mensajes de excepción quedan fuera de la interfaz.
- Usa los modelos de `:domain` para mostrar el contenido cuando corresponda. La API para `:app` consiste en composables con callbacks e IDs de tipo `Int`.

## Tema, medidas y textos

- Define los colores y las medidas en `dp` o `sp` en `designsystem/`. Añade allí las medidas que falten.
- Guarda cada texto propio de la app en recursos, incluidas las descripciones de contenido y las unidades. Usa argumentos y `plurals` cuando corresponda. Añade cada clave al archivo base en inglés (`res/values/strings.xml`) y a la traducción al español (`res/values-es/strings.xml`). Los datos de SWAPI se muestran tal como llegan.

## Accesibilidad

- Mantén una etiqueta persistente en el buscador, nombres localizados para las acciones de limpiar y volver y un área táctil de al menos `StarshipsTheme.sizes.minTouchTarget`.
- Cada fila de resultados es un único elemento enfocable y pulsable. Para imágenes decorativas, usa `contentDescription = null`.
- Comunica los estados de carga, vacío y error con texto y marca los títulos de pantalla con `heading()`. Comprueba que el contenido siga visible con un tamaño de letra grande.

## Paginación de búsqueda

El ViewModel mantiene los resultados al cargar la siguiente página y distingue la carga y el fallo del botón **Cargar más**. Una nueva edición cancela la carga pendiente y reinicia la lista.

## Imágenes de las naves

`designsystem/StarshipImage` carga la imagen con Coil a partir del ID de SWAPI. Mantén en Presentation tanto la carga desde el CDN como el icono local de radar. Las pruebas de Compose usan el recurso local y respuestas controladas del repositorio.

## Pruebas

- **Unitarias (`src/test`).** ViewModels con `kotlinx-coroutines-test` y tiempo virtual. Casos de uso con repositorios simulados.
- **Compose (`src/androidTest`).** Semántica del buscador, selección de una fila y detalle, con textos leídos de los recursos. Ejecuta `./gradlew :presentation:connectedDebugAndroidTest`.
