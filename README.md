# Starships

Starships es una app Android para buscar naves de Star Wars y consultar sus fichas. Está hecha con Kotlin, Jetpack Compose y Material 3. Para la prueba técnica me centré en que la búsqueda respondiera bien mientras escribes, en separar las responsabilidades del código y en poder comprobar la cancelación y la caché con pruebas.

## API

| Acción | Petición |
|---|---|
| Buscar por nombre o modelo | `GET https://swapi.dev/api/starships/?search=wing` |
| Cargar otra página | `GET https://swapi.dev/api/starships/?search=e&page=2` |
| Consultar la ficha por ID | `GET https://swapi.dev/api/starships/{id}/` |

- Uso los campos `name`, `model`, `manufacturer`, `length`, `crew` y `url`. El ID se obtiene del último segmento de `url` (`…/starships/12/` → `12`).
- La lista muestra primero hasta diez coincidencias. Si SWAPI tiene más resultados, puedes pulsar **Cargar más** al final. La app conserva las filas visibles mientras añade la siguiente página y permite reintentar si la carga falla.
- SWAPI publica valores desconocidos como `unknown` o `n/a` y algunas cifras con separadores o rangos (`1,600`, `30-165`). El modelo usa `Quantity.Known(value)` y `Quantity.Unknown`. La interfaz muestra los valores desconocidos como texto.

### Imágenes de las naves

