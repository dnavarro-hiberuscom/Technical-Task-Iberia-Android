package es.technical_task_iberia_android.domain.model

/**
 * A starship as the app understands it, independent of how SWAPI encodes it.
 *
 * @property id identifier extracted from the SWAPI resource URL (`/api/starships/{id}/`).
 * @property length length in metres, as published by the source.
 * @property crew number of crew members (the source may publish a range such as `30-165`).
 */
data class Starship(
    val id: Int,
    val name: String,
    val model: String,
    val manufacturer: String,
    val length: Quantity,
    val crew: Quantity,
)
