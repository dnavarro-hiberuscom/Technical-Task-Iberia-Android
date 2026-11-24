package es.technical_task_iberia_android.data.network

import kotlinx.serialization.Serializable

/** One starship as SWAPI publishes it. Only the fields the app uses are declared. */
@Serializable
internal data class StarshipDto(
    val name: String,
    val model: String,
    val manufacturer: String,
    val length: String,
    val crew: String,
    /** Resource URL, e.g. `https://swapi.dev/api/starships/12/`; the id is its last segment. */
    val url: String,
)

/** A page of `GET /starships/?search=…`. Only the first page is used. */
@Serializable
internal data class StarshipPageDto(
    val results: List<StarshipDto>,
)