SWAPI devuelve los datos de las naves y sus respuestas carecen de URLs de imagen. En Reddit encontré **Star Wars Gallery**, el CDN comunitario de Sergio Coitiño. Sus archivos usan los mismos IDs que SWAPI, así que puedo cargar la imagen correspondiente tanto en la lista como en el detalle. Por ejemplo, al X-wing le corresponde `12.jpg` en `https://starwarsgallery.netlify.app/assets/starships/{id}.jpg`. [Documentación del autor](https://dev.to/sergiocoitino/free-images-for-swapi-the-star-wars-api-no-api-key-cors-enabled-3873) · [Publicación original en Reddit](https://www.reddit.com/r/learnjavascript/comments/1xirlvc/i_built_a_free_image_api_for_swapi_the_star_wars/)

Coil carga y guarda las imágenes en su propia caché. Mientras se descarga una imagen o si falla la petición al CDN, se muestra un icono local de radar y los datos siguen disponibles.

## Requisitos y comandos

- Android Studio compatible con Android Gradle Plugin 9.3 y Android SDK 37. La app se puede instalar desde Android 10 (API 29).
- El proyecto usa el Gradle Wrapper y solicita JDK 25 para el daemon de Gradle. [Configuración del daemon](gradle/gradle-daemon-jvm.properties)
- La app necesita acceso a internet. Las pruebas unitarias se ejecutan con datos controlados.

```bash
./gradlew installDebug                # Compila e instala la app en un dispositivo o emulador
./gradlew test                        # Ejecuta las pruebas unitarias
./gradlew connectedDebugAndroidTest   # Ejecuta las pruebas de Compose en un dispositivo o emulador
./gradlew lint                        # Ejecuta Android Lint
```

## Arquitectura y decisiones

En la [nota de decisiones técnicas](docs/technical-decisions.md) explico por qué elegí Compose, Koin, Retrofit, Navigation Compose y Coil, cómo pasa la búsqueda por cada módulo y qué cubren las pruebas de accesibilidad y de la app.

Las flechas representan dependencias de compilación declaradas con `implementation`.

```text
:app          → :presentation, :data
:presentation → :domain
:data         → :domain
:domain       → ningún otro módulo del proyecto
```

- **`:domain`** es Kotlin puro. Contiene `Starship`, el contrato `StarshipRepository`, los casos de uso `SearchStarships` y `GetStarship`, y las reglas de `SearchQuery`. Las consultas vacías se resuelven dentro del caso de uso.
- **`:data`** implementa el repositorio. Retrofit, los DTO, el mapeo y la caché son `internal`. Su entrada pública es `dataModule`.
- **`:presentation`** contiene las pantallas, los ViewModels, los estados de UI y el tema Material 3.
- **`:app`** inicia Koin, aloja la actividad y monta el grafo de navegación. Se comunica con las pantallas mediante callbacks e IDs de tipo `Int`.

`ArchitectureRulesTest` comprueba que `StarshipsApplication` sea el único punto de acceso a `:data` a través de `dataModule` y que los ViewModels lleguen a los datos mediante casos de uso inyectados por constructor.

**Inyección de dependencias.** `dataModule` registra el cliente de red y el repositorio como `single`. `presentationModule` registra los casos de uso como `factory` y los ViewModels con `viewModelOf`. El dominio queda libre de Koin. `KoinGraphTest` valida el grafo con `verify()`.

**Caché de búsqueda.** Se guarda en memoria durante tres minutos por página y consulta normalizada. La clave usa el número de página y el texto recortado en minúsculas. El tiempo cuenta desde que llega una respuesta correcta. A los tres minutos, la siguiente búsqueda vuelve a pedir los datos. Se guardan las listas vacías si la API respondió correctamente. Los fallos y las cancelaciones se descartan. Las pruebas pueden adelantar el reloj gracias a un `TimeSource` monotónico inyectado.

**Cancelación.** El ViewModel recoge las ediciones con `collectLatest`. La espera de 300 ms va dentro del bloque cancelable, de modo que una edición cancela la petición anterior en cuanto llega. Retrofit transmite la cancelación a la llamada HTTP, el repositorio deja que `CancellationException` se propague y `ensureActive()` evita que una respuesta cancelada modifique el estado. Al editar desaparecen los resultados anteriores y al limpiar el campo se recupera el estado inicial. `SearchViewModelTest` detectaría una regresión si el `debounce` se pusiera antes de `collectLatest`.

**Interfaz y accesibilidad.** Los textos usan recursos en inglés y español. Los colores, la tipografía, las formas y las medidas se centralizan en `presentation/designsystem`. El buscador tiene una etiqueta persistente, las acciones tienen nombres traducidos y áreas táctiles de al menos 48 dp, y cada resultado es un único elemento enfocable.

## Pruebas

Estas dos órdenes ejecutan las pruebas que cubren la caducidad de la caché y la cancelación de búsquedas.

```bash
./gradlew :data:testDebugUnitTest --tests '*StarshipRepositoryCacheTest'
./gradlew :presentation:testDebugUnitTest --tests '*SearchViewModelTest'
```

| Tipo | Módulo | Qué comprueba |
|---|---|---|
| Unitarias con repositorio simulado | `domain` | Consultas vacías sin acceder al repositorio, recorte de espacios, resultados y propagación de fallos. |
| Unitarias con servicio simulado y reloj controlado | `data` | Reutilización para consultas equivalentes, entradas independientes, vigencia justo antes de tres minutos, caducidad al llegar a tres minutos, reutilización del valor actualizado, reintentos y mapeo. |
| Unitarias con tiempo virtual | `presentation` | Cancelación antes de que termine la espera de la nueva búsqueda, descarte de resultados y errores antiguos, limpieza del campo, errores tipados, carga y reintento del detalle. |
| Unitarias | `app` | Grafo de Koin con `verify()` y reglas de arquitectura. |
| Compose en dispositivo o emulador | `presentation`, `app` | Semántica y limpieza del buscador, selección de resultados, campos del detalle y valores desconocidos, paginación hasta la última página y vuelta a la misma búsqueda desde la ficha. |

Las pruebas de Compose usan datos controlados y un recurso local para las imágenes. Los datos de prueba se mantienen en los tests y las vistas previas.

Con la paginación, pasaron la compilación de depuración, las **50 pruebas unitarias**, Android Lint y las **6 pruebas de Compose de la pantalla de búsqueda** en Android 11 (API 30). La última ejecución de la suite de Compose completa, antes de añadir la paginación, pasó en Android 11 y Android 16 (API 36).

También comprobé con SWAPI real que, al buscar `e`, **Cargar más** amplía la lista de 10 a 20 naves. Las comprobaciones manuales incluyeron la apertura del X-wing, la vuelta a la consulta, la limpieza del campo y una búsqueda sin coincidencias. Revisé el diseño en una tablet simulada y en una pantalla compacta con modo oscuro y el tamaño de letra al 160 %. Queda pendiente una comprobación manual con TalkBack.

## Diseño visual

Para el diseño de la app combiné un hangar ilustrado en azul petróleo con un buscador marfil y una lista de color papel. Mientras se carga una búsqueda, la pantalla indica qué consulta está en curso. Si escribes otra, debajo del buscador aparece un aviso con la consulta que se canceló.

--


