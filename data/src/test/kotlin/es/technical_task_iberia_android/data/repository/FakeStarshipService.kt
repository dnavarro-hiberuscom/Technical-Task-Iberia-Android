package es.technical_task_iberia_android.data.repository

import es.technical_task_iberia_android.data.network.StarshipDto
import es.technical_task_iberia_android.data.network.StarshipPageDto
import es.technical_task_iberia_android.data.network.StarshipService

/** Service double: counts calls per query and answers from a mutable table or throws [failure]. */
internal class FakeStarshipService : StarshipService {

    val searchCalls = mutableListOf<String>()
    val responses = mutableMapOf<String, List<StarshipDto>>()
    var failure: Throwable? = null

    override suspend fun searchStarships(query: String): StarshipPageDto {
        searchCalls += query
        failure?.let { throw it }
        return StarshipPageDto(results = responses[query].orEmpty())
    }

    override suspend fun getStarship(id: Int): StarshipDto {
        failure?.let { throw it }
        return responses.values.flatten().first { it.url.endsWith("/$id/") }
    }
}

internal fun starshipDto(
    name: String,
    id: Int,
    crew: String = "1",
    length: String = "12.5",
) = StarshipDto(
    name = name,
    model = "Model of $name",
    manufacturer = "Maker of $name",
    length = length,
    crew = crew,
    url = "https://swapi.dev/api/starships/$id/",
)
