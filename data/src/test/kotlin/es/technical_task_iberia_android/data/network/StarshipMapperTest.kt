package es.technical_task_iberia_android.data.network

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.StarshipException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class StarshipMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Trimmed copy of a real SWAPI answer: unused fields must be ignored.
    private val page = """
        {
          "count": 1, "next": null, "previous": null,
          "results": [{
            "name": "X-wing", "model": "T-65 X-wing", "manufacturer": "Incom Corporation",
            "cost_in_credits": "149999", "length": "12.5", "crew": "1", "passengers": "0",
            "pilots": ["https://swapi.dev/api/people/1/"], "films": [],
            "url": "https://swapi.dev/api/starships/12/"
          }]
        }
    """.trimIndent()

    @Test
    fun `maps a SWAPI page and takes the id from the url`() {
        val starship = json.decodeFromString<StarshipPageDto>(page).results.single().toDomain()

        assertEquals(12, starship.id)
        assertEquals("X-wing", starship.name)
        assertEquals("T-65 X-wing", starship.model)
        assertEquals("Incom Corporation", starship.manufacturer)
        assertEquals(Quantity.Known("12.5"), starship.length)
        assertEquals(Quantity.Known("1"), starship.crew)
    }

    @Test
    fun `unknown markers become Quantity Unknown`() {
        assertEquals(Quantity.Unknown, "unknown".toQuantity())
        assertEquals(Quantity.Unknown, "n/a".toQuantity())
        assertEquals(Quantity.Unknown, " ".toQuantity())
    }

    @Test
    fun `figures are kept as published`() {
        assertEquals(Quantity.Known("1,600"), "1,600".toQuantity())
        assertEquals(Quantity.Known("30-165"), "30-165".toQuantity())
    }

    @Test
    fun `id is extracted with or without trailing slash`() {
        assertEquals(9, "https://swapi.dev/api/starships/9/".toStarshipId())
        assertEquals(9, "https://swapi.dev/api/starships/9".toStarshipId())
    }

    @Test
    fun `url without an id is an unexpected failure`() {
        try {
            "https://swapi.dev/api/starships/".toStarshipId()
            fail("Expected StarshipException.Unexpected")
        } catch (expected: StarshipException.Unexpected) {
            // expected
        }
    }
}
