package es.technical_task_iberia_android.data.network

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipException

/** Values SWAPI uses when it does not know a figure. */
private val UnknownMarkers = setOf("unknown", "n/a")

internal fun StarshipDto.toDomain(): Starship = Starship(
    id = url.toStarshipId(),
    name = name,
    model = model,
    manufacturer = manufacturer,
    length = length.toQuantity(),
    crew = crew.toQuantity(),
)

internal fun String.toQuantity(): Quantity {
    val trimmed = trim()
    return if (trimmed.isEmpty() || trimmed.lowercase() in UnknownMarkers) {
        Quantity.Unknown
    } else {
        Quantity.Known(trimmed)
    }
}

/** Extracts `12` from `https://swapi.dev/api/starships/12/`. */
internal fun String.toStarshipId(): Int =
    trimEnd('/').substringAfterLast('/').toIntOrNull()
        ?: throw StarshipException.Unexpected(IllegalArgumentException("No starship id in url: $this"))
